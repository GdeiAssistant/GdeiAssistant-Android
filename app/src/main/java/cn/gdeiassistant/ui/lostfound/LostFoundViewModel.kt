package cn.gdeiassistant.ui.lostfound

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.data.LostFoundRepository
import cn.gdeiassistant.data.mapper.LostFoundDisplayMapper
import cn.gdeiassistant.model.LostFoundItem
import cn.gdeiassistant.model.LostFoundType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LostFoundFilter {
    ALL, LOST, FOUND
}

data class LostFoundUiState(
    val selectedFilter: LostFoundFilter = LostFoundFilter.ALL,
    val items: List<LostFoundItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val visibleItems: List<LostFoundItem>
        get() = when (selectedFilter) {
            LostFoundFilter.ALL -> items
            LostFoundFilter.LOST -> items.filter { it.type == LostFoundType.LOST }
            LostFoundFilter.FOUND -> items.filter { it.type == LostFoundType.FOUND }
        }
}

@HiltViewModel
class LostFoundViewModel @Inject constructor(
    private val lostFoundRepository: LostFoundRepository,
    private val displayMapper: LostFoundDisplayMapper
) : ViewModel() {

    private val _state = MutableStateFlow(LostFoundUiState(isLoading = true))
    val state: StateFlow<LostFoundUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun selectFilter(filter: LostFoundFilter) {
        _state.update { it.copy(selectedFilter = filter) }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            lostFoundRepository.getItems()
                .map { displayMapper.applyItemDefaults(it) }
                .onSuccess { items ->
                    _state.update { it.copy(items = items, isLoading = false, error = null) }
                }
                .onFailure { error ->
                    _state.update { it.copy(items = emptyList(), isLoading = false, error = error.message) }
                }
        }
    }
}
