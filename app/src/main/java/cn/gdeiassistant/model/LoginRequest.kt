package cn.gdeiassistant.model

import androidx.compose.runtime.Immutable
import java.io.Serializable

@Immutable
data class LoginRequest(
    val username: String,
    val password: String,
    val campusCredentialConsent: Boolean? = null,
    val consentScene: String? = null,
    val policyDate: String? = null,
    val effectiveDate: String? = null
) : Serializable
