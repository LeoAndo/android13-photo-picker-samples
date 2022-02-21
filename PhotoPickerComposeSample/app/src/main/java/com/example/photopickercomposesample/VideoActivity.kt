package com.example.photopickercomposesample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.widget.VideoView

class VideoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        val videoUrl = intent.data ?: return
        Log.d("TAG", "videoUrl: $videoUrl")
        val fullscreenVideoView = findViewById<VideoView>(R.id.fullscreenVideoView)
        fullscreenVideoView.setVideoURI(videoUrl)
        fullscreenVideoView.setOnCompletionListener {
            Toast.makeText(this@VideoActivity, "video Finish!", Toast.LENGTH_SHORT).show()
            finish()
        }
        fullscreenVideoView.setOnPreparedListener {
            Toast.makeText(this@VideoActivity, "onPrepared", Toast.LENGTH_SHORT).show()
            fullscreenVideoView.start()
        }
    }
}