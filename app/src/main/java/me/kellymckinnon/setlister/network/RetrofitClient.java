package me.kellymckinnon.setlister.network;

import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

/** Interface used by Retrofit to issue network requests to the setlist.fm API */
public class RetrofitClient {

  private static final String SETLIST_FM_BASE_URL = "https://api.setlist.fm/rest/1.0/";

  private static Retrofit retrofit = null;

  public static SetlistFMService getSetlistFMService() {
    return getClient().create(SetlistFMService.class);
  }

  private static Retrofit getClient() {
    if (retrofit == null) {
      retrofit =
          new Retrofit.Builder()
              .baseUrl(SETLIST_FM_BASE_URL)
              .addConverterFactory(MoshiConverterFactory.create())
              .build();
    }
    return retrofit;
  }
}
