package cn.gdeiassistant.network.mock

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import okhttp3.Request
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MockDataConsistencyTest {

    private val gson = Gson()

    private fun parseDataArray(json: String): JsonArray {
        val root = gson.fromJson(json, JsonObject::class.java)
        assertTrue("response must be successful", root.get("success").asBoolean)
        return root.getAsJsonArray("data")
    }

    @Test
    fun marketplaceItemsHaveAllRequiredFields() {
        val items = MockCommunityProvider.mockMarketplaceItems
        assertTrue("marketplace items should not be empty", items.isNotEmpty())

        items.forEach { item ->
            assertTrue("id must be positive: ${item.id}", item.id > 0)
            assertFalse("name must not be blank: id=${item.id}", item.name.isBlank())
            assertFalse("description must not be blank: id=${item.id}", item.description.isBlank())
            assertFalse("price must not be blank: id=${item.id}", item.price.isBlank())
            assertTrue("state must be 0, 1, or 2: id=${item.id}", item.state in 0..3)
            assertFalse("publishTime must not be blank: id=${item.id}", item.publishTime.isBlank())
        }
    }

    @Test
    fun lostFoundItemsHaveAllRequiredFields() {
        val items = MockCommunityProvider.mockLostFoundItems
        assertTrue("lostandfound items should not be empty", items.isNotEmpty())

        items.forEach { item ->
            assertTrue("id must be positive: ${item.id}", item.id > 0)
            assertFalse("name must not be blank: id=${item.id}", item.name.isBlank())
            assertFalse("description must not be blank: id=${item.id}", item.description.isBlank())
            assertFalse("location must not be blank: id=${item.id}", item.location.isBlank())
            assertTrue("itemType must be non-negative: id=${item.id}", item.itemType >= 0)
            assertTrue("lostType must be 0 or 1: id=${item.id}", item.lostType in 0..1)
            assertTrue("state must be 0 or 1: id=${item.id}", item.state in 0..1)
            assertFalse("publishTime must not be blank: id=${item.id}", item.publishTime.isBlank())
        }
    }

    @Test
    fun expressPostsHaveAllRequiredFields() {
        val posts = MockCommunityProvider.mockExpressPosts
        assertTrue("express posts should not be empty", posts.isNotEmpty())

        posts.forEach { post ->
            assertTrue("id must be positive: ${post.id}", post.id > 0)
            assertFalse("nickname must not be blank: id=${post.id}", post.nickname.isBlank())
            assertFalse("content must not be blank: id=${post.id}", post.content.isBlank())
            assertTrue("selfGender must be 0, 1, or 2: id=${post.id}", post.selfGender in 0..2)
            assertTrue("personGender must be 0, 1, or 2: id=${post.id}", post.personGender in 0..2)
            assertFalse("publishTime must not be blank: id=${post.id}", post.publishTime.isBlank())
        }
    }

    @Test
    fun deliveryOrdersHaveAllRequiredFields() {
        val orders = MockCommunityProvider.mockDeliveryOrderRecords
        assertTrue("delivery orders should not be empty", orders.isNotEmpty())

        orders.forEach { order ->
            assertTrue("orderId must be positive: ${order.orderId}", order.orderId > 0)
            assertFalse("name must not be blank: orderId=${order.orderId}", order.name.isBlank())
            assertFalse("number must not be blank: orderId=${order.orderId}", order.number.isBlank())
            assertFalse("phone must not be blank: orderId=${order.orderId}", order.phone.isBlank())
            assertTrue("price must be positive: orderId=${order.orderId}", order.price > 0)
            assertFalse("company must not be blank: orderId=${order.orderId}", order.company.isBlank())
            assertFalse("address must not be blank: orderId=${order.orderId}", order.address.isBlank())
            assertTrue("state must be 0, 1, or 2: orderId=${order.orderId}", order.state in 0..2)
            assertFalse("orderTime must not be blank: orderId=${order.orderId}", order.orderTime.isBlank())
        }
    }

    @Test
    fun newsEndpointReturnsItemsWithRequiredFields() {
        val request = Request.Builder()
            .url("https://mock/api/information/news/type/1/start/0/size/20")
            .build()
        val json = MockInfoProvider.mockNews(request)
        val data = parseDataArray(json)
        assertTrue("news response should not be empty", data.size() > 0)

        for (i in 0 until data.size()) {
            val item = data.get(i).asJsonObject
            val id = item.get("id").asString
            assertFalse("id must not be blank", id.isBlank())
            assertFalse("title must not be blank: id=$id", item.get("title").asString.isBlank())
            assertTrue("type must be positive: id=$id", item.get("type").asInt > 0)
            assertFalse("publishDate must not be blank: id=$id", item.get("publishDate").asString.isBlank())
            assertFalse("sourceUrl must not be blank: id=$id", item.get("sourceUrl").asString.isBlank())
        }
    }

    @Test
    fun announcementEndpointReturnsItemsWithRequiredFields() {
        val request = Request.Builder()
            .url("https://mock/api/information/announcement/start/0/size/20")
            .build()
        val json = MockInfoProvider.mockAnnouncementPage(request)
        val data = parseDataArray(json)
        assertTrue("announcement response should not be empty", data.size() > 0)

        for (i in 0 until data.size()) {
            val item = data.get(i).asJsonObject
            val id = item.get("id").asString
            assertFalse("id must not be blank", id.isBlank())
            assertFalse("title must not be blank: id=$id", item.get("title").asString.isBlank())
            assertFalse("content must not be blank: id=$id", item.get("content").asString.isBlank())
            assertFalse("publishTime must not be blank: id=$id", item.get("publishTime").asString.isBlank())
        }
    }
}
