package cn.gdeiassistant.model

import androidx.compose.runtime.Immutable
import java.io.Serializable

object CampusCredentialDefaults {
    const val POLICY_DATE = "2026-04-25"
    const val EFFECTIVE_DATE = "2026-05-11"
    const val SCENE_LOGIN = "LOGIN"
}

@Immutable
data class CampusCredentialConsentMetadata(
    val scene: String = CampusCredentialDefaults.SCENE_LOGIN,
    val policyDate: String = CampusCredentialDefaults.POLICY_DATE,
    val effectiveDate: String = CampusCredentialDefaults.EFFECTIVE_DATE
) : Serializable

@Immutable
data class CampusCredentialStatus(
    val hasActiveConsent: Boolean = false,
    val hasSavedCredential: Boolean = false,
    val quickAuthEnabled: Boolean = false,
    val consentedAt: String? = null,
    val revokedAt: String? = null,
    val policyDate: String? = null,
    val effectiveDate: String? = null,
    val maskedCampusAccount: String? = null
) : Serializable
