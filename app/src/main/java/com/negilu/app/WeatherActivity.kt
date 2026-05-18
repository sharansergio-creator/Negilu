package com.negilu.app

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WeatherActivity : AppCompatActivity() {

    private lateinit var temperatureText: TextView
    private lateinit var humidityText: TextView
    private lateinit var rainfallText: TextView
    private lateinit var descriptionText: TextView
    private lateinit var windText: TextView
    private lateinit var irrigationText: TextView
    private lateinit var loadingBar: ProgressBar
    private lateinit var contentLayout: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        temperatureText = findViewById(R.id.temperatureText)
        humidityText = findViewById(R.id.humidityText)
        rainfallText = findViewById(R.id.rainfallText)
        descriptionText = findViewById(R.id.descriptionText)
        windText = findViewById(R.id.windText)
        irrigationText = findViewById(R.id.irrigationText)
        loadingBar = findViewById(R.id.loadingBar)
        contentLayout = findViewById(R.id.contentLayout)

        loadWeatherForFarm()
    }

    private fun loadWeatherForFarm() {
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
                        val centerLat = points.map { it["lat"] ?: 0.0 }.average()
                        val centerLng = points.map { it["lng"] ?: 0.0 }.average()
                        fetchWeather(centerLat, centerLng)
                    }
                }
            }
    }

    private fun fetchWeather(lat: Double, lng: Double) {
        val weatherService = WeatherService(getString(R.string.openweather_api_key))
        weatherService.getWeather(lat, lng) { weatherData ->
            runOnUiThread {
                loadingBar.visibility = View.GONE
                contentLayout.visibility = View.VISIBLE

                if (weatherData != null) {
                    temperatureText.text = "${weatherData.temperature}°C"
                    humidityText.text = "${weatherData.humidity}%"
                    rainfallText.text = "${weatherData.rainfall} mm"
                    descriptionText.text = weatherData.description.replaceFirstChar { it.uppercase() }
                    windText.text = "${weatherData.windSpeed} m/s"

                    // Irrigation decision
                    val decision = when {
                        weatherData.rainfall > 5.0 -> "✅ Skip irrigation — sufficient rainfall today"
                        weatherData.humidity > 80 -> "✅ Skip irrigation — high humidity"
                        weatherData.temperature > 35 -> "💧 Irrigate today — high temperature stress"
                        else -> "💧 Irrigate today — normal conditions"
                    }
                    irrigationText.text = decision

                } else {
                    temperatureText.text = "Unable to fetch weather"
                }
            }
        }
    }
}