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

interface MarketplaceApi {

    @GET("api/ershou/item/start/{start}")
    suspend fun getItems(
        @Path("start") start: Int
    ): DataJsonResult<List<MarketplaceItemDto>>

    @GET("api/ershou/item/type/{type}/start/{start}")
    suspend fun getItemsByType(
        @Path("type") type: Int,
        @Path("start") start: Int
    ): DataJsonResult<List<MarketplaceItemDto>>

    @GET("api/ershou/item/id/{id}")
    suspend fun getItemDetail(
        @Path("id") id: String
    ): DataJsonResult<MarketplaceDetailDto>

    @GET("api/ershou/item/id/{id}/preview")
    suspend fun getItemPreview(
        @Path("id") id: String
    ): DataJsonResult<String>

    @GET("api/ershou/profile")
    suspend fun getProfileSummary(): DataJsonResult<MarketplacePersonalSummaryDto>

    @Multipart
    @POST("api/ershou/item")
    suspend fun publish(
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("price") price: RequestBody,
        @Part("location") location: RequestBody,
        @Part("type") type: RequestBody,
        @Part("qq") qq: RequestBody,
        @Part("phone") phone: RequestBody? = null,
        @Part images: List<MultipartBody.Part>
    ): JsonResult

    @FormUrlEncoded
    @POST("api/ershou/item/id/{id}")
    suspend fun updateItem(
        @Path("id") id: String,
        @Field("name") name: String,
        @Field("description") description: String,
        @Field("price") price: Double,
        @Field("location") location: String,
        @Field("type") type: Int,
        @Field("qq") qq: String,
        @Field("phone") phone: String? = null
    ): JsonResult

    @POST("api/ershou/item/state/id/{id}")
    suspend fun updateItemState(
        @Path("id") id: String,
        @Query("state") state: Int
    ): JsonResult
}

data class MarketplaceItemDto(
    val id: Int? = null,
    val username: String? = null,
    val name: String? = null,
    val description: String? = null,
    val price: String? = null,
    val location: String? = null,
    val type: Int? = null,
    val qq: String? = null,
    val phone: String? = null,
    val state: Int? = null,
    val publishTime: String? = null,
    val pictureURL: List<String>? = null
)

data class MarketplaceDetailDto(
    val profile: MarketplaceProfileDto? = null,
    val secondhandItem: MarketplaceItemDto? = null
)

data class MarketplaceProfileDto(
    val avatarURL: String? = null,
    val username: String? = null,
    val nickname: String? = null,
    val faculty: Int? = null,
    val enrollment: Int? = null,
    val major: String? = null
)

data class MarketplacePersonalSummaryDto(
    val doing: List<MarketplaceItemDto>? = null,
    val sold: List<MarketplaceItemDto>? = null,
    val off: List<MarketplaceItemDto>? = null
)
