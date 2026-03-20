package cn.gdeiassistant.model

import androidx.compose.runtime.Immutable
import java.io.Serializable
import java.util.Calendar

@Immutable
data class ProfileLocationCity(
    val code: String,
    val name: String
) : Serializable

@Immutable
data class ProfileLocationState(
    val code: String,
    val name: String,
    val cities: List<ProfileLocationCity>
) : Serializable

@Immutable
data class ProfileLocationRegion(
    val code: String,
    val name: String,
    val states: List<ProfileLocationState>
) : Serializable

@Immutable
data class ProfileLocationSelection(
    val displayName: String,
    val regionCode: String,
    val stateCode: String,
    val cityCode: String
) : Serializable

@Immutable
data class ProfileUpdateRequest(
    val nickname: String,
    val college: String,
    val major: String,
    val grade: String,
    val bio: String,
    val birthday: String,
    val location: ProfileLocationSelection?,
    val hometown: ProfileLocationSelection?
) : Serializable

@Immutable
data class ProfileDictionaryOption(
    val code: Int,
    val label: String
) : Serializable

@Immutable
data class ProfileFacultyOption(
    val code: Int,
    val label: String,
    val majors: List<String>
) : Serializable

@Immutable
data class ProfileOptions(
    val faculties: List<ProfileFacultyOption>,
    val marketplaceItemTypes: List<ProfileDictionaryOption>,
    val lostFoundItemTypes: List<ProfileDictionaryOption>,
    val lostFoundModes: List<ProfileDictionaryOption>
) : Serializable {

    val facultyOptions: List<String>
        get() = faculties.map(ProfileFacultyOption::label)

    fun majorOptionsFor(faculty: String): List<String> {
        val normalizedFaculty = normalizeOptionLookup(faculty)
        return faculties.firstOrNull { normalizeOptionLookup(it.label) == normalizedFaculty }
            ?.majors
            ?.takeIf(List<String>::isNotEmpty)
            ?: listOf(ProfileFormSupport.UnselectedOption)
    }

    fun canSelectMajor(faculty: String): Boolean {
        val normalized = ProfileFormSupport.normalizeSelection(faculty)
        return normalized.isNotEmpty() && normalized != ProfileFormSupport.UnselectedOption
    }

    fun facultyCodeFor(college: String): Int? {
        val normalizedCollege = normalizeOptionLookup(college)
        return faculties.firstOrNull { normalizeOptionLookup(it.label) == normalizedCollege }?.code
    }

    fun facultyNameFor(code: Int?): String? {
        val label = code?.let { value ->
            faculties.firstOrNull { it.code == value }?.label
        }
        val normalized = label?.trim().orEmpty()
        return normalized.takeIf { it.isNotEmpty() && it != ProfileFormSupport.UnselectedOption }
    }

    fun marketplaceTypeOptions(): List<MarketplaceTypeOption> {
        return marketplaceItemTypes.map { option ->
            MarketplaceTypeOption(id = option.code, title = option.label)
        }
    }

    fun lostFoundItemTypeOptions(): List<LostFoundItemTypeOption> {
        return lostFoundItemTypes.map { option ->
            LostFoundItemTypeOption(id = option.code, title = option.label)
        }
    }

    fun marketplaceTypeTitle(value: Int?): String {
        return dictionaryLabelFor(
            options = marketplaceItemTypes,
            code = value,
            fallback = "其他"
        )
    }

    fun lostFoundItemTypeTitle(value: Int?): String {
        return dictionaryLabelFor(
            options = lostFoundItemTypes,
            code = value,
            fallback = "其他"
        )
    }

    fun lostFoundModeTitle(value: Int?): String {
        return dictionaryLabelFor(
            options = lostFoundModes,
            code = value,
            fallback = ProfileFormSupport.UnselectedOption
        )
    }
}

object ProfileFormSupport {
    const val UnselectedOption = "未选择"

