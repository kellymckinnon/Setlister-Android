package me.kellymckinnon.setlister.fragments;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import me.kellymckinnon.setlister.R;
import me.kellymckinnon.setlister.models.Show;
import me.kellymckinnon.setlister.utils.JSONRetriever;
import me.kellymckinnon.setlister.utils.Utility;
import me.kellymckinnon.setlister.views.RecyclerViewDivider;
import me.kellymckinnon.setlister.views.ShowAdapter;


/**
 * Uses the passed in query (either an artist, venue, or city)
 * to search the setlist.fm database for shows, and displays them
 * in a list that contains the artist, date, location, tour,
 * and number of songs in the setlist for that show.
 */
public class ListingFragment extends Fragment {

    private LinearLayoutManager llm;
    private RecyclerView rv;
    private String query;
    private TextView noShows;
    private int pagesLoaded;
    private int firstVisibleItem, visibleItemCount, totalItemCount;
    private boolean loading = true;
    private ShowAdapter adapter;
    private int numPages; // Number of pages of setlists from API
    private String searchType;
    private String id;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_listing, container, false);
        query = getArguments().getString("QUERY");
        searchType = getArguments().getString("SEARCH_TYPE");
        id = getArguments().getString("ID");
        noShows = (TextView) rootView.findViewById(R.id.no_shows);
        rv = (RecyclerView) rootView.findViewById(R.id.show_list);
        rv.addItemDecoration(
                new me.kellymckinnon.setlister.views.RecyclerViewDivider(getActivity(),
                        RecyclerViewDivider.VERTICAL_LIST));
        llm = new LinearLayoutManager(getActivity());

        firstVisibleItem = 0;
        visibleItemCount = 0;
        totalItemCount = 0;

        adapter = new ShowAdapter(getActivity());
        rv.setAdapter(adapter);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(llm);
        rv.setItemAnimator(new DefaultItemAnimator());

        /* Load new page(s) if the user scrolls to the end */
        rv.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                visibleItemCount = llm.getChildCount();
                totalItemCount = llm.getItemCount();
                firstVisibleItem = llm.findFirstVisibleItemPosition();

                if (loading && pagesLoaded < numPages) {
                    if ((visibleItemCount + firstVisibleItem) >= totalItemCount - 10) {
                        loading = false;
                        new ShowSearch().execute();
                    }
                }
            }
        });

        // Start the initial search
        new ShowSearch().execute();

        return rootView;
    }

    /* Gets shows from the setlist.fm API */
    private class ShowSearch extends AsyncTask<Void, Void, Void> {

        int numShowsAdded = 0; // Shows added on this search only
        ArrayList<Show> shows = new ArrayList<>();

        @Override
        protected Void doInBackground(Void... params) {
            loading = true;
            StringBuilder url = new StringBuilder();
            url.append("http://api.setlist.fm/rest/0.1/search/setlists.json?");
            try {
                /* If an ID was passed in, use that for the most accurate results.
                Otherwise, just use the query as artist/venue/city name */
                String parameter;
                switch (searchType) {
                    case "Artist":
                        if (id != null) {
                            parameter = "artistMbid=" + id;
                        } else {
                            parameter = "artistName=" + URLEncoder.encode(query, "UTF-8");
                        }
                        break;
                    case "Venue":
                        if (id != null) {
                            parameter = "venueId=" + id;
                        } else {
                            parameter = "venueName=" + URLEncoder.encode(query, "UTF-8");
                        }
                        break;
                    case "City":
                        if (id != null) {
                            parameter = "cityId=" + id;
                        } else {
                            parameter = "cityName=" + URLEncoder.encode(query, "UTF-8");
                        }
                        break;
                    default:
                        throw new RuntimeException("Invalid search type");
                }

                url.append(parameter);

                if (JSONRetriever.getRequest(url.toString()) == null) { // No results found
                    return null;
                }

                JSONObject json;

                // On first run, calculate total number of pages
                if (pagesLoaded == 0) {
                    json = JSONRetriever.getRequest(url.toString()).getJSONObject("setlists");
                    int numShows = Integer.parseInt(json.getString("@total"));

                    // One 20-item array per page
                    numPages = numShows / 20;
                    if (numShows % 20 != 0) {
                        numPages++;
                    }

                    // If only one show, it's a JSONObject instead of an array
                    if (numShows == 1) {
                        JSONObject currentSetlist = json.getJSONObject("setlist");
                        populateShow(currentSetlist);
                        pagesLoaded++;
                        return null;
                    }
                }

                // Add at least 12 shows (or one page, whichever is bigger) on each scroll
                while (numShowsAdded < 12 && pagesLoaded < numPages) {
                    String currentPageQuery = url.toString() + "&p=" + (pagesLoaded + 1);
                    json = JSONRetriever.getRequest(currentPageQuery).getJSONObject("setlists");
                    JSONArray items = json.getJSONArray("setlist");

                    for (int i = 0; i < items.length(); i++) {
                        JSONObject currentSetlist = items.getJSONObject(i);
                        populateShow(currentSetlist);
                    }

                    pagesLoaded++;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (shows.size() == 0 && adapter.getItemCount() == 0) { // No results
                if (!Utility.isNetworkConnected(getActivity())) {
                    noShows.setText(R.string.no_connection); // Because there's no signal
                } // Or because there are no shows for that query
                noShows.setVisibility(View.VISIBLE);
                rv.setVisibility(View.GONE);
                return;
            }

            for (Show s : shows) {
                adapter.add(s);
            }
        }

        /**
         * Move data from JSON response into Show objects
         */
        private void populateShow(JSONObject currentShow) {
            Show show = new Show();

            try {
                show.band = currentShow.getJSONObject("artist").getString("@name");
            } catch (JSONException e) {
                show.band = "Unknown band";
            }
            try {
                show.tour = currentShow.getString("@tour");
            } catch (JSONException e) {
                show.tour = "";
            }

            try {
                String year = currentShow.getString("@eventDate").substring(6);
                String day = currentShow.getString("@eventDate").substring(0, 2);
                String month = currentShow.getString("@eventDate").substring(3, 5);
                show.date = month + "/" + day + "/" + year;
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }

            try {
                show.venue = currentShow.getJSONObject("venue").getString("@name") + ", "
                        + currentShow.getJSONObject("venue")
                        .getJSONObject("city")
                        .getString("@name") + ", "
                        + currentShow.getJSONObject("venue")
                        .getJSONObject("city")
                        .getString("@stateCode") + ", "
                        + currentShow.getJSONObject("venue")
                        .getJSONObject("city")
                        .getJSONObject("country")
                        .getString("@code");
            } catch (JSONException e) {
                show.venue = "Unknown venue";
            }

            /* First, assume there is exactly ONE set. "sets" will
            be an object that contains another object, "set".*/
            try {
                JSONArray songs = currentShow.getJSONObject("sets")
                        .getJSONObject("set")
                        .getJSONArray("song");
                String[] setlist = new String[songs.length()];
                for (int i = 0; i < songs.length(); i++) {
                    setlist[i] = songs.getJSONObject(i).getString("@name");
                }
                show.setlist = setlist;
                numShowsAdded++;
                shows.add(show);
                return;
            } catch (JSONException e) {
                // If this fails, there are multiple sets for the show.
            }

            /* There are multiple sets, so "set" is an array. */
            try {
                JSONArray sets = currentShow.getJSONObject("sets")
                        .getJSONArray("set");
                ArrayList<String> setlist = new ArrayList<>();

                // Combine all of the sets into one for viewing
                for (int i = 0; i < sets.length(); i++) {
                    JSONArray songs = sets.getJSONObject(i).getJSONArray("song");
                    for (int j = 0; j < songs.length(); j++) {
                        setlist.add(songs.getJSONObject(j).getString("@name"));
                    }
                }
                show.setlist = setlist.toArray(new String[setlist.size()]);
                numShowsAdded++;
                shows.add(show);
            } catch (JSONException e) {
                // Usually, this just means there are no songs in the setlist, and "sets"
                // is an empty string instead of an object.
                try {
                    currentShow.getString("sets");
                } catch (JSONException f) {
                    e.printStackTrace(); // There was an actual problem
                }
            }
        }
    }
}
