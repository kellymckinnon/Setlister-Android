package me.kellymckinnon.setlister.models;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

/** Model for a concert */
public class Show implements Parcelable {

  public static final Creator<Show> CREATOR =
      new Creator<Show>() {
        @Override
        public Show createFromParcel(Parcel in) {
          return new Show(in);
        }

        @Override
        public Show[] newArray(int size) {
          return new Show[size];
        }
      };
  private String band;
  private String venue;
  private String date;
  private String tour;
  private String[] songs;

  public Show(Setlist setlist) {
    tour =
        setlist.getTour() == null || setlist.getTour().getName().equals("No Tour Assigned")
            ? ""
            : setlist.getTour().getName();

    String year = setlist.getEventDate().substring(6);
    String day = setlist.getEventDate().substring(0, 2);
    String month = setlist.getEventDate().substring(3, 5);
    date = month + "/" + day + "/" + year;

    try {
      venue =
          setlist.getVenue().getName()
              + ", "
              + setlist.getVenue().getCity().getName()
              + ", "
              + setlist.getVenue().getCity().getStateCode()
              + ", "
              + setlist.getVenue().getCity().getCountry().getCode();
    } catch (Exception e) {
      venue = "Unknown venue";
    }

    List<Set> sets = setlist.getSets() == null ? new ArrayList<>() : setlist.getSets().getSet();
    ArrayList<String> songList = new ArrayList<>();

    // Combine all of the sets into one for viewing
    for (Set set : sets) {
      for (Song song : set.getSong()) {
        if (!song.getName().isEmpty()) {
          songList.add(song.getName());
        }
      }
    }

    songs = songList.toArray(new String[0]);
    band = setlist.getArtist().getName();
  }

  private Show(Parcel in) {
    band = in.readString();
    venue = in.readString();
    date = in.readString();
    tour = in.readString();
    songs = in.createStringArray();
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(band);
    dest.writeString(venue);
    dest.writeString(date);
    dest.writeString(tour);
    dest.writeStringArray(songs);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public String getBand() {
    return band;
  }

  public String getVenue() {
    return venue;
  }

  public String getDate() {
    return date;
  }

  public String getTour() {
    return tour;
  }

  public String[] getSongs() {
    return songs;
  }
}
