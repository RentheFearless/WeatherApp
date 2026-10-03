package com.example.weatherapp.data

import android.content.Context
import com.example.weatherapp.data.local.WeatherDatabase
import com.example.weatherapp.data.remote.WeatherApi
import com.example.weatherapp.data.secure.SecureStorage

/** Простий ручний DI: створює залежності один раз на весь застосунок. */
class AppContainer(context: Context) {
    private val database = WeatherDatabase.getInstance(context)
    val secureStorage = SecureStorage(context)
    private val weatherApi: WeatherApi = WeatherApi.create()

    val searchHistoryRepository = SearchHistoryRepository(database.searchHistoryDao())
    val weatherRepository = WeatherRepository(weatherApi, database.weatherDao(), secureStorage)
}
