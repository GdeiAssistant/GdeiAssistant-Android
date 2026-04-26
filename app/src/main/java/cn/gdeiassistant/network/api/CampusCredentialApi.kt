package cn.gdeiassistant.network.api

import cn.gdeiassistant.model.DataJsonResult
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

data class CampusCredentialStatusDto(
    val hasActiveConsent: Boolean? = null,
    val hasSavedCredential: Boolean? = null,
    val quickAuthEnabled: Boolean? = null,
    val consentedAt: String? = null,
    val revokedAt: String? = null,
    val policyDate: String? = null,
    val effectiveDate: String? = null,
    val maskedCampusAccount: String? = null
)

data class CampusCredentialConsentRequest(
    val scene: String,
    val policyDate: String,
    val effectiveDate: String
)

data class CampusCredentialQuickAuthRequest(
    val enabled: Boolean
)

interface CampusCredentialApi {

    @GET("api/campus-credential/status")
    suspend fun getStatus(): DataJsonResult<CampusCredentialStatusDto>

    @POST("api/campus-credential/consent")
    suspend fun recordConsent(
        @Body body: CampusCredentialConsentRequest
    ): DataJsonResult<CampusCredentialStatusDto>

    @POST("api/campus-credential/revoke")
    suspend fun revokeConsent(): DataJsonResult<CampusCredentialStatusDto>

    @DELETE("api/campus-credential")
    suspend fun deleteCredential(): DataJsonResult<CampusCredentialStatusDto>

    @POST("api/campus-credential/quick-auth")
    suspend fun updateQuickAuth(
        @Body body: CampusCredentialQuickAuthRequest
    ): DataJsonResult<CampusCredentialStatusDto>
}
