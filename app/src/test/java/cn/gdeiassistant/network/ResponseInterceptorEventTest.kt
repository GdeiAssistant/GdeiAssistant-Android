package cn.gdeiassistant.network

import android.content.Context
import cn.gdeiassistant.R
import cn.gdeiassistant.data.SessionManager
import cn.gdeiassistant.event.GlobalEvent
import cn.gdeiassistant.event.GlobalEventManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ResponseInterceptorEventTest {
    private val dispatcher = StandardTestDispatcher()
    private var previousContext: Context? = null

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        previousContext = AppContextProvider.contextOrNull
        val context: Context = mock()
        whenever(context.applicationContext).thenReturn(context)
        whenever(context.getString(R.string.login_expired_toast)).thenReturn("Login expired")
        whenever(context.getString(R.string.network_error_request_failed)).thenReturn("Request failed")
        AppContextProvider.init(context)
    }

    @After fun tearDown() {
        val context: Context = mock()
        whenever(context.applicationContext).thenReturn(previousContext)
        AppContextProvider.init(context)
        Dispatchers.resetMain()
    }

    @Test fun unauthorizedEmitsOnlyUnauthorizedAndClearsSession() = assertError(
        401, """{"message":"expired"}""", "expired", GlobalEvent.Unauthorized
    )

    @Test fun serverErrorEmitsOnlyBackendMessageToast() = assertError(
        500, """{"message":"backend failure"}""", "backend failure", null
    )

    @Test fun malformedBodyUsesRequestFailureFallback() = assertError(
        422, "not JSON", "Request failed", null
    )

    @Test fun missingUnauthorizedMessageUsesLoginFallback() = assertError(
        401, "{}", "Login expired", GlobalEvent.Unauthorized
    )

    private fun assertError(code: Int, body: String, expectedMessage: String, event: GlobalEvent?) = runTest(dispatcher) {
        // Drain pending emissions from other tests before collecting this response's events.
        runCurrent()
        val events = mutableListOf<GlobalEvent>()
        val collector = launch(UnconfinedTestDispatcher(testScheduler)) {
            GlobalEventManager.events.collect { events += it }
        }
        try {
            val session: SessionManager = mock()
            whenever(session.clearTokensIfCurrent("synthetic-token")).thenReturn(true)
            val request = Request.Builder().url("https://example.test/test").header("Authorization", "Bearer synthetic-token").build()
            val chain: Interceptor.Chain = mock()
            whenever(chain.request()).thenReturn(request)
            whenever(chain.proceed(any())).thenReturn(Response.Builder().request(request)
                .protocol(Protocol.HTTP_1_1).code(code).message("Synthetic response")
                .body(body.toResponseBody("application/json".toMediaType())).build())
            val error = runCatching { ResponseInterceptor(session).intercept(chain) }.exceptionOrNull()
            assertTrue(error is AppException)
            assertEquals(code, (error as AppException).code)
            assertEquals(expectedMessage, error.message)
            runCurrent()
            assertEquals(listOfNotNull(event), events)
            if (code == 401) verify(session).clearTokensIfCurrent("synthetic-token") else verify(session, never()).clearTokensIfCurrent("synthetic-token")
        } finally {
            collector.cancel()
        }
    }
}
