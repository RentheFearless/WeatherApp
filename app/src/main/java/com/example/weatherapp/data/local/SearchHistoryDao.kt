package com.example.weatherapp.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {

    /**
     * Останні N міст, від найновішого.
     * Повертає Flow: Room сам надсилає новий список після кожної зміни таблиці,
     * тому UI оновлюється реактивно, без ручного перезавантаження.
     */
    @Query("SELECT * FROM search_history ORDER BY searchedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<SearchHistoryEntity>>

    /** Додає місто або оновлює час пошуку, якщо воно вже є (місто піднімається нагору). */
    @Upsert
    suspend fun upsert(item: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE cityKey = :cityKey")
    suspend fun delete(cityKey: String)

    @Query("DELETE FROM search_history")
    suspend fun clear()
}
