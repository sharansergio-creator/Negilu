package com.negilu.app

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BoundaryWalkActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private lateinit var statusText: TextView
    private lateinit var pointCountText: TextView
    private lateinit var startButton: Button
    private lateinit var addPointButton: Button
    private lateinit var finishButton: Button

    private val boundaryPoints = mutableListOf<Map<String, Double>>()
    private var isWalking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_boundary_walk)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        statusText = findViewById(R.id.statusText)
        pointCountText = findViewById(R.id.pointCountText)
        startButton = findViewById(R.id.startButton)
        addPointButton = findViewById(R.id.addPointButton)
        finishButton = findViewById(R.id.finishButton)

        addPointButton.isEnabled = false
        finishButton.isEnabled = false

        startButton.setOnClickListener {
            if (checkLocationPermission()) {
                startWalk()
            } else {
                requestLocationPermission()
            }
        }

        addPointButton.setOnClickListener {
            captureCurrentLocation()
        }

        finishButton.setOnClickListener {
            finishWalk()
        }
    }

    private fun startWalk() {
        isWalking = true
        boundaryPoints.clear()
        statusText.text = "Walking started. Move to the first corner of your farm and tap Add Point."
        startButton.isEnabled = false
        addPointButton.isEnabled = true
        finishButton.isEnabled = false
        pointCountText.text = "Points captured: 0"

        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 2000
        ).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                // Location updating in background
            }
        }

        if (checkLocationPermission()) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private fun captureCurrentLocation() {
        if (!checkLocationPermission()) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val point = mapOf(
                    "lat" to location.latitude,
                    "lng" to location.longitude
                )
                boundaryPoints.add(point)
                pointCountText.text = "Points captured: ${boundaryPoints.size}"
                statusText.text = "Point ${boundaryPoints.size} captured. Move to next corner."

                if (boundaryPoints.size >= 3) {
                    finishButton.isEnabled = true
                }

                Toast.makeText(
                    this,
                    "Point captured: ${String.format("%.6f", location.latitude)}, ${String.format("%.6f", location.longitude)}",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(this, "GPS not ready. Wait a moment and try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun finishWalk() {
        if (boundaryPoints.size < 3) {
            Toast.makeText(this, "Need at least 3 points to form a boundary", Toast.LENGTH_SHORT).show()
            return
        }

        stopLocationUpdates()
        saveBoundaryToFirestore()
    }

    private fun saveBoundaryToFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        statusText.text = "Saving boundary..."
        finishButton.isEnabled = false

        val boundaryData = hashMapOf(
            "points" to boundaryPoints,
            "pointCount" to boundaryPoints.size,
            "createdAt" to System.currentTimeMillis()
        )

        db.collection("farmers")
            .document(userId)
            .collection("farms")
            .document("main")
            .set(boundaryData)
            .addOnSuccessListener {
                statusText.text = "Boundary saved! ${boundaryPoints.size} points recorded."
                Toast.makeText(this, "Farm boundary saved successfully!", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { e ->
                statusText.text = "Failed to save: ${e.message}"
                finishButton.isEnabled = true
            }
    }

    private fun stopLocationUpdates() {
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            1001
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startWalk()
        } else {
            Toast.makeText(this, "Location permission required for boundary walk", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }
}