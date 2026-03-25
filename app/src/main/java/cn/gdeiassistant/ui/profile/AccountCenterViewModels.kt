package cn.gdeiassistant.ui.profile

import android.content.Context
import cn.gdeiassistant.BuildConfig
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.R
import cn.gdeiassistant.data.ProfileRepository
import cn.gdeiassistant.data.SessionManager
import cn.gdeiassistant.data.mapper.PhoneAttributionCatalog
import cn.gdeiassistant.data.mapper.ProfileDisplayMapper
import cn.gdeiassistant.data.SettingsRepository
import cn.gdeiassistant.model.ContactBindingStatus
import cn.gdeiassistant.model.FeedbackSubmission
import cn.gdeiassistant.model.LoginRecordItem
import cn.gdeiassistant.model.PhoneAttribution
import cn.gdeiassistant.model.PrivacySettings
import cn.gdeiassistant.model.UserDataExportState
import cn.gdeiassistant.network.NetworkEnvironment
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

data class DownloadDataUiState(
    val exportState: UserDataExportState = UserDataExportState.NOT_EXPORTED,
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val error: String? = null
)

sealed interface DownloadDataEvent {
    data class ShowMessage(val message: String) : DownloadDataEvent
    data class OpenDownload(val url: String) : DownloadDataEvent
}

@HiltViewModel
class DownloadDataViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DownloadDataUiState())
    val state: StateFlow<DownloadDataUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<DownloadDataEvent>()
    val events: SharedFlow<DownloadDataEvent> = _events.asSharedFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            profileRepository.getUserDataExportState()
                .onSuccess { exportState ->
                    _state.update {
                        it.copy(
                            exportState = exportState,
                            isLoading = false,
                            isSubmitting = false,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isSubmitting = false,
                            error = error.message
                        )
                    }
                }
        }
    }

    fun startExport() {
        if (_state.value.isSubmitting) return
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null) }
            profileRepository.requestUserDataExport()
                .onSuccess {
                    _state.update {
                        it.copy(
                            exportState = UserDataExportState.EXPORTING,
                            isSubmitting = false
                        )
                    }
                    _events.emit(DownloadDataEvent.ShowMessage(context.getString(R.string.profile_export_started)))
                }
                .onFailure { error ->
                    _state.update { it.copy(isSubmitting = false, error = error.message) }
                    _events.emit(
                        DownloadDataEvent.ShowMessage(
                            error.message ?: context.getString(R.string.profile_export_submit_failed)
                        )
                    )
                }
        }
    }

    fun download() {
        viewModelScope.launch {
            profileRepository.getUserDataDownloadUrl()
                .onSuccess { url ->
                    _state.update {
                        it.copy(
                            exportState = UserDataExportState.EXPORTED,
                            error = null
                        )
                    }
                    _events.emit(DownloadDataEvent.OpenDownload(url))
                }
                .onFailure { error ->
                    _events.emit(
                        DownloadDataEvent.ShowMessage(
                            error.message ?: context.getString(R.string.profile_export_download_failed)
                        )
                    )
                }
        }
    }

}

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

data class LoginRecordsUiState(
    val items: List<LoginRecordItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class LoginRecordsViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val displayMapper: ProfileDisplayMapper
) : ViewModel() {

    private val _state = MutableStateFlow(LoginRecordsUiState())
    val state: StateFlow<LoginRecordsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            profileRepository.getLoginRecords()
                .onSuccess { records ->
                    _state.update { it.copy(items = displayMapper.applyLoginRecordDefaults(records), isLoading = false, error = null) }
                }
                .onFailure { error ->
                    _state.update { it.copy(items = emptyList(), isLoading = false, error = error.message) }
                }
        }
    }
}

data class BindPhoneUiState(
    val status: ContactBindingStatus = ContactBindingStatus(),
    val attributions: List<PhoneAttribution> = emptyList(),
    val selectedAttributionCode: Int = 86,
    val isLoading: Boolean = true,
    val isSendingCode: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null
)

