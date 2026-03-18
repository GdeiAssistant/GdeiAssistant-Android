package cn.gdeiassistant.network.api

import cn.gdeiassistant.model.JsonResult
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * 评教提交。对接 /api/evaluate/submit。
 */
interface EvaluateApi {

    @POST("api/evaluate/submit")
    suspend fun submit(
        @Header("token") token: String,
        @Body body: EvaluateSubmitBody?
    ): JsonResult
}

data class EvaluateSubmitBody(
    val directSubmit: Boolean? = null
)
