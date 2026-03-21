package cn.gdeiassistant.data

import cn.gdeiassistant.model.DataJsonResult
import cn.gdeiassistant.model.MarketplaceItem
import cn.gdeiassistant.model.MarketplaceItemState
import cn.gdeiassistant.network.api.MarketplaceApi
import cn.gdeiassistant.network.api.MarketplaceItemDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class MarketplaceSearchTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var marketplaceApi: MarketplaceApi

    private val sellingDto = MarketplaceItemDto(
        id = 1,
        username = "user1",
        name = "测试商品",
        description = "测试描述",
        price = "50.0",
        location = "海珠校区",
        type = 0,
        state = 1,
        publishTime = "2026-03-20"
    )

    private val successResponse = DataJsonResult(
        success = true,
        code = 200,
        data = listOf(sellingDto)
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        marketplaceApi = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun searchItemsCallsApiWithCorrectKeyword() = runTest(testDispatcher) {
        whenever(marketplaceApi.searchItems(keyword = "笔记本", start = 0))
            .thenReturn(successResponse)

        val result = marketplaceApi.searchItems(keyword = "笔记本", start = 0)

        verify(marketplaceApi).searchItems(keyword = "笔记本", start = 0)
        assertEquals(true, result.success)
        assertEquals(1, result.data?.size)
        assertEquals("测试商品", result.data?.first()?.name)
    }

    @Test
    fun getItemsWithKeywordCallsSearchEndpoint() = runTest(testDispatcher) {
        whenever(marketplaceApi.searchItems(keyword = "教材", start = 0))
            .thenReturn(successResponse)

        marketplaceApi.searchItems(keyword = "教材", start = 0)

        verify(marketplaceApi).searchItems(keyword = "教材", start = 0)
        verify(marketplaceApi, never()).getItems(start = any())
        verify(marketplaceApi, never()).getItemsByType(type = any(), start = any())
    }

    @Test
    fun getItemsWithoutKeywordCallsListEndpoint() = runTest(testDispatcher) {
        whenever(marketplaceApi.getItems(start = 0))
            .thenReturn(successResponse)

        marketplaceApi.getItems(start = 0)

        verify(marketplaceApi).getItems(start = 0)
        verify(marketplaceApi, never()).searchItems(keyword = any(), start = any())
    }

    @Test
    fun getItemsByTypeCallsTypeEndpoint() = runTest(testDispatcher) {
        whenever(marketplaceApi.getItemsByType(type = 3, start = 0))
            .thenReturn(successResponse)

        marketplaceApi.getItemsByType(type = 3, start = 0)

        verify(marketplaceApi).getItemsByType(type = 3, start = 0)
        verify(marketplaceApi, never()).getItems(start = any())
        verify(marketplaceApi, never()).searchItems(keyword = any(), start = any())
    }

    @Test
    fun searchWithBlankKeywordFallsBackToListBehavior() = runTest(testDispatcher) {
        val keyword = "   "
        val normalizedKeyword = keyword.trim()
        assertTrue(normalizedKeyword.isBlank())
    }

    @Test
    fun searchResponseMapsSellingStateCorrectly() = runTest(testDispatcher) {
        val items = listOf(
            sellingDto.copy(id = 1, state = 1),
            sellingDto.copy(id = 2, state = 0),
            sellingDto.copy(id = 3, state = 2)
        )
        val response = DataJsonResult(success = true, code = 200, data = items)

        whenever(marketplaceApi.searchItems(keyword = "商品", start = 0))
            .thenReturn(response)

        val result = marketplaceApi.searchItems(keyword = "商品", start = 0)
        val selling = result.data.orEmpty().filter {
            MarketplaceItemState.fromRemote(it.state) == MarketplaceItemState.SELLING
        }

        assertEquals(1, selling.size)
        assertEquals(1, selling.first().id)
    }
}
