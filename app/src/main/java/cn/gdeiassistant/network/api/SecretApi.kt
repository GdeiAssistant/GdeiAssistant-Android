package cn.gdeiassistant.network.api

import cn.gdeiassistant.model.DataJsonResult
import cn.gdeiassistant.model.JsonResult
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface SecretApi {

    @GET("api/secret/info/start/{start}/size/{size}")
    suspend fun getPosts(
        @Path("start") start: Int,
        @Path("size") size: Int
    ): DataJsonResult<List<SecretPostDto>>

    @GET("api/secret/profile/start/{start}/size/{size}")
    suspend fun getMyPosts(
        @Path("start") start: Int,
        @Path("size") size: Int
    ): DataJsonResult<List<SecretPostDto>>

    @GET("api/secret/id/{id}")
    suspend fun getDetail(
        @Path("id") id: String
    ): DataJsonResult<SecretPostDto>

    @GET("api/secret/id/{id}/comments")
    suspend fun getComments(
        @Path("id") id: String
    ): DataJsonResult<List<SecretCommentDto>>

    @Multipart
    @POST("api/secret/info")
    suspend fun publish(
        @Part("theme") theme: RequestBody,
        @Part("content") content: RequestBody?,
        @Part("type") type: RequestBody,
        @Part("timer") timer: RequestBody,
        @Part voice: MultipartBody.Part? = null
    ): JsonResult

    @POST("api/secret/id/{id}/comment")
    suspend fun submitComment(
        @Path("id") id: String,
        @Query("comment") comment: String
    ): JsonResult

    @POST("api/secret/id/{id}/like")
    suspend fun setLike(
        @Path("id") id: String,
        @Query("like") like: Int
    ): JsonResult
}

data class SecretPostDto(
    val id: Int? = null,
    val username: String? = null,
    val content: String? = null,
    val theme: Int? = null,
    val type: Int? = null,
    val timer: Int? = null,
    val state: Int? = null,
    val publishTime: String? = null,
    val likeCount: Int? = null,
    val commentCount: Int? = null,
    val liked: Int? = null,
    val voiceURL: String? = null,
    val secretCommentList: List<SecretCommentDto>? = null
)

data class SecretCommentDto(
    val id: Int? = null,
    val contentId: Int? = null,
    val username: String? = null,
    val comment: String? = null,
    val publishTime: String? = null,
    val avatarTheme: Int? = null
)
