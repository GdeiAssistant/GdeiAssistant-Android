package cn.gdeiassistant.ui.datacenter

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.R
import cn.gdeiassistant.data.DataCenterRepository
import cn.gdeiassistant.model.ElectricityBill
import cn.gdeiassistant.model.ElectricityQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ElectricityFeesUiState(
    val query: ElectricityQuery = ElectricityQuery(),
    val bill: ElectricityBill? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val availableYears: List<Int> = ElectricityQuery.yearOptions()
)

@HiltViewModel
class ElectricityFeesViewModel @Inject constructor(
    private val dataCenterRepository: DataCenterRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(ElectricityFeesUiState())
    val state: StateFlow<ElectricityFeesUiState> = _state.asStateFlow()

    fun updateYear(year: Int) {
        _state.update { it.copy(query = it.query.copy(year = year)) }
    }

    fun updateName(name: String) {
        _state.update { it.copy(query = it.query.copy(name = name)) }
    }

    fun updateStudentNumber(studentNumber: String) {
        _state.update { it.copy(query = it.query.copy(studentNumber = studentNumber.filter(Char::isDigit))) }
    }

    fun submit() {
        val query = _state.value.query
        if (query.name.isBlank() || query.studentNumber.isBlank()) {
            _state.update {
                it.copy(error = context.getString(R.string.electricity_error_incomplete), bill = null)
            }
            return
        }
        if (query.studentNumber.length != 11) {
            _state.update {
                it.copy(error = context.getString(R.string.electricity_error_student_number), bill = null)
            }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            dataCenterRepository.queryElectricity(query)
                .onSuccess { bill ->
                    _state.update { it.copy(isLoading = false, bill = bill, error = null) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, bill = null, error = error.message) }
                }
        }
    }
}
