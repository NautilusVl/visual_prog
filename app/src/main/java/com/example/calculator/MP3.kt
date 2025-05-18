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
import android.Manifest
import android.content.pm.PackageManager
import android.os.Environment
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException

class MP3 : AppCompatActivity() {
    private lateinit var music: MediaPlayer
    private lateinit var playButton: Button
    private lateinit var pauseButton: Button
    private lateinit var stopButton: Button
    private lateinit var songTitle: TextView
    private lateinit var seekbar: SeekBar
    private lateinit var nextButton: Button
    private lateinit var previousButton: Button
    private var currentSongIndex = 0
    private val songsList = mutableListOf<String>()
    companion object {
        private const val REQUEST_PERMISSION_CODE = 123
    }
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
        nextButton = findViewById(R.id.next_button)
        previousButton = findViewById(R.id.previous_button)

        if (checkPermission()) {
            loadSongs()
            setupMediaPlayer()
        } else {
            requestPermission()
        }

        playButton.setOnClickListener {
            if (songsList.isNotEmpty() && !music.isPlaying) {
                music.start()
                songTitle.text = "Playing: ${File(songsList[currentSongIndex]).name}"
            }
        }

        pauseButton.setOnClickListener {
            if (music.isPlaying) {
                music.pause()
                songTitle.text = "Paused: ${File(songsList[currentSongIndex]).name}"
            }
        }

        stopButton.setOnClickListener {
            if (music.isPlaying) {
                music.stop()
                setupMediaPlayer()
                songTitle.text = "Stopped"
            }
        }

        nextButton.setOnClickListener {
            playNextSong()
        }

        previousButton.setOnClickListener {
            playPreviousSong()
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
    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }
    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            REQUEST_PERMISSION_CODE
        )
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadSongs()
                setupMediaPlayer()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun loadSongs() {
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        addSongsFromDirectory(downloadDir)
        addSongsFromDirectory(musicDir)
    }
    private fun addSongsFromDirectory(directory: File) {
        if (directory.exists() && directory.isDirectory) {
            directory.listFiles()?.forEach { file ->
                if (file.isFile && (file.name.endsWith(".mp3") || file.name.endsWith(".wav"))) {
                    songsList.add(file.absolutePath)
                }
            }
        }
    }
    private fun setupMediaPlayer() {
        if (songsList.isNotEmpty()) {
            try {
                music = MediaPlayer()
                music.setDataSource(songsList[currentSongIndex])
                music.prepare()
                songTitle.text = "Ready: ${File(songsList[currentSongIndex]).name}"
                seekbar.max = music.duration
                startSeekbarUpdate()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Error loading song", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No songs found", Toast.LENGTH_SHORT).show()
        }
    }
    private fun playNextSong() {
        if (songsList.isEmpty()) return

        currentSongIndex = (currentSongIndex + 1) % songsList.size
        playSelectedSong()
    }
    private fun playPreviousSong() {
        if (songsList.isEmpty()) return

        currentSongIndex = (currentSongIndex - 1 + songsList.size) % songsList.size
        playSelectedSong()
    }
    private fun playSelectedSong() {
        music.reset()
        try {
            music.setDataSource(songsList[currentSongIndex])
            music.prepare()
            music.start()
            songTitle.text = "Playing: ${File(songsList[currentSongIndex]).name}"
            seekbar.max = music.duration
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    private fun startSeekbarUpdate() {
        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                try {
                    if (music.isPlaying) {
                        seekbar.progress = music.currentPosition
                    }
                    handler.postDelayed(this, 1000)
                } catch (e: Exception) {
                    seekbar.progress = 0
                }
            }
        }, 0)
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