package cn.gdeiassistant.ui.book

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.R
import cn.gdeiassistant.data.BookRepository
import cn.gdeiassistant.model.CollectionBorrowItem
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

data class BookDetailUiState(
    val item: CollectionBorrowItem? = null,
    val isRenewing: Boolean = false,
    val error: String? = null
)

sealed interface BookDetailEvent {
    data class ShowMessage(val message: String) : BookDetailEvent
    data object RenewSucceeded : BookDetailEvent
}

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    private val repository: BookRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(BookDetailUiState())
    val state: StateFlow<BookDetailUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<BookDetailEvent>()
    val events: SharedFlow<BookDetailEvent> = _events.asSharedFlow()

    fun bindItem(item: CollectionBorrowItem?) {
        _state.update { it.copy(item = item, error = null) }
    }

    fun renewBook() {
        val item = _state.value.item ?: return
        viewModelScope.launch {
            _state.update { it.copy(isRenewing = true, error = null) }
            repository.renewBook(item)
                .onSuccess {
                    _state.update {
                        it.copy(
                            item = item.copy(renewCount = item.renewCount + 1),
                            isRenewing = false,
                            error = null
                        )
                    }
                    _events.emit(BookDetailEvent.ShowMessage(context.getString(R.string.book_renew_success)))
                    _events.emit(BookDetailEvent.RenewSucceeded)
                }
                .onFailure { error ->
                    _state.update { it.copy(isRenewing = false, error = error.message) }
                    _events.emit(
                        BookDetailEvent.ShowMessage(
                            error.message ?: context.getString(R.string.book_detail_renew_failed)
                        )
                    )
                }
        }
    }
}
