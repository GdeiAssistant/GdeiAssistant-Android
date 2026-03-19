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

    /** 获取验证码图片。后端当前直接返回 Base64 字符串。 */
    suspend fun getCheckCode(): Result<String> = withContext(Dispatchers.IO) {
        try {
            safeApiCall { cetApi.getCheckCode() }.fold(
                onSuccess = { data ->
                    if (!data.isNullOrBlank()) {
                        Result.success(data)
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
