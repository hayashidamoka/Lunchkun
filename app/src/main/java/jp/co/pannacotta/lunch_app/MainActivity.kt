package jp.co.pannacotta.lunch_app

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import jp.co.pannacotta.lunch_app.MainActivity
import java.io.IOException

class MainActivity() : AppCompatActivity() {
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var isSuccessLocation = false
    private var mediaPlayer: MediaPlayer? = null
    private var locationManager: LocationManager? = null
    private var isLocationSetting = false
    private var requestTitle: String? = null
    private var requestMessage: String? = null
    private var ok: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestTitle = getString(R.string.request_title)
        requestMessage = getString(R.string.request_message)
        ok = getString(R.string.ok)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        //BGMスタート
        audioPlay()
        //アプリの位置情報の権限確認
        checkLocationPermission()
        val rl = findViewById<RelativeLayout>(R.id.mainActivity)
        rl.setOnClickListener(View.OnClickListener
        //ランチ君をおす
        { //位置情報を取得してね
            getlocation()
            //アニメーション始まり
            startRotation()
        })
    }

    private fun checkLocationPermission(): Boolean {
        //アプリの位置情報の権限確認
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            //既に許可してあります
            //端末のGPSがONになっているか確認しにいく
            initLocation()
            return true
        } else {
            //許可してません
            //おねがいダイアログ出す
            AlertDialog.Builder(this)
                    .setTitle(requestTitle)
                    .setMessage(requestMessage)
                    .setPositiveButton(ok, object : DialogInterface.OnClickListener {
                        override fun onClick(dialogInterface: DialogInterface, i: Int) {
                            //おねがいダイアログOKした
                            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), MY_PERMISSIONS_REQUEST_LOCATION)
                            //onRequestPermissionsResultへ
                        }
                    }).create()
                    .show()
            return false
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        //アプリの位置情報の権限ONになった？
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //アプリの位置情報の権限ONになったよ
                //端末のGPSがONになっているか確認しにいく
                initLocation()
            } else {
                //アプリの位置情報の権限ONになってないよ
                //BGM終わり
                audioStop()
                //アプリ終わり
                finish()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun initLocation() {
        //端末のGPSがONになっているか確認
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gpsEnabled = locationManager != null && locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!gpsEnabled && !isLocationSetting) {
            //端末のGPSがOFFだよ
            //enableLocationSettingsへ
            enableLocationSettings()
        } else {
            //端末のGPSがONだよ
        }
    }

    private fun enableLocationSettings() {
        //端末のGPSをONにしてもらうため設定画面にいく
        val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivityForResult(settingsIntent, REQUEST_LOCATION_SETTING)
    }//アプリの位置情報の権限OFF
    //アプリの位置情報の権限確認しにいく
//位置情報とれなかった
    //ごめんね画面
    //goErrorActivity();
//位置情報とれた
    //緯度と経度を保存
//アプリの位置情報の権限ON

    //アプリの位置情報の権限ONか確認
        private fun getlocation() {
            //アプリの位置情報の権限ONか確認
            if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                //アプリの位置情報の権限ON
                mFusedLocationClient!!.lastLocation.addOnSuccessListener(this, object : OnSuccessListener<Location?> {
                    override fun onSuccess(location: Location?) {
                        if (location != null) {
                            //位置情報とれた
                            //緯度と経度を保存
                            val pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                            val editor = pref.edit()
                            editor.putString("lat", location.latitude.toString())
                            editor.putString("lng", location.longitude.toString())
                            editor.apply()
                            isSuccessLocation = true
                        } else {
                            //位置情報とれなかった
                            //ごめんね画面
                            //goErrorActivity();
                        }
                    }
                })
            } else {
                //アプリの位置情報の権限OFF
                //アプリの位置情報の権限確認しにいく
                checkLocationPermission()
            }
        }

    private fun startRotation() {
        val lunchkun = findViewById<ImageView>(R.id.lunchkun_top)
        val rotate = RotateAnimation(0.0f, 360.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f)
        rotate.duration = 2000
        rotate.fillAfter = true
        rotate.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                if (isSuccessLocation) {
                    //位置情報が取れていたらアニメーションが終わってリザルト画面にいくよ
                    val intent = Intent()
                    intent.setClass(this@MainActivity, ResultActivity::class.java)
                    startActivity(intent)
                } else {
                    //位置情報が取れてないよ
                    //ごめんね画面
                    goErrorActivity()
                }
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        lunchkun.startAnimation(rotate)
    }

    private fun audioSetup(): Boolean {
        var fileCheck = false
        mediaPlayer = MediaPlayer()
        val filePath = "lunch.wav"
        try {
            assets.openFd(filePath).use { afdescripter ->
                // MediaPlayerに読み込んだ音楽ファイルを指定
                mediaPlayer!!.setDataSource(afdescripter.getFileDescriptor(),
                        afdescripter.getStartOffset(),
                        afdescripter.getLength())
                // 音量調整を端末のボタンに任せる
                setVolumeControlStream(AudioManager.STREAM_MUSIC)
                mediaPlayer!!.prepare()
                mediaPlayer!!.setLooping(true)
                fileCheck = true
            }
        } catch (e1: IOException) {
            e1.printStackTrace()
        }
        return fileCheck
    }

    private fun audioPlay() {
        if (mediaPlayer == null) {
            if (audioSetup()) {
            } else {
                return
            }
        } else {
            mediaPlayer!!.stop()
            mediaPlayer!!.reset()
            mediaPlayer!!.release()
        }
        // 再生する
        mediaPlayer!!.start()
        // 終了を検知するリスナー
        mediaPlayer!!.setOnCompletionListener(object : OnCompletionListener {
            override fun onCompletion(mp: MediaPlayer) {
                audioStop()
            }
        })
    }

    fun audioStop() {
        // 再生終了
        mediaPlayer!!.stop()
        // リセット
        mediaPlayer!!.reset()
        // リソースの解放
        mediaPlayer!!.release()
        mediaPlayer = null
    }

    private fun goErrorActivity() {
        val intent = Intent()
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        intent.setClass(this@MainActivity, ErrorActivity::class.java)
        startActivity(intent)
    }

    public override fun onPause() {
        super.onPause()
        audioStop()
    }

    public override fun onRestart() {
        super.onRestart()
        audioPlay()
    }

    companion object {
        val MY_PERMISSIONS_REQUEST_LOCATION = 0
        private val REQUEST_LOCATION_SETTING = 1
    }
}