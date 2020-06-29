package jp.co.pannacotta.lunch_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.gms.common.api.ApiException;
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
    public static final String API_URL = "http://webservice.recruit.co.jp";
    private FusedLocationProviderClient mFusedLocationClient;
    private boolean isSuccessLocation = false;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RelativeLayout rl = findViewById(R.id.mainActivity);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        audioPlay();

        rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkLocationPermission();
            }

        });
    }

    private boolean checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //既に許可してあります
            getLocation();
            startRotation();
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

        private void requestLocationSetting() {
            final LocationSettingsRequest request = new LocationSettingsRequest.Builder()
                    .setAlwaysShow(true)
                    .addLocationRequest(LocationRequest.create().setPriority(mPriority))
                    .build();

            final Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(this)
                    .checkLocationSettings(request);

            result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
                @Override
                public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                    try {
                        task.getResult(ApiException.class);
                        // 既に設定済み
                        finishForResult(Locset.ResultCode.SUCCESS);
                    } catch (ApiException exception) {
                        switch (exception.getStatusCode()) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                try {
                                    // ユーザに位置情報設定を変更してもらうためのダイアログを表示する
                                    resolvable.startResolutionForResult(MainActivity.this, REQUEST_CODE_LOCATION_SETTING);
                                    return;
                                } catch (IntentSender.SendIntentException e) {
                                    // Ignore the error.
                                } catch (ClassCastException e) {
                                    // Ignore, should be an impossible error.
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                // 位置情報が取得できず、その状態からの復帰も難しい時呼ばれる
                                break;
                        }
                        finishForResult(Locset.ResultCode.LOCATION_SETTING_FAILURE);
                    }
                }
            });
        }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                    startRotation();
                } else {
                    finish();
                }
        }
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("lat", String.valueOf(location.getLatitude()));
                        editor.putString("lng", String.valueOf(location.getLongitude()));
                        editor.apply();
                        isSuccessLocation = true;
                    } else {
                        checkLocationPermission()
                        //ごめんね画面
                        //goErrorActivity();
                    }
                }
            });
        } else {
            //ごめんね画面
            goErrorActivity();
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
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, ResultActivity.class);
                    startActivity(intent);
                } else {
                    //ごめんね画面
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
