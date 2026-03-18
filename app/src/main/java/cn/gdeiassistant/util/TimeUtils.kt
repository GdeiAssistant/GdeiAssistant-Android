package cn.gdeiassistant.util

import java.util.Date

object TimeUtils {
    enum class TimeUnit(val unit: Int) { SECOND(0), MINUTES(1), HOUR(2), DAY(3) }
    @JvmStatic
    fun GetTimestampDifference(beginDate: Date?, endDate: Date?, timeUnit: TimeUnit): Long {
        if (beginDate == null || endDate == null) return 0L
        val millis = endDate.time - beginDate.time
        return when (timeUnit.unit) {
            0 -> millis / 1000
            1 -> millis / (1000 * 60)
            2 -> millis / (1000 * 60 * 60)
            3 -> millis / (1000 * 60 * 60 * 24)
            else -> 0L
        }
    }
}
