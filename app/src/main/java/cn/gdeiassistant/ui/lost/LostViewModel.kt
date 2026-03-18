package cn.gdeiassistant.ui.lost

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.R
import cn.gdeiassistant.data.CardRepository
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

data class LostUiState(
    val password: String = "",
    val isLoading: Boolean = false,
    val error: UiText? = null,
    val hasSucceeded: Boolean = false
) {
    val canSubmit: Boolean
        get() = password.length == 6 && !isLoading
}

sealed class LostEvent {
    data class ShowMessage(val message: String) : LostEvent()
}

@HiltViewModel
class LostViewModel @Inject constructor(
    private val repository: CardRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(LostUiState())
    val state: StateFlow<LostUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<LostEvent>()
    val events: SharedFlow<LostEvent> = _events.asSharedFlow()

    fun updatePassword(value: String) {
        _state.update { it.copy(password = value, error = null, hasSucceeded = false) }
    }

    fun submit() {
        val pwd = _state.value.password.trim()
        when {
            pwd.isBlank() -> _state.update { it.copy(error = UiText.StringResource(R.string.lost_password_empty)) }
            pwd.length != 6 -> _state.update { it.copy(error = UiText.StringResource(R.string.lost_password_length)) }
            else -> {
                viewModelScope.launch {
                    _state.update { it.copy(isLoading = true, error = null, hasSucceeded = false) }
                    repository.lostCard(pwd).fold(
                        onSuccess = {
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    hasSucceeded = true,
                                    password = ""
                                )
                            }
                            _events.emit(
                                LostEvent.ShowMessage(
                                    context.getString(R.string.lost_success)
                                )
                            )
                        },
                        onFailure = { e ->
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    error = e.message?.takeIf { m -> m.isNotBlank() }?.let { UiText.DynamicString(it) }
                                        ?: UiText.StringResource(R.string.lost_failed)
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
