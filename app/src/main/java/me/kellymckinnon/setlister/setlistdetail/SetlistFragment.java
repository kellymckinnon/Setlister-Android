package me.kellymckinnon.setlister.setlistdetail;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import io.reactivex.disposables.CompositeDisposable;
import me.kellymckinnon.setlister.R;
import me.kellymckinnon.setlister.common.SetlisterConstants;
import me.kellymckinnon.setlister.models.Show;
import me.kellymckinnon.setlister.network.SpotifyHandler;
import me.kellymckinnon.setlister.common.Utility;

/**
 * Displays the setlist for the given show and uses a floating action button to give user the option
 * to add all songs in the setlist to a Spotify playlist.
 */
public class SetlistFragment extends Fragment {

  private ShareActionProvider mShareActionProvider;
  private Show mShow;
  private View mRootView;

  /** Collects all subscriptions to unsubscribe later */
  private CompositeDisposable mCompositeDisposable;

  public static SetlistFragment newInstance(Show show) {
    SetlistFragment setlistFragment = new SetlistFragment();

    Bundle args = new Bundle();
    args.putParcelable(SetlisterConstants.EXTRA_SHOW, show);
    setlistFragment.setArguments(args);

    return setlistFragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mShow = getArguments().getParcelable(SetlisterConstants.EXTRA_SHOW);
    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    mRootView = inflater.inflate(R.layout.fragment_setlist, container, false);

    ListView setlist = mRootView.findViewById(R.id.setlist);

    ArrayAdapter<String> adapter =
        new ArrayAdapter<>(getActivity(), R.layout.single_line_list_row, mShow.getSongs());

    setlist.setAdapter(adapter);

    FloatingActionButton spotify = mRootView.findViewById(R.id.spotify);
    spotify.setOnClickListener(v -> SpotifyHandler.authenticateUser(SetlistFragment.this));

    return mRootView;
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    inflater.inflate(R.menu.menu_setlister, menu);
    MenuItem item = menu.findItem(R.id.action_share);
    item.setVisible(true);
    mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType("text/plain");
    if (mShow != null) {
      updateShareIntent();
    }
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if (getActivity() == null) {
      return false;
    }

    int id = item.getItemId();

    if (id == R.id.action_about) {
      Utility.showAboutDialog(getContext());
      return true;
    } else if (id == R.id.action_feedback) {
      Utility.startFeedbackEmail(getActivity());
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode != SetlisterConstants.SPOTIFY_LOGIN_ACTIVITY_ID) {
      return; // This shouldn't happen
    }

    AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);

    switch (response.getType()) {
        // Response was successful and contains auth token, we can create a Spotify playlist
      case TOKEN:
        if (getContext() == null) {
          return;
        }

        mCompositeDisposable =
            SpotifyPlaylistCreator.createPlaylist(
                "Bearer " + response.getAccessToken(), mRootView, getContext(), mShow);
        break;

        // Auth flow returned an error
      case ERROR:
        Snackbar.make(
                mRootView,
                getString(R.string.spotify_connection_failed_snackbar),
                Snackbar.LENGTH_SHORT)
            .show();
        break;
      case CODE:
      case EMPTY:
      case UNKNOWN:
        // Other cases mean that most likely auth flow was cancelled. We'll do nothing
    }
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

  /** Provide information for share button */
  private void updateShareIntent() {
    String shareTitle =
        getString(R.string.setlist_share_title, mShow.getBand(), mShow.getDate(), mShow.getVenue());

    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType("text/plain");
    intent.putExtra(Intent.EXTRA_SUBJECT, shareTitle);
    StringBuilder text = new StringBuilder();
    text.append(shareTitle).append(":\n");
    for (String s : mShow.getSongs()) {
      text.append("\n");
      text.append(s);
    }
    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
    intent.putExtra(Intent.EXTRA_TEXT, text.toString());
    mShareActionProvider.setShareIntent(intent);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    if (mCompositeDisposable != null) {
      mCompositeDisposable.clear();
    }
  }
}
