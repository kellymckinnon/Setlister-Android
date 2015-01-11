package me.kellymckinnon.setlister.network;

import com.spotify.sdk.android.authentication.SpotifyAuthentication;

import android.app.Activity;

/**
 * Connects to Spotify to authenticate user
 */
public class SpotifyHandler {

    private static final String CLIENT_ID = "ff3ee1e9272442a0bd941aae3e4f3649";
    private static final String REDIRECT_URI = "setlister://callback";
    private static String[] permissionsNeeded = {"playlist-modify-public"};

    public static void authenticateUser(Activity activity) {
        SpotifyAuthentication.openAuthWindow(CLIENT_ID, "token", REDIRECT_URI,
                permissionsNeeded, null, activity);
    }

}
