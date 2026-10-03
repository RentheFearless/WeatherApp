package com.example.weatherapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import com.example.weatherapp.WeatherApplication
import com.example.weatherapp.data.MissingApiKeyException
import com.example.weatherapp.data.WeatherRepository
import com.example.weatherapp.data.model.Weather
import com.example.weatherapp.ui.navigation.ResultsRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/** Стан екрана результатів. */
data class ResultsUiState(
    val weather: Weather? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * city приходить у конструктор (а не SavedStateHandle напряму),
 * щоб ViewModel легко тестувалась без Android (Лаба 6).
 */
class ResultsViewModel(
    val city: String,
    private val repository: WeatherRepository
) : ViewModel() {

    private val loading = MutableStateFlow(true)
    private val error = MutableStateFlow<String?>(null)

    /** Кеш з Room + стан завантаження → один стан для UI. */
    val uiState: StateFlow<ResultsUiState> = combine(
        repository.observeWeather(city), loading, error
    ) { weather, isLoading, errorMessage ->
        ResultsUiState(weather, isLoading, errorMessage)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ResultsUiState())

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            loading.value = true
            error.value = null
            repository.refresh(city).onFailure { error.value = it.toUserMessage() }
            loading.value = false
        }
    }

    private fun Throwable.toUserMessage(): String = when (this) {
        is MissingApiKeyException -> "Додайте API-ключ: ⚙ на екрані пошуку"
        is HttpException -> when (code()) {
            401 -> "Ключ недійсний або ще не активувався (до 2 годин після створення)"
            404 -> "Місто не знайдено. Перевірте назву"
            429 -> "Забагато запитів, спробуйте пізніше"
            else -> "Помилка сервера (${code()})"
        }
        is IOException -> "Немає з'єднання з інтернетом"
        else -> "Помилка: ${message ?: javaClass.simpleName}"
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) as WeatherApplication
                ResultsViewModel(
                    // Типізований аргумент з навігації (Лаба 2)
                    city = createSavedStateHandle().toRoute<ResultsRoute>().city,
                    repository = app.container.weatherRepository
                )
            }
        }
    }
}
