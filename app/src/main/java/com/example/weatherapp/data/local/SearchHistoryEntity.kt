package com.example.weatherapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Запис історії пошуку.
 * cityKey — назва в нижньому регістрі, щоб «Львів» і «львів» були одним записом.
 */
@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey val cityKey: String,
    val cityName: String,
    val searchedAt: Long
)
