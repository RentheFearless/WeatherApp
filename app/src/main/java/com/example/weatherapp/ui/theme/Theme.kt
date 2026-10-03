package com.example.weatherapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColors = darkColorScheme(
    primary = SkyBlue80,
    onPrimary = SkyBlue20,
    primaryContainer = SkyBlue30,
    onPrimaryContainer = SkyBlue90,
    secondary = Sun80,
    onSecondary = Sun20,
    background = Night10,
    onBackground = Night90,
    surface = Night10,
    onSurface = Night90,
    surfaceVariant = Night20,
    onSurfaceVariant = Night80,
)

private val LightColors = lightColorScheme(
    primary = SkyBlue40,
    onPrimary = Color.White,
    primaryContainer = SkyBlue90,
    onPrimaryContainer = SkyBlue20,
    secondary = Sun40,
    onSecondary = Color.White,
    background = Day99,
    onBackground = Ink10,
    surface = Day99,
    onSurface = Ink10,
    surfaceVariant = Day95,
    onSurfaceVariant = Ink40,
)

@Composable
fun WeatherAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic Color (Android 12+) вимкнено, щоб завжди була наша палітра
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // з Type.kt
        content = content
    )
}