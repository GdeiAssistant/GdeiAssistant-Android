package cn.gdeiassistant.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SocialAvatarUrlsTest {

    @Test
    fun nullAvatarReturnsNullForPlaceholderWithoutFabricatingAuthUrl() {
        val url = SocialAvatarUrls.resolve(
            rawBaseUrl = "https://gdeiassistant.azurewebsites.net/api/",
            userId = "user-public-1",
            avatarUrl = null
        )
        assertNull(url)
    }

    @Test
    fun blankAvatarReturnsNullForPlaceholder() {
        assertNull(
            SocialAvatarUrls.resolve(
                rawBaseUrl = "https://gdeiassistant.azurewebsites.net/",
                userId = "user-public-1",
                avatarUrl = "  "
            )
        )
    }

    @Test
    fun absoluteHttpUrlKeptAsIs() {
        val absolute = "https://cdn.example.com/a.png"
        val url = SocialAvatarUrls.resolve(
            rawBaseUrl = "https://gdeiassistant.azurewebsites.net/",
            userId = "u1",
            avatarUrl = absolute
        )
        assertEquals(absolute, url)
    }

    @Test
    fun relativePathResolvedAgainstBaseWithoutToken() {
        val url = SocialAvatarUrls.resolve(
            rawBaseUrl = "https://gdeiassistant.azurewebsites.net/",
            userId = "u1",
            avatarUrl = "/static/avatar/u1.png"
        )
        assertTrue(url!!.startsWith("https://gdeiassistant.azurewebsites.net/static/avatar/u1.png"))
        assertFalse(url.contains("?"))
        assertFalse(url.contains("token="))
    }

    @Test
    fun explicitAuthRelativePathResolvedWithoutTokenQuery() {
        val url = SocialAvatarUrls.resolve(
            rawBaseUrl = "https://gdeiassistant.azurewebsites.net/",
            userId = "user-public-1",
            avatarUrl = "/api/social/users/user-public-1/avatar"
        )
        assertEquals(
            "https://gdeiassistant.azurewebsites.net/api/social/users/user-public-1/avatar",
            url
        )
        assertFalse(url!!.contains("token="))
    }

    @Test
    fun blankUserIdReturnsNull() {
        assertNull(
            SocialAvatarUrls.resolve(
                rawBaseUrl = "https://gdeiassistant.azurewebsites.net/",
                userId = "  ",
                avatarUrl = "https://cdn.example.com/a.png"
            )
        )
    }
}
