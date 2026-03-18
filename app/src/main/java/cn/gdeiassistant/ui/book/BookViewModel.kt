package cn.gdeiassistant.ui.book

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.R
import cn.gdeiassistant.data.BookRepository
import cn.gdeiassistant.model.CollectionBorrowItem
import cn.gdeiassistant.model.CollectionSearchItem
import cn.gdeiassistant.ui.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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

data class BookUiState(
    val searchKeyword: String = "",
    val borrowPassword: String = "",
    val searchResults: List<CollectionSearchItem> = emptyList(),
    val borrowedItems: List<CollectionBorrowItem> = emptyList(),
    val totalPages: Int = 0,
    val isSearching: Boolean = false,
    val isBorrowLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val renewingId: String? = null,
    val searchError: String? = null,
    val borrowError: String? = null
) {
    val borrowedCount: Int
        get() = borrowedItems.size
}

sealed interface BookEvent {
    data class ShowMessage(val message: UiText) : BookEvent
}

@HiltViewModel
class BookViewModel @Inject constructor(
    private val repository: BookRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(BookUiState())
    val state: StateFlow<BookUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<BookEvent>()
    val events: SharedFlow<BookEvent> = _events.asSharedFlow()

    fun updateSearchKeyword(keyword: String) {
        _state.update { it.copy(searchKeyword = keyword) }
    }

    fun updateBorrowPassword(password: String) {
        _state.update { it.copy(borrowPassword = password) }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            val borrowDeferred = async { repository.getBorrowedBooks(password = _state.value.borrowPassword) }
            val searchDeferred = if (_state.value.searchKeyword.isNotBlank()) {
                async { repository.searchCollections(keyword = _state.value.searchKeyword, page = 1) }
            } else {
                null
            }

            val borrowResult = borrowDeferred.await()
            val searchResult = searchDeferred?.await()
            _state.update {
                it.copy(
                    borrowedItems = borrowResult.getOrDefault(it.borrowedItems),
                    borrowError = borrowResult.exceptionOrNull()?.message,
                    searchResults = searchResult?.getOrNull()?.items ?: it.searchResults,
                    totalPages = searchResult?.getOrNull()?.sumPage ?: it.totalPages,
                    searchError = searchResult?.exceptionOrNull()?.message,
                    isBorrowLoading = false,
                    isSearching = false,
                    isRefreshing = false
                )
            }
        }
    }

    fun search() {
        val keyword = _state.value.searchKeyword.trim()
        if (keyword.isBlank()) {
            _state.update {
                it.copy(
                    searchResults = emptyList(),
                    totalPages = 0,
                    searchError = null
                )
            }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSearching = true, searchError = null) }
            repository.searchCollections(keyword = keyword, page = 1)
                .onSuccess { page ->
                    _state.update {
                        it.copy(
                            searchResults = page.items,
                            totalPages = page.sumPage,
                            isSearching = false,
                            searchError = null
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(isSearching = false, searchError = error.message) }
                }
        }
    }

    fun clearSearch() {
        _state.update {
            it.copy(
                searchKeyword = "",
                searchResults = emptyList(),
                totalPages = 0,
                searchError = null
            )
        }
    }

    fun loadBorrowedBooks() {
        viewModelScope.launch {
            _state.update { it.copy(isBorrowLoading = true, borrowError = null) }
            repository.getBorrowedBooks(password = _state.value.borrowPassword)
                .onSuccess { items ->
                    _state.update { it.copy(borrowedItems = items, isBorrowLoading = false, borrowError = null) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isBorrowLoading = false, borrowError = error.message) }
                }
        }
    }

    fun renewBook(item: CollectionBorrowItem) {
        if (_state.value.renewingId == item.id) return
        viewModelScope.launch {
            _state.update { it.copy(renewingId = item.id) }
            repository.renewBook(item)
                .onSuccess {
                    _events.emit(BookEvent.ShowMessage(UiText.StringResource(R.string.book_renew_success)))
                    _state.update { it.copy(renewingId = null) }
                    loadBorrowedBooks()
                }
                .onFailure { error ->
                    _state.update { it.copy(renewingId = null) }
                    _events.emit(
                        BookEvent.ShowMessage(
                            UiText.DynamicString(error.message ?: context.getString(R.string.book_detail_renew_failed))
                        )
                    )
                }
        }
    }
}
