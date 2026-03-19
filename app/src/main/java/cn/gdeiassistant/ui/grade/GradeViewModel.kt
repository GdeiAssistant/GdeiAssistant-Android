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
                    val firstTermGrades = result?.firstTermGradeList.orEmpty().toPersistentList()
                    val secondTermGrades = result?.secondTermGradeList.orEmpty().toPersistentList()
                    _state.update {
                        it.copy(
                            isLoading = false,
                            rawResult = result,
                            selectedYear = resolvedYear,
                            selectedTerm = it.selectedTerm.resolveWith(
                                firstTermHasData = firstTermGrades.isNotEmpty(),
                                secondTermHasData = secondTermGrades.isNotEmpty()
                            ),
                            firstTermGrades = firstTermGrades,
                            secondTermGrades = secondTermGrades
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

    fun setTerm(term: GradeTerm) {
        _state.update { it.copy(selectedTerm = term) }
    }

    fun refresh() {
        loadGrades(_state.value.selectedYear)
    }
}

private fun GradeTerm.resolveWith(firstTermHasData: Boolean, secondTermHasData: Boolean): GradeTerm {
    return when (this) {
        GradeTerm.FIRST -> if (firstTermHasData || !secondTermHasData) GradeTerm.FIRST else GradeTerm.SECOND
        GradeTerm.SECOND -> if (secondTermHasData || !firstTermHasData) GradeTerm.SECOND else GradeTerm.FIRST
    }
}
