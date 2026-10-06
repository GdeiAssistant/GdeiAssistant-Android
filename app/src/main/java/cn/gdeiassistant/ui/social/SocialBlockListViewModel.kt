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

data class SocialBlockListUiState(
    val items: List<SocialUser> = emptyList(),
    val nextCursor: String? = null,
    val hasMore: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SocialBlockListViewModel @Inject constructor(
    private val socialRepository: SocialRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(SocialBlockListUiState(isLoading = true))
    val state: StateFlow<SocialBlockListUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<SocialUiEvent>()
    val events: SharedFlow<SocialUiEvent> = _events.asSharedFlow()

    init {
        refresh()
    }

    fun refresh() = load(true)

    fun loadMore() {
        if (_state.value.isLoading || !_state.value.hasMore) return
        load(false)
    }

    fun unblock(userId: String) {
        viewModelScope.launch {
            socialRepository.unblock(userId)
                .onSuccess { refresh() }
                .onFailure { error ->
                    _events.emit(
                        SocialUiEvent.ShowMessage(
                            error.message ?: context.getString(R.string.messages_action_failed)
                        )
                    )
                }
        }
    }

    private fun load(reset: Boolean) {
        viewModelScope.launch {
            _state.update {
                it.copy(isLoading = true, error = null, items = if (reset) emptyList() else it.items)
            }
            socialRepository.getBlocks(cursor = if (reset) null else _state.value.nextCursor)
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
