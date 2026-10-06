package cn.gdeiassistant.network.mock

import cn.gdeiassistant.model.DmPolicy
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MockSocialProviderHttpAndIdempotencyTest {

    private val gson = Gson()

    @Before
    fun setUp() {
        MockSocialProvider.resetDemoGraph()
    }

    @Test
    fun socialFailureUsesPayloadHttpCodeAndKeepsErrorCode() {
        val result = MockSocialProvider.route(
            postJson(
                "http://localhost/api/social/conversations",
                """{"peerId":"${MockSocialProvider.PEER_DAVE_ID}"}"""
            )
        )!!
        assertEquals(403, result.httpCode)
        val json = gson.fromJson(result.body, JsonObject::class.java)
        assertFalse(json.get("success").asBoolean)
        assertEquals(403, json.get("code").asInt)
        assertEquals("PRIVACY_RESTRICTED", json.get("errorCode").asString)
    }

    @Test
    fun unknownDmPolicyKeepsMutual() {
        val updated = MockSocialProvider.route(
            putJson("http://localhost/api/social/privacy", """{"dmPolicy":"NOT_A_REAL_POLICY"}""")
        )!!
        assertEquals(200, updated.httpCode)
        val policy = gson.fromJson(updated.body, JsonObject::class.java)
            .getAsJsonObject("data")
            .get("dmPolicy")
            .asString
        assertEquals(DmPolicy.MUTUAL.name, policy)
    }

    @Test
    fun submittedClientIdRetryReturnsSameMessageAfterPrivacyTightened() {
        val conversationId = gson.fromJson(
            MockSocialProvider.route(get("http://localhost/api/social/conversations"))!!.body,
            JsonObject::class.java
        ).getAsJsonObject("data").getAsJsonArray("items")[0].asJsonObject.get("id").asString

        val clientId = "already-sent-1"
        val first = MockSocialProvider.route(
            postJson(
                "http://localhost/api/social/conversations/$conversationId/messages",
                """{"clientMessageId":"$clientId","content":"已发送正文"}"""
            )
        )!!
        assertEquals(200, first.httpCode)
        val firstId = gson.fromJson(first.body, JsonObject::class.java)
            .getAsJsonObject("data").get("id").asString

        // 收紧对方隐私后，同 clientMessageId 重试仍返回原消息，不新建。
        MockSocialProvider.setPeerDmPolicyForTest(
            peerIdOfFirstConversation(conversationId),
            policy = DmPolicy.NONE
        )
        val retry = MockSocialProvider.route(
            postJson(
                "http://localhost/api/social/conversations/$conversationId/messages",
                """{"clientMessageId":"$clientId","content":"已发送正文"}"""
            )
        )!!
        assertEquals(200, retry.httpCode)
        val retryJson = gson.fromJson(retry.body, JsonObject::class.java)
        assertTrue(retryJson.get("success").asBoolean)
        assertEquals(firstId, retryJson.getAsJsonObject("data").get("id").asString)
    }

    @Test
    fun imageMultipartUsesRealBytesAndShaIdempotency() {
        val conversationId = gson.fromJson(
            MockSocialProvider.route(get("http://localhost/api/social/conversations"))!!.body,
            JsonObject::class.java
        ).getAsJsonObject("data").getAsJsonArray("items")[0].asJsonObject.get("id").asString

        val seededImage = gson.fromJson(
            MockSocialProvider.route(get("http://localhost/api/social/conversations/$conversationId/messages"))!!.body,
            JsonObject::class.java
        ).getAsJsonObject("data").getAsJsonArray("items")
            .first { it.asJsonObject.get("type").asString == "IMAGE" }.asJsonObject
        val seededId = seededImage.get("id").asString
        val jpeg = MockSocialProvider.route(
            get("http://localhost/api/social/conversations/$conversationId/messages/$seededId/image")
        )!!.binaryBody!!
        val decoded = javax.imageio.ImageIO.read(java.io.ByteArrayInputStream(jpeg))
        assertEquals(1, decoded.width)
        assertEquals(1, decoded.height)
        val clientId = "78ae321d-724d-4c65-a1a1-dddd79883146"
        val first = MockSocialProvider.route(
            postMultipartImage(
                "http://localhost/api/social/conversations/$conversationId/messages/image",
                clientId,
                jpeg
            )
        )!!
        assertEquals(200, first.httpCode)
        val firstData = gson.fromJson(first.body, JsonObject::class.java).getAsJsonObject("data")
        assertEquals("IMAGE", firstData.get("type").asString)
        assertTrue(firstData.getAsJsonObject("image").has("url"))
        val messageId = firstData.get("id").asString

        MockSocialProvider.setPeerDmPolicyForTest(peerIdOfFirstConversation(conversationId), cn.gdeiassistant.model.DmPolicy.NONE)
        val sameRetry = MockSocialProvider.route(
            postMultipartImage(
                "http://localhost/api/social/conversations/$conversationId/messages/image",
                clientId,
                jpeg
            )
        )!!
        assertEquals(200, sameRetry.httpCode)
        assertEquals(
            messageId,
            gson.fromJson(sameRetry.body, JsonObject::class.java).getAsJsonObject("data").get("id").asString
        )

        val conflict = MockSocialProvider.route(
            postMultipartImage(
                "http://localhost/api/social/conversations/$conversationId/messages/image",
                clientId,
                jpeg + byteArrayOf(0x00)
            )
        )!!
        assertEquals(409, conflict.httpCode)

        val getImage = MockSocialProvider.route(
            get("http://localhost/api/social/conversations/$conversationId/messages/$messageId/image")
        )!!
        assertEquals(200, getImage.httpCode)
        assertEquals("image/jpeg", getImage.contentType)
        assertEquals("private, no-store", getImage.headers["Cache-Control"])
        assertEquals("nosniff", getImage.headers["X-Content-Type-Options"])
        getImage.toResponse(get("http://localhost/api/social/conversations/$conversationId/messages/$messageId/image")).use { response ->
            assertEquals(200, response.code)
            assertEquals("private, no-store", response.header("Cache-Control"))
            assertEquals("nosniff", response.header("X-Content-Type-Options"))
            assertEquals("image/jpeg", response.header("Content-Type"))
            assertEquals("image/jpeg", response.body?.contentType()?.toString())
            org.junit.Assert.assertArrayEquals(jpeg, response.body?.bytes())
        }
        assertTrue(getImage.binaryBody != null && getImage.binaryBody!!.isNotEmpty())
        assertEquals(0xFF.toByte(), getImage.binaryBody!![0])
        assertEquals(0xD8.toByte(), getImage.binaryBody!![1])
    }

    @Test
    fun privateImageRequiresBearerAndNeverReturnsTextAsImage() {
        val conversation = gson.fromJson(
            MockSocialProvider.route(get("http://localhost/api/social/conversations"))!!.body,
            JsonObject::class.java
        ).getAsJsonObject("data").getAsJsonArray("items")[0].asJsonObject
        val id = conversation.get("id").asString
        val last = conversation.getAsJsonObject("lastMessage").get("id").asString
        val unauthenticated = MockSocialProvider.route(Request.Builder()
            .url("http://localhost/api/social/conversations/$id/messages/$last/image").get().build())!!
        assertEquals(401, unauthenticated.httpCode)
        assertEquals("private, no-store", unauthenticated.headers["Cache-Control"])
        val textMessage = gson.fromJson(
            MockSocialProvider.route(get("http://localhost/api/social/conversations/$id/messages"))!!.body,
            JsonObject::class.java
        ).getAsJsonObject("data").getAsJsonArray("items")
            .first { it.asJsonObject.get("type").asString == "TEXT" }.asJsonObject.get("id").asString
        val text = MockSocialProvider.route(get("http://localhost/api/social/conversations/$id/messages/$textMessage/image"))!!
        assertEquals(404, text.httpCode)
        assertEquals("nosniff", text.headers["X-Content-Type-Options"])
        val wrongConversation = MockSocialProvider.route(get("http://localhost/api/social/conversations/unknown/messages/$last/image"))!!
        assertEquals(404, wrongConversation.httpCode)
    }

    @Test
    fun oneShotImageFailureDoesNotCommitAndIdenticalRetryUploadsOriginalBytesOnce() {
        val conversationId = gson.fromJson(
            MockSocialProvider.route(get("http://localhost/api/social/conversations"))!!.body,
            JsonObject::class.java
        ).getAsJsonObject("data").getAsJsonArray("items")[0].asJsonObject.get("id").asString
        val messagesUrl = "http://localhost/api/social/conversations/$conversationId/messages"
        val seededImage = gson.fromJson(MockSocialProvider.route(get(messagesUrl))!!.body, JsonObject::class.java)
            .getAsJsonObject("data").getAsJsonArray("items")
            .first { it.asJsonObject.get("type").asString == "IMAGE" }.asJsonObject.get("id").asString
        val jpeg = MockSocialProvider.route(get("$messagesUrl/$seededImage/image"))!!.binaryBody!!
        val clientId = "78ae321d-724d-4c65-a1a1-dddd79883147"
        val request = postMultipartImage("$messagesUrl/image", clientId, jpeg)

        MockSocialProvider.failNextImageSendForTest()
        assertEquals(503, MockSocialProvider.route(request)!!.httpCode)
        val afterFailure = gson.fromJson(MockSocialProvider.route(get(messagesUrl))!!.body, JsonObject::class.java)
            .getAsJsonObject("data").getAsJsonArray("items")
        assertFalse(afterFailure.any { it.asJsonObject.get("clientMessageId").asString == clientId })

        val retry = MockSocialProvider.route(request)!!
        assertEquals(200, retry.httpCode)
        val serverId = gson.fromJson(retry.body, JsonObject::class.java).getAsJsonObject("data").get("id").asString
        val duplicate = MockSocialProvider.route(request)!!
        assertEquals(serverId, gson.fromJson(duplicate.body, JsonObject::class.java).getAsJsonObject("data").get("id").asString)
        val committed = gson.fromJson(MockSocialProvider.route(get(messagesUrl))!!.body, JsonObject::class.java)
            .getAsJsonObject("data").getAsJsonArray("items")
        assertEquals(1, committed.count { it.asJsonObject.get("clientMessageId").asString == clientId })
        org.junit.Assert.assertArrayEquals(jpeg, MockSocialProvider.route(get("$messagesUrl/$serverId/image"))!!.binaryBody)
        assertEquals(3, MockSocialProvider.imageSendAttemptsForTest().size)
        assertTrue(MockSocialProvider.imageSendAttemptsForTest().all {
            it.clientMessageId == clientId && it.sha256 == cn.gdeiassistant.data.SocialChatImageMetadata.sha256Hex(jpeg)
        })
    }

    private fun peerIdOfFirstConversation(conversationId: String): String {
        val conversation = gson.fromJson(
            MockSocialProvider.route(get("http://localhost/api/social/conversations/$conversationId"))!!.body,
            JsonObject::class.java
        ).getAsJsonObject("data")
        return conversation.getAsJsonObject("peer").get("id").asString
    }

    private fun get(url: String): Request = Request.Builder().url(url).header("Authorization", "Bearer demo-fixture").get().build()

    private fun putJson(url: String, body: String): Request =
        Request.Builder().url(url).put(body.toRequestBody("application/json".toMediaType())).build()

    private fun postJson(url: String, body: String): Request =
        Request.Builder().url(url).post(body.toRequestBody("application/json".toMediaType())).build()

    private fun postMultipartImage(url: String, clientMessageId: String, imageBytes: ByteArray): Request {
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("clientMessageId", null, clientMessageId.toRequestBody("text/plain; charset=utf-8".toMediaType()))
            .addFormDataPart(
                "image",
                "chat.jpg",
                imageBytes.toRequestBody("image/jpeg".toMediaType())
            )
            .build()
        return Request.Builder().url(url).post(body).build()
    }
}
