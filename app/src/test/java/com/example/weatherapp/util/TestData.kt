package com.example.weatherapp.util

import com.example.weatherapp.data.model.HourlyForecast
import com.example.weatherapp.data.model.Weather
import com.example.weatherapp.data.remote.CurrentWeatherDto
import com.example.weatherapp.data.remote.ForecastDto
import com.example.weatherapp.data.remote.ForecastItemDto
import com.example.weatherapp.data.remote.MainDto
import com.example.weatherapp.data.remote.SysDto
import com.example.weatherapp.data.remote.WeatherDescriptionDto
import com.example.weatherapp.data.remote.WindDto

/** Тестові дані, схожі на реальну відповідь OpenWeatherMap для Львова. */
object TestData {

    val currentDto = CurrentWeatherDto(
        name = "Lviv",
        dt = 1_790_000_000,
        main = MainDto(temp = 14.5, feelsLike = 13.2, pressure = 1012, humidity = 78),
        weather = listOf(WeatherDescriptionDto(500, "Rain", "легкий дощ", "10d")),
        wind = WindDto(speed = 4.2),
        sys = SysDto(country = "UA")
    )

    val forecastDto = ForecastDto(
        list = listOf(
            ForecastItemDto(
                dt = 1_790_010_800,
                main = MainDto(13.0, 12.0, 1011, 80),
                weather = listOf(WeatherDescriptionDto(500, "Rain", "легкий дощ", "10d")),
                pop = 0.8
            ),
            ForecastItemDto(
                dt = 1_790_021_600,
                main = MainDto(11.0, 10.0, 1010, 85),
                weather = listOf(WeatherDescriptionDto(804, "Clouds", "хмарно", "04n")),
                pop = 0.1
            )
        )
    )

    val weather = Weather(
        cityName = "Львів",
        country = "UA",
        temperature = 14.5,
        feelsLike = 13.2,
        description = "легкий дощ",
        icon = "10d",
        humidity = 78,
        windSpeed = 4.2,
        pressure = 1012,
        forecast = listOf(HourlyForecast(1_790_010_800_000, 13.0, "10d", "легкий дощ", 0.8)),
        updatedAt = 1_790_000_000_000
    )
}
