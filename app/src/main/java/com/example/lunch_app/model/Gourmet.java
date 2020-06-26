package com.example.lunch_app.model;

import org.parceler.Parcel;

@Parcel
public class Gourmet {
    public Results results;

    public Results getResults(){
        return results;
    }

    public void setResults(Results results){
        this.results = results;
    }
}
