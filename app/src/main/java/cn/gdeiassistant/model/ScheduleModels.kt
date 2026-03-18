package cn.gdeiassistant.model

import androidx.compose.runtime.Immutable
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
@Immutable
data class Schedule(
    val id: String? = null,
    val scheduleLength: Int? = null,
    val scheduleName: String? = null,
    val scheduleType: String? = null,
    val scheduleLesson: String? = null,
    val scheduleTeacher: String? = null,
    val scheduleLocation: String? = null,
    val colorCode: String? = null,
    val position: Int? = null,
    val row: Int? = null,
    val column: Int? = null,
    val minScheduleWeek: Int? = null,
    val maxScheduleWeek: Int? = null,
    val scheduleWeek: String? = null
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
@Immutable
data class ScheduleQueryResult(
    val scheduleList: List<Schedule>? = null,
    val week: Int? = null
) : Serializable
