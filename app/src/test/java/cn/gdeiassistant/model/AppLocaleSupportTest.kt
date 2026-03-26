package cn.gdeiassistant.model

import org.junit.Assert.assertEquals
import org.junit.Test

class AppLocaleSupportTest {

    @Test
    fun normalizeLocaleLimitsValuesToSupportedSixLocales() {
        assertEquals("zh-CN", AppLocaleSupport.normalizeLocale(null))
        assertEquals("zh-CN", AppLocaleSupport.normalizeLocale("fr-FR"))
        assertEquals("zh-HK", AppLocaleSupport.normalizeLocale("zh-Hant-HK"))
        assertEquals("zh-TW", AppLocaleSupport.normalizeLocale("zh-Hant"))
        assertEquals("en", AppLocaleSupport.normalizeLocale("en-US"))
        assertEquals("ja", AppLocaleSupport.normalizeLocale("ja-JP"))
        assertEquals("ko", AppLocaleSupport.normalizeLocale("ko-KR"))
    }

    @Test
    fun currentLocalePrefersAppliedAppLocaleOverSystemLocale() {
        try {
            AppLocaleSupport.setCurrentLocale("en-US")
            assertEquals("en", AppLocaleSupport.currentLocale())
            assertEquals("en", AppLocaleSupport.localeObject().language)
        } finally {
            AppLocaleSupport.setCurrentLocale(null)
        }
    }
}
