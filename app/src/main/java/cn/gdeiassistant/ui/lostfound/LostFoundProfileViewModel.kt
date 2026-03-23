package cn.gdeiassistant.ui.lostfound

import android.content.Context
import cn.gdeiassistant.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.data.LostFoundRepository
import cn.gdeiassistant.data.mapper.LostFoundDisplayMapper
import cn.gdeiassistant.model.LostFoundItem
import cn.gdeiassistant.model.LostFoundPersonalSummary
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LostFoundProfileTab {
    LOST, FOUND, DID_FOUND
}

data class LostFoundProfileUiState(
    val summary: LostFoundPersonalSummary? = null,
    val selectedTab: LostFoundProfileTab = LostFoundProfileTab.LOST,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val visibleItems: List<LostFoundItem>
        get() = when (selectedTab) {
            LostFoundProfileTab.LOST -> summary?.lost.orEmpty()
            LostFoundProfileTab.FOUND -> summary?.found.orEmpty()
            LostFoundProfileTab.DID_FOUND -> summary?.didFound.orEmpty()
        }
}

sealed interface LostFoundProfileEvent {
    data class ShowMessage(val message: String) : LostFoundProfileEvent
}

@HiltViewModel
class LostFoundProfileViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lostFoundRepository: LostFoundRepository,
    private val displayMapper: LostFoundDisplayMapper
) : ViewModel() {

    private val _state = MutableStateFlow(LostFoundProfileUiState())
    val state: StateFlow<LostFoundProfileUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<LostFoundProfileEvent>()
    val events: SharedFlow<LostFoundProfileEvent> = _events.asSharedFlow()

    init {
        refresh()
    }

    fun selectTab(tab: LostFoundProfileTab) {
        _state.update { it.copy(selectedTab = tab) }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            lostFoundRepository.getProfileSummary()
                .map { displayMapper.applyPersonalSummaryDefaults(it) }
                .onSuccess { summary ->
                    _state.update { it.copy(summary = summary, isLoading = false, error = null) }
                }
                .onFailure { error ->
                    _state.update { it.copy(summary = null, isLoading = false, error = error.message) }
                }
        }
    }

    fun markDidFound(itemId: String) {
        viewModelScope.launch {
            lostFoundRepository.markDidFound(itemId)
                .onSuccess {
                    _events.emit(LostFoundProfileEvent.ShowMessage(context.getString(R.string.lost_found_mark_found_success)))
                    refresh()
                }
                .onFailure { error ->
                    _events.emit(
                        LostFoundProfileEvent.ShowMessage(
                            error.message ?: context.getString(R.string.lost_found_action_failed)
                        )
                    )
                }
        }
    }
}
