package cn.gdeiassistant.ui.profile

import android.content.Context
import cn.gdeiassistant.BuildConfig
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.R
import cn.gdeiassistant.data.CampusCredentialRepository
import cn.gdeiassistant.data.ProfileRepository
import cn.gdeiassistant.data.SessionManager
import cn.gdeiassistant.data.mapper.PhoneAttributionCatalog
import cn.gdeiassistant.data.mapper.ProfileDisplayMapper
import cn.gdeiassistant.data.SettingsRepository
import cn.gdeiassistant.model.CampusCredentialStatus
import cn.gdeiassistant.model.ContactBindingStatus
import cn.gdeiassistant.model.FeedbackSubmission
import cn.gdeiassistant.model.LoginRecordItem
import cn.gdeiassistant.model.PhoneAttribution
import cn.gdeiassistant.model.PrivacySettings
import cn.gdeiassistant.model.UserDataExportState
import cn.gdeiassistant.network.NetworkEnvironment
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
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

data class ProfileSettingsUiState(
    val isMockModeEnabled: Boolean = false,
    val canUseDemoMode: Boolean = SettingsRepository.canUseDemoMode(),
    val networkEnvironment: NetworkEnvironment = NetworkEnvironment.default(),
    val environmentBaseUrl: String = NetworkEnvironment.default().baseUrl,
    val canChangeNetworkEnvironment: Boolean = SettingsRepository.canChangeNetworkEnvironment(),
    val appVersion: String = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
    val campusCredentialStatus: CampusCredentialStatus = CampusCredentialStatus(),
    val isCampusCredentialLoading: Boolean = true,
    val isCampusCredentialActionRunning: Boolean = false,
    val isBackendTargetChanging: Boolean = false,
    val campusCredentialError: String? = null
)

sealed interface ProfileSettingsEvent {
    data class ShowMessage(val message: String) : ProfileSettingsEvent
}

