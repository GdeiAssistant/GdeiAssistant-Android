package cn.gdeiassistant.data

import cn.gdeiassistant.model.GraduateExamQuery
import cn.gdeiassistant.model.GraduateExamScore
import cn.gdeiassistant.network.api.GraduateExamApi
import cn.gdeiassistant.network.api.GraduateExamQueryDto
import cn.gdeiassistant.network.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GraduateExamRepository @Inject constructor(
    private val graduateExamApi: GraduateExamApi
) {

    suspend fun queryScore(query: GraduateExamQuery): Result<GraduateExamScore> = withContext(Dispatchers.IO) {
        safeApiCall {
            graduateExamApi.queryScore(
                GraduateExamQueryDto(
                    name = query.name.trim(),
                    examNumber = query.examNumber.trim(),
                    idNumber = query.idNumber.trim()
                )
            )
        }.mapCatching { dto ->
            val score = dto ?: throw IllegalStateException("暂无考研查询结果")
            GraduateExamScore(
                name = score.name?.trim().orEmpty().ifBlank { "未命名考生" },
                signupNumber = score.signUpNumber?.trim().orEmpty().ifBlank { "暂无" },
                examNumber = score.examNumber?.trim().orEmpty().ifBlank { "暂无" },
                totalScore = score.totalScore?.trim().orEmpty().ifBlank { "0" },
                politicsScore = score.firstScore?.trim().orEmpty().ifBlank { "0" },
                foreignLanguageScore = score.secondScore?.trim().orEmpty().ifBlank { "0" },
                businessOneScore = score.thirdScore?.trim().orEmpty().ifBlank { "0" },
                businessTwoScore = score.fourthScore?.trim().orEmpty().ifBlank { "0" }
            )
        }
    }
}
