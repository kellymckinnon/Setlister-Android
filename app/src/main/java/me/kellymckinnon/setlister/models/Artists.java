package me.kellymckinnon.setlister.models;

import com.squareup.moshi.Json;
import java.util.List;

public class Artists {

  @Json(name = "artist")
  private List<Artist> artist = null;

  @Json(name = "total")
  private Integer total;

  public List<Artist> getArtist() {
    return artist;
  }

  public void setArtist(List<Artist> artist) {
    this.artist = artist;
  }

  public Integer getTotal() {
    return total;
  }

  public void setTotal(Integer total) {
    this.total = total;
  }
}
