package cn.gdeiassistant.data

import cn.gdeiassistant.model.ProfileFormSupport
import cn.gdeiassistant.network.api.ProfileDictionaryOptionDto
import cn.gdeiassistant.network.api.ProfileFacultyOptionDto
import cn.gdeiassistant.network.api.ProfileOptionsDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProfileOptionsRepositoryTest {

    @Test
    fun mapProfileOptionsNormalizesMajorsAndFallsBackToSharedDefaults() {
        val options = mapProfileOptions(
            ProfileOptionsDto(
                faculties = listOf(
                    ProfileFacultyOptionDto(
                        code = 3,
                        label = " 中文系 ",
                        majors = listOf("汉语言文学", " ", "秘书学")
                    )
                ),
                marketplaceItemTypes = listOf(
                    ProfileDictionaryOptionDto(code = 8, label = " 图书教材 ")
                ),
                lostFoundItemTypes = emptyList(),
                lostFoundModes = listOf(
                    ProfileDictionaryOptionDto(code = 1, label = "失物招领")
                )
            )
        )

        assertEquals("中文系", options.facultyNameFor(3))
        assertEquals(
            listOf(ProfileFormSupport.UnselectedOption, "汉语言文学", "秘书学"),
            options.majorOptionsFor("中文系")
        )
        assertEquals("图书教材", options.marketplaceTypeTitle(8))
        assertEquals(
            ProfileFormSupport.defaultOptions.lostFoundItemTypes,
            options.lostFoundItemTypes
        )
        assertEquals("失物招领", options.lostFoundModeTitle(1))
    }

    @Test
    fun invalidDictionaryEntriesAreDroppedBeforeLookup() {
        val options = mapProfileOptions(
            ProfileOptionsDto(
                faculties = listOf(
                    ProfileFacultyOptionDto(code = null, label = "无效院系", majors = listOf("A")),
                    ProfileFacultyOptionDto(code = 11, label = "计算机科学系", majors = listOf("软件工程"))
                ),
                marketplaceItemTypes = listOf(
                    ProfileDictionaryOptionDto(code = null, label = "无效类型"),
                    ProfileDictionaryOptionDto(code = 3, label = "数码配件")
                ),
                lostFoundItemTypes = listOf(
                    ProfileDictionaryOptionDto(code = 10, label = "数码配件")
                ),
                lostFoundModes = listOf(
                    ProfileDictionaryOptionDto(code = 0, label = "寻物启事")
                )
            )
        )

        assertEquals(11, options.facultyCodeFor("计算机科学系"))
        assertNull(options.facultyCodeFor("无效院系"))
        assertEquals("数码配件", options.marketplaceTypeTitle(3))
    }
}
