package cn.gdeiassistant.network

import cn.gdeiassistant.data.SettingsRepository
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BaseUrlOverrideInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val targetUrl = SettingsRepository.currentNetworkEnvironmentSync().httpUrl
        val currentUrl = request.url

        val rewrittenUrl = currentUrl.newBuilder()
            .scheme(targetUrl.scheme)
            .host(targetUrl.host)
            .port(targetUrl.port)
            .build()

        return chain.proceed(
            request.newBuilder()
                .url(rewrittenUrl)
                .build()
        )
    }
}
