package cn.gdeiassistant.ui.notice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import cn.gdeiassistant.data.NoticeDetail
import cn.gdeiassistant.data.NoticeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class NoticeDetailUiState(
    val detail: NoticeDetail? = null
)

@HiltViewModel
class NoticeDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    noticeRepository: NoticeRepository
) : ViewModel() {

    private val noticeId = savedStateHandle.get<String>("noticeId")?.toIntOrNull() ?: 0
    val state = NoticeDetailUiState(
        detail = noticeRepository.getNoticeDetail(noticeId)
    )
}
