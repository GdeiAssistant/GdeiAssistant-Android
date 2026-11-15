package cn.gdeiassistant.network

import android.content.Context
import cn.gdeiassistant.data.SessionManager
import cn.gdeiassistant.event.GlobalEvent
import cn.gdeiassistant.event.GlobalEventManager
import okhttp3.Interceptor
import java.util.UUID
import javax.inject.Inject

/**
 * Auth 拦截器：自动注入 JWT Bearer Token 与稳定的应用级随机设备标识。
 */
class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager
) : Interceptor {

    @Volatile
    private var cachedDeviceId: String? = null

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request().newBuilder().apply {
            sessionManager.currentToken()?.takeIf { it.isNotBlank() }?.let { token ->
                addHeader("Authorization", "Bearer $token")
            }
            // Existing backend contract requires a stable X-Device-ID header; use an app-scoped UUID.
            addHeader("X-Device-ID", resolveDeviceId())
        }.build()
        return chain.proceed(request)
    }

    private fun resolveDeviceId(): String {
        cachedDeviceId?.let { return it }
        synchronized(this) {
            cachedDeviceId?.let { return it }
            val resolved = runCatching {
                val appContext = AppContextProvider.contextOrNull?.let { it.applicationContext ?: it }
                val prefs = appContext?.getSharedPreferences(DEVICE_PREFS_NAME, Context.MODE_PRIVATE)
                val stored = prefs?.getString(KEY_APP_DEVICE_ID, null)?.trim()?.takeIf(String::isNotEmpty)
                stored ?: UUID.randomUUID().toString().also { generated ->
                    prefs?.edit()?.putString(KEY_APP_DEVICE_ID, generated)?.apply()
                }
            }.getOrNull() ?: UUID.randomUUID().toString()
            cachedDeviceId = resolved
            return resolved
        }
    }

    private companion object {
        const val DEVICE_PREFS_NAME = "GdeiAssistant_device_identity"
        const val KEY_APP_DEVICE_ID = "app_device_id"
    }
}

/**
 * 响应拦截器：WebSocket 101 升级直接放行；401 时清理会话并发出 Unauthorized 事件；非 2xx 响应统一抛出 AppException。
 */
class ResponseInterceptor @Inject constructor(
    private val sessionManager: SessionManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val response = chain.proceed(chain.request())

        if (response.code == 101) {
            return response
        }

        if (!response.isSuccessful) {
            // This interceptor owns error responses because it replaces them with an exception.
            // Close the original body even if reading it or handling session expiry fails.
            response.use {
                val bodyStr = response.peekBody(64 * 1024).string()
                val parsed = parseErrorBody(bodyStr)
                if (response.code == NetworkConstants.HTTP_UNAUTHORIZED) {
                    val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")
                    if (sessionManager.clearTokensIfCurrent(requestToken)) {
                        GlobalEventManager.emit(GlobalEvent.Unauthorized)
                    }
                    throw AppException(
                        message = parsed?.first ?: NetworkConstants.messageLoginExpired(),
                        code = response.code,
                        errorCode = parsed?.second ?: "AUTH_REQUIRED"
                    )
                }
                val errorMessage = parsed?.first ?: NetworkConstants.messageRequestFailed()
                throw AppException(errorMessage, response.code, parsed?.second)
            }
        }

        return response
    }
}
