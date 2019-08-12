package me.kellymckinnon.setlister.artistsearch;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;
import me.kellymckinnon.setlister.models.Artist;
import me.kellymckinnon.setlister.models.SearchedArtist;

/**
 * The ViewModel's role is to provide data to the ArtistSearchFragment and survive configuration
 * changes. It acts as a communication center between the Repository and the UI.
 *
 * <p>Separating your app's UI data from your Activity and Fragment classes lets you better follow
 * the single responsibility principle: Your activities and fragments are responsible for drawing
 * data to the screen, while your ViewModel can take care of holding and processing all the data
 * needed for the UI.
 */
public class ArtistSearchViewModel extends AndroidViewModel {

  private final ArtistSearchRepository mRepository;
  private final LiveData<List<SearchedArtist>> mSearchedArtists;

  public ArtistSearchViewModel(@NonNull Application application) {
    super(application);
    mRepository = new ArtistSearchRepository(application);
    mSearchedArtists = mRepository.getSearchedArtists();
  }

  /** Get the user's previous searched artists from the DB. */
  LiveData<List<SearchedArtist>> getSearchedArtists() {
    return mSearchedArtists;
  }

  /** Add a new artist into the list of previously searched artists in the DB. */
  void insertSearchedArtist(SearchedArtist artist) {
    artist.setTimeSearched(System.currentTimeMillis());
    mRepository.insertSearchedArtist(artist);
  }

  /** Get a list of artist suggestions for a given query. */
  LiveData<List<Artist>> getArtistSuggestions(String query) {
    return mRepository.getArtistSuggestions(query);
  }

  /** Cancel an ongoing search for a particular query. */
  void cancelSearch(String query) {
    mRepository.cancelSearch(query);
  }
}
