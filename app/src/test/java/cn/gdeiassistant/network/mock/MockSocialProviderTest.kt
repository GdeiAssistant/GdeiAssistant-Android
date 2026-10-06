package cn.gdeiassistant.network.mock

import cn.gdeiassistant.model.DmPolicy
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MockSocialProviderTest {

    private val gson = Gson()

    @Before
    fun setUp() {
        MockSocialProvider.resetDemoGraph()
    }

    @Test
    fun meReturnsSelfRelationshipAndCounts() {
        val json = MockSocialProvider.route(get("http://localhost/api/social/me"))!!.body
        val data = gson.fromJson(json, JsonObject::class.java).getAsJsonObject("data")
        assertEquals(MockSocialProvider.CURRENT_PUBLIC_ID, data.get("id").asString)
        assertEquals("SELF", data.get("relationship").asString)
        assertTrue(data.get("followingCount").asInt >= 1)
        assertTrue(data.get("friendCount").asInt >= 1)
    }

    @Test
    fun followIsIdempotentAndPrivacyDirectionUsesReceiverPolicy() {
        val follow = MockSocialProvider.route(
            put("http://localhost/api/social/users/${MockSocialProvider.PEER_CAROL_ID}/follow")
        )!!.body
        assertTrue(gson.fromJson(follow, JsonObject::class.java).get("success").asBoolean)

        val again = MockSocialProvider.route(
            put("http://localhost/api/social/users/${MockSocialProvider.PEER_CAROL_ID}/follow")
        )!!.body
        assertTrue(gson.fromJson(again, JsonObject::class.java).get("success").asBoolean)

        // Dave uses NONE: sender cannot create conversation.
        val denied = MockSocialProvider.route(
            postJson(
                "http://localhost/api/social/conversations",
                """{"peerId":"${MockSocialProvider.PEER_DAVE_ID}"}"""
            )
        )!!
        assertEquals(403, denied.httpCode)
        val deniedJson = gson.fromJson(denied.body, JsonObject::class.java)
        assertFalse(deniedJson.get("success").asBoolean)
        assertEquals("PRIVACY_RESTRICTED", deniedJson.get("errorCode").asString)
    }

    @Test
    fun sendMessageRetriesSameClientIdAndConflictsOnDifferentBody() {
        val conversationId = gson.fromJson(
            MockSocialProvider.route(get("http://localhost/api/social/conversations"))!!.body,
            JsonObject::class.java
        ).getAsJsonObject("data").getAsJsonArray("items")[0].asJsonObject.get("id").asString

        val clientId = "retry-client-1"
        val first = MockSocialProvider.route(
            postJson(
                "http://localhost/api/social/conversations/$conversationId/messages",
                """{"clientMessageId":"$clientId","content":"第一次"}"""
            )
        )!!.body
        val firstId = gson.fromJson(first, JsonObject::class.java)
            .getAsJsonObject("data").get("id").asString

        val retry = MockSocialProvider.route(
            postJson(
                "http://localhost/api/social/conversations/$conversationId/messages",
                """{"clientMessageId":"$clientId","content":"第一次"}"""
            )
        )!!.body
        assertEquals(
            firstId,
            gson.fromJson(retry, JsonObject::class.java).getAsJsonObject("data").get("id").asString
        )

        val conflict = MockSocialProvider.route(
            postJson(
                "http://localhost/api/social/conversations/$conversationId/messages",
                """{"clientMessageId":"$clientId","content":"不同正文"}"""
            )
        )!!
        assertEquals(409, conflict.httpCode)
        val conflictJson = gson.fromJson(conflict.body, JsonObject::class.java)
        assertFalse(conflictJson.get("success").asBoolean)
        assertEquals("CLIENT_MESSAGE_CONFLICT", conflictJson.get("errorCode").asString)
    }

    @Test
    fun privacyDefaultsToMutualAndUpdates() {
        val current = gson.fromJson(
            MockSocialProvider.route(get("http://localhost/api/social/privacy"))!!.body,
            JsonObject::class.java
        ).getAsJsonObject("data").get("dmPolicy").asString
        assertEquals(DmPolicy.MUTUAL.name, current)

        val updated = MockSocialProvider.route(
            putJson("http://localhost/api/social/privacy", """{"dmPolicy":"FOLLOWING"}""")
        )!!.body
        assertEquals(
            "FOLLOWING",
            gson.fromJson(updated, JsonObject::class.java).getAsJsonObject("data").get("dmPolicy").asString
        )
    }

    private fun get(url: String): Request = Request.Builder().url(url).get().build()

    private fun put(url: String): Request = Request.Builder().url(url).put(ByteArray(0).toRequestBody(null)).build()

    private fun putJson(url: String, body: String): Request =
        Request.Builder().url(url).put(body.toRequestBody("application/json".toMediaType())).build()

    private fun postJson(url: String, body: String): Request =
        Request.Builder().url(url).post(body.toRequestBody("application/json".toMediaType())).build()
}
