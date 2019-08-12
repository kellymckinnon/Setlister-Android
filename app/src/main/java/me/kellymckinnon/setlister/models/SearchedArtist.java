package me.kellymckinnon.setlister.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * This is used only for storing the user's searched artists in the DB.
 * We cannot use {@link Artist} directly because sometimes, a user's search includes a full
 * Artist object (MBID, name), and sometimes it's just the name if they don't wait for the
 * suggestions to come up before searching.
 */
@Entity(tableName = "searched_artist_table")
public class SearchedArtist {

  @ColumnInfo(name = "time_searched")
  private long mTimeSearched;

  @ColumnInfo(name = "mbid")
  private String mMbid;

  @PrimaryKey
  @NonNull
  @ColumnInfo(name = "name")
  private String mName;

  public SearchedArtist(long timeSearched, String mbid, @NonNull String name) {
    mTimeSearched = timeSearched;
    mMbid = mbid;
    mName = name;
  }

  @Ignore
  public SearchedArtist(String mbid, @NonNull String name) {
    mMbid = mbid;
    mName = name;
  }

  @Ignore
  public SearchedArtist(Artist artist) {
    mMbid = artist.getMbid();
    mName = artist.getName();
  }

  public String getMbid() {
    return mMbid;
  }

  public String getName() {
    return mName;
  }


  public long getTimeSearched() {
    return mTimeSearched;
  }

  public void setTimeSearched(long timeSearched) {
    mTimeSearched = timeSearched;
  }

  public void setMbid(String mbid) {
    mMbid = mbid;
  }

  public void setName(String name) {
    mName = name;
  }

  @NonNull
  @Override
  public String toString() {
    // TODO: This isn't necessary once we move away from ArrayAdapter/ListView
    return mName;
  }
}
