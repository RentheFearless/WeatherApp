package com.example.weatherapp.data.remote

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "uk"
    ): CurrentWeatherDto

    /** cnt = 8 → 8 точок по 3 години = найближча доба. */
    @GET("data/2.5/forecast")
    suspend fun getForecast(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("cnt") count: Int = 8,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "uk"
    ): ForecastDto

    companion object {
        private const val BASE_URL = "https://api.openweathermap.org/"

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        fun create(): WeatherApi = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(WeatherApi::class.java)
    }
}
