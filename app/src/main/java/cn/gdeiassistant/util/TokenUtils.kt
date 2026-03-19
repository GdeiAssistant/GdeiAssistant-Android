package cn.gdeiassistant.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.auth0.android.jwt.DecodeException
import com.auth0.android.jwt.JWT
import java.util.Date

object TokenUtils {
    private const val PREFS_NAME = "GdeiAssistant_secure"
    private const val KEY_ACCESS = "AccessToken"
    private const val LEGACY_KEY_REFRESH = "RefreshToken"

    private fun prefs(context: Context): SharedPreferences {
        val appContext = context.applicationContext
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            appContext,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @JvmStatic fun GetUserAccessToken(context: Context): String? = prefs(context).getString(KEY_ACCESS, null)
    @JvmStatic fun ClearUserToken(context: Context) { prefs(context).edit().clear().apply() }
    @JvmStatic fun SaveUserToken(accessToken: String, context: Context) {
        prefs(context).edit()
            .putString(KEY_ACCESS, accessToken)
            .remove(LEGACY_KEY_REFRESH)
            .apply()
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
