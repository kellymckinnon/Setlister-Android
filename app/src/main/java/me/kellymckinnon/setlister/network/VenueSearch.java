package me.kellymckinnon.setlister.network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.view.View;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import me.kellymckinnon.setlister.fragments.SearchFragment;
import me.kellymckinnon.setlister.models.Venue;
import me.kellymckinnon.setlister.utils.JSONRetriever;
import me.kellymckinnon.setlister.utils.Utility;

/**
 * Uses the setlist.fm API to find venues that match the query.
 */
public class VenueSearch extends AsyncTask<Void, Void, Void> {

    private SearchFragment mSearchFragment;
    private List<Venue> venues = new ArrayList<>();
    private String venueName;

    public VenueSearch(SearchFragment searchFragment) {
        mSearchFragment = searchFragment;
        venueName = mSearchFragment.searchBar.getText().toString();
    }

    public void populateVenue(JSONObject currentVenue) throws JSONException {
        Venue venue = new Venue();
        venue.name = currentVenue.getString("@name");
        venue.id = currentVenue.getString("@id");

        JSONObject cityObject = currentVenue.getJSONObject("city");
        venue.city = cityObject.getString("@name") + ", " + cityObject.getString("@stateCode");
        venues.add(venue);
    }

    @Override
    protected Void doInBackground(Void... params) {
        StringBuilder query = new StringBuilder();
        query.append("http://api.setlist.fm/rest/0.1/search/venues.json?name=");
        try {
            query.append(URLEncoder.encode(venueName, "UTF-8"));
            JSONObject json;

            if (JSONRetriever.getRequest(query.toString()) == null) { // No results found
                return null;
            }

            json = JSONRetriever.getRequest(query.toString()).getJSONObject("venues");

            // If only one result, it's a JSONObject, else an array
            if (json.getString("@total").equals("1")) {
                JSONObject currentVenue = json.getJSONObject("venue");
                populateVenue(currentVenue);
            } else {
                JSONArray items = json.getJSONArray("venue");

                for (int i = 0; i < items.length(); i++) {
                    JSONObject currentVenue = items.getJSONObject(i);
                    populateVenue(currentVenue);
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
        int size = venues.size();

        if (size > 0) {
            mSearchFragment.listAdapter.clear();

            for (int i = 0; i < size; i++) {
                mSearchFragment.nameIdMap.put(venues.get(i).name + ", " + venues.get(i).city,
                        venues.get(i).id);
                mSearchFragment.listAdapter.add(venues.get(i).name + ", " + venues.get(i).city);
            }

            mSearchFragment.listAdapter.notifyDataSetChanged();
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
