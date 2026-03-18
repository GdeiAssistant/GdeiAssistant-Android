package cn.gdeiassistant.network.api

import cn.gdeiassistant.model.DataJsonResult
import cn.gdeiassistant.model.JsonResult
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface BookApi {

    @GET("api/collection/search")
    suspend fun searchCollections(
        @Query("keyword") keyword: String,
        @Query("page") page: Int
    ): DataJsonResult<CollectionSearchResponseDto>

    @GET("api/collection/detail")
    suspend fun getCollectionDetail(
        @Query("detailURL") detailUrl: String
    ): DataJsonResult<CollectionDetailDto>

    @GET("api/collection/borrow")
    suspend fun getBorrowedBooks(
        @Query("password") password: String? = null
    ): DataJsonResult<List<CollectionBorrowDto>>

    @POST("api/collection/renew")
    suspend fun renew(
        @Query("sn") sn: String,
        @Query("code") code: String
    ): JsonResult
}

data class CollectionSearchResponseDto(
    val sumPage: Int? = null,
    val collectionList: List<CollectionItemDto>? = null
)

data class CollectionItemDto(
    val bookname: String? = null,
    val author: String? = null,
    val publishingHouse: String? = null,
    val detailURL: String? = null
)

data class CollectionDistributionDto(
    val location: String? = null,
    val callNumber: String? = null,
    val barcode: String? = null,
    val state: String? = null
)

data class CollectionDetailDto(
    val collectionDistributionList: List<CollectionDistributionDto>? = null,
    val bookname: String? = null,
    val author: String? = null,
    val principal: String? = null,
    val publishingHouse: String? = null,
    val price: String? = null,
    val physicalDescriptionArea: String? = null,
    val personalPrincipal: String? = null,
    val subjectTheme: String? = null,
    val chineseLibraryClassification: String? = null
)

data class CollectionBorrowDto(
    val id: String? = null,
    val sn: String? = null,
    val code: String? = null,
    val name: String? = null,
    val author: String? = null,
    val borrowDate: String? = null,
    val returnDate: String? = null,
    val renewTime: Int? = null
)
