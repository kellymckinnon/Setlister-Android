package me.kellymckinnon.setlister.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import me.kellymckinnon.setlister.R;
import me.kellymckinnon.setlister.models.Artist;
import me.kellymckinnon.setlister.models.Artists;
import me.kellymckinnon.setlister.network.RetrofitClient;
import me.kellymckinnon.setlister.network.SetlistFMService;
import me.kellymckinnon.setlister.utils.Utility;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragment opened by SetlisterActivity that holds a search bar and displays recent results as well as
 * suggestions as the user types
 */
public class SearchFragment extends Fragment {

  private static final int TRIGGER_SEARCH = 1;
  private static final long SEARCH_DELAY_IN_MS = 500;
  private static final int NUM_RECENT_SEARCHES = 5;

  private ProgressBar mLoadingSpinner;
  private EditText mSearchEditText;
  private ListView mSuggestionListView;
  private ArrayAdapter<String> mSuggestionListAdapter;
  private HashMap<String, String> mNameToIdMap;
  private View mRootView;
  private TextView mNoResultsTextView;
  private TextView mNoConnectionTextView;
  private TextView mSuggestionsHeader;
  private TextView mNoRecentSearchesText;
  private ArrayList<String> mRecentSearchesList;
  private SetlistFMService mSetlistFMService;
  private OnArtistSelectedListener mOnArtistSelectedListener;
  private String mCurrentSearch;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mSetlistFMService = RetrofitClient.getSetlistFMService();
  }

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    if (mRootView != null) {
      return mRootView;
    }

    mRootView = inflater.inflate(R.layout.fragment_search, container, false);

    mSuggestionListView = mRootView.findViewById(R.id.suggestion_list);
    mLoadingSpinner = mRootView.findViewById(R.id.loading_suggestions);
    mNoResultsTextView = mRootView.findViewById(R.id.no_results_text);
    mNoConnectionTextView = mRootView.findViewById(R.id.no_connection_text);
    mSearchEditText = mRootView.findViewById(R.id.search_bar);
    mSuggestionsHeader = mRootView.findViewById(R.id.suggestion_header);
    mNoRecentSearchesText = mRootView.findViewById(R.id.no_searches_text);

    final ImageButton clearSearchButton = mRootView.findViewById(R.id.clear_search);
    clearSearchButton.setOnClickListener(
        new ImageButton.OnClickListener() {
          @Override
          public void onClick(View v) {
            mSearchEditText.setText("");
          }
        });

    updateRecentSearchesList();
    showRecentSearchesView();

    // Search when enter is pressed
    mSearchEditText.setOnKeyListener(
        new View.OnKeyListener() {
          @Override
          public boolean onKey(View v, int keyCode, KeyEvent event) {
            String text = mSearchEditText.getText().toString();

            // We only want to do this once
            if (event.getAction() != KeyEvent.ACTION_UP) {
              return false;
            }

            if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == EditorInfo.IME_ACTION_SEARCH)
                && text.length() != 0) {
              String formattedQuery = Utility.capitalizeFirstLetters(text);
              addRecentSearch(formattedQuery, "0");
              mOnArtistSelectedListener.onArtistSelected(formattedQuery, "0" /* artistId */);
              return true;
            }
            return false;
          }
        });

    // Populate list with recent searches
    mSuggestionListAdapter = new ArrayAdapter<>(getActivity(), R.layout.single_line_list_row);
    mSuggestionListView.setAdapter(mSuggestionListAdapter);
    mSuggestionListAdapter.addAll(mRecentSearchesList);

    // Search for the clicked suggestion/recent search
    mSuggestionListView.setOnItemClickListener(
        new AdapterView.OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String query = (String) mSuggestionListView.getItemAtPosition(position);
            String searchId = mNameToIdMap.get(query);
            addRecentSearch(query, searchId);
            mOnArtistSelectedListener.onArtistSelected(query, "0" /* artistId */);
          }
        });

    return mRootView;
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);

    try {
      mOnArtistSelectedListener = (OnArtistSelectedListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(context.toString() + " must implement OnArtistSelectedListener");
    }
  }

  @Override
  public void onResume() {
    super.onResume();

    ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(false);
    actionBar.setTitle(R.string.app_name);
    actionBar.setSubtitle(null);

    // Don't search until user has stopped typing
    final Handler handler =
        new Handler() {
          @Override
          public void handleMessage(Message msg) {
            if (msg.what == TRIGGER_SEARCH && mSearchEditText.getText().toString().length() != 0) {
              showLoadingView();
              startSearch();
            }
          }
        };

    mSearchEditText.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {}

          @Override
          public void afterTextChanged(Editable s) {
            if (s.length() == 0) { // Empty search, show recent searches
              showRecentSearchesView();

              // Stop any current searches
              handler.removeMessages(TRIGGER_SEARCH);

              if (mSuggestionListAdapter != null) {
                mSuggestionListAdapter.clear();
                mSuggestionListAdapter.addAll(mRecentSearchesList);
              }
            } else { // There is a query, get suggestions
              showLoadingView();
              handler.removeMessages(TRIGGER_SEARCH);
              handler.sendEmptyMessageDelayed(TRIGGER_SEARCH, SEARCH_DELAY_IN_MS);
            }
          }
        });

    updateRecentSearchesList();

    if (mSuggestionListAdapter != null
        && mSuggestionsHeader != null
        && mSuggestionsHeader.getText().equals(getString(R.string.recent_searches_header))) {
      mSuggestionListAdapter.clear();
      mSuggestionListAdapter.addAll(mRecentSearchesList);
    }
  }

  private void updateRecentSearchesList() {
    SharedPreferences recentFile =
        getActivity().getSharedPreferences(getString(R.string.prefs_name), Context.MODE_PRIVATE);

    mRecentSearchesList = new ArrayList<>();

    if (mNameToIdMap == null) {
      mNameToIdMap = new HashMap<>();
    }

    // Recent searches are stored as search0, search1, search2, etc up to search4 (5 maximum)
    for (int i = 0; i < NUM_RECENT_SEARCHES; i++) {
      if (recentFile.contains("search" + i)) {
        String query = recentFile.getString("search" + i, "");
        String id = recentFile.getString("id" + i, "0");
        mRecentSearchesList.add(query);
        mNameToIdMap.put(query, id);
      }
    }
  }

  /** Initiate a search of the selected type */
  private void startSearch() {
    if (mCurrentSearch != null && mCurrentSearch.equals(mSearchEditText.getText().toString())) {
      return; // We're already performing the correct search
    }

    mSetlistFMService.getArtists(mCurrentSearch).cancel();

    mCurrentSearch = mSearchEditText.getText().toString();

    mSetlistFMService
        .getArtists(mSearchEditText.getText().toString())
        .enqueue(
            new Callback<Artists>() {
              @Override
              public void onResponse(Call<Artists> call, Response<Artists> response) {
                if (!response.isSuccessful() || response.body() == null) {
                  Log.e(
                      this.getClass().getSimpleName(),
                      "Artists search failed. Response code was: "
                          + response.code()
                          + ". Message was: "
                          + response.errorBody());
                  showNullState();
                  return;
                }

                if (response.body().getTotal() == 0) { // No results
                  showNullState();
                  return;
                }

                mSuggestionListAdapter.clear();

                List<Artist> artists = response.body().getArtist();
                for (Artist artist : artists) {
                  mNameToIdMap.put(artist.getName(), artist.getMbid());
                  mSuggestionListAdapter.add(artist.getName());
                }

                mLoadingSpinner.setVisibility(View.GONE);
                mSuggestionListView.setVisibility(View.VISIBLE);
                mSuggestionListAdapter.notifyDataSetChanged();
              }

              @Override
              public void onFailure(Call<Artists> call, Throwable t) {
                Log.e(getClass().getSimpleName(), t.toString());
                showNullState();
              }
            });
  }

  private void showLoadingView() {
    mSuggestionsHeader.setText(getString(R.string.best_matches_header));
    mNoResultsTextView.setVisibility(View.GONE);
    mNoConnectionTextView.setVisibility(View.GONE);
    mLoadingSpinner.setVisibility(View.VISIBLE);
    mSuggestionListView.setVisibility(View.GONE);
    mNoRecentSearchesText.setVisibility(View.GONE);
  }

  private void showRecentSearchesView() {
    mSuggestionsHeader.setText(getString(R.string.recent_searches_header));
    mLoadingSpinner.setVisibility(View.GONE);
    mNoResultsTextView.setVisibility(View.GONE);
    mNoConnectionTextView.setVisibility(View.GONE);

    if (mRecentSearchesList.isEmpty()) {
      mSuggestionListView.setVisibility(View.GONE);
      mNoRecentSearchesText.setVisibility(View.VISIBLE);
    } else {
      mSuggestionListView.setVisibility(View.VISIBLE);
    }
  }

  private void showNullState() {
    mLoadingSpinner.setVisibility(View.GONE);
    mSuggestionListView.setVisibility(View.GONE);
    if (getActivity() != null && Utility.isNetworkConnected(getActivity())) {
      mNoResultsTextView.setVisibility(View.VISIBLE);
    } else {
      mNoConnectionTextView.setVisibility(View.VISIBLE);
    }
  }

  /**
   * Add the given search to the top of the recent searches list, pushing off the oldest entry. Both
   * names and ids are stored, under search0 (most recent), search1, ... and id0, id1, ... up to a
   * maximum of five entries.
   *
   * <p>If the id for a recent search is 0, this indicates that the search was done via the enter
   * key and since no specific result was chosen, there is no associated id.
   */
  private void addRecentSearch(String query, String id) {
    if (getActivity() == null) {
      return;
    }

    SharedPreferences recentFile =
        getActivity().getSharedPreferences(getString(R.string.prefs_name), Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = recentFile.edit();

    for (String s : mRecentSearchesList) {
      if (s.equalsIgnoreCase(query)) {
        // TODO: Instead of doing nothing, move the search to the top
        // And replace the ID if necessary
        return;
      }
    }

    for (int i = NUM_RECENT_SEARCHES - 2; i >= 0; i--) {
      if (recentFile.contains("search" + i)) {
        editor.putString("search" + (i + 1), recentFile.getString("search" + i, ""));
        editor.putString("id" + (i + 1), recentFile.getString("id" + i, "0"));
      }
    }

    editor.putString("search" + 0, query);
    editor.putString("id" + 0, id);
    editor.apply();
  }

  /**
   * Callback to inform the activity that the user has selected an artist and we should display
   * relevant setlists for that artist.
   */
  public interface OnArtistSelectedListener {
    // TODO: Change artistId to be @Nullable, instead of passing "0" everywhere

    /**
     * Notifies listener that artist has been selected
     *
     * @param artistName query, like "The Killers"
     * @param artistId ID for the artist, if available. Due to the way setlist.fm works, sometimes
     *     we do not have the ID, only the name.
     */
    void onArtistSelected(String artistName, String artistId);
  }
}
