package cn.gdeiassistant.network.mock

import okhttp3.Request
import org.junit.Assert.assertTrue
import org.junit.Test

class MockProfileProviderTest {

    @Test
    fun mockProfilePayloadUsesRequestedLocale() {
        val request = Request.Builder()
            .url("https://example.com/api/user/profile")
            .header("Accept-Language", "en-US")
            .build()

        val response = MockProfileProvider.mockUserProfile(request)

        assertTrue(response.contains("\"facultyCode\":11"))
        assertTrue(response.contains("\"majorCode\":\"software_engineering\""))
        assertTrue(response.contains("\"location\":{\"regionCode\":\"CN\",\"stateCode\":\"44\",\"cityCode\":\"1\"}"))
        assertTrue(response.contains("\"hometown\":{\"regionCode\":\"CN\",\"stateCode\":\"44\",\"cityCode\":\"5\"}"))
        assertTrue(response.contains("\"ipArea\":\"Guangdong\""))
    }

    @Test
    fun mockProfileOptionsUseRequestedLocale() {
        val request = Request.Builder()
            .url("https://example.com/api/profile/options")
            .header("Accept-Language", "ja-JP")
            .build()

        val response = MockProfileProvider.mockProfileOptions(request)

        assertTrue(response.contains("\"faculties\":[{\"code\":0,\"majors\":[\"unselected\"]}"))
        assertTrue(response.contains("\"code\":11,\"majors\":[\"unselected\",\"software_engineering\""))
        assertTrue(response.contains("\"marketplaceItemTypes\":[0,1,2"))
        assertTrue(response.contains("\"lostFoundModes\":[0,1]"))
    }
}
