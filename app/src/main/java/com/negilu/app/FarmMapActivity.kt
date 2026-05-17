package com.negilu.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mapbox.common.MapboxOptions
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.fillLayer
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.negilu.app.BuildConfig


class FarmMapActivity : AppCompatActivity() {

    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapboxOptions.accessToken = BuildConfig.MAPBOX_TOKEN
        setContentView(R.layout.activity_farm_map)

        mapView = findViewById(R.id.mapView)

        loadFarmBoundary()
    }

    private fun loadFarmBoundary() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("farmers")
            .document(userId)
            .collection("farms")
            .document("main")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val points = document.get("points") as? List<Map<String, Double>>
                    if (!points.isNullOrEmpty()) {
                        showBoundaryOnMap(points)
                    }
                }
            }
    }

    private fun showBoundaryOnMap(points: List<Map<String, Double>>) {
        val coordinates = points.map { point ->
            Point.fromLngLat(
                point["lng"] ?: 0.0,
                point["lat"] ?: 0.0
            )
        }.toMutableList()

        // Close the polygon by adding first point at end
        if (coordinates.isNotEmpty()) {
            coordinates.add(coordinates[0])
        }

        val centerLat = points.map { it["lat"] ?: 0.0 }.average()
        val centerLng = points.map { it["lng"] ?: 0.0 }.average()

        mapView.mapboxMap.loadStyle(Style.SATELLITE) { style ->

            // Camera to center of farm
            mapView.mapboxMap.setCamera(
                CameraOptions.Builder()
                    .center(Point.fromLngLat(centerLng, centerLat))
                    .zoom(18.0)
                    .pitch(45.0)
                    .build()
            )

            // Add boundary polygon source
            style.addSource(
                geoJsonSource("farm-boundary-source") {
                    geometry(Polygon.fromLngLats(listOf(coordinates)))
                }
            )

            // Fill layer — semi transparent green
            style.addLayer(
                fillLayer("farm-fill-layer", "farm-boundary-source") {
                    fillColor("#4caf7d")
                    fillOpacity(0.3)
                }
            )

            // Outline layer
            style.addLayer(
                lineLayer("farm-line-layer", "farm-boundary-source") {
                    lineColor("#4caf7d")
                    lineWidth(3.0)
                }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }
}