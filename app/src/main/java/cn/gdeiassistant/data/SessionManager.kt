package cn.gdeiassistant.data

import android.content.Context
import cn.gdeiassistant.GdeiAssistantApplication
import cn.gdeiassistant.util.TokenUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val app: GdeiAssistantApplication?
        get() = context.applicationContext as? GdeiAssistantApplication

    fun currentToken(): String? {
        return app?.token ?: TokenUtils.GetUserAccessToken(context)
    }

    fun currentRefreshToken(): String? = TokenUtils.GetUserRefreshToken(context)

    fun currentUsername(): String? {
        return app?.username ?: TokenUtils.GetAccessTokenUsername(currentToken())
    }

    fun saveTokens(accessToken: String, refreshToken: String, username: String? = null) {
        TokenUtils.SaveUserToken(accessToken, refreshToken, context)
        app?.token = accessToken
        username?.trim()?.takeIf(String::isNotBlank)?.let { resolvedUsername ->
            app?.username = resolvedUsername
        }
    }

    fun clearTokens() {
        app?.removeAllData()
        TokenUtils.ClearUserToken(context)
    }
}
