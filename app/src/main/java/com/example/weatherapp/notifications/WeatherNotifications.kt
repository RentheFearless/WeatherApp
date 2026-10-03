package com.example.weatherapp.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.weatherapp.MainActivity
import com.example.weatherapp.R
import com.example.weatherapp.ui.navigation.DEEP_LINK_RESULTS

/**
 * Notification Channel + PendingIntent / Deep Link (AI-завдання Лаби 5).
 */
object WeatherNotifications {

    private const val CHANNEL_ID = "weather_alerts"
    private const val NOTIFICATION_ID = 1001

    /** Канал створюється один раз (Android 8+). Високий пріоритет → спливаюче сповіщення. */
    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Попередження про погоду",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Сповіщення про дощ та зміну погоди"
            enableVibration(true)
        }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    fun canNotify(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    /** Показує сповіщення; клік відкриває екран результатів цього міста через deep link. */
    fun show(context: Context, city: String, title: String, text: String) {
        if (!canNotify(context)) return

        val deepLinkIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("$DEEP_LINK_RESULTS/${Uri.encode(city)}"),
            context,
            MainActivity::class.java
        ).apply {
            // Новий чистий стек: Пошук → Результати (як у CosmoTrack)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            city.hashCode(),
            deepLinkIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_rain)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Дозвіл відкликали між перевіркою та показом — просто пропускаємо
        }
    }
}
