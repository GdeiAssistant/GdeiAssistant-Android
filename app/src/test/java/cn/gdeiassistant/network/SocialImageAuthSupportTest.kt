package cn.gdeiassistant.network

import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SocialImageAuthSupportTest {

    private val apiBase = "https://gdeiassistant.azurewebsites.net/".toHttpUrl()

    @Test
    fun sameOriginAvatarGetsBearerHeader() {
        val request = Request.Builder()
            .url("https://gdeiassistant.azurewebsites.net/api/social/users/u1/avatar")
            .get()
            .build()
        val authorized = SocialImageAuthSupport.applyAuthIfNeeded(
            request = request,
            apiBase = apiBase,
            bearerToken = "secret-token"
        )
        assertEquals("Bearer secret-token", authorized.header("Authorization"))
        assertFalse(authorized.url.toString().contains("token="))
        assertEquals(request.url, authorized.url)
    }

    @Test
    fun externalCdnDoesNotGetBearerOrRewrite() {
        val cdn = "https://cdn.example.com/photos/a.png"
        val request = Request.Builder().url(cdn).get().build()
        val result = SocialImageAuthSupport.applyAuthIfNeeded(
            request = request,
            apiBase = apiBase,
            bearerToken = "secret-token"
        )
        assertNull(result.header("Authorization"))
        assertEquals(cdn, result.url.toString())
    }

    @Test
    fun sameHostDifferentPortDoesNotGetBearer() {
        val request = Request.Builder()
            .url("https://gdeiassistant.azurewebsites.net:8443/api/social/users/u1/avatar")
            .get()
            .build()
        assertFalse(SocialImageAuthSupport.shouldAttachAuthorization(request.url, apiBase))
        val result = SocialImageAuthSupport.applyAuthIfNeeded(request, apiBase, "secret-token")
        assertNull(result.header("Authorization"))
    }

    @Test
    fun sameHostDifferentSchemeDoesNotGetBearer() {
        val request = Request.Builder()
            .url("http://gdeiassistant.azurewebsites.net/api/social/users/u1/avatar")
            .get()
            .build()
        assertFalse(SocialImageAuthSupport.shouldAttachAuthorization(request.url, apiBase))
    }

    @Test
    fun sameOriginNonAvatarPathDoesNotGetBearer() {
        val request = Request.Builder()
            .url("https://gdeiassistant.azurewebsites.net/api/social/users/u1")
            .get()
            .build()
        assertFalse(SocialImageAuthSupport.shouldAttachAuthorization(request.url, apiBase))
        assertTrue(
            SocialImageAuthSupport.shouldAttachAuthorization(
                "https://gdeiassistant.azurewebsites.net/api/social/users/u1/avatar",
                "https://gdeiassistant.azurewebsites.net/api/"
            )
        )
    }

    @Test
    fun sameOriginChatImageGetsBearerHeader() {
        val request = Request.Builder()
            .url("https://gdeiassistant.azurewebsites.net/api/social/conversations/c1/messages/m1/image")
            .get()
            .build()
        val authorized = SocialImageAuthSupport.applyAuthIfNeeded(
            request = request,
            apiBase = apiBase,
            bearerToken = "secret-token"
        )
        assertEquals("Bearer secret-token", authorized.header("Authorization"))
        assertTrue(SocialImageAuthSupport.isAuthenticatedChatImagePath(request.url.encodedPath))
        assertFalse(authorized.url.toString().contains("token="))
    }

    @Test
    fun chatImageOnlyAuthorizesExactUnadornedGetPath() {
        val origin = "https://gdeiassistant.azurewebsites.net"
        listOf(
            "/api/social/conversations/1/messages/2/image?token=other",
            "/api/social/conversations/1/messages/2/image#photo",
            "/api/social/conversations/1%2F3/messages/2/image",
            "/api/social/conversations/1/messages/2/image/",
            "/api/social/conversations/1/messages/2/image/other"
        ).forEach { path ->
            assertFalse(SocialImageAuthSupport.shouldAttachAuthorization("$origin$path", origin))
        }
        val userInfo = "$origin/api/social/conversations/1/messages/2/image"
            .toHttpUrl().newBuilder().username("user").password("password").build()
        assertFalse(SocialImageAuthSupport.shouldAttachAuthorization(userInfo, apiBase))
        val head = Request.Builder().url("$origin/api/social/conversations/1/messages/2/image").head().build()
        assertNull(SocialImageAuthSupport.applyAuthIfNeeded(head, apiBase, "secret-token").header("Authorization"))
    }

    @Test
    fun blankTokenDoesNotAddAuthorizationHeader() {
        val request = Request.Builder()
            .url("https://gdeiassistant.azurewebsites.net/api/social/users/u1/avatar")
            .get()
            .build()
        val result = SocialImageAuthSupport.applyAuthIfNeeded(request, apiBase, "  ")
        assertNull(result.header("Authorization"))
    }
}
