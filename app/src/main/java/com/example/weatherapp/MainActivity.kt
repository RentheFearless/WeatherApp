package com.example.weatherapp

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.example.weatherapp.notifications.WeatherNotifications
import com.example.weatherapp.ui.navigation.WeatherNavHost
import com.example.weatherapp.ui.theme.WeatherAppTheme

class MainActivity : ComponentActivity() {

    // Лаба 5: запит дозволу на сповіщення (Android 13+)
    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !WeatherNotifications.canNotify(this)) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            WeatherAppTheme {
                // NavHost сам обробляє deep link з intent (клік по сповіщенню)
                WeatherNavHost()
            }
        }
    }
}
