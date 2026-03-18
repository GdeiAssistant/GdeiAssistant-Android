package cn.gdeiassistant.network.api

import cn.gdeiassistant.model.DataJsonResult
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface DataCenterApi {

    @FormUrlEncoded
    @POST("api/data/electricfees")
    suspend fun queryElectricity(
        @Field("name") name: String,
        @Field("number") number: String,
        @Field("year") year: Int
    ): DataJsonResult<ElectricityBillDto>

    @GET("api/data/yellowpage")
    suspend fun getYellowPages(): DataJsonResult<YellowPageResultDto>
}

data class ElectricityBillDto(
    val year: Int? = null,
    val buildingNumber: String? = null,
    val roomNumber: Int? = null,
    val peopleNumber: Int? = null,
    val department: String? = null,
    val usedElectricAmount: Double? = null,
    val freeElectricAmount: Double? = null,
    val feeBasedElectricAmount: Double? = null,
    val electricPrice: Double? = null,
    val totalElectricBill: Double? = null,
    val averageElectricBill: Double? = null
)

data class YellowPageResultDto(
    val data: List<YellowPageEntryDto>? = null,
    val type: List<YellowPageTypeDto>? = null
)

data class YellowPageTypeDto(
    val typeCode: Int? = null,
    val typeName: String? = null
)

data class YellowPageEntryDto(
    val id: Int? = null,
    val typeCode: Int? = null,
    val typeName: String? = null,
    val section: String? = null,
    val campus: String? = null,
    val majorPhone: String? = null,
    val minorPhone: String? = null,
    val address: String? = null,
    val email: String? = null,
    val website: String? = null
)
