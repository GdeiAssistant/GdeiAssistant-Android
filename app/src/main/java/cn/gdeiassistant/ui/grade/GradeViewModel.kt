package cn.gdeiassistant.ui.grade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.R
import cn.gdeiassistant.data.GradeRepository
import cn.gdeiassistant.ui.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GradeViewModel @Inject constructor(
    private val repository: GradeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(GradeUiState())
    val state: StateFlow<GradeUiState> = _state.asStateFlow()

    init {
        loadGrades()
    }

    fun loadGrades(year: Int? = null) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.loadGrades(year).fold(
                onSuccess = { result ->
                    val resolvedYear = year ?: _state.value.selectedYear
                    _state.update {
                        it.copy(
                            isLoading = false,
                            rawResult = result,
                            selectedYear = resolvedYear,
                            firstTermGrades = result?.firstTermGradeList.orEmpty().toPersistentList(),
                            secondTermGrades = result?.secondTermGradeList.orEmpty().toPersistentList()
                        )
                    }
                },
                onFailure = { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = if (throwable.message.isNullOrBlank()) {
                                UiText.StringResource(R.string.load_failed)
                            } else {
                                UiText.DynamicString(throwable.message!!)
                            }
                        )
                    }
                }
            )
        }
    }

    fun setYear(year: Int) {
        _state.update { it.copy(selectedYear = year) }
        loadGrades(year)
    }

    fun refresh() {
        loadGrades(_state.value.selectedYear)
    }
}
