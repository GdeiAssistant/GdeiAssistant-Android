package cn.gdeiassistant.data

import cn.gdeiassistant.model.GradeQueryResult
import cn.gdeiassistant.network.api.GradeApi
import cn.gdeiassistant.network.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GradeRepository @Inject constructor(
    private val gradeApi: GradeApi,
    private val sessionManager: SessionManager
) {

    suspend fun loadGrades(year: Int? = null): Result<GradeQueryResult?> = withContext(Dispatchers.IO) {
        val token = sessionManager.currentToken()
        if (token.isNullOrBlank()) {
            return@withContext Result.failure(IllegalStateException("请先登录"))
        }
        safeApiCall { gradeApi.queryGrade(token = token, year = year) }
    }
}
