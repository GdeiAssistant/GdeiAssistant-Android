package cn.gdeiassistant.ui.delivery

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.R
import cn.gdeiassistant.data.DeliveryRepository
import cn.gdeiassistant.data.mapper.DeliveryDisplayMapper
import cn.gdeiassistant.model.DeliveryDraft
import cn.gdeiassistant.model.DeliveryMineSummary
import cn.gdeiassistant.model.DeliveryOrder
import cn.gdeiassistant.model.DeliveryOrderDetail
import cn.gdeiassistant.model.DeliveryOrderState
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

enum class DeliveryFilter {
    ALL, PENDING, DELIVERING, COMPLETED
}

enum class DeliveryMineTab {
    PUBLISHED, ACCEPTED
}

data class DeliveryUiState(
    val orders: List<DeliveryOrder> = emptyList(),
    val mine: DeliveryMineSummary = DeliveryMineSummary(emptyList(), emptyList()),
    val isLoading: Boolean = true,
    val error: String? = null
)

data class DeliveryDetailUiState(
    val detail: DeliveryOrderDetail? = null,
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val error: String? = null
) 

data class DeliveryMineUiState(
    val summary: DeliveryMineSummary = DeliveryMineSummary(emptyList(), emptyList()),
    val selectedTab: DeliveryMineTab = DeliveryMineTab.PUBLISHED,
    val selectedFilter: DeliveryFilter = DeliveryFilter.ALL,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val visibleOrders: List<DeliveryOrder>
        get() {
            val base = when (selectedTab) {
                DeliveryMineTab.PUBLISHED -> summary.published
                DeliveryMineTab.ACCEPTED -> summary.accepted
            }
            return base.filterBy(selectedFilter)
        }
}

data class DeliveryPublishUiState(
    val isSubmitting: Boolean = false
)

sealed interface DeliveryEvent {
    data class ShowMessage(val message: String) : DeliveryEvent
    data object Published : DeliveryEvent
}

private fun List<DeliveryOrder>.filterBy(filter: DeliveryFilter): List<DeliveryOrder> {
    return when (filter) {
        DeliveryFilter.ALL -> this
        DeliveryFilter.PENDING -> filter { it.state == DeliveryOrderState.PENDING }
        DeliveryFilter.DELIVERING -> filter { it.state == DeliveryOrderState.DELIVERING }
        DeliveryFilter.COMPLETED -> filter { it.state == DeliveryOrderState.COMPLETED }
    }
}

@HiltViewModel
class DeliveryViewModel @Inject constructor(
    private val deliveryRepository: DeliveryRepository,
    private val displayMapper: DeliveryDisplayMapper
) : ViewModel() {

    private val _state = MutableStateFlow(DeliveryUiState())
    val state: StateFlow<DeliveryUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            deliveryRepository.getCombined()
                .onSuccess { (orders, mine) ->
                    _state.update {
                        it.copy(
                            orders = displayMapper.applyOrderDefaults(orders),
                            mine = displayMapper.applyMineSummaryDefaults(mine),
                            isLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(orders = emptyList(), isLoading = false, error = error.message) }
                }
        }
    }
}

@HiltViewModel
class DeliveryDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val deliveryRepository: DeliveryRepository,
    private val displayMapper: DeliveryDisplayMapper
) : ViewModel() {

    private val orderId: String = savedStateHandle.get<String>(Routes.DELIVERY_ORDER_ID).orEmpty()

    private val _state = MutableStateFlow(DeliveryDetailUiState())
    val state: StateFlow<DeliveryDetailUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<DeliveryEvent>()
    val events: SharedFlow<DeliveryEvent> = _events.asSharedFlow()

    init {
        refresh()
    }

    fun refresh() {
        if (orderId.isBlank()) {
            _state.update { it.copy(isLoading = false, error = context.getString(R.string.delivery_detail_missing)) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            deliveryRepository.getDetail(orderId)
                .onSuccess { detail ->
                    _state.update { it.copy(detail = displayMapper.applyDetailDefaults(detail), isLoading = false, isSubmitting = false, error = null) }
                }
                .onFailure { error ->
                    _state.update { it.copy(detail = null, isLoading = false, isSubmitting = false, error = error.message) }
                }
        }
    }

    fun acceptOrder() {
        if (orderId.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }
            deliveryRepository.acceptOrder(orderId)
                .onSuccess {
                    _events.emit(DeliveryEvent.ShowMessage(context.getString(R.string.delivery_result_accept)))
                    refresh()
                }
                .onFailure { error ->
                    _events.emit(DeliveryEvent.ShowMessage(error.message ?: context.getString(R.string.delivery_toast_accept_failed)))
                    _state.update { it.copy(isSubmitting = false) }
                }
        }
    }

    fun finishTrade(tradeId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }
            deliveryRepository.finishTrade(tradeId)
                .onSuccess {
                    _events.emit(DeliveryEvent.ShowMessage(context.getString(R.string.delivery_result_finish)))
                    refresh()
                }
                .onFailure { error ->
                    _events.emit(DeliveryEvent.ShowMessage(error.message ?: context.getString(R.string.delivery_toast_finish_failed)))
                    _state.update { it.copy(isSubmitting = false) }
                }
        }
    }
}

