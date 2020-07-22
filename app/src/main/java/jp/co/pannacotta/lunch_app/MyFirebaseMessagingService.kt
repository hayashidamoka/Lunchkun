package jp.co.pannacotta.lunch_app

import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        // 端末＋アプリを一意に識別するためのトークンを取得
        Log.i("FIREBASE", "[SERVICE] Token = ${token ?: "Empty"}")
    }
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        // 通知内のタイトルとメッセージを格納する変数
        var title: String
        var message: String
        // 通知タイトル内容の決定
        if (remoteMessage!!.notification!!.title != null) { title = remoteMessage!!.notification!!.title!! }
        else { title = "" }
        // 通知メッセージ内容の決定
        if (remoteMessage!!.notification!!.body != null) {message = remoteMessage!!.notification!!.body!! }
        else { message = "" }
        Log.i("title", title)
        Log.i("message", message)
    }
}