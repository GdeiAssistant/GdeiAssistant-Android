package cn.gdeiassistant.network

import cn.gdeiassistant.data.SessionManager
import cn.gdeiassistant.event.GlobalEvent
import cn.gdeiassistant.event.GlobalEventManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import okhttp3.Interceptor
import javax.inject.Inject

/**
 * Auth 拦截器：自动注入 JWT Bearer Token。
 */
class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request().newBuilder().apply {
            sessionManager.currentToken()?.takeIf { it.isNotBlank() }?.let { token ->
                addHeader("Authorization", "Bearer $token")
            }
        }.build()
        return chain.proceed(request)
    }
}

/**
 * 响应拦截器：401 时清理会话并发出 Unauthorized 事件；非 2xx 响应统一抛出 AppException。
 */
class ResponseInterceptor @Inject constructor(
    private val sessionManager: SessionManager
) : Interceptor {

    private val gson = Gson()

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val response = chain.proceed(chain.request())

        if (response.code == NetworkConstants.HTTP_UNAUTHORIZED) {
            val bodyStr = response.peekBody(64 * 1024).string()
            val message = parseMessageFromBody(bodyStr)
            sessionManager.clearTokens()
            GlobalEventManager.emit(GlobalEvent.Unauthorized)
            throw AppException(message ?: NetworkConstants.messageLoginExpired(), NetworkConstants.HTTP_UNAUTHORIZED)
        }

        if (!response.isSuccessful) {
            val bodyStr = response.peekBody(64 * 1024).string()
            val message = parseMessageFromBody(bodyStr)
            GlobalEventManager.emit(GlobalEvent.ShowToast(message ?: NetworkConstants.messageRequestFailed()))
            throw AppException(message ?: NetworkConstants.messageRequestFailed(), response.code)
        }

        return response
    }

    private fun parseMessageFromBody(bodyStr: String?): String? {
        if (bodyStr.isNullOrBlank()) return null
        return try {
            (gson.fromJson(bodyStr, JsonObject::class.java)?.get("message") as? JsonPrimitive)?.asString
        } catch (_: Exception) {
            null
        }
    }
}
