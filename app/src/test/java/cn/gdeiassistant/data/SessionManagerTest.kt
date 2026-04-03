package cn.gdeiassistant.data

import android.content.Context
import cn.gdeiassistant.util.TokenUtils
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.MockedStatic
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.mock

class SessionManagerTest {

    private lateinit var context: Context
    private lateinit var tokenUtilsMock: MockedStatic<TokenUtils>

    @Before
    fun setUp() {
        context = mock()
        tokenUtilsMock = mockStatic(TokenUtils::class.java)
    }

    @After
    fun tearDown() {
        tokenUtilsMock.close()
    }

    @Test
    fun constructorRestoresCachedTokenAndUsername() {
        val token = "header.payload.signature"
        tokenUtilsMock.`when`<String?> { TokenUtils.GetUserAccessToken(context) }.thenReturn(token)
        tokenUtilsMock.`when`<String?> { TokenUtils.GetAccessTokenUsername(token) }.thenReturn("alice")

        val sessionManager = SessionManager(context)

        assertTrue(sessionManager.hasActiveSession())
        assertEquals(token, sessionManager.currentToken())
        assertEquals("alice", sessionManager.currentUsername())
    }

    @Test
    fun saveTokenUpdatesInMemorySessionUsingTrimmedUsername() {
        tokenUtilsMock.`when`<String?> { TokenUtils.GetUserAccessToken(context) }.thenReturn(null)

        val sessionManager = SessionManager(context)
        sessionManager.saveToken("fresh-token", "  alice  ")

        tokenUtilsMock.verify { TokenUtils.SaveUserToken("fresh-token", context) }
        assertEquals("fresh-token", sessionManager.currentToken())
        assertEquals("alice", sessionManager.currentUsername())
    }

    @Test
    fun clearTokensClearsCachedSessionState() {
        val token = "cached-token"
        tokenUtilsMock.`when`<String?> { TokenUtils.GetUserAccessToken(context) }.thenReturn(token, null)
        tokenUtilsMock.`when`<String?> { TokenUtils.GetAccessTokenUsername(token) }.thenReturn("alice")

        val sessionManager = SessionManager(context)
        sessionManager.clearTokens()

        tokenUtilsMock.verify { TokenUtils.ClearUserToken(context) }
        assertFalse(sessionManager.hasActiveSession())
        assertEquals(null, sessionManager.currentToken())
        assertEquals(null, sessionManager.currentUsername())
    }
}
