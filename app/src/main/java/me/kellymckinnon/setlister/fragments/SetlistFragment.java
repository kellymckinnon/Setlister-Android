package me.kellymckinnon.setlister.fragments;

import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
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
import me.kellymckinnon.setlister.utils.Utility;

/**
 * Displays the setlist for the given show and uses a floating action button to give user the option
 * to add all songs in the setlist to a Spotify playlist.
 */
public class SetlistFragment extends Fragment {

  private Show mShow;

  @Override
  public View onCreateView(
          @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_setlist, container, false);

    Bundle arguments = getArguments();

    mShow = arguments.getParcelable(SetlisterExtras.EXTRA_SHOW);

    ListView setlist = rootView.findViewById(R.id.setlist);

    ArrayAdapter<String> adapter =
        new ArrayAdapter<>(getActivity(), R.layout.single_line_list_row, mShow.getSongs());

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

  @Override
  public void onResume() {
    super.onResume();

    ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);

    String formattedDate = Utility.formatDate(mShow.getDate(), "MM/dd/yyyy", "MMMM d, yyyy");

    actionBar.setTitle(formattedDate);
    actionBar.setSubtitle(mShow.getBand());
  }
}
