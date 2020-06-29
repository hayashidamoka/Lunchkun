package jp.co.pannacotta.lunch_app;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;

public class ErrorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_erorr);

        Button button = findViewById(R.id.again_botton);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(ErrorActivity.this, MainActivity.class);
                startActivity(intent);
//                stop
            }
        });
    }
//    private void audioStop() {
//        // 再生終了
//        mediaPlayer.stop();
//        // リセット
//        mediaPlayer.reset();
//        // リソースの解放
//        mediaPlayer.release();
//
//        mediaPlayer = null;
//    }
}