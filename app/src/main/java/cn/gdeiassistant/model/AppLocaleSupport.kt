package cn.gdeiassistant.model

import java.util.Locale

object AppLocaleSupport {
    const val fallbackLocale: String = "zh-CN"

    @Volatile
    private var currentLocaleOverride: String? = null

    fun normalizeLocale(locale: String?): String {
        val normalized = locale
            ?.trim()
            ?.replace('_', '-')
            .orEmpty()

        if (normalized.isEmpty()) {
            return fallbackLocale
        }

        val lower = normalized.lowercase(Locale.ROOT)
        return when {
            lower == "zh-cn" || lower == "zh-hans" || lower == "zh-hans-cn" || lower == "zh" -> "zh-CN"
            lower == "zh-hk" || lower == "zh-hant-hk" -> "zh-HK"
            lower == "zh-tw" || lower == "zh-hant" || lower == "zh-hant-tw" -> "zh-TW"
            lower.startsWith("zh-hk") -> "zh-HK"
            lower.startsWith("zh-tw") || lower.startsWith("zh-hant") -> "zh-TW"
            lower.startsWith("zh") -> "zh-CN"
            lower.startsWith("en") -> "en"
            lower.startsWith("ja") -> "ja"
            lower.startsWith("ko") -> "ko"
            else -> fallbackLocale
        }
    }

    fun detectSystemLocale(defaultLocale: Locale = Locale.getDefault()): String {
        return normalizeLocale(defaultLocale.toLanguageTag())
    }

    fun currentLocale(): String {
        return currentLocaleOverride ?: detectSystemLocale()
    }

    fun setCurrentLocale(locale: String?) {
        currentLocaleOverride = locale?.let(::normalizeLocale)
    }

    fun localeObject(locale: String? = currentLocale()): Locale {
        return Locale.forLanguageTag(normalizeLocale(locale))
    }
}
