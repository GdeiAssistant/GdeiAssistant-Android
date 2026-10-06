package cn.gdeiassistant.data

import cn.gdeiassistant.network.cancellableRunCatching
import cn.gdeiassistant.model.DeliveryDraft
import cn.gdeiassistant.model.DeliveryMineSummary
import cn.gdeiassistant.model.DeliveryOrder
import cn.gdeiassistant.model.DeliveryOrderDetail
import cn.gdeiassistant.model.DeliveryOrderState
import cn.gdeiassistant.model.DeliveryTrade
import cn.gdeiassistant.network.api.DeliveryApi
import cn.gdeiassistant.network.api.DeliveryDetailDto
import cn.gdeiassistant.network.api.DeliveryMineDto
import cn.gdeiassistant.network.api.DeliveryOrderDto
import cn.gdeiassistant.network.api.DeliveryTradeDto
import cn.gdeiassistant.network.safeApiCall
import cn.gdeiassistant.network.safeJsonResultCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import java.util.Locale


@Singleton
class DeliveryRepository @Inject constructor(
    private val deliveryApi: DeliveryApi
) {

    suspend fun getOrders(): Result<List<DeliveryOrder>> = withContext(Dispatchers.IO) {
        safeApiCall { deliveryApi.getOrders(start = 0, size = 20) }
            .mapCatching { items ->
                items.orEmpty()
                    .map(::mapOrder)
                    .sortedByDescending { it.orderTime }
            }
    }

    suspend fun getMine(): Result<DeliveryMineSummary> = withContext(Dispatchers.IO) {
        safeApiCall { deliveryApi.getMine() }
            .mapCatching { dto -> mapMine(dto) }
    }

    suspend fun getCombined(): Result<Pair<List<DeliveryOrder>, DeliveryMineSummary>> = withContext(Dispatchers.IO) {
        cancellableRunCatching {
            coroutineScope {
                val ordersDeferred = async { getOrders() }
                val mineDeferred = async { getMine() }
                Pair(
                    ordersDeferred.await().getOrThrow(),
                    mineDeferred.await().getOrThrow()
                )
            }
        }
    }

    suspend fun getDetail(orderId: String): Result<DeliveryOrderDetail> = withContext(Dispatchers.IO) {
        safeApiCall { deliveryApi.getDetail(orderId) }
            .mapCatching { dto ->
                val detail = dto ?: throw IllegalStateException("Delivery detail not found")
                mapDetail(detail)
            }
    }

    suspend fun acceptOrder(orderId: String): Result<Unit> = withContext(Dispatchers.IO) {
        safeJsonResultCall { deliveryApi.acceptOrder(orderId) }
    }

    suspend fun finishTrade(tradeId: String): Result<Unit> = withContext(Dispatchers.IO) {
        safeJsonResultCall { deliveryApi.finishTrade(tradeId) }
    }

    suspend fun deleteOrder(orderId: String): Result<Unit> = withContext(Dispatchers.IO) {
        safeJsonResultCall { deliveryApi.deleteOrder(orderId) }
    }

    suspend fun publish(draft: DeliveryDraft, taskName: String): Result<Unit> = withContext(Dispatchers.IO) {
        safeJsonResultCall {
            deliveryApi.publish(cn.gdeiassistant.network.api.DeliveryPublishDto(
                taskName = taskName,
                pickupCode = draft.pickupNumber,
                contactPhone = draft.phone,
                price = String.format(Locale.ROOT, "%.2f", draft.price),
                pickupLocation = draft.pickupPlace,
                deliveryAddress = draft.address,
                remarks = draft.remarks
            ))
        }
    }

    private fun mapMine(dto: DeliveryMineDto?): DeliveryMineSummary {
        return DeliveryMineSummary(
            published = dto?.published.orEmpty().map(::mapOrder),
            accepted = dto?.accepted.orEmpty().map(::mapOrder)
        )
    }

    private fun mapDetail(dto: DeliveryDetailDto): DeliveryOrderDetail {
        val order = dto.order ?: throw IllegalStateException("Delivery detail not found")
        return DeliveryOrderDetail(
            order = mapOrder(order),
            detailType = dto.detailType ?: 2,
            trade = dto.trade?.let(::mapTrade)
        )
    }

    private fun mapOrder(dto: DeliveryOrderDto): DeliveryOrder {
        return DeliveryOrder(
            orderId = dto.orderId?.takeIf { it > 0 }?.toString() ?: throw IllegalStateException("Missing delivery order ID"),
            displayName = dto.displayName.orEmpty(),
            taskName = dto.taskName.orEmpty(),
            pickupCode = dto.pickupCode.orEmpty(),
            contactPhone = dto.contactPhone.orEmpty(),
            price = dto.price ?: 0.0,
            company = dto.pickupLocation.orEmpty(),
            address = dto.deliveryAddress.orEmpty(),
            state = DeliveryOrderState.fromRemote(dto.state),
            remarks = dto.remarks.orEmpty(),
            orderTime = dto.orderTime.orEmpty()
        )
    }

    private fun mapTrade(dto: DeliveryTradeDto): DeliveryTrade {
        return DeliveryTrade(
            tradeId = dto.tradeId?.takeIf { it > 0 }?.toString() ?: throw IllegalStateException("Missing delivery trade ID"),
            orderId = dto.orderId?.takeIf { it > 0 }?.toString() ?: throw IllegalStateException("Missing delivery trade order ID"),
            createTime = dto.createTime.orEmpty(),
            displayName = dto.displayName.orEmpty(),
            state = dto.state ?: -1
        )
    }
}
