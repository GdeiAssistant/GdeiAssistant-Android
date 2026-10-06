package cn.gdeiassistant.network.api

import cn.gdeiassistant.model.DataJsonResult
import cn.gdeiassistant.model.JsonResult
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface DeliveryApi {

    @GET("api/delivery/order/start/{start}/size/{size}")
    suspend fun getOrders(
        @Path("start") start: Int,
        @Path("size") size: Int
    ): DataJsonResult<List<DeliveryOrderDto>>

    @GET("api/delivery/mine")
    suspend fun getMine(): DataJsonResult<DeliveryMineDto>

    @GET("api/delivery/order/id/{id}")
    suspend fun getDetail(
        @Path("id") id: String
    ): DataJsonResult<DeliveryDetailDto>

    @POST("api/delivery/acceptorder")
    suspend fun acceptOrder(
        @Query("orderId") orderId: String
    ): JsonResult

    @POST("api/delivery/trade/id/{id}/finishtrade")
    suspend fun finishTrade(
        @Path("id") tradeId: String
    ): JsonResult

    @DELETE("api/delivery/order/id/{id}")
    suspend fun deleteOrder(
        @Path("id") orderId: String
    ): JsonResult

    @POST("api/delivery/order")
    suspend fun publish(@retrofit2.http.Body body: DeliveryPublishDto): JsonResult

}

data class DeliveryOrderDto(
    val orderId: Int? = null,
    val displayName: String? = null,
    val orderTime: String? = null,
    val taskName: String? = null,
    val pickupCode: String? = null,
    val contactPhone: String? = null,
    val price: Double? = null,
    val pickupLocation: String? = null,
    val deliveryAddress: String? = null,
    val state: Int? = null,
    val remarks: String? = null
)

data class DeliveryTradeDto(
    val tradeId: Int? = null,
    val orderId: Int? = null,
    val createTime: String? = null,
    val displayName: String? = null,
    val state: Int? = null
)

data class DeliveryDetailDto(
    val order: DeliveryOrderDto? = null,
    val detailType: Int? = null,
    val trade: DeliveryTradeDto? = null
)

data class DeliveryMineDto(
    val published: List<DeliveryOrderDto>? = null,
    val accepted: List<DeliveryOrderDto>? = null
)

data class DeliveryPublishDto(
    val taskName: String,
    val pickupCode: String,
    val contactPhone: String,
    val price: String,
    val pickupLocation: String,
    val deliveryAddress: String,
    val remarks: String
)
