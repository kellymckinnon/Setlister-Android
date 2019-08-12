package me.kellymckinnon.setlister;

import android.app.Application;
import android.os.AsyncTask;
import androidx.lifecycle.LiveData;
import java.util.List;
import me.kellymckinnon.setlister.db.SearchedArtistDao;
import me.kellymckinnon.setlister.db.SearchedArtistRoomDatabase;
import me.kellymckinnon.setlister.models.SearchedArtist;

/**
 * This acts a layer between the DB (and eventually network) and the ViewModel. It handles all
 * reading/writing to/from the DB. This makes the ViewModel easily testable.
 *
 * <p>// TODO: Move all networking code here as well
 */
class SearchRepository {
  private SearchedArtistDao mSearchedArtistDao;
  private LiveData<List<SearchedArtist>> mSearchedArtists;

  SearchRepository(Application application) {
    SearchedArtistRoomDatabase db = SearchedArtistRoomDatabase.getDatabase(application);
    mSearchedArtistDao = db.artistDao();
    mSearchedArtists = mSearchedArtistDao.getAllArtists();
  }

  LiveData<List<SearchedArtist>> getSearchedArtists() {
    return mSearchedArtists;
  }

  public void insertSearchedArtist(SearchedArtist artist) {
    new insertAsyncTask(mSearchedArtistDao).execute(artist);
  }

  private static class insertAsyncTask extends AsyncTask<SearchedArtist, Void, Void> {

    private SearchedArtistDao mAsyncTaskDao;

    insertAsyncTask(SearchedArtistDao dao) {
      mAsyncTaskDao = dao;
    }

    @Override
    protected Void doInBackground(final SearchedArtist... params) {
      mAsyncTaskDao.insert(params[0]);
      return null;
    }
  }
}
