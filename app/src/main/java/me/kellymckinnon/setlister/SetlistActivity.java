package me.kellymckinnon.setlister;

import com.spotify.sdk.android.Spotify;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.authentication.SpotifyAuthentication;
import com.spotify.sdk.android.playback.Player;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.animation.StateListAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Config;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


public class SetlistActivity extends ActionBarActivity {

    String[] songs;
    String artist;
    String date;
    String venue;
    String tour;
    String accessToken;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_setlist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
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
            date = Utility.formatDate(date, "MM/dd/yyyy", "MMMM d, yyyy");
            ab.setTitle(date);
            ab.setSubtitle(artist);
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArray("SONGS", songs);
        outState.putString("ARTIST", artist);
        outState.putString("DATE", date);
        outState.putString("TOUR", tour);
        outState.putString("VENUE", venue);
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
            //TODO: replace this with user error message
            throw new RuntimeException("Connecting to Spotify failed.");
        }
        AuthenticationResponse response = SpotifyAuthentication.parseOauthResponse(uri);

        // THIS IS WHAT'S IMPORTANT
        // TODO: Save this so authentication is not necessary every time -- needs a refresh token (see web API)
        accessToken = response.getAccessToken();

        new PlaylistCreator().execute();


    }

    public class PlaylistCreator extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
//            HttpClient client = new DefaultHttpClient(new BasicHttpParams());
//            HttpGet httpGet = new HttpGet("https://api.spotify.com/v1/me");
//            httpGet.setHeader("Content-type", "application/json");
//            httpGet.addHeader("Authorization", "Bearer " + accessToken);

            String username = "";

            JSONObject userJson = JSONRetriever.getJSON("https://api.spotify.com/v1/me", "Bearer", accessToken);

            try {
                username = userJson.getString("id");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            System.out.println("THE USERNAME IS " + username);

            JSONObject createPlaylistJson;
            //TODO: refactor POST logic into JSONRetriever
            try {
                StringBuilder response  = new StringBuilder();
                URL url = new URL("https://api.spotify.com/v1/users/"+username+"/playlists");
                HttpURLConnection httpconn = (HttpURLConnection)url.openConnection();
                httpconn.setRequestProperty("Authorization", "Bearer " + accessToken);
                httpconn.setRequestProperty("Content-Type", "application/json");
                httpconn.setRequestMethod("POST");

                JSONObject playlistInfo = new JSONObject();
                playlistInfo.put("name", "Test Playlist");
                playlistInfo.put("public", "true");

                OutputStreamWriter os = new OutputStreamWriter(httpconn.getOutputStream());
                os.write(playlistInfo.toString());
                os.close();

                if (httpconn.getResponseCode() == HttpURLConnection.HTTP_CREATED)
                {
                    BufferedReader input = new BufferedReader(new InputStreamReader(httpconn.getInputStream()),8192);
                    String strLine = null;
                    while ((strLine = input.readLine()) != null)
                    {
                        response.append(strLine);
                    }
                    input.close();

                    createPlaylistJson = new JSONObject(response.toString());
                    System.out.println("THE PLAYLIST ID IS " + createPlaylistJson.getString("id"));
                } else {
                    Log.e("SetlistActivity", "Playlist creation request received response code " + httpconn.getResponseCode() + "");
                }
            } catch (IOException e) {
                Log.e("JSONRetriever", "IOException");
                e.printStackTrace();
                return null;
            } catch (JSONException e) {
                Log.e("JSONRetriever", "JSONException");
                e.printStackTrace();
                return null;
            }

            //TODO: Add test URIs and add them to the playlist
            //TODO: Get actual URIs of tracks using search



            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

    }
}
