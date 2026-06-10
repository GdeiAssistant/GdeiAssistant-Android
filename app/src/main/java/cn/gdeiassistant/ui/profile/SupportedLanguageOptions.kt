package cn.gdeiassistant.ui.profile

data class SupportedLanguageOption(
    val code: String,
    val nativeName: String
)

val supportedLanguageOptions = listOf(
    SupportedLanguageOption("zh-CN", "简体中文"),
    SupportedLanguageOption("zh-HK", "繁體中文（港澳）"),
    SupportedLanguageOption("zh-TW", "繁體中文（台灣）"),
    SupportedLanguageOption("en", "English"),
    SupportedLanguageOption("ja", "日本語"),
    SupportedLanguageOption("ko", "한국어")
)
