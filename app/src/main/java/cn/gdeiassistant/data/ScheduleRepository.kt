package cn.gdeiassistant.data

import android.content.Context
import cn.gdeiassistant.R
import cn.gdeiassistant.model.Schedule
import cn.gdeiassistant.model.ScheduleQueryResult
import cn.gdeiassistant.network.api.ScheduleApi
import cn.gdeiassistant.network.safeApiCall
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scheduleApi: ScheduleApi,
    private val sessionManager: SessionManager
) {

    suspend fun loadSchedule(week: Int? = null): Result<ScheduleQueryResult?> = withContext(Dispatchers.IO) {
        val token = sessionManager.currentToken()
        if (token.isNullOrBlank()) {
            return@withContext Result.failure(IllegalStateException(context.getString(R.string.common_login_required)))
        }
        safeApiCall { scheduleApi.querySchedule(token = token, week = week) }
    }

    suspend fun loadTodaySchedule(): Result<List<Schedule>> = withContext(Dispatchers.IO) {
        loadSchedule(null).fold(
            onSuccess = { result ->
                val all = result?.scheduleList.orEmpty()
                val cal = java.util.Calendar.getInstance()
                val dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK)
                val column = if (dayOfWeek == java.util.Calendar.SUNDAY) 6 else dayOfWeek - 2
                Result.success(all.filter { it.column == column })
            },
            onFailure = { e -> Result.failure(e) }
        )
    }
}
