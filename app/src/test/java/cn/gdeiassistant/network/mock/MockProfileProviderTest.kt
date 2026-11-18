package cn.gdeiassistant.network.mock

import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MockProfileProviderTest {

    private val gson = Gson()

    @After
    fun tearDown() {
        MockProfileProvider.resetMockProfile()
    }

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

    @Test
    fun mockPatchProfileUpdatesAndExplicitlyClearsNullableFieldsAtomically() {
        val patchRequest = Request.Builder()
            .url("https://example.com/api/profile")
            .header("Accept-Language", "zh-CN")
            .patch(
                """
                {
                  "nickname":"新昵称",
                  "introduction":"新的简介",
                  "faculty":3,
                  "major":"history",
                  "enrollment":2025,
                  "birthday":{"year":2005,"month":5,"date":20},
                  "location":{"region":"CN","state":"44","city":"1"},
                  "hometown":null
                }
                """.trimIndent().toRequestBody("application/json; charset=utf-8".toMediaType())
            )
            .build()

        val patchResult = gson.fromJson(MockProfileProvider.mockPatchProfile(patchRequest), JsonObject::class.java)
        assertTrue(patchResult.get("success").asBoolean)

        val clearRequest = Request.Builder()
            .url("https://example.com/api/profile")
            .patch(
                """
                {
                  "nickname":"新昵称",
                  "introduction":"",
                  "faculty":11,
                  "major":null,
                  "enrollment":null,
                  "birthday":null,
                  "location":null,
                  "hometown":null
                }
                """.trimIndent().toRequestBody("application/json; charset=utf-8".toMediaType())
            )
            .build()

        val clearResult = gson.fromJson(MockProfileProvider.mockPatchProfile(clearRequest), JsonObject::class.java)
        assertTrue(clearResult.get("success").asBoolean)

        val profileData = gson.fromJson(
            MockProfileProvider.mockUserProfile(Request.Builder().url("https://example.com/api/user/profile").build()),
            JsonObject::class.java
        ).getAsJsonObject("data")
        assertEquals("新昵称", profileData.get("nickname").asString)
        assertEquals("", profileData.get("introduction").asString)
        assertEquals(11, profileData.get("facultyCode").asInt)
        assertEquals("", profileData.get("majorCode").asString)
        assertEquals("", profileData.get("enrollment").asString)
        assertEquals("", profileData.get("birthday").asString)
        assertTrue(!profileData.has("location") || profileData.get("location").isJsonNull)
        assertTrue(!profileData.has("hometown") || profileData.get("hometown").isJsonNull)
    }

    @Test
    fun mockPatchProfileRejectsMismatchedMajorWithoutPartialMutation() {
        val invalidPatch = Request.Builder()
            .url("https://example.com/api/profile")
            .patch(
                """
                {
                  "nickname":"不应生效",
                  "faculty":3,
                  "major":"software_engineering"
                }
                """.trimIndent().toRequestBody("application/json; charset=utf-8".toMediaType())
            )
            .build()

        val response = gson.fromJson(MockProfileProvider.mockPatchProfile(invalidPatch), JsonObject::class.java)
        assertFalse(response.get("success").asBoolean)

        val profileData = gson.fromJson(
            MockProfileProvider.mockUserProfile(Request.Builder().url("https://example.com/api/user/profile").build()),
            JsonObject::class.java
        ).getAsJsonObject("data")
        assertEquals(11, profileData.get("facultyCode").asInt)
        assertEquals("software_engineering", profileData.get("majorCode").asString)
    }

    @Test
    fun mockPatchProfileAcceptsDefaultEnglishIntroductionWhenEditingUnrelatedField() {
        val englishProfile = gson.fromJson(
            MockProfileProvider.mockUserProfile(
                Request.Builder()
                    .url("https://example.com/api/user/profile")
                    .header("Accept-Language", "en-US")
                    .build()
            ),
            JsonObject::class.java
        ).getAsJsonObject("data")
        val defaultEnglishIntro = englishProfile.get("introduction").asString
        assertTrue(defaultEnglishIntro.length <= 80)

        val patchRequest = Request.Builder()
            .url("https://example.com/api/profile")
            .header("Accept-Language", "en-US")
            .patch(
                gson.toJson(
                    mapOf(
                        "nickname" to "Updated English Name",
                        "introduction" to defaultEnglishIntro,
                        "faculty" to englishProfile.get("facultyCode").asInt,
                        "major" to englishProfile.get("majorCode").asString
                    )
                ).toRequestBody("application/json; charset=utf-8".toMediaType())
            )
            .build()

        val result = gson.fromJson(MockProfileProvider.mockPatchProfile(patchRequest), JsonObject::class.java)
        assertTrue(result.get("success").asBoolean)
    }
}
