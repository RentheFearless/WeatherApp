package com.example.weatherapp.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Data-класи, згенеровані з JSON-відповідей OpenWeatherMap (AI-завдання Лаби 4).
// Зайві поля відповіді ігноруються (ignoreUnknownKeys = true).

/** GET /data/2.5/weather — поточна погода. */
@Serializable
data class CurrentWeatherDto(
    val name: String,
    val dt: Long,
    val main: MainDto,
    val weather: List<WeatherDescriptionDto>,
    val wind: WindDto,
    val sys: SysDto = SysDto()
)

@Serializable
data class MainDto(
    val temp: Double,
    @SerialName("feels_like") val feelsLike: Double,
    val pressure: Int,
    val humidity: Int
)

@Serializable
data class WeatherDescriptionDto(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

@Serializable
data class WindDto(val speed: Double)

@Serializable
data class SysDto(val country: String? = null)

/** GET /data/2.5/forecast — прогноз з кроком 3 години. */
@Serializable
data class ForecastDto(val list: List<ForecastItemDto>)

@Serializable
data class ForecastItemDto(
    val dt: Long,
    val main: MainDto,
    val weather: List<WeatherDescriptionDto>,
    /** Ймовірність опадів 0..1 */
    val pop: Double = 0.0
)
