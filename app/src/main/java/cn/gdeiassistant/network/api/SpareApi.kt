package cn.gdeiassistant.network.api

import cn.gdeiassistant.model.DataJsonResult
import retrofit2.http.Body
import retrofit2.http.POST

interface SpareApi {

    @POST("api/spare/query")
    suspend fun querySpareRooms(
        @Body body: SpareRoomQueryDto
    ): DataJsonResult<List<SpareRoomDto>>
}

data class SpareRoomQueryDto(
    val zone: Int,
    val type: Int,
    val minSeating: Int? = null,
    val maxSeating: Int? = null,
    val startTime: Int,
    val endTime: Int,
    val minWeek: Int,
    val maxWeek: Int,
    val weekType: Int,
    val classNumber: Int
)

data class SpareRoomDto(
    val number: String? = null,
    val name: String? = null,
    val type: String? = null,
    val zone: String? = null,
    val classSeating: String? = null,
    val section: String? = null,
    val examSeating: String? = null
)
