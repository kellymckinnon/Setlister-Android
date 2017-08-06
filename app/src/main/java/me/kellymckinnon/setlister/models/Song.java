
package me.kellymckinnon.setlister.models;

import com.squareup.moshi.Json;

public class Song {

    @Json(name = "name")
    private String name;
    @Json(name = "info")
    private String info;
    @Json(name = "cover")
    private Artist originalArtist;
    @Json(name = "with")
    private Artist with;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public Artist getCover() {
        return originalArtist;
    }

    public void setCover(Artist originalArtist) {
        this.originalArtist = originalArtist;
    }

    public Artist getWith() {
        return with;
    }

    public void setWith(Artist with) {
        this.with = with;
    }

}
