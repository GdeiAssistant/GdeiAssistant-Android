package cn.gdeiassistant.ui.secret

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.data.SecretRepository
import cn.gdeiassistant.data.mapper.SecretDisplayMapper
import cn.gdeiassistant.model.SecretPost
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SecretProfileUiState(
    val items: List<SecretPost> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class SecretProfileViewModel @Inject constructor(
    private val secretRepository: SecretRepository,
    private val displayMapper: SecretDisplayMapper
) : ViewModel() {

    private val pageSize = 20
    private val _state = MutableStateFlow(SecretProfileUiState())
    val state: StateFlow<SecretProfileUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            secretRepository.getMyPosts(start = 0, size = pageSize)
                .map { displayMapper.applyPostDefaults(it) }
                .onSuccess { items ->
                    _state.update {
                        it.copy(
                            items = items,
                            isLoading = false,
                            hasMore = items.size >= pageSize,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(items = emptyList(), isLoading = false, error = error.message) }
                }
        }
    }

    fun loadMore() {
        val current = _state.value
        if (current.isLoadingMore || !current.hasMore) return

        viewModelScope.launch {
            _state.update { it.copy(isLoadingMore = true) }
            secretRepository.getMyPosts(start = current.items.size, size = pageSize)
                .map { displayMapper.applyPostDefaults(it) }
                .onSuccess { newItems ->
                    _state.update {
                        it.copy(
                            items = it.items + newItems,
                            isLoadingMore = false,
                            hasMore = newItems.size >= pageSize
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoadingMore = false, error = error.message) }
                }
        }
    }
}
