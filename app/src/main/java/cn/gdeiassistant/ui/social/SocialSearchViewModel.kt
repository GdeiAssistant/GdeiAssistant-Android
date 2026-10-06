package cn.gdeiassistant.ui.social

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.R
import cn.gdeiassistant.data.SocialRepository
import cn.gdeiassistant.model.SocialUser
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

data class SocialSearchUiState(
    val query: String = "",
    val items: List<SocialUser> = emptyList(),
    val nextCursor: String? = null,
    val hasMore: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface SocialUiEvent {
    data class ShowMessage(val message: String) : SocialUiEvent
    data class OpenConversation(val conversationId: String) : SocialUiEvent
}

@HiltViewModel
class SocialSearchViewModel @Inject constructor(
    private val socialRepository: SocialRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(SocialSearchUiState())
    val state: StateFlow<SocialSearchUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<SocialUiEvent>()
    val events: SharedFlow<SocialUiEvent> = _events.asSharedFlow()

    fun updateQuery(value: String) {
        _state.update { it.copy(query = value) }
    }

    fun search(reset: Boolean = true) {
        viewModelScope.launch {
            val query = _state.value.query.trim()
            _state.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    items = if (reset) emptyList() else it.items,
                    nextCursor = if (reset) null else it.nextCursor
                )
            }
            socialRepository.searchUsers(
                query = query.takeIf(String::isNotBlank),
                cursor = if (reset) null else _state.value.nextCursor
            ).onSuccess { page ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        items = if (reset) page.items else it.items + page.items,
                        nextCursor = page.nextCursor,
                        hasMore = page.hasMore
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(isLoading = false, error = error.message ?: context.getString(R.string.load_failed))
                }
            }
        }
    }

    fun loadMore() {
        if (_state.value.isLoading || !_state.value.hasMore) return
        search(reset = false)
    }
}
