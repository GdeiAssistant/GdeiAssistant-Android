package cn.gdeiassistant.network

import android.content.Context
import android.content.SharedPreferences
import cn.gdeiassistant.data.SessionManager
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class AuthInterceptorTest {

    private var previousContext: Context? = null

    @Before
    fun setUp() {
        previousContext = AppContextProvider.contextOrNull
        clearAppContext()
    }

    @After
    fun tearDown() {
        restoreAppContext(previousContext)
    }

    @Test
    fun injectsBearerTokenAndGeneratedUuidWithoutContext() {
        val sessionManager: SessionManager = mock()
        whenever(sessionManager.currentToken()).thenReturn("jwt-token-123")

        val interceptor = AuthInterceptor(sessionManager)
        val firstReq = executeIntercept(interceptor)
        val secondReq = executeIntercept(interceptor)

        assertEquals("Bearer jwt-token-123", firstReq.header("Authorization"))
        val deviceId1 = firstReq.header("X-Device-ID")
        val deviceId2 = secondReq.header("X-Device-ID")
        assertNotNull(deviceId1)
        UUID.fromString(deviceId1)
        assertEquals(deviceId1, deviceId2)
    }

    @Test
    fun omitsAuthorizationWhenNoTokenPresent() {
        val sessionManager: SessionManager = mock()
        whenever(sessionManager.currentToken()).thenReturn(null)

        val request = executeIntercept(AuthInterceptor(sessionManager))

        assertNull(request.header("Authorization"))
        assertTrue(!request.header("X-Device-ID").isNullOrBlank())
    }

    @Test
    fun persistsAndReusesDeviceIdAcrossInstancesViaSharedPreferences() {
        val backingMap = mutableMapOf<String, String?>()
        val editor: SharedPreferences.Editor = mock()
        whenever(editor.putString(any(), any())).thenAnswer { invocation ->
            backingMap[invocation.getArgument(0)] = invocation.getArgument(1)
            editor
        }
        val prefs: SharedPreferences = mock()
        whenever(prefs.getString(any(), any())).thenAnswer { invocation ->
            backingMap[invocation.getArgument(0)] ?: invocation.getArgument(1)
        }
        whenever(prefs.edit()).thenReturn(editor)

        val context: Context = mock()
        whenever(context.applicationContext).thenReturn(context)
        whenever(context.getSharedPreferences(any(), any())).thenReturn(prefs)
        AppContextProvider.init(context)

        val sessionManager: SessionManager = mock()
        val req1 = executeIntercept(AuthInterceptor(sessionManager))
        val req2 = executeIntercept(AuthInterceptor(sessionManager))

        val id1 = req1.header("X-Device-ID")
        val id2 = req2.header("X-Device-ID")
        assertNotNull(id1)
        UUID.fromString(id1)
        assertEquals(id1, id2)
        assertEquals(id1, backingMap["app_device_id"])
    }

    private fun executeIntercept(interceptor: AuthInterceptor): Request {
        val original = Request.Builder().url("https://example.test/api/test").build()
        val chain: Interceptor.Chain = mock()
        whenever(chain.request()).thenReturn(original)
        val captor = argumentCaptor<Request>()
        whenever(chain.proceed(captor.capture())).thenAnswer { invocation ->
            val forwarded = invocation.getArgument<Request>(0)
            Response.Builder()
                .request(forwarded)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body("{}".toResponseBody())
                .build()
        }
        interceptor.intercept(chain).close()
        verify(chain).proceed(any())
        return captor.firstValue
    }

    private fun clearAppContext() {
        val resetter: Context = mock()
        whenever(resetter.applicationContext).thenReturn(null)
        AppContextProvider.init(resetter)
    }

    private fun restoreAppContext(context: Context?) {
        if (context == null) {
            clearAppContext()
        } else {
            val wrapper: Context = mock()
            whenever(wrapper.applicationContext).thenReturn(context)
            AppContextProvider.init(wrapper)
        }
    }
}
