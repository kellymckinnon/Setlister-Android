package me.kellymckinnon.setlister;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kelly on 1/6/15.
 */
public class ArtistSearch extends AsyncTask<Void, Void, Void> {

    private SearchFragment mSearchFragment;
    private List<Artist> artists = new ArrayList<Artist>();
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
            artist.genre = currentArtist.getString("@disambiguation");
        } catch (JSONException e) {
            artist.genre = "";
        }

        // If the artist has no setlists, don't add it to the list of choices
        JSONObject json = JSONRetriever.getRequest(
                "http://api.setlist.fm/rest/0.1/artist/" + artist.mbid + "/setlists.json");

        if (json == null) {
            return;
        }

        artists.add(artist);
    }

    @Override
    protected Void doInBackground(Void... params) {
        StringBuilder query = new StringBuilder();
        query.append("http://api.setlist.fm/rest/0.1/search/artists.json?artistName=");
        try {
            query.append(URLEncoder.encode(artistName, "UTF-8"));

            Log.d("URL IS: ", query.toString());

            JSONObject json = null;

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
            Log.e("ArtistSearch", "JSONException");
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            Log.e("ArtistSearch", "UEE");
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        int size = artists.size();

        if (size > 0) {
            mSearchFragment.listAdapter.clear();

            for (int i = 0; i < size; i++) {
                mSearchFragment.nameIdMap.put(artists.get(i).name, artists.get(i).mbid);
                mSearchFragment.listAdapter.add(artists.get(i).name);
                Log.i("ADDED", artists.get(i).name + " " + artists.get(i).mbid);
            }

            mSearchFragment.listAdapter.notifyDataSetChanged();
            mSearchFragment.suggestionList.setVisibility(View.VISIBLE);
        } else {
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
