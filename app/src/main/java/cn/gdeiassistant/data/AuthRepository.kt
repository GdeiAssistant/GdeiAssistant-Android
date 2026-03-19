package cn.gdeiassistant.data

import android.content.Context
import cn.gdeiassistant.R
import cn.gdeiassistant.model.UserLoginResult
import cn.gdeiassistant.network.api.AuthApi
import cn.gdeiassistant.network.safeApiCall
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 登录认证仓库：调用 AuthApi，持久化 Token。
 */
@Singleton
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authApi: AuthApi,
    private val sessionManager: SessionManager
) {

    suspend fun login(username: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        safeApiCall {
            authApi.login(mapOf("username" to username, "password" to password))
        }.mapCatching { response ->
            val data: UserLoginResult = response
                ?: throw IllegalStateException(context.getString(R.string.service_unavailable))
            val token = data.token
                ?.takeIf { it.isNotBlank() }
                ?: throw IllegalStateException(context.getString(R.string.service_unavailable))
            sessionManager.saveToken(token, username.trim())
            Unit
        }
    }
}
