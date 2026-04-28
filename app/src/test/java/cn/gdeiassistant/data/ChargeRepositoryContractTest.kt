package cn.gdeiassistant.data

import android.content.Context
import cn.gdeiassistant.model.Charge
import cn.gdeiassistant.model.ChargeOrder
import cn.gdeiassistant.model.DataJsonResult
import cn.gdeiassistant.network.IdempotencyKeyGenerator
import cn.gdeiassistant.network.api.ChargeApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ChargeRepositoryContractTest {

    private lateinit var context: Context
    private lateinit var chargeApi: ChargeApi
    private lateinit var cardRepository: CardRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var idempotencyKeyGenerator: IdempotencyKeyGenerator
    private lateinit var repository: ChargeRepository

    @Before
    fun setUp() {
        context = mock()
        chargeApi = mock()
        cardRepository = mock()
        sessionManager = mock()
        idempotencyKeyGenerator = mock()
        repository = ChargeRepository(
            context = context,
            chargeApi = chargeApi,
            cardRepository = cardRepository,
            sessionManager = sessionManager,
            idempotencyKeyGenerator = idempotencyKeyGenerator
        )
    }

    @Test
    fun submitChargeSendsGeneratedIdempotencyKey() = runTest {
        whenever(idempotencyKeyGenerator.newKey()).thenReturn("synthetic-idempotency-key")
        whenever(
            chargeApi.submitCharge(
                idempotencyKey = "synthetic-idempotency-key",
                amount = "50",
                password = "synthetic-charge-password"
            )
        ).thenReturn(successfulCharge())

        val result = repository.submitCharge(50, "synthetic-charge-password")

        assertTrue(result.isSuccess)
        verify(chargeApi).submitCharge(
            idempotencyKey = eq("synthetic-idempotency-key"),
            amount = eq("50"),
            password = eq("synthetic-charge-password")
        )
    }

    @Test
    fun submitChargeGeneratesNewKeyForNewUserAttempt() = runTest {
        whenever(idempotencyKeyGenerator.newKey())
            .thenReturn("synthetic-idempotency-key-1", "synthetic-idempotency-key-2")
        whenever(
            chargeApi.submitCharge(
                idempotencyKey = "synthetic-idempotency-key-1",
                amount = "50",
                password = "synthetic-charge-password"
            )
        ).thenReturn(successfulCharge())
        whenever(
            chargeApi.submitCharge(
                idempotencyKey = "synthetic-idempotency-key-2",
                amount = "50",
                password = "synthetic-charge-password"
            )
        ).thenReturn(successfulCharge())

        val first = repository.submitCharge(50, "synthetic-charge-password")
        val second = repository.submitCharge(50, "synthetic-charge-password")

        assertTrue(first.isSuccess)
        assertTrue(second.isSuccess)
        verify(chargeApi).submitCharge(
            idempotencyKey = eq("synthetic-idempotency-key-1"),
            amount = eq("50"),
            password = eq("synthetic-charge-password")
        )
        verify(chargeApi).submitCharge(
            idempotencyKey = eq("synthetic-idempotency-key-2"),
            amount = eq("50"),
            password = eq("synthetic-charge-password")
        )
    }

    @Test
    fun getChargeOrderDelegatesToApiAndReturnsSafeFields() = runTest {
        whenever(chargeApi.getChargeOrder("synthetic-order-id")).thenReturn(
            DataJsonResult(
                success = true,
                code = 200,
                data = ChargeOrder(
                    orderId = "synthetic-order-id",
                    amount = 50,
                    status = "PAYMENT_SESSION_CREATED",
                    message = "支付请求已生成，请完成支付并刷新余额。该状态不代表最终到账。",
                    updatedAt = "2026-04-28 10:01:00"
                )
            )
        )

        val result = repository.getChargeOrder("synthetic-order-id")

        assertTrue(result.isSuccess)
        assertEquals("synthetic-order-id", result.getOrNull()?.orderId)
        assertEquals("PAYMENT_SESSION_CREATED", result.getOrNull()?.status)
        verify(chargeApi).getChargeOrder("synthetic-order-id")
    }

    @Test
    fun getRecentChargeOrdersDelegatesPagingAndStatus() = runTest {
        whenever(chargeApi.getRecentChargeOrders(page = 0, size = 5, status = "UNKNOWN")).thenReturn(
            DataJsonResult(
                success = true,
                code = 200,
                data = listOf(
                    ChargeOrder(
                        orderId = "synthetic-order-unknown",
                        amount = 20,
                        status = "UNKNOWN",
                        retryAfter = 60
                    )
                )
            )
        )

        val result = repository.getRecentChargeOrders(page = 0, size = 5, status = "UNKNOWN")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("synthetic-order-unknown", result.getOrNull()?.firstOrNull()?.orderId)
        verify(chargeApi).getRecentChargeOrders(page = 0, size = 5, status = "UNKNOWN")
    }

    private fun successfulCharge(): DataJsonResult<Charge> {
        return DataJsonResult(
            success = true,
            code = 200,
            message = "",
            data = Charge(
                alipayURL = "https://pay.example.invalid/synthetic-charge",
                orderId = "synthetic-order-id",
                status = "PAYMENT_SESSION_CREATED",
                message = "支付请求已生成，请完成支付并刷新余额。该状态不代表最终到账。"
            )
        )
    }
}
