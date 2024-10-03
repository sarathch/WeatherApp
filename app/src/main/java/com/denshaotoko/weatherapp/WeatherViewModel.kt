package com.denshaotoko.weatherapp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denshaotoko.weatherapp.data.WeatherRepository
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel: ViewModel() {

    private val _uiState: MutableStateFlow<UiState> =
        MutableStateFlow(UiState.Initial)
    val uiState: StateFlow<UiState> =
        _uiState.asStateFlow()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.API_KEY
    )

    fun getWeatherData(city: String) {
        _uiState.value = UiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val responseD = WeatherRepository.getWeather(city, BuildConfig.WEATHER_API_KEY)
            responseD?.let { response ->
                Log.d("WeatherViewModel", response.toString())

                val generatedText = sendPrompt("Describe in detail to regular user today's weather resonse - ${response} and indicate temperature in celcius and farenheit.")
                generatedText?.let {
                    _uiState.value = UiState.Success(
                        city,
                        response,
                        generatedText
                    )
                } ?: run {
                    _uiState.value = UiState.Error("failed with generating weather description")
                }
            } ?: run {
                _uiState.value = UiState.Error("failed with fetching weather data")
            }
        }
    }

    private suspend fun sendPrompt(
        prompt: String
    ): String? {
        try {
            val response = generativeModel.generateContent(
                content {
                    text(prompt)
                }
            )
            return response.text
        } catch (e: Exception) {
            Log.e("WeatherViewModel", "sendPrompt: ", e)
            return null
        }
    }
}