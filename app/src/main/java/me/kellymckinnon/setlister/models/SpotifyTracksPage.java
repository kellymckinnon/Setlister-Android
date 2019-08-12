package me.kellymckinnon.setlister.models;

import com.squareup.moshi.Json;

public class SpotifyTracksPage {
  @Json(name = "tracks")
  private SpotifyTrackList trackList;

  public SpotifyTrackList getTrackList() {
    return trackList;
  }
}
