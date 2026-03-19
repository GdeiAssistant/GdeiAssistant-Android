package cn.gdeiassistant.ui.notice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.data.AnnouncementRepository
import cn.gdeiassistant.model.AnnouncementItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NoticeListUiState(
    val isLoading: Boolean = false,
    val notices: List<AnnouncementItem> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class NoticeListViewModel @Inject constructor(
    private val announcementRepository: AnnouncementRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NoticeListUiState(isLoading = true))
    val state: StateFlow<NoticeListUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            announcementRepository.getAnnouncementPage(size = 20).fold(
                onSuccess = { list ->
                    _state.update { it.copy(isLoading = false, notices = list) }
                },
                onFailure = { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }
}
