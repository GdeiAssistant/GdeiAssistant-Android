package cn.gdeiassistant.network.api

import cn.gdeiassistant.model.DataJsonResult
import retrofit2.http.GET
import retrofit2.http.Path

interface NoticeApi {

    @GET("api/news/type/{type}/start/{start}/size/{size}")
    suspend fun queryNewsList(
        @Path("type") type: Int,
        @Path("start") start: Int,
        @Path("size") size: Int
    ): DataJsonResult<List<NewsItemDto>>
}

data class NewsItemDto(
    val id: String? = null,
    val title: String? = null,
    val publishDate: String? = null,
    val content: String? = null
)
