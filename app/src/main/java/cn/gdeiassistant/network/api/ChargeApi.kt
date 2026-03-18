package cn.gdeiassistant.network.api

import cn.gdeiassistant.model.DataJsonResult
import cn.gdeiassistant.model.EncryptedData
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * 校园卡充值接口。
 */
interface ChargeApi {

    @GET("api/encryption/rsa/publickey")
    suspend fun getServerPublicKey(): DataJsonResult<String>

    @FormUrlEncoded
    @POST("api/card/charge")
    suspend fun submitCharge(
        @Header("Version-Code") versionCode: String,
        @Header("Client-Type") clientType: String,
        @Header("User-Agent") userAgent: String,
        @Field("amount") amount: String,
        @Field("token") token: String,
        @Field("nonce") nonce: String,
        @Field("timestamp") timestamp: String,
        @Field("signature") signature: String,
        @Field("clientRSAPublicKey") clientRSAPublicKey: String,
        @Field("clientAESKey") clientAESKey: String,
        @Field("clientRSASignature") clientRSASignature: String
    ): DataJsonResult<EncryptedData>
}
