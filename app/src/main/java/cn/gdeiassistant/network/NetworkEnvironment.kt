package cn.gdeiassistant.network

import cn.gdeiassistant.BuildConfig
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

internal fun normalizeApiBaseUrl(rawBaseUrl: String): String {
    val trimmed = rawBaseUrl.trim()
    val withoutTrailingSlash = trimmed.removeSuffix("/")
    val rootBaseUrl = if (withoutTrailingSlash.endsWith("/api", ignoreCase = true)) {
        withoutTrailingSlash.removeSuffix("/api")
    } else {
        withoutTrailingSlash
    }

    return "$rootBaseUrl/"
}

enum class NetworkEnvironment(
    val storageValue: String,
    rawBaseUrl: String
) {
    DEV(
        storageValue = "dev",
        rawBaseUrl = BuildConfig.BASE_URL_DEV
    ),
    STAGING(
        storageValue = "staging",
        rawBaseUrl = BuildConfig.BASE_URL_STAGING
    ),
    PROD(
        storageValue = "prod",
        rawBaseUrl = BuildConfig.BASE_URL_PROD
    );

    val baseUrl: String = normalizeApiBaseUrl(rawBaseUrl)

    val httpUrl: HttpUrl
        get() = baseUrl.toHttpUrl()

    companion object {
        fun default(): NetworkEnvironment {
            return entries.firstOrNull {
                it.storageValue.equals(BuildConfig.DEFAULT_NETWORK_ENVIRONMENT, ignoreCase = true)
            } ?: PROD
        }

        fun fromStorage(value: String?): NetworkEnvironment {
            return entries.firstOrNull {
                it.storageValue.equals(value?.trim(), ignoreCase = true)
            } ?: default()
        }
    }
}
