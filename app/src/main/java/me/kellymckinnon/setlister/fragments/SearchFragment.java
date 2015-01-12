package me.kellymckinnon.setlister.fragments;

import com.rengwuxian.materialedittext.MaterialEditText;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import me.kellymckinnon.setlister.ListingActivity;
import me.kellymckinnon.setlister.R;
import me.kellymckinnon.setlister.network.ArtistSearch;
import me.kellymckinnon.setlister.network.CitySearch;
import me.kellymckinnon.setlister.network.VenueSearch;
import me.kellymckinnon.setlister.utils.Utility;

/**
 * Fragment opened by SearchActivity that holds a search bar
 * and displays recent results as well as suggestions as the user types
 */
public class SearchFragment extends Fragment {

    private static final int TRIGGER_SEARCH = 1;
    private static final long SEARCH_DELAY_IN_MS = 500;
    private static final int NUM_RECENT_SEARCHES = 5;
    public ProgressBar loadingSpinner;
    public MaterialEditText searchBar;
    public ListView suggestionList;
    public ArrayAdapter<String> listAdapter;
    public HashMap<String, String> nameIdMap;
    public TextView noResultsText;
    public TextView noConnectionText;
    private AsyncTask<Void, Void, Void> searchTask;
    private String searchType;
    private ArrayList<String> recentSearches;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        suggestionList = (ListView) rootView.findViewById(R.id.suggestion_list);
        loadingSpinner = (ProgressBar) rootView.findViewById(R.id.loading_suggestions);
        noResultsText = (TextView) rootView.findViewById(R.id.no_results_text);
        noConnectionText = (TextView) rootView.findViewById(R.id.no_connection_text);
        searchBar = (MaterialEditText) rootView.findViewById(R.id.search_bar);

        final TextView suggestionHeader = (TextView) rootView.findViewById(R.id.suggestion_header);
        final TextView noRecentSearchesText = (TextView) rootView.findViewById(
                R.id.no_searches_text);
        final TextView artistSelectionText = (TextView) rootView.findViewById(
                R.id.artist_selection);
        final TextView venueSelectionText = (TextView) rootView.findViewById(R.id.venue_selection);
        final TextView citySelectionText = (TextView) rootView.findViewById(R.id.city_selection);

