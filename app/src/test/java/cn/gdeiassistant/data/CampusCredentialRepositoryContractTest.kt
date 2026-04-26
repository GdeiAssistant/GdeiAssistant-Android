package cn.gdeiassistant.data

import cn.gdeiassistant.model.CampusCredentialConsentMetadata
import cn.gdeiassistant.model.DataJsonResult
import cn.gdeiassistant.network.api.CampusCredentialApi
import cn.gdeiassistant.network.api.CampusCredentialQuickAuthRequest
import cn.gdeiassistant.network.api.CampusCredentialStatusDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class CampusCredentialRepositoryContractTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var campusCredentialApi: CampusCredentialApi
    private lateinit var repository: CampusCredentialRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        campusCredentialApi = mock()
        repository = CampusCredentialRepository(campusCredentialApi)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun getStatusMapsSafeFieldsAndMasksAccount() = runTest(testDispatcher) {
        whenever(campusCredentialApi.getStatus()).thenReturn(
            DataJsonResult(
                success = true,
                code = 200,
                data = CampusCredentialStatusDto(
                    hasActiveConsent = true,
                    hasSavedCredential = true,
                    quickAuthEnabled = true,
                    consentedAt = "2026-04-26 10:00:00",
                    maskedCampusAccount = "2023001746"
                )
            )
        )

        val result = repository.getCampusCredentialStatus().getOrThrow()

        assertTrue(result.hasActiveConsent)
        assertTrue(result.hasSavedCredential)
        assertTrue(result.quickAuthEnabled)
        assertEquals("2026-04-26 10:00:00", result.consentedAt)
        assertEquals("20****46", result.maskedCampusAccount)
    }

    @Test
    fun recordConsentDelegatesMetadata() = runTest(testDispatcher) {
        whenever(campusCredentialApi.recordConsent(any())).thenReturn(
            DataJsonResult(success = true, code = 200, data = CampusCredentialStatusDto(hasActiveConsent = true))
        )

        repository.recordCampusCredentialConsent(
            CampusCredentialConsentMetadata(
                scene = "LOGIN",
                policyDate = "2026-04-25",
                effectiveDate = "2026-05-11"
            )
        )

        verify(campusCredentialApi).recordConsent(
            argThat {
                scene == "LOGIN" &&
                    policyDate == "2026-04-25" &&
                    effectiveDate == "2026-05-11"
            }
        )
    }

    @Test
    fun revokeAndDeleteDelegateToApi() = runTest(testDispatcher) {
        whenever(campusCredentialApi.revokeConsent()).thenReturn(
            DataJsonResult(success = true, code = 200, data = CampusCredentialStatusDto())
        )
        whenever(campusCredentialApi.deleteCredential()).thenReturn(
            DataJsonResult(success = true, code = 200, data = CampusCredentialStatusDto())
        )

        repository.revokeCampusCredentialConsent()
        repository.deleteCampusCredential()

        verify(campusCredentialApi).revokeConsent()
        verify(campusCredentialApi).deleteCredential()
    }

    @Test
    fun setQuickAuthEnabledDelegatesFlagAndKeepsFailureMessage() = runTest(testDispatcher) {
        whenever(campusCredentialApi.updateQuickAuth(any())).thenReturn(
            DataJsonResult(success = false, code = 400, message = "Missing active consent")
        )

        val result = repository.setQuickAuthEnabled(true)

        verify(campusCredentialApi).updateQuickAuth(CampusCredentialQuickAuthRequest(enabled = true))
        assertTrue(result.isFailure)
        assertEquals("Missing active consent", result.exceptionOrNull()?.message)
    }
}
