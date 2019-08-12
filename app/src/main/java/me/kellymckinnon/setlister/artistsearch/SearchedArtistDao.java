package me.kellymckinnon.setlister.artistsearch;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;
import me.kellymckinnon.setlister.models.SearchedArtist;

@Dao
public interface SearchedArtistDao {

  /* By using this conflict strategy, if the artist is already in the DB, we replace it with the
  new instance of it, which will have the updated time searched. */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insert(SearchedArtist artist);

  @Query("SELECT * from searched_artist_table ORDER BY time_searched DESC")
  LiveData<List<SearchedArtist>> getAllArtists();
}
