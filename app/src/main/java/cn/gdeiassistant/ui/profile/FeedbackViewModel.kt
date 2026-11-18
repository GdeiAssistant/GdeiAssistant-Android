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
