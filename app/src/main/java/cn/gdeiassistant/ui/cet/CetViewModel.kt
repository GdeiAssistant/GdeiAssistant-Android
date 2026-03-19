package cn.gdeiassistant.ui.cet

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.R
import cn.gdeiassistant.data.CetRepository
import cn.gdeiassistant.model.Cet
import cn.gdeiassistant.ui.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CetUiState(
    val checkCodeImageBase64: String? = null,
    val ticketNumber: String = "",
    val name: String = "",
    val checkcode: String = "",
    val isLoading: Boolean = false,
    val isCheckCodeLoading: Boolean = false,
    val error: UiText? = null,
    val result: Cet? = null,
    val showResult: Boolean = false
) {
    val canQuery: Boolean
        get() = ticketNumber.isNotBlank() && name.isNotBlank() && checkcode.isNotBlank() && !isLoading
}

@HiltViewModel
class CetViewModel @Inject constructor(
    private val repository: CetRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(CetUiState())
    val state: StateFlow<CetUiState> = _state.asStateFlow()

    init {
        loadCheckCode()
    }

    fun loadCheckCode() {
        viewModelScope.launch {
            _state.update { it.copy(error = null, isCheckCodeLoading = true) }
            repository.getCheckCode().fold(
                onSuccess = { imageBase64 ->
                    _state.update {
                        it.copy(
                            checkCodeImageBase64 = imageBase64,
                            isCheckCodeLoading = false
                        )
                    }
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(
                            isCheckCodeLoading = false,
                            error = UiText.DynamicString(
                                e.message ?: context.getString(R.string.cet_checkcode_unavailable)
                            )
                        )
                    }
                }
            )
        }
    }

    fun updateTicketNumber(value: String) {
        _state.update { it.copy(ticketNumber = value, error = null) }
    }

    fun updateName(value: String) {
        _state.update { it.copy(name = value, error = null) }
    }

    fun updateCheckcode(value: String) {
        _state.update { it.copy(checkcode = value, error = null) }
    }

    fun query() {
        val s = _state.value
        if (s.ticketNumber.isBlank() || s.name.isBlank() || s.checkcode.isBlank()) {
            _state.update {
                it.copy(error = UiText.StringResource(R.string.cet_form_incomplete))
            }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, result = null, showResult = false) }
            repository.query(s.ticketNumber.trim(), s.name.trim(), s.checkcode.trim()).fold(
                onSuccess = { cet ->
                    _state.update {
                        it.copy(isLoading = false, result = cet, showResult = true)
                    }
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            showResult = false,
                            result = null,
                            error = e.message?.takeIf { m -> m.isNotBlank() }?.let { UiText.DynamicString(it) }
                                ?: UiText.StringResource(R.string.cet_no_data)
                        )
                    }
                }
            )
        }
    }

    fun reset() {
        _state.update {
            it.copy(
                ticketNumber = "",
                name = "",
                checkcode = "",
                result = null,
                showResult = false,
                error = null,
                isLoading = false
            )
        }
        loadCheckCode()
    }
}
