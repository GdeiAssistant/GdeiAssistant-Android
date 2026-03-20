package cn.gdeiassistant.network.api

import cn.gdeiassistant.model.DataJsonResult
import retrofit2.http.GET
import retrofit2.http.Path

interface NoticeApi {

    @GET("api/information/news/type/{type}/start/{start}/size/{size}")
    suspend fun queryNewsList(
        @Path("type") type: Int,
        @Path("start") start: Int,
        @Path("size") size: Int
    ): DataJsonResult<List<NewsItemDto>>

    @GET("api/information/news/id/{id}")
    suspend fun queryNewsDetail(
        @Path("id") id: String
    ): DataJsonResult<NewsItemDto>
}

data class NewsItemDto(
    val id: String? = null,
    val type: Int? = null,
    val title: String? = null,
    val publishDate: String? = null,
    val content: String? = null,
    val sourceUrl: String? = null
)
