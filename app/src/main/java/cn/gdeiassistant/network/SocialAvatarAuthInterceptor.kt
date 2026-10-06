package cn.gdeiassistant.network

import cn.gdeiassistant.data.SessionManager
import cn.gdeiassistant.data.SettingsRepository
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 图片专用：仅对同源社交鉴权图片路径附加 Bearer：
 * - `/api/social/users/{uuid}/avatar`
 * - `/api/social/conversations/{id}/messages/{messageId}/image`
 * 不改写 URL；不触碰外部 CDN / 不同 port/scheme。
 */
@Singleton
class SocialAvatarAuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val apiBase = SettingsRepository.currentNetworkEnvironmentSync().httpUrl
        val token = sessionManager.currentToken()
        val authorized = SocialImageAuthSupport.applyAuthIfNeeded(
            request = chain.request(),
            apiBase = apiBase,
            bearerToken = token
        )
        return chain.proceed(authorized)
    }
}
