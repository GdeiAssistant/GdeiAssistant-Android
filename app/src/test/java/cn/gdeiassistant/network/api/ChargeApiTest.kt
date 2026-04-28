package cn.gdeiassistant.network.api

import cn.gdeiassistant.model.ChargeOrder
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ChargeApiTest {

    private val server = MockWebServer()
    private lateinit var api: ChargeApi

    @Before
    fun setUp() {
        server.start()
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ChargeApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun submitChargeDoesNotSendLegacyHmacFields() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"success":true,"code":200,"message":"","data":{"alipayURL":"https://pay.example.invalid/synthetic"}}""")
        )

        api.submitCharge(
            idempotencyKey = "synthetic-idempotency-key",
            amount = "50",
            password = "synthetic-charge-password"
        )

        val request = server.takeRequest()
        val body = request.body.readUtf8()

        assertTrue(request.getHeader("Idempotency-Key") == "synthetic-idempotency-key")
        assertTrue(body.contains("amount=50"))
        assertTrue(body.contains("password=synthetic-charge-password"))
        assertFalse(body.contains("hmac="))
        assertFalse(body.contains("timestamp="))
    }

    @Test
    fun getChargeOrderParsesSafeStatusFields() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {"success":true,"code":200,"message":"","data":{
                        "orderId":"synthetic-order-id",
                        "amount":50,
                        "status":"PAYMENT_SESSION_CREATED",
                        "message":"支付请求已生成，请完成支付并刷新余额。该状态不代表最终到账。",
                        "createdAt":"2026-04-28 10:00:00",
                        "updatedAt":"2026-04-28 10:01:00",
                        "idempotencyKeyHash":"synthetic-internal-hash",
                        "payloadFingerprint":"synthetic-payload-fingerprint",
                        "paymentUrlHash":"synthetic-payment-url-hash"
                    }}
                    """.trimIndent()
                )
        )

        val response = api.getChargeOrder("synthetic-order-id")
        val request = server.takeRequest()
        val exposedFields = ChargeOrder::class.java.declaredFields.map { it.name }.toSet()

        assertEquals("/api/card/charge/orders/synthetic-order-id", request.path)
        assertEquals("synthetic-order-id", response.data?.orderId)
        assertEquals(50, response.data?.amount)
        assertEquals("PAYMENT_SESSION_CREATED", response.data?.status)
        assertFalse(exposedFields.contains("idempotencyKeyHash"))
        assertFalse(exposedFields.contains("payloadFingerprint"))
        assertFalse(exposedFields.contains("deviceIdHash"))
        assertFalse(exposedFields.contains("paymentUrlHash"))
        assertFalse(exposedFields.contains("manualReviewNote"))
    }

    @Test
    fun getRecentChargeOrdersUsesPagingAndStatusQuery() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {"success":true,"code":200,"message":"","data":[
                        {"orderId":"synthetic-order-new","amount":50,"status":"UNKNOWN","message":"充值状态暂无法确认，请稍后查看订单状态，避免重复提交。","retryAfter":60},
                        {"orderId":"synthetic-order-old","amount":20,"status":"PROCESSING","message":"充值请求正在处理中，请稍后查看结果，避免重复提交。","retryAfter":60}
                    ]}
                    """.trimIndent()
                )
        )

        val response = api.getRecentChargeOrders(page = 0, size = 20, status = "UNKNOWN")
        val request = server.takeRequest()

        assertEquals("/api/card/charge/orders?page=0&size=20&status=UNKNOWN", request.path)
        assertEquals(2, response.data?.size)
        assertEquals("synthetic-order-new", response.data?.firstOrNull()?.orderId)
        assertEquals("UNKNOWN", response.data?.firstOrNull()?.status)
        assertEquals(60, response.data?.firstOrNull()?.retryAfter)
    }
}
