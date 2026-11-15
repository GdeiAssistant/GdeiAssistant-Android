package cn.gdeiassistant.network

import cn.gdeiassistant.data.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.buffer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Tests for the existing [ResponseInterceptor]:
 * - 2xx responses pass through without throwing.
 * - 401 responses throw [AppException] with the message from the JSON body and clear tokens.
 * - Non-2xx responses throw [AppException] with the message from the JSON body.
 *
 * All test responses include a JSON `message` field so the interceptor uses the body message
 * rather than falling back to Android-context-dependent string resources.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ResponseInterceptorTest {

    private val server = MockWebServer()
    private lateinit var sessionManager: SessionManager
    private lateinit var client: OkHttpClient

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        // Provide a main dispatcher so GlobalEventManager.emit() can dispatch its coroutine
        // without crashing the test thread.
        Dispatchers.setMain(testDispatcher)
        server.start()
        sessionManager = mock()
        client = OkHttpClient.Builder()
            .addInterceptor(ResponseInterceptor(sessionManager))
            .build()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        server.shutdown()
    }

    @Test
    fun successfulResponsePassesThrough() {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"success":true}"""))

        val response = client.newCall(Request.Builder().url(server.url("/api/test")).build()).execute()

        assertEquals(200, response.code)
        response.close()
        verify(sessionManager, never()).clearTokens()
    }

    @Test
    fun unauthorizedResponseThrowsAppExceptionWithBodyMessage() {
        server.enqueue(
            MockResponse()
                .setResponseCode(401)
                .addHeader("Content-Type", "application/json")
                .setBody("""{"success":false,"message":"登录已过期，请重新登录"}""")
        )

        var caught: AppException? = null
        try {
            client.newCall(Request.Builder().url(server.url("/api/test")).build()).execute()
        } catch (e: AppException) {
            caught = e
        }

        assertNotNull("Expected AppException for 401 response", caught)
        assertEquals("登录已过期，请重新登录", caught!!.message)
        assertEquals(NetworkConstants.HTTP_UNAUTHORIZED, caught.code)
    }

    @Test
    fun unauthorizedResponseClearsTokens() {
        server.enqueue(
            MockResponse()
                .setResponseCode(401)
                .addHeader("Content-Type", "application/json")
                .setBody("""{"success":false,"message":"Token expired"}""")
        )

        try {
            client.newCall(Request.Builder().url(server.url("/api/test")).build()).execute()
        } catch (_: AppException) {
            // expected
        }

        verify(sessionManager).clearTokensIfCurrent(org.mockito.kotlin.anyOrNull())
    }

    @Test
    fun serverErrorResponseThrowsAppExceptionWithBodyMessage() {
        server.enqueue(
            MockResponse()
                .setResponseCode(500)
                .addHeader("Content-Type", "application/json")
                .setBody("""{"success":false,"message":"内部服务错误"}""")
        )

        var caught: AppException? = null
        try {
            client.newCall(Request.Builder().url(server.url("/api/test")).build()).execute()
        } catch (e: AppException) {
            caught = e
        }

        assertNotNull("Expected AppException for 500 response", caught)
        assertEquals("内部服务错误", caught!!.message)
        assertEquals(500, caught.code)
    }

    @Test
    fun clientErrorResponseThrowsAppExceptionWithBodyMessage() {
        server.enqueue(
            MockResponse()
                .setResponseCode(422)
                .addHeader("Content-Type", "application/json")
                .setBody("""{"success":false,"message":"参数校验失败"}""")
        )

        var caught: AppException? = null
        try {
            client.newCall(Request.Builder().url(server.url("/api/test")).build()).execute()
        } catch (e: AppException) {
            caught = e
        }

        assertNotNull("Expected AppException for 422 response", caught)
        assertEquals("参数校验失败", caught!!.message)
    }

    @Test
    fun switchingProtocols101PassesThroughWithoutClosingBodyOrThrowing() {
        val body = TrackingResponseBody()
        val request = Request.Builder().url("https://example.test/api/social/realtime").build()
        val rawResponse = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(101)
            .message("Switching Protocols")
            .body(body)
            .build()
        val chain: Interceptor.Chain = mock()
        whenever(chain.request()).thenReturn(request)
        whenever(chain.proceed(any())).thenReturn(rawResponse)

        val result = ResponseInterceptor(sessionManager).intercept(chain)

        assertSame(rawResponse, result)
        assertEquals(101, result.code)
        assertFalse("101 response body must stay open for WebSocket", body.closed)
        verify(sessionManager, never()).clearTokens()
        verify(sessionManager, never()).clearTokensIfCurrent(org.mockito.kotlin.anyOrNull())
    }

    @Test
    fun webSocketUpgradeRequestPassesThroughWithoutThrowing() {
        val body = TrackingResponseBody()
        val request = Request.Builder()
            .url("https://example.test/api/social/realtime")
            .header("Upgrade", "websocket")
            .build()
        val rawResponse = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .body(body)
            .build()
        val chain: Interceptor.Chain = mock()
        whenever(chain.request()).thenReturn(request)
        whenever(chain.proceed(any())).thenReturn(rawResponse)

        val result = ResponseInterceptor(sessionManager).intercept(chain)

        assertSame(rawResponse, result)
        assertEquals(401, result.code)
        assertFalse("WebSocket upgrade response body must not be consumed or closed", body.closed)
        verify(sessionManager, never()).clearTokensIfCurrent(org.mockito.kotlin.anyOrNull())
    }

    private class TrackingResponseBody : ResponseBody() {
        var closed = false
        private val source = object : ForwardingSource(Buffer()) {
            override fun close() {
                closed = true
                super.close()
            }
        }.buffer()

        override fun contentType(): MediaType = "application/octet-stream".toMediaType()
        override fun contentLength(): Long = 0L
        override fun source(): BufferedSource = source
    }
}
