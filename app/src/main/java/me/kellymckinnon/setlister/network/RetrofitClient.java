package me.kellymckinnon.setlister.network;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;

/** Interface used by Retrofit to issue network requests to the setlist.fm API */
public class RetrofitClient {

  private static final String SETLIST_FM_BASE_URL = "https://api.setlist.fm/rest/1.0/";
  private static final String SPOTIFY_BASE_URL = "https://api.spotify.com/v1/";

  private static Retrofit setlistFMRetrofit = null;
  private static Retrofit spotifyRetrofit = null;

  public static SetlistFMService getSetlistFMService() {
    return getSetlistFMClient().create(SetlistFMService.class);
  }

  public static SpotifyService getSpotifyService() {
    return getSpotifyClient().create(SpotifyService.class);
  }

  private static Retrofit getSetlistFMClient() {
    if (setlistFMRetrofit == null) {
      setlistFMRetrofit =
          new Retrofit.Builder()
              .baseUrl(SETLIST_FM_BASE_URL)
              .addConverterFactory(MoshiConverterFactory.create())
              .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
              .build();
    }
    return setlistFMRetrofit;
  }

  private static Retrofit getSpotifyClient() {
    if (spotifyRetrofit == null) {
      spotifyRetrofit =
          new Retrofit.Builder()
              .baseUrl(SPOTIFY_BASE_URL)
              .addConverterFactory(MoshiConverterFactory.create())
              .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
              .build();
    }
    return spotifyRetrofit;
  }
}
