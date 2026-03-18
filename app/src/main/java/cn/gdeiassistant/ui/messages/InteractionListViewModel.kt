package cn.gdeiassistant.ui.messages

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.R
import cn.gdeiassistant.data.MessagesRepository
import cn.gdeiassistant.model.InteractionMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InteractionListUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val items: List<InteractionMessage> = emptyList(),
    val error: String? = null,
    val unreadCount: Int = 0
)

sealed interface InteractionListEvent {
    data class ShowMessage(val message: String) : InteractionListEvent
}

@HiltViewModel
class InteractionListViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val messagesRepository: MessagesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(InteractionListUiState(isLoading = true))
    val state: StateFlow<InteractionListUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<InteractionListEvent>()
    val events: SharedFlow<InteractionListEvent> = _events.asSharedFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            fetchData()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, error = null) }
            fetchData()
        }
    }

    private suspend fun fetchData() {
        val (itemsResult, unreadResult) = coroutineScope {
            val items = async { messagesRepository.getInteractionMessages(size = 50) }
            val unread = async { messagesRepository.getUnreadCount() }
            items.await() to unread.await()
        }
        _state.update {
            it.copy(
                isLoading = false,
                isRefreshing = false,
                items = itemsResult.getOrNull().orEmpty(),
                error = itemsResult.exceptionOrNull()?.message,
                unreadCount = unreadResult.getOrNull()
                    ?: itemsResult.getOrNull()?.count { m -> !m.isRead }
                    ?: 0
            )
        }
    }

    fun markMessageRead(id: String) {
        val target = _state.value.items.firstOrNull { it.id == id } ?: return
        if (target.isRead) return
        viewModelScope.launch {
            messagesRepository.markMessageRead(id)
                .onSuccess {
                    _state.update { state ->
                        state.copy(
                            items = state.items.map { item ->
                                if (item.id == id) item.copy(isRead = true) else item
                            },
                            unreadCount = (state.unreadCount - 1).coerceAtLeast(0)
                        )
                    }
                }
                .onFailure { error ->
                    _events.emit(
                        InteractionListEvent.ShowMessage(
                            error.message ?: context.getString(R.string.messages_mark_read_failed)
                        )
                    )
                }
        }
    }

    fun markAllRead() {
        if (_state.value.unreadCount == 0) return
        viewModelScope.launch {
            messagesRepository.markAllRead()
                .onSuccess {
                    _state.update { state ->
                        state.copy(
                            items = state.items.map { it.copy(isRead = true) },
                            unreadCount = 0
                        )
                    }
                }
                .onFailure { error ->
                    _events.emit(
                        InteractionListEvent.ShowMessage(
                            error.message ?: context.getString(R.string.messages_action_failed)
                        )
                    )
                }
        }
    }
}
