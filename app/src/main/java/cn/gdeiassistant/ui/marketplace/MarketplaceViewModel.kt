package cn.gdeiassistant.ui.marketplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.data.MarketplaceRepository
import cn.gdeiassistant.model.MarketplaceItem
import cn.gdeiassistant.model.MarketplaceTypeOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MarketplaceUiState(
    val typeOptions: List<MarketplaceTypeOption> = emptyList(),
    val selectedTypeId: Int? = null,
    val items: List<MarketplaceItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MarketplaceViewModel @Inject constructor(
    private val marketplaceRepository: MarketplaceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(
        MarketplaceUiState(
            typeOptions = marketplaceRepository.typeOptions,
            isLoading = true
        )
    )
    val state: StateFlow<MarketplaceUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun selectType(typeId: Int?) {
        if (_state.value.selectedTypeId == typeId) return
        _state.update { it.copy(selectedTypeId = typeId) }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            marketplaceRepository.getItems(_state.value.selectedTypeId)
                .onSuccess { items ->
                    _state.update { it.copy(items = items, isLoading = false, error = null) }
                }
                .onFailure { error ->
                    _state.update { it.copy(items = emptyList(), isLoading = false, error = error.message) }
                }
        }
    }
}
