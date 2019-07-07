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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import me.kellymckinnon.setlister.R;
import me.kellymckinnon.setlister.models.Set;
import me.kellymckinnon.setlister.models.Setlist;
import me.kellymckinnon.setlister.models.Setlists;
import me.kellymckinnon.setlister.models.Show;
import me.kellymckinnon.setlister.models.Song;
import me.kellymckinnon.setlister.network.RetrofitClient;
import me.kellymckinnon.setlister.network.SetlistFMService;
import me.kellymckinnon.setlister.utils.Utility;
import me.kellymckinnon.setlister.views.RecyclerViewDivider;
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

  private LinearLayoutManager llm;
  private RecyclerView rv;
  private String query;
  private TextView noShows;
  private ProgressBar loadingShows;
  private int pagesLoaded;
  private int firstVisibleItem, visibleItemCount, totalItemCount;
  private boolean loading = false;
  private ShowAdapter adapter;
  private int numPages; // Number of pages of setlists from API
  private String id;
  private SetlistFMService mService;
  private OnSetlistSelectedListener mOnSetlistSelectedListener;

  /**
   * Callback to inform the activity that the user has selected a setlist and we should display
   * relevant details for that setlist.
   */
  public interface OnSetlistSelectedListener {

    /** Notifies listener that setlist has been selected */
    // TODO: Refactor to just take a POJO object instead of all these strings
    void onSetlistSelected(String band, String venue, String date, String tour, String[] setlist);
  }

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // TODO: Handle title change

    mService = RetrofitClient.getSetlistFMService();

    View rootView = inflater.inflate(R.layout.fragment_listing, container, false);
    query = getArguments().getString("QUERY");
    id = getArguments().getString("ID");
    noShows = rootView.findViewById(R.id.no_shows);
    loadingShows = rootView.findViewById(R.id.loading_shows);
    rv = rootView.findViewById(R.id.show_list);
    rv.addItemDecoration(
        new me.kellymckinnon.setlister.views.RecyclerViewDivider(
            getActivity(), RecyclerViewDivider.VERTICAL_LIST));
    llm = new LinearLayoutManager(getActivity());

    firstVisibleItem = 0;
    visibleItemCount = 0;
    totalItemCount = 0;

    adapter = new ShowAdapter(mOnSetlistSelectedListener);
    rv.setAdapter(adapter);
    rv.setHasFixedSize(true);
    rv.setLayoutManager(llm);
    rv.setItemAnimator(new DefaultItemAnimator());

    /* Load new page(s) if the user scrolls to the end */
    rv.addOnScrollListener(
        new RecyclerView.OnScrollListener() {
          @Override
          public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {

            visibleItemCount = llm.getChildCount();
            totalItemCount = llm.getItemCount();
            firstVisibleItem = llm.findFirstVisibleItemPosition();

            if (!loading && pagesLoaded < numPages) {
              if ((visibleItemCount + firstVisibleItem) >= totalItemCount - 10) {
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

  private void getSetlists() {
    loading = true;
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
            if (pagesLoaded == 0) {
              int numShows = response.body().getTotal();

              // One 20-item array per page
              numPages = numShows / 20;
              if (numShows % 20 != 0) {
                numPages++;
              }
            }

            List<Setlist> setlists = response.body().getSetlist();
            for (Setlist setlist : setlists) {
              Show show = getShowModel(setlist);
              if (show == null) { // No setlists available for this show
                continue;
              }

              adapter.add(show);
            }

            pagesLoaded++;

            if (adapter.getItemCount() > 0) {
              loadingShows.setVisibility(View.GONE);
              rv.setVisibility(View.VISIBLE);
            } else {
              showNullState();
            }
            loading = false;
          }

          @Override
          public void onFailure(Call<Setlists> call, Throwable t) {
            Log.e(getClass().getSimpleName(), t.toString());
            showNullState();
          }
        };

    if (id != null) { // We have an MBID, use that
      mService.getSetlistsByArtistMbid(id, pagesLoaded + 1).enqueue(callback);
    } else { // We have a plaintext name, use that
      try {
        mService
            .getSetlistsByArtistName(URLEncoder.encode(query, "UTF-8"), pagesLoaded + 1)
            .enqueue(callback);
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
        showNullState();
      }
    }
  }

  /** Move data from JSON response into Show objects */
  private Show getShowModel(Setlist setlist) {
    Show show = new Show();

    show.band = setlist.getArtist().getName();
    show.tour =
        setlist.getTour() == null || setlist.getTour().getName().equals("No Tour Assigned")
            ? ""
            : setlist.getTour().getName();

    String year = setlist.getEventDate().substring(6);
    String day = setlist.getEventDate().substring(0, 2);
    String month = setlist.getEventDate().substring(3, 5);
    show.date = month + "/" + day + "/" + year;

    try {
      show.venue =
          setlist.getVenue().getName()
              + ", "
              + setlist.getVenue().getCity().getName()
              + ", "
              + setlist.getVenue().getCity().getStateCode()
              + ", "
              + setlist.getVenue().getCity().getCountry().getCode();
    } catch (Exception e) {
      show.venue = "Unknown venue";
    }

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

      show.setlist = songList.toArray(new String[0]);
    } catch (Exception e) {
      // Usually, this just means there are no songs in the setlist, and "sets"
      // is an empty string instead of an object.

      e.printStackTrace();
      return null;
    }
    return show;
  }

  private void showNullState() {
    loadingShows.setVisibility(View.GONE);
    if (!Utility.isNetworkConnected(getActivity())) {
      noShows.setText(R.string.no_connection); // Because there's no signal
    } // Or because there are no shows for that query
    noShows.setVisibility(View.VISIBLE);
    rv.setVisibility(View.GONE);
  }
}
