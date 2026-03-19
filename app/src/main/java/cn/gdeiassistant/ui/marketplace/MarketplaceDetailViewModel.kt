package cn.gdeiassistant.ui.marketplace

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.R
import cn.gdeiassistant.data.MarketplaceRepository
import cn.gdeiassistant.model.MarketplaceDetail
import cn.gdeiassistant.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MarketplaceDetailUiState(
    val detail: MarketplaceDetail? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class MarketplaceDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val marketplaceRepository: MarketplaceRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val itemId: String = savedStateHandle.get<String>(Routes.MARKETPLACE_ITEM_ID).orEmpty()

    private val _state = MutableStateFlow(MarketplaceDetailUiState())
    val state: StateFlow<MarketplaceDetailUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        if (itemId.isBlank()) {
            _state.update {
                it.copy(isLoading = false, error = context.getString(R.string.marketplace_detail_missing_id))
            }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            marketplaceRepository.getItemDetail(itemId)
                .onSuccess { detail ->
                    _state.update { it.copy(detail = detail, isLoading = false, error = null) }
                }
                .onFailure { error ->
                    _state.update { it.copy(detail = null, isLoading = false, error = error.message) }
                }
        }
    }
}
