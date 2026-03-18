package cn.gdeiassistant.ui.evaluate

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.R
import cn.gdeiassistant.data.EvaluateRepository
import cn.gdeiassistant.ui.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EvaluateUiState(
    val directSubmit: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: UiText? = null,
    val status: UiText? = null
)

sealed interface EvaluateEvent {
    data class ShowMessage(val message: String) : EvaluateEvent
}

@HiltViewModel
class EvaluateViewModel @Inject constructor(
    private val repository: EvaluateRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(EvaluateUiState())
    val state: StateFlow<EvaluateUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<EvaluateEvent>()
    val events: SharedFlow<EvaluateEvent> = _events.asSharedFlow()

    fun setDirectSubmit(enabled: Boolean) {
        _state.update { it.copy(directSubmit = enabled) }
    }

    fun submit() {
        if (_state.value.isSubmitting) return
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null, status = null) }
            repository.submit(_state.value.directSubmit).fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            status = UiText.StringResource(
                                if (it.directSubmit) R.string.evaluate_submit_success_direct else R.string.evaluate_submit_success_saved
                            )
                        )
                    }
                    _events.emit(EvaluateEvent.ShowMessage(context.getString(R.string.evaluate_submit_toast)))
                },
                onFailure = { throwable ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
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
}
