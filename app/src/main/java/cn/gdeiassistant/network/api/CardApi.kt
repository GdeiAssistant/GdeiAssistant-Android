package cn.gdeiassistant.network.api

import cn.gdeiassistant.model.CardInfo
import cn.gdeiassistant.model.DataJsonResult
import cn.gdeiassistant.model.CardQueryResult
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * 校园卡接口。对接 /api/card/info、/api/card/query、/api/card/lost。
 */
interface CardApi {

    @GET("api/card/info")
    suspend fun getCardInfo(@Header("token") token: String): DataJsonResult<CardInfo>

    @POST("api/card/query")
    suspend fun queryCard(
        @Header("token") token: String,
        @Body body: CardQueryBody
    ): DataJsonResult<CardQueryResult>

    @FormUrlEncoded
    @POST("api/card/lost")
    suspend fun reportLost(
        @Header("token") token: String,
        @Field("cardPassword") cardPassword: String
    ): DataJsonResult<Unit>
}

/** 与后端 CardQuery 对应，用于 POST /api/card/query 的 JSON Body */
data class CardQueryBody(
    val year: Int? = null,
    val month: Int? = null,
    val date: Int? = null
)
