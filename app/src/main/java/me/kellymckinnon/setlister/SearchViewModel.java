package me.kellymckinnon.setlister;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;
import me.kellymckinnon.setlister.models.SearchedArtist;

/**
 * The ViewModel's role is to provide data to the SearchFragment and survive configuration changes.
 * It acts as a communication center between the Repository and the UI.
 *
 * <p>Separating your app's UI data from your Activity and Fragment classes lets you better follow
 * the single responsibility principle: Your activities and fragments are responsible for drawing
 * data to the screen, while your ViewModel can take care of holding and processing all the data
 * needed for the UI.
 */
public class SearchViewModel extends AndroidViewModel {

  private final SearchRepository mRepository;
  private final LiveData<List<SearchedArtist>> mSearchedArtists;

  public SearchViewModel(@NonNull Application application) {
    super(application);
    mRepository = new SearchRepository(application);
    mSearchedArtists = mRepository.getSearchedArtists();
  }

  public LiveData<List<SearchedArtist>> getSearchedArtists() {
    return mSearchedArtists;
  }

  public void insertSearchedArtist(SearchedArtist artist) {
    artist.setTimeSearched(System.currentTimeMillis());
    mRepository.insertSearchedArtist(artist);
  }
}
