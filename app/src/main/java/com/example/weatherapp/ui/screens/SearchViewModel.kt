package com.example.weatherapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.weatherapp.WeatherApplication
import com.example.weatherapp.data.SearchHistoryRepository
import com.example.weatherapp.data.secure.SecureStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SearchViewModel(
    private val history: SearchHistoryRepository,
    private val secureStorage: SecureStorage
) : ViewModel() {

    /** Останні 5 міст. Оновлюється автоматично, щойно змінюється таблиця в Room. */
    val recentCities: StateFlow<List<String>> = history.recentCities(limit = 5)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _apiKey = MutableStateFlow(secureStorage.getApiKey())
    val apiKey: StateFlow<String?> = _apiKey.asStateFlow()

    fun onSearch(city: String) {
        viewModelScope.launch { history.addSearch(city) }
    }

    fun removeCity(city: String) {
        viewModelScope.launch { history.remove(city) }
    }

    fun clearHistory() {
        viewModelScope.launch { history.clear() }
    }

    fun saveApiKey(key: String) {
        val trimmed = key.trim()
        if (trimmed.isEmpty()) return
        secureStorage.saveApiKey(trimmed)
        _apiKey.value = trimmed
    }

    fun clearApiKey() {
        secureStorage.clearApiKey()
        _apiKey.value = null
    }

    companion object {
        /** Фабрика: бере залежності з AppContainer (ручний DI). */
        val Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) as WeatherApplication
                SearchViewModel(
                    history = app.container.searchHistoryRepository,
                    secureStorage = app.container.secureStorage
                )
            }
        }
    }
}
