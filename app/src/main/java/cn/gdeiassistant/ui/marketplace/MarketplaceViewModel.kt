package cn.gdeiassistant.ui.marketplace

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.R
import cn.gdeiassistant.data.MarketplaceRepository
import cn.gdeiassistant.model.MarketplaceDraft
import cn.gdeiassistant.model.MarketplaceItem
import cn.gdeiassistant.model.MarketplaceTypeOption
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

data class MarketplaceUiState(
    val query: String = "",
    val typeOptions: List<MarketplaceTypeOption> = emptyList(),
    val selectedTypeId: Int? = null,
    val items: List<MarketplaceItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class MarketplacePublishUiState(
    val typeOptions: List<MarketplaceTypeOption> = emptyList(),
    val isSubmitting: Boolean = false,
    val error: String? = null
)

sealed interface MarketplacePublishEvent {
    data class ShowMessage(val message: String) : MarketplacePublishEvent
    data object Submitted : MarketplacePublishEvent
}

@HiltViewModel
class MarketplaceViewModel @Inject constructor(
    private val marketplaceRepository: MarketplaceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(
        MarketplaceUiState(
            typeOptions = marketplaceRepository.currentTypeOptions(),
            isLoading = true
        )
    )
    val state: StateFlow<MarketplaceUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun updateQuery(value: String) {
        _state.update { it.copy(query = value) }
    }

    fun clearQuery() {
        _state.update { it.copy(query = "") }
        refresh()
    }

    fun selectType(typeId: Int?) {
        if (_state.value.selectedTypeId == typeId) return
        _state.update { it.copy(selectedTypeId = typeId, query = "") }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val typeOptions = marketplaceRepository.getTypeOptions()
                .getOrDefault(_state.value.typeOptions.ifEmpty { marketplaceRepository.currentTypeOptions() })
            val keyword = _state.value.query.trim().takeIf { it.isNotBlank() }
            val typeId = if (keyword != null) null else _state.value.selectedTypeId
            marketplaceRepository.getItems(typeId = typeId, keyword = keyword)
                .onSuccess { items ->
                    _state.update { it.copy(typeOptions = typeOptions, items = items, isLoading = false, error = null) }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            typeOptions = typeOptions,
                            items = emptyList(),
                            isLoading = false,
                            error = error.message
                        )
                    }
                }
        }
    }
}

@HiltViewModel
class MarketplacePublishViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val marketplaceRepository: MarketplaceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(
        MarketplacePublishUiState(typeOptions = marketplaceRepository.currentTypeOptions())
    )
    val state: StateFlow<MarketplacePublishUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<MarketplacePublishEvent>()
    val events: SharedFlow<MarketplacePublishEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            val typeOptions = marketplaceRepository.getTypeOptions()
                .getOrDefault(_state.value.typeOptions.ifEmpty { marketplaceRepository.currentTypeOptions() })
            _state.update { it.copy(typeOptions = typeOptions) }
        }
    }

    fun submit(
        title: String,
        priceText: String,
        description: String,
        location: String,
        typeId: Int,
        qq: String,
        phone: String,
        images: List<Uri>
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
            images.isEmpty() -> emitMessage(context.getString(R.string.marketplace_publish_images_required))
            else -> {
                viewModelScope.launch {
                    _state.update { it.copy(isSubmitting = true, error = null) }
                    marketplaceRepository.publish(
                        draft = MarketplaceDraft(
                            title = normalizedTitle,
                            price = price,
                            description = normalizedDescription,
                            location = normalizedLocation,
                            typeId = typeId,
                            qq = normalizedQq,
                            phone = normalizedPhone.ifBlank { null }
                        ),
                        images = images
                    ).onSuccess {
                        _state.update { it.copy(isSubmitting = false, error = null) }
                        _events.emit(MarketplacePublishEvent.ShowMessage(context.getString(R.string.marketplace_publish_success)))
                        _events.emit(MarketplacePublishEvent.Submitted)
                    }.onFailure { error ->
                        _state.update { it.copy(isSubmitting = false, error = error.message) }
                        emitMessage(error.message ?: context.getString(R.string.marketplace_publish_failed))
                    }
                }
            }
        }
    }

    private fun emitMessage(message: String) {
        viewModelScope.launch {
            _events.emit(MarketplacePublishEvent.ShowMessage(message))
        }
    }
}
