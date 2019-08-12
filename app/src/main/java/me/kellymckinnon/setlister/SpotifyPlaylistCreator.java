package me.kellymckinnon.setlister;

import static me.kellymckinnon.setlister.network.SpotifyService.PLAYLIST_NAME_EXTRA;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.kellymckinnon.setlister.models.Show;
import me.kellymckinnon.setlister.models.SpotifyPlaylist;
import me.kellymckinnon.setlister.models.SpotifyTrack;
import me.kellymckinnon.setlister.models.SpotifyTracksPage;
import me.kellymckinnon.setlister.models.SpotifyUser;
import me.kellymckinnon.setlister.network.RetrofitClient;
import okhttp3.ResponseBody;

/** Helper class used to create a Spotify playlist and add a set of songs to it */
public class SpotifyPlaylistCreator {

  /**
   * Create a playlist on the user's Spotify with the songs from the given Show.
   *
   * @param auth authorization header String for Spotify - "Bearer + [auth token]"
   * @param rootView root view used for showing snackbar
   * @param context context
   * @param show Show that we're getting the setlist from
   * @return a CompositeDisposable object so that the caller can dispose of it properly
   */
  public static CompositeDisposable createPlaylist(
      String auth, View rootView, Context context, Show show) {
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    Snackbar.make(
            rootView,
            context.getString(R.string.spotify_creating_playlist_snackbar),
            Snackbar.LENGTH_SHORT)
        .show();

    ArrayList<String> failedSongs =
        new ArrayList<>(); // This is where we will store tracks that aren't found

    compositeDisposable.add(
        getCreateSpotifyPlaylistObservable(auth, show)
            .zipWith(
                getFetchTracksObservable(auth, show, failedSongs),
                ((spotifyPlaylist, uris) -> {
                  String concatenatedTrackUris = TextUtils.join(",", uris);
                  String playlistId = spotifyPlaylist.getId();

                  return new Pair<>(playlistId, concatenatedTrackUris);
                }))
            .flatMap(
                (Function<Pair<String, String>, Single<ResponseBody>>)
                    pair ->
                        RetrofitClient.getSpotifyService()
                            .addTracksToPlaylist(auth, pair.first, pair.second))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(
                new DisposableSingleObserver<ResponseBody>() {
                  @Override
                  public void onSuccess(ResponseBody spotifyResponse) {
                    showSuccessSnackbar(context, rootView, failedSongs);
                  }

                  @Override
                  public void onError(Throwable e) {
                    Toast.makeText(context, R.string.generic_error, Toast.LENGTH_SHORT).show();
                    Log.e(SpotifyPlaylistCreator.class.getSimpleName(), e.toString());
                  }
                }));

    return compositeDisposable;
  }

  private static Single<List<String>> getFetchTracksObservable(
      String auth, Show show, ArrayList<String> failedSongs) {
    return Observable.fromArray(show.getSongs())
        .flatMap( // Search Spotify for a list of track suggestions for each song
            setlistFmSongName ->
                RetrofitClient.getSpotifyService()
                    .searchTracks(auth, "track:" + setlistFmSongName.replace("'", "") + " artist:" + show.getBand())
                    .map(
                        spotifySearchResults ->
                            new Pair<>(setlistFmSongName, spotifySearchResults)))
        .subscribeOn(Schedulers.newThread())
        .map( // Get a URI for each track
            pair -> getUriForTrack(pair.first, pair.second, failedSongs))
        .filter(uri -> !uri.equals("")) // Filter out tracks that couldn't be found
        .toList(); // Get a list of the track URIs
  }

  private static String getUriForTrack(
      String setlistFmSongName,
      SpotifyTracksPage spotifySearchResults,
      ArrayList<String> failedSongs) {
    if (spotifySearchResults == null
        || spotifySearchResults.getTrackList() == null
        || spotifySearchResults.getTrackList().getTracks() == null
        || spotifySearchResults.getTrackList().getTracks().isEmpty()) {
      failedSongs.add(setlistFmSongName);
      Log.d(
          SpotifyPlaylistCreator.class.getSimpleName(),
          "Could not find Spotify track for song: " + setlistFmSongName);
      return ""; // Track could not be found on spotify
    }

    // The first match isn't always the best one (e.g. X remix), so we check if
    // any of the top 5 are an exact match to X
    SpotifyTrack firstChoice = spotifySearchResults.getTrackList().getTracks().get(0);
    for (SpotifyTrack track : spotifySearchResults.getTrackList().getTracks()) {
      if (track.getName().equals(setlistFmSongName)) {
        firstChoice = track;
        break;
      }
    }

    return firstChoice.getUri();
  }

  /** This is used to create an empty Spotify playlist. */
  private static Single<SpotifyPlaylist> getCreateSpotifyPlaylistObservable(
      String auth, Show show) {
    return RetrofitClient.getSpotifyService()
        .getUser(auth)
        .subscribeOn(Schedulers.io())
        .flatMap(
            (Function<SpotifyUser, Single<SpotifyPlaylist>>)
                spotifyUser -> {
                  if (spotifyUser == null) {
                    Log.e(SpotifyPlaylistCreator.class.getSimpleName(), "Spotify user was null");
                    return null;
                  }

                  String playlistName =
                      show.getBand() + ", " + show.getVenue() + ", " + show.getDate();
                  Map<String, String> playlistCreationParams = new HashMap<>();
                  playlistCreationParams.put(PLAYLIST_NAME_EXTRA, playlistName);

                  return RetrofitClient.getSpotifyService()
                      .createPlaylist(auth, spotifyUser.getId(), playlistCreationParams);
                });
  }

  @SuppressLint("WrongConstant")
  private static void showSuccessSnackbar(
      Context context, View rootView, ArrayList<String> failedSongs) {
    Snackbar snackbar =
        Snackbar.make(
            rootView,
            context.getString(R.string.spotify_playlist_created_snackbar),
            Snackbar.LENGTH_SHORT);

    if (!failedSongs.isEmpty()) {
      snackbar
          .setDuration(Snackbar.LENGTH_LONG)
          .setAction(
              context
                  .getResources()
                  .getQuantityString(
                      R.plurals.spotify_missing_songs_snackbar,
                      failedSongs.size(),
                      failedSongs.size()),
              view -> {
                StringBuilder content = new StringBuilder();
                content.append(context.getString(R.string.spotify_missing_songs_dialog_body));
                content.append("\n");
                for (String s : failedSongs) {
                  content.append("\n");
                  content.append("• ");
                  content.append(s);
                }

                new MaterialAlertDialogBuilder(context)
                    .setTitle(R.string.spotify_missing_songs_dialog_title)
                    .setMessage(content)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
              })
          .setActionTextColor(ContextCompat.getColor(context, R.color.colorAccent));
    }
    snackbar.show();
  }
}
