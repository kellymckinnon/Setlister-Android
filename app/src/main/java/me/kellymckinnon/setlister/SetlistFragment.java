package me.kellymckinnon.setlister;

import com.getbase.floatingactionbutton.FloatingActionButton;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Created by kelly on 12/22/14.
 */
public class SetlistFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_setlist, container, false);

        Bundle arguments = getArguments();

        String[] songs = arguments.getStringArray("SONGS");

        // Not used currently, but may be later on
        String artist = arguments.getString("ARTIST");
        String tour = arguments.getString("TOUR");
        String venue = arguments.getString("VENUE");
        String date = arguments.getString("DATE");

        ListView setlist = (ListView) rootView.findViewById(R.id.setlist);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, songs);

        setlist.setAdapter(adapter);

        FloatingActionButton spotify = (FloatingActionButton) rootView.findViewById(R.id.spotify);
        spotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpotifyHandler.authenticateUser(getActivity());
            }
        });

        return rootView;
    }
}
