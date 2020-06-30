package jp.co.pannacotta.lunch_app;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.location.LocationManager;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 0;
    private static final int REQUEST_LOCATION_SETTING = 1;
    public static final String API_URL = "http://webservice.recruit.co.jp";
    private FusedLocationProviderClient mFusedLocationClient;
    private boolean isSuccessLocation = false;
    private MediaPlayer mediaPlayer;
    private LocationManager locationManager;
    private boolean isLocationSetting;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        //BGMスタート
        audioPlay();
        //アプリの位置情報の権限確認
        checkLocationPermission();


    }

    private boolean checkLocationPermission() {
        //アプリの位置情報の権限確認
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //既に許可してあります
            //端末のGPSがONになっているか確認しにいく
            initLocation();
            return true;
        } else {
            //許可してません
            //おねがいダイアログ出す
            new AlertDialog.Builder(this)
                    .setTitle("おねがい")
                    .setMessage("お店を探すために位置情報の権限を許可してね")
                    .setPositiveButton("OK！", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //おねがいダイアログOKした
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
                            //onRequestPermissionsResultへ
                        }
                    }).create()
                    .show();
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        //アプリの位置情報の権限ONになった？
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //アプリの位置情報の権限ONになったよ
                    //端末のGPSがONになっているか確認しにいく
                    initLocation();
                } else {
                    //アプリの位置情報の権限ONになってないよ
                    //アプリ終わり
                    finish();
                }
        }
    }

    @SuppressLint("MissingPermission")
    private void initLocation() {
        //端末のGPSがONになっているか確認
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        final boolean gpsEnabled = locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled && !isLocationSetting) {
            //端末のGPSがOFFだよ
            //enableLocationSettingsへ
            enableLocationSettings();

        } else {
            //端末のGPSがONだよ
            RelativeLayout rl = findViewById(R.id.mainActivity);
            rl.setOnClickListener(new View.OnClickListener() {
                //ランチ君をおす
                @Override
                public void onClick(View view) {
                    //位置情報を取得してね
                    getLocation();
                    //アニメーション始まり
                    startRotation();
                }
            });
        }
    }

    private void enableLocationSettings() {
        //端末のGPSをONにしてもらうため設定画面にいく
        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(settingsIntent, REQUEST_LOCATION_SETTING);
    }



    private void getLocation() {
        //アプリの位置情報の権限ONか確認
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //アプリの位置情報の権限ON
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        //位置情報とれた
                        //緯度と経度を保存
                        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("lat", String.valueOf(location.getLatitude()));
                        editor.putString("lng", String.valueOf(location.getLongitude()));
                        editor.apply();
                        isSuccessLocation = true;
                    } else {
                        //位置情報とれなかった
                        //ごめんね画面
                        //goErrorActivity();
                    }
                }
            });
        } else {
            //アプリの位置情報の権限OFF
            //アプリの位置情報の権限確認しにいく
            checkLocationPermission();
        }
    }

    private void startRotation() {
        ImageView lunchkun = findViewById(R.id.lunchkun_top);

        RotateAnimation rotate = new RotateAnimation(0.0f, 360.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(2000);
        rotate.setFillAfter(true);
        rotate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (isSuccessLocation) {
                    //位置情報が取れていたらアニメーションが終わってリザルト画面にいくよ
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, ResultActivity.class);
                    startActivity(intent);
                } else {
                    //位置情報が取れてないよ
                    //ごめんね画面
                    goErrorActivity();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        lunchkun.startAnimation(rotate);
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
            // audio ファイルを読出し
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
        // 終了を検知するリスナー
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                audioStop();
            }
        });
    }

    private void audioStop() {
        // 再生終了
        mediaPlayer.stop();
        // リセット
        mediaPlayer.reset();
        // リソースの解放
        mediaPlayer.release();

        mediaPlayer = null;
    }

    private void goErrorActivity() {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, ErrorActivity.class);
        startActivity(intent);
    }

}
