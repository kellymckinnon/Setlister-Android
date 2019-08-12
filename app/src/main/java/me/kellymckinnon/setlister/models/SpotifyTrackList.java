package me.kellymckinnon.setlister.models;

import com.squareup.moshi.Json;
import java.util.List;

public class SpotifyTrackList {
  @Json(name = "items")
  private List<SpotifyTrack> tracks;

  public List<SpotifyTrack> getTracks() {
    return tracks;
  }
}
