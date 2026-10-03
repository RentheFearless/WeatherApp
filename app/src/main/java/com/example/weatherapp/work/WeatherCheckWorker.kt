package com.example.weatherapp.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.weatherapp.WeatherApplication
import com.example.weatherapp.data.model.HourlyForecast
import com.example.weatherapp.notifications.WeatherNotifications
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Фонова задача: бере останнє місто з історії, оновлює прогноз
 * і, якщо найближчу добу очікується дощ, шле сповіщення «Сьогодні дощ!».
 */
class WeatherCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as WeatherApplication).container
        // force = true — ручна перевірка з кнопки: показуємо результат завжди
        val force = inputData.getBoolean(KEY_FORCE, false)

        val city = container.searchHistoryRepository.recentCities(limit = 1).first().firstOrNull()
            ?: return Result.success() // ще нічого не шукали

        val refreshed = container.weatherRepository.refresh(city)
        val weather = container.weatherRepository.observeWeather(city).first()
        if (weather == null) {
            // Немає ні свіжих даних, ні кешу — спробуємо пізніше
            return if (refreshed.isFailure) Result.retry() else Result.success()
        }

        val rain = weather.forecast.firstOrNull { it.isRainy() }
            ?: if (isRainyIcon(weather.icon)) HourlyForecast(System.currentTimeMillis(), weather.temperature, weather.icon, weather.description, 1.0) else null

        val prefs = applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) + "|" + city.lowercase()

        when {
            rain != null && (force || prefs.getString(KEY_LAST_NOTIFIED, null) != todayKey) -> {
                val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(rain.time))
                val chance = (rain.pop * 100).roundToInt()
                WeatherNotifications.show(
                    applicationContext,
                    city = city,
                    title = "🌧 Сьогодні дощ!",
                    text = "$city: ${rain.description.ifBlank { "опади" }} близько $time" +
                        (if (chance in 1..99) " (ймовірність $chance%)" else "") +
                        ". Не забудьте парасольку ☂"
                )
                // Не більше одного попередження на день для міста
                prefs.edit().putString(KEY_LAST_NOTIFIED, todayKey).apply()
            }
            force && rain == null -> WeatherNotifications.show(
                applicationContext,
                city = city,
                title = "☀️ Дощу не очікується",
                text = "$city: зараз ${weather.temperature.roundToInt()}°C, ${weather.description}. " +
                    "Найближчу добу опадів не буде."
            )
        }
        return Result.success()
    }

    private fun HourlyForecast.isRainy() = isRainyIcon(icon) || pop >= RAIN_PROBABILITY

    /** 09 — злива, 10 — дощ, 11 — гроза (коди іконок OpenWeatherMap). */
    private fun isRainyIcon(icon: String) = icon.take(2) in setOf("09", "10", "11")

    companion object {
        const val KEY_FORCE = "force"
        private const val RAIN_PROBABILITY = 0.5
        private const val PREFS = "weather_worker"
        private const val KEY_LAST_NOTIFIED = "last_rain_notified"
    }
}
