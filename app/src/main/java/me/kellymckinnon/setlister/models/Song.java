
package com.example;

import com.squareup.moshi.Json;

public class Song {

    @Json(name = "name")
    private String name;
    @Json(name = "info")
    private String info;
    @Json(name = "cover")
    private Cover cover;
    @Json(name = "with")
    private With with;

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

    public Cover getCover() {
        return cover;
    }

    public void setCover(Cover cover) {
        this.cover = cover;
    }

    public With getWith() {
        return with;
    }

    public void setWith(With with) {
        this.with = with;
    }

}
