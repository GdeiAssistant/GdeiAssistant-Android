package cn.gdeiassistant.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SocialRealtimeUrlsTest {

    @Test
    fun buildFromHttpsBaseUsesWssAndDoesNotDuplicateApi() {
        val url = SocialRealtimeUrls.build("https://gdeiassistant.azurewebsites.net/api/")
        assertEquals("wss://gdeiassistant.azurewebsites.net/api/social/realtime", url)
        assertFalse(url.contains("/api/api/"))
        assertFalse(url.contains("token="))
        assertFalse(url.contains("Authorization"))
    }

    @Test
    fun buildFromHttpBaseUsesWsAndKeepsNonDefaultPort() {
        val url = SocialRealtimeUrls.build("http://10.0.2.2:8080/")
        assertEquals("ws://10.0.2.2:8080/api/social/realtime", url)
        assertTrue(url.startsWith("ws://"))
    }

    @Test
    fun buildOmitsDefaultHttpsPort() {
        val url = SocialRealtimeUrls.build("https://example.com:443/")
        assertEquals("wss://example.com/api/social/realtime", url)
    }
}
