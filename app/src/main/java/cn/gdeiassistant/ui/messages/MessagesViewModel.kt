package cn.gdeiassistant.ui.messages

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.R
import cn.gdeiassistant.data.AnnouncementRepository
import cn.gdeiassistant.data.MessagesRepository
import cn.gdeiassistant.data.NewsRepository
import cn.gdeiassistant.model.AnnouncementItem
import cn.gdeiassistant.model.Festival
import cn.gdeiassistant.model.InteractionMessage
import cn.gdeiassistant.model.SchoolNews
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MessagesUiState(
    val isLoading: Boolean = false,
    val newsItems: List<SchoolNews> = emptyList(),
    val newsError: String? = null,
    val announcementItems: List<AnnouncementItem> = emptyList(),
    val announcementError: String? = null,
    val interactionItems: List<InteractionMessage> = emptyList(),
    val interactionError: String? = null,
    val unreadCount: Int = 0,
    val festival: Festival? = null
)

sealed interface MessagesEvent {
    data class ShowMessage(val message: String) : MessagesEvent
}

@HiltViewModel
class MessagesViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val newsRepository: NewsRepository,
    private val announcementRepository: AnnouncementRepository,
    private val messagesRepository: MessagesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MessagesUiState(isLoading = true))
    val state: StateFlow<MessagesUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<MessagesEvent>()
    val events: SharedFlow<MessagesEvent> = _events.asSharedFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val newsDeferred = async { newsRepository.getLatestNews(size = 5) }
            val announcementDeferred = async { announcementRepository.getAnnouncements(limit = 5) }
            val interactionDeferred = async { messagesRepository.getInteractionMessages(size = 10) }
            val unreadDeferred = async { messagesRepository.getUnreadCount() }
            val festivalDeferred = async { announcementRepository.getFestival() }

            val newsResult = newsDeferred.await()
            val announcementResult = announcementDeferred.await()
            val interactionResult = interactionDeferred.await()
            val unreadResult = unreadDeferred.await()
            val festivalResult = festivalDeferred.await()

            _state.update {
                it.copy(
                    isLoading = false,
                    newsItems = newsResult.getOrNull().orEmpty(),
                    newsError = newsResult.exceptionOrNull()?.message,
                    announcementItems = announcementResult.getOrNull().orEmpty(),
                    announcementError = announcementResult.exceptionOrNull()?.message,
                    interactionItems = interactionResult.getOrNull().orEmpty(),
                    interactionError = interactionResult.exceptionOrNull()?.message,
                    unreadCount = unreadResult.getOrNull() ?: interactionResult.getOrNull()?.count { !it.isRead } ?: 0,
                    festival = festivalResult.getOrNull()
                )
            }
        }
    }

    fun markMessageRead(id: String) {
        val target = _state.value.interactionItems.firstOrNull { it.id == id } ?: return
        if (target.isRead) return
        viewModelScope.launch {
            messagesRepository.markMessageRead(id)
                .onSuccess {
                    _state.update { state ->
                        state.copy(
                            interactionItems = state.interactionItems.map { item ->
                                if (item.id == id) item.copy(isRead = true) else item
                            },
                            unreadCount = (state.unreadCount - 1).coerceAtLeast(0)
                        )
                    }
                }
                .onFailure { error ->
                    _events.emit(
                        MessagesEvent.ShowMessage(
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
                            interactionItems = state.interactionItems.map { it.copy(isRead = true) },
                            unreadCount = 0
                        )
                    }
                }
                .onFailure { error ->
                    _events.emit(
                        MessagesEvent.ShowMessage(
                            error.message ?: context.getString(R.string.messages_action_failed)
                        )
                    )
                }
        }
    }
}
