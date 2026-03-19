package cn.gdeiassistant.network

import cn.gdeiassistant.BuildConfig
import cn.gdeiassistant.data.SessionManager
import cn.gdeiassistant.event.GlobalEvent
import cn.gdeiassistant.event.GlobalEventManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

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
 * 响应拦截器：401 时先尝试无感刷新 Token，成功则重试原请求；失败则清除并发出 Unauthorized 事件。
 * 非 2xx 响应统一抛出 AppException。
 */
class ResponseInterceptor @Inject constructor(
    private val sessionManager: SessionManager
) : Interceptor {

    private val gson = Gson()

    private val refreshClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(NetworkConstants.REFRESH_CLIENT_TIMEOUT_SECONDS.toLong(), TimeUnit.SECONDS)
            .readTimeout(NetworkConstants.REFRESH_CLIENT_TIMEOUT_SECONDS.toLong(), TimeUnit.SECONDS)
            .writeTimeout(NetworkConstants.REFRESH_CLIENT_TIMEOUT_SECONDS.toLong(), TimeUnit.SECONDS)
            .cookieJar(sessionManager.cookieJar)
            .build()
    }

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        var request = chain.request()
        var response = chain.proceed(request)

        if (response.code == NetworkConstants.HTTP_UNAUTHORIZED) {
            val refreshToken = sessionManager.currentRefreshToken()?.takeIf { it.isNotBlank() }
            if (refreshToken != null) {
                val newTokens = doSilentRefresh(refreshToken)
                if (newTokens != null) {
                    sessionManager.saveTokens(newTokens.first, newTokens.second)
                    request = request.newBuilder()
                        .removeHeader("Authorization")
                        .addHeader("Authorization", "Bearer ${newTokens.first}")
                        .build()
                    response.close()
                    response = chain.proceed(request)
                    if (response.isSuccessful) return response
                }
            }
            val bodyStr = response.peekBody(64 * 1024).string()
            val message = parseMessageFromBody(bodyStr)
            sessionManager.clearTokens()
            GlobalEventManager.emit(GlobalEvent.Unauthorized)
            throw AppException(message ?: NetworkConstants.MESSAGE_LOGIN_EXPIRED, NetworkConstants.HTTP_UNAUTHORIZED)
        }

        if (!response.isSuccessful) {
            val bodyStr = response.peekBody(64 * 1024).string()
            val message = parseMessageFromBody(bodyStr)
            GlobalEventManager.emit(GlobalEvent.ShowToast(message ?: "请求失败"))
            throw AppException(message ?: "请求失败", response.code)
        }

        return response
    }

    private fun doSilentRefresh(refreshToken: String): Pair<String, String>? {
        val url = "${BuildConfig.BASE_URL.trimEnd('/')}/api/token/refresh"
        val body = FormBody.Builder().add("token", refreshToken).build()
        val request = Request.Builder().url(url).post(body).build()
        val response = refreshClient.newCall(request).execute()
        if (!response.isSuccessful) return null
        val bodyStr = response.body?.string() ?: return null
        return try {
            val json = gson.fromJson(bodyStr, JsonObject::class.java) ?: return null
            if ((json.get("success") as? JsonPrimitive)?.asBoolean != true) return null
            val data = json.getAsJsonObject("data") ?: return null
            val access = data.getAsJsonObject("accessToken")?.get("signature")?.asString ?: return null
            val refresh = data.getAsJsonObject("refreshToken")?.get("signature")?.asString ?: return null
            Pair(access, refresh)
        } catch (_: Exception) {
            null
        }
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