@HiltViewModel
class DeliveryMineViewModel @Inject constructor(
    private val deliveryRepository: DeliveryRepository,
    private val displayMapper: DeliveryDisplayMapper
) : ViewModel() {

    private val _state = MutableStateFlow(DeliveryMineUiState())
    val state: StateFlow<DeliveryMineUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun selectTab(tab: DeliveryMineTab) {
        _state.update { it.copy(selectedTab = tab) }
    }

    fun selectFilter(filter: DeliveryFilter) {
        _state.update { it.copy(selectedFilter = filter) }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            deliveryRepository.getMine()
                .onSuccess { summary ->
                    _state.update { it.copy(summary = displayMapper.applyMineSummaryDefaults(summary), isLoading = false, error = null) }
                }
                .onFailure { error ->
                    _state.update { it.copy(summary = DeliveryMineSummary(emptyList(), emptyList()), isLoading = false, error = error.message) }
                }
        }
    }
}

@HiltViewModel
class DeliveryPublishViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val deliveryRepository: DeliveryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DeliveryPublishUiState())
    val state: StateFlow<DeliveryPublishUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<DeliveryEvent>()
    val events: SharedFlow<DeliveryEvent> = _events.asSharedFlow()

    fun publish(
        pickupPlace: String,
        pickupNumber: String,
        phone: String,
        rewardText: String,
        address: String,
        remarks: String
    ) {
        val trimmedPickupPlace = pickupPlace.trim()
        val trimmedPickupNumber = pickupNumber.trim()
        val trimmedPhone = phone.trim()
        val trimmedAddress = address.trim()
        val trimmedRemarks = remarks.trim()

        when {
            trimmedPickupPlace.isBlank() -> emitMessage(context.getString(R.string.delivery_publish_pickup_place_required))
            trimmedPickupPlace.length > 10 -> emitMessage(context.getString(R.string.delivery_publish_pickup_place_limit))
            trimmedPhone.isBlank() -> emitMessage(context.getString(R.string.delivery_publish_phone_required))
            trimmedPhone.length > 11 -> emitMessage(context.getString(R.string.delivery_publish_phone_limit))
            trimmedAddress.isBlank() -> emitMessage(context.getString(R.string.delivery_publish_address_required))
            trimmedAddress.length > 50 -> emitMessage(context.getString(R.string.delivery_publish_address_limit))
            trimmedRemarks.length > 100 -> emitMessage(context.getString(R.string.delivery_publish_remarks_limit))
            else -> {
                val reward = rewardText.trim().toDoubleOrNull()
                if (reward == null || reward <= 0 || reward > 9999.99) {
                    emitMessage(context.getString(R.string.delivery_publish_reward_invalid))
                    return
                }
                viewModelScope.launch {
                    _state.update { it.copy(isSubmitting = true) }
                    deliveryRepository.publish(
                        draft = DeliveryDraft(
                            pickupPlace = trimmedPickupPlace,
                            pickupNumber = trimmedPickupNumber,
                            phone = trimmedPhone,
                            price = reward,
                            address = trimmedAddress,
                            remarks = trimmedRemarks
                        ),
                        taskName = context.getString(R.string.delivery_task_name),
                        defaultPickupCode = context.getString(R.string.delivery_default_pickup_code)
                    )
                        .onSuccess {
                            _state.update { it.copy(isSubmitting = false) }
                            _events.emit(DeliveryEvent.ShowMessage(context.getString(R.string.delivery_publish_success)))
                            _events.emit(DeliveryEvent.Published)
                        }
                        .onFailure { error ->
                            _state.update { it.copy(isSubmitting = false) }
                            _events.emit(
                                DeliveryEvent.ShowMessage(
                                    error.message ?: context.getString(R.string.delivery_publish_failed)
                                )
                            )
                        }
                }
            }
        }
    }

    private fun emitMessage(message: String) {
        viewModelScope.launch {
            _events.emit(DeliveryEvent.ShowMessage(message))
        }
    }
}
