package me.kellymckinnon.setlister;

import com.spotify.sdk.android.Spotify;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.authentication.SpotifyAuthentication;
import com.spotify.sdk.android.playback.Player;

import android.animation.StateListAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Config;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class SetlistActivity extends ActionBarActivity {

    String [] songs;
    String artist;
    String date;
    String venue;
    String tour;

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

        if(savedInstanceState == null) {
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
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
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
        if (uri != null) {
            AuthenticationResponse response = SpotifyAuthentication.parseOauthResponse(uri);

            // THIS IS WHAT'S IMPORTANT
            // TODO: Save this so authentication is not necessary every time -- needs a refresh token (see web API)
            String accessToken = response.getAccessToken();
        }
    }
}