@HiltViewModel
class ProfileSettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val campusCredentialRepository: CampusCredentialRepository
) : ViewModel() {

    private val initialNetworkEnvironment = SettingsRepository.currentNetworkEnvironmentSync()
    private val _state = MutableStateFlow(
        ProfileSettingsUiState(
            isMockModeEnabled = SettingsRepository.isMockModeEnabledSync(),
            networkEnvironment = initialNetworkEnvironment,
            environmentBaseUrl = initialNetworkEnvironment.baseUrl
        )
    )
    val state: StateFlow<ProfileSettingsUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ProfileSettingsEvent>()
    val events: SharedFlow<ProfileSettingsEvent> = _events.asSharedFlow()
    private var campusCredentialRefreshJob: Job? = null
    private var campusCredentialRefreshSequence = 0

    init {
        viewModelScope.launch {
            settingsRepository.isMockModeEnabled.collectLatest { enabled ->
                val previous = _state.value.isMockModeEnabled
                val shouldRefresh = previous != enabled &&
                    !_state.value.isBackendTargetChanging
                _state.update { it.copy(isMockModeEnabled = enabled) }
                if (shouldRefresh) {
                    refreshCampusCredentialStatus()
                }
            }
        }
        viewModelScope.launch {
            settingsRepository.networkEnvironment.collectLatest { environment ->
                val previous = _state.value.networkEnvironment
                val shouldRefresh = previous != environment &&
                    !_state.value.isBackendTargetChanging
                _state.update {
                    it.copy(
                        networkEnvironment = environment,
                        environmentBaseUrl = environment.baseUrl
                    )
                }
                if (shouldRefresh) {
                    refreshCampusCredentialStatus()
                }
            }
        }
        refreshCampusCredentialStatus()
    }

    fun setMockModeEnabled(enabled: Boolean) {
        if (!SettingsRepository.canUseDemoMode() ||
            _state.value.isBackendTargetChanging ||
            _state.value.isCampusCredentialActionRunning
        ) return
        _state.update {
            it.copy(
                isMockModeEnabled = enabled,
                isBackendTargetChanging = true,
                campusCredentialError = null
            )
        }
        viewModelScope.launch {
            runCatching { settingsRepository.setMockModeEnabled(enabled) }
                .onSuccess {
                    refreshCampusCredentialStatus()
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isMockModeEnabled = SettingsRepository.isMockModeEnabledSync(),
                            isBackendTargetChanging = false
                        )
                    }
                    _events.emit(
                        ProfileSettingsEvent.ShowMessage(
                            error.message ?: context.getString(R.string.profile_settings_toggle_failed)
                        )
                    )
                }
        }
    }

    fun setNetworkEnvironment(environment: NetworkEnvironment) {
        if (!SettingsRepository.canChangeNetworkEnvironment() ||
            _state.value.isBackendTargetChanging ||
            _state.value.isCampusCredentialActionRunning
        ) return
        _state.update {
            it.copy(
                networkEnvironment = environment,
                environmentBaseUrl = environment.baseUrl,
                isBackendTargetChanging = true,
                campusCredentialError = null
            )
        }
        viewModelScope.launch {
            runCatching { settingsRepository.setNetworkEnvironment(environment) }
                .onSuccess {
                    refreshCampusCredentialStatus()
                }
                .onFailure { error ->
                    val currentEnvironment = SettingsRepository.currentNetworkEnvironmentSync()
                    _state.update {
                        it.copy(
                            networkEnvironment = currentEnvironment,
                            environmentBaseUrl = currentEnvironment.baseUrl,
                            isBackendTargetChanging = false
                        )
                    }
                    _events.emit(
                        ProfileSettingsEvent.ShowMessage(
                            error.message ?: context.getString(R.string.profile_settings_toggle_failed)
                        )
                    )
                }
        }
    }

    fun refreshCampusCredentialStatus() {
        val refreshSequence = ++campusCredentialRefreshSequence
        campusCredentialRefreshJob?.cancel()
        campusCredentialRefreshJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    isCampusCredentialLoading = true,
                    campusCredentialError = null
                )
            }
            campusCredentialRepository.getCampusCredentialStatus()
                .onSuccess { status ->
                    if (refreshSequence != campusCredentialRefreshSequence) return@launch
                    _state.update {
                        it.copy(
                            campusCredentialStatus = status,
                            isCampusCredentialLoading = false,
                            isBackendTargetChanging = false,
                            campusCredentialError = null
                        )
                    }
                }
                .onFailure { error ->
                    if (refreshSequence != campusCredentialRefreshSequence) return@launch
                    _state.update {
                        it.copy(
                            isCampusCredentialLoading = false,
                            isBackendTargetChanging = false,
                            campusCredentialError = error.message
                        )
                    }
                }
        }
    }

    fun revokeCampusCredentialConsent() {
        performCampusCredentialAction(
            action = { campusCredentialRepository.revokeCampusCredentialConsent() },
            successMessage = context.getString(R.string.profile_settings_campus_credentials_revoke_success)
        )
    }

    fun deleteCampusCredential() {
        performCampusCredentialAction(
            action = { campusCredentialRepository.deleteCampusCredential() },
            successMessage = context.getString(R.string.profile_settings_campus_credentials_delete_success)
        )
    }

    fun setQuickAuthEnabled(enabled: Boolean) {
        performCampusCredentialAction(
            action = { campusCredentialRepository.setQuickAuthEnabled(enabled) },
            successMessage = context.getString(
                if (enabled) {
                    R.string.profile_settings_campus_credentials_quick_auth_enabled_success
                } else {
                    R.string.profile_settings_campus_credentials_quick_auth_disabled_success
                }
            )
        )
    }

    private fun performCampusCredentialAction(
        action: suspend () -> Result<CampusCredentialStatus>,
        successMessage: String
    ) {
        if (_state.value.isCampusCredentialActionRunning || _state.value.isBackendTargetChanging) return
        _state.update {
            it.copy(
                isCampusCredentialActionRunning = true,
                campusCredentialError = null
            )
        }
        viewModelScope.launch {
            action()
                .onSuccess { status ->
                    _state.update {
                        it.copy(
                            campusCredentialStatus = status,
                            isCampusCredentialLoading = false,
                            isCampusCredentialActionRunning = false,
                            campusCredentialError = null
                        )
                    }
                    _events.emit(ProfileSettingsEvent.ShowMessage(successMessage))
                }
                .onFailure { error ->
                    _state.update { it.copy(isCampusCredentialActionRunning = false) }
                    _events.emit(
                        ProfileSettingsEvent.ShowMessage(
                            error.message ?: context.getString(R.string.profile_settings_campus_credentials_action_failed)
                        )
                    )
                }
        }
    }
}
