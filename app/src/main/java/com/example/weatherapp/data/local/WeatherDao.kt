package com.example.weatherapp.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {

    /** Реактивно віддає кеш міста; null — якщо даних ще немає. */
    @Query("SELECT * FROM weather_cache WHERE cityKey = :cityKey")
    fun observe(cityKey: String): Flow<WeatherCacheEntity?>

    @Upsert
    suspend fun upsert(entity: WeatherCacheEntity)
}
