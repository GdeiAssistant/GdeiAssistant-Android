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

data class BindEmailUiState(
    val status: ContactBindingStatus = ContactBindingStatus(),
    val isLoading: Boolean = true,
    val isSendingCode: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null
)

sealed interface BindEmailEvent {
    data class ShowMessage(val message: String) : BindEmailEvent
}

@HiltViewModel
class BindEmailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val profileRepository: ProfileRepository,
    private val displayMapper: ProfileDisplayMapper
) : ViewModel() {

    private val _state = MutableStateFlow(BindEmailUiState())
    val state: StateFlow<BindEmailUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<BindEmailEvent>()
    val events: SharedFlow<BindEmailEvent> = _events.asSharedFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            profileRepository.getEmailStatus()
                .onSuccess { status ->
                    _state.update { it.copy(status = displayMapper.applyBindingNote(status, ProfileDisplayMapper.BindingType.EMAIL), isLoading = false, error = null) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    fun sendVerification(email: String) {
        val normalized = email.trim()
        if (normalized.isBlank()) {
            emitEmailMessage(context.getString(R.string.profile_email_required))
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSendingCode = true) }
            profileRepository.sendEmailVerification(normalized)
                .onSuccess {
                    _state.update { it.copy(isSendingCode = false) }
                    _events.emit(BindEmailEvent.ShowMessage(context.getString(R.string.profile_email_code_sent)))
                }
                .onFailure { error ->
                    _state.update { it.copy(isSendingCode = false) }
                    _events.emit(
                        BindEmailEvent.ShowMessage(
                            error.message ?: context.getString(R.string.profile_email_code_failed)
                        )
                    )
                }
        }
    }

    fun bind(email: String, randomCode: String) {
        val normalizedEmail = email.trim()
        val normalizedCode = randomCode.filter(Char::isDigit)
        if (normalizedEmail.isBlank()) {
            emitEmailMessage(context.getString(R.string.profile_email_required))
            return
        }
        if (normalizedCode.isBlank()) {
            emitEmailMessage(context.getString(R.string.profile_verification_required))
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }
            profileRepository.bindEmail(normalizedEmail, normalizedCode)
                .onSuccess { status ->
                    _state.update { it.copy(status = displayMapper.applyBindingNote(status, ProfileDisplayMapper.BindingType.EMAIL), isSubmitting = false, error = null) }
                    _events.emit(BindEmailEvent.ShowMessage(context.getString(R.string.profile_email_bind_success)))
                }
                .onFailure { error ->
                    _state.update { it.copy(isSubmitting = false, error = error.message) }
                    _events.emit(
                        BindEmailEvent.ShowMessage(
                            error.message ?: context.getString(R.string.profile_email_bind_failed)
                        )
                    )
                }
        }
    }

    fun unbind() {
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }
            profileRepository.unbindEmail()
                .onSuccess { status ->
                    _state.update { it.copy(status = displayMapper.applyBindingNote(status, ProfileDisplayMapper.BindingType.EMAIL), isSubmitting = false, error = null) }
                    _events.emit(BindEmailEvent.ShowMessage(context.getString(R.string.profile_email_unbind_success)))
                }
                .onFailure { error ->
                    _state.update { it.copy(isSubmitting = false, error = error.message) }
                    _events.emit(
                        BindEmailEvent.ShowMessage(
                            error.message ?: context.getString(R.string.profile_email_unbind_failed)
                        )
                    )
                }
        }
    }

    private fun emitEmailMessage(message: String) {
        viewModelScope.launch {
            _events.emit(BindEmailEvent.ShowMessage(message))
        }
    }
}
