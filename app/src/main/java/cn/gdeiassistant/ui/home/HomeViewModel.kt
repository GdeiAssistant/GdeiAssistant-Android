package cn.gdeiassistant.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.data.CardRepository
import cn.gdeiassistant.data.NoticeItem
import cn.gdeiassistant.data.NoticeRepository
import cn.gdeiassistant.data.ProfileRepository
import cn.gdeiassistant.data.ScheduleRepository
import cn.gdeiassistant.data.SessionManager
import cn.gdeiassistant.model.CardInfo
import cn.gdeiassistant.model.Schedule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val displayName: String? = null,
    val isLoading: Boolean = false,
    val todayCourses: List<Schedule> = emptyList(),
    val scheduleError: String? = null,
    val cardInfo: CardInfo? = null,
    val cardError: String? = null,
    val notices: List<NoticeItem> = emptyList(),
    val noticeError: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val cardRepository: CardRepository,
    private val noticeRepository: NoticeRepository,
    private val profileRepository: ProfileRepository,
    sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(
        HomeUiState(
            displayName = sessionManager.currentUsername()?.trim()?.takeIf(String::isNotBlank),
            isLoading = true
        )
    )
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val todayScheduleDeferred = async { scheduleRepository.loadTodaySchedule() }
            val cardDeferred = async { loadCardInfo() }
            val noticeDeferred = async { noticeRepository.getLatestNoticeList(limit = 5) }
            val profileDeferred = async { profileRepository.getProfile() }

            val todayResult = todayScheduleDeferred.await()
            val cardResult = cardDeferred.await()
            val noticeResult = noticeDeferred.await()
            val profileResult = profileDeferred.await()

            val todayList = todayResult.getOrNull().orEmpty().sortedBy { it.position ?: Int.MAX_VALUE }

            val nickname = profileResult.getOrNull()?.nickname?.takeIf { it.isNotBlank() }
                ?: _state.value.displayName

            _state.update {
                it.copy(
                    isLoading = false,
                    displayName = nickname,
                    todayCourses = todayList,
                    scheduleError = todayResult.exceptionOrNull()?.message,
                    cardInfo = cardResult.getOrNull(),
                    cardError = cardResult.exceptionOrNull()?.message,
                    notices = noticeResult.getOrNull().orEmpty(),
                    noticeError = noticeResult.exceptionOrNull()?.message
                )
            }
        }
    }

    private suspend fun loadCardInfo(): Result<CardInfo?> {
        return cardRepository.getCardInfo()
    }
}
