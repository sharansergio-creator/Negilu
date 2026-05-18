package com.negilu.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val welcomeText = findViewById<TextView>(R.id.welcomeText)
        val viewMapButton = findViewById<Button>(R.id.viewMapButton)
        val boundaryButton = findViewById<Button>(R.id.boundaryWalkButton)
        val weatherButton = findViewById<Button>(R.id.weatherButton)

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("farmers")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val name = document.getString("farmerName") ?: "Farmer"
                val village = document.getString("village") ?: ""
                welcomeText.text = "Welcome, $name\n$village"
            }

        viewMapButton.setOnClickListener {
            startActivity(Intent(this, FarmMapActivity::class.java))
        }

        boundaryButton.setOnClickListener {
            startActivity(Intent(this, BoundaryWalkActivity::class.java))
        }

        weatherButton.setOnClickListener {
            startActivity(Intent(this, WeatherActivity::class.java))
        }
    }
}