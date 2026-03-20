package cn.gdeiassistant.ui.library

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.R
import cn.gdeiassistant.data.LibraryRepository
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

data class LibraryDetailUiState(
    val item: CollectionBorrowItem? = null,
    val isRenewing: Boolean = false,
    val error: String? = null
)

sealed interface LibraryDetailEvent {
    data class ShowMessage(val message: String) : LibraryDetailEvent
    data object RenewSucceeded : LibraryDetailEvent
}

@HiltViewModel
class LibraryDetailViewModel @Inject constructor(
    private val repository: LibraryRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(LibraryDetailUiState())
    val state: StateFlow<LibraryDetailUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<LibraryDetailEvent>()
    val events: SharedFlow<LibraryDetailEvent> = _events.asSharedFlow()

    fun bindItem(item: CollectionBorrowItem?) {
        _state.update { it.copy(item = item, error = null) }
    }

    fun renewBook(password: String) {
        val item = _state.value.item ?: return
        viewModelScope.launch {
            _state.update { it.copy(isRenewing = true, error = null) }
            repository.renewBook(item, password)
                .onSuccess {
                    _state.update {
                        it.copy(
                            item = item.copy(renewCount = item.renewCount + 1),
                            isRenewing = false,
                            error = null
                        )
                    }
                    _events.emit(LibraryDetailEvent.ShowMessage(context.getString(R.string.library_renew_success)))
                    _events.emit(LibraryDetailEvent.RenewSucceeded)
                }
                .onFailure { error ->
                    _state.update { it.copy(isRenewing = false, error = error.message) }
                    _events.emit(
                        LibraryDetailEvent.ShowMessage(
                            error.message ?: context.getString(R.string.library_detail_renew_failed)
                        )
                    )
                }
        }
    }
}
