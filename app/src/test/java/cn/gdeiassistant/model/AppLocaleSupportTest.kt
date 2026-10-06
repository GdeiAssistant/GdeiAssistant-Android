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

    @Test
    fun hongKongAndMacauVariantsUseTheHongKongCatalog() {
        listOf("zh-HK", "ZH_hant_HK", "zh-MO", "zh_Hant_MO", "zh-Hant-HK-u-nu-hanidec").forEach {
            assertEquals(it, "zh-HK", AppLocaleSupport.normalizeLocale(it))
        }
        assertEquals("zh-HK", AppLocaleSupport.detectSystemLocale(java.util.Locale.forLanguageTag("zh-MO")))
    }

    @Test
    fun acceptLanguageUsesFirstItemWithoutQualityOrWhitespace() {
        assertEquals("zh-HK", AppLocaleSupport.normalizeLocale(" ZH_HANT_HK ;q=0.9, en;q=0.8 "))
        assertEquals("zh-TW", AppLocaleSupport.normalizeLocale("zh-Hant;q=0.7, zh-CN"))
        assertEquals("en", AppLocaleSupport.normalizeLocale("en-GB, ja;q=0.5"))
    }

    @Test
    fun repeatedLanguageSwitchesDoNotKeepAPreviousRegionOrLanguage() {
        try {
            listOf("zh-CN", "zh-HK", "zh-TW", "en", "ja", "ko", "zh-CN").forEach { locale ->
                AppLocaleSupport.setCurrentLocale(locale)
                assertEquals(locale, AppLocaleSupport.currentLocale())
                assertEquals(locale, AppLocaleSupport.normalizeLocale(AppLocaleSupport.localeObject().toLanguageTag()))
            }
        } finally {
            AppLocaleSupport.setCurrentLocale(null)
        }
    }
}
