package cn.gdeiassistant.ui.charge

import android.content.Context
import cn.gdeiassistant.R
import cn.gdeiassistant.data.ChargeRepository
import cn.gdeiassistant.model.CardInfo
import cn.gdeiassistant.model.Charge
import cn.gdeiassistant.model.ChargeOrder
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
        assertEquals("synthetic-order-id", state.recentOrders.firstOrNull()?.orderId)
        assertFalse(state.latestOrder?.message.orEmpty().contains("到账成功"))
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

        viewModel.refreshChargeOrders()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("PROCESSING", state.latestOrder?.status)
        assertEquals("PROCESSING", state.recentOrders.firstOrNull()?.status)
    }

    @Test
    fun refreshPrefersFetchedOrderWhenSubmittedSummaryHasNoOrderId() = runTest(testDispatcher) {
        whenever(repository.getCardInfo()).thenReturn(Result.success(CardInfo(cardBalance = "52.50")))
        whenever(repository.getRecentChargeOrders(page = 0, size = 5)).thenReturn(
            Result.success(emptyList()),
            Result.success(
                listOf(
                    ChargeOrder(
                        orderId = "synthetic-order-from-backend",
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
        assertEquals(null, viewModel.state.value.latestOrder?.orderId)

        viewModel.refreshChargeOrders()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("synthetic-order-from-backend", state.latestOrder?.orderId)
        assertEquals("UNKNOWN", state.latestOrder?.status)
    }
}
