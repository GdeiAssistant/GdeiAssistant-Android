package cn.gdeiassistant.data

/**
 * 私信正文校验：trim 后 1–1000 个 Unicode 字符（按 code point 计数）。
 */
object SocialTextSupport {
    const val MIN_MESSAGE_CODE_POINTS = 1
    const val MAX_MESSAGE_CODE_POINTS = 1000

    fun normalizeMessageContent(raw: String?): String = raw.orEmpty().trim()

    fun unicodeLength(value: String): Int {
        return value.codePointCount(0, value.length)
    }

    fun isValidMessageContent(raw: String?): Boolean {
        val content = normalizeMessageContent(raw)
        if (content.isEmpty()) return false
        val length = unicodeLength(content)
        return length in MIN_MESSAGE_CODE_POINTS..MAX_MESSAGE_CODE_POINTS
    }
}
