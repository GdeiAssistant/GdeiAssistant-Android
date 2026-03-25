package cn.gdeiassistant.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProfileOptionsContractTest {

    @Test
    fun facultyNameAndMajorOptionsStayAligned() {
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
    fun communityTypeTitlesReuseSharedProfileDictionarySource() {
        assertEquals("数码配件", marketplaceTypeTitle(3))
        assertEquals("数码配件", lostFoundItemTypeTitle(10))
        assertEquals("失物招领", ProfileFormSupport.defaultOptions.lostFoundModeTitle(1))
    }
}
