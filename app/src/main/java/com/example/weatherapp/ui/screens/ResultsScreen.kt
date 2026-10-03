package com.example.weatherapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherapp.data.model.HourlyForecast
import com.example.weatherapp.data.model.Weather
import com.example.weatherapp.ui.components.ShimmerBox
import com.example.weatherapp.ui.theme.WeatherAppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    city: String,
    state: ResultsUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(state.weather?.cityName ?: city) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh, enabled = !state.isLoading) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Оновити")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Тонка смужка, коли оновлюємо вже показані (кешовані) дані
            if (state.isLoading && state.weather != null) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                val weather = state.weather
                when {
                    weather != null -> {
                        // Офлайн / помилка: показуємо кеш і попередження
                        state.error?.let { InfoBanner("$it. Показано дані від ${formatTime(weather.updatedAt)}") }
                        WeatherContent(weather)
                    }
                    state.isLoading -> WeatherShimmer()
                    state.error != null -> ErrorContent(state.error, onRefresh)
                }
            }
        }
    }
}

@Composable
private fun WeatherContent(weather: Weather) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = weatherEmoji(weather.icon), fontSize = 64.sp)
            Text(
                text = "${weather.temperature.roundToInt()}°C",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = weather.description.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Відчувається як ${weather.feelsLike.roundToInt()}°C",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }

    Spacer(Modifier.height(16.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        DetailCard("💧", "Вологість", "${weather.humidity}%", Modifier.weight(1f))
        DetailCard("💨", "Вітер", "${weather.windSpeed.roundToInt()} м/с", Modifier.weight(1f))
        DetailCard("🧭", "Тиск", "${weather.pressure} гПа", Modifier.weight(1f))
    }

    if (weather.forecast.isNotEmpty()) {
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Прогноз на добу",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(weather.forecast, key = { it.time }) { item -> ForecastItemCard(item) }
        }
    }

    Spacer(Modifier.height(16.dp))
    Text(
        text = "Оновлено о ${formatTime(weather.updatedAt)}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun DetailCard(emoji: String, label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 20.sp)
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ForecastItemCard(item: HourlyForecast) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.width(76.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = formatTime(item.time),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(weatherEmoji(item.icon), fontSize = 24.sp)
            Text(
                text = "${item.temperature.roundToInt()}°",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (item.pop >= 0.1) {
                Text(
                    text = "☔ ${(item.pop * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/** Заготовка екрана під час першого завантаження (коли кешу ще немає). */
@Composable
private fun WeatherShimmer() {
    ShimmerBox(
        Modifier
            .fillMaxWidth()
            .height(230.dp)
    )
    Spacer(Modifier.height(16.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(3) { ShimmerBox(Modifier.weight(1f).height(84.dp)) }
    }
    Spacer(Modifier.height(24.dp))
    ShimmerBox(Modifier.width(160.dp).height(20.dp))
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(4) { ShimmerBox(Modifier.size(width = 76.dp, height = 100.dp)) }
    }
}

@Composable
private fun InfoBanner(text: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondary)
            Spacer(Modifier.width(8.dp))
            Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondary)
        }
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("😕", fontSize = 56.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry, shape = RoundedCornerShape(16.dp)) { Text("Спробувати ще") }
    }
}

/** Код іконки OpenWeatherMap (наприклад, "10d") → емодзі. */
private fun weatherEmoji(icon: String): String = when (icon.take(2)) {
    "01" -> if (icon.endsWith("n")) "🌙" else "☀️"
    "02" -> if (icon.endsWith("n")) "☁️" else "🌤"
    "03", "04" -> "☁️"
    "09" -> "🌧"
    "10" -> "🌦"
    "11" -> "⛈"
    "13" -> "❄️"
    "50" -> "🌫"
    else -> "🌡"
}

private fun formatTime(millis: Long): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(millis))

private val previewWeather = Weather(
    cityName = "Львів", country = "UA", temperature = 14.3, feelsLike = 13.1,
    description = "легкий дощ", icon = "10d", humidity = 78, windSpeed = 4.2, pressure = 1012,
    forecast = List(8) { i ->
        HourlyForecast(1_790_000_000_000 + i * 10_800_000L, 14.0 - i, if (i % 2 == 0) "10d" else "04d", "", i * 0.1)
    },
    updatedAt = 1_790_000_000_000
)

@Preview(name = "Дані", showBackground = true)
@Composable
private fun ResultsDataPreview() {
    WeatherAppTheme(darkTheme = true) {
        ResultsScreen("Львів", ResultsUiState(previewWeather, isLoading = false), {}, {})
    }
}

@Preview(name = "Shimmer", showBackground = true)
@Composable
private fun ResultsLoadingPreview() {
    WeatherAppTheme(darkTheme = false) {
        ResultsScreen("Львів", ResultsUiState(isLoading = true), {}, {})
    }
}

@Preview(name = "Офлайн", showBackground = true)
@Composable
private fun ResultsOfflinePreview() {
    WeatherAppTheme(darkTheme = false) {
        ResultsScreen("Львів", ResultsUiState(previewWeather, false, "Немає з'єднання з інтернетом"), {}, {})
    }
}
