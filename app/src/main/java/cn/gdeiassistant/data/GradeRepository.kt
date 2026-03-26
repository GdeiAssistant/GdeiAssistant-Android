package cn.gdeiassistant.data

import android.content.Context
import cn.gdeiassistant.R
import cn.gdeiassistant.model.GradeQueryResult
import cn.gdeiassistant.network.api.GradeApi
import cn.gdeiassistant.network.safeApiCall
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GradeRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gradeApi: GradeApi,
    private val sessionManager: SessionManager
) {

    suspend fun loadGrades(year: Int? = null): Result<GradeQueryResult?> = withContext(Dispatchers.IO) {
        val token = sessionManager.currentToken()
        if (token.isNullOrBlank()) {
            return@withContext Result.failure(IllegalStateException(context.getString(R.string.common_login_required)))
        }
        safeApiCall { gradeApi.queryGrade(token = token, year = year) }
    }
}
