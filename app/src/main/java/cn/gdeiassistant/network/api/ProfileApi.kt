package cn.gdeiassistant.network.api

import cn.gdeiassistant.model.DataJsonResult
import cn.gdeiassistant.model.JsonResult
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.TypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ProfileApi {

    @GET("api/user/profile")
    suspend fun getUserProfile(): DataJsonResult<UserProfileDto>

    @GET("api/profile/locations")
    suspend fun getLocationRegions(): DataJsonResult<List<ProfileLocationRegionDto>>

    @GET("api/profile/options")
    suspend fun getProfileOptions(): DataJsonResult<ProfileOptionsDto>

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

    @PATCH("api/profile")
    suspend fun updateProfile(@Body body: ProfilePatchDto): JsonResult

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

    @POST("api/close/submit")
    suspend fun deleteAccount(
        @Body body: PasswordDto
    ): JsonResult
}

data class UserProfileDto(
    val username: String? = null,
    val nickname: String? = null,
    val avatar: String? = null,
    val facultyCode: Int? = null,
    val majorCode: String? = null,
    val enrollment: String? = null,
    val location: ProfileLocationValueDto? = null,
    val hometown: ProfileLocationValueDto? = null,
    val introduction: String? = null,
    val birthday: String? = null,
    val ipArea: String? = null,
    val age: Int? = null
)

data class ProfileLocationValueDto(
    val regionCode: String? = null,
    val stateCode: String? = null,
    val cityCode: String? = null
)

data class PhoneStatusDto(
    val username: String? = null,
    val phone: String? = null,
    val code: Int? = null
)

@JsonAdapter(ProfilePatchDtoTypeAdapter::class)
data class ProfilePatchDto(
    val nickname: String,
    val introduction: String,
    val faculty: Int,
    val major: String? = null,
    val enrollment: Int? = null,
    val birthday: BirthdayUpdateDto? = null,
    val location: LocationUpdateDto? = null,
    val hometown: LocationUpdateDto? = null
)

data class BirthdayUpdateDto(
    val year: Int,
    val month: Int,
    val date: Int
)

data class LocationUpdateDto(
    val region: String,
    val state: String? = null,
    val city: String? = null
)

class ProfilePatchDtoTypeAdapter : TypeAdapter<ProfilePatchDto>() {
    override fun write(out: JsonWriter, value: ProfilePatchDto?) {
        if (value == null) {
            out.nullValue()
            return
        }
        val previousSerializeNulls = out.serializeNulls
        out.serializeNulls = true
        try {
            out.beginObject()
            out.name("nickname").value(value.nickname)
            out.name("introduction").value(value.introduction)
            out.name("faculty").value(value.faculty)
            out.name("major")
            if (value.major != null) out.value(value.major) else out.nullValue()
            out.name("enrollment")
            if (value.enrollment != null) out.value(value.enrollment) else out.nullValue()
            out.name("birthday")
            writeBirthday(out, value.birthday)
            out.name("location")
            writeLocation(out, value.location)
            out.name("hometown")
            writeLocation(out, value.hometown)
            out.endObject()
        } finally {
            out.serializeNulls = previousSerializeNulls
        }
    }

    override fun read(`in`: JsonReader): ProfilePatchDto {
        throw UnsupportedOperationException("ProfilePatchDto is write-only")
    }

    private fun writeBirthday(out: JsonWriter, birthday: BirthdayUpdateDto?) {
        if (birthday == null) {
            out.nullValue()
            return
        }
        out.beginObject()
        out.name("year").value(birthday.year)
        out.name("month").value(birthday.month)
        out.name("date").value(birthday.date)
        out.endObject()
    }

    private fun writeLocation(out: JsonWriter, location: LocationUpdateDto?) {
        if (location == null) {
            out.nullValue()
            return
        }
        out.beginObject()
        out.name("region").value(location.region)
        location.state?.let { out.name("state").value(it) }
        location.city?.let { out.name("city").value(it) }
        out.endObject()
    }
}

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

@JsonAdapter(ProfileDictionaryOptionDtoAdapter::class)
data class ProfileDictionaryOptionDto(
    val code: Int? = null,
    val label: String? = null
)

@JsonAdapter(ProfileFacultyOptionDtoAdapter::class)
data class ProfileFacultyOptionDto(
    val code: Int? = null,
    val label: String? = null,
    val majors: List<ProfileMajorOptionDto>? = null
)

@JsonAdapter(ProfileMajorOptionDtoAdapter::class)
data class ProfileMajorOptionDto(
    val code: String? = null,
    val label: String? = null
)

data class ProfileOptionsDto(
    val faculties: List<ProfileFacultyOptionDto>? = null,
    val marketplaceItemTypes: List<ProfileDictionaryOptionDto>? = null,
    val lostFoundItemTypes: List<ProfileDictionaryOptionDto>? = null,
    val lostFoundModes: List<ProfileDictionaryOptionDto>? = null
)

class ProfileDictionaryOptionDtoAdapter : JsonDeserializer<ProfileDictionaryOptionDto> {
    override fun deserialize(json: JsonElement?, typeOfT: java.lang.reflect.Type?, context: JsonDeserializationContext?): ProfileDictionaryOptionDto {
        if (json == null || json.isJsonNull) return ProfileDictionaryOptionDto()
        if (json.isJsonPrimitive) {
            return ProfileDictionaryOptionDto(code = json.asInt, label = null)
        }

        val jsonObject = json.asJsonObject
        return ProfileDictionaryOptionDto(
            code = jsonObject.get("code")?.takeUnless { it.isJsonNull }?.asInt,
            label = jsonObject.get("label")?.takeUnless { it.isJsonNull }?.asString
        )
    }
}

class ProfileMajorOptionDtoAdapter : JsonDeserializer<ProfileMajorOptionDto> {
    override fun deserialize(json: JsonElement?, typeOfT: java.lang.reflect.Type?, context: JsonDeserializationContext?): ProfileMajorOptionDto {
        if (json == null || json.isJsonNull) return ProfileMajorOptionDto()
        if (json.isJsonPrimitive) {
            return ProfileMajorOptionDto(code = json.asString, label = null)
        }

        val jsonObject = json.asJsonObject
        return ProfileMajorOptionDto(
            code = jsonObject.get("code")?.takeUnless { it.isJsonNull }?.asString,
            label = jsonObject.get("label")?.takeUnless { it.isJsonNull }?.asString
        )
    }
}

class ProfileFacultyOptionDtoAdapter : JsonDeserializer<ProfileFacultyOptionDto> {
    override fun deserialize(json: JsonElement?, typeOfT: java.lang.reflect.Type?, context: JsonDeserializationContext?): ProfileFacultyOptionDto {
        if (json == null || json.isJsonNull || !json.isJsonObject) {
            throw JsonParseException("Invalid profile faculty option")
        }

        val jsonObject = json.asJsonObject
        val majorsType = object : TypeToken<List<ProfileMajorOptionDto>>() {}.type

        return ProfileFacultyOptionDto(
            code = jsonObject.get("code")?.takeUnless { it.isJsonNull }?.asInt,
            label = jsonObject.get("label")?.takeUnless { it.isJsonNull }?.asString,
            majors = context?.deserialize(jsonObject.get("majors"), majorsType)
        )
    }
}
