package cn.gdeiassistant.network

import cn.gdeiassistant.data.SessionManager
import cn.gdeiassistant.model.JsonResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Dispatcher
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
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/** Uses only synthetic responses and loopback MockWebServer traffic. */
@OptIn(ExperimentalCoroutinesApi::class)
class ResponseInterceptorRegressionTest {
    @Before fun setUp() { Dispatchers.setMain(StandardTestDispatcher()) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun async401PreservesStatusMessageAndClearsSession() = assertAsyncError(401)
    @Test fun async422PreservesStatusMessage() = assertAsyncError(422)
    @Test fun async500PreservesStatusMessage() = assertAsyncError(500)

    private fun assertAsyncError(code: Int) {
        val uncaught = AtomicReference<Throwable?>()
        val failure = AtomicReference<IOException?>()
        val completed = CountDownLatch(1)
        val worker = AtomicReference<Thread>()
        val executor = Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "response-interceptor-regression").apply {
                isDaemon = true
                uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { _, error -> uncaught.set(error) }
                worker.set(this)
            }
        }
        val session: SessionManager = mock()
        val server = MockWebServer()
        val client = OkHttpClient.Builder().dispatcher(Dispatcher(executor))
            .addInterceptor(ResponseInterceptor(session)).build()
        try {
            server.start()
            server.enqueue(MockResponse().setResponseCode(code).setBody("""{"message":"synthetic failure","errorCode":"SYNTHETIC_ERROR"}"""))
            client.newCall(Request.Builder().url(server.url("/test")).build()).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    failure.set(e)
                    completed.countDown()
                }
                override fun onResponse(call: Call, response: Response) {
                    response.close()
                    completed.countDown()
                }
            })
            assertTrue("Async callback did not complete", completed.await(5, TimeUnit.SECONDS))
            // Await the worker itself, including its uncaught-exception handler, rather than
            // merely observing the callback (OkHttp may invoke it before rethrowing).
            executor.shutdown()
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS))
            worker.get().join(5_000)
            assertFalse("OkHttp worker is still running", worker.get().isAlive)
            assertNull("HTTP failure escaped OkHttp's executor", uncaught.get())
            assertTrue("Failure must retain AppException, not a generic IOException", failure.get() is AppException)
            val error = failure.get() as AppException
            assertEquals(code, error.code)
            assertEquals("synthetic failure", error.message)
            assertEquals("SYNTHETIC_ERROR", error.errorCode)
            if (code == 401) verify(session).clearTokensIfCurrent(org.mockito.kotlin.anyOrNull()) else verify(session, never()).clearTokensIfCurrent(org.mockito.kotlin.anyOrNull())
        } finally {
            client.dispatcher.cancelAll()
            executor.shutdownNow()
            executor.awaitTermination(5, TimeUnit.SECONDS)
            client.connectionPool.evictAll()
            server.shutdown()
        }
    }

    interface TestApi {
        @GET("test") suspend fun request(): JsonResult
    }

    @Test fun retrofitSuspendAndSafeApiCallPreserveHttpError() = runBlocking {
        val server = MockWebServer()
        val client = OkHttpClient.Builder().addInterceptor(ResponseInterceptor(mock())).build()
        try {
            server.start()
            val api = Retrofit.Builder().baseUrl(server.url("/"))
                .client(client).addConverterFactory(GsonConverterFactory.create())
                .build().create(TestApi::class.java)
            server.enqueue(MockResponse().setResponseCode(422).setBody("""{"message":"validation failed","errorCode":"SYNTHETIC_VALIDATION"}"""))
            val error = safeJsonResultCall { api.request() }.exceptionOrNull()
            assertTrue(error is AppException)
            assertEquals(422, (error as AppException).code)
            assertEquals("validation failed", error.message)
            assertEquals("SYNTHETIC_VALIDATION", error.errorCode)
        } finally {
            client.dispatcher.executorService.shutdownNow()
            client.connectionPool.evictAll()
            server.shutdown()
        }
    }

    @Test fun errorResponsesCloseOriginalBodyForAllStatusesAndLargeBodies() {
        for (code in listOf(401, 422, 500)) {
            for (padding in listOf("", " ".repeat(70_000))) {
                // Trailing whitespace makes the peeked JSON valid even when the full body is larger.
                val body = TrackingBody("""{"message":"synthetic failure"}""" + padding)
                val error = runCatching { ResponseInterceptor(mock()).intercept(chain(code, body)) }.exceptionOrNull()
                assertTrue(error is AppException)
                assertEquals(code, (error as AppException).code)
                assertTrue("Original body must close for HTTP $code", body.closed)
            }
        }
    }

    @Test fun successfulResponseRemainsOpenForCaller() {
        val body = TrackingBody("success")
        val response = ResponseInterceptor(mock()).intercept(chain(200, body))
        assertFalse(body.closed)
        assertSame(body, response.body)
        assertEquals("success", response.body.string())
        assertTrue(body.closed)
    }

    @Test fun responseClosesWhenPeekingBodyFails() {
        val body = TrackingBody("", failRead = true)
        val error = runCatching { ResponseInterceptor(mock()).intercept(chain(500, body)) }.exceptionOrNull()
        assertTrue(error is IOException)
        assertEquals("synthetic read failure", error?.message)
        assertTrue(body.closed)
    }

    @Test fun responseClosesWhenSessionCleanupFails() {
        val session: SessionManager = mock()
        doThrow(IllegalStateException("synthetic cleanup failure")).whenever(session).clearTokensIfCurrent(org.mockito.kotlin.anyOrNull())
        val body = TrackingBody("""{"message":"expired"}""")
        val error = runCatching { ResponseInterceptor(session).intercept(chain(401, body)) }.exceptionOrNull()
        assertTrue(error is IllegalStateException)
        assertTrue(body.closed)
    }

    private fun chain(code: Int, body: ResponseBody): Interceptor.Chain {
        val request = Request.Builder().url("https://example.test/test").build()
        val chain: Interceptor.Chain = mock()
        whenever(chain.request()).thenReturn(request)
        whenever(chain.proceed(any())).thenReturn(Response.Builder().request(request)
            .protocol(Protocol.HTTP_1_1).code(code).message("Synthetic response").body(body).build())
        return chain
    }

    private class TrackingBody(text: String, failRead: Boolean = false) : ResponseBody() {
        var closed = false
        private val bytes = text.toByteArray(Charsets.UTF_8)
        private val source = object : ForwardingSource(Buffer().write(bytes)) {
            override fun read(sink: Buffer, byteCount: Long): Long {
                if (failRead) throw IOException("synthetic read failure")
                return super.read(sink, byteCount)
            }
            override fun close() {
                closed = true
                super.close()
            }
        }.buffer()
        override fun contentType(): MediaType = "application/json".toMediaType()
        override fun contentLength(): Long = bytes.size.toLong()
        override fun source(): BufferedSource = source
    }
}
