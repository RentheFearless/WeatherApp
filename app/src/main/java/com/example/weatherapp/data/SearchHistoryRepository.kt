package com.example.weatherapp.data

import com.example.weatherapp.data.local.SearchHistoryDao
import com.example.weatherapp.data.local.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SearchHistoryRepository(private val dao: SearchHistoryDao) {

    /** Реактивний список назв останніх міст. */
    fun recentCities(limit: Int = 5): Flow<List<String>> =
        dao.observeRecent(limit).map { list -> list.map { it.cityName } }

    suspend fun addSearch(city: String) {
        val name = city.trim()
        dao.upsert(
            SearchHistoryEntity(
                cityKey = name.lowercase(),
                cityName = name,
                searchedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun remove(city: String) = dao.delete(city.trim().lowercase())

    suspend fun clear() = dao.clear()
}
