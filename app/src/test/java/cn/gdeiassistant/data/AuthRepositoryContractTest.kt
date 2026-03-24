package cn.gdeiassistant.data

import cn.gdeiassistant.model.DataJsonResult
import cn.gdeiassistant.model.UserLoginResult
import cn.gdeiassistant.network.AppException
import cn.gdeiassistant.network.api.AuthApi
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
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Contract tests for [AuthRepository]: verifies that the repository correctly maps
 * [AuthApi] responses to [Result] and delegates token persistence to [SessionManager].
 *
 * These tests operate at the repository level using mocked API/session dependencies.
 * They do not require Android context for the tested paths.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryContractTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authApi: AuthApi
    private lateinit var sessionManager: SessionManager
    private lateinit var authRepository: AuthRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authApi = mock()
        sessionManager = mock()
        authRepository = AuthRepository(
            context = mock(),
            authApi = authApi,
            sessionManager = sessionManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loginSuccessTokenIsSavedAndResultIsSuccess() = runTest(testDispatcher) {
        val token = "eyJhbGciOiJIUzI1NiJ9.test.signature"
        whenever(authApi.login(any())).thenReturn(
            DataJsonResult(success = true, code = 200, data = UserLoginResult(token = token))
        )

        val result = authRepository.login("testuser", "password123")

        assertTrue("Expected success result", result.isSuccess)
        verify(sessionManager).saveToken(token, "testuser")
    }

    @Test
    fun loginSuccessTrimsUsernameBeforeSaving() = runTest(testDispatcher) {
        val token = "valid-token"
        whenever(authApi.login(any())).thenReturn(
            DataJsonResult(success = true, code = 200, data = UserLoginResult(token = token))
        )

        authRepository.login("  testuser  ", "password")

        verify(sessionManager).saveToken(token, "testuser")
    }

    @Test
    fun loginServerRejectionReturnsFailureWithBackendMessage() = runTest(testDispatcher) {
        whenever(authApi.login(any())).thenReturn(
            DataJsonResult(
                success = false,
                code = 401,
                message = "用户名或密码错误"
            )
        )

        val result = authRepository.login("testuser", "wrongpassword")

        assertTrue("Expected failure result", result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue("Exception should be AppException", exception is AppException)
        assertEquals("用户名或密码错误", exception?.message)
    }

    @Test
    fun loginApiCallIncludesUsernameAndPassword() = runTest(testDispatcher) {
        whenever(authApi.login(any())).thenReturn(
            DataJsonResult(success = true, code = 200, data = UserLoginResult(token = "tok"))
        )

        authRepository.login("alice", "s3cret")

        verify(authApi).login(mapOf("username" to "alice", "password" to "s3cret"))
    }
}
