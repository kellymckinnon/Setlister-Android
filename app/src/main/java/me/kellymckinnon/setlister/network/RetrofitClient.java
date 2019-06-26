package me.kellymckinnon.setlister.network;

import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

/** Interface used by Retrofit to issue network requests to the setlist.fm API */
public class RetrofitClient {

  private static Retrofit retrofit = null;

  public static Retrofit getClient(String baseUrl) {
    if (retrofit == null) {
      retrofit =
          new Retrofit.Builder()
              .baseUrl(baseUrl)
              .addConverterFactory(MoshiConverterFactory.create())
              .build();
    }
    return retrofit;
  }
}
