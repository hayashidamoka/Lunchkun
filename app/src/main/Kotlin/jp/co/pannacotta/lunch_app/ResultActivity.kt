package jp.co.pannacotta.lunch_app

import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import jp.co.pannacotta.lunch_app.model.Gourmet
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.*

class ResultActivity : AppCompatActivity() {
    private val htmlcreditText: String = getString(R.string.htmlcredit)

    private var mediaPlayer: MediaPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        audioPlay()
        val CregitTextView = findViewById<TextView>(R.id.CregitTextView)
//        val htmlcredit = "【画像提供：ホットペッパー グルメ】<br>Powered by <a href=\"http://webservice.recruit.co.jp/\">ホットペッパー Webサービス</a>【画像提供：ホットペッパー グルメ】<br>Powered by <a href=\"http://webservice.recruit.co.jp/\">ホットペッパー Webサービス</a>"
        val htmlcredit = htmlcreditText
        val cregitChar: CharSequence = Html.fromHtml(htmlcredit)
        CregitTextView.text = cregitChar
        val mMethod = LinkMovementMethod.getInstance()
        CregitTextView.movementMethod = mMethod
        val button = findViewById<Button>(R.id.next_shop_botton)
        button.setOnClickListener {
            val intent = Intent()
            intent.setClass(this@ResultActivity, AngryActivity::class.java)
            startActivity(intent)
            mediaPlayer!!.stop()
        }
        val web_button = findViewById<Button>(R.id.shop_web_botton)
        web_button.setOnClickListener { mediaPlayer!!.stop() }
        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor { chain ->
            val original = chain.request()

            //header設定
            val request = original.newBuilder()
                    .header("Accept", "application/json")
                    .method(original.method(), original.body())
                    .build()
            chain.proceed(request)
        }

        //ログ出力設定
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BASIC
        httpClient.addInterceptor(logging)

        //クライアント生成
        val client = httpClient.build()
        val retrofit = Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        val service = retrofit.create(Hotpepperapi::class.java)
        val pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val lat = pref.getString("lat", "")
        val lng = pref.getString("lng", "")
        val call = service.webservice("ae27b52bed304677", lat, lng, "3", "1", "100", "json")
        call!!.enqueue(object : Callback<Gourmet> {
            override fun onResponse(call: Call<Gourmet>, response: retrofit2.Response<Gourmet>) {
                Log.d("ろぐ", response.toString())
                val shop_count = response.body().results.shop.size
                if (shop_count > 0) {
                    val random = Random()
                    val todayShopnum = random.nextInt(shop_count - 1)
                    val todayShop = response.body().results.shop[todayShopnum]
                    val todayShopName = todayShop.name
                    val shop_photo = findViewById<ImageView>(R.id.shop_photo)
                    val todayShopPhotoUrl = todayShop.photo.pc.l
                    Glide.with(this@ResultActivity).load(todayShopPhotoUrl).apply(RequestOptions().override(700, 1000)).into(shop_photo)
                    val todayShopCatchCopy = todayShop.catchCopy
                    val shop_name = findViewById<TextView>(R.id.shop_name)
                    val shop_catch_copy = findViewById<TextView>(R.id.shop_catch_copy)
                    shop_name.text = todayShopName
                    shop_catch_copy.text = todayShopCatchCopy
                    val button = findViewById<Button>(R.id.shop_web_botton)
                    button.setOnClickListener {
                        val todayShopUrl = todayShop.urls.pc
                        val builder = CustomTabsIntent.Builder()
                        val customTabsIntent = builder.build()
                        customTabsIntent.launchUrl(this@ResultActivity, Uri.parse(todayShopUrl))
                    }
                } else {
                    goErrorActivity()
                }
            }

            override fun onFailure(call: Call<Gourmet>, t: Throwable) {
                goErrorActivity()
            }
        })
    }

    private fun goErrorActivity() {
        val intent = Intent()
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        intent.setClass(this@ResultActivity, ErrorActivity::class.java)
        startActivity(intent)
    }

    private fun audioSetup(): Boolean {
        var fileCheck = false
        mediaPlayer = MediaPlayer()
        val filePath = "lunch.wav"
        try {
            assets.openFd(filePath).use { afdescripter ->
                // MediaPlayerに読み込んだ音楽ファイルを指定
                mediaPlayer!!.setDataSource(afdescripter.fileDescriptor,
                        afdescripter.startOffset,
                        afdescripter.length)
                // 音量調整を端末のボタンに任せる
                volumeControlStream = AudioManager.STREAM_MUSIC
                mediaPlayer!!.prepare()
                mediaPlayer!!.isLooping = true
                fileCheck = true
            }
        } catch (e1: IOException) {
            e1.printStackTrace()
        }
        return fileCheck
    }

    private fun audioPlay() {
        if (this.mediaPlayer == null) {
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
    }

    public override fun onPause() {
        super.onPause()
        mediaPlayer!!.pause()
    }

    public override fun onRestart() {
        super.onRestart()
        mediaPlayer!!.start()
    }

    companion object {
        const val API_URL = "http://webservice.recruit.co.jp"
    }
}