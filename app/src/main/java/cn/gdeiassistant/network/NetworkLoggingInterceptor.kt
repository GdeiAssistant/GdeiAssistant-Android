package cn.gdeiassistant.network

import okhttp3.Interceptor
import okhttp3.Response
import java.util.logging.Logger
import javax.inject.Inject

/**
 * Application interceptor that logs each HTTP exchange: request ID, method, URL path,
 * HTTP status code, and round-trip elapsed time.
 *
 * Request/response bodies are intentionally not logged to keep production logs
 * lightweight and avoid accidentally leaking sensitive payloads.
 *
 * The [X-Request-ID] header is expected to have been set by [RequestIdInterceptor] earlier in
 * the chain. If the backend echoes a different request ID in the response, both IDs are logged
 * so that client-side and server-side logs can be correlated.
 */
class NetworkLoggingInterceptor @Inject constructor() : Interceptor {

    private val logger = Logger.getLogger("GdeiNetwork")

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestId = request.header("X-Request-ID") ?: "?"
        val method = request.method
        val path = request.url.encodedPath

        val start = System.currentTimeMillis()
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: AppException) {
            // AppException is thrown by ResponseInterceptor for 4xx/5xx responses.
            // e.code is the HTTP status code — log it as a structured field rather than "FAILED".
            // The backend-echoed X-Request-ID is unavailable here because ResponseInterceptor
            // consumed the raw response before throwing; log what we have.
            val elapsed = System.currentTimeMillis() - start
            logger.warning("rid:$requestId | $method $path | ${e.code} | ${elapsed}ms | error")
            throw e
        } catch (e: Exception) {
            val elapsed = System.currentTimeMillis() - start
            logger.warning("rid:$requestId | $method $path | FAILED | ${elapsed}ms | ${e.javaClass.simpleName}")
            throw e
        }

        val elapsed = System.currentTimeMillis() - start
        val status = response.code
        val backendRid = response.header("X-Request-ID")
        if (backendRid != null) {
            logger.info("client-rid:$requestId backend-rid:$backendRid | $method $path | $status | ${elapsed}ms")
        } else {
            logger.info("rid:$requestId | $method $path | $status | ${elapsed}ms")
        }
        return response
    }
}
