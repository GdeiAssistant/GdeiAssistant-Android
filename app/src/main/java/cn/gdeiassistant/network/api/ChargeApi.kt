package cn.gdeiassistant.network.api

import cn.gdeiassistant.model.Charge
import cn.gdeiassistant.model.DataJsonResult
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * 校园卡充值接口。
 */
interface ChargeApi {

    @FormUrlEncoded
    @POST("api/card/charge")
    suspend fun submitCharge(
        @Field("amount") amount: String,
        @Field("password") password: String
    ): DataJsonResult<Charge>
}
