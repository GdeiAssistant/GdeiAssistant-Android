package cn.gdeiassistant.network.api

import cn.gdeiassistant.model.DataJsonResult
import cn.gdeiassistant.model.JsonResult
import cn.gdeiassistant.model.TokenRefreshResult
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Token 刷新与过期检测。对接 /api/token/refresh、/api/token/expire。
 */
interface TokenApi {

    @FormUrlEncoded
    @POST("api/token/refresh")
    suspend fun refresh(@Field("token") refreshToken: String): DataJsonResult<TokenRefreshResult>

    @FormUrlEncoded
    @POST("api/token/expire")
    suspend fun expire(@Field("token") accessToken: String): JsonResult
}
