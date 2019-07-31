package me.kellymckinnon.setlister.network;

import android.content.Intent;
import androidx.fragment.app.Fragment;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import me.kellymckinnon.setlister.SetlisterConstants;

/** Connects to Spotify to authenticate user */
public class SpotifyHandler {

  private static final String CLIENT_ID = "ff3ee1e9272442a0bd941aae3e4f3649";
  private static final String REDIRECT_URI = "setlister://callback";
  private static final String[] permissionsNeeded = {"playlist-modify-public"};

  public static void authenticateUser(Fragment fragment) {
    if (fragment == null || fragment.getActivity() == null) {
      return;
    }

    AuthenticationRequest.Builder builder =
        new AuthenticationRequest.Builder(
            CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);

    builder.setScopes(permissionsNeeded);
    AuthenticationRequest request = builder.build();

    Intent intent = AuthenticationClient.createLoginActivityIntent(fragment.getActivity(), request);
    fragment.startActivityForResult(intent, SetlisterConstants.SPOTIFY_LOGIN_ACTIVITY_ID);
  }
}
