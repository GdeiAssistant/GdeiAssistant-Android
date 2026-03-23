package cn.gdeiassistant.data.mapper

import cn.gdeiassistant.R
import cn.gdeiassistant.data.text.DisplayTextResolver
import cn.gdeiassistant.model.DeliveryMineSummary
import cn.gdeiassistant.model.DeliveryOrder
import cn.gdeiassistant.model.DeliveryOrderDetail
import cn.gdeiassistant.model.DeliveryTrade
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Applies display-layer formatting to raw delivery domain objects returned by
 * DeliveryRepository. Resolves localized fallback strings that were previously
 * baked into the repository layer.
 */
@Singleton
class DeliveryDisplayMapper @Inject constructor(
    private val textResolver: DisplayTextResolver
) {

    fun applyOrderDefaults(order: DeliveryOrder): DeliveryOrder {
        return order.copy(
            username = order.username.ifBlank { textResolver.getString(R.string.delivery_default_username) },
            taskName = order.taskName.ifBlank { textResolver.getString(R.string.delivery_task_name) },
            pickupCode = order.pickupCode.ifBlank { textResolver.getString(R.string.delivery_default_pickup_code) },
            contactPhone = order.contactPhone.ifBlank { textResolver.getString(R.string.delivery_default_phone_mask) },
            company = order.company.ifBlank { textResolver.getString(R.string.delivery_default_company) },
            address = order.address.ifBlank { textResolver.getString(R.string.delivery_default_address) },
            orderTime = order.orderTime.ifBlank { textResolver.getString(R.string.common_just_now) }
        )
    }

    fun applyOrderDefaults(orders: List<DeliveryOrder>): List<DeliveryOrder> {
        return orders.map(::applyOrderDefaults)
    }

    fun applyTradeDefaults(trade: DeliveryTrade): DeliveryTrade {
        return trade.copy(
            createTime = trade.createTime.ifBlank { textResolver.getString(R.string.common_just_now) },
            username = trade.username.ifBlank { textResolver.getString(R.string.delivery_default_runner) }
        )
    }

    fun applyDetailDefaults(detail: DeliveryOrderDetail): DeliveryOrderDetail {
        return detail.copy(
            order = applyOrderDefaults(detail.order),
            trade = detail.trade?.let(::applyTradeDefaults)
        )
    }

    fun applyMineSummaryDefaults(summary: DeliveryMineSummary): DeliveryMineSummary {
        return summary.copy(
            published = applyOrderDefaults(summary.published),
            accepted = applyOrderDefaults(summary.accepted)
        )
    }
}
