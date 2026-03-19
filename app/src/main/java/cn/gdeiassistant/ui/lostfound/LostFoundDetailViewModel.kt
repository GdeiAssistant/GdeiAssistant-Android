package cn.gdeiassistant.ui.lostfound

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.R
import cn.gdeiassistant.data.LostFoundRepository
import cn.gdeiassistant.model.LostFoundDetail
import cn.gdeiassistant.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LostFoundDetailUiState(
    val detail: LostFoundDetail? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class LostFoundDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val lostFoundRepository: LostFoundRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val itemId: String = savedStateHandle.get<String>(Routes.LOST_FOUND_ITEM_ID).orEmpty()

    private val _state = MutableStateFlow(LostFoundDetailUiState())
    val state: StateFlow<LostFoundDetailUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        if (itemId.isBlank()) {
            _state.update {
                it.copy(isLoading = false, error = context.getString(R.string.lost_found_detail_missing_id))
            }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            lostFoundRepository.getItemDetail(itemId)
                .onSuccess { detail ->
                    _state.update { it.copy(detail = detail, isLoading = false, error = null) }
                }
                .onFailure { error ->
                    _state.update { it.copy(detail = null, isLoading = false, error = error.message) }
                }
        }
    }
}
