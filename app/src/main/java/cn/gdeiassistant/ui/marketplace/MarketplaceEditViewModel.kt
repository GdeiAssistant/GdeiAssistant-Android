package cn.gdeiassistant.ui.marketplace

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.R
import cn.gdeiassistant.data.MarketplaceRepository
import cn.gdeiassistant.model.MarketplaceEditableItem
import cn.gdeiassistant.model.MarketplaceTypeOption
import cn.gdeiassistant.model.MarketplaceUpdateDraft
import cn.gdeiassistant.ui.navigation.Routes
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

data class MarketplaceEditUiState(
    val item: MarketplaceEditableItem? = null,
    val typeOptions: List<MarketplaceTypeOption> = emptyList(),
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val error: String? = null
)

sealed interface MarketplaceEditEvent {
    data class ShowMessage(val message: String) : MarketplaceEditEvent
    data object Submitted : MarketplaceEditEvent
}

@HiltViewModel
class MarketplaceEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val marketplaceRepository: MarketplaceRepository
) : ViewModel() {

    private val itemId: String = savedStateHandle.get<String>(Routes.MARKETPLACE_EDIT_ITEM_ID).orEmpty()

    private val _state = MutableStateFlow(
        MarketplaceEditUiState(
            typeOptions = marketplaceRepository.currentTypeOptions()
        )
    )
    val state: StateFlow<MarketplaceEditUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<MarketplaceEditEvent>()
    val events: SharedFlow<MarketplaceEditEvent> = _events.asSharedFlow()

    init {
        refresh()
    }

    fun refresh() {
        if (itemId.isBlank()) {
            _state.update {
                it.copy(
                    isLoading = false,
                    error = context.getString(R.string.marketplace_edit_missing)
                )
            }
            return
        }
        viewModelScope.launch {
            val typeOptions = marketplaceRepository.getTypeOptions()
                .getOrDefault(_state.value.typeOptions.ifEmpty { marketplaceRepository.currentTypeOptions() })
            _state.update { it.copy(typeOptions = typeOptions, isLoading = true, error = null) }
            marketplaceRepository.getEditableItem(itemId)
                .onSuccess { item ->
                    _state.update { it.copy(item = item, typeOptions = typeOptions, isLoading = false, error = null) }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            item = null,
                            typeOptions = typeOptions,
                            isLoading = false,
                            error = error.message
                        )
                    }
                }
        }
    }

    fun submit(
        title: String,
        priceText: String,
        description: String,
        location: String,
        typeId: Int,
        qq: String,
        phone: String
    ) {
        val normalizedTitle = title.trim()
        val normalizedDescription = description.trim()
        val normalizedLocation = location.trim()
        val normalizedQq = qq.trim()
        val normalizedPhone = phone.trim()
        val price = priceText.trim().toDoubleOrNull()

        when {
            normalizedTitle.isBlank() -> emitMessage(context.getString(R.string.marketplace_publish_title_required))
            normalizedTitle.length > 25 -> emitMessage(context.getString(R.string.marketplace_publish_title_too_long))
            price == null || price <= 0.0 || price > 9999.99 -> emitMessage(context.getString(R.string.marketplace_publish_price_invalid))
            normalizedDescription.isBlank() -> emitMessage(context.getString(R.string.marketplace_publish_description_required))
            normalizedDescription.length > 100 -> emitMessage(context.getString(R.string.marketplace_publish_description_too_long))
            normalizedLocation.isBlank() -> emitMessage(context.getString(R.string.marketplace_publish_location_required))
            normalizedLocation.length > 30 -> emitMessage(context.getString(R.string.marketplace_publish_location_too_long))
            normalizedQq.isBlank() -> emitMessage(context.getString(R.string.marketplace_publish_qq_required))
            normalizedQq.length > 20 -> emitMessage(context.getString(R.string.marketplace_publish_qq_too_long))
            normalizedPhone.length > 11 -> emitMessage(context.getString(R.string.marketplace_publish_phone_too_long))
            else -> {
                viewModelScope.launch {
                    _state.update { it.copy(isSubmitting = true, error = null) }
                    marketplaceRepository.updateItem(
                        id = itemId,
                        draft = MarketplaceUpdateDraft(
                            title = normalizedTitle,
                            price = price,
                            description = normalizedDescription,
                            location = normalizedLocation,
                            typeId = typeId,
                            qq = normalizedQq,
                            phone = normalizedPhone.ifBlank { null }
                        )
                    ).onSuccess {
                        _state.update { it.copy(isSubmitting = false, error = null) }
                        _events.emit(MarketplaceEditEvent.ShowMessage(context.getString(R.string.marketplace_edit_success)))
                        _events.emit(MarketplaceEditEvent.Submitted)
                    }.onFailure { error ->
                        _state.update { it.copy(isSubmitting = false, error = error.message) }
                        emitMessage(error.message ?: context.getString(R.string.marketplace_edit_failed))
                    }
                }
            }
        }
    }

    private fun emitMessage(message: String) {
        viewModelScope.launch {
            _events.emit(MarketplaceEditEvent.ShowMessage(message))
        }
    }
}
