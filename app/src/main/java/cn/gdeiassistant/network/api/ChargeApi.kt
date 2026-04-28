package cn.gdeiassistant.network.api

import cn.gdeiassistant.model.Charge
import cn.gdeiassistant.model.ChargeOrder
import cn.gdeiassistant.model.DataJsonResult
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * 校园卡充值接口。
 */
interface ChargeApi {

    @FormUrlEncoded
    @POST("api/card/charge")
    suspend fun submitCharge(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Field("amount") amount: String,
        @Field("password") password: String
    ): DataJsonResult<Charge>

    @GET("api/card/charge/orders/{orderId}")
    suspend fun getChargeOrder(
        @Path("orderId") orderId: String
    ): DataJsonResult<ChargeOrder>

    @GET("api/card/charge/orders")
    suspend fun getRecentChargeOrders(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("status") status: String? = null
    ): DataJsonResult<List<ChargeOrder>>
}
