package com.example.weatherapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [SearchHistoryEntity::class, WeatherCacheEntity::class],
    version = 2,
    exportSchema = false
)
abstract class WeatherDatabase : RoomDatabase() {

    abstract fun searchHistoryDao(): SearchHistoryDao

    abstract fun weatherDao(): WeatherDao

    companion object {
        /** Лаба 4: додали таблицю кешу погоди, історія пошуку зберігається. */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `weather_cache` (" +
                        "`cityKey` TEXT NOT NULL, `cityName` TEXT NOT NULL, " +
                        "`country` TEXT NOT NULL, `temperature` REAL NOT NULL, " +
                        "`feelsLike` REAL NOT NULL, `description` TEXT NOT NULL, " +
                        "`icon` TEXT NOT NULL, `humidity` INTEGER NOT NULL, " +
                        "`windSpeed` REAL NOT NULL, `pressure` INTEGER NOT NULL, " +
                        "`forecastJson` TEXT NOT NULL, `updatedAt` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`cityKey`))"
                )
            }
        }

        @Volatile
        private var INSTANCE: WeatherDatabase? = null

        /** Одна база на весь застосунок (Singleton). */
        fun getInstance(context: Context): WeatherDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    WeatherDatabase::class.java,
                    "weather.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
