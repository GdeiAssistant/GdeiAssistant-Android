package cn.gdeiassistant.ui.secret

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.data.SecretRepository
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
    val error: String? = null
)

@HiltViewModel
class SecretProfileViewModel @Inject constructor(
    private val secretRepository: SecretRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SecretProfileUiState())
    val state: StateFlow<SecretProfileUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            secretRepository.getMyPosts()
                .onSuccess { items ->
                    _state.update { it.copy(items = items, isLoading = false, error = null) }
                }
                .onFailure { error ->
                    _state.update { it.copy(items = emptyList(), isLoading = false, error = error.message) }
                }
        }
    }
}
