package cn.gdeiassistant.data

import cn.gdeiassistant.model.Cet
import cn.gdeiassistant.network.api.CetApi
import cn.gdeiassistant.network.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CetRepository @Inject constructor(
    private val cetApi: CetApi,
    private val sessionManager: SessionManager
) {

    private fun requireToken(): String {
        val token = sessionManager.currentToken()
        if (token.isNullOrBlank()) throw IllegalStateException("请先登录")
        return token
    }

    /** 获取验证码图片与会话 token（用于后续 query）。 */
    suspend fun getCheckCode(): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        try {
            safeApiCall { cetApi.getCheckCode() }.fold(
                onSuccess = { data ->
                    val token = data?.token
                    val imageBase64 = data?.imageBase64
                    if (!token.isNullOrBlank() && !imageBase64.isNullOrBlank()) {
                        Result.success(Pair(token, imageBase64))
                    } else {
                        Result.failure(Exception("验证码加载失败"))
                    }
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** 查询四六级成绩。使用用户 JWT。 */
    suspend fun query(
        ticketNumber: String,
        name: String,
        checkcode: String
    ): Result<Cet> = withContext(Dispatchers.IO) {
        try {
            val token = requireToken()
            safeApiCall {
                cetApi.query(
                    token = token,
                    ticketNumber = ticketNumber,
                    name = name,
                    checkcode = checkcode
                )
            }.fold(
                onSuccess = { data ->
                    if (data != null) Result.success(data) else Result.failure(Exception("暂无成绩数据"))
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
