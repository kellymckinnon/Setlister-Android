
package me.kellymckinnon.setlister.models;

import java.util.List;
import com.squareup.moshi.Json;

public class Set {

    @Json(name = "song")
    private List<Song> song = null;
    @Json(name = "encore")
    private Integer encore;
    @Json(name = "name")
    private String name;

    public List<Song> getSong() {
        return song;
    }

    public void setSong(List<Song> song) {
        this.song = song;
    }

    public Integer getEncore() {
        return encore;
    }

    public void setEncore(Integer encore) {
        this.encore = encore;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
