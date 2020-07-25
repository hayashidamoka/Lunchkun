package jp.co.pannacotta.lunch_app

import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException

class ErrorActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private var soundPool: SoundPool? = null
    private var voice_sorry = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_erorr)
        audioPlay()
        val button = findViewById<Button>(R.id.again_button)
        val audioAttributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build()
        soundPool = SoundPool.Builder().setAudioAttributes(audioAttributes).setMaxStreams(2).build()
        voice_sorry = soundPool!!.load(this,R.raw.sorry,1)

        soundPool!!.setOnLoadCompleteListener { soundPool, sampleId, status ->
            if (0 == status) {
                when  (sampleId) {
                    voice_sorry -> soundPool!!.play(voice_sorry, 0.3f, 0.3f, 0, 0, 1.0f)
                }
            }
        }
        button.setOnClickListener {
            val intent = Intent()
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.setClass(this@ErrorActivity, MainActivity::class.java)
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
}