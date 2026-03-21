package cn.gdeiassistant.network.api

import cn.gdeiassistant.model.DataJsonResult
import retrofit2.http.GET
import retrofit2.http.Path

interface AnnouncementApi {

    @GET("api/information/announcement/start/{start}/size/{size}")
    suspend fun queryAnnouncementPage(
        @Path("start") start: Int,
        @Path("size") size: Int
    ): DataJsonResult<List<AnnouncementDto>>

    @GET("api/information/announcement/id/{id}")
    suspend fun queryAnnouncementDetail(
        @Path("id") id: String
    ): DataJsonResult<AnnouncementDto>

    @GET("api/information/festival")
    suspend fun getFestival(): DataJsonResult<FestivalDto>
}

data class AnnouncementDto(
    val id: String? = null,
    val title: String? = null,
    val content: String? = null,
    val publishTime: String? = null
)

data class FestivalDto(
    val name: String? = null,
    val description: List<String>? = null
)
