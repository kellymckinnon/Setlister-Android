package me.kellymckinnon.setlister;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

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
    private ArtistSearch search;

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
                    search = new ArtistSearch();
                    search.execute();
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
        actv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //FIXME: Change to MBID implementation -- will need a HashMap of Name -> MBID
                Intent intent = new Intent(getActivity(), ListingActivity.class);
                intent.putExtra("ARTIST_NAME", actv.getText().toString());
                startActivity(intent);
            }
        });

        actv.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {


                if(keyCode == KeyEvent.KEYCODE_ENTER || keyCode == EditorInfo.IME_ACTION_SEARCH) {
                    Intent intent = new Intent(getActivity(), ListingActivity.class);
                    intent.putExtra("ARTIST_NAME", actv.getText().toString());
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });

        ImageButton clearSearch = (ImageButton) rootView.findViewById(R.id.clear_search);
        clearSearch.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                actv.setText("");
            }
        });

        adapter.setNotifyOnChange(false);

        return rootView;

    }

    @Override
    public void onStop() {
        //check the state of the task
        if(search != null && search.getStatus() == AsyncTask.Status.RUNNING)
            search.cancel(true);

        super.onStop();

    }

    public class ArtistSearch extends AsyncTask<Void, Void, Void> {

        List<Artist> artists = new ArrayList<Artist>();


        @Override
        protected Void doInBackground(Void... params) {
            String artistName = actv.getText().toString();

            StringBuilder query = new StringBuilder();
            query.append("http://api.setlist.fm/rest/0.1/search/artists.json?artistName=");
            try {
                query.append(URLEncoder.encode(artistName, "UTF-8"));

                Log.d("URL IS: ", query.toString());

                JSONObject json = null;

                if (JSONRetriever.getJSON(query.toString()) == null) { // No results found
                    return null;
                }

                json = JSONRetriever.getJSON(query.toString()).getJSONObject("artists");

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
                adapter.clear();

                for (int i = 0; i < size; i++) {
                    adapter.add(artists.get(i).name);
                    Log.i("ADDED", artists.get(i).name + " " + artists.get(i).mbid);
                }

                adapter.notifyDataSetChanged();
                actv.showDropDown();
            }

            super.onPostExecute(aVoid);
        }

        public void populateArtist(JSONObject currentArtist) throws JSONException {
            String name = currentArtist.getString("@name");
            Artist artist = new Artist();
            artist.name = currentArtist.getString("@name");
            artist.mbid = currentArtist.getString("@mbid");

            try {
                artist.genre = currentArtist.getString("@disambiguation");
            } catch (JSONException e) {
                artist.genre = "";
            }

            // If the artist has no setlists, don't add it to the list of choices
            JSONObject json = JSONRetriever.getJSON("http://api.setlist.fm/rest/0.1/artist/"+artist.mbid+"/setlists.json");

            if(json == null) {
                return;
            }

            artists.add(artist);
        }
    }
}

