package com.example.weatherapp.data

import com.example.weatherapp.data.local.WeatherDao
import com.example.weatherapp.data.model.Weather
import com.example.weatherapp.data.remote.WeatherApi
import com.example.weatherapp.data.remote.toCacheEntity
import com.example.weatherapp.data.remote.toWeather
import com.example.weatherapp.data.secure.SecureStorage
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.cancellation.CancellationException

class MissingApiKeyException : Exception("API-ключ не задано")

/**
 * Offline-First репозиторій (Single Source of Truth).
 * UI завжди читає дані тільки з Room; мережа лише оновлює базу.
 * Тож без інтернету показується останній збережений прогноз.
 */
class WeatherRepository(
    private val api: WeatherApi,
    private val dao: WeatherDao,
    private val secureStorage: SecureStorage
) {
    private val json = WeatherApi.json

    fun observeWeather(city: String): Flow<Weather?> =
        dao.observe(cityKey(city)).map { it?.toWeather(json) }

    /** Тягне свіжі дані з API і записує в кеш. Room сам оповістить UI через Flow. */
    suspend fun refresh(city: String): Result<Unit> = try {
        val apiKey = secureStorage.getApiKey() ?: throw MissingApiKeyException()
        coroutineScope {
            val current = async { api.getCurrentWeather(city.trim(), apiKey) }
            val forecast = async { api.getForecast(city.trim(), apiKey) }
            dao.upsert(toCacheEntity(cityKey(city), city.trim(), current.await(), forecast.await(), json))
        }
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

    private fun cityKey(city: String) = city.trim().lowercase()
}
