package cn.gdeiassistant.network.api

import cn.gdeiassistant.model.DataJsonResult
import cn.gdeiassistant.model.JsonResult
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ExpressApi {

    @GET("api/express/start/{start}/size/{size}")
    suspend fun getPosts(
        @Path("start") start: Int,
        @Path("size") size: Int
    ): DataJsonResult<List<ExpressPostDto>>

    @GET("api/express/keyword/{keyword}/start/{start}/size/{size}")
    suspend fun searchPosts(
        @Path("keyword") keyword: String,
        @Path("start") start: Int,
        @Path("size") size: Int
    ): DataJsonResult<List<ExpressPostDto>>

    @GET("api/express/profile/start/{start}/size/{size}")
    suspend fun getMyPosts(
        @Path("start") start: Int,
        @Path("size") size: Int
    ): DataJsonResult<List<ExpressPostDto>>

    @GET("api/express/id/{id}")
    suspend fun getDetail(
        @Path("id") id: String
    ): DataJsonResult<ExpressPostDto>

    @GET("api/express/id/{id}/comment")
    suspend fun getComments(
        @Path("id") id: String
    ): DataJsonResult<List<ExpressCommentDto>>

    @POST("api/express/id/{id}/comment")
    suspend fun submitComment(
        @Path("id") id: String,
        @Query("comment") comment: String
    ): JsonResult

    @POST("api/express/id/{id}/like")
    suspend fun like(
        @Path("id") id: String
    ): JsonResult

    @POST("api/express/id/{id}/guess")
    suspend fun guess(
        @Path("id") id: String,
        @Query("name") name: String
    ): DataJsonResult<Boolean>

    @FormUrlEncoded
    @POST("api/express")
    suspend fun publish(
        @Field("nickname") nickname: String,
        @Field("realname") realName: String?,
        @Field("selfGender") selfGender: Int,
        @Field("name") targetName: String,
        @Field("content") content: String,
        @Field("personGender") personGender: Int
    ): JsonResult
}

data class ExpressPostDto(
    val id: Int? = null,
    val username: String? = null,
    val nickname: String? = null,
    val realname: String? = null,
    val selfGender: Int? = null,
    val name: String? = null,
    val content: String? = null,
    val personGender: Int? = null,
    val publishTime: String? = null,
    val likeCount: Int? = null,
    val liked: Boolean? = null,
    val commentCount: Int? = null,
    val guessCount: Int? = null,
    val guessSum: Int? = null,
    val canGuess: Boolean? = null
)

data class ExpressCommentDto(
    val id: Int? = null,
    val username: String? = null,
    val nickname: String? = null,
    val expressId: Int? = null,
    val comment: String? = null,
    val publishTime: String? = null
)
