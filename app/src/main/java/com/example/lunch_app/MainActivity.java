package com.example.lunch_app;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.lunch_app.model.Results;
import com.example.lunch_app.model.Shop;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.example.lunch_app.model.Gourmet;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private FusedLocationProviderClient mFusedLocationClient;
    public static final String API_URL = "http://webservice.recruit.co.jp";
    public static String lat = "";
    public static String lng = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton button = findViewById(R.id.kensakuButton);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        checkLocationPermission();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, ResultActivity.class);
                startActivity(intent);
            }

        });
    }

    private boolean checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //既に許可してあります
            return true;
        } else {
            //許可してません
            new AlertDialog.Builder(this)
                    .setTitle("おねがい")
                    .setMessage("お店を探すために位置情報の権限を許可してね")
                    .setPositiveButton("OK！", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
                        }
                    }).create()
                    .show();
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else {
                    finish();
                }
        }
    }

    public void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        SharedPreferences prefer = getSharedPreferences("緯度経度", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefer.edit();
                        editor.putString("lat", String.valueOf(location.getLatitude()));
                        editor.putString("lng", String.valueOf(location.getLongitude()));
                        editor.commit();
                    } else {
                        //ごめんね画面
                    }
                }
            });
        } else {
            //ごめんね画面
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request original = chain.request();

                //header設定
                Request request = original.newBuilder()
                        .header("Accept", "application/json")
                        .method(original.method(), original.body())
                        .build();

                okhttp3.Response response = chain.proceed(request);
                return response;
            }
        });

        //ログ出力設定
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        httpClient.addInterceptor(logging);

        //クライアント生成
        OkHttpClient client = httpClient.build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        Hotpepperapi service = retrofit.create(Hotpepperapi.class);
        SharedPreferences pref = getSharedPreferences("緯度経度", MODE_PRIVATE);
        lat = pref.getString("lat", "");
        lng = pref.getString("lng", "");
        Call<Gourmet> call = service.webservice("ae27b52bed304677", lat, lng, "3","1","json");
        call.enqueue(new Callback<Gourmet>() {
            @Override
            public void onResponse(Call<Gourmet> call, Response<Gourmet> response) {
                Log.d("ろぐ", response.toString());
                Random random = new Random();
                int sum = random.nextInt();

                Gourmet shop_count = response.body();
                Results results = shop_count.getResults();
                List shop = results.getShop();
                shop.size();

//                Random random = new Random();
//                int num = rand.nextInt(10) + 100;
//                System.out.println(num);
            }

            @Override
            public void onFailure(Call<Gourmet> call, Throwable t) {
                Log.d("ろぐ", t.toString());
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, ErrorActivity.class);
                startActivity(intent);
            }
        });


    }
}
