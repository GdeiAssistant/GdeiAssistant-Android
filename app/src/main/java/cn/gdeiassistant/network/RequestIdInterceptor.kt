package cn.gdeiassistant.network

import okhttp3.Interceptor
import okhttp3.Response
import java.util.UUID
import javax.inject.Inject

/**
 * Application interceptor that attaches a per-request [X-Request-ID] header to every outgoing
 * call so that it can be correlated with backend logs.
 *
 * If the request already carries an [X-Request-ID] header (e.g., set by a test or an upstream
 * proxy), the existing value is kept unchanged to preserve end-to-end traceability.
 */
class RequestIdInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val request = if (original.header("X-Request-ID").isNullOrBlank()) {
            original.newBuilder()
                .header("X-Request-ID", UUID.randomUUID().toString())
                .build()
        } else {
            original
        }
        return chain.proceed(request)
    }
}
