package me.kellymckinnon.setlister.network;

import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.Map;
import me.kellymckinnon.setlister.models.SpotifyPlaylist;
import me.kellymckinnon.setlister.models.SpotifyTracksPage;
import me.kellymckinnon.setlister.models.SpotifyUser;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Interacts with the Spotify API to perform various actions.
 */
public interface SpotifyService {

  String PLAYLIST_NAME_EXTRA = "name";

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
   * @param accessToken accessToken
   * @param userId The playlist's owner's User ID
   * @param body The body parameters
   * @see <a href="https://developer.spotify.com/web-api/create-playlist/">Create a Playlist</a>
   */
  @POST("users/{user_id}/playlists")
  Single<SpotifyPlaylist> createPlaylist(
      @Header("Authorization") String accessToken,
      @Path("user_id") String userId,
      @Body Map<String, String> body);

  /**
   * Get Spotify catalog information about tracks that match a keyword string.
   *
   * @param accessToken accessToken
   * @param query The search query's keywords (and optional field filters and operators), for example
   *     "roadhouse+blues"
   * @see <a href="https://developer.spotify.com/web-api/search-item/">Search for an Item</a>
   */
  @GET("search?type=track&limit=5")
  Observable<SpotifyTracksPage> searchTracks(
      @Header("Authorization") String accessToken, @Query("q") String query);

  /**
   * Add tracks to a playlist
   *
   * @param accessToken accessToken
   * @param playlistId The playlist's ID
   * @param trackUris list of URIs of tracks to add to the playlist, as a comma-separated string
   * @return A snapshot ID (the version of the playlist)
   * @see <a href="https://developer.spotify.com/web-api/add-tracks-to-playlist/">Add Tracks to a
   *     Playlist</a>
   */
  @POST("playlists/{playlist_id}/tracks")
  Single<ResponseBody> addTracksToPlaylist(
      @Header("Authorization") String accessToken,
      @Path("playlist_id") String playlistId,
      @Query("uris") String trackUris);
}

