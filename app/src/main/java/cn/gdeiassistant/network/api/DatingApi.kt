package cn.gdeiassistant.network.api

import cn.gdeiassistant.model.DataJsonResult
import cn.gdeiassistant.model.JsonResult
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface DatingApi {

    @GET("api/dating/pick/my/received")
    suspend fun getReceivedPicks(): DataJsonResult<List<DatingPickDto>>

    @GET("api/dating/pick/my/sent")
    suspend fun getSentPicks(): DataJsonResult<List<DatingPickDto>>

    @GET("api/dating/profile/my")
    suspend fun getMyPosts(): DataJsonResult<List<DatingProfileDto>>

    @POST("api/dating/pick/id/{id}")
    suspend fun updatePickState(
        @Path("id") id: String,
        @Query("state") state: Int
    ): JsonResult

    @POST("api/dating/profile/id/{id}/state")
    suspend fun updateProfileState(
        @Path("id") id: String,
        @Query("state") state: Int
    ): JsonResult
}

data class DatingProfileDto(
    val profileId: Int? = null,
    val username: String? = null,
    val nickname: String? = null,
    val grade: Int? = null,
    val faculty: String? = null,
    val hometown: String? = null,
    val content: String? = null,
    val qq: String? = null,
    val wechat: String? = null,
    val area: Int? = null,
    val state: Int? = null,
    val pictureURL: String? = null
)

data class DatingPickDto(
    val pickId: Int? = null,
    val roommateProfile: DatingProfileDto? = null,
    val username: String? = null,
    val content: String? = null,
    val state: Int? = null
)
