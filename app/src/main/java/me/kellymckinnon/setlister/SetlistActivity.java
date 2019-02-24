package me.kellymckinnon.setlister;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.listeners.ActionClickListener;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.authentication.SpotifyAuthentication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;
import java.util.ArrayList;

import me.kellymckinnon.setlister.fragments.SetlistFragment;
import me.kellymckinnon.setlister.utils.JSONRetriever;
import me.kellymckinnon.setlister.utils.Utility;

/**
 * Final activity which uses a SetlistFragment to display the
 * setlist for the selected show, and gives the option to
 * create a Spotify playlist out of this setlist.
 */
public class SetlistActivity extends AppCompatActivity {

    private String[] songs;
    private String artist, date, venue, tour;
    private String accessToken;
    private ArrayList<String> failedSpotifySongs = new ArrayList<>();
    private androidx.appcompat.widget.ShareActionProvider mShareActionProvider;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_setlist, menu);
        MenuItem item = menu.findItem(R.id.menu_item_share);
        mShareActionProvider
                = (androidx.appcompat.widget.ShareActionProvider) MenuItemCompat.getActionProvider(
                item);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        mShareActionProvider.setShareIntent(intent); //dummy, in case
        if (artist != null) {
            updateShareIntent();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_about) {
            new MaterialDialog.Builder(this)
                    .title("About Setlister")
                    .customView(R.layout.about_dialog, true)
                    .positiveText("OK")
                    .show();
            return true;
        } else if (id == R.id.action_feedback) {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", getString(R.string.email), null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_subject));
            startActivity(Intent.createChooser(emailIntent, "Send email..."));
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

        songs = arguments.getStringArray("SONGS");
        artist = arguments.getString("ARTIST");
        date = arguments.getString("DATE");
        venue = arguments.getString("VENUE");
        tour = arguments.getString("TOUR");

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            String formattedDate = Utility.formatDate(date, "MM/dd/yyyy",
                    "MMMM d, yyyy");
            ab.setTitle(formattedDate);
            ab.setSubtitle(artist);
        }

        if (mShareActionProvider != null) {
            updateShareIntent();
        }

        setContentView(R.layout.activity_setlist);

        SetlistFragment sf = new SetlistFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArray("SONGS", songs);
        bundle.putString("ARTIST", artist);
        bundle.putString("DATE", date);
        bundle.putString("TOUR", tour);
        bundle.putString("VENUE", venue);

        sf.setArguments(bundle);
        getFragmentManager().beginTransaction()
                .add(R.id.activity_setlist, sf)
                .commit();
    }

    /**
     * Called on return from Spotify authentication
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri uri = intent.getData();

        // Spotify authorization failed
        if (uri == null) {
            Snackbar.with(getApplicationContext())
                    .duration(Snackbar.SnackbarDuration.LENGTH_LONG)
                    .text("Connecting to Spotify failed. Try again.")
                    .show(this);
            return;
        }

        // Create playlist in Spotify from setlist
        AuthenticationResponse response = SpotifyAuthentication.parseOauthResponse(uri);
        accessToken = response.getAccessToken();
        Snackbar.with(getApplicationContext())
                .text("Creating playlist...")
                .show(SetlistActivity.this);
        failedSpotifySongs = new ArrayList<>();
        new PlaylistCreator().execute();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArray("SONGS", songs);
        outState.putString("ARTIST", artist);
        outState.putString("DATE", date);
        outState.putString("TOUR", tour);
        outState.putString("VENUE", venue);
    }

    /**
     * Provide information for share button
     */
    private void updateShareIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "SETLISTER: " + artist + " on " + date);
        StringBuilder text = new StringBuilder();
        text.append("SETLISTER: ")
                .append(artist)
                .append(" on ")
                .append(date)
                .append(" at ")
                .append(venue)
                .append(":\n");
        for (String s : songs) {
            text.append("\n");
            text.append(s);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.putExtra(Intent.EXTRA_TEXT, text.toString());
        mShareActionProvider.setShareIntent(intent);
    }

    /**
     * Uses the Spotify API to create a playlist and add the songs
     * from the setlist to the playlist
     */
    private class PlaylistCreator extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                // Get username, which we need to create a playlist
                JSONObject userJson = JSONRetriever.getRequest(
                        "https://api.spotify.com/v1/me",
                        "Bearer", accessToken);
                String username = userJson.getString("id");

                // Create an empty playlist for the authenticated user
                String createPlaylistUrl = "https://api.spotify.com/v1/users/" + username
                        + "/playlists";
                JSONObject playlistInfo = new JSONObject();
                playlistInfo.put("name", artist + ", " + venue + ", " + date);
                playlistInfo.put("public", "true");
                JSONObject createPlaylistJson = JSONRetriever.postRequest(
                        createPlaylistUrl,
                        "Bearer", accessToken, playlistInfo);

                // Get the newly created playlist so the fun can begin
                String playlistId = createPlaylistJson.getString("id");
                StringBuilder tracks = new StringBuilder();
                int numSongsAdded = 0;

                // Add songs one at a time
                for (String s : songs) {
                    // Only 100 songs can be added through the API
                    if (numSongsAdded > 100) {
                        failedSpotifySongs.add(s);
                    }

                    String songQuery = s.replace(' ', '+');
                    String artistQuery = artist.replace(' ', '+');
                    try {
                        JSONObject trackJson = JSONRetriever.getRequest(
                                "https://api.spotify.com/v1/search?q=track:" + songQuery
                                        + "%20artist:" + artistQuery + "&type=track&limit=5",
                                "Bearer",
                                accessToken);
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
                        failedSpotifySongs.add(s);
                    } catch (IOException e) {
                        failedSpotifySongs.add(s);
                    }
                }

                tracks.deleteCharAt(tracks.length() - 1); // Delete last comma

                String addSongsUrl = createPlaylistUrl + "/" + playlistId + "/tracks?uris=" + tracks
                        .toString();
                JSONRetriever.postRequest(addSongsUrl, "Bearer",
                        accessToken, null);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Snackbar s = Snackbar.with(getApplicationContext()).text("Playlist created!");

            // If there were missed songs, give the user the option to see what they were
            if (!failedSpotifySongs.isEmpty()) {
                s.duration(Snackbar.SnackbarDuration.LENGTH_LONG);
                s.actionLabel("Show " + failedSpotifySongs.size() + " missing songs");
                s.actionColorResource(R.color.my_accent);
                s.actionListener(new ActionClickListener() {
                    @Override
                    public void onActionClicked(Snackbar snackbar) {
                        StringBuilder content = new StringBuilder();
                        content.append("The following songs were not found on Spotify:\n");
                        for (String s : failedSpotifySongs) {
                            content.append("\n");
                            content.append("• ");
                            content.append(s);
                        }

                        new MaterialDialog.Builder(SetlistActivity.this)
                                .title("Missing songs")
                                .content(content)
                                .positiveText("OK")
                                .show();
                    }
                });
            }
            s.show(SetlistActivity.this);
            super.onPostExecute(aVoid);
        }

    }
}
