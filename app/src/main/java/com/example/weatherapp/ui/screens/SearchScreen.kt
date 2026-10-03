package com.example.weatherapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherapp.ui.theme.WeatherAppTheme

@Composable
fun SearchScreen(
    onSearch: (city: String) -> Unit,
    modifier: Modifier = Modifier
) {
    // rememberSaveable — стан переживає поворот екрана
    var city by rememberSaveable { mutableStateOf("") }
    var showError by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    fun search() {
        val trimmed = city.trim()
        if (trimmed.isEmpty()) {
            showError = true
            return
        }
        showError = false
        focusManager.clearFocus() // ховаємо клавіатуру
        onSearch(trimmed)        // перехід на екран результатів
    }

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
            Spacer(Modifier.height(32.dp))

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
        }
    }
}

@Preview(name = "Темна", showBackground = true)
@Composable
private fun SearchScreenDarkPreview() {
    WeatherAppTheme(darkTheme = true) { SearchScreen(onSearch = {}) }
}

@Preview(name = "Світла", showBackground = true)
@Composable
private fun SearchScreenLightPreview() {
    WeatherAppTheme(darkTheme = false) { SearchScreen(onSearch = {}) }
}
