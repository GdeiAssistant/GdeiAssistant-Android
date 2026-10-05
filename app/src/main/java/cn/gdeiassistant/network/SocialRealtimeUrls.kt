package cn.gdeiassistant.network

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

/**
 * 从 HTTP(S) baseUrl 构造社交 realtime WebSocket URL。
 * OkHttp [HttpUrl] 仅支持 http/https，故返回字符串交给 [okhttp3.Request.Builder.url]。
 * 不携带 token；路径固定为 `/api/social/realtime`，不因 base 已含 `/api` 而重复拼接。
 */
object SocialRealtimeUrls {

    fun build(baseHttpUrl: HttpUrl): String {
        val scheme = if (baseHttpUrl.isHttps) "wss" else "ws"
        val port = when {
            baseHttpUrl.isHttps && baseHttpUrl.port != 443 -> ":${baseHttpUrl.port}"
            !baseHttpUrl.isHttps && baseHttpUrl.port != 80 -> ":${baseHttpUrl.port}"
            else -> ""
        }
        return "$scheme://${baseHttpUrl.host}$port/api/social/realtime"
    }

    fun build(rawBaseUrl: String): String {
        return build(normalizeApiBaseUrl(rawBaseUrl).toHttpUrl())
    }
}
