package cn.gdeiassistant.network.api

import cn.gdeiassistant.model.DataJsonResult
import cn.gdeiassistant.model.CheckUpgradeResult
import retrofit2.http.GET

/** 检查应用更新。 */
interface UpgradeApi {

    @GET("api/update/android")
    suspend fun getUpgradeInfo(): DataJsonResult<CheckUpgradeResult>
}
