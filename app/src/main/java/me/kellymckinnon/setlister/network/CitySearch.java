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
import me.kellymckinnon.setlister.models.City;
import me.kellymckinnon.setlister.utils.JSONRetriever;
import me.kellymckinnon.setlister.utils.Utility;

/**
 * Uses the setlist.fm API to find cities that match the query.
 */
public class CitySearch extends AsyncTask<Void, Void, Void> {

    private SearchFragment mSearchFragment;
    private List<City> cities = new ArrayList<>();
    private String cityName;

    public CitySearch(SearchFragment searchFragment) {
        mSearchFragment = searchFragment;
        cityName = mSearchFragment.searchBar.getText().toString();
    }

    public void populateCity(JSONObject currentCity) throws JSONException {
        City city = new City();
        city.name = currentCity.getString("@name");
        city.id = currentCity.getString("@id");
        city.state = currentCity.getString("@stateCode");
        cities.add(city);
    }

    @Override
    protected Void doInBackground(Void... params) {
        StringBuilder query = new StringBuilder();
        query.append("http://api.setlist.fm/rest/0.1/search/cities.json?name=");
        try {
            query.append(URLEncoder.encode(cityName, "UTF-8"));
            JSONObject json;

            if (JSONRetriever.getRequest(query.toString()) == null) { // No results found
                return null;
            }

            json = JSONRetriever.getRequest(query.toString()).getJSONObject("cities");

            // If only one result, it's a JSONObject, else an array
            if (json.getString("@total").equals("1")) {
                JSONObject currentCity = json.getJSONObject("cities");
                populateCity(currentCity);
            } else {
                JSONArray items = json.getJSONArray("cities");
                for (int i = 0; i < items.length(); i++) {
                    JSONObject currentCity = items.getJSONObject(i);
                    populateCity(currentCity);
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
        int size = cities.size();
        if (size > 0) {
            mSearchFragment.listAdapter.clear();

            for (int i = 0; i < size; i++) {
                int insertionIndex = i;

                // Hack to get more pleasing city results, for some reason they're really out of
                // order
                if (cities.get(i).name.equalsIgnoreCase(
                        mSearchFragment.searchBar.getText().toString())) {
                    insertionIndex = 0;
                }

                mSearchFragment.nameIdMap.put(cities.get(i).name + ", " + cities.get(i).state,
                        cities.get(i).id);

                mSearchFragment.listAdapter.insert(cities.get(i).name + ", " + cities.get(i).state,
                        insertionIndex);
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
