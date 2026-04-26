package cn.gdeiassistant.ui.login

import android.content.Context
import cn.gdeiassistant.R
import cn.gdeiassistant.data.AuthRepository
import cn.gdeiassistant.data.SettingsRepository
import cn.gdeiassistant.model.CampusCredentialConsentMetadata
import cn.gdeiassistant.ui.util.UiText
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
import java.lang.reflect.Field
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepository: AuthRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var context: Context
    private lateinit var mockModeFlow: MutableStateFlow<Boolean>

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mock()
        settingsRepository = mock()
        context = mock()
        mockModeFlow = MutableStateFlow(false)
        setSyncMockModeEnabled(false)

        whenever(settingsRepository.isMockModeEnabled).thenReturn(mockModeFlow)
        whenever(context.getString(R.string.login_campus_credential_consent_required)).thenReturn("请先完成校园凭证授权")
        whenever(context.getString(R.string.login_error_empty)).thenReturn("请输入账号和密码")
        whenever(context.getString(R.string.service_unavailable)).thenReturn("服务暂不可用")
    }

    @After
    fun tearDown() {
        setSyncMockModeEnabled(false)
        Dispatchers.resetMain()
    }

    @Test
    fun loginBlocksSubmitWhenConsentIsRequiredButUnchecked() = runTest(testDispatcher) {
        val viewModel = LoginViewModel(authRepository, settingsRepository, context)
        viewModel.updateUsername("student")
        viewModel.updatePassword("password123")

        viewModel.login()
        advanceUntilIdle()

        verifyNoInteractions(authRepository)
        assertEquals(
            UiText.StringResource(R.string.login_campus_credential_consent_required),
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun loginSendsConsentMetadataWhenChecked() = runTest(testDispatcher) {
        whenever(authRepository.login(anyOrNull(), anyOrNull(), anyOrNull())).thenReturn(Result.success(Unit))
        val viewModel = LoginViewModel(authRepository, settingsRepository, context)
        val eventDeferred = async { viewModel.events.first() }

        viewModel.updateUsername("student")
        viewModel.updatePassword("password123")
        viewModel.setCampusCredentialConsentChecked(true)
        viewModel.login()
        advanceUntilIdle()

        verify(authRepository).login(
            eq("student"),
            eq("password123"),
            eq(
                CampusCredentialConsentMetadata(
                    scene = "LOGIN",
                    policyDate = "2026-04-25",
                    effectiveDate = "2026-05-11"
                )
            )
        )
        assertEquals(LoginEvent.NavigateToHome, eventDeferred.await())
    }

    @Test
    fun mockModeBypassesCampusCredentialConsentGate() = runTest(testDispatcher) {
        setSyncMockModeEnabled(true)
        mockModeFlow.value = true
        whenever(authRepository.login(anyOrNull(), anyOrNull(), anyOrNull())).thenReturn(Result.success(Unit))
        val viewModel = LoginViewModel(authRepository, settingsRepository, context)

        viewModel.updateUsername("student")
        viewModel.updatePassword("password123")
        viewModel.login()
        advanceUntilIdle()

        verify(authRepository).login(eq("student"), eq("password123"), isNull())
    }

    private fun setSyncMockModeEnabled(value: Boolean) {
        val field: Field = SettingsRepository::class.java.getDeclaredField("mockModeEnabledCache")
        field.isAccessible = true
        field.setBoolean(null, value)
    }

}
