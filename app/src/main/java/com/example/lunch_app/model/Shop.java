package com.example.lunch_app.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

@Parcel
public class Shop {
    public Genre genre;
    public String lat;
    public String lng;
    public String lunch;
    public String name;
    public Photo photo;
    public Urls urls;
    @SerializedName("catch")
    public String catchCopy;
    //public String access;
    //public String free_food;
    // public String private_room;
}
