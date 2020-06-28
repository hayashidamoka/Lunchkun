package com.example.lunch_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.lunch_app.model.Gourmet;
import com.example.lunch_app.model.Shop;
import com.example.lunch_app.model.Urls;
import com.google.android.gms.location.LocationServices;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.lunch_app.MainActivity.lat;
import static com.example.lunch_app.MainActivity.lng;

public class ResultActivity extends AppCompatActivity {

    public static final String API_URL = "http://webservice.recruit.co.jp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        TextView CregitTextView = findViewById(R.id.CregitTextView);
        String htmlcredit = "【画像提供：ホットペッパー グルメ】<br>Powered by <a href=\"http://webservice.recruit.co.jp/\">ホットペッパー Webサービス</a>";

        CharSequence cregitChar = Html.fromHtml(htmlcredit);

        CregitTextView.setText(cregitChar);

        MovementMethod mMethod = LinkMovementMethod.getInstance();
        CregitTextView.setMovementMethod(mMethod);

        Button button = findViewById(R.id.next_shop_botton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(ResultActivity.this, AngryActivity.class);
                startActivity(intent);
            }
        });
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
        Call<Gourmet> call = service.webservice("ae27b52bed304677", lat, lng, "3", "1", "100", "json");
        call.enqueue(new Callback<Gourmet>() {
            @Override
            public void onResponse(Call<Gourmet> call, Response<Gourmet> response) {
                Log.d("ろぐ", response.toString());
                int shop_count = response.body().results.shop.size();
                Random random = new Random();
                int todayShopnum = random.nextInt(shop_count - 1);
                final Shop todayShop = response.body().results.shop.get(todayShopnum);

                String todayShopName = todayShop.name;
                ImageView shop_photo = findViewById(R.id.shop_photo);
                String todayShopPhotoUrl = todayShop.photo.pc.l;
                Glide.with(ResultActivity.this).load(todayShopPhotoUrl).apply(new RequestOptions().override(700, 1000)).into(shop_photo);

                String todayShopCatchCopy = todayShop.catchCopy;

                TextView shop_name = findViewById(R.id.shop_name);

                TextView shop_catch_copy = findViewById(R.id.shop_catch_copy);

                shop_name.setText(todayShopName);


                shop_catch_copy.setText(todayShopCatchCopy);

                Button button = findViewById(R.id.shop_web_botton);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String todayShopUrl = todayShop.urls.pc;
                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                        CustomTabsIntent customTabsIntent = builder.build();
                        customTabsIntent.launchUrl(ResultActivity.this, Uri.parse(todayShopUrl));
                    }
                });


                Log.d("ろぐ", todayShop.toString());
            }

            @Override
            public void onFailure(Call<Gourmet> call, Throwable t) {
                Log.d("ろぐ", t.toString());
//                Intent intent = new Intent();
//                intent.setClass(MainActivity.this, ErrorActivity.class);
//                startActivity(intent);
            }
        });
    }
}



