package cn.gdeiassistant.ui.charge

import android.content.Context
import cn.gdeiassistant.R
import cn.gdeiassistant.data.ChargeRepository
import cn.gdeiassistant.model.CardInfo
import cn.gdeiassistant.model.Charge
import cn.gdeiassistant.model.ChargeOrder
import cn.gdeiassistant.ui.util.UiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ChargeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: ChargeRepository
    private lateinit var context: Context

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
        context = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun refreshLoadsRecentChargeOrders() = runTest(testDispatcher) {
        whenever(repository.getCardInfo()).thenReturn(Result.success(CardInfo(cardBalance = "52.50")))
        whenever(repository.getRecentChargeOrders(page = 0, size = 5)).thenReturn(
            Result.success(
                listOf(
                    ChargeOrder(
                        orderId = "synthetic-order-new",
                        amount = 50,
                        status = "PAYMENT_SESSION_CREATED",
                        message = "支付请求已生成，请完成支付并刷新余额。该状态不代表最终到账。"
                    ),
                    ChargeOrder(
                        orderId = "synthetic-order-processing",
                        amount = 20,
                        status = "PROCESSING",
                        message = "充值请求正在处理中，请稍后查看结果，避免重复提交。"
                    )
                )
            )
        )

        val viewModel = ChargeViewModel(repository, context)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertFalse(state.isLoadingOrders)
        assertEquals(2, state.recentOrders.size)
        assertEquals("synthetic-order-new", state.latestOrder?.orderId)
        verify(repository).getRecentChargeOrders(page = 0, size = 5)
    }

    @Test
    fun submitChargeExposesReturnedOrderStatusWithoutFinalSettlementClaim() = runTest(testDispatcher) {
        whenever(repository.getCardInfo()).thenReturn(Result.success(CardInfo(cardBalance = "52.50")))
        whenever(repository.getRecentChargeOrders(page = 0, size = 5)).thenReturn(Result.success(emptyList()))
        whenever(context.getString(R.string.charge_opening_alipay)).thenReturn("正在启动支付宝，请稍后…")
        whenever(repository.submitCharge(50, "synthetic-charge-password")).thenReturn(
            Result.success(
                Charge(
                    alipayURL = "https://pay.example.invalid/synthetic-charge",
                    orderId = "synthetic-order-id",
                    status = "PAYMENT_SESSION_CREATED",
                    message = "支付请求已生成，请完成支付并刷新余额。该状态不代表最终到账。"
                )
            )
        )

        val viewModel = ChargeViewModel(repository, context)
        advanceUntilIdle()

        viewModel.updateAmount("50")
        viewModel.updatePassword("synthetic-charge-password")
        viewModel.submitCharge()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("synthetic-order-id", state.latestOrder?.orderId)
        assertEquals("PAYMENT_SESSION_CREATED", state.latestOrder?.status)
        assertEquals("synthetic-order-id", state.currentPaymentOrder?.orderId)
        assertEquals("PAYMENT_SESSION_CREATED", state.currentPaymentOrder?.status)
        assertEquals("synthetic-order-id", state.recentOrders.firstOrNull()?.orderId)
        assertFalse(state.latestOrder?.message.orEmpty().contains("到账成功"))
    }

    @Test
    fun submitChargeClearsStaleOrderLoadError() = runTest(testDispatcher) {
        whenever(repository.getCardInfo()).thenReturn(Result.success(CardInfo(cardBalance = "52.50")))
        whenever(repository.getRecentChargeOrders(page = 0, size = 5)).thenReturn(
            Result.failure(IllegalStateException("stale order load failure"))
        )
        whenever(context.getString(R.string.charge_opening_alipay)).thenReturn("正在启动支付宝，请稍后…")
        whenever(repository.submitCharge(50, "synthetic-charge-password")).thenReturn(
            Result.success(
                Charge(
                    alipayURL = "https://pay.example.invalid/synthetic-charge",
                    orderId = "synthetic-order-id",
                    status = "PAYMENT_SESSION_CREATED",
                    message = "支付请求已生成，请完成支付并刷新余额。该状态不代表最终到账。"
                )
            )
        )

        val viewModel = ChargeViewModel(repository, context)
        advanceUntilIdle()
        assertEquals(UiText.StringResource(R.string.charge_order_load_failed), viewModel.state.value.orderError)

        viewModel.updateAmount("50")
        viewModel.updatePassword("synthetic-charge-password")
        viewModel.submitCharge()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(null, state.orderError)
        assertEquals("synthetic-order-id", state.currentPaymentOrder?.orderId)
        assertEquals("synthetic-order-id", state.recentOrders.firstOrNull()?.orderId)
    }

    @Test
    fun refreshUpdatesLatestOrderWhenBackendReturnsSameOrder() = runTest(testDispatcher) {
        whenever(repository.getCardInfo()).thenReturn(Result.success(CardInfo(cardBalance = "52.50")))
        whenever(repository.getRecentChargeOrders(page = 0, size = 5)).thenReturn(
            Result.success(emptyList()),
            Result.success(
                listOf(
                    ChargeOrder(
                        orderId = "synthetic-order-id",
                        amount = 50,
                        status = "PROCESSING",
                        message = "充值请求正在处理中，请稍后查看结果，避免重复提交。"
                    )
                )
            )
        )
        whenever(context.getString(R.string.charge_opening_alipay)).thenReturn("正在启动支付宝，请稍后…")
        whenever(repository.submitCharge(50, "synthetic-charge-password")).thenReturn(
            Result.success(
                Charge(
                    alipayURL = "https://pay.example.invalid/synthetic-charge",
                    orderId = "synthetic-order-id",
                    status = "PAYMENT_SESSION_CREATED",
                    message = "支付请求已生成，请完成支付并刷新余额。该状态不代表最终到账。"
                )
            )
        )

        val viewModel = ChargeViewModel(repository, context)
        advanceUntilIdle()

        viewModel.updateAmount("50")
        viewModel.updatePassword("synthetic-charge-password")
        viewModel.submitCharge()
        advanceUntilIdle()
        assertEquals("PAYMENT_SESSION_CREATED", viewModel.state.value.latestOrder?.status)
        assertEquals("PAYMENT_SESSION_CREATED", viewModel.state.value.currentPaymentOrder?.status)

        viewModel.refreshChargeOrders()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("PROCESSING", state.latestOrder?.status)
        assertEquals("PROCESSING", state.currentPaymentOrder?.status)
        assertEquals("PROCESSING", state.recentOrders.firstOrNull()?.status)
    }

    @Test
    fun submitChargeWithoutOrderDataDoesNotReuseStaleLatestOrderForPayment() = runTest(testDispatcher) {
        val staleLatestOrder = ChargeOrder(
            orderId = "synthetic-stale-order",
            amount = 20,
            status = "UNKNOWN",
            message = "充值状态暂无法确认，请稍后查看订单状态，避免重复提交。"
        )
        whenever(repository.getCardInfo()).thenReturn(Result.success(CardInfo(cardBalance = "52.50")))
        whenever(repository.getRecentChargeOrders(page = 0, size = 5)).thenReturn(Result.success(listOf(staleLatestOrder)))
        whenever(context.getString(R.string.charge_opening_alipay)).thenReturn("正在启动支付宝，请稍后…")
        whenever(repository.submitCharge(50, "synthetic-charge-password")).thenReturn(
            Result.success(
                Charge(
                    alipayURL = "https://pay.example.invalid/synthetic-charge"
                )
            )
        )

        val viewModel = ChargeViewModel(repository, context)
        advanceUntilIdle()
        assertEquals("synthetic-stale-order", viewModel.state.value.latestOrder?.orderId)

        viewModel.updateAmount("50")
        viewModel.updatePassword("synthetic-charge-password")
        viewModel.submitCharge()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("synthetic-stale-order", state.latestOrder?.orderId)
        assertNull(state.currentPaymentOrder)
        assertNotNull(state.paymentSession)
    }

    @Test
    fun refreshWithDifferentOrderUpdatesLatestWithoutReplacingCurrentPaymentOrder() = runTest(testDispatcher) {
        whenever(repository.getCardInfo()).thenReturn(Result.success(CardInfo(cardBalance = "52.50")))
        whenever(repository.getRecentChargeOrders(page = 0, size = 5)).thenReturn(
            Result.success(emptyList()),
            Result.success(
                listOf(
                    ChargeOrder(
                        orderId = "synthetic-different-order",
                        amount = 50,
                        status = "UNKNOWN",
                        message = "充值状态暂无法确认，请稍后查看订单状态，避免重复提交。"
                    )
                )
            )
        )
        whenever(context.getString(R.string.charge_opening_alipay)).thenReturn("正在启动支付宝，请稍后…")
        whenever(repository.submitCharge(50, "synthetic-charge-password")).thenReturn(
            Result.success(
                Charge(
                    alipayURL = "https://pay.example.invalid/synthetic-charge",
                    orderId = "synthetic-current-order",
                    status = "PAYMENT_SESSION_CREATED",
                    message = "支付请求已生成，请完成支付并刷新余额。该状态不代表最终到账。"
                )
            )
        )

        val viewModel = ChargeViewModel(repository, context)
        advanceUntilIdle()

        viewModel.updateAmount("50")
        viewModel.updatePassword("synthetic-charge-password")
        viewModel.submitCharge()
        advanceUntilIdle()
        assertEquals("synthetic-current-order", viewModel.state.value.latestOrder?.orderId)
        assertEquals("synthetic-current-order", viewModel.state.value.currentPaymentOrder?.orderId)

        viewModel.refreshChargeOrders()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("synthetic-different-order", state.latestOrder?.orderId)
        assertEquals("UNKNOWN", state.latestOrder?.status)
        assertEquals("synthetic-current-order", state.currentPaymentOrder?.orderId)
        assertEquals("PAYMENT_SESSION_CREATED", state.currentPaymentOrder?.status)
    }

    @Test
    fun newChargeSubmissionClearsPreviousCurrentPaymentOrderAndOrderError() = runTest(testDispatcher) {
        whenever(repository.getCardInfo()).thenReturn(Result.success(CardInfo(cardBalance = "52.50")))
        whenever(repository.getRecentChargeOrders(page = 0, size = 5)).thenReturn(
            Result.success(emptyList()),
            Result.failure(IllegalStateException("raw order refresh failure"))
        )
        whenever(context.getString(R.string.charge_opening_alipay)).thenReturn("正在启动支付宝，请稍后…")
        whenever(repository.submitCharge(50, "synthetic-charge-password")).thenReturn(
            Result.success(
                Charge(
                    alipayURL = "https://pay.example.invalid/synthetic-charge",
                    orderId = "synthetic-first-order",
                    status = "PAYMENT_SESSION_CREATED",
                    message = "支付请求已生成，请完成支付并刷新余额。该状态不代表最终到账。"
                )
            ),
            Result.success(
                Charge(
                    alipayURL = "https://pay.example.invalid/synthetic-charge-next"
                )
            )
        )

        val viewModel = ChargeViewModel(repository, context)
        advanceUntilIdle()

        viewModel.updateAmount("50")
        viewModel.updatePassword("synthetic-charge-password")
        viewModel.submitCharge()
        advanceUntilIdle()
        assertEquals("synthetic-first-order", viewModel.state.value.currentPaymentOrder?.orderId)

        viewModel.refreshChargeOrders()
        advanceUntilIdle()
        assertEquals(UiText.StringResource(R.string.charge_order_load_failed), viewModel.state.value.orderError)

        viewModel.submitCharge()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertNull(state.currentPaymentOrder)
        assertNull(state.orderError)
        assertEquals("synthetic-first-order", state.latestOrder?.orderId)
    }

    @Test
    fun chargeErrorsUseSafeUiMessages() = runTest(testDispatcher) {
        whenever(repository.getCardInfo()).thenReturn(Result.failure(IllegalStateException("raw card info failure")))
        whenever(repository.getRecentChargeOrders(page = 0, size = 5)).thenReturn(
            Result.failure(IllegalStateException("raw order load failure"))
        )
        whenever(repository.submitCharge(50, "synthetic-charge-password")).thenReturn(
            Result.failure(IllegalStateException("raw charge submit failure"))
        )

        val viewModel = ChargeViewModel(repository, context)
        advanceUntilIdle()
        assertEquals(UiText.StringResource(R.string.charge_info_unavailable), viewModel.state.value.error)
        assertEquals(UiText.StringResource(R.string.charge_order_load_failed), viewModel.state.value.orderError)

        viewModel.updateAmount("50")
        viewModel.updatePassword("synthetic-charge-password")
        viewModel.submitCharge()
        advanceUntilIdle()

        assertEquals(UiText.StringResource(R.string.charge_submit_failed), viewModel.state.value.error)
    }
}
