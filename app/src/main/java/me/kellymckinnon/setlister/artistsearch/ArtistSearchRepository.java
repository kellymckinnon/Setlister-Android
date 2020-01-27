package me.kellymckinnon.setlister.artistsearch;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.ArrayList;
import java.util.List;
import me.kellymckinnon.setlister.SetlisterApplication;
import me.kellymckinnon.setlister.models.Artist;
import me.kellymckinnon.setlister.models.Artists;
import me.kellymckinnon.setlister.models.SearchedArtist;
import me.kellymckinnon.setlister.network.RetrofitClient;
import me.kellymckinnon.setlister.network.SetlistFMService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * This acts a layer between the DB and network and the ViewModel. It handles all reading/writing
 * to/from the DB and network. This makes the ViewModel easily testable.
 */
class ArtistSearchRepository {
  private SearchedArtistDao mSearchedArtistDao;
  private SetlistFMService mSetlistFMService;
  private LiveData<List<SearchedArtist>> mSearchedArtists;

  ArtistSearchRepository(Application application) {
    mSetlistFMService =
        RetrofitClient.getSetlistFMService(((SetlisterApplication) application).getSetlistFMUrl());

    SearchedArtistRoomDatabase db = SearchedArtistRoomDatabase.getDatabase(application);
    mSearchedArtistDao = db.artistDao();
    mSearchedArtists = mSearchedArtistDao.getAllArtists();
  }

  /** Get a list of artist suggestions for a given query from Setlist FM. */
  LiveData<List<Artist>> getArtistSuggestions(String query) {
    final MutableLiveData<List<Artist>> artistSuggestions = new MutableLiveData<>();

    mSetlistFMService
        .getArtists(query)
        .enqueue(
            new Callback<Artists>() {
              @Override
              public void onResponse(Call<Artists> call, Response<Artists> response) {
                artistSuggestions.postValue(
                    response.body() == null ? new ArrayList<>() : response.body().getArtist());
              }

              @Override
              public void onFailure(Call<Artists> call, Throwable t) {
                Log.e(getClass().getSimpleName(), t.toString());
                artistSuggestions.postValue(new ArrayList<>());
              }
            });

    return artistSuggestions;
  }

  /** Get the user's previous searched artists from the DB. */
  LiveData<List<SearchedArtist>> getSearchedArtists() {
    return mSearchedArtists;
  }

  /** Add a new artist into the list of previously searched artists in the DB. */
  void insertSearchedArtist(SearchedArtist artist) {
    new InsertSearchedArtistAsyncTask(mSearchedArtistDao).execute(artist);
  }

  /** Cancel an ongoing search for a particular query. */
  void cancelSearch(String query) {
    mSetlistFMService.getArtists(query).cancel();
  }

  private static class InsertSearchedArtistAsyncTask extends AsyncTask<SearchedArtist, Void, Void> {
    private SearchedArtistDao mAsyncTaskDao;

    InsertSearchedArtistAsyncTask(SearchedArtistDao dao) {
      mAsyncTaskDao = dao;
    }

    @Override
    protected Void doInBackground(final SearchedArtist... params) {
      mAsyncTaskDao.insert(params[0]);
      return null;
    }
  }
}
