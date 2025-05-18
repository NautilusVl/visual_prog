package com.example.calculator

import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.util.Log
import net.objecthunter.exp4j.ExpressionBuilder

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val calcButton: Button = findViewById(R.id.go_to_calculator_activity)
        calcButton.setOnClickListener {
            val intent = Intent(this, Calculator::class.java)
            startActivity(intent)
        }
        val MP3Button: Button = findViewById(R.id.go_to_MP3_activity)
        MP3Button.setOnClickListener {
            val intent = Intent(this, MP3::class.java)
            startActivity(intent)
        }
        val GPSButton: Button = findViewById(R.id.go_to_GPS_activity)
        GPSButton.setOnClickListener {
            val intent = Intent(this, GPS::class.java)
            startActivity(intent)
        }
    }
}

