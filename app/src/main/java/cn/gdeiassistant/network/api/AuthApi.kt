package cn.gdeiassistant.network.api

import cn.gdeiassistant.model.DataJsonResult
import cn.gdeiassistant.model.UserLoginResult
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * 认证相关接口，对接 /api/auth/login。
 */
interface AuthApi {

    @POST("api/auth/login")
    suspend fun login(
        @Body body: Map<String, String>
    ): DataJsonResult<UserLoginResult>
}
