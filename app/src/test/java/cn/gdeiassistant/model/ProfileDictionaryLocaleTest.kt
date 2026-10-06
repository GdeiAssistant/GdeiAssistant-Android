package cn.gdeiassistant.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ProfileDictionaryLocaleTest {
    @Test
    fun cachedOptionsUseCurrentCodeLabelsAcrossAllSixLanguages() {
        val cached = LocalizedProfileCatalog.catalogForLocale("zh-CN").defaultOptions
        val originalFaculty = cached.faculties.first { it.code == 11 }
        try {
            listOf("zh-HK", "zh-TW", "en", "ja", "ko", "zh-CN").forEach { locale ->
                AppLocaleSupport.setCurrentLocale(locale)
                val expected = LocalizedProfileCatalog.catalogForLocale(locale).defaultOptions
                assertEquals(locale, expected.facultyNameFor(11), cached.facultyNameFor(11))
                assertEquals(locale, expected.majorOptionsFor(expected.facultyNameFor(11).orEmpty()), cached.majorOptionsFor(originalFaculty.label))
                assertEquals(locale, expected.marketplaceTypeTitle(3), cached.marketplaceTypeTitle(3))
                assertEquals(locale, expected.lostFoundItemTypeTitle(10), cached.lostFoundItemTypeTitle(10))
                assertEquals(locale, expected.lostFoundModeTitle(1), cached.lostFoundModeTitle(1))
                assertEquals(11, cached.facultyCodeFor(expected.facultyNameFor(11).orEmpty()))
                assertEquals("software_engineering", cached.majorCodeFor(originalFaculty.label, expected.majorLabelFor(expected.facultyNameFor(11).orEmpty(), "software_engineering").orEmpty()))
                assertFalse(cached.canSelectMajor(LocalizedProfileCatalog.catalogForLocale("zh-CN").unselectedLabel))
            }
        } finally {
            AppLocaleSupport.setCurrentLocale(null)
        }
        assertEquals("计算机科学系", originalFaculty.label)
        assertEquals(cached.faculties.map { it.code }, cached.localizedForLocale("en").faculties.map { it.code })
    }

    @Test
    fun categoryObjectsDoNotKeepTheirCachedTitleOrChangeTheSelectedIds() {
        val marketplace = MarketplaceTypeOption(3, "数码配件")
        val lost = LostFoundItemTypeOption(10, "数码配件")
        try {
            AppLocaleSupport.setCurrentLocale("en")
            assertEquals("Digital Accessories", marketplace.displayTitle())
            assertEquals("Digital Accessories", lost.displayTitle())
            AppLocaleSupport.setCurrentLocale("zh-HK")
            assertEquals("數碼配件", marketplace.displayTitle())
            assertEquals("數碼配件", lost.displayTitle())
            assertEquals(3, marketplace.id)
            assertEquals(10, lost.id)
            assertEquals("数码配件", marketplace.title)
        } finally {
            AppLocaleSupport.setCurrentLocale(null)
        }
    }

    @Test
    fun unknownDictionaryCodesAndFreeTextArePreserved() {
        val unknown = ProfileOptions(
            faculties = listOf(ProfileFacultyOption(999, "Original Faculty", listOf(ProfileMajorOption("custom", "Original Major")))),
            marketplaceItemTypes = listOf(ProfileDictionaryOption(999, "Original Type")),
            lostFoundItemTypes = listOf(ProfileDictionaryOption(999, "Original Lost Type")),
            lostFoundModes = listOf(ProfileDictionaryOption(999, "Original Mode"))
        )
        assertEquals(unknown, unknown.localizedForLocale("zh-HK"))
        assertEquals("Original Type", MarketplaceTypeOption(999, "Original Type").displayTitle("ja"))
        assertEquals("Original Lost Type", LostFoundItemTypeOption(999, "Original Lost Type").displayTitle("ko"))
        assertEquals("软件工程同学的个人简介", LocalizedProfileCatalog.localizeFacultyName("软件工程同学的个人简介", "zh-HK"))
        assertEquals("Department of Computer Science", LocalizedProfileCatalog.localizeFacultyName("計算機科學系", "en"))
        assertEquals("計算機科學系", LocalizedProfileCatalog.localizeFacultyName("Department of Computer Science", "zh-TW"))
    }

    @Test
    fun cachedMarketplaceDetailUsesCodesAndPreservesUnknownValues() {
        val item = MarketplaceItem("demo", "用户标题", 1.0, "用户简介", "用户名字", "", "用户位置", MarketplaceItemState.SELLING, emptyList())
        val detail = MarketplaceDetail(item, "数码配件", "用户说明", "", sellerCollege = "计算机科学系",
            sellerMajor = "software_engineering", typeId = 3, sellerFacultyCode = 11)
        assertEquals("Digital Accessories", detail.displayCondition("en"))
        assertEquals("計算機科學系", detail.displaySellerCollege("zh-HK"))
        assertEquals("軟件工程", detail.displaySellerMajor("zh-HK"))
        assertEquals("軟體工程", detail.displaySellerMajor("zh-TW"))
        assertEquals("software_engineering", detail.sellerMajor)
        assertEquals(item, detail.item)
        val unknown = detail.copy(typeId = 999, sellerFacultyCode = 999, sellerCollege = "自定义院系", sellerMajor = "自定义专业")
        assertEquals("数码配件", unknown.displayCondition("en"))
        assertEquals("自定义院系", unknown.displaySellerCollege("en"))
        assertEquals("自定义专业", unknown.displaySellerMajor("en"))
    }
}
