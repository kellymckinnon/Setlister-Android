package me.kellymckinnon.setlister.fragments;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.content.Context;
import android.content.Intent;
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
import android.widget.*;
import me.kellymckinnon.setlister.ListingActivity;
import me.kellymckinnon.setlister.R;
import me.kellymckinnon.setlister.models.Artist;
import me.kellymckinnon.setlister.models.Artists;
import me.kellymckinnon.setlister.models.Setlists;
import me.kellymckinnon.setlister.network.RetrofitClient;
import me.kellymckinnon.setlister.network.SetlistFMService;
import me.kellymckinnon.setlister.utils.Utility;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Fragment opened by SearchActivity that holds a search bar and displays recent results as well as
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
  private TextView mNoResultsTextView;
  private TextView mNoConnectionTextView;
  private TextView mSuggestionsHeader;
  private ArrayList<String> mRecentSearchesList;
  private SetlistFMService mSetlistFMService;

  @Override
  public View onCreateView(
          @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_search, container, false);

    mSetlistFMService = RetrofitClient.getSetlistFMService();

    mSuggestionListView = rootView.findViewById(R.id.suggestion_list);
    mLoadingSpinner = rootView.findViewById(R.id.loading_suggestions);
    mNoResultsTextView = rootView.findViewById(R.id.no_results_text);
    mNoConnectionTextView = rootView.findViewById(R.id.no_connection_text);
    mSearchEditText = rootView.findViewById(R.id.search_bar);
    mSuggestionsHeader = rootView.findViewById(R.id.suggestion_header);

    final TextView noRecentSearchesText = rootView.findViewById(R.id.no_searches_text);

    final ImageButton clearSearchButton = rootView.findViewById(R.id.clear_search);
    clearSearchButton.setOnClickListener(
        new ImageButton.OnClickListener() {
          @Override
          public void onClick(View v) {
            mSearchEditText.setText("");
          }
        });

    SharedPreferences recentFile =
        getActivity().getSharedPreferences(getString(R.string.prefs_name), Context.MODE_PRIVATE);
    mRecentSearchesList = new ArrayList<>();
    mNameToIdMap = new HashMap<>();

    // Recent searches are stored as search0, search1, search2, etc up to search4 (5 maximum)
    for (int i = 0; i < NUM_RECENT_SEARCHES; i++) {
      if (recentFile.contains("search" + i)) {
        String query = recentFile.getString("search" + i, "");
        String id = recentFile.getString("id" + i, "0");
        mRecentSearchesList.add(query);
        mNameToIdMap.put(query, id);
      }
    }

    if (mRecentSearchesList.isEmpty()) {
      noRecentSearchesText.setVisibility(View.VISIBLE);
      mSuggestionListView.setVisibility(View.GONE);
    }

    // Don't search until user has stopped typing
    final Handler handler =
        new Handler() {
          @Override
          public void handleMessage(Message msg) {
            if (msg.what == TRIGGER_SEARCH && mSearchEditText.getText().toString().length() != 0) {
              mNoResultsTextView.setVisibility(View.GONE);
              mNoConnectionTextView.setVisibility(View.GONE);
              mLoadingSpinner.setVisibility(View.VISIBLE);
              mSuggestionListView.setVisibility(View.GONE);

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
              mSuggestionsHeader.setText(getString(R.string.recent_searches_header));
              mLoadingSpinner.setVisibility(View.GONE);
              mNoResultsTextView.setVisibility(View.GONE);
              mNoConnectionTextView.setVisibility(View.GONE);

              if (mRecentSearchesList.isEmpty()) {
                mSuggestionListView.setVisibility(View.GONE);
                noRecentSearchesText.setVisibility(View.VISIBLE);
              } else {
                mSuggestionListView.setVisibility(View.VISIBLE);
              }

              // Stop any current searches
              handler.removeMessages(TRIGGER_SEARCH);

              if (mSuggestionListAdapter != null) {
                mSuggestionListAdapter.clear();
                mSuggestionListAdapter.addAll(mRecentSearchesList);
              }
            } else { // There is a query, get suggestions
              noRecentSearchesText.setVisibility(View.GONE);
              mNoResultsTextView.setVisibility(View.GONE);
              mNoConnectionTextView.setVisibility(View.GONE);
              mLoadingSpinner.setVisibility(View.VISIBLE);
              mSuggestionListView.setVisibility(View.GONE);
              mSuggestionsHeader.setText(getString(R.string.best_matches_header));
              handler.removeMessages(TRIGGER_SEARCH);
              handler.sendEmptyMessageDelayed(TRIGGER_SEARCH, SEARCH_DELAY_IN_MS);
            }
          }
        });

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
              Intent intent = new Intent(getActivity(), ListingActivity.class);
              String formattedQuery = Utility.capitalizeFirstLetters(text);
              intent.putExtra("QUERY", formattedQuery);
              addRecentSearch(formattedQuery, "0");
              startActivity(intent);
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

            Intent intent = new Intent(getActivity(), ListingActivity.class);
            intent.putExtra("QUERY", query);
            if (!searchId.equals("0")) {
              intent.putExtra("ID", searchId);
            }
            startActivity(intent);
          }
        });

    return rootView;
  }

  @Override
  public void onResume() {
    super.onResume();

    // If previous search (still showing) was done before results finished, re-search
    if (mSearchEditText != null
        && mSearchEditText.getText().toString().length() != 0
        && mLoadingSpinner != null
        && mLoadingSpinner.getVisibility() == View.VISIBLE) {
      startSearch();
    }

    // Reload shared preferences
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

    if (mSuggestionListAdapter != null
        && mSuggestionsHeader != null
        && mSuggestionsHeader.getText().equals(getString(R.string.recent_searches_header))) {
      mSuggestionListAdapter.clear();
      mSuggestionListAdapter.addAll(mRecentSearchesList);
    }
  }

  /** Initiate a search of the selected type */
  private void startSearch() {
    mSetlistFMService
        .getArtists(mSearchEditText.getText().toString())
        .enqueue(
            new Callback<Artists>() {
              @Override
              public void onResponse(Call<Artists> call, Response<Artists> response) {
                if (!response.isSuccessful()) {
                  Log.e(
                      this.getClass().getSimpleName(),
                      "Artists search failed. Response code was: "
                          + response.code()
                          + ". Message was: "
                          + response.errorBody());
                  return;
                }

                if (response.body().getTotal() == 0) { // No results
                  mSuggestionListView.setVisibility(View.GONE);
                  mLoadingSpinner.setVisibility(View.GONE);
                  if (getActivity() != null && Utility.isNetworkConnected(getActivity())) {
                    mNoResultsTextView.setVisibility(View.VISIBLE);
                  } else {
                    mNoConnectionTextView.setVisibility(View.VISIBLE);
                  }
                  return;
                }

                mSuggestionListAdapter.clear();
                addArtistsWithSetlists(response.body().getArtist());
              }

              @Override
              public void onFailure(Call<Artists> call, Throwable t) {
                Log.e(getClass().getSimpleName(), t.toString());
              }
            });
  }

  private void addArtistsWithSetlists(final List<Artist> artists) {
    for (int i = 0; i < artists.size(); i++) {
      final Artist artist = artists.get(i);
      final int finalI = i;
      mSetlistFMService
          .getSetlistsByArtistMbid(artist.getMbid(), 1)
          .enqueue(
              new Callback<Setlists>() {
                @Override
                public void onResponse(Call<Setlists> call, Response<Setlists> response) {
                  // If artist has no setlists, request will return 404
                  if (response.isSuccessful()) {
                    mNameToIdMap.put(artist.getName(), artist.getMbid());
                    mSuggestionListAdapter.add(artist.getName());
                  }

                  // TODO: Handle case where artist only has empty setlists (EX: "77 Jefferson")

                  /* If none of the artists had setlists, treat it the same as if no artists were returns */
                  if (finalI == artists.size() - 1) {
                    mLoadingSpinner.setVisibility(View.GONE);

                    if (mSuggestionListAdapter.getCount() == 0) {
                      mSuggestionListView.setVisibility(View.GONE);
                      mLoadingSpinner.setVisibility(View.GONE);
                      if (getActivity() != null && Utility.isNetworkConnected(getActivity())) {
                        mNoResultsTextView.setVisibility(View.VISIBLE);
                      } else {
                        mNoConnectionTextView.setVisibility(View.VISIBLE);
                      }
                    } else {
                      mSuggestionListView.setVisibility(View.VISIBLE);
                      mSuggestionListAdapter.notifyDataSetChanged();
                    }
                  }
                }

                @Override
                public void onFailure(Call<Setlists> call, Throwable t) {
                  Log.e(getClass().getSimpleName(), t.toString());
                }
              });
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
}
