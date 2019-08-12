package me.kellymckinnon.setlister.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import me.kellymckinnon.setlister.R;
import me.kellymckinnon.setlister.SetlisterConstants;
import me.kellymckinnon.setlister.models.Set;
import me.kellymckinnon.setlister.models.Setlist;
import me.kellymckinnon.setlister.models.Setlists;
import me.kellymckinnon.setlister.models.Show;
import me.kellymckinnon.setlister.models.Song;
import me.kellymckinnon.setlister.network.RetrofitClient;
import me.kellymckinnon.setlister.network.SetlistFMService;
import me.kellymckinnon.setlister.utils.Utility;
import me.kellymckinnon.setlister.views.ShowAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Uses the passed in query (either an artist, venue, or city) to search the setlist.fm database for
 * shows, and displays them in a list that contains the artist, date, location, tour, and number of
 * songs in the setlist for that show.
 */
public class ListingFragment extends Fragment {

  private LinearLayoutManager mLinearLayoutManager;
  private RecyclerView mRecyclerView;
  private String mQuery;
  private TextView mNoShowsTextView;
  private ProgressBar mLoadingShowsProgressBar;
  private int mNumPagesLoaded;
  private int mFirstVisibleItemIndex, mVisibleItemCount, mTotalItemCount;
  private boolean mIsLoading = false;
  private ShowAdapter mAdapter;
  private int mNumSetlistPages; // Number of pages of setlists from API
  private String mArtistId;
  private SetlistFMService mSetlistFMService;
  private OnSetlistSelectedListener mOnSetlistSelectedListener;

  public static ListingFragment newInstance(String artistName, String artistId) {
    ListingFragment listingFragment = new ListingFragment();
    Bundle args = new Bundle();
    args.putString(SetlisterConstants.EXTRA_ARTIST_NAME, artistName);

    // TODO: Change artistId to be @Nullable, instead of passing "0" everywhere
    if (!artistId.equals("0")) {
      args.putString(SetlisterConstants.EXTRA_ARTIST_ID, artistId);
    }

    listingFragment.setArguments(args);
    return listingFragment;
  }

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    mSetlistFMService = RetrofitClient.getSetlistFMService();

    View rootView = inflater.inflate(R.layout.fragment_listing, container, false);
    mQuery = getArguments().getString(SetlisterConstants.EXTRA_ARTIST_NAME);
    mArtistId = getArguments().getString(SetlisterConstants.EXTRA_ARTIST_ID);
    mNoShowsTextView = rootView.findViewById(R.id.no_shows);
    mLoadingShowsProgressBar = rootView.findViewById(R.id.loading_shows);
    mRecyclerView = rootView.findViewById(R.id.show_list);
    DividerItemDecoration decoration =
        new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
    mRecyclerView.addItemDecoration(decoration);
    mLinearLayoutManager = new LinearLayoutManager(getActivity());

    mFirstVisibleItemIndex = 0;
    mVisibleItemCount = 0;
    mTotalItemCount = 0;

    mAdapter = new ShowAdapter(mOnSetlistSelectedListener);
    mRecyclerView.setAdapter(mAdapter);
    mRecyclerView.setHasFixedSize(true);
    mRecyclerView.setLayoutManager(mLinearLayoutManager);
    mRecyclerView.setItemAnimator(new DefaultItemAnimator());

    /* Load new page(s) if the user scrolls to the end */
    mRecyclerView.addOnScrollListener(
        new RecyclerView.OnScrollListener() {
          @Override
          public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {

            mVisibleItemCount = mLinearLayoutManager.getChildCount();
            mTotalItemCount = mLinearLayoutManager.getItemCount();
            mFirstVisibleItemIndex = mLinearLayoutManager.findFirstVisibleItemPosition();

            if (!mIsLoading && mNumPagesLoaded < mNumSetlistPages) {
              if ((mVisibleItemCount + mFirstVisibleItemIndex) >= mTotalItemCount - 10) {
                getSetlists();
              }
            }
          }
        });

    getSetlists();

