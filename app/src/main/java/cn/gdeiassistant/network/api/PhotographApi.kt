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

interface PhotographApi {

    @GET("api/photograph/statistics/photos")
    suspend fun getPhotoCount(): DataJsonResult<Int>

    @GET("api/photograph/statistics/comments")
    suspend fun getCommentCount(): DataJsonResult<Int>

    @GET("api/photograph/statistics/likes")
    suspend fun getLikeCount(): DataJsonResult<Int>

    @GET("api/photograph/type/{type}/start/{start}/size/{size}")
    suspend fun getPosts(
        @Path("type") type: Int,
        @Path("start") start: Int,
        @Path("size") size: Int
    ): DataJsonResult<List<PhotographPostDto>>

    @GET("api/photograph/profile/start/{start}/size/{size}")
    suspend fun getMyPosts(
        @Path("start") start: Int,
        @Path("size") size: Int
    ): DataJsonResult<List<PhotographPostDto>>

    @GET("api/photograph/id/{id}")
    suspend fun getDetail(
        @Path("id") id: String
    ): DataJsonResult<PhotographPostDto>

    @GET("api/photograph/id/{id}/index/{index}/image")
    suspend fun getImage(
        @Path("id") id: String,
        @Path("index") index: Int
    ): DataJsonResult<String>

    @GET("api/photograph/id/{id}/comment")
    suspend fun getComments(
        @Path("id") id: String
    ): DataJsonResult<List<PhotographCommentDto>>

    @POST("api/photograph/id/{id}/comment")
    suspend fun submitComment(
        @Path("id") id: String,
        @Query("comment") comment: String
    ): JsonResult

    @POST("api/photograph/id/{id}/like")
    suspend fun like(
        @Path("id") id: String
    ): JsonResult

    @Multipart
    @POST("api/photograph")
    suspend fun publish(
        @Part("title") title: RequestBody,
        @Part("content") content: RequestBody,
        @Part("type") type: RequestBody,
        @Part images: List<MultipartBody.Part>
    ): JsonResult
}

data class PhotographPostDto(
    val id: Int? = null,
    val title: String? = null,
    val content: String? = null,
    val count: Int? = null,
    val type: Int? = null,
    val username: String? = null,
    val createTime: String? = null,
    val likeCount: Int? = null,
    val commentCount: Int? = null,
    val liked: Int? = null,
    val firstImageUrl: String? = null,
    val imageUrls: List<String>? = null,
    val photographCommentList: List<PhotographCommentDto>? = null
)

data class PhotographCommentDto(
    val commentId: Int? = null,
    val photoId: Int? = null,
    val username: String? = null,
    val nickname: String? = null,
    val comment: String? = null,
    val createTime: String? = null
)
