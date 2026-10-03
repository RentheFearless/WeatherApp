package com.example.weatherapp

import android.app.Application
import com.example.weatherapp.data.AppContainer
import com.example.weatherapp.notifications.WeatherNotifications
import com.example.weatherapp.work.WeatherWorkScheduler

/** Клас застосунку: тримає один AppContainer з базою, мережею та репозиторіями. */
class WeatherApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }

    override fun onCreate() {
        super.onCreate()
        // Лаба 5: канал сповіщень і періодична фонова перевірка погоди
        WeatherNotifications.createChannel(this)
        WeatherWorkScheduler.schedulePeriodic(this)
    }
}
