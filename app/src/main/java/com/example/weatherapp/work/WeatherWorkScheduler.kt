package com.example.weatherapp.work

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

object WeatherWorkScheduler {

    private const val PERIODIC_WORK = "weather_periodic_check"
    private const val MANUAL_WORK = "weather_manual_check"

    private val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED) // тільки з інтернетом
        .setRequiresBatteryNotLow(true)                // не садимо батарею
        .build()

    /** Періодична перевірка кожні 3 години. KEEP — не перезапускати, якщо вже заплановано. */
    fun schedulePeriodic(context: Context) {
        val request = PeriodicWorkRequestBuilder<WeatherCheckWorker>(3, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(PERIODIC_WORK, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    /** Разова перевірка «зараз» — для демонстрації на лабі. */
    fun runNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<WeatherCheckWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setInputData(workDataOf(WeatherCheckWorker.KEY_FORCE to true))
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(MANUAL_WORK, ExistingWorkPolicy.REPLACE, request)
    }
}
