package com.example.photopickerjavasample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.widget.VideoView;

public class VideoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        var videoUrl = getIntent().getData();
        Log.d("TAG", "videoUrl: " + videoUrl.toString());
        final VideoView fullscreenVideoView = findViewById(R.id.fullscreenVideoView);
        fullscreenVideoView.setVideoURI(videoUrl);
        fullscreenVideoView.setOnCompletionListener(mediaPlayer -> {
            Toast.makeText(VideoActivity.this, "video Finish!", Toast.LENGTH_SHORT).show();
            finish();
        });
        fullscreenVideoView.setOnPreparedListener(mediaPlayer -> {
            Toast.makeText(VideoActivity.this, "onPrepared", Toast.LENGTH_SHORT).show();
            fullscreenVideoView.start();
        });
    }
}