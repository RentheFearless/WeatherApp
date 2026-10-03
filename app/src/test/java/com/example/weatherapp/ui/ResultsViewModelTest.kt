package com.example.weatherapp.ui

import com.example.weatherapp.data.MissingApiKeyException
import com.example.weatherapp.data.WeatherRepository
import com.example.weatherapp.data.model.Weather
import com.example.weatherapp.ui.screens.ResultsViewModel
import com.example.weatherapp.util.MainDispatcherRule
import com.example.weatherapp.util.TestData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ResultsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<WeatherRepository>()
    /** Імітація таблиці кешу в Room. */
    private val cache = MutableStateFlow<Weather?>(null)

    @Before
    fun setUp() {
        every { repository.observeWeather("Львів") } returns cache
    }

    /** Створює ViewModel і підписується на стан, як це робить екран. */
    private fun TestScope.createViewModel(): ResultsViewModel {
        val viewModel = ResultsViewModel("Львів", repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        return viewModel
    }

    private fun httpError(code: Int) = HttpException(Response.error<Any>(code, "".toResponseBody()))

    @Test
    fun `успішне оновлення показує дані з кешу без помилки`() = runTest {
        coEvery { repository.refresh("Львів") } coAnswers {
            cache.value = TestData.weather // репозиторій записав у Room
            Result.success(Unit)
        }

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(TestData.weather, state.weather)
    }

    @Test
    fun `без інтернету показує кеш і повідомлення про офлайн`() = runTest {
        cache.value = TestData.weather
        coEvery { repository.refresh("Львів") } returns Result.failure(IOException("no network"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(TestData.weather, state.weather)
        assertEquals("Немає з'єднання з інтернетом", state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `404 від API означає, що місто не знайдено`() = runTest {
        coEvery { repository.refresh("Львів") } returns Result.failure(httpError(404))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("Місто не знайдено. Перевірте назву", viewModel.uiState.value.error)
        assertNull(viewModel.uiState.value.weather)
    }

    @Test
    fun `401 від API означає проблему з ключем`() = runTest {
        coEvery { repository.refresh("Львів") } returns Result.failure(httpError(401))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.error!!.startsWith("Ключ недійсний"))
    }

    @Test
    fun `без API-ключа просить його додати`() = runTest {
        coEvery { repository.refresh("Львів") } returns Result.failure(MissingApiKeyException())

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.error!!.contains("API-ключ"))
    }

    @Test
    fun `кнопка оновити повторно звертається до репозиторію`() = runTest {
        coEvery { repository.refresh("Львів") } returns Result.success(Unit)

        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.refresh()
        advanceUntilIdle()

        coVerify(exactly = 2) { repository.refresh("Львів") }
    }
}
