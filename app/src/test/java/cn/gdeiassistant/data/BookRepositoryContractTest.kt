package cn.gdeiassistant.data

import cn.gdeiassistant.model.CollectionBorrowItem
import cn.gdeiassistant.network.api.CollectionBorrowDto
import cn.gdeiassistant.network.api.CollectionDetailDto
import cn.gdeiassistant.network.api.CollectionDistributionDto
import cn.gdeiassistant.network.api.CollectionItemDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BookRepositoryContractTest {

    private val fallbacks = LibraryTextFallbacks(
        defaultTitle = "默认书名",
        defaultAuthor = "佚名",
        defaultPublisher = "未知出版社",
        defaultPrincipal = "未知责任者",
        defaultPrice = "价格未知",
        defaultDescription = "暂无简介",
        defaultSubject = "暂无主题",
        defaultClassification = "暂无分类",
        defaultDistributionLocation = "馆藏位置待补充",
        defaultDistributionCallNumber = "索书号待补充",
        defaultDistributionBarcode = "条码待补充",
        defaultDistributionState = "状态待补充",
        defaultDate = "日期未知"
    )

    @Test
    fun searchAndBorrowMappingsPreserveCanonicalFields() {
        val searchItem = mapCollectionSearchItem(
            dto = CollectionItemDto(
                bookname = "算法导论",
                author = "Thomas H. Cormen",
                publishingHouse = "机械工业出版社",
                detailURL = ""
            ),
            fallbackTitle = fallbacks.defaultTitle,
            fallbackAuthor = fallbacks.defaultAuthor,
            fallbackPublisher = fallbacks.defaultPublisher,
            idFactory = { "generated-detail-url" }
        )

        assertEquals("generated-detail-url", searchItem.id)
        assertEquals("generated-detail-url", searchItem.detailUrl)
        assertEquals("算法导论", searchItem.title)

        val borrowItem = mapCollectionBorrowItem(
            dto = CollectionBorrowDto(
                id = "",
                sn = "SN-001",
                code = "CODE-001",
                name = "",
                author = "",
                borrowDate = null,
                returnDate = "2026-03-28",
                renewTime = 2
            ),
            fallbackTitle = fallbacks.defaultTitle,
            fallbackAuthor = fallbacks.defaultAuthor,
            fallbackDate = fallbacks.defaultDate,
            idFactory = { "generated-borrow-id" }
        )

        assertEquals("SN-001", borrowItem.sn)
        assertEquals("CODE-001", borrowItem.code)
        assertEquals("SN-001", borrowItem.id)
        assertEquals("默认书名", borrowItem.title)
        assertEquals("佚名", borrowItem.author)
        assertEquals("日期未知", borrowItem.borrowDate)
        assertEquals("2026-03-28", borrowItem.returnDate)
        assertEquals(2, borrowItem.renewCount)
    }

    @Test
    fun detailMappingUsesPersonalPrincipalAndDistributionFallbacks() {
        val detail = mapCollectionDetailInfo(
            dto = CollectionDetailDto(
                collectionDistributionList = listOf(
                    CollectionDistributionDto(
                        location = "",
                        callNumber = "",
                        barcode = "",
                        state = ""
                    )
                ),
                bookname = "",
                author = "",
                principal = "",
                publishingHouse = "",
                price = "",
                physicalDescriptionArea = "",
                personalPrincipal = "Thomas H. Cormen 等著",
                subjectTheme = "",
                chineseLibraryClassification = ""
            ),
            fallbacks = fallbacks,
            idFactory = { "generated-detail-id" }
        )

        assertEquals("generated-detail-id", detail.id)
        assertEquals("默认书名", detail.title)
        assertEquals("佚名", detail.author)
        assertEquals("Thomas H. Cormen 等著", detail.principal)
        assertEquals("未知出版社", detail.publisher)
        assertEquals("馆藏位置待补充", detail.distributions.single().location)
        assertEquals("索书号待补充", detail.distributions.single().callNumber)
    }

    @Test
    fun renewRequestRequiresSnCodeAndPasswordAndTrimsPassword() {
        val validBook = CollectionBorrowItem(
            id = "BK-001",
            sn = "SN-001",
            code = "CODE-001",
            title = "算法导论",
            author = "Thomas H. Cormen",
            borrowDate = "2026-03-01",
            returnDate = "2026-03-28",
            renewCount = 1
        )

        val request = buildLibraryRenewRequest(
            book = validBook,
            password = "  library-pass  ",
            missingRenewInfoMessage = "缺少续借参数",
            passwordRequiredMessage = "请输入密码"
        ).getOrThrow()

        assertEquals("SN-001", request.sn)
        assertEquals("CODE-001", request.code)
        assertEquals("library-pass", request.password)

        val missingInfo = buildLibraryRenewRequest(
            book = validBook.copy(sn = ""),
            password = "library-pass",
            missingRenewInfoMessage = "缺少续借参数",
            passwordRequiredMessage = "请输入密码"
        )
        assertTrue(missingInfo.isFailure)
        assertEquals("缺少续借参数", missingInfo.exceptionOrNull()?.message)

        val missingPassword = buildLibraryRenewRequest(
            book = validBook,
            password = "   ",
            missingRenewInfoMessage = "缺少续借参数",
            passwordRequiredMessage = "请输入密码"
        )
        assertTrue(missingPassword.isFailure)
        assertEquals("请输入密码", missingPassword.exceptionOrNull()?.message)
    }
}
