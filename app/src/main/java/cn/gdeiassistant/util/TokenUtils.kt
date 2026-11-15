@file:Suppress("DEPRECATION")

package cn.gdeiassistant.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.auth0.android.jwt.DecodeException
import com.auth0.android.jwt.JWT
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.KeyStore

object TokenUtils {
    private const val PREFS_NAME = "GdeiAssistant_secure"
    private const val KEY_ACCESS = "AccessToken"
    private const val LEGACY_KEY_REFRESH = "RefreshToken"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"

    @Volatile
    private var cachedPrefs: SharedPreferences? = null

    private fun createEncryptedPrefs(appContext: Context): SharedPreferences {
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

    private fun resetCorruptedKeyStoreAndPrefs(appContext: Context) {
        runCatching { appContext.deleteSharedPreferences(PREFS_NAME) }
        runCatching {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)
            keyStore.deleteEntry(MasterKey.DEFAULT_MASTER_KEY_ALIAS)
        }
    }

    private fun getOrCreatePrefs(context: Context): SharedPreferences? {
        cachedPrefs?.let { return it }
        synchronized(this) {
            cachedPrefs?.let { return it }
            val appContext = context.applicationContext ?: context
            val created = try {
                createEncryptedPrefs(appContext)
            } catch (_: GeneralSecurityException) {
                resetCorruptedKeyStoreAndPrefs(appContext)
                runCatching { createEncryptedPrefs(appContext) }.getOrNull()
            } catch (_: SecurityException) {
                resetCorruptedKeyStoreAndPrefs(appContext)
                runCatching { createEncryptedPrefs(appContext) }.getOrNull()
            } catch (_: IOException) {
                runCatching { appContext.deleteSharedPreferences(PREFS_NAME) }
                runCatching { createEncryptedPrefs(appContext) }.getOrNull()
            }
            cachedPrefs = created
            return created
        }
    }

    private fun rebuildCorruptedPrefs(context: Context): SharedPreferences? {
        synchronized(this) {
            cachedPrefs = null
            val appContext = context.applicationContext ?: context
            resetCorruptedKeyStoreAndPrefs(appContext)
            val rebuilt = runCatching { createEncryptedPrefs(appContext) }.getOrNull()
            cachedPrefs = rebuilt
            return rebuilt
        }
    }

    @JvmStatic
    fun GetUserAccessToken(context: Context): String? {
        val prefs = getOrCreatePrefs(context) ?: return null
        return try {
            prefs.getString(KEY_ACCESS, null)
        } catch (_: GeneralSecurityException) {
            rebuildCorruptedPrefs(context)
            null
        } catch (_: SecurityException) {
            rebuildCorruptedPrefs(context)
            null
        }
    }

    @JvmStatic
    fun ClearUserToken(context: Context) {
        val prefs = getOrCreatePrefs(context) ?: return
        try {
            prefs.edit { clear() }
        } catch (_: GeneralSecurityException) {
            rebuildCorruptedPrefs(context)
        } catch (_: SecurityException) {
            rebuildCorruptedPrefs(context)
        }
    }

    @JvmStatic
    fun SaveUserToken(accessToken: String, context: Context) {
        val prefs = getOrCreatePrefs(context) ?: return
        try {
            prefs.edit {
                putString(KEY_ACCESS, accessToken)
                remove(LEGACY_KEY_REFRESH)
            }
        } catch (_: GeneralSecurityException) {
            val rebuilt = rebuildCorruptedPrefs(context) ?: return
            runCatching {
                rebuilt.edit {
                    putString(KEY_ACCESS, accessToken)
                    remove(LEGACY_KEY_REFRESH)
                }
            }
        } catch (_: SecurityException) {
            val rebuilt = rebuildCorruptedPrefs(context) ?: return
            runCatching {
                rebuilt.edit {
                    putString(KEY_ACCESS, accessToken)
                    remove(LEGACY_KEY_REFRESH)
                }
            }
        }
    }

    @JvmStatic
    fun GetAccessTokenUsername(accessToken: String?): String? {
        if (accessToken.isNullOrBlank()) return null
        return try { JWT(accessToken).getClaim("username").asString() } catch (_: DecodeException) { null }
    }
}
