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

interface TopicApi {

    @GET("api/topic/start/{start}/size/{size}")
    suspend fun getPosts(
        @Path("start") start: Int,
        @Path("size") size: Int
    ): DataJsonResult<List<TopicPostDto>>

    @GET("api/topic/keyword/{keyword}/start/{start}/size/{size}")
    suspend fun searchPosts(
        @Path("keyword") keyword: String,
        @Path("start") start: Int,
        @Path("size") size: Int
    ): DataJsonResult<List<TopicPostDto>>

    @GET("api/topic/profile/start/{start}/size/{size}")
    suspend fun getMyPosts(
        @Path("start") start: Int,
        @Path("size") size: Int
    ): DataJsonResult<List<TopicPostDto>>

    @GET("api/topic/id/{id}")
    suspend fun getDetail(
        @Path("id") id: String
    ): DataJsonResult<TopicPostDto>

    @POST("api/topic/id/{id}/like")
    suspend fun like(
        @Path("id") id: String
    ): JsonResult

    @Multipart
    @POST("api/topic")
    suspend fun publish(
        @Part("topic") topic: RequestBody,
        @Part("content") content: RequestBody,
        @Part("count") count: RequestBody,
        @Part images: List<MultipartBody.Part>
    ): JsonResult

    @GET("api/topic/id/{id}/index/{index}/image")
    suspend fun getImage(
        @Path("id") id: String,
        @Path("index") index: Int
    ): DataJsonResult<String>
}

data class TopicPostDto(
    val id: Int? = null,
    val username: String? = null,
    val topic: String? = null,
    val content: String? = null,
    val count: Int? = null,
    val publishTime: String? = null,
    val likeCount: Int? = null,
    val liked: Boolean? = null,
    val firstImageUrl: String? = null,
    val imageUrls: List<String>? = null
)
