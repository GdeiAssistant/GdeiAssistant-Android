package cn.gdeiassistant.ui.notice

import androidx.lifecycle.SavedStateHandle
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

data class NoticeDetailUiState(
    val isLoading: Boolean = false,
    val detail: AnnouncementItem? = null,
    val error: String? = null
)

@HiltViewModel
class NoticeDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val announcementRepository: AnnouncementRepository
) : ViewModel() {

    private val noticeId = savedStateHandle.get<String>("noticeId").orEmpty()

    private val _state = MutableStateFlow(NoticeDetailUiState(isLoading = true))
    val state: StateFlow<NoticeDetailUiState> = _state.asStateFlow()

    init {
        load()
    }

    private fun load() {
        if (noticeId.isBlank()) {
            _state.update {
                it.copy(isLoading = false, error = "未找到该公告")
            }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            announcementRepository.getAnnouncementDetail(noticeId).fold(
                onSuccess = { detail ->
                    _state.update {
                        it.copy(isLoading = false, detail = detail, error = null)
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(isLoading = false, detail = null, error = error.message)
                    }
                }
            )
        }
    }
}
