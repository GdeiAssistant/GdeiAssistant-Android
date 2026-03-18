package cn.gdeiassistant

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import cn.gdeiassistant.data.SessionManager
import cn.gdeiassistant.data.SettingsRepository
import cn.gdeiassistant.model.Access
import cn.gdeiassistant.network.RetrofitClient
import dagger.hilt.android.HiltAndroidApp
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class GdeiAssistantApplication : Application() {

    var access: Access? = null
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var sessionManager: SessionManager
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var _token: String? = null
    var token: String?
        get() = _token
        set(value) { _token = value }

    private var _username: String? = null
    var username: String?
        get() = _username
        set(value) { _username = value }

    private var cookieJar: CookieJar? = null

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            settingsRepository.initializeMockModeCache()
        }
        token = sessionManager.currentToken()
        username = sessionManager.currentUsername()
        RetrofitClient.tokenProvider = sessionManager::currentToken
        RetrofitClient.refreshTokenProvider = sessionManager::currentRefreshToken
        RetrofitClient.onTokensRefreshed = { accessToken, refreshToken ->
            sessionManager.saveTokens(accessToken, refreshToken)
        }
        RetrofitClient.onClearToken = {
            sessionManager.clearTokens()
        }
        createServiceForegroundNotificationChannel()
    }

    fun createServiceForegroundNotificationChannel() {
        val channelId = "service"
        val channelName = "系统通知"
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_NONE
        ).apply {
            enableLights(true)
            lightColor = Color.RED
            setShowBadge(true)
        }
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)
    }

    fun getCookieJar(): CookieJar {
        if (cookieJar != null) return cookieJar!!
        cookieJar = object : CookieJar {
            private val cookieStore = mutableMapOf<String, List<Cookie>>()
            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                cookieStore[url.host] = cookies
            }
            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                return cookieStore[url.host] ?: emptyList()
            }
        }
        return cookieJar!!
    }

    fun setCookieJar(jar: CookieJar?) {
        cookieJar = jar
    }

    fun removeAllData() {
        cookieJar = null
        _username = null
        _token = null
    }
}
