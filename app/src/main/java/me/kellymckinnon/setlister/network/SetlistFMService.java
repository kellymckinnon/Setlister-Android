package me.kellymckinnon.setlister.network;

import me.kellymckinnon.setlister.models.Artists;
import me.kellymckinnon.setlister.models.Setlists;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Interface that contains methods needed to execute requests to setlist.fm. */
public interface SetlistFMService {

  @Headers({
    "x-api-key: bc296136-5d85-4737-8a5e-83b84bc223f9",
    "Accept: application/json",
    "Content-Type: application/json"
  })
  @GET("search/artists/")
  Call<Artists> getArtists(@Query("artistName") String artistName);

  @Headers({
    "x-api-key: bc296136-5d85-4737-8a5e-83b84bc223f9",
    "Accept: application/json",
    "Content-Type: application/json"
  })
  @GET("artist/{mbid}/setlists")
  Call<Setlists> getSetlistsByArtistMbid(@Path("mbid") String mbid, @Query("p") int page);

  @Headers({
    "x-api-key: bc296136-5d85-4737-8a5e-83b84bc223f9",
    "Accept: application/json",
    "Content-Type: application/json"
  })
  @GET("search/setlists")
  Call<Setlists> getSetlistsByArtistName(@Query("artistName") String mbid, @Query("p") int page);
}
