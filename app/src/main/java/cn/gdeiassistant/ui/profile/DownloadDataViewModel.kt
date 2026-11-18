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
