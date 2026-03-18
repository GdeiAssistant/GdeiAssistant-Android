package cn.gdeiassistant.network.api

import cn.gdeiassistant.model.Cet
import cn.gdeiassistant.model.DataJsonResult
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

/**
 * 四六级查询接口。对接 /api/cet/query、/api/cet/checkcode。
 */
interface CetApi {

    @GET("api/cet/query")
    suspend fun query(
        @Header("token") token: String,
        @Query("ticketNumber") ticketNumber: String,
        @Query("name") name: String,
        @Query("checkcode") checkcode: String
    ): DataJsonResult<Cet>

    @GET("api/cet/checkcode")
    suspend fun getCheckCode(): DataJsonResult<CetCheckCodeResult>
}

data class CetCheckCodeResult(
    val token: String? = null,
    val imageBase64: String? = null
)
