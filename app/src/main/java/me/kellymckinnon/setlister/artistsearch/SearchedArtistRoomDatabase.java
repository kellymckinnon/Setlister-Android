package me.kellymckinnon.setlister.artistsearch;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import me.kellymckinnon.setlister.models.SearchedArtist;

@Database(
    entities = {SearchedArtist.class},
    version = 1,
    exportSchema = false)
public abstract class SearchedArtistRoomDatabase extends RoomDatabase {
  public abstract SearchedArtistDao artistDao();

  private static volatile SearchedArtistRoomDatabase INSTANCE;

  public static SearchedArtistRoomDatabase getDatabase(final Context context) {
    if (INSTANCE == null) {
      synchronized (SearchedArtistRoomDatabase.class) {
        if (INSTANCE == null) {
          INSTANCE =
              Room.databaseBuilder(
                      context.getApplicationContext(),
                      SearchedArtistRoomDatabase.class,
                      "searched_artist_database")
                  .build();
        }
      }
    }
    return INSTANCE;
  }
}
