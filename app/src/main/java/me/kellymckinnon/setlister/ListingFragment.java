package me.kellymckinnon.setlister;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;


/**
 * Created by kelly on 12/21/14.
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
                new RecyclerViewDivider(getActivity(), RecyclerViewDivider.VERTICAL_LIST));

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
        ArrayList<Show> shows = new ArrayList<Show>();

        @Override
        protected Void doInBackground(Void... params) {
            loading = true;
            StringBuilder url = new StringBuilder();
            url.append("http://api.setlist.fm/rest/0.1/search/setlists.json?");
            try {
                String parameter;
                if(searchType.equals("Artist")) {
                    if (id != null) {
                        parameter = "artistMbid=" + id;
                    } else {
                        parameter = "artistName=" + URLEncoder.encode(query, "UTF-8");
                    }
                } else if (searchType.equals("Venue")) {
                    if (id != null) {
                        parameter = "venueId=" + id;
                    } else {
                        parameter = "venueName=" + URLEncoder.encode(query, "UTF-8");
                    }
                } else if (searchType.equals("City")) {
                    if (id != null) {
                        parameter = "cityId=" + id;
                    } else {
                        parameter = "cityName=" + URLEncoder.encode(query, "UTF-8");
                    }
                } else {
                    throw new RuntimeException("Invalid search type");
                }

                url.append(parameter);

                Log.d("URL IS: ", url.toString());

                JSONObject json = null;

                if (JSONRetriever.getRequest(url.toString()) == null) { // No results found
                    return null;
                }

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

                // Add at least 12 shows (or one page, whichever's bigger) on each scroll
                while (numShowsAdded < 12 && pagesLoaded < numPages) {
                    String currentPageQuery = url.toString() + "&p=" + (pagesLoaded + 1);
                    Log.d("URL IS: ", currentPageQuery);
                    json = JSONRetriever.getRequest(currentPageQuery).getJSONObject("setlists");
                    JSONArray items = json.getJSONArray("setlist");

                    for (int i = 0; i < items.length(); i++) {
                        JSONObject currentSetlist = items.getJSONObject(i);
                        populateShow(currentSetlist);
                    }

                    pagesLoaded++;
                }
            } catch (JSONException e) {
                Log.e("ShowSearch", "JSONException");
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                Log.e("ShowSearch", "UnsupportedEncodingException");
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (shows.size() == 0 && adapter.getItemCount() == 0) {
                noShows.setVisibility(View.VISIBLE);
                rv.setVisibility(View.GONE);
                Log.e("ShowSearch", "No shows found.");
                return;
            }

            for (Show s : shows) {
                adapter.add(s);
            }
        }

        private void populateShow(JSONObject currentSetlist) {
            Show show = new Show();

            try {
                show.band = currentSetlist.getJSONObject("artist").getString("@name");
            } catch (JSONException e) {
                show.band = "Unknown band";
            }
            try {
                show.tour = currentSetlist.getString("@tour");
            } catch (JSONException e) {
                show.tour = "";
            }

            try {
                String year = currentSetlist.getString("@eventDate").substring(6);
                String day = currentSetlist.getString("@eventDate").substring(0, 2);
                String month = currentSetlist.getString("@eventDate").substring(3, 5);
                show.date = month + "/" + day + "/" + year;
            } catch (JSONException e) {
                Log.e("ShowSearch", "Error processing date");
                e.printStackTrace();
                return;
            }

            try {
                show.venue = currentSetlist.getJSONObject("venue").getString("@name") + ", "
                        + currentSetlist.getJSONObject("venue")
                        .getJSONObject("city")
                        .getString("@name") + ", "
                        + currentSetlist.getJSONObject("venue")
                        .getJSONObject("city")
                        .getString("@stateCode") + ", "
                        + currentSetlist.getJSONObject("venue")
                        .getJSONObject("city")
                        .getJSONObject("country")
                        .getString("@code");
            } catch (JSONException e) {
                show.venue = "Unknown venue";
            }

            try {
                JSONArray songs = currentSetlist.getJSONObject("sets")
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
                // If there are multiple sets (set is an array)
            }

            try {
                JSONArray sets = currentSetlist.getJSONObject("sets")
                        .getJSONArray("set");
                ArrayList<String> setlist = new ArrayList<String>();
                for(int i = 0; i < sets.length(); i++) {
                    JSONArray songs = sets.getJSONObject(i).getJSONArray("song");
                    for(int j = 0; j < songs.length(); j++) {
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
                    currentSetlist.getString("sets");
                } catch (JSONException f) {
                    e.printStackTrace();
                }
            }
        }
    }

}
