package cn.gdeiassistant.ui.profile

import android.content.Context
import cn.gdeiassistant.R
import cn.gdeiassistant.data.SettingsRepository
import cn.gdeiassistant.network.NetworkEnvironment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileSettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var context: Context
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var mockModeFlow: MutableStateFlow<Boolean>
    private lateinit var networkEnvironmentFlow: MutableStateFlow<NetworkEnvironment>

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = mock()
        settingsRepository = mock()
        mockModeFlow = MutableStateFlow(false)
        networkEnvironmentFlow = MutableStateFlow(NetworkEnvironment.STAGING)

        whenever(settingsRepository.isMockModeEnabled).thenReturn(mockModeFlow)
        whenever(settingsRepository.networkEnvironment).thenReturn(networkEnvironmentFlow)
        whenever(context.getString(R.string.profile_settings_toggle_failed)).thenReturn("切换失败")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun stateTracksMockModeAndNetworkEnvironmentFlows() = runTest(testDispatcher) {
        val viewModel = ProfileSettingsViewModel(context, settingsRepository)
        advanceUntilIdle()

        assertEquals(false, viewModel.state.value.isMockModeEnabled)
        assertEquals(NetworkEnvironment.STAGING, viewModel.state.value.networkEnvironment)
        assertEquals(NetworkEnvironment.STAGING.baseUrl, viewModel.state.value.environmentBaseUrl)

        mockModeFlow.value = true
        networkEnvironmentFlow.value = NetworkEnvironment.DEV
        advanceUntilIdle()

        assertEquals(true, viewModel.state.value.isMockModeEnabled)
        assertEquals(NetworkEnvironment.DEV, viewModel.state.value.networkEnvironment)
        assertEquals(NetworkEnvironment.DEV.baseUrl, viewModel.state.value.environmentBaseUrl)
    }

    @Test
    fun setNetworkEnvironmentDelegatesToRepository() = runTest(testDispatcher) {
        val viewModel = ProfileSettingsViewModel(context, settingsRepository)

        viewModel.setNetworkEnvironment(NetworkEnvironment.PROD)
        advanceUntilIdle()

        verify(settingsRepository).setNetworkEnvironment(NetworkEnvironment.PROD)
    }

    @Test
    fun setNetworkEnvironmentEmitsReadableFailureMessage() = runTest(testDispatcher) {
        doThrow(IllegalStateException("保存失败")).whenever(settingsRepository).setNetworkEnvironment(NetworkEnvironment.DEV)
        val viewModel = ProfileSettingsViewModel(context, settingsRepository)
        val eventDeferred = async { viewModel.events.first() }

        viewModel.setNetworkEnvironment(NetworkEnvironment.DEV)
        advanceUntilIdle()

        assertEquals(ProfileSettingsEvent.ShowMessage("保存失败"), eventDeferred.await())
    }
}
