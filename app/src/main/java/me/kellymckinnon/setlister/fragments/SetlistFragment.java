package me.kellymckinnon.setlister.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import me.kellymckinnon.setlister.R;
import me.kellymckinnon.setlister.SetlisterConstants;
import me.kellymckinnon.setlister.models.Show;
import me.kellymckinnon.setlister.models.SpotifyPlaylist;
import me.kellymckinnon.setlister.models.SpotifyUser;
import me.kellymckinnon.setlister.network.RetrofitClient;
import me.kellymckinnon.setlister.network.SpotifyHandler;
import me.kellymckinnon.setlister.utils.JSONRetriever;
import me.kellymckinnon.setlister.utils.Utility;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Displays the setlist for the given show and uses a floating action button to give user the option
 * to add all songs in the setlist to a Spotify playlist.
 */
public class SetlistFragment extends Fragment {

  private String mAccessToken;
  private ArrayList<String> mFailedSpotifySongs = new ArrayList<>();
  private ShareActionProvider mShareActionProvider;
  private Show mShow;
  private View mRootView;

  /** Collects all subscriptions to unsubscribe later */
  @NonNull private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

  private String mSpotifyUserId;

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
    spotify.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            SpotifyHandler.authenticateUser(SetlistFragment.this);
          }
        });
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
        mAccessToken = response.getAccessToken();
        Snackbar.make(
                mRootView,
                getString(R.string.spotify_creating_playlist_snackbar),
                Snackbar.LENGTH_SHORT)
            .show();

        mFailedSpotifySongs = new ArrayList<>();

        createSpotifyPlaylist();
        break;

        // Auth flow returned an error
      case ERROR:
        Snackbar.make(
                mRootView,
                getString(R.string.spotify_connection_failed_snackbar),
                Snackbar.LENGTH_SHORT)
            .show();
        // Other cases mean that most likely auth flow was cancelled. We'll do nothing
    }
  }

  private void createSpotifyPlaylist() {
    final String accessTokenHeader = "Bearer " + mAccessToken;

    mCompositeDisposable.add(
        RetrofitClient.getSpotifyService()
            .getUser(accessTokenHeader)
            .flatMap(
                new Function<SpotifyUser, Single<SpotifyPlaylist>>() {
                  @Override
                  public Single<SpotifyPlaylist> apply(SpotifyUser spotifyUser) {
                    if (spotifyUser == null) {
                      Toast.makeText(getContext(), R.string.generic_error, Toast.LENGTH_SHORT)
                          .show();
                    }

                    mSpotifyUserId = spotifyUser.getId();

                    String playlistName =
                        mShow.getBand() + ", " + mShow.getVenue() + ", " + mShow.getDate();
                    Map<String, String> playlistCreationParams = new HashMap<>();
                    playlistCreationParams.put("name", playlistName);

                    return RetrofitClient.getSpotifyService()
                        .createPlaylist(accessTokenHeader, mSpotifyUserId, playlistCreationParams);
                  }
                })
            .subscribeOn(Schedulers.io()) // Work on IO thread
            .observeOn(AndroidSchedulers.mainThread()) // Observe on UI thread
            .subscribeWith(
                new DisposableSingleObserver<SpotifyPlaylist>() {
                  @Override
                  public void onSuccess(SpotifyPlaylist spotifyPlaylist) {
                    if (spotifyPlaylist == null) {
                      Toast.makeText(getContext(), R.string.generic_error, Toast.LENGTH_SHORT)
                          .show();
                    }

                    new PlaylistCreator().execute(mSpotifyUserId, spotifyPlaylist.getId());
                  }

                  @Override
                  public void onError(Throwable e) {
                    Toast.makeText(getContext(), R.string.generic_error, Toast.LENGTH_SHORT).show();
                    Log.e(SetlistFragment.class.getSimpleName(), e.toString());
                  }
                }));
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

  /**
   * Uses the Spotify API to create a playlist and add the songs from the setlist to the playlist
   */
  private class PlaylistCreator extends AsyncTask<String, Void, Void> {

    @Override
    protected Void doInBackground(String... strings) {
      String userId = strings[0];
      String playlistId = strings[1];
      String createPlaylistUrl = "https://api.spotify.com/v1/users/" + userId + "/playlists";

      StringBuilder tracks = new StringBuilder();
      int numSongsAdded = 0;

      // Add songs one at a time
      for (String s : mShow.getSongs()) {
        // Only 100 songs can be added through the API
        if (numSongsAdded > 100) {
          mFailedSpotifySongs.add(s);
        }

        String songQuery = s.replace(' ', '+');
        String artistQuery = mShow.getBand().replace(' ', '+');
        try {
          JSONObject trackJson =
              JSONRetriever.getRequest(
                  "https://api.spotify.com/v1/search?q=track:"
                      + songQuery
                      + "%20artist:"
                      + artistQuery
                      + "&type=track&limit=5",
                  "Bearer",
                  mAccessToken);
          JSONObject tracking = trackJson.getJSONObject("tracks");
          JSONArray items = tracking.getJSONArray("items");
          JSONObject firstChoice = (JSONObject) items.get(0);

          // The first match isn't always the best one (e.g. X remix), so we check if
          // any of the top 5 are an exact match to X
          for (int i = 0; i < items.length(); i++) {
            JSONObject currentTrack = (JSONObject) items.get(i);
            if (currentTrack.getString("name").equals(s)) {
              firstChoice = currentTrack;
              break;
            }
          }

          tracks.append(firstChoice.getString("uri"));

          tracks.append(",");
          numSongsAdded++;
        } catch (JSONException e) {
          mFailedSpotifySongs.add(s);
        } catch (IOException e) {
          mFailedSpotifySongs.add(s);
        }
      }

      tracks.deleteCharAt(tracks.length() - 1); // Delete last comma

      String addSongsUrl =
          createPlaylistUrl + "/" + playlistId + "/tracks?uris=" + tracks.toString();
      JSONRetriever.postRequest(addSongsUrl, "Bearer", mAccessToken, null);

      return null;
    }

    @SuppressLint("WrongConstant")
    @Override
    protected void onPostExecute(Void aVoid) {
      if (getActivity() == null) {
        return;
      }

      Snackbar snackbar =
          Snackbar.make(
              mRootView,
              getString(R.string.spotify_playlist_created_snackbar),
              Snackbar.LENGTH_SHORT);

      // If there were missed songs, give the user the option to see what they were
      if (!mFailedSpotifySongs.isEmpty()) {
        snackbar
            .setDuration(Snackbar.LENGTH_LONG)
            .setAction(
                getResources()
                    .getQuantityString(
                        R.plurals.spotify_missing_songs_snackbar,
                        mFailedSpotifySongs.size(),
                        mFailedSpotifySongs.size()),
                new View.OnClickListener() {
                  @Override
                  public void onClick(View view) {
                    StringBuilder content = new StringBuilder();
                    content.append(getString(R.string.spotify_missing_songs_dialog_body));
                    content.append("\n");
                    for (String s : mFailedSpotifySongs) {
                      content.append("\n");
                      content.append("• ");
                      content.append(s);
                    }

                    new MaterialAlertDialogBuilder(getActivity())
                        .setTitle(R.string.spotify_missing_songs_dialog_title)
                        .setMessage(content)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
                  }
                })
            .setActionTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
      }
      snackbar.show();
      super.onPostExecute(aVoid);
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    mCompositeDisposable.clear();
  }
}
