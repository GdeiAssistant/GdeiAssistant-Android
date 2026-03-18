package cn.gdeiassistant.network.api

import cn.gdeiassistant.model.DataJsonResult
import cn.gdeiassistant.model.GradeQueryResult
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface GradeApi {

    @GET("api/grade")
    suspend fun queryGrade(
        @Header("token") token: String,
        @Query("year") year: Int? = null
    ): DataJsonResult<GradeQueryResult>
}
