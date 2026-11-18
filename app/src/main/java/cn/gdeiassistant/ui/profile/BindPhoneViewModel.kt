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
