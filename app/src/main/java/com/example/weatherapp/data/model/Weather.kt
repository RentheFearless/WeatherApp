package com.example.weatherapp.data.model

import kotlinx.serialization.Serializable

/** Модель погоди для UI (не залежить від формату API чи бази). */
data class Weather(
    val cityName: String,
    val country: String,
    val temperature: Double,
    val feelsLike: Double,
    val description: String,
    val icon: String,
    val humidity: Int,
    val windSpeed: Double,
    val pressure: Int,
    val forecast: List<HourlyForecast>,
    /** Коли дані отримано з мережі (мс). */
    val updatedAt: Long
)

@Serializable
data class HourlyForecast(
    /** Час у мілісекундах */
    val time: Long,
    val temperature: Double,
    val icon: String,
    val description: String,
    /** Ймовірність опадів 0..1 */
    val pop: Double
)
