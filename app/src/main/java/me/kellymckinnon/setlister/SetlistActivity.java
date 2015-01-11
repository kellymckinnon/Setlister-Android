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
import android.os.PersistableBundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ShareActionProvider;

import java.util.ArrayList;


public class SetlistActivity extends ActionBarActivity {

    String[] songs;
    String artist;
    String date;
    String venue;
    String tour;
    String accessToken;
    ArrayList<String> failedSpotifySongs = new ArrayList<String>();
    private android.support.v7.widget.ShareActionProvider mShareActionProvider;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_setlist, menu);
        MenuItem item = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (android.support.v7.widget.ShareActionProvider) MenuItemCompat.getActionProvider(item);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        if(artist != null) {
            intent.putExtra(Intent.EXTRA_SUBJECT, "SETLISTER: " + artist + " on " + date);
            StringBuilder text = new StringBuilder();
            text.append("SETLISTER: ").append(artist).append(" on ").append(date).append(" at ").append(venue).append(":\n");
            for(String s : songs) {
                text.append("\n");
                text.append(s);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.putExtra(Intent.EXTRA_TEXT, text.toString());
        }
        mShareActionProvider.setShareIntent(intent);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_about) {
            return true;
        } else if (id == R.id.action_feedback) {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", "setlisterapp@gmail.com", null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Setlister Feedback");
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
            String formattedDate = Utility.formatDate(date, "MM/dd/yyyy", "MMMM d, yyyy");
            ab.setTitle(formattedDate);
            ab.setSubtitle(artist);
        }

        if(mShareActionProvider != null) {
            Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "SETLISTER: " + artist + " on " + date);
            StringBuilder text = new StringBuilder();
            text.append("SETLISTER: ").append(artist).append(" setlist on ").append(date).append(" at ").append(venue).append(":");
            for(String s : songs) {
                text.append("\n");
                text.append(s);
            }
            intent.putExtra(Intent.EXTRA_TEXT, text.toString());
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            mShareActionProvider.setShareIntent(intent);
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri uri = intent.getData();

        if (uri == null) {
            Snackbar.with(getApplicationContext())
                    .duration(Snackbar.SnackbarDuration.LENGTH_LONG)
                    .text("Connecting to Spotify failed. Try again.")
                    .show(this);
            return;
        }

        AuthenticationResponse response = SpotifyAuthentication.parseOauthResponse(uri);
        accessToken = response.getAccessToken();
        Snackbar.with(getApplicationContext()).text("Creating playlist...").show(SetlistActivity.this);
        failedSpotifySongs = new ArrayList<String>();
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

    public class PlaylistCreator extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                // Get username, which we need to create a playlist
                JSONObject userJson = JSONRetriever.getRequest("https://api.spotify.com/v1/me",
                        "Bearer", accessToken);

                String username = userJson.getString("id");

                System.out.println("THE USERNAME IS " + username);

                // Create an empty playlist for the authenticated user
                String createPlaylistUrl = "https://api.spotify.com/v1/users/" + username
                        + "/playlists";

                JSONObject playlistInfo = new JSONObject();
                playlistInfo.put("name", artist + ", " + date);
                playlistInfo.put("public", "true");

                JSONObject createPlaylistJson = JSONRetriever.postRequest(createPlaylistUrl,
                        "Bearer", accessToken, playlistInfo);

                System.out.println("The playlist name is: " + createPlaylistJson.getString("name"));

                String playlistId = createPlaylistJson.getString("id");
                StringBuilder tracks = new StringBuilder();

                int numSongsAdded = 0;
                for (String s : songs) {
                    if (numSongsAdded > 100) {
                        failedSpotifySongs.add(s);
                    }

                    String songQuery = s.replace(' ', '+');
                    String artistQuery = artist.replace(' ', '+');
                    try {
                        //TODO: Make the limit 2, and compare the popularity so we don't actually
                        // get
                        //Digital Renegade (instrumental) instead of Digital Renegade
                        JSONObject trackJson = JSONRetriever.getRequest(
                                "https://api.spotify.com/v1/search?q=track:" + songQuery
                                        + "%20artist:" + artistQuery + "&type=track&limit=1");
                        JSONObject tracking = trackJson.getJSONObject("tracks");
                        JSONArray items = tracking.getJSONArray("items");

                        tracks.append(((JSONObject) items.get(0)).getString("uri"));
                        tracks.append(",");
                        numSongsAdded++;
                    } catch (JSONException e) {
                        Log.e("SetlistActivity",
                                "Track: " + s + " for artist: " + artist + " was not found.");
                        failedSpotifySongs.add(s);
                    }
                }

                tracks.deleteCharAt(tracks.length() - 1); // Delete last comma

                String addSongsUrl = createPlaylistUrl + "/" + playlistId + "/tracks?uris=" + tracks
                        .toString();
                System.out.println("The add songs URL is: " + addSongsUrl);

                JSONRetriever.postRequest(addSongsUrl, "Bearer", accessToken, null);

            } catch (JSONException e) {
                Log.e("SetlistActivity", "JSONException");
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Snackbar s = Snackbar.with(getApplicationContext()).text("Playlist created!");
            if (!failedSpotifySongs.isEmpty()) {
                s.duration(Snackbar.SnackbarDuration.LENGTH_LONG);
                s.actionLabel("Show " + failedSpotifySongs.size() + " missing songs");
                s.actionColorResource(R.color.my_accent);
                s.actionListener(new ActionClickListener() {
                    @Override
                    public void onActionClicked(Snackbar snackbar) {
                        StringBuilder content = new StringBuilder();
                        content.append("The following songs were not found on Spotify:\n");
                        for(String s : failedSpotifySongs) {
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
