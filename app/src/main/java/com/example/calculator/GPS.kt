package com.example.calculator

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Environment
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.io.FileWriter
import java.util.*

class GPS : AppCompatActivity(), LocationListener {

    private lateinit var locationManager: LocationManager
    private lateinit var tvCoordinates: TextView
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private var isTracking = false
    private val gpsFile = "gps_track.txt"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 100
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gps)
        tvCoordinates = findViewById(R.id.tvCoordinates)
        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        btnStart.setOnClickListener {
            if (checkLocationPermission()) {
                startTracking()
            } else {
                requestLocationPermission()
            }
        }
        btnStop.setOnClickListener {
            stopTracking()
        }
    }
    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startTracking()
            } else {
                Toast.makeText(
                    this,
                    "Для работы трекера необходимо разрешение на доступ к местоположению",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    private fun startTracking() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5000, // 5 секунд
                    10f, // 10 метров
                    this
                )
                isTracking = true
                Toast.makeText(this, "Начало записи координат", Toast.LENGTH_SHORT).show()
                appendToFile("Начало записи: ${dateFormat.format(Date())}")
            } catch (e: Exception) {
                Toast.makeText(this, "Ошибка GPS: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun stopTracking() {
        locationManager.removeUpdates(this)
        isTracking = false
        Toast.makeText(this, "Запись остановлена", Toast.LENGTH_SHORT).show()
        appendToFile("Конец записи: ${dateFormat.format(Date())}")
    }
    override fun onLocationChanged(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude
        val time = dateFormat.format(Date())
        val coordinates = "$time, $latitude, $longitude"
        tvCoordinates.text = coordinates
        if (isTracking) {
            appendToFile(coordinates)
        }
    }
    override fun onProviderEnabled(provider: String) {
        Toast.makeText(this, "GPS включен", Toast.LENGTH_SHORT).show()
    }
    override fun onProviderDisabled(provider: String) {
        Toast.makeText(this, "GPS выключен", Toast.LENGTH_SHORT).show()
    }
    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    private fun appendToFile(text: String) {
        try {
            val file = File(filesDir, gpsFile)
            FileWriter(file, true).use { writer ->
                writer.append("$text\n")
            }
            Toast.makeText(this, "Файл сохранен: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка записи: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        if (isTracking) {
            stopTracking()
        }
    }
}