package cn.gdeiassistant.data

import cn.gdeiassistant.model.Schedule
import cn.gdeiassistant.model.ScheduleQueryResult
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

val CampusZone: ZoneId = ZoneId.of("Asia/Shanghai")

/** Current API week and campus day only; the contract contains no clock times. */
fun selectTodaySchedule(result: ScheduleQueryResult?, clock: Clock = Clock.system(CampusZone)): List<Schedule>? {
    val week = result?.week?.takeIf { it > 0 } ?: return null
    val all = result.scheduleList ?: return null
    if (all.any { it.column == null || it.column !in 0..6 }) return null
    val column = LocalDate.now(clock.withZone(CampusZone)).dayOfWeek.value - 1
    val today = all.filter { it.column == column }
    if (today.any {
        it.minScheduleWeek == null || it.maxScheduleWeek == null ||
            it.minScheduleWeek < 1 || it.maxScheduleWeek < it.minScheduleWeek
    }) return null
    return today.filter { week in it.minScheduleWeek!!..it.maxScheduleWeek!! }
}

/** Do not derive a period or wall-clock time from missing row/length metadata. */
fun actualLessonLabel(course: Schedule): String? = course.scheduleLesson?.trim()?.takeIf(String::isNotBlank)
