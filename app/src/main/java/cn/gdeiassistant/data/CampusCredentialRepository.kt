package cn.gdeiassistant.data

import cn.gdeiassistant.model.CampusCredentialConsentMetadata
import cn.gdeiassistant.model.CampusCredentialStatus
import cn.gdeiassistant.model.maskAccount
import cn.gdeiassistant.network.api.CampusCredentialApi
import cn.gdeiassistant.network.api.CampusCredentialConsentRequest
import cn.gdeiassistant.network.api.CampusCredentialQuickAuthRequest
import cn.gdeiassistant.network.api.CampusCredentialStatusDto
import cn.gdeiassistant.network.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CampusCredentialRepository @Inject constructor(
    private val campusCredentialApi: CampusCredentialApi
) {

    suspend fun getCampusCredentialStatus(): Result<CampusCredentialStatus> = withContext(Dispatchers.IO) {
        safeApiCall { campusCredentialApi.getStatus() }
            .map { it.toDomain() }
    }

    suspend fun recordCampusCredentialConsent(
        metadata: CampusCredentialConsentMetadata
    ): Result<CampusCredentialStatus> = withContext(Dispatchers.IO) {
        safeApiCall {
            campusCredentialApi.recordConsent(
                CampusCredentialConsentRequest(
                    scene = metadata.scene,
                    policyDate = metadata.policyDate,
                    effectiveDate = metadata.effectiveDate
                )
            )
        }.map { it.toDomain() }
    }

    suspend fun revokeCampusCredentialConsent(): Result<CampusCredentialStatus> = withContext(Dispatchers.IO) {
        safeApiCall { campusCredentialApi.revokeConsent() }
            .map { it.toDomain() }
    }

    suspend fun deleteCampusCredential(): Result<CampusCredentialStatus> = withContext(Dispatchers.IO) {
        safeApiCall { campusCredentialApi.deleteCredential() }
            .map { it.toDomain() }
    }

    suspend fun setQuickAuthEnabled(enabled: Boolean): Result<CampusCredentialStatus> = withContext(Dispatchers.IO) {
        safeApiCall {
            campusCredentialApi.updateQuickAuth(CampusCredentialQuickAuthRequest(enabled = enabled))
        }.map { it.toDomain() }
    }

    private fun CampusCredentialStatusDto?.toDomain(): CampusCredentialStatus {
        return CampusCredentialStatus(
            hasActiveConsent = this?.hasActiveConsent ?: false,
            hasSavedCredential = this?.hasSavedCredential ?: false,
            quickAuthEnabled = this?.quickAuthEnabled ?: false,
            consentedAt = this?.consentedAt?.trim()?.takeIf(String::isNotBlank),
            revokedAt = this?.revokedAt?.trim()?.takeIf(String::isNotBlank),
            policyDate = this?.policyDate?.trim()?.takeIf(String::isNotBlank),
            effectiveDate = this?.effectiveDate?.trim()?.takeIf(String::isNotBlank),
            maskedCampusAccount = maskAccount(this?.maskedCampusAccount)
                .takeIf(String::isNotBlank)
        )
    }
}
