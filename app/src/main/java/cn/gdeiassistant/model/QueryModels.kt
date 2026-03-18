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
    val maxWeek: Int = 0,
    val weekType: Int = 0,
    val classNumber: Int = 1
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

val spareZoneTitles = listOf("白云校区", "龙洞校区", "三水校区", "东莞校区", "外部教学点")
val spareTypeTitles = listOf("普通课室", "多媒体课室", "机房", "实验室", "阶梯教室", "智慧课室")
val spareWeekTypeTitles = listOf("全部", "单周", "双周")
val spareWeekdayTitles = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
