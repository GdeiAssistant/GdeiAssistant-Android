package cn.gdeiassistant.ui.library

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.R
import cn.gdeiassistant.data.LibraryRepository
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

data class LibraryUiState(
    val searchKeyword: String = "",
    val currentPage: Int = 1,
    val searchResults: List<CollectionSearchItem> = emptyList(),
    val borrowPassword: String = "",
    val borrowedItems: List<CollectionBorrowItem> = emptyList(),
    val totalPages: Int = 0,
    val isSearching: Boolean = false,
    val isBorrowLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val hasLoadedBorrowedBooks: Boolean = false,
    val renewingId: String? = null,
    val searchError: String? = null,
    val borrowError: String? = null,
    val renewError: String? = null
) {
    val borrowedCount: Int
        get() = borrowedItems.size
}

sealed interface LibraryEvent {
    data class ShowMessage(val message: UiText) : LibraryEvent
    data object RenewSucceeded : LibraryEvent
}

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: LibraryRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(LibraryUiState())
    val state: StateFlow<LibraryUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<LibraryEvent>()
    val events: SharedFlow<LibraryEvent> = _events.asSharedFlow()

    fun updateSearchKeyword(keyword: String) {
        _state.update { it.copy(searchKeyword = keyword) }
    }

    fun updateBorrowPassword(password: String) {
        _state.update { it.copy(borrowPassword = password, borrowError = null) }
    }

    fun clearRenewError() {
        _state.update { it.copy(renewError = null) }
    }

    fun refresh() {
        viewModelScope.launch {
            val currentState = _state.value
            _state.update { it.copy(isRefreshing = true) }
            val borrowDeferred = if (currentState.hasLoadedBorrowedBooks && currentState.borrowPassword.isNotBlank()) {
                async { repository.getBorrowedBooks(currentState.borrowPassword) }
            } else {
                null
            }
            val searchDeferred = if (currentState.searchKeyword.isNotBlank()) {
                async { repository.searchCollections(keyword = currentState.searchKeyword, page = currentState.currentPage) }
            } else {
                null
            }

            val borrowResult = borrowDeferred?.await()
            val searchResult = searchDeferred?.await()
            _state.update {
                it.copy(
                    borrowedItems = borrowResult?.getOrDefault(it.borrowedItems) ?: it.borrowedItems,
                    borrowError = borrowResult?.exceptionOrNull()?.message ?: it.borrowError,
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
        _state.update { it.copy(currentPage = 1) }
        searchPage(1)
    }

    fun goToPreviousPage() {
        val current = _state.value.currentPage
        if (current > 1) {
            _state.update { it.copy(currentPage = current - 1) }
            searchPage(current - 1)
        }
    }

    fun goToNextPage() {
        val current = _state.value.currentPage
        if (current < _state.value.totalPages) {
            _state.update { it.copy(currentPage = current + 1) }
            searchPage(current + 1)
        }
    }

    private fun searchPage(page: Int) {
        val keyword = _state.value.searchKeyword.trim()
        if (keyword.isBlank()) {
            _state.update {
                it.copy(
                    searchResults = emptyList(),
                    totalPages = 0,
                    currentPage = 1,
                    searchError = null
                )
            }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSearching = true, searchError = null) }
            repository.searchCollections(keyword = keyword, page = page)
                .onSuccess { result ->
                    _state.update {
                        it.copy(
                            searchResults = result.items,
                            totalPages = result.sumPage,
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
                currentPage = 1,
                searchResults = emptyList(),
                totalPages = 0,
                searchError = null
            )
        }
    }

    fun loadBorrowedBooks() {
        val password = _state.value.borrowPassword.trim()
        if (password.isBlank()) {
            _state.update {
                it.copy(
                    borrowError = context.getString(R.string.library_borrow_password_required),
                    isBorrowLoading = false
                )
            }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isBorrowLoading = true, borrowError = null) }
            repository.getBorrowedBooks(password)
                .onSuccess { items ->
                    _state.update {
                        it.copy(
                            borrowedItems = items,
                            borrowPassword = password,
                            isBorrowLoading = false,
                            hasLoadedBorrowedBooks = true,
                            borrowError = null
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(isBorrowLoading = false, borrowError = error.message) }
                }
        }
    }

    fun renewBook(item: CollectionBorrowItem, password: String) {
        if (_state.value.renewingId == item.id) return
        val normalizedPassword = password.trim()
        if (normalizedPassword.isBlank()) {
            viewModelScope.launch {
                _state.update { it.copy(renewError = context.getString(R.string.library_borrow_password_required)) }
                _events.emit(LibraryEvent.ShowMessage(UiText.StringResource(R.string.library_borrow_password_required)))
            }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(renewingId = item.id, renewError = null) }
            repository.renewBook(item, normalizedPassword)
                .onSuccess {
                    _events.emit(LibraryEvent.ShowMessage(UiText.StringResource(R.string.library_renew_success)))
                    _events.emit(LibraryEvent.RenewSucceeded)
                    _state.update {
                        it.copy(
                            renewingId = null,
                            renewError = null,
                            borrowPassword = normalizedPassword
                        )
                    }
                    loadBorrowedBooks()
                }
                .onFailure { error ->
                    _state.update { it.copy(renewingId = null, renewError = error.message) }
                    _events.emit(
                        LibraryEvent.ShowMessage(
                            UiText.DynamicString(error.message ?: context.getString(R.string.library_detail_renew_failed))
                        )
                    )
                }
        }
    }
}
