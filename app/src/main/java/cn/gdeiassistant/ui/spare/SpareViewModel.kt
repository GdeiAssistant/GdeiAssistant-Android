package cn.gdeiassistant.ui.spare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.data.SpareRoomRepository
import cn.gdeiassistant.model.SpareRoomItem
import cn.gdeiassistant.model.SpareRoomQuery
import cn.gdeiassistant.model.selectedWeekdayValue
import cn.gdeiassistant.model.sparePeriodRange
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SpareUiState(
    val query: SpareRoomQuery = SpareRoomQuery(),
    val items: List<SpareRoomItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val selectedWeekday: Int
        get() = query.selectedWeekdayValue()
}

@HiltViewModel
class SpareViewModel @Inject constructor(
    private val spareRoomRepository: SpareRoomRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SpareUiState())
    val state: StateFlow<SpareUiState> = _state.asStateFlow()

    fun updateZone(zone: Int) {
        _state.update { it.copy(query = it.query.copy(zone = zone)) }
    }

    fun updateType(type: Int) {
        _state.update { it.copy(query = it.query.copy(type = type)) }
    }

    fun updateMinSeating(value: String) {
        _state.update { state ->
            state.copy(
                query = state.query.copy(
                    minSeating = value.filter(Char::isDigit).toIntOrNull()
                )
            )
        }
    }

    fun updateMaxSeating(value: String) {
        _state.update { state ->
            state.copy(
                query = state.query.copy(
                    maxSeating = value.filter(Char::isDigit).toIntOrNull()
                )
            )
        }
    }

    fun updateWeekday(weekday: Int) {
        _state.update { state ->
            val minWeek = if (weekday < 0) 0 else weekday
            val maxWeek = if (weekday < 0) 6 else weekday
            state.copy(
                query = state.query.copy(
                    minWeek = minWeek,
                    maxWeek = maxWeek
                )
            )
        }
    }

    fun updateWeekType(weekType: Int) {
        _state.update { it.copy(query = it.query.copy(weekType = weekType)) }
    }

    fun updateClassNumber(classNumber: Int) {
        _state.update { state ->
            val (startTime, endTime) = sparePeriodRange(classNumber)
            state.copy(
                query = state.query.copy(
                    classNumber = classNumber,
                    startTime = startTime,
                    endTime = endTime
                )
            )
        }
    }

    fun submitQuery() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            spareRoomRepository.queryRooms(_state.value.query)
                .onSuccess { items ->
                    _state.update { it.copy(isLoading = false, items = items, error = null) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, items = emptyList(), error = error.message) }
                }
        }
    }
}
