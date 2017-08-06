
package me.kellymckinnon.setlister.models;

import com.squareup.moshi.Json;

public class Country {

    @Json(name = "code")
    private String code;
    @Json(name = "name")
    private String name;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
