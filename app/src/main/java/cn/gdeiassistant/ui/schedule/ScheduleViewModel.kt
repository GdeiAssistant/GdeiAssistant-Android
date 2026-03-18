package cn.gdeiassistant.ui.schedule

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.R
import cn.gdeiassistant.data.ScheduleRepository
import cn.gdeiassistant.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val repository: ScheduleRepository,
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(ScheduleUiState())
    val state: StateFlow<ScheduleUiState> = _state.asStateFlow()

    init {
        val current = currentWeekNumber()
        _state.update { it.copy(selectedWeek = current) }
        observeBackgroundSetting()
        loadSchedule(current)
    }

    fun loadSchedule(week: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.loadSchedule(week).fold(
                onSuccess = { result ->
                    val list = result?.scheduleList.orEmpty()
                    val maxRow = list.maxOfOrNull { (it.row ?: 0) + (it.scheduleLength ?: 1).coerceAtLeast(1) - 1 } ?: 0
                    _state.update {
                        it.copy(
                            isLoading = false,
                            scheduleList = list,
                            selectedWeek = result?.week ?: week,
                            totalSections = (maxRow + 1).coerceIn(10, 14)
                        )
                    }
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(isLoading = false, error = e.message ?: context.getString(R.string.load_failed))
                    }
                }
            )
        }
    }

    fun setWeek(week: Int) {
        _state.update { it.copy(selectedWeek = week.coerceIn(1, it.maxWeeks)) }
        loadSchedule(week.coerceIn(1, _state.value.maxWeeks))
    }

    fun refresh() {
        loadSchedule(_state.value.selectedWeek)
    }

    fun setBackgroundImageUri(uri: String?) {
        viewModelScope.launch {
            settingsRepository.setScheduleBackgroundUri(uri)
        }
    }

    private fun observeBackgroundSetting() {
        viewModelScope.launch {
            settingsRepository.scheduleBackgroundUri.collectLatest { uri ->
                _state.update { it.copy(backgroundImageUri = uri) }
            }
        }
    }
}
