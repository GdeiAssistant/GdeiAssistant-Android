package cn.gdeiassistant.network

import cn.gdeiassistant.data.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocaleInterceptor @Inject constructor(
    private val settingsRepository: SettingsRepository
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val locale = runBlocking { settingsRepository.locale.first() }
        val request = chain.request().newBuilder()
            .header("Accept-Language", locale)
            .build()
        return chain.proceed(request)
    }
}
