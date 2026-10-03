package com.example.weatherapp.ui

import com.example.weatherapp.data.SearchHistoryRepository
import com.example.weatherapp.data.secure.SecureStorage
import com.example.weatherapp.ui.screens.SearchViewModel
import com.example.weatherapp.util.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val history = mockk<SearchHistoryRepository>(relaxed = true)
    private val secureStorage = mockk<SecureStorage>(relaxed = true)

    @Before
    fun setUp() {
        every { history.recentCities(5) } returns flowOf(listOf("Львів", "Київ"))
        every { secureStorage.getApiKey() } returns null
    }

    private fun createViewModel() = SearchViewModel(history, secureStorage)

    @Test
    fun `recentCities віддає останні міста з репозиторію`() = runTest {
        val viewModel = createViewModel()
        // stateIn(WhileSubscribed) рахує лише при наявності підписника
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.recentCities.collect {} }
        advanceUntilIdle()

        assertEquals(listOf("Львів", "Київ"), viewModel.recentCities.value)
    }

    @Test
    fun `onSearch зберігає місто в історію`() = runTest {
        createViewModel().onSearch("Львів")
        advanceUntilIdle()
        coVerify { history.addSearch("Львів") }
    }

    @Test
    fun `removeCity і clearHistory викликають репозиторій`() = runTest {
        val viewModel = createViewModel()
        viewModel.removeCity("Київ")
        viewModel.clearHistory()
        advanceUntilIdle()

        coVerify { history.remove("Київ") }
        coVerify { history.clear() }
    }

    @Test
    fun `saveApiKey обрізає пробіли і зберігає в зашифроване сховище`() {
        val viewModel = createViewModel()

        viewModel.saveApiKey("  abc123  ")

        verify { secureStorage.saveApiKey("abc123") }
        assertEquals("abc123", viewModel.apiKey.value)
    }

    @Test
    fun `порожній ключ не зберігається`() {
        val viewModel = createViewModel()

        viewModel.saveApiKey("   ")

        verify(exactly = 0) { secureStorage.saveApiKey(any()) }
        assertNull(viewModel.apiKey.value)
    }

    @Test
    fun `clearApiKey видаляє збережений ключ`() {
        every { secureStorage.getApiKey() } returns "old-key"
        val viewModel = createViewModel()
        assertEquals("old-key", viewModel.apiKey.value)

        viewModel.clearApiKey()

        verify { secureStorage.clearApiKey() }
        assertNull(viewModel.apiKey.value)
    }
}
