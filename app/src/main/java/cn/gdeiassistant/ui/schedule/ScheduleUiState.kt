package cn.gdeiassistant.ui.schedule

import cn.gdeiassistant.model.Schedule

/**
 * 课表页 UI 状态：当前周、总周数、7xN 矩阵（按 row/column 放置课程）、加载与错误。
 * 矩阵：行 = 节次（1..N），列 = 周一..周日（0..6）。
 */
data class ScheduleUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    /** 自定义课表背景图 URI（来自系统文档选择器） */
    val backgroundImageUri: String? = null,
    /** 当前选中的周数 1..maxWeeks */
    val selectedWeek: Int = 1,
    /** 最大周数（如 20） */
    val maxWeeks: Int = 20,
    /** 当前周的课表列表（后端返回的 scheduleList） */
    val scheduleList: List<Schedule> = emptyList(),
    /** 节次数（行数），如 10 表示 1～10 节 */
    val totalSections: Int = 10
) {
    val hasCustomBackground: Boolean
        get() = !backgroundImageUri.isNullOrBlank()

    val totalCourses: Int
        get() = scheduleList.size

    val busiestDayIndex: Int?
        get() = (0..6)
            .maxByOrNull { day -> coursesForDay(day).size }
            ?.takeIf { day -> coursesForDay(day).isNotEmpty() }

    fun coursesForDay(day: Int): List<Schedule> =
        scheduleList
            .filter { it.column == day }
            .sortedWith(
                compareBy<Schedule> { it.row ?: Int.MAX_VALUE }
                    .thenBy { it.position ?: Int.MAX_VALUE }
            )

    /** 该格子 (row, col) 是否被某课程占据（含跨节）；返回占据该格的课程（取 row 最小的即“起始行”的那条） */
    fun courseAt(row: Int, col: Int): Schedule? =
        scheduleList.firstOrNull { s ->
            val r = s.row ?: return@firstOrNull false
            val c = s.column ?: return@firstOrNull false
            val len = (s.scheduleLength ?: 1).coerceAtLeast(1)
            c == col && row in r until (r + len)
        }

    /** 在 (row, col) 起始的课程（用于宫格只在该格绘制卡片） */
    fun courseStartingAt(row: Int, col: Int): Schedule? =
        scheduleList.firstOrNull { it.row == row && it.column == col }
}

/**
 * 根据开学日期计算当前是第几周（1-based）。
 * 若未配置开学日期，使用默认：当年 9 月第一个周一。
 */
fun currentWeekNumber(semesterStartMillis: Long = defaultSemesterStartMillis()): Int {
    val now = System.currentTimeMillis()
    if (now < semesterStartMillis) return 1
    val week = ((now - semesterStartMillis) / (7 * 24 * 60 * 60 * 1000L)).toInt() + 1
    return week.coerceIn(1, 25)
}

private fun defaultSemesterStartMillis(): Long {
    val cal = java.util.Calendar.getInstance()
    val year = cal.get(java.util.Calendar.YEAR)
    val september = java.util.Calendar.getInstance().apply {
        set(year, java.util.Calendar.SEPTEMBER, 1)
    }
    var day = september.get(java.util.Calendar.DAY_OF_WEEK)
    // Calendar.SUNDAY=1, MONDAY=2, ...
    val daysUntilMonday = if (day == java.util.Calendar.SUNDAY) 1 else (9 - day) % 7
    september.add(java.util.Calendar.DAY_OF_MONTH, daysUntilMonday)
    return september.timeInMillis
}
