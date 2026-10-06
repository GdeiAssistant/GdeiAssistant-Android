package cn.gdeiassistant.network.mock

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import okhttp3.Request
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 匿名树洞/表白对外 payload 不得下发可关联校园账号的 username/realname；
 * 内部 record 身份仍保留（竞猜与本人筛选）。
 */
class MockCommunityAnonymityPayloadTest {

    private val gson = Gson()

    @Test
    fun secretPayloadUsesAnonymousLabelsNotCampusUsername() {
        val json = MockCommunityProvider.mockSecretPostList(
            Request.Builder().url("http://localhost/api/secret/info/start/0").get().build()
        )
        val items = dataArray(json)
        assertTrue(items.size() > 0)
        items.forEach { element ->
            val obj = element.asJsonObject
            val username = obj.get("username").asString
            assertFalse(username == MockUtils.MOCK_CURRENT_USERNAME)
            assertFalse(username == MockUtils.MOCK_STUDENT_NUMBER)
            assertTrue(
                "expected anonymous label, got: $username",
                username.contains("匿名") || username.contains("Anonymous", ignoreCase = true)
            )
            assertFalse(obj.has("authorId"))
        }
        // 内部仍保留校园身份字段
        assertTrue(
            MockCommunityProvider.mockSecretPosts.any {
                it.username == MockUtils.MOCK_CURRENT_USERNAME
            }
        )
    }

    @Test
    fun secretCommentsAlsoUseAnonymousLabels() {
        val json = MockCommunityProvider.mockSecretCommentList(
            Request.Builder().url("http://localhost/api/secret/id/6101/comments").get().build()
        )
        val items = dataArray(json)
        assertTrue(items.size() > 0)
        items.forEach { element ->
            val username = element.asJsonObject.get("username").asString
            assertFalse(username == MockUtils.MOCK_CURRENT_USERNAME)
            assertTrue(
                username.contains("匿名") || username.contains("Anonymous", ignoreCase = true)
            )
        }
    }

    @Test
    fun expressPayloadOmitsUsernameRealnameAuthorIdButKeepsInternalIdentity() {
        val json = MockCommunityProvider.mockExpressPostList(
            Request.Builder().url("http://localhost/api/express/start/0").get().build()
        )
        val items = dataArray(json)
        assertTrue(items.size() > 0)
        items.forEach { element ->
            val obj = element.asJsonObject
            assertFalse(obj.has("username"))
            assertFalse(obj.has("realname"))
            assertFalse(obj.has("authorId"))
            assertTrue(obj.has("nickname"))
        }
        assertTrue(MockCommunityProvider.mockExpressPosts.any { it.realname.isNotBlank() })
        assertTrue(
            MockCommunityProvider.mockExpressPosts.any {
                it.username == MockUtils.MOCK_CURRENT_USERNAME
            }
        )
    }

    @Test
    fun expressCommentsOmitUsername() {
        val json = MockCommunityProvider.mockExpressCommentList(
            Request.Builder().url("http://localhost/api/express/id/10101/comment").get().build()
        )
        val items = dataArray(json)
        assertTrue(items.size() > 0)
        items.forEach { element ->
            val obj = element.asJsonObject
            assertFalse(obj.has("username"))
            assertTrue(obj.has("nickname"))
        }
    }

    private fun dataArray(json: String): JsonArray {
        val root = gson.fromJson(json, JsonObject::class.java)
        val data = root.get("data")
        return when {
            data != null && data.isJsonArray -> data.asJsonArray
            else -> JsonArray()
        }
    }
}
