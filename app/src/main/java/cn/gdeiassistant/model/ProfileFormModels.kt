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
data class ProfileMajorOption(
    val code: String,
    val label: String
) : Serializable

@Immutable
data class ProfileFacultyOption(
    val code: Int,
    val label: String,
    val majors: List<ProfileMajorOption>
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
            ?.map(ProfileMajorOption::label)
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

    fun majorCodeFor(faculty: String, majorLabel: String): String? {
        val normalizedFaculty = normalizeOptionLookup(faculty)
        val normalizedMajor = normalizeOptionLookup(majorLabel)
        return faculties.firstOrNull { normalizeOptionLookup(it.label) == normalizedFaculty }
            ?.majors
            ?.firstOrNull { normalizeOptionLookup(it.label) == normalizedMajor }
            ?.code
    }

    fun majorLabelFor(faculty: String, majorCode: String): String? {
        val normalizedFaculty = normalizeOptionLookup(faculty)
        return faculties.firstOrNull { normalizeOptionLookup(it.label) == normalizedFaculty }
            ?.majors
            ?.firstOrNull { it.code == majorCode }
            ?.label
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
        ProfileFacultyOption(0, UnselectedOption, makeMajorOptions(listOf(UnselectedOption))),
        ProfileFacultyOption(1, "教育学院", makeMajorOptions(listOf(UnselectedOption, "教育学", "学前教育", "小学教育", "特殊教育"))),
        ProfileFacultyOption(2, "政法系", makeMajorOptions(listOf(UnselectedOption, "法学", "思想政治教育", "社会工作"))),
        ProfileFacultyOption(3, "中文系", makeMajorOptions(listOf(UnselectedOption, "汉语言文学", "历史学", "秘书学"))),
        ProfileFacultyOption(4, "数学系", makeMajorOptions(listOf(UnselectedOption, "数学与应用数学", "信息与计算科学", "统计学"))),
        ProfileFacultyOption(5, "外语系", makeMajorOptions(listOf(UnselectedOption, "英语", "商务英语", "日语", "翻译"))),
        ProfileFacultyOption(6, "物理与信息工程系", makeMajorOptions(listOf(UnselectedOption, "物理学", "电子信息工程", "通信工程"))),
        ProfileFacultyOption(7, "化学系", makeMajorOptions(listOf(UnselectedOption, "化学", "应用化学", "材料化学"))),
        ProfileFacultyOption(8, "生物与食品工程学院", makeMajorOptions(listOf(UnselectedOption, "生物科学", "生物技术", "食品科学与工程"))),
        ProfileFacultyOption(9, "体育学院", makeMajorOptions(listOf(UnselectedOption, "体育教育", "社会体育指导与管理"))),
        ProfileFacultyOption(10, "美术学院", makeMajorOptions(listOf(UnselectedOption, "美术学", "视觉传达设计", "环境设计"))),
        ProfileFacultyOption(11, "计算机科学系", makeMajorOptions(listOf(UnselectedOption, "软件工程", "网络工程", "计算机科学与技术", "物联网工程"))),
        ProfileFacultyOption(12, "音乐系", makeMajorOptions(listOf(UnselectedOption, "音乐学", "音乐表演", "舞蹈学"))),
        ProfileFacultyOption(13, "教师研修学院", makeMajorOptions(listOf(UnselectedOption, "教育学", "教育技术学"))),
        ProfileFacultyOption(14, "成人教育学院", makeMajorOptions(listOf(UnselectedOption, "汉语言文学", "学前教育", "行政管理"))),
        ProfileFacultyOption(15, "网络教育学院", makeMajorOptions(listOf(UnselectedOption, "计算机科学与技术", "工商管理", "会计学"))),
        ProfileFacultyOption(16, "马克思主义学院", makeMajorOptions(listOf(UnselectedOption, "思想政治教育", "马克思主义理论")))
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

private fun makeMajorOptions(labels: List<String>): List<ProfileMajorOption> {
    return labels.map { label ->
        ProfileMajorOption(
            code = majorCodeMap[label] ?: normalizeOptionLookup(label),
            label = label
        )
    }
}

private val majorCodeMap = mapOf(
    "未选择" to "unselected",
    "教育学" to "education",
    "学前教育" to "preschool_education",
    "小学教育" to "primary_education",
    "特殊教育" to "special_education",
    "法学" to "law",
    "思想政治教育" to "ideological_political_education",
    "社会工作" to "social_work",
    "汉语言文学" to "chinese_language_literature",
    "历史学" to "history",
    "秘书学" to "secretarial_studies",
    "数学与应用数学" to "mathematics_applied_mathematics",
    "信息与计算科学" to "information_computing_science",
    "统计学" to "statistics",
    "英语" to "english",
    "商务英语" to "business_english",
    "日语" to "japanese",
    "翻译" to "translation",
    "物理学" to "physics",
    "电子信息工程" to "electronic_information_engineering",
    "通信工程" to "communication_engineering",
    "化学" to "chemistry",
    "应用化学" to "applied_chemistry",
    "材料化学" to "materials_chemistry",
    "生物科学" to "biological_science",
    "生物技术" to "biotechnology",
    "食品科学与工程" to "food_science_engineering",
    "体育教育" to "physical_education",
    "社会体育指导与管理" to "social_sports_guidance_management",
    "美术学" to "fine_arts",
    "视觉传达设计" to "visual_communication_design",
    "环境设计" to "environmental_design",
    "软件工程" to "software_engineering",
    "网络工程" to "network_engineering",
    "计算机科学与技术" to "computer_science_technology",
    "物联网工程" to "internet_of_things_engineering",
    "音乐学" to "musicology",
    "音乐表演" to "music_performance",
    "舞蹈学" to "dance",
    "教育技术学" to "educational_technology",
    "行政管理" to "public_administration",
    "工商管理" to "business_administration",
    "会计学" to "accounting",
    "马克思主义理论" to "marxist_theory"
)

private fun dictionaryLabelFor(
    options: List<ProfileDictionaryOption>,
    code: Int?,
    fallback: String
): String {
    return options.firstOrNull { it.code == code }?.label
        ?: options.lastOrNull()?.label.orEmpty().ifBlank { fallback }
}
