package me.kellymckinnon.setlister.fragments;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.getbase.floatingactionbutton.FloatingActionButton;

import me.kellymckinnon.setlister.R;
import me.kellymckinnon.setlister.SetlisterExtras;
import me.kellymckinnon.setlister.models.Show;
import me.kellymckinnon.setlister.network.SpotifyHandler;

/**
 * Displays the setlist for the given show and uses a floating action button to give user the option
 * to add all songs in the setlist to a Spotify playlist.
 */
public class SetlistFragment extends Fragment {

  @Override
  public View onCreateView(
          @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_setlist, container, false);

    Bundle arguments = getArguments();

    Show show = arguments.getParcelable(SetlisterExtras.EXTRA_SHOW);

    ListView setlist = rootView.findViewById(R.id.setlist);

    ArrayAdapter<String> adapter =
        new ArrayAdapter<>(getActivity(), R.layout.single_line_list_row, show.getSongs());

    setlist.setAdapter(adapter);

    FloatingActionButton spotify = rootView.findViewById(R.id.spotify);
    spotify.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            SpotifyHandler.authenticateUser(getActivity());
          }
        });
    return rootView;
  }
}
