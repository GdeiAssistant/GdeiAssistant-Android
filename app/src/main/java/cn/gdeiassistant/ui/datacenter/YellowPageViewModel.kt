package cn.gdeiassistant.ui.datacenter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.data.DataCenterRepository
import cn.gdeiassistant.model.YellowPageCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class YellowPageUiState(
    val categories: List<YellowPageCategory> = emptyList(),
    val selectedCategoryId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class YellowPageViewModel @Inject constructor(
    private val dataCenterRepository: DataCenterRepository
) : ViewModel() {

    private val _state = MutableStateFlow(YellowPageUiState(isLoading = true))
    val state: StateFlow<YellowPageUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun selectCategory(categoryId: String) {
        _state.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            dataCenterRepository.getYellowPages()
                .onSuccess { categories ->
                    _state.update { state ->
                        state.copy(
                            categories = categories,
                            selectedCategoryId = state.selectedCategoryId
                                ?.takeIf { selected -> categories.any { it.id == selected } }
                                ?: categories.firstOrNull()?.id,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(categories = emptyList(), isLoading = false, error = error.message) }
                }
        }
    }
}
