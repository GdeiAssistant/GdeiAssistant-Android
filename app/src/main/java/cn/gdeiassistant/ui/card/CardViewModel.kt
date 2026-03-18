package cn.gdeiassistant.ui.card

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.data.CardRepository
import cn.gdeiassistant.model.Card
import cn.gdeiassistant.model.CardInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class CardUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val cardInfo: CardInfo? = null,
    val recordsByMonth: Map<String, List<Card>> = emptyMap(),
    val selectedDate: LocalDate? = null
) {
    val balanceText: String
        get() = cardInfo?.cardBalance ?: "—"

    val cardNumberText: String
        get() = cardInfo?.cardNumber ?: "—"
}

@HiltViewModel
class CardViewModel @Inject constructor(
    private val repository: CardRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CardUiState(isLoading = true))
    val state: StateFlow<CardUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        val date = _state.value.selectedDate
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val infoResult = repository.getCardInfo()
            val recordsResult = repository.getRecords(
                year = date?.year,
                month = date?.monthValue,
                date = date?.dayOfMonth
            )

            val info = infoResult.getOrNull()
            val records = recordsResult.getOrNull()?.cardList.orEmpty()
            val grouped = repository.groupRecordsByMonth(records)

            val errorMsg = infoResult.exceptionOrNull()?.message
                ?: recordsResult.exceptionOrNull()?.message

            _state.update {
                it.copy(
                    isLoading = false,
                    error = errorMsg,
                    cardInfo = info,
                    recordsByMonth = grouped
                )
            }
        }
    }

    fun queryByDate(date: LocalDate?) {
        _state.update { it.copy(selectedDate = date) }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val recordsResult = repository.getRecords(
                year = date?.year,
                month = date?.monthValue,
                date = date?.dayOfMonth
            )
            val records = recordsResult.getOrNull()?.cardList.orEmpty()
            val grouped = repository.groupRecordsByMonth(records)
            val errorMsg = recordsResult.exceptionOrNull()?.message
            _state.update {
                it.copy(
                    isLoading = false,
                    error = errorMsg,
                    recordsByMonth = grouped
                )
            }
        }
    }
}
