package com.example.calculator

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import android.widget.TextView
import android.media.MediaPlayer
import android.os.Handler
import android.widget.SeekBar

class MP3 : AppCompatActivity() {
    private lateinit var music: MediaPlayer
    private lateinit var playButton: Button
    private lateinit var pauseButton: Button
    private lateinit var stopButton: Button
    private lateinit var songTitle: TextView
    private lateinit var seekbar: SeekBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mp3)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        playButton = findViewById(R.id.play_button)
        pauseButton = findViewById(R.id.pause_button)
        stopButton = findViewById(R.id.stop_button)
        songTitle = findViewById(R.id.text_info)
        seekbar = findViewById(R.id.seek_bar)

        music = MediaPlayer.create(this, R.raw.song)
        SeekBarCreate()
        playButton.setOnClickListener {
            if (!music.isPlaying) {
                music.start()
                songTitle.text = "Playing"
            }
        }

        pauseButton.setOnClickListener {
            if (music.isPlaying) {
                music.pause()
                songTitle.text = "Paused"
            }
        }

        stopButton.setOnClickListener {
            if (music.isPlaying) {
                music.stop()
                music.reset()
                music = MediaPlayer.create(this, R.raw.song)
                songTitle.text = "Stopped"
            }
        }
        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean){
                if (fromUser) music?.seekTo(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }
    override fun onDestroy() {
        super.onDestroy()
        if (music.isPlaying) {
            music.stop()
        }
        music.release()
    }
    private fun SeekBarCreate(){
        seekbar.max = music!!.duration
        val handler = Handler()
        handler.postDelayed(object: Runnable {
            override fun run(){
                try {
                    seekbar.progress = music!!.currentPosition
                    handler.postDelayed(this, 1000)
                } catch(e: Exception){
                    seekbar.progress = 0
                }
            }
        }, 0)
    }
}