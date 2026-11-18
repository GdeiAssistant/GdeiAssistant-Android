package cn.gdeiassistant.network.mock

import cn.gdeiassistant.model.CampusCredentialDefaults
import cn.gdeiassistant.model.maskAccount
import cn.gdeiassistant.network.mock.MockUtils.getBoolean
import cn.gdeiassistant.network.mock.MockUtils.getString
import cn.gdeiassistant.network.mock.MockUtils.jsonObjectBody
import okhttp3.Request

object MockCampusCredentialProvider {

    private data class MockCampusCredentialState(
        val hasActiveConsent: Boolean = true,
        val hasSavedCredential: Boolean = true,
        val quickAuthAllowed: Boolean = true,
        val consentedAt: String? = "2026-04-26 10:32:15",
        val revokedAt: String? = null,
        val policyDate: String = CampusCredentialDefaults.POLICY_DATE,
        val effectiveDate: String = CampusCredentialDefaults.EFFECTIVE_DATE,
        val campusAccount: String? = MockUtils.MOCK_STUDENT_NUMBER
    )

    private var state = MockCampusCredentialState()

    fun resetForLogin(request: Request) {
        val body = request.jsonObjectBody()
        val hasConsent = body?.getBoolean("campusCredentialConsent") ?: true
        state = if (hasConsent) {
            MockCampusCredentialState()
        } else {
            MockCampusCredentialState(
                hasActiveConsent = false,
                hasSavedCredential = false,
                quickAuthAllowed = false,
                consentedAt = null,
                campusAccount = null
            )
        }
    }

    fun mockStatus(request: Request): String = MockUtils.successDataJson(state.toPayload())

    fun mockConsent(request: Request): String {
        val body = request.jsonObjectBody()
        state = state.copy(
            hasActiveConsent = true,
            consentedAt = "2026-04-26 11:00:00",
            revokedAt = null,
            policyDate = body?.getString("policyDate") ?: CampusCredentialDefaults.POLICY_DATE,
            effectiveDate = body?.getString("effectiveDate") ?: CampusCredentialDefaults.EFFECTIVE_DATE,
            campusAccount = state.campusAccount ?: MockUtils.MOCK_STUDENT_NUMBER
        )
        return MockUtils.successDataJson(state.toPayload(), message = "success")
    }

    fun mockRevoke(request: Request): String {
        state = state.copy(
            hasActiveConsent = false,
            hasSavedCredential = false,
            quickAuthAllowed = false,
            revokedAt = "2026-04-26 11:30:00",
            campusAccount = null
        )
        return MockUtils.successDataJson(state.toPayload(), message = "success")
    }

    fun mockDelete(request: Request): String {
        state = state.copy(
            hasActiveConsent = false,
            hasSavedCredential = false,
            quickAuthAllowed = false,
            revokedAt = state.revokedAt ?: "2026-04-26 11:45:00",
            campusAccount = null
        )
        return MockUtils.successDataJson(state.toPayload(), message = "success")
    }

    fun mockQuickAuth(request: Request): String {
        val enabled = request.jsonObjectBody()?.getBoolean("enabled") ?: false
        if (enabled && !state.hasActiveConsent) {
            return MockUtils.failureJson("缺少有效授权，无法开启快速认证")
        }
        if (enabled && !state.hasSavedCredential) {
            return MockUtils.failureJson("没有已保存的校园凭证，无法开启快速认证")
        }
        state = state.copy(quickAuthAllowed = enabled)
        return MockUtils.successDataJson(state.toPayload(), message = "success")
    }

    private fun MockCampusCredentialState.toPayload(): Map<String, Any?> {
        val quickAuthEnabled = hasActiveConsent && hasSavedCredential && quickAuthAllowed
        return linkedMapOf(
            "hasActiveConsent" to hasActiveConsent,
            "hasSavedCredential" to hasSavedCredential,
            "quickAuthEnabled" to quickAuthEnabled,
            "consentedAt" to consentedAt,
            "revokedAt" to revokedAt,
            "policyDate" to policyDate,
            "effectiveDate" to effectiveDate,
            "maskedCampusAccount" to maskAccount(campusAccount)
        )
    }
}
