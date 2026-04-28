package cn.gdeiassistant.network

import cn.gdeiassistant.data.SettingsRepository
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MockInterceptorSmokeTest {

    private val gson = Gson()
    private lateinit var client: OkHttpClient
    private var fallbackHits = 0

    @Before
    fun setUp() {
        setMockModeEnabled(true)
        fallbackHits = 0
        client = OkHttpClient.Builder()
            .addInterceptor(MockInterceptor())
            .addInterceptor { chain ->
                fallbackHits += 1
                throw AssertionError("MockInterceptor did not intercept route: ${chain.request().method} ${chain.request().url}")
            }
            .build()
    }

    @After
    fun tearDown() {
        setMockModeEnabled(false)
    }

    @Test
    fun smokeCoversAuthProfileAndAccountCenterFlows() {
        val login = executeJson(
            path = "/api/auth/login",
            method = "POST",
            jsonBody = """{"username":"gdeiassistant","password":"gdeiassistant"}"""
        )
        assertTrue(login.dataObject().get("token").asString.isNotBlank())

        val profile = executeJson("/api/user/profile")
        assertTrue(profile.dataObject().get("username").asString.isNotBlank())

        val locations = executeJson("/api/profile/locations")
        assertTrue(locations.dataArray().size() > 0)

        val options = executeJson("/api/profile/options")
        assertTrue(options.dataObject().getAsJsonArray("faculties").size() > 0)

        val privacy = executeJson("/api/privacy")
        assertTrue(privacy.dataObject().has("cacheAllow"))

        val phoneStatus = executeJson("/api/phone/status")
        assertTrue(phoneStatus.dataObject().get("phone").asString.isNotBlank())

        val emailStatus = executeJson("/api/email/status")
        assertTrue(emailStatus.dataElement().asString.contains("@"))

        val loginRecords = executeJson("/api/ip/start/0/size/20")
        assertTrue(loginRecords.dataArray().size() > 0)

        val userdataState = executeJson("/api/userdata/state")
        assertTrue(userdataState.dataElement().isJsonPrimitive)

        val exportStart = executeJson(path = "/api/userdata/export", method = "POST")
        assertTrue(exportStart.root.get("message").asString.isNotBlank())
        assertTrue(!exportStart.root.has("data") || exportStart.dataElement().isJsonNull)

        val download = executeJson(path = "/api/userdata/download", method = "POST")
        assertTrue(download.dataElement().asString.contains("mock-user-data"))

        val feedback = executeJson(
            path = "/api/feedback",
            method = "POST",
            jsonBody = """{"content":"mock smoke feedback","contact":"tester@example.com"}"""
        )
        assertTrue(feedback.root.get("message").asString.isNotBlank())
        assertTrue(!feedback.root.has("data") || feedback.dataElement().isJsonNull || feedback.dataElement().isJsonPrimitive)

        assertEquals(0, fallbackHits)
    }

    @Test
    fun smokeCoversAcademicCampusInfoAndMessageFlows() {
        val grade = executeJson("/api/grade?year=2025")
        assertTrue(grade.dataObject().getAsJsonArray("firstTermGradeList").size() > 0)

        val schedule = executeJson("/api/schedule?week=6")
        assertTrue(schedule.dataObject().getAsJsonArray("scheduleList").size() > 0)

        val cardInfo = executeJson("/api/card/info")
        assertTrue(cardInfo.dataObject().get("cardNumber").asString.isNotBlank())

        val librarySearch = executeJson("/api/library/search?keyword=Android&page=1")
        assertTrue(librarySearch.dataObject().getAsJsonArray("collectionList").size() > 0)

        val libraryDetail = executeJson("/api/library/detail?detailURL=https://mock/detail?id=001")
        assertTrue(libraryDetail.dataObject().get("bookname").asString.isNotBlank())

        val borrowed = executeJson("/api/library/borrow?password=library123")
        val borrowedItems = borrowed.dataArray()
        assertTrue(borrowedItems.size() > 0)
        val borrowedCode = borrowedItems[0].asJsonObject.get("code").asString

        val renew = executeJson(
            path = "/api/library/renew",
            method = "POST",
            jsonBody = """{"code":"$borrowedCode","password":"library123"}"""
        )
        assertTrue(renew.root.get("message").asString.isNotBlank())
        assertTrue(!renew.root.has("data") || renew.dataElement().isJsonNull || renew.dataElement().isJsonPrimitive)

        val captcha = executeJson("/api/cet/checkcode")
        assertTrue(captcha.dataElement().asString.length > 20)

        val cet = executeJson("/api/cet/query?ticketNumber=123456789012345&name=Lin&checkcode=gd26")
        assertTrue(cet.dataObject().get("totalScore").asString.isNotBlank())

        val spare = executeJson(
            path = "/api/spare/query",
            method = "POST",
            jsonBody = """{"zone":0,"type":0,"classNumber":1}"""
        )
        assertTrue(spare.dataArray().size() > 0)

        val graduateExam = executeJson(
            path = "/api/graduate-exam/query",
            method = "POST",
            jsonBody = """{"name":"Lin","examNumber":"441526010203","idNumber":"440101200409160011"}"""
        )
        assertTrue(graduateExam.dataObject().get("totalScore").asString.isNotBlank())

        val electricity = executeJson(
            path = "/api/data/electricfees",
            method = "POST",
            formBody = mapOf("year" to "2026", "number" to "20231234567")
        )
        assertTrue(electricity.dataObject().get("totalElectricBill").asDouble > 0)

        val yellowPage = executeJson("/api/data/yellowpage")
        assertTrue(yellowPage.dataObject().getAsJsonArray("data").size() > 0)

        val charge = executeJson(
            path = "/api/card/charge",
            method = "POST",
            formBody = mapOf("amount" to "50", "password" to "charge123")
        )
        assertTrue(charge.dataObject().get("alipayURL").asString.contains("mockCharge=50"))
        assertEquals("PAYMENT_SESSION_CREATED", charge.dataObject().get("status").asString)

        val chargeOrders = executeJson("/api/card/charge/orders?page=0&size=20")
        assertTrue(chargeOrders.dataArray().size() > 0)
        assertTrue(chargeOrders.dataArray()[0].asJsonObject.has("orderId"))
        assertTrue(chargeOrders.dataArray()[0].asJsonObject.has("status"))

        val chargeOrder = executeJson("/api/card/charge/orders/mock-charge-order-50")
        assertEquals("mock-charge-order-50", chargeOrder.dataObject().get("orderId").asString)
        assertEquals("PAYMENT_SESSION_CREATED", chargeOrder.dataObject().get("status").asString)

        val announcements = executeJson("/api/information/announcement/start/0/size/10")
        val announcementItems = announcements.dataArray()
        assertTrue(announcementItems.size() > 0)
        val announcementId = announcementItems[0].asJsonObject.get("id").asString

        val announcementDetail = executeJson("/api/information/announcement/id/$announcementId")
        assertEquals(announcementId, announcementDetail.dataObject().get("id").asString)

        val news = executeJson("/api/information/news/type/1/start/0/size/10")
        val newsItems = news.dataArray()
        assertTrue(newsItems.size() > 0)
        val newsId = newsItems[0].asJsonObject.get("id").asString

        val newsDetail = executeJson("/api/information/news/id/$newsId")
        assertEquals(newsId, newsDetail.dataObject().get("id").asString)

        val interactions = executeJson("/api/information/message/interaction/start/0/size/10")
        val interactionItems = interactions.dataArray()
        assertTrue(interactionItems.size() > 0)
        val messageId = interactionItems[0].asJsonObject.get("id").asString

        val unread = executeJson("/api/information/message/unread")
        assertTrue(unread.dataElement().asInt >= 0)

        val readOne = executeJson(path = "/api/information/message/id/$messageId/read", method = "POST")
        assertEquals(messageId, readOne.dataObject().get("id").asString)
        assertTrue(readOne.dataObject().get("isRead").asBoolean)

        val readAll = executeJson(path = "/api/information/message/readall", method = "POST")
        assertTrue(readAll.root.get("message").asString.isNotBlank())
        assertTrue(!readAll.root.has("data") || readAll.dataElement().isJsonNull || readAll.dataElement().isJsonPrimitive)

        assertEquals(0, fallbackHits)
    }

    @Test
    fun smokeCoversCommunityFlows() {
        val marketplace = executeJson("/api/ershou/item/start/0")
        val marketplaceItems = marketplace.dataArray()
        assertTrue(marketplaceItems.size() > 0)
        val marketplaceId = marketplaceItems[0].asJsonObject.get("id").asString

        val marketplaceDetail = executeJson("/api/ershou/item/id/$marketplaceId")
        assertEquals(marketplaceId, marketplaceDetail.dataObject().getAsJsonObject("secondhandItem").get("id").asString)
        val marketplaceProfile = executeJson("/api/ershou/profile")
        assertTrue(marketplaceProfile.dataObject().has("doing"))

        val lostFound = executeJson("/api/lostandfound/lostitem/start/0")
        val lostFoundItems = lostFound.dataArray()
        assertTrue(lostFoundItems.size() > 0)
        val lostFoundId = lostFoundItems[0].asJsonObject.get("id").asString

        val lostFoundDetail = executeJson("/api/lostandfound/item/id/$lostFoundId")
        assertEquals(lostFoundId, lostFoundDetail.dataObject().getAsJsonObject("item").get("id").asString)
        val lostFoundProfile = executeJson("/api/lostandfound/profile")
        assertTrue(lostFoundProfile.dataObject().has("lost"))

        val secret = executeJson("/api/secret/info/start/0/size/10")
        val secretItems = secret.dataArray()
        assertTrue(secretItems.size() > 0)
        val secretId = secretItems[0].asJsonObject.get("id").asString

        val secretDetail = executeJson("/api/secret/id/$secretId")
        assertEquals(secretId, secretDetail.dataObject().get("id").asString)
        val secretComments = executeJson("/api/secret/id/$secretId/comments")
        assertTrue(secretComments.dataArray().size() >= 0)

        val dating = executeJson("/api/dating/profile/area/0/start/0")
        val datingItems = dating.dataArray()
        assertTrue(datingItems.size() > 0)
        val datingId = datingItems[0].asJsonObject.get("profileId").asString

        val datingDetail = executeJson("/api/dating/profile/id/$datingId")
        assertEquals(datingId, datingDetail.dataObject().getAsJsonObject("profile").get("profileId").asString)
        val datingMine = executeJson("/api/dating/profile/my")
        assertTrue(datingMine.dataArray().size() >= 0)

        val express = executeJson("/api/express/start/0/size/10")
        val expressItems = express.dataArray()
        assertTrue(expressItems.size() > 0)
        val expressId = expressItems[0].asJsonObject.get("id").asString

        val expressDetail = executeJson("/api/express/id/$expressId")
        assertEquals(expressId, expressDetail.dataObject().get("id").asString)
        val expressComments = executeJson("/api/express/id/$expressId/comment")
        assertTrue(expressComments.dataArray().size() >= 0)

        val topic = executeJson("/api/topic/start/0/size/10")
        val topicItems = topic.dataArray()
        assertTrue(topicItems.size() > 0)
        val topicId = topicItems[0].asJsonObject.get("id").asString

        val topicDetail = executeJson("/api/topic/id/$topicId")
        assertEquals(topicId, topicDetail.dataObject().get("id").asString)

        val delivery = executeJson("/api/delivery/order/start/0/size/10")
        val deliveryItems = delivery.dataArray()
        assertTrue(deliveryItems.size() > 0)
        val deliveryId = deliveryItems[0].asJsonObject.get("orderId").asString

        val deliveryDetail = executeJson("/api/delivery/order/id/$deliveryId")
        assertEquals(deliveryId, deliveryDetail.dataObject().getAsJsonObject("order").get("orderId").asString)
        val deliveryMine = executeJson("/api/delivery/mine")
        assertTrue(deliveryMine.dataObject().has("published"))

        val photos = executeJson("/api/photograph/statistics/photos")
        assertTrue(photos.dataElement().asInt >= 0)
        val comments = executeJson("/api/photograph/statistics/comments")
        assertTrue(comments.dataElement().asInt >= 0)
        val likes = executeJson("/api/photograph/statistics/likes")
        assertTrue(likes.dataElement().asInt >= 0)

        val photograph = executeJson("/api/photograph/type/1/start/0/size/10")
        val photographItems = photograph.dataArray()
        assertTrue(photographItems.size() > 0)
        val photographId = photographItems[0].asJsonObject.get("id").asString

        val photographDetail = executeJson("/api/photograph/id/$photographId")
        assertEquals(photographId, photographDetail.dataObject().get("id").asString)
        val photographComments = executeJson("/api/photograph/id/$photographId/comment")
        assertTrue(photographComments.dataArray().size() >= 0)

        assertEquals(0, fallbackHits)
    }

    private fun executeJson(
        path: String,
        method: String = "GET",
        jsonBody: String? = null,
        formBody: Map<String, String>? = null
    ): JsonResponse {
        val requestBuilder = Request.Builder().url("https://mock$path")
        when (method) {
            "POST" -> requestBuilder.post(createRequestBody(jsonBody, formBody))
            "DELETE" -> requestBuilder.delete(createRequestBody(jsonBody, formBody))
            else -> requestBuilder.get()
        }
        val response = client.newCall(requestBuilder.build()).execute()
        assertEquals(200, response.code)
        val body = response.body?.string().orEmpty()
        response.close()
        val root = gson.fromJson(body, JsonObject::class.java)
        assertNotNull("response json should not be null for $path", root)
        assertTrue("response should be successful for $path", root.get("success").asBoolean)
        return JsonResponse(path, root)
    }

    private fun createRequestBody(jsonBody: String?, formBody: Map<String, String>?): RequestBody {
        formBody?.let {
            val builder = FormBody.Builder()
            it.forEach { (key, value) -> builder.add(key, value) }
            return builder.build()
        }
        jsonBody?.let {
            return it.toRequestBody("application/json; charset=utf-8".toMediaType())
        }
        return ByteArray(0).toRequestBody(null)
    }

    private fun setMockModeEnabled(enabled: Boolean) {
        val outerResult = runCatching {
            val field = SettingsRepository::class.java.getDeclaredField("mockModeEnabledCache")
            field.isAccessible = true
            field.setBoolean(null, enabled)
        }
        if (outerResult.isSuccess) {
            return
        }

        val companionField = SettingsRepository.Companion::class.java.getDeclaredField("mockModeEnabledCache")
        companionField.isAccessible = true
        companionField.setBoolean(SettingsRepository.Companion, enabled)
    }

    private data class JsonResponse(
        val path: String,
        val root: JsonObject
    ) {
        fun dataElement(): JsonElement {
            assertTrue("response for $path should contain data", root.has("data"))
            return root.get("data")
        }

        fun dataObject(): JsonObject {
            val data = dataElement()
            assertTrue("response data for $path should be object", data.isJsonObject)
            return data.asJsonObject
        }

        fun dataArray(): JsonArray {
            val data = dataElement()
            assertTrue("response data for $path should be array", data.isJsonArray)
            return data.asJsonArray
        }
    }
}
