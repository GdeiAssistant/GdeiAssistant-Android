package cn.gdeiassistant.ui.graduateExam

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.R
import cn.gdeiassistant.data.GraduateExamRepository
import cn.gdeiassistant.model.GraduateExamQuery
import cn.gdeiassistant.model.GraduateExamScore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GraduateExamUiState(
    val query: GraduateExamQuery = GraduateExamQuery(),
    val score: GraduateExamScore? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class GraduateExamViewModel @Inject constructor(
    private val graduateExamRepository: GraduateExamRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(GraduateExamUiState())
    val state: StateFlow<GraduateExamUiState> = _state.asStateFlow()

    fun updateName(name: String) {
        _state.update { it.copy(query = it.query.copy(name = name)) }
    }

    fun updateExamNumber(examNumber: String) {
        _state.update { it.copy(query = it.query.copy(examNumber = examNumber)) }
    }

    fun updateIdNumber(idNumber: String) {
        _state.update { it.copy(query = it.query.copy(idNumber = idNumber)) }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    fun submit() {
        val query = _state.value.query
        if (query.name.isBlank() || query.examNumber.isBlank() || query.idNumber.isBlank()) {
            _state.update {
                it.copy(error = context.getString(R.string.graduate_exam_error_incomplete), score = null)
            }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            graduateExamRepository.queryScore(query)
                .onSuccess { score ->
                    _state.update { it.copy(isLoading = false, score = score, error = null) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, score = null, error = error.message) }
                }
        }
    }
}
