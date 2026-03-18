package cn.gdeiassistant.ui.charge

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.R
import cn.gdeiassistant.data.ChargeRepository
import cn.gdeiassistant.model.CardInfo
import cn.gdeiassistant.model.Charge
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

data class ChargeUiState(
    val amount: String = "",
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: UiText? = null,
    val cardInfo: CardInfo? = null,
    val paymentSession: Charge? = null
) {
    val balanceText: String
        get() = cardInfo?.cardBalance?.takeIf { it.isNotBlank() } ?: "—"

    val cardNumber: String
        get() = cardInfo?.cardNumber?.takeIf { it.isNotBlank() } ?: "—"

    val canSubmit: Boolean
        get() = amount.toIntOrNull()?.let { it in 1..500 } == true && !isSubmitting && !isLoading
}

sealed class ChargeEvent {
    data class ShowMessage(val message: String) : ChargeEvent()
}

@HiltViewModel
class ChargeViewModel @Inject constructor(
    private val repository: ChargeRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(ChargeUiState(isLoading = true))
    val state: StateFlow<ChargeUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ChargeEvent>()
    val events: SharedFlow<ChargeEvent> = _events.asSharedFlow()

    init {
        refresh()
    }

    fun updateAmount(value: String) {
        _state.update { it.copy(amount = value.filter(Char::isDigit).take(3), error = null) }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getCardInfo().fold(
                onSuccess = { cardInfo ->
                    _state.update {
                        it.copy(isLoading = false, cardInfo = cardInfo, error = null)
                    }
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = e.message?.takeIf(String::isNotBlank)?.let(UiText::DynamicString)
                                ?: UiText.StringResource(R.string.charge_info_unavailable)
                        )
                    }
                }
            )
        }
    }

    fun submitCharge() {
        val amount = state.value.amount.toIntOrNull()
        when {
            state.value.amount.isBlank() -> _state.update {
                it.copy(error = UiText.StringResource(R.string.charge_amount_empty))
            }

            amount == null -> _state.update {
                it.copy(error = UiText.StringResource(R.string.charge_amount_invalid))
            }

            amount !in 1..500 -> _state.update {
                it.copy(error = UiText.StringResource(R.string.charge_amount_range))
            }

            else -> {
                viewModelScope.launch {
                    _state.update { it.copy(isSubmitting = true, error = null) }
                    repository.submitCharge(amount).fold(
                        onSuccess = { charge ->
                            val paymentUrl = charge.alipayURL.orEmpty()
                            if (paymentUrl.isBlank()) {
                                _state.update {
                                    it.copy(
                                        isSubmitting = false,
                                        error = UiText.StringResource(R.string.load_failed)
                                    )
                                }
                                return@fold
                            }
                            _state.update {
                                it.copy(isSubmitting = false, paymentSession = charge, error = null)
                            }
                            _events.emit(
                                ChargeEvent.ShowMessage(
                                    context.getString(R.string.charge_opening_alipay)
                                )
                            )
                        },
                        onFailure = { e ->
                            _state.update {
                                it.copy(
                                    isSubmitting = false,
                                    error = e.message?.takeIf(String::isNotBlank)?.let(UiText::DynamicString)
                                        ?: UiText.StringResource(R.string.load_failed)
                                )
                            }
                        }
                    )
                }
            }
        }
    }

    fun clearPaymentPage() {
        _state.update { it.copy(paymentSession = null) }
    }
}