sealed interface BindPhoneEvent {
    data class ShowMessage(val message: String) : BindPhoneEvent
}

@HiltViewModel
class BindPhoneViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val profileRepository: ProfileRepository,
    private val displayMapper: ProfileDisplayMapper
) : ViewModel() {

    private val _state = MutableStateFlow(BindPhoneUiState())
    val state: StateFlow<BindPhoneUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<BindPhoneEvent>()
    val events: SharedFlow<BindPhoneEvent> = _events.asSharedFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val bundledAttributions = PhoneAttributionCatalog.load(context)
            val attributionsResult = profileRepository.getPhoneAttributions()
            val statusResult = profileRepository.getPhoneStatus()
            val attributions = displayMapper.applyPhoneAttributionDefaults(
                PhoneAttributionCatalog.mergeAndSort(
                    primary = bundledAttributions,
                    overlay = attributionsResult.getOrDefault(emptyList())
                )
            )
            val rawStatus = statusResult.getOrDefault(ContactBindingStatus())
            val status = displayMapper.applyBindingNote(rawStatus, ProfileDisplayMapper.BindingType.PHONE)
            val selectedCode = statusResult.getOrNull()?.countryCode
                ?: attributions.firstOrNull { it.code == 86 }?.code
                ?: attributions.firstOrNull()?.code
                ?: 86
            _state.update {
                it.copy(
                    status = status,
                    attributions = attributions,
                    selectedAttributionCode = selectedCode,
                    isLoading = false,
                    error = attributionsResult.exceptionOrNull()?.message ?: statusResult.exceptionOrNull()?.message
                )
            }
        }
    }

    fun selectAttribution(code: Int) {
        _state.update { it.copy(selectedAttributionCode = code) }
    }

    fun sendVerification(phone: String) {
        val normalized = phone.filter(Char::isDigit)
        if (normalized.isBlank()) {
            emitPhoneMessage(context.getString(R.string.profile_phone_required))
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSendingCode = true) }
            profileRepository.sendPhoneVerification(_state.value.selectedAttributionCode, normalized)
                .onSuccess {
                    _state.update { it.copy(isSendingCode = false) }
                    _events.emit(BindPhoneEvent.ShowMessage(context.getString(R.string.profile_phone_code_sent)))
                }
                .onFailure { error ->
                    _state.update { it.copy(isSendingCode = false) }
                    _events.emit(
                        BindPhoneEvent.ShowMessage(
                            error.message ?: context.getString(R.string.profile_phone_code_failed)
                        )
                    )
                }
        }
    }

    fun bind(phone: String, randomCode: String) {
        val normalizedPhone = phone.filter(Char::isDigit)
        val normalizedCode = randomCode.filter(Char::isDigit)
        if (normalizedPhone.isBlank()) {
            emitPhoneMessage(context.getString(R.string.profile_phone_required))
            return
        }
        if (normalizedCode.isBlank()) {
            emitPhoneMessage(context.getString(R.string.profile_verification_required))
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }
            profileRepository.bindPhone(
                areaCode = _state.value.selectedAttributionCode,
                phone = normalizedPhone,
                randomCode = normalizedCode
            ).onSuccess { status ->
                _state.update { it.copy(status = displayMapper.applyBindingNote(status, ProfileDisplayMapper.BindingType.PHONE), isSubmitting = false, error = null) }
                _events.emit(BindPhoneEvent.ShowMessage(context.getString(R.string.profile_phone_bind_success)))
            }.onFailure { error ->
                _state.update { it.copy(isSubmitting = false, error = error.message) }
                _events.emit(
                    BindPhoneEvent.ShowMessage(
                        error.message ?: context.getString(R.string.profile_phone_bind_failed)
                    )
                )
            }
        }
    }

    fun unbind() {
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }
            profileRepository.unbindPhone()
                .onSuccess { status ->
                    _state.update { it.copy(status = displayMapper.applyBindingNote(status, ProfileDisplayMapper.BindingType.PHONE), isSubmitting = false, error = null) }
                    _events.emit(BindPhoneEvent.ShowMessage(context.getString(R.string.profile_phone_unbind_success)))
                }
                .onFailure { error ->
                    _state.update { it.copy(isSubmitting = false, error = error.message) }
                    _events.emit(
                        BindPhoneEvent.ShowMessage(
                            error.message ?: context.getString(R.string.profile_phone_unbind_failed)
                        )
                    )
                }
        }
    }

    private fun emitPhoneMessage(message: String) {
        viewModelScope.launch {
            _events.emit(BindPhoneEvent.ShowMessage(message))
        }
    }
}

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

