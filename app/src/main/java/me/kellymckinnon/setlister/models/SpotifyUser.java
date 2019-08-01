package me.kellymckinnon.setlister.models;

import com.squareup.moshi.Json;

public class SpotifyUser {

  @Json(name = "id")
  private String id;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}
