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
import android.os.Looper
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView

class MP3 : AppCompatActivity() {
    private lateinit var music: MediaPlayer
    private lateinit var playButton: Button
    private lateinit var pauseButton: Button
    private lateinit var stopButton: Button
    private lateinit var songTitle: TextView
    private lateinit var seekbar: SeekBar
    private lateinit var nextButton: Button
    private lateinit var previousButton: Button
    private lateinit var songsListView: ListView
    private lateinit var currentTimeText: TextView
    private lateinit var totalTimeText: TextView
    private var currentSongIndex = 0
    private val songsList = mutableListOf<String>()
    private val logTag = "MP3Player"
    private var isPaused = false
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            loadSongs()
            setupMediaPlayer()
            handler.post {
                if (::music.isInitialized) {
                    seekbar.max = music.duration
                    totalTimeText.text = formatTime(music.duration)
                }
            }
        }
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

        initViews()

        if (checkPermission()) {
            loadSongs()
            setupMediaPlayer()
        } else {
            requestPermission()
        }

        setupButtonListeners()
    }

    private fun initViews() {
        playButton = findViewById(R.id.play_button)
        pauseButton = findViewById(R.id.pause_button)
        stopButton = findViewById(R.id.stop_button)
        songTitle = findViewById(R.id.text_info)
        seekbar = findViewById(R.id.seek_bar)
        nextButton = findViewById(R.id.next_button)
        previousButton = findViewById(R.id.previous_button)
        songsListView = findViewById(R.id.songs_list_view)
        currentTimeText = findViewById(R.id.current_time)
        totalTimeText = findViewById(R.id.total_time)
    }

    private fun setupButtonListeners() {
        playButton.setOnClickListener {
            if (songsList.isNotEmpty()) {
                if (!music.isPlaying) {
                    if (music.currentPosition > 0) {
                        music.start()
                    } else {
                        try {
                            music.prepare()
                            music.start()
                        } catch (e: IllegalStateException) {
                            music.start()
                        }
                    }
                    updateSongTitle("Playing")
                    startSeekbarUpdate()
                }
            }
        }

        pauseButton.setOnClickListener {
            if (music.isPlaying) {
                music.pause()
                isPaused = true
                updateSongTitle("Paused")
            } else {
                music.start()
                isPaused = false
                updateSongTitle("Playing")
                startSeekbarUpdate()
            }
        }

        stopButton.setOnClickListener {
            if (::music.isInitialized) {
                try {
                    if (music.isPlaying) {
                        music.stop()
                    }
                    music.reset()
                    music.setDataSource(songsList[currentSongIndex])
                    music.prepare()
                    seekbar.progress = 0
                    updateSongTitle("Stopped")
                    isPaused = false
                } catch (e: Exception) {
                    Log.e(logTag, "Stop error", e)
                    setupMediaPlayer()
                }
            }
        }

        nextButton.setOnClickListener { playNextSong() }
        previousButton.setOnClickListener { playPreviousSong() }

        songsListView.setOnItemClickListener { _, _, position, _ ->
            currentSongIndex = position
            playSelectedSong()
        }

        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser && ::music.isInitialized) {
                    try {
                        music.seekTo(progress)
                    } catch (e: Exception) {
                        Log.e(logTag, "Seek error", e)
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                handler.removeCallbacks(updateSeekBar)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (::music.isInitialized && (music.isPlaying || isPaused)) {
                    startSeekbarUpdate()
                }
            }
        })
    }
    private fun formatTime(millis: Int): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        val hours = (millis / (1000 * 60 * 60))

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        Toast.makeText(this, "Please grant permission to access music files", Toast.LENGTH_LONG).show()
        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    private fun loadSongs() {
        songsList.clear()
        val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        scanDirectoryForSongs(musicDir)
        scanDirectoryForSongs(downloadsDir)
        if (songsList.isEmpty()) {
            Toast.makeText(this, "No music files found", Toast.LENGTH_SHORT).show()
        } else {
            updateSongsListUI()
        }
    }
    private fun scanDirectoryForSongs(directory: File) {
        if (!directory.exists() || !directory.isDirectory) {
            Log.d(logTag, "Directory does not exist: ${directory.absolutePath}")
            return
        }
        val files = directory.listFiles() ?: return
        for (file in files) {
            when {
                file.isDirectory -> {
                    scanDirectoryForSongs(file)
                }
                file.isFile && isAudioFile(file) -> {
                    songsList.add(file.absolutePath)
                    Log.d(logTag, "Found audio file: ${file.name}")
                }
            }
        }
    }
    private fun isAudioFile(file: File): Boolean {
        val name = file.name.lowercase()
        return name.endsWith(".mp3") || name.endsWith(".wav")
    }
    private fun updateSongsListUI() {
        val songNames = songsList.map { File(it).nameWithoutExtension }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, songNames)
        songsListView.adapter = adapter
    }
    private fun updateSongTitle(state: String) {
        songTitle.text = "$state: ${File(songsList[currentSongIndex]).nameWithoutExtension}"
    }

    private fun setupMediaPlayer() {
        if (songsList.isEmpty()) return

        try {
            if (::music.isInitialized) {
                music.release()
            }

            music = MediaPlayer().apply {
                setDataSource(songsList[currentSongIndex])
                prepare()

                setOnPreparedListener { mp ->
                    seekbar.max = mp.duration
                    totalTimeText.text = formatTime(mp.duration)
                    currentTimeText.text = formatTime(0)
                    if (mp.isPlaying) {
                        startSeekbarUpdate()
                    }
                }
                setOnCompletionListener {
                    handler.removeCallbacks(updateSeekBar)
                    seekbar.progress = seekbar.max
                    currentTimeText.text = totalTimeText.text
                }
            }
        } catch (e: IOException) {
            Log.e(logTag, "Error setting up media player", e)
            Toast.makeText(this, "Error loading song", Toast.LENGTH_SHORT).show()
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
        try {
            handler.removeCallbacks(updateSeekBar)
            music.reset()
            music.setDataSource(songsList[currentSongIndex])
            music.prepare()
            music.start()
            updateSongTitle("Playing")
            seekbar.max = music.duration
            totalTimeText.text = formatTime(music.duration)
            currentTimeText.text = "00:00"
            startSeekbarUpdate()
            songsListView.smoothScrollToPosition(currentSongIndex)
        } catch (e: IOException) {
            Log.e(logTag, "Error playing selected song", e)
            Toast.makeText(this, "Error playing song", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startSeekbarUpdate() {
        handler.removeCallbacks(updateSeekBar)

        if (::music.isInitialized && music.isPlaying) {
            seekbar.progress = music.currentPosition
            currentTimeText.text = formatTime(music.currentPosition)
            handler.post(updateSeekBar)
        }
    }
    private val handler = Handler(Looper.getMainLooper())
    private val updateSeekBar = object : Runnable {
        override fun run() {
            try {
                if (::music.isInitialized && music.isPlaying) {
                    val currentPos = music.currentPosition
                    seekbar.progress = currentPos
                    currentTimeText.text = formatTime(currentPos)
                    if (music.isPlaying) {
                        handler.postDelayed(this, 200)
                    }
                }
            } catch (e: Exception) {
                Log.e(logTag, "SeekBar update error", e)
            }
        }
    }
    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        if (::music.isInitialized) {
            try {
                if (music.isPlaying) {
                    music.stop()
                }
                music.release()
            } catch (e: Exception) {
                Log.e(logTag, "onDestroy error", e)
            }
        }
        super.onDestroy()
    }
}