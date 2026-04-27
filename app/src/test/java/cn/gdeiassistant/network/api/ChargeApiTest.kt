package cn.gdeiassistant.network.api

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
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
            amount = "50",
            password = "synthetic-charge-password"
        )

        val body = server.takeRequest().body.readUtf8()

        assertTrue(body.contains("amount=50"))
        assertTrue(body.contains("password=synthetic-charge-password"))
        assertFalse(body.contains("hmac="))
        assertFalse(body.contains("timestamp="))
    }
}
