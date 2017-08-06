
package com.example;

import com.squareup.moshi.Json;

public class Venue {

    @Json(name = "id")
    private String id;
    @Json(name = "name")
    private String name;
    @Json(name = "city")
    private City city;
    @Json(name = "url")
    private String url;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
