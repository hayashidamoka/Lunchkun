package com.example.lunch_app.model;

import org.parceler.Parcel;

import java.util.List;

@Parcel
public class Results {
    public List<Shop> shop;

    public List<Shop> getShop(){
        return shop;
    }

    public void setShop(List<Shop> shop){
        this.shop = shop;
    }


}
