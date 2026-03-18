package cn.gdeiassistant.util

import android.content.Context
import android.content.SharedPreferences
import com.auth0.android.jwt.DecodeException
import com.auth0.android.jwt.JWT
import java.util.Date

object TokenUtils {
    private const val PREFS_NAME = "GdeiAssistant"
    private const val KEY_ACCESS = "AccessToken"
    private const val KEY_REFRESH = "RefreshToken"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    @JvmStatic fun GetUserAccessToken(context: Context): String? = prefs(context).getString(KEY_ACCESS, null)
    @JvmStatic fun GetUserRefreshToken(context: Context): String? = prefs(context).getString(KEY_REFRESH, null)
    @JvmStatic fun ClearUserToken(context: Context) { prefs(context).edit().clear().apply() }
    @JvmStatic fun SaveUserToken(accessToken: String, refreshToken: String, context: Context) {
        prefs(context).edit().putString(KEY_ACCESS, accessToken).putString(KEY_REFRESH, refreshToken).apply()
    }

    @JvmStatic
    fun ValidateTokenTimestamp(token: String?): Boolean {
        if (token.isNullOrBlank()) return false
        return try {
            val expireTime = JWT(token).getClaim("expireTime").asString()
            if (expireTime.isNullOrBlank()) return false
            val expireMillis = expireTime.toLongOrNull() ?: return false
            TimeUtils.GetTimestampDifference(Date(), Date(expireMillis), TimeUtils.TimeUnit.HOUR) > 1
        } catch (_: DecodeException) { false } catch (_: NumberFormatException) { false }
    }

    @JvmStatic
    fun GetAccessTokenUsername(accessToken: String?): String? {
        if (accessToken.isNullOrBlank()) return null
        return try { JWT(accessToken).getClaim("username").asString() } catch (_: DecodeException) { null }
    }
}
