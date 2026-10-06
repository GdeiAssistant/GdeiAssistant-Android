package cn.gdeiassistant.network

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Request

/**
 * 图片加载鉴权判定：
 * - 同源 `/api/social/users/{uuid}/avatar`
 * - 同源 `/api/social/conversations/{id}/messages/{messageId}/image`
 * 不改写外部 URL；不同 scheme/host/port 一律不加 token。
 */
object SocialImageAuthSupport {

    private val AUTHENTICATED_AVATAR_PATH =
        Regex("^/api/social/users/[^/]+/avatar$")
    private val AUTHENTICATED_CHAT_IMAGE_PATH =
        Regex("^/api/social/conversations/[A-Za-z0-9_-]+/messages/[A-Za-z0-9_-]+/image$")

    fun isAuthenticatedAvatarPath(encodedPath: String): Boolean {
        return AUTHENTICATED_AVATAR_PATH.matches(encodedPath)
    }

    fun isAuthenticatedChatImagePath(encodedPath: String): Boolean {
        return AUTHENTICATED_CHAT_IMAGE_PATH.matches(encodedPath)
    }

    fun isAuthenticatedSocialImagePath(encodedPath: String): Boolean {
        return isAuthenticatedAvatarPath(encodedPath) || isAuthenticatedChatImagePath(encodedPath)
    }

    fun isSameOrigin(requestUrl: HttpUrl, apiBase: HttpUrl): Boolean {
        return requestUrl.scheme.equals(apiBase.scheme, ignoreCase = true) &&
            requestUrl.host.equals(apiBase.host, ignoreCase = true) &&
            requestUrl.port == apiBase.port
    }

    fun shouldAttachAuthorization(requestUrl: HttpUrl, apiBase: HttpUrl): Boolean {
        return isSameOrigin(requestUrl, apiBase) &&
            isAuthenticatedSocialImagePath(requestUrl.encodedPath) &&
            requestUrl.username.isEmpty() && requestUrl.password.isEmpty() &&
            requestUrl.encodedQuery == null && requestUrl.fragment == null
    }

    fun shouldAttachAuthorization(requestUrl: String, apiBaseUrl: String): Boolean {
        val request = requestUrl.toHttpUrlOrNull() ?: return false
        val apiBase = normalizeApiBaseUrl(apiBaseUrl).toHttpUrlOrNull() ?: return false
        return shouldAttachAuthorization(request, apiBase)
    }

    /**
     * 若目标为同源鉴权社交图片则附加 Authorization；否则原样返回。
     * Token 只进 header，不进 URL / 缓存 key。
     */
    fun applyAuthIfNeeded(request: Request, apiBase: HttpUrl, bearerToken: String?): Request {
        if (request.method != "GET" || !shouldAttachAuthorization(request.url, apiBase)) {
            return request
        }
        val token = bearerToken?.trim().orEmpty()
        if (token.isEmpty()) {
            return request
        }
        return request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
    }
}
