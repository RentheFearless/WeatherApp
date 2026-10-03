package com.example.weatherapp.data

import com.example.weatherapp.data.local.WeatherCacheEntity
import com.example.weatherapp.data.local.WeatherDao
import com.example.weatherapp.data.remote.WeatherApi
import com.example.weatherapp.data.remote.toCacheEntity
import com.example.weatherapp.data.secure.SecureStorage
import com.example.weatherapp.util.TestData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class WeatherRepositoryTest {

    private val api = mockk<WeatherApi>()
    private val dao = mockk<WeatherDao>(relaxed = true)
    private val secureStorage = mockk<SecureStorage>()
    private val repository = WeatherRepository(api, dao, secureStorage)

    private fun givenApiReturnsData() {
        coEvery { api.getCurrentWeather("Львів", "test-key", any(), any()) } returns TestData.currentDto
        coEvery { api.getForecast("Львів", "test-key", any(), any(), any()) } returns TestData.forecastDto
    }

    @Test
    fun `refresh без API-ключа повертає MissingApiKeyException і не ходить у мережу`() = runTest {
        every { secureStorage.getApiKey() } returns null

        val result = repository.refresh("Львів")

        assertTrue(result.exceptionOrNull() is MissingApiKeyException)
        coVerify(exactly = 0) { api.getCurrentWeather(any(), any(), any(), any()) }
        coVerify(exactly = 0) { dao.upsert(any()) }
    }

    @Test
    fun `успішний refresh записує прогноз у кеш Room`() = runTest {
        every { secureStorage.getApiKey() } returns "test-key"
        givenApiReturnsData()
        val saved = slot<WeatherCacheEntity>()

        val result = repository.refresh("  Львів ")

        assertTrue(result.isSuccess)
        coVerify { dao.upsert(capture(saved)) }
        assertEquals("львів", saved.captured.cityKey)
        assertEquals("Львів", saved.captured.cityName)
        assertEquals(14.5, saved.captured.temperature, 0.0)
    }

    @Test
    fun `помилка мережі повертає failure і не чіпає кеш (offline-first)`() = runTest {
        every { secureStorage.getApiKey() } returns "test-key"
        coEvery { api.getCurrentWeather(any(), any(), any(), any()) } throws IOException("offline")
        coEvery { api.getForecast(any(), any(), any(), any(), any()) } returns TestData.forecastDto

        val result = repository.refresh("Львів")

        assertTrue(result.exceptionOrNull() is IOException)
        coVerify(exactly = 0) { dao.upsert(any()) }
    }

    @Test
    fun `observeWeather перетворює кеш з бази на модель для UI`() = runTest {
        val cached = toCacheEntity("львів", "Львів", TestData.currentDto, TestData.forecastDto, WeatherApi.json)
        every { dao.observe("львів") } returns flowOf(cached)

        val weather = repository.observeWeather(" Львів ").first()

        assertEquals("Львів", weather?.cityName)
        assertEquals(2, weather?.forecast?.size)
    }

    @Test
    fun `observeWeather повертає null, якщо кешу для міста ще немає`() = runTest {
        every { dao.observe(any()) } returns flowOf(null)
        assertNull(repository.observeWeather("Одеса").first())
    }
}
