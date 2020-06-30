package jp.co.pannacotta.lunch_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import jp.co.pannacotta.lunch_app.model.Gourmet;
import jp.co.pannacotta.lunch_app.model.Shop;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ResultActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    public static final String API_URL = "http://webservice.recruit.co.jp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        audioPlay();

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
                mediaPlayer.stop();
            }
        });

        Button web_button = findViewById(R.id.shop_web_botton);
        web_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
            }
        });

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
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String lat = pref.getString("lat", "");
        String lng = pref.getString("lng", "");
        Call<Gourmet> call = service.webservice("ae27b52bed304677", lat, lng, "3", "1", "100", "json");
        call.enqueue(new Callback<Gourmet>() {
            @Override
            public void onResponse(Call<Gourmet> call, Response<Gourmet> response) {
                Log.d("ろぐ", response.toString());
                int shop_count = response.body().results.shop.size();
                if (shop_count > 0) {
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
                } else {
                    goErrorActivity();
                }
            }

            @Override
            public void onFailure(Call<Gourmet> call, Throwable t) {
                goErrorActivity();
            }
        });

    }

    private void goErrorActivity() {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setClass(ResultActivity.this, ErrorActivity.class);
        startActivity(intent);
    }
    private boolean audioSetup() {
        boolean fileCheck = false;

        mediaPlayer = new MediaPlayer();

        String filePath = "lunch.wav";

        try (AssetFileDescriptor afdescripter = getAssets().openFd(filePath);) {
            // MediaPlayerに読み込んだ音楽ファイルを指定
            mediaPlayer.setDataSource(afdescripter.getFileDescriptor(),
                    afdescripter.getStartOffset(),
                    afdescripter.getLength());
            // 音量調整を端末のボタンに任せる
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepare();
            mediaPlayer.setLooping(true);
            fileCheck = true;
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return fileCheck;
    }

    private void audioPlay() {

        if (mediaPlayer == null) {
            if (audioSetup()) {

            } else {
                return;
            }
        } else {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
        }
        // 再生する
        mediaPlayer.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        mediaPlayer.pause();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        mediaPlayer.start();
    }
}



