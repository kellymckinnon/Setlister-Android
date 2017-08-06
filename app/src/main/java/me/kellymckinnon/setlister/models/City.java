
package com.example;

import com.squareup.moshi.Json;

public class City {

    @Json(name = "id")
    private String id;
    @Json(name = "name")
    private String name;
    @Json(name = "state")
    private String state;
    @Json(name = "stateCode")
    private String stateCode;
    @Json(name = "coords")
    private Coords coords;
    @Json(name = "country")
    private Country country;

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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public Coords getCoords() {
        return coords;
    }

    public void setCoords(Coords coords) {
        this.coords = coords;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

}
