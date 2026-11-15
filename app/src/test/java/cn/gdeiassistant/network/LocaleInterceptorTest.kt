package cn.gdeiassistant.network

import cn.gdeiassistant.data.SettingsRepository
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class LocaleInterceptorTest {

    @Test
    fun injectsNormalizedAcceptLanguageFromSyncCacheWithoutBlockingFlow() {
        val settingsRepository: SettingsRepository = mock()
        whenever(settingsRepository.currentLocaleSync()).thenReturn("en_US")

        val interceptor = LocaleInterceptor(settingsRepository)
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

        assertEquals("en", captor.firstValue.header("Accept-Language"))
        verify(settingsRepository).currentLocaleSync()
        verify(settingsRepository, never()).locale
    }
}
