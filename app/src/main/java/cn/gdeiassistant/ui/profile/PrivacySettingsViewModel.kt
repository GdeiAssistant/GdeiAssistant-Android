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

data class PrivacySettingsUiState(
    val settings: PrivacySettings = PrivacySettings(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null
)

sealed interface PrivacySettingsEvent {
    data class ShowMessage(val message: String) : PrivacySettingsEvent
}

@HiltViewModel
class PrivacySettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PrivacySettingsUiState())
    val state: StateFlow<PrivacySettingsUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<PrivacySettingsEvent>()
    val events: SharedFlow<PrivacySettingsEvent> = _events.asSharedFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            profileRepository.getPrivacySettings()
                .onSuccess { settings ->
                    _state.update { it.copy(settings = settings, isLoading = false, error = null) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    fun updateSettings(transform: (PrivacySettings) -> PrivacySettings) {
        _state.update { current -> current.copy(settings = transform(current.settings)) }
    }

    fun save() {
        if (_state.value.isSaving) return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            profileRepository.updatePrivacySettings(_state.value.settings)
                .onSuccess { settings ->
                    _state.update { it.copy(settings = settings, isSaving = false, error = null) }
                    _events.emit(PrivacySettingsEvent.ShowMessage(context.getString(R.string.profile_privacy_saved)))
                }
                .onFailure { error ->
                    _state.update { it.copy(isSaving = false, error = error.message) }
                    _events.emit(
                        PrivacySettingsEvent.ShowMessage(
                            error.message ?: context.getString(R.string.profile_privacy_save_failed)
                        )
                    )
                }
        }
    }
}
