package com.example.weatherapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherapp.ui.theme.WeatherAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    recentCities: List<String>,
    apiKey: String?,
    onSearch: (city: String) -> Unit,
    onRemoveCity: (city: String) -> Unit,
    onClearHistory: () -> Unit,
    onSaveApiKey: (key: String) -> Unit,
    onClearApiKey: () -> Unit,
    onCheckWeatherNow: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // rememberSaveable — стан переживає поворот екрана
    var city by rememberSaveable { mutableStateOf("") }
    var showError by rememberSaveable { mutableStateOf(false) }
    var showKeyDialog by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    fun search(value: String = city) {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) {
            showError = true
            return
        }
        showError = false
        focusManager.clearFocus() // ховаємо клавіатуру
        onSearch(trimmed)         // збереження в історію + перехід на результати
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    // Лаба 5: ручний запуск фонової перевірки погоди
                    IconButton(onClick = onCheckWeatherNow, enabled = recentCities.isNotEmpty()) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Перевірити дощ зараз")
                    }
                    IconButton(onClick = { showKeyDialog = true }) {
                        Icon(Icons.Filled.Settings, contentDescription = "API-ключ")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            Text(text = "🌤", fontSize = 64.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Погода",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Дізнайтеся прогноз для будь-якого міста",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (apiKey == null) {
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { showKeyDialog = true }) {
                    Text("⚙ Додайте API-ключ OpenWeatherMap", color = MaterialTheme.colorScheme.secondary)
                }
            }
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = city,
                onValueChange = {
                    city = it
                    if (showError) showError = false
                },
                label = { Text("Місто") },
                placeholder = { Text("Наприклад, Львів") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                isError = showError,
                supportingText = {
                    if (showError) Text("Назва міста не може бути порожньою")
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(onSearch = { search() }),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { search() },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Знайти погоду", style = MaterialTheme.typography.titleMedium)
            }

            // Лаба 3: останні 5 міст з Room (оновлюються реактивно через Flow)
            if (recentCities.isNotEmpty()) {
                Spacer(Modifier.height(32.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Останні пошуки",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(onClick = onClearHistory) { Text("Очистити") }
                }
                Spacer(Modifier.height(8.dp))
                recentCities.forEach { name ->
                    RecentCityItem(
                        name = name,
                        onClick = {
                            city = name
                            search(name)
                        },
                        onRemove = { onRemoveCity(name) }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showKeyDialog) {
        ApiKeyDialog(
            currentKey = apiKey,
            onDismiss = { showKeyDialog = false },
            onSave = {
                onSaveApiKey(it)
                showKeyDialog = false
            },
            onClear = {
                onClearApiKey()
                showKeyDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecentCityItem(name: String, onClick: () -> Unit, onRemove: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Close, contentDescription = "Видалити $name з історії")
            }
        }
    }
}

@Composable
private fun ApiKeyDialog(
    currentKey: String?,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onClear: () -> Unit
) {
    var input by rememberSaveable { mutableStateOf("") }
    var visible by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Lock, contentDescription = null) },
        title = { Text("API-ключ OpenWeatherMap") },
        text = {
            Column {
                Text(
                    text = if (currentKey != null) "Збережений ключ: ${maskKey(currentKey)}"
                    else "Ключ ще не збережено",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Зберігається в зашифрованому сховищі (EncryptedSharedPreferences, AES-256).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it.trim() },
                    label = { Text("Новий ключ") },
                    singleLine = true,
                    visualTransformation = if (visible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    trailingIcon = {
                        TextButton(onClick = { visible = !visible }) {
                            Text(if (visible) "Сховати" else "Показати")
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(input) }, enabled = input.isNotBlank()) {
                Text("Зберегти")
            }
        },
        dismissButton = {
            Row {
                if (currentKey != null) {
                    TextButton(onClick = onClear) { Text("Видалити") }
                }
                TextButton(onClick = onDismiss) { Text("Скасувати") }
            }
        }
    )
}

/** Показуємо лише останні 4 символи ключа. */
private fun maskKey(key: String): String =
    if (key.length <= 4) "••••" else "••••••" + key.takeLast(4)

private val previewCities = listOf("Львів", "Київ", "Перемишль")

@Preview(name = "Темна", showBackground = true)
@Composable
private fun SearchScreenDarkPreview() {
    WeatherAppTheme(darkTheme = true) {
        SearchScreen(previewCities, null, {}, {}, {}, {}, {})
    }
}

@Preview(name = "Світла", showBackground = true)
@Composable
private fun SearchScreenLightPreview() {
    WeatherAppTheme(darkTheme = false) {
        SearchScreen(previewCities, "abcd1234", {}, {}, {}, {}, {})
    }
}
