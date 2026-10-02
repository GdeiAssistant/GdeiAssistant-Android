package cn.gdeiassistant.data

import android.content.Context
import cn.gdeiassistant.util.TokenUtils
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
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

    @Test fun cookieForAnotherPathMustRemainStoredAfterUnrelatedRequest() {
        val jar = SessionManager(context).cookieJar
        val cardUrl = "https://example.test/api/card/balance".toHttpUrl()
        val newsUrl = "https://example.test/api/news".toHttpUrl()
        val cookie = Cookie.Builder().name("audit_session").value("synthetic")
            .hostOnlyDomain("example.test").path("/api/card")
            .expiresAt(System.currentTimeMillis() + 60_000).build()
        jar.saveFromResponse(cardUrl, listOf(cookie))
        assertEquals(listOf(cookie), jar.loadForRequest(cardUrl))
        assertTrue("Path cookie must not be sent to unrelated path", jar.loadForRequest(newsUrl).isEmpty())
        assertEquals("Unrelated path lookup destroyed valid cookie", listOf(cookie), jar.loadForRequest(cardUrl))
    }

    @Test fun secureCookieMustRemainStoredAfterHttpLookup() {
        val jar = SessionManager(context).cookieJar
        val secureUrl = "https://example.test/audit".toHttpUrl()
        val insecureUrl = "http://example.test/audit".toHttpUrl()
        val cookie = Cookie.Builder().name("audit_secure").value("synthetic")
            .hostOnlyDomain("example.test").path("/").secure()
            .expiresAt(System.currentTimeMillis() + 60_000).build()
        jar.saveFromResponse(secureUrl, listOf(cookie))
        assertEquals(listOf(cookie), jar.loadForRequest(secureUrl))
        assertTrue("Secure cookie must not be sent over HTTP", jar.loadForRequest(insecureUrl).isEmpty())
        assertEquals("HTTP lookup destroyed still-valid Secure cookie", listOf(cookie), jar.loadForRequest(secureUrl))
    }

    @Test fun cookieRepeatedMatchingLookupControl() {
        val jar = SessionManager(context).cookieJar
        val url = "https://example.test/api/card/balance".toHttpUrl()
        val cookie = Cookie.Builder().name("audit_control").value("synthetic")
            .hostOnlyDomain("example.test").path("/api/card")
            .expiresAt(System.currentTimeMillis() + 60_000).build()
        jar.saveFromResponse(url, listOf(cookie))
        repeat(3) { assertEquals(listOf(cookie), jar.loadForRequest(url)) }
    }

    @Test
    fun expiredReplacementRemovesOnlyItsCookie() {
        val jar = SessionManager(context).cookieJar
        val url = "https://example.test/api/card".toHttpUrl()
        val cookie = Cookie.Builder().name("session").value("first")
            .hostOnlyDomain("example.test").path("/")
            .expiresAt(System.currentTimeMillis() + 60_000).build()
        val unrelated = Cookie.Builder().name("other").value("retained")
            .hostOnlyDomain("example.test").path("/other").build()
        jar.saveFromResponse(url, listOf(cookie, unrelated))
        val replacement = Cookie.Builder().name("session").value("updated")
            .hostOnlyDomain("example.test").path("/").build()
        jar.saveFromResponse(url, listOf(replacement))
        assertEquals(listOf(replacement), jar.loadForRequest(url))
        val expired = Cookie.Builder().name("session").value("deleted")
            .hostOnlyDomain("example.test").path("/").expiresAt(1).build()
        jar.saveFromResponse(url, listOf(expired))
        assertTrue(jar.loadForRequest(url).isEmpty())
        assertEquals(listOf(unrelated), jar.loadForRequest("https://example.test/other".toHttpUrl()))
    }

    @Test
    fun clearingTokensAlsoClearsCookiesForAllPaths() {
        val manager = SessionManager(context)
        val url = "https://example.test/private".toHttpUrl()
        val cookie = Cookie.Builder().name("session").value("synthetic")
            .hostOnlyDomain("example.test").path("/private").secure().build()
        manager.cookieJar.saveFromResponse(url, listOf(cookie))
        assertTrue(manager.cookieJar.loadForRequest("https://other.test/private".toHttpUrl()).isEmpty())
        assertEquals(listOf(cookie), manager.cookieJar.loadForRequest(url))
        manager.clearTokens()
        assertTrue(manager.cookieJar.loadForRequest(url).isEmpty())
    }
}
