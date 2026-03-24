package cn.gdeiassistant.network

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RequestIdInterceptorTest {

    private val server = MockWebServer()
    private lateinit var client: OkHttpClient

    @Before
    fun setUp() {
        server.start()
        client = OkHttpClient.Builder()
            .addInterceptor(RequestIdInterceptor())
            .build()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun injectsRequestIdWhenAbsent() {
        server.enqueue(MockResponse().setResponseCode(200))

        client.newCall(Request.Builder().url(server.url("/api/test")).build()).execute().close()

        val recorded = server.takeRequest()
        val rid = recorded.getHeader("X-Request-ID")
        assertNotNull("X-Request-ID header should be present", rid)
        assertTrue("X-Request-ID should be non-blank", rid!!.isNotBlank())
    }

    @Test
    fun generatedIdMatchesUuidFormat() {
        server.enqueue(MockResponse().setResponseCode(200))

        client.newCall(Request.Builder().url(server.url("/api/test")).build()).execute().close()

        val rid = server.takeRequest().getHeader("X-Request-ID")!!
        // UUID: 8-4-4-4-12 lowercase hex digits separated by hyphens
        assertTrue(
            "X-Request-ID should be UUID format, was: $rid",
            rid.matches(Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))
        )
    }

    @Test
    fun doesNotOverwriteExistingRequestId() {
        server.enqueue(MockResponse().setResponseCode(200))

        val existingId = "test-client-id-001"
        client.newCall(
            Request.Builder()
                .url(server.url("/api/test"))
                .header("X-Request-ID", existingId)
                .build()
        ).execute().close()

        assertEquals(existingId, server.takeRequest().getHeader("X-Request-ID"))
    }

    @Test
    fun eachRequestReceivesDifferentId() {
        server.enqueue(MockResponse().setResponseCode(200))
        server.enqueue(MockResponse().setResponseCode(200))

        val url = server.url("/api/test")
        client.newCall(Request.Builder().url(url).build()).execute().close()
        client.newCall(Request.Builder().url(url).build()).execute().close()

        val rid1 = server.takeRequest().getHeader("X-Request-ID")
        val rid2 = server.takeRequest().getHeader("X-Request-ID")
        assertNotEquals("Each request should receive a distinct X-Request-ID", rid1, rid2)
    }
}
