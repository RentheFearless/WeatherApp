package com.example.weatherapp.data

import com.example.weatherapp.data.remote.WeatherApi
import com.example.weatherapp.data.remote.toCacheEntity
import com.example.weatherapp.data.remote.toWeather
import com.example.weatherapp.util.TestData
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherMappersTest {

    private val json = WeatherApi.json

    @Test
    fun `toCacheEntity бере назву міста, введену користувачем, а не латиницю з API`() {
        val entity = toCacheEntity("львів", "Львів", TestData.currentDto, TestData.forecastDto, json, now = 42L)

        assertEquals("львів", entity.cityKey)
        assertEquals("Львів", entity.cityName)
        assertEquals("UA", entity.country)
        assertEquals(14.5, entity.temperature, 0.0)
        assertEquals("легкий дощ", entity.description)
        assertEquals(42L, entity.updatedAt)
    }

    @Test
    fun `toCacheEntity з порожньою назвою використовує назву з API`() {
        val entity = toCacheEntity("lviv", "", TestData.currentDto, TestData.forecastDto, json)
        assertEquals("Lviv", entity.cityName)
    }

    @Test
    fun `прогноз переживає збереження в JSON і читання назад`() {
        val entity = toCacheEntity("львів", "Львів", TestData.currentDto, TestData.forecastDto, json)
        val weather = entity.toWeather(json)

        assertEquals(2, weather.forecast.size)
        // секунди з API → мілісекунди в моделі
        assertEquals(1_790_010_800_000, weather.forecast[0].time)
        assertEquals(0.8, weather.forecast[0].pop, 0.0)
        assertEquals("04n", weather.forecast[1].icon)
    }

    @Test
    fun `пошкоджений JSON прогнозу дає порожній список, а не падіння`() {
        val entity = toCacheEntity("львів", "Львів", TestData.currentDto, TestData.forecastDto, json)
            .copy(forecastJson = "{not json")
        assertEquals(emptyList<Any>(), entity.toWeather(json).forecast)
    }
}
