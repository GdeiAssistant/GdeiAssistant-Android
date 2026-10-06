package cn.gdeiassistant.ui.social

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.R
import cn.gdeiassistant.data.SocialRepository
import cn.gdeiassistant.model.DmPolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SocialDmPrivacyUiState(
    val dmPolicy: DmPolicy = DmPolicy.MUTUAL,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SocialDmPrivacyViewModel @Inject constructor(
    private val socialRepository: SocialRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(SocialDmPrivacyUiState())
    val state: StateFlow<SocialDmPrivacyUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<SocialUiEvent>()
    val events: SharedFlow<SocialUiEvent> = _events.asSharedFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            socialRepository.getPrivacy()
                .onSuccess { privacy ->
                    _state.update { it.copy(isLoading = false, dmPolicy = privacy.dmPolicy) }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(isLoading = false, error = error.message ?: context.getString(R.string.load_failed))
                    }
                }
        }
    }

    fun select(policy: DmPolicy) {
        _state.update { it.copy(dmPolicy = policy) }
    }

    fun save() {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            socialRepository.updatePrivacy(_state.value.dmPolicy)
                .onSuccess { privacy ->
                    _state.update { it.copy(isSaving = false, dmPolicy = privacy.dmPolicy) }
                    _events.emit(SocialUiEvent.ShowMessage(context.getString(R.string.social_dm_privacy_saved)))
                }
                .onFailure { error ->
                    _state.update { it.copy(isSaving = false) }
                    _events.emit(
                        SocialUiEvent.ShowMessage(
                            error.message ?: context.getString(R.string.profile_privacy_save_failed)
                        )
                    )
                }
        }
    }
}
