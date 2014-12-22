package me.kellymckinnon.setlister;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ProgressBar;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kelly on 12/19/14.
 */
public class SearchFragment extends Fragment {

    private DelayAutoCompleteTextView actv;
    private Filter filter;
    private ArrayAdapter<String> adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);
        actv = (DelayAutoCompleteTextView) rootView.findViewById(R.id.searchAutoComplete);

        filter = new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                Log.i("Filter",
                        "Filter:" + constraint + " thread: " + Thread.currentThread());
                if (constraint != null) {
                    Log.i("Filter", "doing a search ..");
                    new ArtistSearch().execute();
                }
                return null;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
            }
        };

        adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_dropdown_item_1line) {
            public Filter getFilter() {
                return filter;
            }
        };

        actv.setAdapter(adapter);
        actv.setLoadingIndicator((ProgressBar) rootView.findViewById(R.id.search_progress_bar));
        actv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ListingActivity.class);
                intent.putExtra("ARTIST_NAME", actv.getText().toString());
                startActivity(intent);
            }
        });

        adapter.setNotifyOnChange(false);

        return rootView;

    }

    public class ArtistSearch extends AsyncTask<Void, Void, Void> {

        List<String> artists = new ArrayList<String>();


        @Override
        protected Void doInBackground(Void... params) {
            String artist = actv.getText().toString();

            StringBuilder query = new StringBuilder();
            query.append("http://api.setlist.fm/rest/0.1/search/artists.json?artistName=");
            try {
                query.append(URLEncoder.encode(artist, "UTF-8"));

                Log.d("URL IS: ", query.toString());

                JSONObject json = null;

                if(JSONRetriever.getJSON(query.toString()) == null) { // No results found
                    return null;
                }

                json = JSONRetriever.getJSON(query.toString()).getJSONObject("artists");

                // If only one result, it's a JSONObject, else an array
                if (json.getString("@total").equals("1")) {
                    JSONObject currentArtist = json.getJSONObject("artist");
                    String name = currentArtist.getString("@name");
                    artists.add(name);
                } else {
                    JSONArray items = json.getJSONArray("artist");

                    for (int i = 0; i < items.length(); i++) {
                        JSONObject currentArtist = items.getJSONObject(i);
                        String name = currentArtist.getString("@name");
                        artists.add(name);
                    }
                }
            } catch (JSONException e) {
                System.out.println("JSONException");
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                System.out.println("UnsupportedEncodingException");
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            int size = artists.size();
            if (size > 0) {
                adapter.clear();
                Log.i("ADAPTER SIZE", "" + size);
                for (int i = 0; i < size; i++) {
                    adapter.add(artists.get(i));
                    Log.i("ADDED", artists.get(i));
                }

                adapter.notifyDataSetChanged();
                actv.showDropDown();
            }

            super.onPostExecute(aVoid);
        }
    }
}
