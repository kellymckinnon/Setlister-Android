
package com.example;

import java.util.List;
import com.squareup.moshi.Json;

public class Sets {

    @Json(name = "set")
    private List<Set> set = null;

    public List<Set> getSet() {
        return set;
    }

    public void setSet(List<Set> set) {
        this.set = set;
    }

}
