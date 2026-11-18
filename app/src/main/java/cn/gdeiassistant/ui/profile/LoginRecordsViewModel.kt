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
