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

data class DeleteAccountUiState(
    val isSubmitting: Boolean = false,
    val error: String? = null
)

sealed interface DeleteAccountEvent {
    data class ShowMessage(val message: String) : DeleteAccountEvent
    data object Deleted : DeleteAccountEvent
}

@HiltViewModel
class DeleteAccountViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val profileRepository: ProfileRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(DeleteAccountUiState())
    val state: StateFlow<DeleteAccountUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<DeleteAccountEvent>()
    val events: SharedFlow<DeleteAccountEvent> = _events.asSharedFlow()

    fun submit(password: String) {
        if (password.isBlank()) {
            viewModelScope.launch {
                _events.emit(DeleteAccountEvent.ShowMessage(context.getString(R.string.profile_delete_password_required)))
            }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null) }
            profileRepository.deleteAccount(password)
                .onSuccess {
                    sessionManager.clearTokens()
                    _state.update { it.copy(isSubmitting = false, error = null) }
                    _events.emit(DeleteAccountEvent.ShowMessage(context.getString(R.string.profile_delete_success)))
                    _events.emit(DeleteAccountEvent.Deleted)
                }
                .onFailure { error ->
                    _state.update { it.copy(isSubmitting = false, error = error.message) }
                    _events.emit(
                        DeleteAccountEvent.ShowMessage(
                            error.message ?: context.getString(R.string.profile_delete_failed)
                        )
                    )
                }
        }
    }
}