    private val defaultFaculties = listOf(
        ProfileFacultyOption(0, UnselectedOption, listOf(UnselectedOption)),
        ProfileFacultyOption(1, "教育学院", listOf(UnselectedOption, "教育学", "学前教育", "小学教育", "特殊教育")),
        ProfileFacultyOption(2, "政法系", listOf(UnselectedOption, "法学", "思想政治教育", "社会工作")),
        ProfileFacultyOption(3, "中文系", listOf(UnselectedOption, "汉语言文学", "历史学", "秘书学")),
        ProfileFacultyOption(4, "数学系", listOf(UnselectedOption, "数学与应用数学", "信息与计算科学", "统计学")),
        ProfileFacultyOption(5, "外语系", listOf(UnselectedOption, "英语", "商务英语", "日语", "翻译")),
        ProfileFacultyOption(6, "物理与信息工程系", listOf(UnselectedOption, "物理学", "电子信息工程", "通信工程")),
        ProfileFacultyOption(7, "化学系", listOf(UnselectedOption, "化学", "应用化学", "材料化学")),
        ProfileFacultyOption(8, "生物与食品工程学院", listOf(UnselectedOption, "生物科学", "生物技术", "食品科学与工程")),
        ProfileFacultyOption(9, "体育学院", listOf(UnselectedOption, "体育教育", "社会体育指导与管理")),
        ProfileFacultyOption(10, "美术学院", listOf(UnselectedOption, "美术学", "视觉传达设计", "环境设计")),
        ProfileFacultyOption(11, "计算机科学系", listOf(UnselectedOption, "软件工程", "网络工程", "计算机科学与技术", "物联网工程")),
        ProfileFacultyOption(12, "音乐系", listOf(UnselectedOption, "音乐学", "音乐表演", "舞蹈学")),
        ProfileFacultyOption(13, "教师研修学院", listOf(UnselectedOption, "教育学", "教育技术学")),
        ProfileFacultyOption(14, "成人教育学院", listOf(UnselectedOption, "汉语言文学", "学前教育", "行政管理")),
        ProfileFacultyOption(15, "网络教育学院", listOf(UnselectedOption, "计算机科学与技术", "工商管理", "会计学")),
        ProfileFacultyOption(16, "马克思主义学院", listOf(UnselectedOption, "思想政治教育", "马克思主义理论"))
    )

    private val defaultMarketplaceItemTypes = listOf(
        ProfileDictionaryOption(0, "校园代步"),
        ProfileDictionaryOption(1, "手机"),
        ProfileDictionaryOption(2, "电脑"),
        ProfileDictionaryOption(3, "数码配件"),
        ProfileDictionaryOption(4, "数码"),
        ProfileDictionaryOption(5, "电器"),
        ProfileDictionaryOption(6, "运动健身"),
        ProfileDictionaryOption(7, "衣物伞帽"),
        ProfileDictionaryOption(8, "图书教材"),
        ProfileDictionaryOption(9, "租赁"),
        ProfileDictionaryOption(10, "生活娱乐"),
        ProfileDictionaryOption(11, "其他")
    )

    private val defaultLostFoundItemTypes = listOf(
        ProfileDictionaryOption(0, "手机"),
        ProfileDictionaryOption(1, "校园卡"),
        ProfileDictionaryOption(2, "身份证"),
        ProfileDictionaryOption(3, "银行卡"),
        ProfileDictionaryOption(4, "书"),
        ProfileDictionaryOption(5, "钥匙"),
        ProfileDictionaryOption(6, "包包"),
        ProfileDictionaryOption(7, "衣帽"),
        ProfileDictionaryOption(8, "校园代步"),
        ProfileDictionaryOption(9, "运动健身"),
        ProfileDictionaryOption(10, "数码配件"),
        ProfileDictionaryOption(11, "其他")
    )

    private val defaultLostFoundModes = listOf(
        ProfileDictionaryOption(0, "寻物启事"),
        ProfileDictionaryOption(1, "失物招领")
    )

    val defaultOptions: ProfileOptions = ProfileOptions(
        faculties = defaultFaculties,
        marketplaceItemTypes = defaultMarketplaceItemTypes,
        lostFoundItemTypes = defaultLostFoundItemTypes,
        lostFoundModes = defaultLostFoundModes
    )

    val enrollmentOptions: List<String>
        get() {
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            return (2014..currentYear).map(Int::toString)
        }

    fun makeLocationDisplay(region: String, state: String, city: String): String {
        return listOf(region, state, city)
            .map(String::trim)
            .filter(String::isNotEmpty)
            .fold(mutableListOf<String>()) { result, item ->
                if (result.lastOrNull() != item) {
                    result.add(item)
                }
                result
            }
            .joinToString(" ")
    }

    fun normalizeSelection(value: String): String {
        val trimmed = value.trim()
        return if (trimmed.isEmpty()) UnselectedOption else trimmed
    }

    fun normalizeOptionalSelection(value: String): String? {
        val trimmed = value.trim()
        return trimmed.takeIf { it.isNotEmpty() && it != UnselectedOption }
    }
}

private fun normalizeOptionLookup(value: String): String {
    return value
        .trim()
        .replace(" ", "")
        .replace("\u3000", "")
}

private fun dictionaryLabelFor(
    options: List<ProfileDictionaryOption>,
    code: Int?,
    fallback: String
): String {
    return options.firstOrNull { it.code == code }?.label
        ?: options.lastOrNull()?.label.orEmpty().ifBlank { fallback }
}
