package cn.gdeiassistant.ui.social

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.R
import cn.gdeiassistant.data.SocialRepository
import cn.gdeiassistant.data.SocialSessionCoordinator
import cn.gdeiassistant.model.Conversation
import cn.gdeiassistant.model.SocialRealtimeEvent
import cn.gdeiassistant.network.SocialRealtimeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConversationListUiState(
    val items: List<Conversation> = emptyList(),
    val nextCursor: String? = null,
    val hasMore: Boolean = false,
    val unreadTotal: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ConversationListViewModel @Inject constructor(
    private val socialRepository: SocialRepository,
    private val socialSessionCoordinator: SocialSessionCoordinator,
    private val realtimeManager: SocialRealtimeManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(ConversationListUiState(isLoading = true))
    val state: StateFlow<ConversationListUiState> = _state.asStateFlow()

    private var pollJob: Job? = null

    init {
        refresh()
        viewModelScope.launch {
            socialSessionCoordinator.unreadTotal.collect { total ->
                _state.update { it.copy(unreadTotal = total) }
            }
        }
        viewModelScope.launch {
            realtimeManager.events.collect { event ->
                when (event) {
                    is SocialRealtimeEvent.MessageCreated,
                    is SocialRealtimeEvent.ConversationRead,
                    SocialRealtimeEvent.Ready,
                    SocialRealtimeEvent.SocialChanged -> refresh()
                }
            }
        }
        startPolling()
    }

    fun refresh() {
        load(true)
        socialSessionCoordinator.refreshUnread()
    }

    fun loadMore() {
        if (_state.value.isLoading || !_state.value.hasMore) return
        load(false)
    }

    fun onVisible(visible: Boolean) {
        if (visible) startPolling() else pollJob?.cancel()
    }

    private fun startPolling() {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (isActive) {
                delay(30_000)
                refresh()
            }
        }
    }

    private fun load(reset: Boolean) {
        viewModelScope.launch {
            _state.update {
                it.copy(isLoading = true, error = null, items = if (reset) emptyList() else it.items)
            }
            socialRepository.getConversations(cursor = if (reset) null else _state.value.nextCursor)
                .onSuccess { page ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            items = if (reset) page.items else it.items + page.items,
                            nextCursor = page.nextCursor,
                            hasMore = page.hasMore
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(isLoading = false, error = error.message ?: context.getString(R.string.load_failed))
                    }
                }
        }
    }
}
