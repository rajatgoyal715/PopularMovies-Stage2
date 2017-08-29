package com.rajatgoyal.popularmovies_stage2.model;

/**
 * Created by rajat on 29/8/17.
 */

public class Trailer {
    String key;
    String name;

    public Trailer(String key, String name) {
        this.key = key;
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }
}
