package cn.gdeiassistant.ui.profile

import android.content.Context
import cn.gdeiassistant.data.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.runBlocking
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileThemeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var context: Context
    private lateinit var settingsRepository: SettingsRepository
    private val themeColorFlow = MutableStateFlow(SettingsRepository.DEFAULT_THEME_COLOR)
    private val localeFlow = MutableStateFlow("zh-CN")

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = mock()
        settingsRepository = mock()
        whenever(settingsRepository.themeColor).thenReturn(themeColorFlow)
        whenever(settingsRepository.locale).thenReturn(localeFlow)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateUsesDefaultTheme() = runTest(testDispatcher) {
        val viewModel = ProfileThemeViewModel(context, settingsRepository)
        advanceUntilIdle()

        assertEquals(SettingsRepository.DEFAULT_THEME_COLOR, viewModel.state.value.themeColor)
    }

    @Test
    fun selectingThemeUpdatesState() = runTest(testDispatcher) {
        val viewModel = ProfileThemeViewModel(context, settingsRepository)
        advanceUntilIdle()

        themeColorFlow.value = "deep-indigo"
        advanceUntilIdle()

        assertEquals("deep-indigo", viewModel.state.value.themeColor)
    }

    @Test
    fun selectingSameThemeDoesNotCallRepository() = runTest(testDispatcher) {
        val viewModel = ProfileThemeViewModel(context, settingsRepository)
        advanceUntilIdle()

        viewModel.selectTheme(SettingsRepository.DEFAULT_THEME_COLOR)
        advanceUntilIdle()

        runBlocking { verify(settingsRepository, never()).setThemeColor(any()) }
    }
}
