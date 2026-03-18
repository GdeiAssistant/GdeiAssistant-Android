package cn.gdeiassistant.ui.spare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.data.SpareRoomRepository
import cn.gdeiassistant.model.SpareRoomItem
import cn.gdeiassistant.model.SpareRoomQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SpareUiState(
    val query: SpareRoomQuery = SpareRoomQuery(minWeek = 1, maxWeek = 20),
    val items: List<SpareRoomItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

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

    fun adjustMinWeek(delta: Int) {
        _state.update { state ->
            val next = (state.query.minWeek + delta).coerceIn(1, state.query.maxWeek)
            state.copy(query = state.query.copy(minWeek = next))
        }
    }

    fun adjustMaxWeek(delta: Int) {
        _state.update { state ->
            val next = (state.query.maxWeek + delta).coerceIn(state.query.minWeek, 20)
            state.copy(query = state.query.copy(maxWeek = next))
        }
    }

    fun updateWeekType(weekType: Int) {
        _state.update { it.copy(query = it.query.copy(weekType = weekType)) }
    }

    fun adjustStartTime(delta: Int) {
        _state.update { state ->
            val nextStart = (state.query.startTime + delta).coerceIn(1, 20)
            val nextEnd = state.query.endTime.coerceAtLeast(nextStart)
            state.copy(query = state.query.copy(startTime = nextStart, endTime = nextEnd))
        }
    }

    fun adjustEndTime(delta: Int) {
        _state.update { state ->
            val nextEnd = (state.query.endTime + delta).coerceIn(state.query.startTime, 20)
            state.copy(query = state.query.copy(endTime = nextEnd))
        }
    }

    fun adjustClassNumber(delta: Int) {
        _state.update { state ->
            val nextClassNumber = (state.query.classNumber + delta).coerceIn(1, 10)
            state.copy(query = state.query.copy(classNumber = nextClassNumber))
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
