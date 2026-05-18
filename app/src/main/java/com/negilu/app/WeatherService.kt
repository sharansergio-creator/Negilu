package com.negilu.app

import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class WeatherService(private val apiKey: String) {

    private val client = OkHttpClient()

    data class WeatherData(
        val temperature: Double,
        val humidity: Int,
        val rainfall: Double,
        val description: String,
        val windSpeed: Double
    )

    fun getWeather(lat: Double, lng: Double, onResult: (WeatherData?) -> Unit) {
        val url = "https://api.openweathermap.org/data/2.5/weather" +
                "?lat=$lat&lon=$lng&appid=$apiKey&units=metric"

        Log.d("WeatherService", "Fetching: $url")

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("WeatherService", "Failed: ${e.message}")
                onResult(null)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val body = response.body?.string() ?: return
                    Log.d("WeatherService", "Response: $body")
                    val json = JSONObject(body)

                    val main = json.getJSONObject("main")
                    val wind = json.getJSONObject("wind")
                    val weatherArray = json.getJSONArray("weather")
                    val weatherDesc = weatherArray.getJSONObject(0).getString("description")

                    val rainfall = if (json.has("rain")) {
                        val rain = json.getJSONObject("rain")
                        if (rain.has("1h")) rain.getDouble("1h") else 0.0
                    } else 0.0

                    val weatherData = WeatherData(
                        temperature = main.getDouble("temp"),
                        humidity = main.getInt("humidity"),
                        rainfall = rainfall,
                        description = weatherDesc,
                        windSpeed = wind.getDouble("speed")
                    )

                    onResult(weatherData)

                } catch (e: Exception) {
                    Log.e("WeatherService", "Parse error: ${e.message}")
                    onResult(null)
                }
            }
        })
    }
}