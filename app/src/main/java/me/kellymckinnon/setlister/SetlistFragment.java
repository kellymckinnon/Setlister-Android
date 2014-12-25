package me.kellymckinnon.setlister;

import android.app.ListFragment;
import android.os.Bundle;
import android.widget.ArrayAdapter;

/**
 * Created by kelly on 12/22/14.
 */
public class SetlistFragment extends ListFragment {

    @Override
    public void onActivityCreated(
            Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        Bundle arguments = getArguments();

        String[] songs = arguments.getStringArray("SONGS");

        // Not used currently, but may be later on
        String artist = arguments.getString("ARTIST");
        String tour = arguments.getString("TOUR");
        String venue = arguments.getString("VENUE");
        String date = arguments.getString("DATE");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, songs);

        setListAdapter(adapter);
    }
}
