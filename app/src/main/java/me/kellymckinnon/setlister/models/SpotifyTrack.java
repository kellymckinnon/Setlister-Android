package me.kellymckinnon.setlister.models;

import com.squareup.moshi.Json;

public class SpotifyTrack {

  @Json(name = "name")
  private String name;

  @Json(name = "uri")
  private String uri;

  public String getName() {
    return name;
  }

  public String getUri() {
    return uri;
  }
}
