package me.kellymckinnon.setlister;

import android.app.Fragment;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kelly on 12/19/14.
 */
public class SearchFragment extends Fragment {
    private DelayAutoCompleteTextView actv;
    private Filter filter;
    private ArrayAdapter<String> adapter;

    public SearchFragment() {}

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
                    new SearchTask().execute();
                }
                return null;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
            }
        };

        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line) {
            public Filter getFilter() {
                return filter;
            }
        };

        actv.setAdapter(adapter);
        actv.setLoadingIndicator((ProgressBar) rootView.findViewById(R.id.search_progress_bar));
        actv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO: implement clicking a search result
            }
        });

        adapter.setNotifyOnChange(false);

        return rootView;

    }

    public class SearchTask extends AsyncTask<Void, Void, Void> {
        List<String> artists = new ArrayList<String>();


        @Override
        protected Void doInBackground(Void... params) {
            String query = actv.getText().toString();
            artists.add("test1");
            artists.add("test2");
            // TODO: perform API call here
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            int size = artists.size();
            if(size > 0) {
                adapter.clear();
                Log.i("ADAPTER SIZE", "" + size);
                for(int i = 0; i < size; i++){
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
