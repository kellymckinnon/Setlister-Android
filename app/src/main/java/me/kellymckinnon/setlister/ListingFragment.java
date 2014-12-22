package me.kellymckinnon.setlister;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
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

    RecyclerView.Adapter adapter;
    RecyclerView rv;
    String artist;
    TextView noShows;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_listing, container, false);
        artist = getArguments().getString("ARTIST_NAME");
        noShows = (TextView) rootView.findViewById(R.id.no_shows);
        rv = (RecyclerView) rootView.findViewById(R.id.show_list);

        rv.addItemDecoration(
                new RecyclerViewDivider(getActivity(), RecyclerViewDivider.VERTICAL_LIST));

        rv.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);
        rv.setItemAnimator(new DefaultItemAnimator());

        new ShowSearch().execute();


        return rootView;
    }

    private class ShowSearch extends AsyncTask<Void, Void, Void> {

        ArrayList<Show> shows = new ArrayList<Show>();

        @Override
        protected Void doInBackground(Void... params) {
            StringBuilder query = new StringBuilder();
            query.append("http://api.setlist.fm/rest/0.1/search/setlists.json?artistName=");
            try {
                query.append(URLEncoder.encode(artist, "UTF-8"));

                Log.d("URL IS: ", query.toString());

                JSONObject json = null;

                if(JSONRetriever.getJSON(query.toString()) == null) { // No results found
                    return null;
                }

                json = JSONRetriever.getJSON(query.toString()).getJSONObject("setlists");

                int numShows = Integer.parseInt(json.getString("@total"));

                // If only one result, it's a JSONObject, else an array
                if (numShows == 1) {
                    JSONObject currentSetlist = json.getJSONObject("setlist");
                    populateShow(currentSetlist);
                } else {
                    // One 20-item array per page

                    int numPages = numShows/20;
                    if(numShows % 20 != 0) {
                        numPages++;
                    }

                    for(int a = 1; a <= numPages; a++) {
                        String currentPageQuery = query.toString() + "&p=" + a;
                        Log.d("URL IS: ", currentPageQuery);
                        json = JSONRetriever.getJSON(currentPageQuery).getJSONObject("setlists");
                        JSONArray items = json.getJSONArray("setlist");

                        for (int i = 0; i < items.length(); i++) {
                            JSONObject currentSetlist = items.getJSONObject(i);
                            populateShow(currentSetlist);
                        }
                    }

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
            if(shows.size() == 0) {
                noShows.setVisibility(View.VISIBLE);
                rv.setVisibility(View.GONE);
                Log.e("ShowSearch", "No shows found.");
                return;
            }

            adapter = new ShowAdapter(shows);
            rv.setAdapter(adapter);
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
                String day = currentSetlist.getString("@eventDate").substring(0,2);
                String month = currentSetlist.getString("@eventDate").substring(3,5);
                show.date = month + "/" + day + "/" + year;
            } catch (JSONException e) {
                Log.e("ShowSearch", "Error processing date");
                e.printStackTrace();
                return;
            }

            try {
                show.venue = currentSetlist.getJSONObject("venue").getString("@name") + ", "
                        + currentSetlist.getJSONObject("venue").getJSONObject("city").getString("@name") + ", "
                        + currentSetlist.getJSONObject("venue").getJSONObject("city").getString("@stateCode") + ", "
                        + currentSetlist.getJSONObject("venue").getJSONObject("city").getJSONObject("country").getString("@code");
            } catch (JSONException e) {
                show.venue = "Unknown venue";
            }

            try {
                JSONArray songs = currentSetlist.getJSONObject("sets").getJSONObject("set").getJSONArray("song");
                String[] setlist = new String[songs.length()];
                for(int i = 0; i < songs.length(); i++) {
                    setlist[i] = songs.getJSONObject(i).getString("@name");
                }
                show.setlist = setlist;
            } catch (JSONException e) {
                // Usually, this just means there are no songs in the setlist, and "sets"
                // is an empty string instead of an object.
                try {
                    currentSetlist.getString("sets");
                } catch (JSONException f) {
                    e.printStackTrace();
                }
                return;
            }

            shows.add(show);
        }
    }

}
