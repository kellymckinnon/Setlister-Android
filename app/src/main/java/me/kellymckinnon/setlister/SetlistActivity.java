package me.kellymckinnon.setlister;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.authentication.SpotifyAuthentication;

import me.kellymckinnon.setlister.models.Show;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import me.kellymckinnon.setlister.fragments.SetlistFragment;
import me.kellymckinnon.setlister.utils.JSONRetriever;
import me.kellymckinnon.setlister.utils.Utility;

/**
 * Final activity which uses a SetlistFragment to display the setlist for the selected show, and
 * gives the option to create a Spotify playlist out of this setlist.
 */
public class SetlistActivity extends AppCompatActivity {

  private String[] mSongs;
  private String mArtist, mDate, mVenue, mTour;
  private String mAccessToken;
  private ArrayList<String> mFailedSpotifySongs = new ArrayList<>();
  private ShareActionProvider mShareActionProvider;

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_setlister, menu);
    MenuItem item = menu.findItem(R.id.action_share);
    mShareActionProvider =
        (ShareActionProvider) MenuItemCompat.getActionProvider(item);
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType("text/plain");
    mShareActionProvider.setShareIntent(intent); // dummy, in case
    if (mArtist != null) {
      updateShareIntent();
    }
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    int id = item.getItemId();

    if (id == R.id.action_about) {
      new MaterialAlertDialogBuilder(this)
              .setTitle(R.string.about_setlister)
              .setView(R.layout.about_dialog)
              .show();
      return true;
    } else if (id == R.id.action_feedback) {
      Intent emailIntent =
          new Intent(
              Intent.ACTION_SENDTO, Uri.fromParts("mailto", getString(R.string.email), null));
      emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_subject));
      startActivity(Intent.createChooser(emailIntent, getString(R.string.send_email)));
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Bundle arguments;

    if (savedInstanceState == null) {
      arguments = getIntent().getExtras();
    } else {
      arguments = savedInstanceState;
    }

    Show show = arguments.getParcelable(SetlisterExtras.EXTRA_SHOW);

    ActionBar ab = getSupportActionBar();
    if (ab != null) {
      String formattedDate = Utility.formatDate(show.getDate(), "MM/dd/yyyy", "MMMM d, yyyy");
      ab.setTitle(formattedDate);
      ab.setSubtitle(mArtist);
    }

    if (mShareActionProvider != null) {
      updateShareIntent();
    }

    setContentView(R.layout.activity_setlist);

    SetlistFragment sf = new SetlistFragment();
    Bundle bundle = new Bundle();
    bundle.putParcelable(SetlisterExtras.EXTRA_SHOW, show);

    sf.setArguments(bundle);
    getSupportFragmentManager().beginTransaction().add(R.id.activity_setlist, sf).commit();
  }

  /** Called on return from Spotify authentication */
  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    Uri uri = intent.getData();

    // Spotify authorization failed
    if (uri == null) {
      Snackbar.make(
              findViewById(android.R.id.content),
              getString(R.string.spotify_connection_failed_snackbar),
              Snackbar.LENGTH_SHORT)
          .show();
      return;
    }

    // Create playlist in Spotify from setlist
    AuthenticationResponse response = SpotifyAuthentication.parseOauthResponse(uri);
    mAccessToken = response.getAccessToken();

    Snackbar.make(
            findViewById(android.R.id.content),
            getString(R.string.spotify_creating_playlist_snackbar),
            Snackbar.LENGTH_SHORT)
        .show();

    mFailedSpotifySongs = new ArrayList<>();
    new PlaylistCreator().execute();
  }

  /** Provide information for share button */
  private void updateShareIntent() {
    String shareTitle = getString(R.string.setlist_share_title, mArtist, mDate, mVenue);

    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType("text/plain");
    intent.putExtra(Intent.EXTRA_SUBJECT, shareTitle);
    StringBuilder text = new StringBuilder();
    text.append(shareTitle)
        .append(":\n");
    for (String s : mSongs) {
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
  private class PlaylistCreator extends AsyncTask<Void, Void, Void> {

    @Override
    protected Void doInBackground(Void... params) {
      try {
        // Get username, which we need to create a playlist
        JSONObject userJson =
            JSONRetriever.getRequest("https://api.spotify.com/v1/me", "Bearer", mAccessToken);
        String username = userJson.getString("id");

        // Create an empty playlist for the authenticated user
        String createPlaylistUrl = "https://api.spotify.com/v1/users/" + username + "/playlists";
        JSONObject playlistInfo = new JSONObject();
        playlistInfo.put("name", mArtist + ", " + mVenue + ", " + mDate);
        playlistInfo.put("public", "true");
        JSONObject createPlaylistJson =
            JSONRetriever.postRequest(createPlaylistUrl, "Bearer", mAccessToken, playlistInfo);

        // Get the newly created playlist so the fun can begin
        String playlistId = createPlaylistJson.getString("id");
        StringBuilder tracks = new StringBuilder();
        int numSongsAdded = 0;

        // Add songs one at a time
        for (String s : mSongs) {
          // Only 100 songs can be added through the API
          if (numSongsAdded > 100) {
            mFailedSpotifySongs.add(s);
          }

          String songQuery = s.replace(' ', '+');
          String artistQuery = mArtist.replace(' ', '+');
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
      } catch (JSONException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }

      return null;
    }

    @SuppressLint("WrongConstant")
    @Override
    protected void onPostExecute(Void aVoid) {
      Snackbar snackbar =
          Snackbar.make(
              findViewById(android.R.id.content),
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

                    new MaterialAlertDialogBuilder(SetlistActivity.this)
                            .setTitle(R.string.spotify_missing_songs_dialog_title)
                            .setMessage(content)
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                  }
                })
            .setActionTextColor(ContextCompat.getColor(SetlistActivity.this, R.color.colorAccent));
      }
      snackbar.show();
      super.onPostExecute(aVoid);
    }
  }
}
