package com.example.calculator

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class GPS : AppCompatActivity(), LocationListener {

    private lateinit var locationManager: LocationManager
    private lateinit var tvCoordinates: TextView
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private var isTracking = false
    private val gpsFile = "gps_track.json"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startTracking()
        } else {
            Toast.makeText(
                this,
                "Для работы трекера необходимо разрешение на доступ к местоположению",
                Toast.LENGTH_SHORT
            ).show()
        }
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
        locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun startTracking() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                lastLocation?.let {
                    updateLocationInfo(it)
                }

                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5000,
                    10f,
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
        updateLocationInfo(location)
        if (isTracking) {
            appendToFile(formatLocationData(location))
        }
    }

    private fun updateLocationInfo(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude
        val altitude = location.altitude
        val time = dateFormat.format(location.time)

        val coordinates = """
            Latitude: $latitude
            Longitude: $longitude
            Altitude: $altitude
            Time: $time
        """.trimIndent()

        tvCoordinates.text = coordinates
    }

    private fun formatLocationData(location: Location): String {
        return """
            {
                "latitude": ${location.latitude},
                "longitude": ${location.longitude},
                "altitude": ${location.altitude},
                "time": ${location.time},
                "formatted_time": "${dateFormat.format(location.time)}"
            }
        """.trimIndent()
    }

    override fun onProviderEnabled(provider: String) {
        Toast.makeText(this, "GPS включен", Toast.LENGTH_SHORT).show()
    }

    override fun onProviderDisabled(provider: String) {
        Toast.makeText(this, "GPS выключен", Toast.LENGTH_SHORT).show()
    }
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    private fun appendToFile(text: String) {
        try {
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val file = File(dir, gpsFile)

            FileWriter(file, true).use { writer ->
                writer.append("$text\n")
            }

            Toast.makeText(
                this,
                "Файл доступен в Documents: ${file.name}",
                Toast.LENGTH_LONG
            ).show()

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