
package com.example;

import com.squareup.moshi.Json;

public class Setlist {

    @Json(name = "id")
    private String id;
    @Json(name = "versionId")
    private String versionId;
    @Json(name = "eventDate")
    private String eventDate;
    @Json(name = "lastUpdated")
    private String lastUpdated;
    @Json(name = "artist")
    private Artist artist;
    @Json(name = "venue")
    private Venue venue;
    @Json(name = "tour")
    private Tour tour;
    @Json(name = "sets")
    private Sets sets;
    @Json(name = "url")
    private String url;
    @Json(name = "info")
    private String info;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public Venue getVenue() {
        return venue;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }

    public Tour getTour() {
        return tour;
    }

    public void setTour(Tour tour) {
        this.tour = tour;
    }

    public Sets getSets() {
        return sets;
    }

    public void setSets(Sets sets) {
        this.sets = sets;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

}
