package cn.gdeiassistant.network

import cn.gdeiassistant.BuildConfig
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

enum class NetworkEnvironment(
    val storageValue: String,
    val baseUrl: String
) {
    DEV(
        storageValue = "dev",
        baseUrl = BuildConfig.BASE_URL_DEV
    ),
    STAGING(
        storageValue = "staging",
        baseUrl = BuildConfig.BASE_URL_STAGING
    ),
    PROD(
        storageValue = "prod",
        baseUrl = BuildConfig.BASE_URL_PROD
    );

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
