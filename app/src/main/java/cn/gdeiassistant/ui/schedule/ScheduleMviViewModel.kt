package cn.gdeiassistant.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.model.ScheduleQueryResult
import cn.gdeiassistant.data.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * MVI 架构的课表 ViewModel，使用 sealed interface 表达三态 UI。
 */
sealed interface ScheduleUiMviState {
    data object Loading : ScheduleUiMviState
    data class Success(val scheduleData: ScheduleQueryResult) : ScheduleUiMviState
    data class Error(val message: String) : ScheduleUiMviState
}

@HiltViewModel
class ScheduleMviViewModel @Inject constructor(
    private val repository: ScheduleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScheduleUiMviState>(ScheduleUiMviState.Loading)
    val uiState: StateFlow<ScheduleUiMviState> = _uiState.asStateFlow()

    init {
        fetchSchedule()
    }

    fun fetchSchedule(week: Int? = null) {
        viewModelScope.launch {
            _uiState.value = ScheduleUiMviState.Loading
            try {
                val result = repository.loadSchedule(week)
                result.fold(
                    onSuccess = { data ->
                        if (data != null) {
                            _uiState.value = ScheduleUiMviState.Success(scheduleData = data)
                        } else {
                            _uiState.value = ScheduleUiMviState.Error(message = "未获取到课表数据")
                        }
                    },
                    onFailure = { e ->
                        _uiState.value = ScheduleUiMviState.Error(
                            message = e.message ?: "加载课表失败"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = ScheduleUiMviState.Error(
                    message = e.message ?: "网络请求异常"
                )
            }
        }
    }

    fun refresh() {
        fetchSchedule()
    }
}
