package cn.gdeiassistant.network.api

import cn.gdeiassistant.model.DataJsonResult
import cn.gdeiassistant.model.JsonResult
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface LostFoundApi {

    @GET("api/lostandfound/lostitem/start/{start}")
    suspend fun getLostItems(
        @Path("start") start: Int
    ): DataJsonResult<List<LostFoundItemDto>>

    @GET("api/lostandfound/founditem/start/{start}")
    suspend fun getFoundItems(
        @Path("start") start: Int
    ): DataJsonResult<List<LostFoundItemDto>>

    @GET("api/lostandfound/item/id/{id}")
    suspend fun getItemDetail(
        @Path("id") id: String
    ): DataJsonResult<LostFoundDetailDto>

    @GET("api/lostandfound/item/id/{id}/preview")
    suspend fun getItemPreview(
        @Path("id") id: String
    ): DataJsonResult<String>

    @GET("api/lostandfound/profile")
    suspend fun getProfileSummary(): DataJsonResult<LostFoundPersonalSummaryDto>

    @POST("api/lostandfound/item/id/{id}/didfound")
    suspend fun markDidFound(
        @Path("id") id: String
    ): JsonResult
}

data class LostFoundItemDto(
    val id: Int? = null,
    val username: String? = null,
    val name: String? = null,
    val description: String? = null,
    val location: String? = null,
    val itemType: Int? = null,
    val lostType: Int? = null,
    val qq: String? = null,
    val wechat: String? = null,
    val phone: String? = null,
    val state: Int? = null,
    val publishTime: String? = null,
    val pictureURL: List<String>? = null
)

data class LostFoundDetailDto(
    val item: LostFoundItemDto? = null,
    val profile: LostFoundProfileDto? = null
)

data class LostFoundProfileDto(
    val avatarURL: String? = null,
    val username: String? = null,
    val nickname: String? = null
)

data class LostFoundPersonalSummaryDto(
    val lost: List<LostFoundItemDto>? = null,
    val found: List<LostFoundItemDto>? = null,
    val didfound: List<LostFoundItemDto>? = null
)
