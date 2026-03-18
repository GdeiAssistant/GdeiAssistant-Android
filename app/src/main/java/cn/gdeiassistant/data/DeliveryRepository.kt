package cn.gdeiassistant.data

import android.content.Context
import cn.gdeiassistant.R
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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeliveryRepository @Inject constructor(
    @ApplicationContext private val context: Context,
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
        runCatching {
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
                val detail = dto ?: throw IllegalStateException(context.getString(R.string.delivery_detail_missing))
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

    suspend fun publish(draft: DeliveryDraft): Result<Unit> = withContext(Dispatchers.IO) {
        safeJsonResultCall {
            deliveryApi.publish(
                name = context.getString(R.string.delivery_task_name),
                number = draft.pickupNumber.ifBlank { context.getString(R.string.delivery_default_pickup_code) },
                phone = draft.phone,
                price = String.format("%.2f", draft.price),
                company = draft.pickupPlace,
                address = draft.address,
                remarks = draft.remarks
            )
        }
    }

    private fun mapMine(dto: DeliveryMineDto?): DeliveryMineSummary {
        return DeliveryMineSummary(
            published = dto?.published.orEmpty().map(::mapOrder),
            accepted = dto?.accepted.orEmpty().map(::mapOrder)
        )
    }

    private fun mapDetail(dto: DeliveryDetailDto): DeliveryOrderDetail {
        val order = dto.order ?: throw IllegalStateException(context.getString(R.string.delivery_detail_missing))
        return DeliveryOrderDetail(
            order = mapOrder(order),
            detailType = dto.detailType ?: 1,
            trade = dto.trade?.let(::mapTrade)
        )
    }

    private fun mapOrder(dto: DeliveryOrderDto): DeliveryOrder {
        return DeliveryOrder(
            orderId = dto.orderId?.toString() ?: System.nanoTime().toString(),
            username = dto.username.orEmpty().ifBlank { context.getString(R.string.delivery_default_username) },
            taskName = dto.name.orEmpty().ifBlank { context.getString(R.string.delivery_task_name) },
            pickupCode = dto.number.orEmpty().ifBlank { context.getString(R.string.delivery_default_pickup_code) },
            contactPhone = dto.phone.orEmpty().ifBlank { context.getString(R.string.delivery_default_phone_mask) },
            price = dto.price ?: 0.0,
            company = dto.company.orEmpty().ifBlank { context.getString(R.string.delivery_default_company) },
            address = dto.address.orEmpty().ifBlank { context.getString(R.string.delivery_default_address) },
            state = DeliveryOrderState.fromRemote(dto.state),
            remarks = dto.remarks.orEmpty(),
            orderTime = dto.orderTime.orEmpty().ifBlank { context.getString(R.string.common_just_now) }
        )
    }

    private fun mapTrade(dto: DeliveryTradeDto): DeliveryTrade {
        return DeliveryTrade(
            tradeId = dto.tradeId?.toString() ?: System.nanoTime().toString(),
            orderId = dto.orderId?.toString() ?: "",
            createTime = dto.createTime.orEmpty().ifBlank { context.getString(R.string.common_just_now) },
            username = dto.username.orEmpty().ifBlank { context.getString(R.string.delivery_default_runner) },
            state = dto.state ?: 0
        )
    }
}
