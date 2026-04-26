package cn.gdeiassistant.ui.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.R
import cn.gdeiassistant.data.AuthRepository
import cn.gdeiassistant.data.SettingsRepository
import cn.gdeiassistant.model.CampusCredentialConsentMetadata
import cn.gdeiassistant.ui.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>()
    val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            settingsRepository.isMockModeEnabled.collectLatest { enabled ->
                _uiState.update { current ->
                    current.copy(
                        isMockModeEnabled = enabled,
                        errorMessage = null
                    )
                }
            }
        }
    }

    fun updateUsername(value: String) {
        _uiState.update { it.copy(username = value, errorMessage = null) }
    }

    fun updatePassword(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun setCampusCredentialConsentChecked(checked: Boolean) {
        _uiState.update {
            it.copy(
                isCampusCredentialConsentChecked = checked,
                errorMessage = null
            )
        }
    }

    fun setMockModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            runCatching { settingsRepository.setMockModeEnabled(enabled) }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            errorMessage = UiText.DynamicString(
                                error.message ?: context.getString(R.string.service_unavailable)
                            )
                        )
                    }
                }
        }
    }

    fun login() {
        val state = _uiState.value
        if (state.isLoading) return
        if (state.username.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = UiText.StringResource(R.string.login_error_empty)) }
            return
        }
        if (state.requiresCampusCredentialConsent && !state.isCampusCredentialConsentChecked) {
            _uiState.update {
                it.copy(errorMessage = UiText.StringResource(R.string.login_campus_credential_consent_required))
            }
            return
        }

        val consentMetadata = if (state.requiresCampusCredentialConsent) {
            CampusCredentialConsentMetadata()
        } else {
            null
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.login(
                username = state.username.trim(),
                password = state.password,
                consentMetadata = consentMetadata
            ).onSuccess {
                _uiState.update { it.copy(isLoading = false) }
                _events.emit(LoginEvent.NavigateToHome)
            }.onFailure { error ->
                val message = error.message
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = if (message.isNullOrBlank()) {
                            UiText.StringResource(R.string.service_unavailable)
                        } else {
                            UiText.DynamicString(message)
                        }
                    )
                }
            }
        }
    }
}
