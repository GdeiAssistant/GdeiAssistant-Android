package cn.gdeiassistant.network.api

import cn.gdeiassistant.model.DataJsonResult
import retrofit2.http.Body
import retrofit2.http.POST

interface GraduateExamApi {

    @POST("api/kaoyan/query")
    suspend fun queryScore(
        @Body body: GraduateExamQueryDto
    ): DataJsonResult<GraduateExamScoreDto>
}

data class GraduateExamQueryDto(
    val name: String,
    val examNumber: String,
    val idNumber: String
)

data class GraduateExamScoreDto(
    val name: String? = null,
    val signUpNumber: String? = null,
    val examNumber: String? = null,
    val totalScore: String? = null,
    val firstScore: String? = null,
    val secondScore: String? = null,
    val thirdScore: String? = null,
    val fourthScore: String? = null
)
