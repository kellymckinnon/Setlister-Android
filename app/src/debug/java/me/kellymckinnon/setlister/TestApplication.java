package me.kellymckinnon.setlister;

public class TestApplication extends SetlisterApplication {

  @Override
  public String getSetlistFMUrl() {
    return "http://localhost:8080/";
  }

  @Override
  public String getSpotifyBaseUrl() {
    return "http://localhost:8080/";
  }
}
