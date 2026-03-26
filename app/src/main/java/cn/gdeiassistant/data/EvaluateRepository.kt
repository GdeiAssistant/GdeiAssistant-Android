package cn.gdeiassistant.data

import android.content.Context
import cn.gdeiassistant.R
import cn.gdeiassistant.network.api.EvaluateApi
import cn.gdeiassistant.network.api.EvaluateSubmitBody
import cn.gdeiassistant.network.safeJsonResultCall
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EvaluateRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val evaluateApi: EvaluateApi,
    private val sessionManager: SessionManager
) {

    suspend fun submit(directSubmit: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val token = sessionManager.currentToken()
        if (token.isNullOrBlank()) {
            return@withContext Result.failure(IllegalStateException(context.getString(R.string.common_login_required)))
        }
        safeJsonResultCall {
            evaluateApi.submit(
                token = token,
                body = EvaluateSubmitBody(directSubmit = directSubmit)
            )
        }
    }
}
