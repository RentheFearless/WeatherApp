package com.example.weatherapp.data

import com.example.weatherapp.data.local.SearchHistoryDao
import com.example.weatherapp.data.local.SearchHistoryEntity
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchHistoryRepositoryTest {

    private val dao = mockk<SearchHistoryDao>(relaxed = true)
    private val repository = SearchHistoryRepository(dao)

    @Test
    fun `addSearch обрізає пробіли і будує ключ у нижньому регістрі`() = runTest {
        val saved = slot<SearchHistoryEntity>()

        repository.addSearch("  Львів ")

        coVerify { dao.upsert(capture(saved)) }
        assertEquals("львів", saved.captured.cityKey)
        assertEquals("Львів", saved.captured.cityName)
        assertTrue(saved.captured.searchedAt > 0)
    }

    @Test
    fun `recentCities повертає назви міст у порядку з бази`() = runTest {
        every { dao.observeRecent(5) } returns flowOf(
            listOf(
                SearchHistoryEntity("київ", "Київ", 2),
                SearchHistoryEntity("львів", "Львів", 1)
            )
        )

        assertEquals(listOf("Київ", "Львів"), repository.recentCities(5).first())
    }

    @Test
    fun `remove видаляє за ключем незалежно від регістру`() = runTest {
        repository.remove(" ЛЬВІВ ")
        coVerify { dao.delete("львів") }
    }

    @Test
    fun `clear очищає всю історію`() = runTest {
        repository.clear()
        coVerify { dao.clear() }
    }
}
