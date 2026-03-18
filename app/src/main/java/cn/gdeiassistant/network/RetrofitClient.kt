package cn.gdeiassistant.network

import cn.gdeiassistant.BuildConfig
import cn.gdeiassistant.event.GlobalEvent
import cn.gdeiassistant.event.GlobalEventManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * 单例 Retrofit 客户端。
 * AuthInterceptor：JWT 自动注入。
 * ResponseInterceptor：401 时先尝试无感刷新 Token，成功则重试原请求；失败再清除并 Unauthorized。
 */
object RetrofitClient {

    /** 由 Application 设置，用于 JWT 自动注入 */
    var tokenProvider: (() -> String?)? = null

    /** 由 Application 设置，用于 401 无感刷新时获取 refreshToken */
    var refreshTokenProvider: (() -> String?)? = null

    /** 刷新成功后由 Application 保存新 Token 并更新内存 */
    var onTokensRefreshed: ((accessToken: String, refreshToken: String) -> Unit)? = null

    /** 401 且刷新失败时调用 */
    var onClearToken: (() -> Unit)? = null

    private val gson = Gson()

    /** 仅用于同步 refresh，不经过任何拦截器，避免递归 */
    private val refreshClient = OkHttpClient.Builder()
        .connectTimeout(NetworkConstants.REFRESH_CLIENT_TIMEOUT_SECONDS.toLong(), TimeUnit.SECONDS)
        .readTimeout(NetworkConstants.REFRESH_CLIENT_TIMEOUT_SECONDS.toLong(), TimeUnit.SECONDS)
        .build()

    private fun parseMessageFromBody(bodyStr: String?): String? {
        if (bodyStr.isNullOrBlank()) return null
        return try {
            (gson.fromJson(bodyStr, JsonObject::class.java)?.get("message") as? JsonPrimitive)?.asString
        } catch (_: Exception) {
            null
        }
    }

    /** 同步调用 /api/token/refresh，成功返回 Pair(accessToken, refreshToken)，失败返回 null */
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

    private val authInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder().apply {
            tokenProvider?.invoke()?.takeIf { it.isNotBlank() }?.let { token ->
                addHeader("Authorization", "Bearer $token")
            }
        }.build()
        chain.proceed(request)
    }

    private val responseInterceptor = Interceptor { chain ->
        var request = chain.request()
        var response = chain.proceed(request)
        if (response.code == NetworkConstants.HTTP_UNAUTHORIZED) {
            val refreshToken = refreshTokenProvider?.invoke()?.takeIf { it.isNotBlank() }
            if (refreshToken != null) {
                val newTokens = doSilentRefresh(refreshToken)
                if (newTokens != null) {
                    onTokensRefreshed?.invoke(newTokens.first, newTokens.second)
                    request = request.newBuilder()
                        .removeHeader("Authorization")
                        .addHeader("Authorization", "Bearer ${newTokens.first}")
                        .build()
                    response.close()
                    response = chain.proceed(request)
                    if (response.isSuccessful) return@Interceptor response
                }
            }
            val bodyStr = response.peekBody(Long.MAX_VALUE).string()
            val message = parseMessageFromBody(bodyStr)
            onClearToken?.invoke()
            GlobalEventManager.emit(GlobalEvent.Unauthorized)
            throw AppException(message ?: NetworkConstants.MESSAGE_LOGIN_EXPIRED, NetworkConstants.HTTP_UNAUTHORIZED)
        }
        if (!response.isSuccessful) {
            val bodyStr = response.peekBody(Long.MAX_VALUE).string()
            val message = parseMessageFromBody(bodyStr)
            GlobalEventManager.emit(GlobalEvent.ShowToast(message ?: "请求失败"))
            throw AppException(message ?: "请求失败", response.code)
        }
        response
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(NetworkConstants.CONNECT_TIMEOUT_SECONDS.toLong(), TimeUnit.SECONDS)
        .readTimeout(NetworkConstants.READ_WRITE_TIMEOUT_SECONDS.toLong(), TimeUnit.SECONDS)
        .writeTimeout(NetworkConstants.READ_WRITE_TIMEOUT_SECONDS.toLong(), TimeUnit.SECONDS)
        .addInterceptor(MockInterceptor())
        .addInterceptor(authInterceptor)
        .addInterceptor(responseInterceptor)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun <T> create(service: Class<T>): T = retrofit.create(service)

    inline fun <reified T> create(): T = create(T::class.java)
}
