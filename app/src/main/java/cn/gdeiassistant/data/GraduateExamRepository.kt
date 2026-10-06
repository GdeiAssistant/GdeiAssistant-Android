package cn.gdeiassistant.data

import android.content.Context
import cn.gdeiassistant.R
import dagger.hilt.android.qualifiers.ApplicationContext

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
    @ApplicationContext private val context: Context,
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
            val score = dto ?: throw IllegalStateException(context.getString(R.string.graduate_exam_no_result))
            GraduateExamScore(
                name = score.name?.trim().orEmpty().ifBlank { context.getString(R.string.graduate_exam_unknown_candidate) },
                signupNumber = score.signUpNumber?.trim().orEmpty().ifBlank { context.getString(R.string.graduate_exam_unavailable) },
                examNumber = score.examNumber?.trim().orEmpty().ifBlank { context.getString(R.string.graduate_exam_unavailable) },
                totalScore = score.totalScore?.trim().orEmpty().ifBlank { "0" },
                politicsScore = score.firstScore?.trim().orEmpty().ifBlank { "0" },
                foreignLanguageScore = score.secondScore?.trim().orEmpty().ifBlank { "0" },
                businessOneScore = score.thirdScore?.trim().orEmpty().ifBlank { "0" },
                businessTwoScore = score.fourthScore?.trim().orEmpty().ifBlank { "0" }
            )
        }
    }
}