    return rootView;
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);

    try {
      mOnSetlistSelectedListener = (OnSetlistSelectedListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(
          context.toString() + " must implement OnSetlistSelectedListener");
    }
  }

  @Override
  public void onResume() {
    super.onResume();

    ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);
    actionBar.setTitle(getArguments().getString(SetlisterConstants.EXTRA_ARTIST_NAME));
    actionBar.setSubtitle(null);
  }

  private void getSetlists() {
    mIsLoading = true;
    Callback<Setlists> callback =
        new Callback<Setlists>() {
          @Override
          public void onResponse(Call<Setlists> call, Response<Setlists> response) {
            // If artist has no setlists, request will return 404
            if (!response.isSuccessful() || response.body() == null) {
              showNullState();
              return;
            }

            // On first run, calculate total number of pages
            if (mNumPagesLoaded == 0) {
              int numShows = response.body().getTotal();

              // One 20-item array per page
              mNumSetlistPages = numShows / 20;
              if (numShows % 20 != 0) {
                mNumSetlistPages++;
              }
            }

            List<Setlist> setlists = response.body().getSetlist();
            for (Setlist setlist : setlists) {
              Show show = getShowModel(setlist);
              if (show == null) { // No setlists available for this show
                continue;
              }

              mAdapter.add(show);
            }

            mNumPagesLoaded++;

            if (mAdapter.getItemCount() > 0) {
              mLoadingShowsProgressBar.setVisibility(View.GONE);
              mRecyclerView.setVisibility(View.VISIBLE);
            } else {
              showNullState();
            }
            mIsLoading = false;
          }

          @Override
          public void onFailure(Call<Setlists> call, Throwable t) {
            Log.e(getClass().getSimpleName(), t.toString());
            showNullState();
          }
        };

    if (mArtistId != null) { // We have an MBID, use that
      mSetlistFMService.getSetlistsByArtistMbid(mArtistId, mNumPagesLoaded + 1).enqueue(callback);
    } else { // We have a plaintext name, use that
      mSetlistFMService
          .getSetlistsByArtistName(mQuery, mNumPagesLoaded + 1)
          .enqueue(callback);
    }
  }

  /** Move data from JSON response into Show objects */
  private Show getShowModel(Setlist setlist) {
    String tour =
        setlist.getTour() == null || setlist.getTour().getName().equals("No Tour Assigned")
            ? ""
            : setlist.getTour().getName();

    String year = setlist.getEventDate().substring(6);
    String day = setlist.getEventDate().substring(0, 2);
    String month = setlist.getEventDate().substring(3, 5);
    String date = month + "/" + day + "/" + year;

    String venue;
    try {
      StringBuilder builder = new StringBuilder();
      builder.append(setlist.getVenue().getName());
      builder.append(", ");
      builder.append(setlist.getVenue().getCity().getName());
      builder.append(", ");
      builder.append(setlist.getVenue().getCity().getStateCode());
      builder.append(", ");
      builder.append(setlist.getVenue().getCity().getCountry().getCode());

      venue = builder.toString();
    } catch (Exception e) {
      venue = getString(R.string.unknown_venue);
    }

    String[] songs;
    try {
      List<Set> sets = setlist.getSets().getSet();
      ArrayList<String> songList = new ArrayList<>();

      // Combine all of the sets into one for viewing
      for (Set set : sets) {
        for (Song song : set.getSong()) {
          if (!song.getName().isEmpty()) {
            songList.add(song.getName());
          }
        }
      }

      songs = songList.toArray(new String[0]);
    } catch (Exception e) {
      // Usually, this just means there are no songs in the setlist, and "sets"
      // is an empty string instead of an object.

      e.printStackTrace();
      return null;
    }

    return new Show.Builder()
        .setBand(setlist.getArtist().getName())
        .setTour(tour)
        .setDate(date)
        .setVenue(venue)
        .setSongs(songs)
        .createShow();
  }

  private void showNullState() {
    mLoadingShowsProgressBar.setVisibility(View.GONE);
    if (!Utility.isNetworkConnected(getActivity())) {
      mNoShowsTextView.setText(R.string.no_connection); // Because there's no signal
    } // Or because there are no shows for that query
    mNoShowsTextView.setVisibility(View.VISIBLE);
    mRecyclerView.setVisibility(View.GONE);
  }

  /**
   * Callback to inform the activity that the user has selected a setlist and we should display
   * relevant details for that setlist.
   */
  public interface OnSetlistSelectedListener {

    /** Notifies listener that setlist has been selected */
    void onSetlistSelected(Show show);
  }
}
