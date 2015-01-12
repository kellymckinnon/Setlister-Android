package me.kellymckinnon.setlister.fragments;

import com.getbase.floatingactionbutton.FloatingActionButton;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import me.kellymckinnon.setlister.R;
import me.kellymckinnon.setlister.network.SpotifyHandler;

/**
 * Displays the setlist for the given show and uses a
 * floating action button to give user the option to add
 * all songs in the setlist to a Spotify playlist.
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

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
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
