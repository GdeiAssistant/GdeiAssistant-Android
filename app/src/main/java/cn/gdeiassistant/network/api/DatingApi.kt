package cn.gdeiassistant.network.api

import cn.gdeiassistant.model.DataJsonResult
import cn.gdeiassistant.model.JsonResult
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface DatingApi {

    @GET("api/dating/profile/area/{area}/start/{start}")
    suspend fun getProfiles(
        @Path("area") area: Int,
        @Path("start") start: Int
    ): DataJsonResult<List<DatingProfileDto>>

    @GET("api/dating/profile/id/{id}")
    suspend fun getProfileDetail(
        @Path("id") id: String
    ): DataJsonResult<DatingProfileDetailDto>

    @GET("api/dating/pick/my/received")
    suspend fun getReceivedPicks(): DataJsonResult<List<DatingPickDto>>

    @GET("api/dating/pick/my/sent")
    suspend fun getSentPicks(): DataJsonResult<List<DatingPickDto>>

    @GET("api/dating/profile/my")
    suspend fun getMyPosts(): DataJsonResult<List<DatingProfileDto>>

    @Multipart
    @POST("api/dating/profile")
    suspend fun publish(
        @Part("nickname") nickname: RequestBody,
        @Part("grade") grade: RequestBody,
        @Part("faculty") faculty: RequestBody,
        @Part("hometown") hometown: RequestBody,
        @Part("content") content: RequestBody,
        @Part("area") area: RequestBody,
        @Part("qq") qq: RequestBody? = null,
        @Part("wechat") wechat: RequestBody? = null,
        @Part image: MultipartBody.Part? = null
    ): JsonResult

    @FormUrlEncoded
    @POST("api/dating/pick")
    suspend fun submitPick(
        @Field("profileId") profileId: String,
        @Field("content") content: String
    ): JsonResult

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

data class DatingProfileDetailDto(
    val profile: DatingProfileDto? = null,
    val pictureURL: String? = null,
    val isContactVisible: Boolean? = null,
    val isPickNotAvailable: Boolean? = null
)

data class DatingPickDto(
    val pickId: Int? = null,
    val roommateProfile: DatingProfileDto? = null,
    val username: String? = null,
    val content: String? = null,
    val state: Int? = null
)
