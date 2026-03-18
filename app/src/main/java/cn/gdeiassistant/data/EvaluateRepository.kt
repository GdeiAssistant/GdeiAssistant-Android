package cn.gdeiassistant.data

import cn.gdeiassistant.network.api.EvaluateApi
import cn.gdeiassistant.network.api.EvaluateSubmitBody
import cn.gdeiassistant.network.safeJsonResultCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EvaluateRepository @Inject constructor(
    private val evaluateApi: EvaluateApi,
    private val sessionManager: SessionManager
) {

    suspend fun submit(directSubmit: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val token = sessionManager.currentToken()
        if (token.isNullOrBlank()) {
            return@withContext Result.failure(IllegalStateException("请先登录"))
        }
        safeJsonResultCall {
            evaluateApi.submit(
                token = token,
                body = EvaluateSubmitBody(directSubmit = directSubmit)
            )
        }
    }
}
