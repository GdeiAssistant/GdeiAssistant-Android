package cn.gdeiassistant.network

import android.annotation.SuppressLint
import cn.gdeiassistant.data.SessionManager
import cn.gdeiassistant.event.GlobalEvent
import cn.gdeiassistant.event.GlobalEventManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import android.provider.Settings
import okhttp3.Interceptor
import javax.inject.Inject

/**
 * Auth 拦截器：自动注入 JWT Bearer Token。
 */
class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager
) : Interceptor {
    @SuppressLint("HardwareIds")
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request().newBuilder().apply {
            sessionManager.currentToken()?.takeIf { it.isNotBlank() }?.let { token ->
                addHeader("Authorization", "Bearer $token")
            }
            // Existing backend contract requires a stable X-Device-ID header.
            val deviceId = Settings.Secure.getString(
                AppContextProvider.context.contentResolver,
                Settings.Secure.ANDROID_ID
            )
            if (!deviceId.isNullOrBlank()) {
                addHeader("X-Device-ID", deviceId)
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
            val parsed = parseErrorFromBody(bodyStr)
            sessionManager.clearTokens()
            GlobalEventManager.emit(GlobalEvent.Unauthorized)
            throw AppException(
                message = parsed.first ?: NetworkConstants.messageLoginExpired(),
                code = NetworkConstants.HTTP_UNAUTHORIZED,
                errorCode = parsed.second ?: "AUTH_REQUIRED"
            )
        }

        if (!response.isSuccessful) {
            val bodyStr = response.peekBody(64 * 1024).string()
            val parsed = parseErrorFromBody(bodyStr)
            GlobalEventManager.emit(GlobalEvent.ShowToast(parsed.first ?: NetworkConstants.messageRequestFailed()))
            throw AppException(
                message = parsed.first ?: NetworkConstants.messageRequestFailed(),
                code = response.code,
                errorCode = parsed.second
            )
        }

        return response
    }

    private fun parseErrorFromBody(bodyStr: String?): Pair<String?, String?> {
        if (bodyStr.isNullOrBlank()) return null to null
        return try {
            val json = gson.fromJson(bodyStr, JsonObject::class.java) ?: return null to null
            val message = (json.get("message") as? JsonPrimitive)?.asString
            val errorCode = (json.get("errorCode") as? JsonPrimitive)?.asString
            message to errorCode
        } catch (_: Exception) {
            null to null
        }
    }
}
