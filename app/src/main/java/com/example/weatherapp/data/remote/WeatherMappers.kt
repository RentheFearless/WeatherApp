package com.example.weatherapp.data.remote

import com.example.weatherapp.data.local.WeatherCacheEntity
import com.example.weatherapp.data.model.HourlyForecast
import com.example.weatherapp.data.model.Weather
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private val hourlyListSerializer = ListSerializer(HourlyForecast.serializer())

/** Відповідь API (поточна + прогноз) → запис кешу в Room. */
fun toCacheEntity(
    cityKey: String,
    /** Назва так, як її ввів користувач (API повертає латиницю, напр. "Lviv") */
    displayName: String,
    current: CurrentWeatherDto,
    forecast: ForecastDto,
    json: Json,
    now: Long = System.currentTimeMillis()
): WeatherCacheEntity {
    val description = current.weather.firstOrNull()
    val hourly = forecast.list.map { item ->
        val w = item.weather.firstOrNull()
        HourlyForecast(
            time = item.dt * 1000,
            temperature = item.main.temp,
            icon = w?.icon.orEmpty(),
            description = w?.description.orEmpty(),
            pop = item.pop
        )
    }
    return WeatherCacheEntity(
        cityKey = cityKey,
        cityName = displayName.ifBlank { current.name },
        country = current.sys.country.orEmpty(),
        temperature = current.main.temp,
        feelsLike = current.main.feelsLike,
        description = description?.description.orEmpty(),
        icon = description?.icon.orEmpty(),
        humidity = current.main.humidity,
        windSpeed = current.wind.speed,
        pressure = current.main.pressure,
        forecastJson = json.encodeToString(hourlyListSerializer, hourly),
        updatedAt = now
    )
}

/** Запис кешу → модель для UI. */
fun WeatherCacheEntity.toWeather(json: Json): Weather = Weather(
    cityName = cityName,
    country = country,
    temperature = temperature,
    feelsLike = feelsLike,
    description = description,
    icon = icon,
    humidity = humidity,
    windSpeed = windSpeed,
    pressure = pressure,
    forecast = runCatching { json.decodeFromString(hourlyListSerializer, forecastJson) }
        .getOrDefault(emptyList()),
    updatedAt = updatedAt
)