data class FeedbackUiState(
    val isSubmitting: Boolean = false,
    val error: String? = null
)

sealed interface FeedbackEvent {
    data class ShowMessage(val message: String) : FeedbackEvent
    data object Submitted : FeedbackEvent
}

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FeedbackUiState())
    val state: StateFlow<FeedbackUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<FeedbackEvent>()
    val events: SharedFlow<FeedbackEvent> = _events.asSharedFlow()

    fun submit(content: String, contact: String, type: String) {
        val normalizedContent = content.trim()
        if (normalizedContent.isBlank()) {
            viewModelScope.launch {
                _events.emit(FeedbackEvent.ShowMessage(context.getString(R.string.profile_feedback_required)))
            }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null) }
            profileRepository.submitFeedback(
                FeedbackSubmission(
                    content = normalizedContent,
                    contact = contact.trim().takeIf(String::isNotBlank),
                    type = type.trim().takeIf(String::isNotBlank)
                )
            ).onSuccess {
                _state.update { it.copy(isSubmitting = false, error = null) }
                _events.emit(FeedbackEvent.ShowMessage(context.getString(R.string.profile_feedback_success)))
                _events.emit(FeedbackEvent.Submitted)
            }.onFailure { error ->
                _state.update { it.copy(isSubmitting = false, error = error.message) }
                _events.emit(
                    FeedbackEvent.ShowMessage(
                        error.message ?: context.getString(R.string.profile_feedback_failed)
                    )
                )
            }
        }
    }
}

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

data class ProfileSettingsUiState(
    val isMockModeEnabled: Boolean = true,
    val networkEnvironment: NetworkEnvironment = NetworkEnvironment.default(),
    val environmentBaseUrl: String = NetworkEnvironment.default().baseUrl,
    val canChangeNetworkEnvironment: Boolean = BuildConfig.DEBUG,
    val appVersion: String = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
)

sealed interface ProfileSettingsEvent {
    data class ShowMessage(val message: String) : ProfileSettingsEvent
}

@HiltViewModel
class ProfileSettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileSettingsUiState())
    val state: StateFlow<ProfileSettingsUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ProfileSettingsEvent>()
    val events: SharedFlow<ProfileSettingsEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            settingsRepository.isMockModeEnabled.collectLatest { enabled ->
                _state.update { it.copy(isMockModeEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            settingsRepository.networkEnvironment.collectLatest { environment ->
                _state.update {
                    it.copy(
                        networkEnvironment = environment,
                        environmentBaseUrl = environment.baseUrl
                    )
                }
            }
        }
    }

    fun setMockModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            runCatching { settingsRepository.setMockModeEnabled(enabled) }
                .onFailure { error ->
                    _events.emit(
                        ProfileSettingsEvent.ShowMessage(
                            error.message ?: context.getString(R.string.profile_settings_toggle_failed)
                        )
                    )
                }
        }
    }

    fun setNetworkEnvironment(environment: NetworkEnvironment) {
        viewModelScope.launch {
            runCatching { settingsRepository.setNetworkEnvironment(environment) }
                .onFailure { error ->
                    _events.emit(
                        ProfileSettingsEvent.ShowMessage(
                            error.message ?: context.getString(R.string.profile_settings_toggle_failed)
                        )
                    )
                }
        }
    }
}
