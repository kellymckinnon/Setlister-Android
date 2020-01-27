package me.kellymckinnon.setlister.network;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;

/** Interface used by Retrofit to issue network requests to the setlist.fm API */
public class RetrofitClient {

  private static Retrofit setlistFMRetrofit = null;
  private static Retrofit spotifyRetrofit = null;

  public static SetlistFMService getSetlistFMService(String url) {
    return getSetlistFMClient(url).create(SetlistFMService.class);
  }

  public static SpotifyService getSpotifyService(String url) {
    return getSpotifyClient(url).create(SpotifyService.class);
  }

  private static Retrofit getSetlistFMClient(String url) {
    if (setlistFMRetrofit == null) {
      setlistFMRetrofit =
          new Retrofit.Builder()
              .baseUrl(url)
              .addConverterFactory(MoshiConverterFactory.create())
              .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
              .build();
    }
    return setlistFMRetrofit;
  }

  private static Retrofit getSpotifyClient(String url) {
    if (spotifyRetrofit == null) {
      spotifyRetrofit =
          new Retrofit.Builder()
              .baseUrl(url)
              .addConverterFactory(MoshiConverterFactory.create())
              .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
              .build();
    }
    return spotifyRetrofit;
  }
}
