package cn.gdeiassistant.data

import cn.gdeiassistant.model.CheckUpgradeResult
import cn.gdeiassistant.network.api.UpgradeApi
import cn.gdeiassistant.network.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpgradeRepository @Inject constructor(
    private val upgradeApi: UpgradeApi
) {

    suspend fun checkUpgrade(): Result<CheckUpgradeResult?> = withContext(Dispatchers.IO) {
        try {
            safeApiCall { upgradeApi.getUpgradeInfo() }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
