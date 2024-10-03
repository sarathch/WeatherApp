package com.denshaotoko.weatherapp.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


@Serializable
data class WeatherResponse(
    val main: MainData,
    val weather: List<WeatherDescription>,
    val visibility: Int,
    val wind: WindData,
)

@Serializable
data class MainData(
    val temp: Double,
)

@Serializable
data class WeatherDescription(
    val icon: String,val description: String
)

@Serializable
data class WindData(
    val speed: Double,
    val deg: Int,
)

object WeatherRepository {
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun getWeather(city: String, apiKey: String): WeatherResponse? {
        return try {
            client.get("https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey")
                .body()
        } catch (e: Exception) {
            null
        }
    }
}