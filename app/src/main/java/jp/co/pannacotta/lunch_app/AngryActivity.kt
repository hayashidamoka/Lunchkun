package jp.co.pannacotta.lunch_app

import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import jp.co.pannacotta.lunch_app.MainActivity
import java.io.IOException

class AngryActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_angry)
        audioPlay()
        val button = findViewById<Button>(R.id.sorry_button)
        button.setOnClickListener {
            val intent = Intent()
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.setClass(this@AngryActivity, MainActivity::class.java)
            startActivity(intent)
            mediaPlayer!!.stop()
        }
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

    public override fun onPause() {
        super.onPause()
        mediaPlayer!!.pause()
    }

    public override fun onRestart() {
        super.onRestart()
        mediaPlayer!!.start()
    }
}