package me.kellymckinnon.setlister.network;

import io.reactivex.Single;
import java.util.Map;
import me.kellymckinnon.setlister.models.SpotifyPlaylist;
import me.kellymckinnon.setlister.models.SpotifyUser;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SpotifyService {

  /**
   * Get the currently logged in user profile information.
   *
   * @param accessToken accessToken
   * @see <a href="https://developer.spotify.com/web-api/get-current-users-profile/">Get Current
   *     User's Profile</a>
   */
  @GET("me")
  Single<SpotifyUser> getUser(@Header("Authorization") String accessToken);

  /**
   * Create a playlist
   *
   * @param userId The playlist's owner's User ID
   * @param body The body parameters
   * @param callback Callback method
   * @see <a href="https://developer.spotify.com/web-api/create-playlist/">Create a Playlist</a>
   */
  @POST("users/{user_id}/playlists")
  Single<SpotifyPlaylist> createPlaylist(
      @Header("Authorization") String accessToken,
      @Path("user_id") String userId,
      @Body Map<String, String> body);
}
