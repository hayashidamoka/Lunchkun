package com.example.lunch_app;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toolbar;

import java.io.IOException;
import java.security.Key;
import java.util.logging.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lunch_app.model.Gourmet;

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

    public static final String TAG = "RetrofitActivity";
    public static final String API_URL = "http://webservice.recruit.co.jp";
    private TextView result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        TextView CregitTextView = findViewById(R.id.CregitTextView);
        String htmlcredit = "Powered by <a href=\"http://webservice.recruit.co.jp/\">ホットペッパー Webサービス</a>";

        CharSequence cregitChar = Html.fromHtml(htmlcredit);

        CregitTextView.setText(cregitChar);

        MovementMethod mMethod = LinkMovementMethod.getInstance();
        CregitTextView.setMovementMethod(mMethod);
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

//        Hotpepperapi service = retrofit.create(Hotpepperapi.class);
//       Call<Gourmet> call = service.webservice("ae27b52bed304677","魚",lat,lng,"json");
//        call.enqueue(new Callback<Gourmet>() {
//            @Override
//            public void onResponse(Call<Gourmet> call, Response<Gourmet> response) {
//                Log.d("ろぐ",response.toString());
//            }
//
//            @Override
//            public void onFailure(Call<Gourmet> call, Throwable t) {
//                Log.d("ろぐ",t.toString());
//
//            }
//        });

    }
}


