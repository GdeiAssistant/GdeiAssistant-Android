package cn.gdeiassistant.data

import android.content.Context
import cn.gdeiassistant.R
import cn.gdeiassistant.model.CollectionBorrowItem
import cn.gdeiassistant.model.DataJsonResult
import cn.gdeiassistant.network.api.CollectionBorrowDto
import cn.gdeiassistant.network.api.CollectionDetailDto
import cn.gdeiassistant.network.api.CollectionDistributionDto
import cn.gdeiassistant.network.api.CollectionItemDto
import cn.gdeiassistant.network.api.CollectionSearchResponseDto
import cn.gdeiassistant.network.api.LibraryApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class LibraryRepositoryContractTest {

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
    fun searchAndBorrowMappingsPreserveCanonicalFieldsAndDropMissingIds() = runTest {
        val searchItem = mapCollectionSearchItem(
            dto = CollectionItemDto(
                bookname = "算法导论",
                author = "Thomas H. Cormen",
                publishingHouse = "机械工业出版社",
                detailURL = " https://lib.gdeiassistant.cn/book/detail?id=101 "
            ),
            fallbackTitle = fallbacks.defaultTitle,
            fallbackAuthor = fallbacks.defaultAuthor,
            fallbackPublisher = fallbacks.defaultPublisher
        )

        assertEquals("https://lib.gdeiassistant.cn/book/detail?id=101", searchItem?.id)
        assertEquals("https://lib.gdeiassistant.cn/book/detail?id=101", searchItem?.detailUrl)
        assertEquals("算法导论", searchItem?.title)

        assertNull(
            mapCollectionSearchItem(
                dto = CollectionItemDto(
                    bookname = "无效条目",
                    author = "匿名",
                    publishingHouse = "出版社",
                    detailURL = "   "
                ),
                fallbackTitle = fallbacks.defaultTitle,
                fallbackAuthor = fallbacks.defaultAuthor,
                fallbackPublisher = fallbacks.defaultPublisher
            )
        )

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
            fallbackDate = fallbacks.defaultDate
        )!!

        assertEquals("SN-001", borrowItem.sn)
        assertEquals("CODE-001", borrowItem.code)
        assertEquals("SN-001", borrowItem.id)
        assertEquals("默认书名", borrowItem.title)
        assertEquals("佚名", borrowItem.author)
        assertEquals("日期未知", borrowItem.borrowDate)
        assertEquals("2026-03-28", borrowItem.returnDate)
        assertEquals(2, borrowItem.renewCount)

        assertNull(
            mapCollectionBorrowItem(
                dto = CollectionBorrowDto(id = " ", sn = null, code = "CODE-002", name = "脏借阅记录"),
                fallbackTitle = fallbacks.defaultTitle,
                fallbackAuthor = fallbacks.defaultAuthor,
                fallbackDate = fallbacks.defaultDate
            )
        )

        val context = mock<Context>()
        whenever(context.getString(R.string.library_collection_default_title)).thenReturn(fallbacks.defaultTitle)
        whenever(context.getString(R.string.library_collection_default_author)).thenReturn(fallbacks.defaultAuthor)
        whenever(context.getString(R.string.library_collection_default_publisher)).thenReturn(fallbacks.defaultPublisher)
        whenever(context.getString(R.string.library_collection_unknown_date)).thenReturn(fallbacks.defaultDate)
        val libraryApi = mock<LibraryApi>()
        whenever(libraryApi.searchCollections("算法", 1)).thenReturn(
            DataJsonResult(
                success = true,
                code = 200,
                data = CollectionSearchResponseDto(
                    sumPage = 1,
                    collectionList = listOf(
                        CollectionItemDto(bookname = "脏条目", detailURL = ""),
                        CollectionItemDto(bookname = "有效条目", detailURL = "https://lib.gdeiassistant.cn/book/detail?id=102")
                    )
                )
            )
        )
        whenever(libraryApi.getBorrowedBooks(cn.gdeiassistant.network.api.PasswordDto("pwd"))).thenReturn(
            DataJsonResult(
                success = true,
                code = 200,
                data = listOf(
                    CollectionBorrowDto(id = null, sn = "", name = "无主键借阅"),
                    CollectionBorrowDto(id = "b2", sn = "SN-002", code = "C-002", name = "正常借阅")
                )
            )
        )
        val repository = LibraryRepository(context, libraryApi)
        val page = repository.searchCollections("算法", 1).getOrThrow()
        assertEquals(1, page.items.size)
        assertEquals("https://lib.gdeiassistant.cn/book/detail?id=102", page.items.single().detailUrl)

        val borrowed = repository.getBorrowedBooks("pwd").getOrThrow()
        assertEquals(1, borrowed.size)
        assertEquals("b2", borrowed.single().id)
    }

    @Test
    fun detailMappingUsesRemoteIdentifierAndDropsDistributionsWithoutBarcode() {
        val detail = mapCollectionDetailInfo(
            detailUrl = "https://lib.gdeiassistant.cn/book/detail?id=001",
            dto = CollectionDetailDto(
                collectionDistributionList = listOf(
                    CollectionDistributionDto(
                        location = "无效馆藏",
                        callNumber = "TP312",
                        barcode = " ",
                        state = "在馆"
                    ),
                    CollectionDistributionDto(
                        location = "",
                        callNumber = "",
                        barcode = "BAR-001",
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
            fallbacks = fallbacks
        )

        assertEquals("https://lib.gdeiassistant.cn/book/detail?id=001", detail.id)
        assertEquals("默认书名", detail.title)
        assertEquals("佚名", detail.author)
        assertEquals("Thomas H. Cormen 等著", detail.principal)
        assertEquals("未知出版社", detail.publisher)
        assertEquals(1, detail.distributions.size)
        assertEquals("BAR-001", detail.distributions.single().id)
        assertEquals("BAR-001", detail.distributions.single().barcode)
        assertEquals("馆藏位置待补充", detail.distributions.single().location)
        assertEquals("索书号待补充", detail.distributions.single().callNumber)

        assertThrows(IllegalStateException::class.java) {
            mapCollectionDetailInfo(
                detailUrl = " ",
                dto = CollectionDetailDto(bookname = " "),
                fallbacks = fallbacks
            )
        }
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
