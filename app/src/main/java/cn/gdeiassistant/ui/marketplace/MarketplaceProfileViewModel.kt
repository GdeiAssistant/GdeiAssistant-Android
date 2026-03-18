package cn.gdeiassistant.ui.marketplace

import android.content.Context
import cn.gdeiassistant.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.data.MarketplaceRepository
import cn.gdeiassistant.model.MarketplaceItem
import cn.gdeiassistant.model.MarketplaceItemState
import cn.gdeiassistant.model.MarketplacePersonalSummary
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

enum class MarketplaceProfileTab {
    DOING, SOLD, OFF
}

data class MarketplaceProfileUiState(
    val summary: MarketplacePersonalSummary? = null,
    val selectedTab: MarketplaceProfileTab = MarketplaceProfileTab.DOING,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val visibleItems: List<MarketplaceItem>
        get() = when (selectedTab) {
            MarketplaceProfileTab.DOING -> summary?.doing.orEmpty()
            MarketplaceProfileTab.SOLD -> summary?.sold.orEmpty()
            MarketplaceProfileTab.OFF -> summary?.off.orEmpty()
        }
}

sealed interface MarketplaceProfileEvent {
    data class ShowMessage(val message: String) : MarketplaceProfileEvent
}

@HiltViewModel
class MarketplaceProfileViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val marketplaceRepository: MarketplaceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MarketplaceProfileUiState())
    val state: StateFlow<MarketplaceProfileUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<MarketplaceProfileEvent>()
    val events: SharedFlow<MarketplaceProfileEvent> = _events.asSharedFlow()

    init {
        refresh()
    }

    fun selectTab(tab: MarketplaceProfileTab) {
        _state.update { it.copy(selectedTab = tab) }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            marketplaceRepository.getProfileSummary()
                .onSuccess { summary ->
                    _state.update { it.copy(summary = summary, isLoading = false, error = null) }
                }
                .onFailure { error ->
                    _state.update { it.copy(summary = null, isLoading = false, error = error.message) }
                }
        }
    }

    fun updateItemState(itemId: String, targetState: MarketplaceItemState) {
        viewModelScope.launch {
            marketplaceRepository.updateItemState(itemId, targetState)
                .onSuccess {
                    _events.emit(MarketplaceProfileEvent.ShowMessage(context.getString(R.string.marketplace_state_updated)))
                    refresh()
                }
                .onFailure { error ->
                    _events.emit(
                        MarketplaceProfileEvent.ShowMessage(
                            error.message ?: context.getString(R.string.marketplace_action_failed)
                        )
                    )
                }
        }
    }
}
