package me.kellymckinnon.setlister.models;

import com.squareup.moshi.Json;
import java.util.List;

public class Sets {

  @Json(name = "set")
  private List<Set> set = null;

  public List<Set> getSet() {
    return set;
  }

  public void setSet(List<Set> set) {
    this.set = set;
  }
}
