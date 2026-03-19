package cn.gdeiassistant.model

import androidx.compose.runtime.Immutable
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
import java.util.Calendar

@Immutable
data class SpareRoomQuery(
    val zone: Int = 0,
    val type: Int = 0,
    val minSeating: Int? = null,
    val maxSeating: Int? = null,
    val startTime: Int = 1,
    val endTime: Int = 2,
    val minWeek: Int = 0,
    val maxWeek: Int = 6,
    val weekType: Int = 0,
    val classNumber: Int = 0
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
@Immutable
data class SpareRoomItem(
    val id: String,
    val roomNumber: String,
    val roomName: String,
    val roomType: String,
    val zoneName: String,
    val classSeating: String,
    val sectionText: String,
    val examSeating: String
) : Serializable

@Immutable
data class GraduateExamQuery(
    val name: String = "",
    val examNumber: String = "",
    val idNumber: String = ""
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
@Immutable
data class GraduateExamScore(
    val name: String,
    val signupNumber: String,
    val examNumber: String,
    val totalScore: String,
    val politicsScore: String,
    val foreignLanguageScore: String,
    val businessOneScore: String,
    val businessTwoScore: String
) : Serializable

@Immutable
data class ElectricityQuery(
    val year: Int = Calendar.getInstance().get(Calendar.YEAR),
    val name: String = "",
    val studentNumber: String = ""
) : Serializable {
    companion object {
        fun yearOptions(currentYear: Int = Calendar.getInstance().get(Calendar.YEAR)): List<Int> {
            return (currentYear downTo 2016).toList()
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
@Immutable
data class ElectricityBill(
    val year: Int,
    val buildingNumber: String,
    val roomNumber: String,
    val peopleNumber: String,
    val department: String,
    val usedElectricAmount: String,
    val freeElectricAmount: String,
    val feeBasedElectricAmount: String,
    val electricPrice: String,
    val totalElectricBill: String,
    val averageElectricBill: String
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
@Immutable
data class YellowPageEntry(
    val id: String,
    val section: String,
    val campus: String,
    val majorPhone: String,
    val minorPhone: String,
    val address: String,
    val email: String,
    val website: String
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
@Immutable
data class YellowPageCategory(
    val id: String,
    val name: String,
    val items: List<YellowPageEntry>
) : Serializable

@Immutable
data class SpareOption(
    val value: Int,
    val label: String
) : Serializable

val spareZoneOptions = listOf(
    SpareOption(0, "不限"),
    SpareOption(1, "海珠"),
    SpareOption(2, "花都"),
    SpareOption(3, "广东轻工南海校区"),
    SpareOption(4, "业余函授校区")
)

val spareTypeOptions = listOf(
    SpareOption(0, "不限"),
    SpareOption(1, "不用课室的课程"),
    SpareOption(2, "操场"),
    SpareOption(3, "大多媒体"),
    SpareOption(4, "电脑专业机房"),
    SpareOption(5, "雕塑室"),
    SpareOption(6, "多媒体教室"),
    SpareOption(7, "翻译室"),
    SpareOption(8, "服装实验室"),
    SpareOption(9, "钢琴室"),
    SpareOption(10, "钢琴室"),
    SpareOption(11, "公共机房"),
    SpareOption(12, "国画临摹室"),
    SpareOption(13, "画室"),
    SpareOption(14, "化学实验室"),
    SpareOption(15, "机房"),
    SpareOption(16, "教具室"),
    SpareOption(17, "教育实验室"),
    SpareOption(18, "解剖实验室"),
    SpareOption(19, "金融数学实验室"),
    SpareOption(20, "美术课室"),
    SpareOption(21, "蒙氏教学法专用课室"),
    SpareOption(22, "模型制作实验室"),
    SpareOption(23, "平面制作实验室"),
    SpareOption(24, "琴房"),
    SpareOption(25, "摄影实验室"),
    SpareOption(26, "声乐课室"),
    SpareOption(27, "生物实验室"),
    SpareOption(28, "实训室"),
    SpareOption(29, "视唱练耳室"),
    SpareOption(30, "陶艺室"),
    SpareOption(31, "体操房"),
    SpareOption(32, "网络实验室"),
    SpareOption(33, "微格课室"),
    SpareOption(34, "无须课室"),
    SpareOption(35, "舞蹈室"),
    SpareOption(36, "舞蹈室"),
    SpareOption(37, "物理实验室"),
    SpareOption(38, "小多媒体"),
    SpareOption(39, "小多媒体(<70)"),
    SpareOption(40, "小普通课室(<70)"),
    SpareOption(41, "小组课室"),
    SpareOption(42, "形体房"),
    SpareOption(43, "音乐室"),
    SpareOption(44, "音乐专业课室"),
    SpareOption(45, "语音室"),
    SpareOption(46, "智能录像室"),
    SpareOption(47, "中多媒体(70-100)"),
    SpareOption(48, "专业课教室"),
    SpareOption(49, "专业理论课室"),
    SpareOption(50, "专业实验室"),
    SpareOption(51, "咨询室"),
    SpareOption(52, "综合绘画实验室")
)

val spareWeekTypeOptions = listOf(
    SpareOption(0, "不限"),
    SpareOption(1, "单"),
    SpareOption(2, "双")
)

val spareWeekdayOptions = listOf(
    SpareOption(-1, "不限"),
    SpareOption(0, "周一"),
    SpareOption(1, "周二"),
    SpareOption(2, "周三"),
    SpareOption(3, "周四"),
    SpareOption(4, "周五"),
    SpareOption(5, "周六"),
    SpareOption(6, "周日")
)

val spareClassNumberOptions = listOf(
    SpareOption(0, "第1,2节"),
    SpareOption(1, "第3节"),
    SpareOption(2, "第4,5节"),
    SpareOption(3, "第6,7节"),
    SpareOption(4, "第8,9节"),
    SpareOption(5, "第10,11,12节"),
    SpareOption(6, "上午"),
    SpareOption(7, "下午"),
    SpareOption(8, "晚上"),
    SpareOption(9, "白天"),
    SpareOption(10, "整天")
)

fun sparePeriodRange(classNumber: Int): Pair<Int, Int> {
    return when (classNumber) {
        0 -> 1 to 2
        1 -> 3 to 3
        2 -> 4 to 5
        3 -> 6 to 7
        4 -> 8 to 9
        5 -> 10 to 12
        6 -> 1 to 4
        7 -> 6 to 9
        8 -> 10 to 12
        9 -> 1 to 9
        10 -> 1 to 12
        else -> 1 to 2
    }
}

fun SpareRoomQuery.selectedWeekdayValue(): Int {
    return if (minWeek == 0 && maxWeek == 6) -1 else minWeek.coerceIn(0, 6)
}
