package cn.gdeiassistant.data

import cn.gdeiassistant.model.ProfileFormSupport
import cn.gdeiassistant.network.api.ProfileDictionaryOptionDto
import cn.gdeiassistant.network.api.ProfileFacultyOptionDto
import cn.gdeiassistant.network.api.ProfileMajorOptionDto
import cn.gdeiassistant.network.api.ProfileOptionsDto
import com.google.gson.Gson
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
                        majors = listOf(
                            ProfileMajorOptionDto(code = "chinese_language_literature", label = "汉语言文学"),
                            ProfileMajorOptionDto(code = " ", label = " "),
                            ProfileMajorOptionDto(code = "secretarial_studies", label = "秘书学")
                        )
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
        assertEquals("secretarial_studies", options.majorCodeFor("中文系", "秘书学"))
        assertEquals("秘书学", options.majorLabelFor("中文系", "secretarial_studies"))
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
                    ProfileFacultyOptionDto(
                        code = null,
                        label = "无效院系",
                        majors = listOf(ProfileMajorOptionDto(code = "a", label = "A"))
                    ),
                    ProfileFacultyOptionDto(
                        code = 11,
                        label = "计算机科学系",
                        majors = listOf(ProfileMajorOptionDto(code = "software_engineering", label = "软件工程"))
                    )
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
        assertEquals("software_engineering", options.majorCodeFor("计算机科学系", "软件工程"))
        assertEquals("数码配件", options.marketplaceTypeTitle(3))
    }

    @Test
    fun codeOnlyProfileOptionsReuseSharedCatalogLabels() {
        val fallbackOptions = ProfileFormSupport.defaultOptions
        val facultyName = fallbackOptions.facultyNameFor(11).orEmpty()
        val majorName = fallbackOptions.majorLabelFor(facultyName, "software_engineering").orEmpty()
        val options = mapProfileOptions(
            ProfileOptionsDto(
                faculties = listOf(
                    ProfileFacultyOptionDto(
                        code = 11,
                        label = null,
                        majors = listOf(
                            ProfileMajorOptionDto(code = "unselected", label = null),
                            ProfileMajorOptionDto(code = "software_engineering", label = null)
                        )
                    )
                ),
                marketplaceItemTypes = listOf(
                    ProfileDictionaryOptionDto(code = 0, label = null),
                    ProfileDictionaryOptionDto(code = 11, label = null)
                ),
                lostFoundItemTypes = listOf(
                    ProfileDictionaryOptionDto(code = 0, label = null)
                ),
                lostFoundModes = listOf(
                    ProfileDictionaryOptionDto(code = 1, label = null)
                )
            )
        )

        assertEquals(facultyName, options.facultyNameFor(11))
        assertEquals(
            listOf(ProfileFormSupport.UnselectedOption, majorName),
            options.majorOptionsFor(facultyName)
        )
        assertEquals("software_engineering", options.majorCodeFor(facultyName, majorName))
        assertEquals(fallbackOptions.marketplaceTypeTitle(0), options.marketplaceTypeTitle(0))
        assertEquals(fallbackOptions.marketplaceTypeTitle(11), options.marketplaceTypeTitle(11))
        assertEquals(fallbackOptions.lostFoundModeTitle(1), options.lostFoundModeTitle(1))
    }

    @Test
    fun gsonParsesCodeOnlyProfileOptionsPayload() {
        val dto = Gson().fromJson(
            """
            {
              "faculties": [
                { "code": 11, "majors": ["unselected", "software_engineering"] }
              ],
              "marketplaceItemTypes": [0, 11],
              "lostFoundItemTypes": [0],
              "lostFoundModes": [1]
            }
            """.trimIndent(),
            ProfileOptionsDto::class.java
        )

        assertEquals(11, dto.faculties?.firstOrNull()?.code)
        assertEquals("software_engineering", dto.faculties?.firstOrNull()?.majors?.getOrNull(1)?.code)
        assertEquals(11, dto.marketplaceItemTypes?.getOrNull(1)?.code)
        assertEquals(1, dto.lostFoundModes?.firstOrNull()?.code)
    }
}
