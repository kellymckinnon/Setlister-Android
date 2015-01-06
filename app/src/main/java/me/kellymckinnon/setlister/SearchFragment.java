package me.kellymckinnon.setlister;

import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ListView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment opened by SearchActivity that holds a search bar
 * and displays recent results as well as suggestions
 */
public class SearchFragment extends Fragment {

    private MaterialEditText searchBar;
    private Filter filter;
    private ArrayAdapter<String> adapter;
    private ArtistSearch search;

    private final int TRIGGER_SEARCH = 1;
    private final long SEARCH_DELAY_IN_MS = 500;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);
        searchBar = (MaterialEditText) rootView.findViewById(R.id.searchBar);

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == TRIGGER_SEARCH) {
                    search = new ArtistSearch();
                    search.execute();
                }
            }
        };

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                handler.removeMessages(TRIGGER_SEARCH);
                handler.sendEmptyMessageDelayed(TRIGGER_SEARCH, SEARCH_DELAY_IN_MS);
            }
        });

        searchBar.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == EditorInfo.IME_ACTION_SEARCH) {
                    Intent intent = new Intent(getActivity(), ListingActivity.class);
                    intent.putExtra("ARTIST_NAME", searchBar.getText().toString());
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });

        final ImageButton clearSearch = (ImageButton) rootView.findViewById(R.id.clear_search);
        clearSearch.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBar.setText("");
            }
        });


        ListView suggestionList = (ListView) rootView.findViewById(R.id.suggestionList);

        adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1);

        suggestionList.setAdapter(adapter);

        suggestionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //FIXME: Change to MBID implementation -- will need a HashMap of Name -> MBID
                Intent intent = new Intent(getActivity(), ListingActivity.class);
                intent.putExtra("ARTIST_NAME", searchBar.getText().toString());
                startActivity(intent);
            }
        });

        return rootView;

    }

    @Override
    public void onStop() {
        //check the state of the task
        if (search != null && search.getStatus() == AsyncTask.Status.RUNNING) {
            search.cancel(true);
        }

        super.onStop();

    }

    public class ArtistSearch extends AsyncTask<Void, Void, Void> {

        List<Artist> artists = new ArrayList<Artist>();

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
            JSONObject json = JSONRetriever.getRequest(
                    "http://api.setlist.fm/rest/0.1/artist/" + artist.mbid + "/setlists.json");

            if (json == null) {
                return;
            }

            artists.add(artist);
        }

        @Override
        protected Void doInBackground(Void... params) {
            String artistName = searchBar.getText().toString();

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
                adapter.clear();

                for (int i = 0; i < size; i++) {
                    adapter.add(artists.get(i).name);
                    Log.i("ADDED", artists.get(i).name + " " + artists.get(i).mbid);
                }

                adapter.notifyDataSetChanged();
            }

            super.onPostExecute(aVoid);
        }
    }
}

