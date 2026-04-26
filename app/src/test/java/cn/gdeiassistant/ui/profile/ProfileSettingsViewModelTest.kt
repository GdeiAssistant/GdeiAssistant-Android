package cn.gdeiassistant.ui.profile

import android.content.Context
import cn.gdeiassistant.R
import cn.gdeiassistant.data.CampusCredentialRepository
import cn.gdeiassistant.data.SettingsRepository
import cn.gdeiassistant.model.CampusCredentialStatus
import cn.gdeiassistant.network.NetworkEnvironment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileSettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var context: Context
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var campusCredentialRepository: CampusCredentialRepository
    private lateinit var mockModeFlow: MutableStateFlow<Boolean>
    private lateinit var networkEnvironmentFlow: MutableStateFlow<NetworkEnvironment>

    private val defaultStatus = CampusCredentialStatus(
        hasActiveConsent = true,
        hasSavedCredential = true,
        quickAuthEnabled = true,
        consentedAt = "2026-04-26 10:32:15",
        maskedCampusAccount = "20****46"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = mock()
        settingsRepository = mock()
        campusCredentialRepository = mock()
        mockModeFlow = MutableStateFlow(false)
        networkEnvironmentFlow = MutableStateFlow(NetworkEnvironment.STAGING)

        whenever(settingsRepository.isMockModeEnabled).thenReturn(mockModeFlow)
        whenever(settingsRepository.networkEnvironment).thenReturn(networkEnvironmentFlow)
        runBlocking {
            whenever(campusCredentialRepository.getCampusCredentialStatus()).thenReturn(Result.success(defaultStatus))
            whenever(campusCredentialRepository.revokeCampusCredentialConsent()).thenReturn(
                Result.success(defaultStatus.copy(hasActiveConsent = false, hasSavedCredential = false, quickAuthEnabled = false))
            )
            whenever(campusCredentialRepository.deleteCampusCredential()).thenReturn(
                Result.success(defaultStatus.copy(hasActiveConsent = false, hasSavedCredential = false, quickAuthEnabled = false))
            )
            whenever(campusCredentialRepository.setQuickAuthEnabled(true)).thenReturn(
                Result.failure(IllegalStateException("Missing active consent"))
            )
            whenever(campusCredentialRepository.setQuickAuthEnabled(false)).thenReturn(
                Result.success(defaultStatus.copy(quickAuthEnabled = false))
            )
        }
        whenever(context.getString(R.string.profile_settings_toggle_failed)).thenReturn("切换失败")
        whenever(context.getString(R.string.profile_settings_campus_credentials_revoke_success)).thenReturn("授权已撤回")
        whenever(context.getString(R.string.profile_settings_campus_credentials_delete_success)).thenReturn("凭证已删除")
        whenever(context.getString(R.string.profile_settings_campus_credentials_quick_auth_enabled_success)).thenReturn("快速认证已开启")
        whenever(context.getString(R.string.profile_settings_campus_credentials_quick_auth_disabled_success)).thenReturn("快速认证已关闭")
        whenever(context.getString(R.string.profile_settings_campus_credentials_action_failed)).thenReturn("操作失败")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun stateTracksFlowsAndLoadsCampusCredentialStatus() = runTest(testDispatcher) {
        val viewModel = ProfileSettingsViewModel(context, settingsRepository, campusCredentialRepository)
        advanceUntilIdle()

        assertEquals(false, viewModel.state.value.isMockModeEnabled)
        assertEquals(NetworkEnvironment.STAGING, viewModel.state.value.networkEnvironment)
        assertEquals(NetworkEnvironment.STAGING.baseUrl, viewModel.state.value.environmentBaseUrl)
        assertEquals(defaultStatus, viewModel.state.value.campusCredentialStatus)
        assertEquals(false, viewModel.state.value.isCampusCredentialLoading)

        mockModeFlow.value = true
        networkEnvironmentFlow.value = NetworkEnvironment.DEV
        advanceUntilIdle()

        assertEquals(true, viewModel.state.value.isMockModeEnabled)
        assertEquals(NetworkEnvironment.DEV, viewModel.state.value.networkEnvironment)
        assertEquals(NetworkEnvironment.DEV.baseUrl, viewModel.state.value.environmentBaseUrl)
    }

    @Test
    fun refreshesCampusCredentialStatusWhenBackendTargetFlowsChange() = runTest(testDispatcher) {
        ProfileSettingsViewModel(context, settingsRepository, campusCredentialRepository)
        advanceUntilIdle()

        mockModeFlow.value = true
        advanceUntilIdle()
        networkEnvironmentFlow.value = NetworkEnvironment.DEV
        advanceUntilIdle()

        verify(campusCredentialRepository, times(3)).getCampusCredentialStatus()
    }

    @Test
    fun setNetworkEnvironmentDelegatesToRepository() = runTest(testDispatcher) {
        val viewModel = ProfileSettingsViewModel(context, settingsRepository, campusCredentialRepository)

        viewModel.setNetworkEnvironment(NetworkEnvironment.PROD)
        advanceUntilIdle()

        verify(settingsRepository).setNetworkEnvironment(NetworkEnvironment.PROD)
    }

    @Test
    fun backendTargetChangeBlocksCampusCredentialActionsUntilPersisted() = runTest(testDispatcher) {
        val viewModel = ProfileSettingsViewModel(context, settingsRepository, campusCredentialRepository)
        advanceUntilIdle()

        viewModel.setMockModeEnabled(true)
        viewModel.revokeCampusCredentialConsent()
        advanceUntilIdle()

        verify(campusCredentialRepository, never()).revokeCampusCredentialConsent()
    }

    @Test
    fun campusCredentialActionBlocksBackendTargetChangesUntilFinished() = runTest(testDispatcher) {
        val viewModel = ProfileSettingsViewModel(context, settingsRepository, campusCredentialRepository)
        advanceUntilIdle()

        viewModel.revokeCampusCredentialConsent()
        viewModel.setMockModeEnabled(true)
        viewModel.setNetworkEnvironment(NetworkEnvironment.PROD)
        advanceUntilIdle()

        verify(settingsRepository, never()).setMockModeEnabled(true)
        verify(settingsRepository, never()).setNetworkEnvironment(NetworkEnvironment.PROD)
    }

    @Test
    fun setNetworkEnvironmentEmitsReadableFailureMessage() = runTest(testDispatcher) {
        doThrow(IllegalStateException("保存失败")).whenever(settingsRepository).setNetworkEnvironment(NetworkEnvironment.DEV)
        val viewModel = ProfileSettingsViewModel(context, settingsRepository, campusCredentialRepository)
        val eventDeferred = async { viewModel.events.first() }

        viewModel.setNetworkEnvironment(NetworkEnvironment.DEV)
        advanceUntilIdle()

        assertEquals(ProfileSettingsEvent.ShowMessage("保存失败"), eventDeferred.await())
    }

    @Test
    fun revokeConsentUpdatesStatusAndShowsMessage() = runTest(testDispatcher) {
        val viewModel = ProfileSettingsViewModel(context, settingsRepository, campusCredentialRepository)
        advanceUntilIdle()
        val eventDeferred = async { viewModel.events.first() }

        viewModel.revokeCampusCredentialConsent()
        advanceUntilIdle()

        assertEquals(false, viewModel.state.value.campusCredentialStatus.hasActiveConsent)
        assertEquals(false, viewModel.state.value.campusCredentialStatus.quickAuthEnabled)
        assertEquals(ProfileSettingsEvent.ShowMessage("授权已撤回"), eventDeferred.await())
    }

    @Test
    fun deleteCredentialUpdatesStatusAndShowsMessage() = runTest(testDispatcher) {
        val viewModel = ProfileSettingsViewModel(context, settingsRepository, campusCredentialRepository)
        advanceUntilIdle()
        val eventDeferred = async { viewModel.events.first() }

        viewModel.deleteCampusCredential()
        advanceUntilIdle()

        assertEquals(false, viewModel.state.value.campusCredentialStatus.hasSavedCredential)
        assertEquals(ProfileSettingsEvent.ShowMessage("凭证已删除"), eventDeferred.await())
    }


    @Test
    fun quickAuthDisableSuccessUpdatesStatusAndShowsMessage() = runTest(testDispatcher) {
        val viewModel = ProfileSettingsViewModel(context, settingsRepository, campusCredentialRepository)
        advanceUntilIdle()
        val eventDeferred = async { viewModel.events.first() }

        viewModel.setQuickAuthEnabled(false)
        advanceUntilIdle()

        assertEquals(false, viewModel.state.value.campusCredentialStatus.quickAuthEnabled)
        assertEquals(ProfileSettingsEvent.ShowMessage("快速认证已关闭"), eventDeferred.await())
    }

    @Test
    fun quickAuthFailureUsesBackendMessage() = runTest(testDispatcher) {
        val viewModel = ProfileSettingsViewModel(context, settingsRepository, campusCredentialRepository)
        advanceUntilIdle()
        val eventDeferred = async { viewModel.events.first() }

        viewModel.setQuickAuthEnabled(true)
        advanceUntilIdle()

        assertEquals(ProfileSettingsEvent.ShowMessage("Missing active consent"), eventDeferred.await())
        assertTrue(!viewModel.state.value.isCampusCredentialActionRunning)
    }
}
