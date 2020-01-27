package me.kellymckinnon.setlister;

import android.app.Application;

public class SetlisterApplication extends Application {

  private static final String SETLIST_FM_BASE_URL = "https://api.setlist.fm/rest/1.0/";
  private static final String SPOTIFY_BASE_URL = "https://api.spotify.com/v1/";

  public String getSetlistFMUrl() {
    return SETLIST_FM_BASE_URL;
  }

  public String getSpotifyBaseUrl() {
    return SPOTIFY_BASE_URL;
  }
}
