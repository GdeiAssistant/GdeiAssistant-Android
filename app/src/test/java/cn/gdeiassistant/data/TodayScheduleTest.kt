package cn.gdeiassistant.data

import cn.gdeiassistant.model.Schedule
import cn.gdeiassistant.model.ScheduleQueryResult
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TodayScheduleTest {
    private val tuesdayInChina = Clock.fixed(Instant.parse("2026-10-12T16:30:00Z"), ZoneOffset.UTC)
    private fun course(column: Int? = 1, min: Int? = 1, max: Int? = 16) = Schedule(
        id = "course", column = column, minScheduleWeek = min, maxScheduleWeek = max,
        scheduleName = "软件测试", scheduleLesson = "第 3-4 节", scheduleLocation = "教学楼 C402"
    )

    @Test fun campusDayUsesChinaEvenWhenClockOrDeviceIsAbroad() {
        assertEquals(listOf(course()), selectTodaySchedule(
            ScheduleQueryResult(listOf(course(), course(column = 0)), 5), tuesdayInChina
        ))
    }

    @Test fun campusSundayIsColumnSix() {
        val sunday = Clock.fixed(Instant.parse("2026-10-11T04:00:00Z"), ZoneOffset.UTC)
        assertEquals(listOf(course(6)), selectTodaySchedule(ScheduleQueryResult(listOf(course(6)), 5), sunday))
    }

    @Test fun filtersActualCurrentWeekInclusivelyWithoutInventingAnotherWeek() {
        val active = course(min = 5, max = 5)
        assertEquals(listOf(active), selectTodaySchedule(ScheduleQueryResult(
            listOf(course(min = 6), active, course(max = 4)), 5
        ), tuesdayInChina))
    }

    @Test fun unknownPayloadOrWeekIsNotAnEmptySuccessfulDay() {
        for (result in listOf(null, ScheduleQueryResult(), ScheduleQueryResult(emptyList(), null),
            ScheduleQueryResult(emptyList(), 0), ScheduleQueryResult(null, 5))) {
            assertNull(selectTodaySchedule(result, tuesdayInChina))
        }
    }

    @Test fun unknownDayOrTodayWeekBoundsRemainUnavailable() {
        for (item in listOf(course(null), course(7), course(min = null), course(max = null),
            course(min = 0), course(min = 9, max = 4))) {
            assertNull(selectTodaySchedule(ScheduleQueryResult(listOf(item), 5), tuesdayInChina))
        }
    }

    @Test fun knownEmptyScheduleAndExcludedWeeksCanEstablishNoClasses() {
        assertEquals(emptyList<Schedule>(), selectTodaySchedule(ScheduleQueryResult(emptyList(), 5), tuesdayInChina))
        assertEquals(emptyList<Schedule>(), selectTodaySchedule(ScheduleQueryResult(listOf(course(min = 6)), 5), tuesdayInChina))
    }

    @Test fun lessonComesFromActualLabelRatherThanRowDefaultsOrClockMap() {
        assertEquals("第 3-4 节", actualLessonLabel(course().copy(row = 0, scheduleLength = null)))
        assertNull(actualLessonLabel(Schedule(row = 0, scheduleLength = 1)))
        assertNull(actualLessonLabel(Schedule(scheduleLesson = " ")))
        assertEquals("7-9", actualLessonLabel(Schedule(scheduleLesson = "7-9", row = 6, scheduleLength = 3)))
    }
}
