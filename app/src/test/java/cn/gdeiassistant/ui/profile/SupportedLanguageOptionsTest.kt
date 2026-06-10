package cn.gdeiassistant.ui.profile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SupportedLanguageOptionsTest {

    @Test
    fun traditionalChineseOptionsUseHongKongMacauAndTaiwanLabels() {
        val labelsByCode = supportedLanguageOptions.associate { it.code to it.nativeName }

        assertEquals("繁體中文（港澳）", labelsByCode["zh-HK"])
        assertEquals("繁體中文（台灣）", labelsByCode["zh-TW"])
        assertFalse(supportedLanguageOptions.any { it.nativeName == "繁體中文（香港）" })
    }

    @Test
    fun supportedLanguageOptionsKeepExpectedLocaleOrder() {
        assertEquals(
            listOf("zh-CN", "zh-HK", "zh-TW", "en", "ja", "ko"),
            supportedLanguageOptions.map { it.code }
        )
    }
}
