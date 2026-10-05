package cn.gdeiassistant.network

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

/**
 * 私信图片鉴权端点 URL；不把 token 放进 query，仅供 Coil/OkHttp 同源 Bearer 加载。
 */
object SocialChatImageUrls {

    fun messageImage(
        baseHttpUrl: HttpUrl,
        conversationId: String,
        messageId: String
    ): String {
        val conv = conversationId.trim()
        val msg = messageId.trim()
        val segment = Regex("[A-Za-z0-9_-]+")
        require(segment.matches(conv) && segment.matches(msg))
        return baseHttpUrl.newBuilder()
            .username("")
            .password("")
            .encodedPath("/api/social/conversations/$conv/messages/$msg/image")
            .encodedQuery(null)
            .fragment(null)
            .build()
            .toString()
    }

    fun messageImage(rawBaseUrl: String, conversationId: String, messageId: String): String {
        return messageImage(
            baseHttpUrl = normalizeApiBaseUrl(rawBaseUrl).toHttpUrl(),
            conversationId = conversationId,
            messageId = messageId
        )
    }
}
