package me.kellymckinnon.setlister.network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.view.View;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import me.kellymckinnon.setlister.fragments.SearchFragment;
import me.kellymckinnon.setlister.models.Artist;
import me.kellymckinnon.setlister.utils.JSONRetriever;
import me.kellymckinnon.setlister.utils.Utility;

/**
 * Uses the setlist.fm API to get all artists matching the
 * query, and returns any that actually have listed sets.
 * <p/>
 * Unfortunately, this is a minority of them (setlist.fm uses
 * MusicBrainz to get artists, and most have no sets), so artist
 * search is significantly slower than any other search. The
 * alternative, however, is to have pages and pages of useless suggestions.
 */
public class ArtistSearch extends AsyncTask<Void, Void, Void> {

    private SearchFragment mSearchFragment;
    private List<me.kellymckinnon.setlister.models.Artist> artists = new ArrayList<>();
    private String artistName;

    public ArtistSearch(SearchFragment searchFragment) {
        mSearchFragment = searchFragment;
        artistName = mSearchFragment.searchBar.getText().toString();
    }

    public void populateArtist(JSONObject currentArtist) throws JSONException {
        Artist artist = new Artist();
        artist.name = currentArtist.getString("@name");
        artist.mbid = currentArtist.getString("@mbid");

        try {
            artist.disambiguation = currentArtist.getString("@disambiguation");
        } catch (JSONException e) {
            artist.disambiguation = "";
        }

        // If the artist has no setlists, don't add it to the list of choices
        try {
            URL url = new URL(
                    "http://api.setlist.fm/rest/0.1/artist/" + artist.mbid + "/setlists.json");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        artists.add(artist);
    }

    @Override
    protected Void doInBackground(Void... params) {
        StringBuilder query = new StringBuilder();
        query.append("http://api.setlist.fm/rest/0.1/search/artists.json?artistName=");
        try {
            query.append(URLEncoder.encode(artistName, "UTF-8"));
            JSONObject json;

            if (JSONRetriever.getRequest(query.toString()) == null) { // No results found
                return null;
            }

            json = JSONRetriever.getRequest(query.toString()).getJSONObject("artists");

            // If only one result, it's a JSONObject, else an array
            if (json.getString("@total").equals("1")) {
                JSONObject currentArtist = json.getJSONObject("artist");
                populateArtist(currentArtist);
            } else {
                JSONArray items = json.getJSONArray("artist");

                for (int i = 0; i < items.length(); i++) {
                    JSONObject currentArtist = items.getJSONObject(i);
                    populateArtist(currentArtist);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        int size = artists.size();

        if (size > 0) { // Display list of results
            mSearchFragment.listAdapter.clear();

            for (int i = 0; i < size; i++) {
                mSearchFragment.nameIdMap.put(artists.get(i).name, artists.get(i).mbid);
                mSearchFragment.listAdapter.add(artists.get(i).name);
            }

            mSearchFragment.listAdapter.notifyDataSetChanged();
            mSearchFragment.suggestionList.setVisibility(View.VISIBLE);
        } else { // No results
            mSearchFragment.suggestionList.setVisibility(View.GONE);
            if (Utility.isNetworkConnected(mSearchFragment.getActivity())) {
                mSearchFragment.noResultsText.setVisibility(View.VISIBLE);
            } else {
                mSearchFragment.noConnectionText.setVisibility(View.VISIBLE);
            }
        }

        mSearchFragment.loadingSpinner.setVisibility(View.GONE);
        super.onPostExecute(aVoid);
    }
}
