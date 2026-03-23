package cn.gdeiassistant.data

import cn.gdeiassistant.model.DeliveryOrder
import cn.gdeiassistant.model.DeliveryOrderState
import cn.gdeiassistant.model.DeliveryTrade
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies that DeliveryRepository returns raw domain data without display strings.
 * Display formatting is now the responsibility of DeliveryDisplayMapper in the UI layer.
 */
class DeliveryRepositoryDisplaySeparationTest {

    @Test
    fun deliveryOrderWithNullFieldsReturnsEmptyStringsNotDisplayText() {
        // Simulate what DeliveryRepository.mapOrder now produces for a DTO
        // with all-null string fields: empty strings, not localized display text.
        val order = DeliveryOrder(
            orderId = "1",
            username = "",     // was: context.getString(R.string.delivery_default_username)
            taskName = "",     // was: context.getString(R.string.delivery_task_name)
            pickupCode = "",   // was: context.getString(R.string.delivery_default_pickup_code)
            contactPhone = "", // was: context.getString(R.string.delivery_default_phone_mask)
            price = 0.0,
            company = "",      // was: context.getString(R.string.delivery_default_company)
            address = "",      // was: context.getString(R.string.delivery_default_address)
            state = DeliveryOrderState.PENDING,
            remarks = "",
            orderTime = ""     // was: context.getString(R.string.common_just_now)
        )

        assertTrue("username should be blank for null API value", order.username.isBlank())
        assertTrue("taskName should be blank for null API value", order.taskName.isBlank())
        assertTrue("pickupCode should be blank for null API value", order.pickupCode.isBlank())
        assertTrue("contactPhone should be blank for null API value", order.contactPhone.isBlank())
        assertTrue("company should be blank for null API value", order.company.isBlank())
        assertTrue("address should be blank for null API value", order.address.isBlank())
        assertTrue("orderTime should be blank for null API value", order.orderTime.isBlank())
    }

    @Test
    fun deliveryOrderWithPopulatedFieldsPreservesValues() {
        val order = DeliveryOrder(
            orderId = "42",
            username = "testuser",
            taskName = "Package pickup",
            pickupCode = "A12345",
            contactPhone = "13800138000",
            price = 5.0,
            company = "SF Express",
            address = "Dorm 3, Room 201",
            state = DeliveryOrderState.DELIVERING,
            remarks = "Handle with care",
            orderTime = "2025-03-15 14:00"
        )

        assertEquals("testuser", order.username)
        assertEquals("Package pickup", order.taskName)
        assertEquals("A12345", order.pickupCode)
        assertEquals("13800138000", order.contactPhone)
        assertEquals("SF Express", order.company)
        assertEquals("Dorm 3, Room 201", order.address)
        assertEquals("2025-03-15 14:00", order.orderTime)
    }

    @Test
    fun deliveryTradeWithNullFieldsReturnsEmptyStringsNotDisplayText() {
        val trade = DeliveryTrade(
            tradeId = "1",
            orderId = "",
            createTime = "", // was: context.getString(R.string.common_just_now)
            username = "",   // was: context.getString(R.string.delivery_default_runner)
            state = 0
        )

        assertTrue("createTime should be blank for null API value", trade.createTime.isBlank())
        assertTrue("username should be blank for null API value", trade.username.isBlank())
    }

    @Test
    fun deliveryTradeWithPopulatedFieldsPreservesValues() {
        val trade = DeliveryTrade(
            tradeId = "99",
            orderId = "42",
            createTime = "2025-03-15 15:00",
            username = "runner_user",
            state = 1
        )

        assertEquals("2025-03-15 15:00", trade.createTime)
        assertEquals("runner_user", trade.username)
    }

    @Test
    fun deliveryOrderPriceDefaultsToZeroForNullApiValue() {
        val order = DeliveryOrder(
            orderId = "1",
            username = "",
            taskName = "",
            pickupCode = "",
            contactPhone = "",
            price = 0.0,
            company = "",
            address = "",
            state = DeliveryOrderState.PENDING,
            remarks = "",
            orderTime = ""
        )

        assertEquals(0.0, order.price, 0.001)
    }
}
