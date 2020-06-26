package com.example.lunch_app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.lunch_app.model.Gourmet;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Hotpepperapi {

    @GET("/hotpepper/gourmet/v1/")
    Call<Gourmet> webservice(
            @Query("key") String key,
            @Query("lat") String lat,
            @Query("lng") String lng,
            @Query("range") String range,
            @Query("lunch") String lunch,
            @Query("count") String count,
            @Query("format") String format
//            @Query("name") String name,
//            @Query("genre") String genre,
//            @Query("free_food") String free_food,
//            @Query("private_room") String private_room,
//            @Query("title") String title
    );
}
