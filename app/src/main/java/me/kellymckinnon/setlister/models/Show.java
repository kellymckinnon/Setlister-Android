package me.kellymckinnon.setlister.models;

import android.os.Parcel;
import android.os.Parcelable;

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

  public Show(String band, String venue, String date, String tour, String[] songs) {
    this.band = band;
    this.venue = venue;
    this.date = date;
    this.tour = tour;
    this.songs = songs;
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

  public static class Builder {

    private String band;
    private String venue;
    private String date;
    private String tour;
    private String[] songs;
    private Parcel in;

    public Builder setBand(String band) {
      this.band = band;
      return this;
    }

    public Builder setVenue(String venue) {
      this.venue = venue;
      return this;
    }

    public Builder setDate(String date) {
      this.date = date;
      return this;
    }

    public Builder setTour(String tour) {
      this.tour = tour;
      return this;
    }

    public Builder setSongs(String[] songs) {
      this.songs = songs;
      return this;
    }

    public Show createShow() {
      return new Show(band, venue, date, tour, songs);
    }
  }
}
