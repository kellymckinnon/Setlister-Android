package me.kellymckinnon.setlister.models;

import com.squareup.moshi.Json;
import java.util.List;

public class Setlists {

  @Json(name = "type")
  private String type;

  @Json(name = "itemsPerPage")
  private Integer itemsPerPage;

  @Json(name = "page")
  private Integer page;

  @Json(name = "total")
  private Integer total;

  @Json(name = "setlist")
  private List<Setlist> setlist = null;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Integer getItemsPerPage() {
    return itemsPerPage;
  }

  public void setItemsPerPage(Integer itemsPerPage) {
    this.itemsPerPage = itemsPerPage;
  }

  public Integer getPage() {
    return page;
  }

  public void setPage(Integer page) {
    this.page = page;
  }

  public Integer getTotal() {
    return total;
  }

  public void setTotal(Integer total) {
    this.total = total;
  }

  public List<Setlist> getSetlist() {
    return setlist;
  }

  public void setSetlist(List<Setlist> setlist) {
    this.setlist = setlist;
  }
}
