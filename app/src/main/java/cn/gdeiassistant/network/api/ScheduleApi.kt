package cn.gdeiassistant.network.api

import cn.gdeiassistant.model.DataJsonResult
import cn.gdeiassistant.model.ScheduleQueryResult
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ScheduleApi {

    @GET("api/schedule")
    suspend fun querySchedule(
        @Header("token") token: String,
        @Query("week") week: Int? = null
    ): DataJsonResult<ScheduleQueryResult>
}
