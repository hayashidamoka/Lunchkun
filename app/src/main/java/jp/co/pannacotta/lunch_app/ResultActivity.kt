package jp.co.pannacotta.lunch_app

import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
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
    private var mediaPlayer: MediaPlayer? = null
    private var htmlcreditText: String? = null
    private var webservice_key: String? = null
    private var webservice_range: String? = null
    private var webservice_lunch: String? = null
    private var webservice_count: String? = null
    private var webservice_format: String? = null
    private var soundPool: SoundPool? = null
    private var voice_koko = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        htmlcreditText = getString(R.string.html_credit)
        webservice_key = getString(R.string.webservice_key)
        webservice_range = getString(R.string.webservice_range)
        webservice_lunch = getString(R.string.webservice_lunch)
        webservice_count = getString(R.string.webservice_count)
        webservice_format = getString(R.string.webservice_format)
        val audioAttributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build()
        soundPool = SoundPool.Builder().setAudioAttributes(audioAttributes).setMaxStreams(2).build()
        voice_koko = soundPool!!.load(this,R.raw.koko,1)

        soundPool!!.setOnLoadCompleteListener { soundPool, sampleId, status ->
            if (0 == status) {
                when  (sampleId) {
                    voice_koko -> soundPool!!.play(voice_koko, 0.3f, 0.3f, 0, 0, 1.0f)
                }
            }
        }
        audioPlay()
        val CregitTextView = findViewById<TextView>(R.id.CregitTextView)
        val htmlcredit = htmlcreditText
        val cregitChar: CharSequence = Html.fromHtml(htmlcredit)
        CregitTextView.text = cregitChar
        val mMethod = LinkMovementMethod.getInstance()
        CregitTextView.movementMethod = mMethod
        val button = findViewById<Button>(R.id.next_shop_button)
        button.setOnClickListener {
            val intent = Intent()
            intent.setClass(this@ResultActivity, AngryActivity::class.java)
            startActivity(intent)
            mediaPlayer!!.stop()
        }
        val web_button = findViewById<Button>(R.id.shop_web_button)
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
        val call = service.webservice(webservice_key, lat, lng, webservice_range, webservice_lunch, webservice_count, webservice_format)
        call!!.enqueue(object : Callback<Gourmet?> {
            override fun onResponse(call: Call<Gourmet?>, response: retrofit2.Response<Gourmet?>) {
                Log.d("ろぐ", response.toString())
                val shop_count = response.body()!!.results!!.shop!!.size
                if (shop_count > 0) {
                    val random = Random()
                    val todayShopnum = random.nextInt(shop_count - 1)
                    val todayShop = response.body()!!.results!!.shop!![todayShopnum]
                    val todayShopName = todayShop.name
                    val shop_photo = findViewById<ImageView>(R.id.shop_photo)
                    val todayShopPhotoUrl = todayShop.photo!!.pc!!.l
                    Glide.with(this@ResultActivity).load(todayShopPhotoUrl).apply(RequestOptions().override(1000, 1000)).into(shop_photo)
                    val todayShopCatchCopy = todayShop.catchCopy
                    val shop_name = findViewById<TextView>(R.id.shop_name)
                    val shop_catch_copy = findViewById<TextView>(R.id.shop_catch_copy)
                    shop_name.text = todayShopName
                    shop_catch_copy.text = todayShopCatchCopy
                    val button = findViewById<Button>(R.id.shop_web_button)
                    button.setOnClickListener {
                        val todayShopUrl = todayShop.urls!!.pc
                        val builder = CustomTabsIntent.Builder()
                        val customTabsIntent = builder.build()
                        customTabsIntent.launchUrl(this@ResultActivity, Uri.parse(todayShopUrl))
                    }
                } else {
                    goErrorActivity()
                }
            }

            override fun onFailure(call: Call<Gourmet?>, t: Throwable) {
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
    }

    fun audioStop() {
        // 再生終了
        mediaPlayer?.stop()
        // リセット
        mediaPlayer?.reset()
        // リソースの解放
        mediaPlayer?.release()
        mediaPlayer = null
    }

    public override fun onPause() {
        super.onPause()
        audioStop()
        soundPool?.stop(1)
    }

    public override fun onRestart() {
        super.onRestart()
        audioPlay()
    }

    companion object {
        const val API_URL = "http://webservice.recruit.co.jp"
    }
}