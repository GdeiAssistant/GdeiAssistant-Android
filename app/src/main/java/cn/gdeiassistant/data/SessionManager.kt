package cn.gdeiassistant.data

import android.content.Context
import cn.gdeiassistant.util.TokenUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val accessTokenRef = AtomicReference(TokenUtils.GetUserAccessToken(context))
    private val usernameRef = AtomicReference(resolveUsername(accessTokenRef.get(), null))
    private val cookieStore = ConcurrentHashMap<String, MutableList<Cookie>>()
    private val cookieLock = Any()

    val cookieJar: CookieJar by lazy {
        object : CookieJar {
            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                synchronized(cookieLock) {
                    val storedCookies = cookieStore[url.host]?.toMutableList() ?: mutableListOf()
                    cookies.forEach { cookie ->
                        storedCookies.removeAll {
                            it.name == cookie.name &&
                                it.domain == cookie.domain &&
                                it.path == cookie.path
                        }
                        if (cookie.expiresAt >= System.currentTimeMillis()) {
                            storedCookies += cookie
                        }
                    }
                    if (storedCookies.isEmpty()) {
                        cookieStore.remove(url.host)
                    } else {
                        cookieStore[url.host] = storedCookies
                    }
                }
            }

            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                synchronized(cookieLock) {
                    val now = System.currentTimeMillis()
                    val storedCookies = cookieStore[url.host].orEmpty()
                    val validCookies = storedCookies.filter { cookie ->
                        cookie.expiresAt >= now && cookie.matches(url)
                    }
                    if (validCookies.size != storedCookies.size) {
                        if (validCookies.isEmpty()) {
                            cookieStore.remove(url.host)
                        } else {
                            cookieStore[url.host] = validCookies.toMutableList()
                        }
                    }
                    return validCookies
                }
            }
        }
    }

    fun hasActiveSession(): Boolean = !currentToken().isNullOrBlank()

    fun currentToken(): String? {
        return accessTokenRef.get()
            ?.takeIf(String::isNotBlank)
            ?: TokenUtils.GetUserAccessToken(context)?.also(accessTokenRef::set)
    }

    fun currentUsername(): String? {
        return usernameRef.get()
            ?.takeIf(String::isNotBlank)
            ?: resolveUsername(currentToken(), null)?.also(usernameRef::set)
    }

    fun saveToken(accessToken: String, username: String? = null) {
        TokenUtils.SaveUserToken(accessToken, context)
        accessTokenRef.set(accessToken)
        usernameRef.set(resolveUsername(accessToken, username))
    }

    fun clearTokens() {
        TokenUtils.ClearUserToken(context)
        accessTokenRef.set(null)
        usernameRef.set(null)
        clearCookies()
    }

    fun clearCookies() {
        synchronized(cookieLock) {
            cookieStore.clear()
        }
    }

    private fun resolveUsername(accessToken: String?, username: String?): String? {
        return username?.trim()?.takeIf(String::isNotBlank)
            ?: TokenUtils.GetAccessTokenUsername(accessToken)
    }
}
