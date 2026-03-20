package cn.gdeiassistant.network.api

import cn.gdeiassistant.model.DataJsonResult
import cn.gdeiassistant.model.JsonResult
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MessageApi {

    @GET("api/information/message/interaction/start/{start}/size/{size}")
    suspend fun getInteractionMessages(
        @Path("start") start: Int,
        @Path("size") size: Int
    ): DataJsonResult<List<InteractionMessageDto>>

    @GET("api/information/message/unread")
    suspend fun getUnreadCount(): DataJsonResult<Int>

    @POST("api/information/message/id/{id}/read")
    suspend fun markMessageRead(
        @Path("id") id: String
    ): JsonResult

    @POST("api/information/message/readall")
    suspend fun markAllRead(): JsonResult
}

data class InteractionMessageDto(
    val id: String? = null,
    val module: String? = null,
    val type: String? = null,
    val title: String? = null,
    val content: String? = null,
    val createdAt: String? = null,
    val isRead: Boolean? = null,
    val targetType: String? = null,
    val targetId: String? = null,
    val targetSubId: String? = null
)
