
package me.kellymckinnon.setlister.models;

import com.squareup.moshi.Json;

public class Artist {

    @Json(name = "mbid")
    private String mbid;
    @Json(name = "name")
    private String name;
    @Json(name = "disambiguation")
    private String disambiguation;
    @Json(name = "url")
    private String url;

    public String getMbid() {
        return mbid;
    }

    public void setMbid(String mbid) {
        this.mbid = mbid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisambiguation() {
        return disambiguation;
    }

    public void setDisambiguation(String disambiguation) {
        this.disambiguation = disambiguation;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
