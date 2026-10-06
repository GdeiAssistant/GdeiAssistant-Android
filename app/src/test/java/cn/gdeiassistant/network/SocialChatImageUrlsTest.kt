package cn.gdeiassistant.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import okhttp3.HttpUrl.Companion.toHttpUrl

class SocialChatImageUrlsTest {

    @Test
    fun buildsSameOriginImagePathWithoutTokenQuery() {
        val url = SocialChatImageUrls.messageImage(
            rawBaseUrl = "https://gdeiassistant.azurewebsites.net/api/",
            conversationId = "c-1",
            messageId = "m-9"
        )
        assertEquals(
            "https://gdeiassistant.azurewebsites.net/api/social/conversations/c-1/messages/m-9/image",
            url
        )
        assertFalse(url.contains("?"))
        assertFalse(url.contains("token="))
        assertTrue(
            SocialImageAuthSupport.shouldAttachAuthorization(
                url,
                "https://gdeiassistant.azurewebsites.net/"
            )
        )
    }

    @Test
    fun clearsInheritedQueryCredentialsAndFragment() {
        val url = SocialChatImageUrls.messageImage(
            "https://user:pass@gdeiassistant.azurewebsites.net/api/?token=old#photo".toHttpUrl(),
            "10", "20"
        )
        assertEquals("https://gdeiassistant.azurewebsites.net/api/social/conversations/10/messages/20/image", url)
    }

    @Test
    fun rejectsPathInjectionInEitherIdentifier() {
        listOf("1/2", "../1", "%2F", "1?token=x", "1#x", "").forEach { invalid ->
            assertTrue(runCatching { SocialChatImageUrls.messageImage("https://example.com", invalid, "2") }.isFailure)
            assertTrue(runCatching { SocialChatImageUrls.messageImage("https://example.com", "1", invalid) }.isFailure)
        }
    }
}
