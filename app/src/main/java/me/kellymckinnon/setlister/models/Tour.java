package me.kellymckinnon.setlister.models;

import com.squareup.moshi.Json;

public class Tour {

  @Json(name = "name")
  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
