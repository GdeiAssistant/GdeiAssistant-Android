package cn.gdeiassistant.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Locale

class ProfileOptionsContractTest {
    private val originalLocale: Locale = Locale.getDefault()

    private fun withLocale(localeTag: String, block: () -> Unit) {
        Locale.setDefault(Locale.forLanguageTag(localeTag))
        try {
            block()
        } finally {
            Locale.setDefault(originalLocale)
        }
    }

    @Test
    fun facultyNameAndMajorOptionsStayAligned() = withLocale("zh-CN") {
        val options = ProfileFormSupport.defaultOptions

        assertEquals("中文系", options.facultyNameFor(3))
        assertEquals(
            listOf("未选择", "汉语言文学", "历史学", "秘书学"),
            options.majorOptionsFor("中文系")
        )
        assertEquals("secretarial_studies", options.majorCodeFor("中文系", "秘书学"))
        assertEquals("秘书学", options.majorLabelFor("中文系", "secretarial_studies"))
        assertNull(options.facultyNameFor(999))
    }

    @Test
    fun communityTypeTitlesReuseSharedProfileDictionarySource() = withLocale("zh-CN") {
        assertEquals("数码配件", marketplaceTypeTitle(3))
        assertEquals("数码配件", lostFoundItemTypeTitle(10))
        assertEquals("失物招领", ProfileFormSupport.defaultOptions.lostFoundModeTitle(1))
    }

    @Test
    fun defaultOptionsFollowCurrentLocale() = withLocale("en-US") {
        val options = ProfileFormSupport.defaultOptions

        assertEquals("Department of Chinese", options.facultyNameFor(3))
        assertEquals(
            listOf("Not selected", "Chinese Language and Literature", "History", "Secretarial Studies"),
            options.majorOptionsFor("Department of Chinese")
        )
        assertEquals("Digital Accessories", marketplaceTypeTitle(3))
        assertEquals("Found Item Notice", ProfileFormSupport.defaultOptions.lostFoundModeTitle(1))
    }
}
