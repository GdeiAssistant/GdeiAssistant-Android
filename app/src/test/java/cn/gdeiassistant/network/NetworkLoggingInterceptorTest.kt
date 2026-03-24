package cn.gdeiassistant.network

import cn.gdeiassistant.data.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import java.util.logging.Handler
import java.util.logging.LogRecord

/**
 * Verifies that [NetworkLoggingInterceptor] emits structured log records for both
 * successful and error HTTP responses.
 *
 * Tests use a chain of [RequestIdInterceptor] + [NetworkLoggingInterceptor] +
 * [ResponseInterceptor] backed by [MockWebServer] so that the real interceptor
 * interaction is exercised end-to-end.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NetworkLoggingInterceptorTest {

    private val server = MockWebServer()
    private val testDispatcher = StandardTestDispatcher()

    // Capture java.util.logging records emitted by NetworkLoggingInterceptor
    private val capturedMessages = mutableListOf<String>()
    private val logHandler = object : Handler() {
        override fun publish(record: LogRecord) { capturedMessages.add(record.message ?: "") }
        override fun flush() {}
        override fun close() {}
    }
    private val networkLogger = java.util.logging.Logger.getLogger("GdeiNetwork")

    private lateinit var client: OkHttpClient

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        server.start()
        capturedMessages.clear()
        networkLogger.addHandler(logHandler)

        client = OkHttpClient.Builder()
            .addInterceptor(RequestIdInterceptor())
            .addInterceptor(NetworkLoggingInterceptor())
            .addInterceptor(ResponseInterceptor(mock<SessionManager>()))
            .build()
    }

    @After
    fun tearDown() {
        networkLogger.removeHandler(logHandler)
        Dispatchers.resetMain()
        server.shutdown()
    }

    @Test
    fun successResponseLogsStatusCode200AndRequestId() {
        server.enqueue(MockResponse().setResponseCode(200))

        client.newCall(
            Request.Builder()
                .url(server.url("/api/test"))
                .header("X-Request-ID", "success-rid-001")
                .build()
        ).execute().close()

        assertTrue(
            "Expected a log entry containing '200'",
            capturedMessages.any { it.contains("200") }
        )
        assertTrue(
            "Expected a log entry containing the request ID",
            capturedMessages.any { it.contains("success-rid-001") }
        )
    }

    @Test
    fun errorResponseLogsActualStatusCodeNotFailed() {
        server.enqueue(
            MockResponse()
                .setResponseCode(403)
                .addHeader("Content-Type", "application/json")
                .setBody("""{"success":false,"message":"无权访问"}""")
        )

        try {
            client.newCall(
                Request.Builder()
                    .url(server.url("/api/admin"))
                    .header("X-Request-ID", "error-rid-002")
                    .build()
            ).execute()
        } catch (_: AppException) { /* expected */ }

        assertTrue(
            "Expected a log entry containing '403' (not just 'FAILED')",
            capturedMessages.any { it.contains("403") }
        )
        assertTrue(
            "Expected the log entry for the error to NOT use the bare 'FAILED' label",
            capturedMessages.none { it.contains("FAILED") }
        )
    }

    @Test
    fun errorResponseLogsRequestIdForCorrelation() {
        server.enqueue(
            MockResponse()
                .setResponseCode(401)
                .addHeader("Content-Type", "application/json")
                .setBody("""{"success":false,"message":"Token expired"}""")
        )

        try {
            client.newCall(
                Request.Builder()
                    .url(server.url("/api/profile"))
                    .header("X-Request-ID", "corr-rid-003")
                    .build()
            ).execute()
        } catch (_: AppException) { /* expected */ }

        assertTrue(
            "Expected the error log entry to include the request ID for correlation",
            capturedMessages.any { msg -> msg.contains("corr-rid-003") && msg.contains("401") }
        )
    }

    @Test
    fun successResponseWithBackendRidLogsBothIds() {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("X-Request-ID", "backend-echo-999")
        )

        client.newCall(
            Request.Builder()
                .url(server.url("/api/items"))
                .header("X-Request-ID", "client-rid-004")
                .build()
        ).execute().close()

        assertTrue(
            "Expected log entry with both client and backend request IDs",
            capturedMessages.any { it.contains("client-rid-004") && it.contains("backend-echo-999") }
        )
    }
}
