package cn.gdeiassistant.network.api

import cn.gdeiassistant.model.DataJsonResult
import cn.gdeiassistant.model.JsonResult
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ProfileApi {

    @GET("api/user/profile")
    suspend fun getUserProfile(): DataJsonResult<UserProfileDto>

    @GET("api/locationList")
    suspend fun getLocationRegions(): DataJsonResult<List<ProfileLocationRegionDto>>

    @GET("api/phone/status")
    suspend fun getPhoneStatus(): DataJsonResult<PhoneStatusDto>

    @GET("api/email/status")
    suspend fun getEmailStatus(): DataJsonResult<String>

    @GET("api/userdata/state")
    suspend fun getUserDataExportState(): DataJsonResult<Int>

    @POST("api/userdata/export")
    suspend fun requestUserDataExport(): JsonResult

    @POST("api/userdata/download")
    suspend fun downloadUserData(): DataJsonResult<String>

    @GET("api/profile/avatar")
    suspend fun getAvatar(): DataJsonResult<String>

    @GET("api/privacy")
    suspend fun getPrivacySettings(): DataJsonResult<PrivacySettingsDto>

    @POST("api/privacy")
    suspend fun updatePrivacySettings(@Body body: PrivacySettingsDto): JsonResult

    @GET("api/ip/start/{start}/size/{size}")
    suspend fun getLoginRecords(
        @Path("start") start: Int,
        @Path("size") size: Int
    ): DataJsonResult<List<LoginRecordDto>>

    @GET("api/phone/attribution")
    suspend fun getPhoneAttributions(): DataJsonResult<List<PhoneAttributionDto>>

    @FormUrlEncoded
    @POST("api/phone/verification")
    suspend fun sendPhoneVerification(
        @Field("code") code: Int,
        @Field("phone") phone: String
    ): JsonResult

    @FormUrlEncoded
    @POST("api/phone/attach")
    suspend fun bindPhone(
        @Field("code") code: Int,
        @Field("phone") phone: String,
        @Field("randomCode") randomCode: String
    ): JsonResult

    @POST("api/phone/unattach")
    suspend fun unbindPhone(): JsonResult

    @FormUrlEncoded
    @POST("api/email/verification")
    suspend fun sendEmailVerification(
        @Field("email") email: String
    ): JsonResult

    @FormUrlEncoded
    @POST("api/email/bind")
    suspend fun bindEmail(
        @Field("email") email: String,
        @Field("randomCode") randomCode: String
    ): JsonResult

    @POST("api/email/unbind")
    suspend fun unbindEmail(): JsonResult

    @POST("api/profile/nickname")
    suspend fun updateNickname(@Body body: NicknameUpdateDto): JsonResult

    @POST("api/profile/birthday")
    suspend fun updateBirthday(@Body body: BirthdayUpdateDto): JsonResult

    @POST("api/profile/faculty")
    suspend fun updateFaculty(@Body body: FacultyUpdateDto): JsonResult

    @POST("api/profile/major")
    suspend fun updateMajor(@Body body: MajorUpdateDto): JsonResult

    @POST("api/profile/enrollment")
    suspend fun updateEnrollment(@Body body: EnrollmentUpdateDto): JsonResult

    @POST("api/profile/location")
    suspend fun updateLocation(@Body body: LocationUpdateDto): JsonResult

    @POST("api/profile/hometown")
    suspend fun updateHometown(@Body body: LocationUpdateDto): JsonResult

    @POST("api/introduction")
    suspend fun updateIntroduction(@Body body: IntroductionUpdateDto): JsonResult

    @Multipart
    @POST("api/profile/avatar")
    suspend fun uploadAvatar(
        @Part avatar: MultipartBody.Part,
        @Part avatarHd: MultipartBody.Part
    ): JsonResult

    @DELETE("api/profile/avatar")
    suspend fun deleteAvatar(): JsonResult

    @POST("api/feedback")
    suspend fun submitFeedback(@Body body: FeedbackSubmissionDto): JsonResult

    @FormUrlEncoded
    @POST("api/close/submit")
    suspend fun deleteAccount(
        @Field("password") password: String
    ): JsonResult
}

data class UserProfileDto(
    val username: String? = null,
    val nickname: String? = null,
    val avatar: String? = null,
    val faculty: String? = null,
    val major: String? = null,
    val enrollment: String? = null,
    val location: String? = null,
    val hometown: String? = null,
    val introduction: String? = null,
    val birthday: String? = null,
    val ipArea: String? = null,
    val age: Int? = null
)

data class PhoneStatusDto(
    val username: String? = null,
    val phone: String? = null,
    val code: Int? = null
)

data class NicknameUpdateDto(
    val nickname: String
)

data class FacultyUpdateDto(
    val faculty: Int
)

data class MajorUpdateDto(
    val major: String
)

data class EnrollmentUpdateDto(
    val year: Int? = null
)

data class IntroductionUpdateDto(
    val introduction: String
)

data class BirthdayUpdateDto(
    val year: Int? = null,
    val month: Int? = null,
    val date: Int? = null
)

data class LocationUpdateDto(
    val region: String,
    val state: String? = null,
    val city: String? = null
)

data class PrivacySettingsDto(
    val facultyOpen: Boolean? = null,
    val majorOpen: Boolean? = null,
    val locationOpen: Boolean? = null,
    val hometownOpen: Boolean? = null,
    val introductionOpen: Boolean? = null,
    val enrollmentOpen: Boolean? = null,
    val ageOpen: Boolean? = null,
    val cacheAllow: Boolean? = null,
    val robotsIndexAllow: Boolean? = null
)

data class LoginRecordDto(
    val id: Int? = null,
    val ip: String? = null,
    val area: String? = null,
    val country: String? = null,
    val province: String? = null,
    val city: String? = null,
    val network: String? = null,
    val time: String? = null
)

data class PhoneAttributionDto(
    val code: Int? = null,
    val flag: String? = null,
    val name: String? = null
)

data class FeedbackSubmissionDto(
    val content: String,
    val contact: String? = null,
    val type: String? = null
)

data class ProfileLocationCityDto(
    val code: String? = null,
    val name: String? = null,
    val aliasesName: String? = null
)

data class ProfileLocationStateDto(
    val code: String? = null,
    val name: String? = null,
    val aliasesName: String? = null,
    val cityMap: Map<String, ProfileLocationCityDto>? = null
)

data class ProfileLocationRegionDto(
    val code: String? = null,
    val name: String? = null,
    val aliasesName: String? = null,
    val stateMap: Map<String, ProfileLocationStateDto>? = null
)
