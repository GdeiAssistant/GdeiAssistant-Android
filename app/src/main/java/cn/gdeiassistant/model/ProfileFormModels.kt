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

object ProfileFormSupport {
    const val UnselectedOption = "未选择"

    val facultyOptions: List<String> = listOf(
        UnselectedOption,
        "教育学院",
        "政法系",
        "中文系",
        "数学系",
        "外语系",
        "物理与信息工程系",
        "化学系",
        "生物与食品工程学院",
        "体育学院",
        "美术学院",
        "计算机科学系",
        "音乐系",
        "教师研修学院",
        "成人教育学院",
        "网络教育学院",
        "马克思主义学院"
    )

    private val majorOptionsByFaculty: Map<String, List<String>> = mapOf(
        UnselectedOption to listOf(UnselectedOption),
        "教育学院" to listOf(UnselectedOption, "教育学", "学前教育", "小学教育", "特殊教育"),
        "政法系" to listOf(UnselectedOption, "法学", "思想政治教育", "社会工作"),
        "中文系" to listOf(UnselectedOption, "汉语言文学", "历史学", "秘书学"),
        "数学系" to listOf(UnselectedOption, "数学与应用数学", "信息与计算科学", "统计学"),
        "外语系" to listOf(UnselectedOption, "英语", "商务英语", "日语", "翻译"),
        "物理与信息工程系" to listOf(UnselectedOption, "物理学", "电子信息工程", "通信工程"),
        "化学系" to listOf(UnselectedOption, "化学", "应用化学", "材料化学"),
        "生物与食品工程学院" to listOf(UnselectedOption, "生物科学", "生物技术", "食品科学与工程"),
        "体育学院" to listOf(UnselectedOption, "体育教育", "社会体育指导与管理"),
        "美术学院" to listOf(UnselectedOption, "美术学", "视觉传达设计", "环境设计"),
        "计算机科学系" to listOf(UnselectedOption, "软件工程", "网络工程", "计算机科学与技术", "物联网工程"),
        "音乐系" to listOf(UnselectedOption, "音乐学", "音乐表演", "舞蹈学"),
        "教师研修学院" to listOf(UnselectedOption, "教育学", "教育技术学"),
        "成人教育学院" to listOf(UnselectedOption, "汉语言文学", "学前教育", "行政管理"),
        "网络教育学院" to listOf(UnselectedOption, "计算机科学与技术", "工商管理", "会计学"),
        "马克思主义学院" to listOf(UnselectedOption, "思想政治教育", "马克思主义理论")
    )

    val enrollmentOptions: List<String>
        get() {
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            return (2014..currentYear).map(Int::toString)
        }

    fun majorOptionsFor(faculty: String): List<String> {
        return majorOptionsByFaculty[normalizeSelection(faculty)] ?: listOf(UnselectedOption)
    }

    fun canSelectMajor(faculty: String): Boolean {
        val normalized = normalizeSelection(faculty)
        return normalized.isNotEmpty() && normalized != UnselectedOption
    }

    fun facultyCodeFor(college: String): Int? {
        val normalizedCollege = normalize(college)
        val index = facultyOptions.indexOfFirst { normalize(it) == normalizedCollege }
        return index.takeIf { it >= 0 }
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

    private fun normalize(value: String): String {
        return value
            .trim()
            .replace(" ", "")
            .replace("\u3000", "")
    }
}