        final ImageButton clearSearchButton = (ImageButton) rootView.findViewById(
                R.id.clear_search);
        clearSearchButton.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBar.setText("");
            }
        });

        final int accentColor = artistSelectionText.getCurrentTextColor();
        final int normalColor = venueSelectionText.getCurrentTextColor();
        searchType = getString(R.string.artist);

        artistSelectionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                artistSelectionText.setTextColor(accentColor);
                venueSelectionText.setTextColor(normalColor);
                citySelectionText.setTextColor(normalColor);

                artistSelectionText.setTypeface(Typeface.DEFAULT_BOLD);
                venueSelectionText.setTypeface(Typeface.DEFAULT);
                citySelectionText.setTypeface(Typeface.DEFAULT);
                searchType = getString(R.string.artist);
            }
        });

        venueSelectionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                artistSelectionText.setTextColor(normalColor);
                venueSelectionText.setTextColor(accentColor);
                citySelectionText.setTextColor(normalColor);
                artistSelectionText.setTypeface(Typeface.DEFAULT);
                venueSelectionText.setTypeface(Typeface.DEFAULT_BOLD);
                citySelectionText.setTypeface(Typeface.DEFAULT);
                searchType = getString(R.string.venue);
            }
        });

        citySelectionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                artistSelectionText.setTextColor(normalColor);
                venueSelectionText.setTextColor(normalColor);
                citySelectionText.setTextColor(accentColor);
                artistSelectionText.setTypeface(Typeface.DEFAULT);
                venueSelectionText.setTypeface(Typeface.DEFAULT);
                citySelectionText.setTypeface(Typeface.DEFAULT_BOLD);
                searchType = getString(R.string.city);
            }
        });

        SharedPreferences recentFile = getActivity().getSharedPreferences(
                getString(R.string.prefs_name),
                Context.MODE_PRIVATE);
        recentSearches = new ArrayList<>();
        nameIdMap = new HashMap<>();

        // Recent searches are stored as search0, search1, search2, etc up to search4 (5 maximum)
        for (int i = 0; i < NUM_RECENT_SEARCHES; i++) {
            if (recentFile.contains("search" + i)) {
                String query = recentFile.getString("search" + i, "");
                String id = recentFile.getString("id" + i, "0");
                recentSearches.add(query);
                nameIdMap.put(query, id);
            }
        }

        if (recentSearches.isEmpty()) {
            noRecentSearchesText.setVisibility(View.VISIBLE);
            suggestionList.setVisibility(View.GONE);
        }

        // Don't search until user has stopped typing
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == TRIGGER_SEARCH && searchBar.getText().toString().length() != 0) {
                    noResultsText.setVisibility(View.GONE);
                    noConnectionText.setVisibility(View.GONE);
                    loadingSpinner.setVisibility(View.VISIBLE);
                    suggestionList.setVisibility(View.GONE);

                    startSearch();
                }
            }
        };

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) { // Empty search, show recent searches
                    suggestionHeader.setText(getString(R.string.recent_searches_header));
                    loadingSpinner.setVisibility(View.GONE);
                    noResultsText.setVisibility(View.GONE);
                    noConnectionText.setVisibility(View.GONE);

                    if (recentSearches.isEmpty()) {
                        suggestionList.setVisibility(View.GONE);
                        noRecentSearchesText.setVisibility(View.VISIBLE);
                    } else {
                        suggestionList.setVisibility(View.VISIBLE);
                    }

                    // Stop any current searches
                    handler.removeMessages(TRIGGER_SEARCH);
                    if (searchTask != null && searchTask.getStatus() == AsyncTask.Status.RUNNING) {
                        searchTask.cancel(true);
                    }

                    if (listAdapter != null) {
                        listAdapter.clear();
                        listAdapter.addAll(recentSearches);
                    }
                } else { // There is a query, get suggestions
                    noRecentSearchesText.setVisibility(View.GONE);
                    noResultsText.setVisibility(View.GONE);
                    noConnectionText.setVisibility(View.GONE);
                    loadingSpinner.setVisibility(View.VISIBLE);
                    suggestionList.setVisibility(View.GONE);
                    suggestionHeader.setText(getString(R.string.best_matches_header));
                    handler.removeMessages(TRIGGER_SEARCH);
                    handler.sendEmptyMessageDelayed(TRIGGER_SEARCH, SEARCH_DELAY_IN_MS);
                }

            }
        });

        // Search when enter is pressed
        searchBar.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                String text = searchBar.getText().toString();

                // We only want to do this once
                if (event.getAction() != KeyEvent.ACTION_UP) {
                    return false;
                }

                if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == EditorInfo.IME_ACTION_SEARCH)
                        && text.length() != 0) {
                    Intent intent = new Intent(getActivity(), ListingActivity.class);
                    String formattedQuery = Utility.capitalizeFirstLetters(
                            text);
                    intent.putExtra("QUERY", formattedQuery);
                    intent.putExtra("SEARCH_TYPE", searchType);
                    addRecentSearch(formattedQuery, "0");
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });

        // Populate list with recent searches
        listAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1);
        suggestionList.setAdapter(listAdapter);
        listAdapter.addAll(recentSearches);

        // Search for the clicked suggestion/recent search
        suggestionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String query = (String) suggestionList.getItemAtPosition(position);
                String searchId = nameIdMap.get(query);

                // Remove search type from recent searches before sending to search
                if (query.contains("(Venue)")) {
                    query = query.substring(0, query.length() - 8);
                } else if (query.contains("(Artist)")) {
                    query = query.substring(0, query.length() - 9);
                } else if (query.contains("(Tour)")) {
                    query = query.substring(0, query.length() - 7);
                }

                addRecentSearch(query, searchId);

                Intent intent = new Intent(getActivity(), ListingActivity.class);
                intent.putExtra("QUERY", query);
                intent.putExtra("SEARCH_TYPE", searchType);
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
        if (searchBar != null && searchBar.getText().toString().length() != 0
                && loadingSpinner != null && loadingSpinner.getVisibility() == View.VISIBLE) {
            startSearch();
        }

        // Reload shared preferences
        SharedPreferences recentFile = getActivity().getSharedPreferences(
                getString(R.string.prefs_name),
                Context.MODE_PRIVATE);

        recentSearches = new ArrayList<>();

        if (nameIdMap == null) {
            nameIdMap = new HashMap<>();
        }

        // Recent searches are stored as search0, search1, search2, etc up to search4 (5 maximum)
        for (int i = 0; i < NUM_RECENT_SEARCHES; i++) {
            if (recentFile.contains("search" + i)) {
                String query = recentFile.getString("search" + i, "");
                String id = recentFile.getString("id" + i, "0");
                recentSearches.add(query);
                nameIdMap.put(query, id);
            }
        }
    }

    @Override
    public void onStop() {
        // Check the state of the task
        if (searchTask != null && searchTask.getStatus() == AsyncTask.Status.RUNNING) {
            searchTask.cancel(true);
        }

        super.onStop();
    }

    /**
     * Initiate a search of the selected type
     */
    public void startSearch() {
        if (searchType.equals(getString(R.string.artist))) {
            searchTask = new ArtistSearch(SearchFragment.this);
        } else if (searchType.equals(getString(R.string.venue))) {
            searchTask = new VenueSearch(SearchFragment.this);
        } else if (searchType.equals(getString(R.string.city))) {
            searchTask = new CitySearch(SearchFragment.this);
        } else {
            // This should never happen
            throw new IllegalArgumentException("Search type not specified.");
        }

        searchTask.execute();
    }

    /**
     * Add the given search to the top of the recent searches list, pushing off the oldest entry.
     * Both names and ids are stored, under search0 (most recent), search1, ... and id0, id1, ...
     * up to a maximum of five entries.
     * <p/>
     * If the id for a recent search is 0, this indicates that the search was done via the enter
     * key and since no specific result was chosen, there is no associated id.
     */
    public void addRecentSearch(String query, String id) {
        SharedPreferences recentFile = getActivity().getSharedPreferences(
                getString(R.string.prefs_name),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = recentFile.edit();

        String formattedRecentSearch = query + " (" + searchType + ")";

        for (String s : recentSearches) {
            if (s.equalsIgnoreCase(formattedRecentSearch)) {
                //TODO: Instead of doing nothing, move the search to the top
                //And replace the ID if necessary
                return;
            }
        }

        for (int i = NUM_RECENT_SEARCHES - 2; i >= 0; i--) {
            if (recentFile.contains("search" + i)) {
                editor.putString("search" + (i + 1), recentFile.getString("search" + i, ""));
                editor.putString("id" + (i + 1), recentFile.getString("id" + i, "0"));
            }
        }

        editor.putString("search" + 0, formattedRecentSearch);
        editor.putString("id" + 0, id);
        editor.apply();
    }
}

