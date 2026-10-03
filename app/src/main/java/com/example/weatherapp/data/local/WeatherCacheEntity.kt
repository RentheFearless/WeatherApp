package com.example.weatherapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Кеш прогнозу для міста — єдине джерело правди для екрана результатів. */
@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey val cityKey: String,
    val cityName: String,
    val country: String,
    val temperature: Double,
    val feelsLike: Double,
    val description: String,
    val icon: String,
    val humidity: Int,
    val windSpeed: Double,
    val pressure: Int,
    /** Погодинний прогноз, збережений як JSON-рядок */
    val forecastJson: String,
    val updatedAt: Long
)
