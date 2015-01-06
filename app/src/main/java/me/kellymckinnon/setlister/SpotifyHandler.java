package me.kellymckinnon.setlister;

import com.spotify.sdk.android.authentication.SpotifyAuthentication;

import android.app.Activity;

/**
 * Created by kelly on 12/25/14.
 */
public class SpotifyHandler {

    public static final String CLIENT_ID = "ff3ee1e9272442a0bd941aae3e4f3649";
    public static final String REDIRECT_URI = "setlister://callback";
    private static String[] permissionsNeeded = {"playlist-modify-public"};

    public static void authenticateUser(Activity activity) {
        SpotifyAuthentication.openAuthWindow(CLIENT_ID, "token", REDIRECT_URI,
                permissionsNeeded, null, activity);
    }

}
