package cn.gdeiassistant.network

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

/**
 * 解析社交头像加载地址。
 * - 绝对 http(s) URL：原样使用（公开 CDN 直达）
 * - 相对路径：挂到当前环境 base（不改写外链）
 * - null/blank：返回 null，UI 显示占位，不假造鉴权头像 URL
 * Token 永不进入 query。
 */
object SocialAvatarUrls {

    fun resolve(baseHttpUrl: HttpUrl, userId: String, avatarUrl: String?): String? {
        val id = userId.trim()
        if (id.isBlank()) return null
        val trimmed = avatarUrl?.trim().orEmpty()
        if (trimmed.isEmpty()) {
            return null
        }
        if (trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true)
        ) {
            return trimmed
        }
        if (trimmed.startsWith("/")) {
            return baseHttpUrl.newBuilder()
                .encodedPath(trimmed)
                .encodedQuery(null)
                .build()
                .toString()
        }
        return baseHttpUrl.newBuilder()
            .addPathSegments(trimmed.trimStart('/'))
            .encodedQuery(null)
            .build()
            .toString()
    }

    fun resolve(rawBaseUrl: String, userId: String, avatarUrl: String?): String? {
        return resolve(normalizeApiBaseUrl(rawBaseUrl).toHttpUrl(), userId, avatarUrl)
    }

    /** 显式构造同源鉴权头像端点（仅当调用方已知需要拉取该路径时使用）。 */
    fun authenticatedEndpoint(baseHttpUrl: HttpUrl, userId: String): String {
        return baseHttpUrl.newBuilder()
            .encodedPath("/api/social/users/${userId.trim()}/avatar")
            .encodedQuery(null)
            .build()
            .toString()
    }
}
