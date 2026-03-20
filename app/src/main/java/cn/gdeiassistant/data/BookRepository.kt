package cn.gdeiassistant.data

import android.content.Context
import cn.gdeiassistant.R
import cn.gdeiassistant.model.CollectionBorrowItem
import cn.gdeiassistant.model.CollectionDetailInfo
import cn.gdeiassistant.model.CollectionDistributionItem
import cn.gdeiassistant.model.CollectionSearchItem
import cn.gdeiassistant.model.CollectionSearchPage
import cn.gdeiassistant.network.api.BookApi
import cn.gdeiassistant.network.api.CollectionBorrowDto
import cn.gdeiassistant.network.api.CollectionDetailDto
import cn.gdeiassistant.network.api.CollectionItemDto
import cn.gdeiassistant.network.api.LibraryRenewDto
import cn.gdeiassistant.network.safeApiCall
import cn.gdeiassistant.network.safeJsonResultCall
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bookApi: BookApi
) {

    suspend fun searchCollections(keyword: String, page: Int = 1): Result<CollectionSearchPage> = withContext(Dispatchers.IO) {
        safeApiCall {
            bookApi.searchCollections(
                keyword = keyword,
                page = page.coerceAtLeast(1)
            )
        }.mapCatching { dto ->
            CollectionSearchPage(
                items = dto?.collectionList.orEmpty().map(::mapSearchItem),
                sumPage = dto?.sumPage ?: 0
            )
        }
    }

    suspend fun getCollectionDetail(detailUrl: String): Result<CollectionDetailInfo> = withContext(Dispatchers.IO) {
        safeApiCall { bookApi.getCollectionDetail(detailUrl = detailUrl) }
            .mapCatching { dto ->
                val item = dto ?: throw IllegalStateException(context.getString(R.string.book_collection_missing))
                mapCollectionDetail(item)
            }
    }

    suspend fun getBorrowedBooks(password: String? = null): Result<List<CollectionBorrowItem>> = withContext(Dispatchers.IO) {
        safeApiCall { bookApi.getBorrowedBooks(password = password?.takeIf(String::isNotBlank)) }
            .mapCatching { items ->
                items.orEmpty().map(::mapBorrowItem)
            }
    }

    suspend fun renewBook(book: CollectionBorrowItem, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (book.sn.isBlank() || book.code.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException(context.getString(R.string.book_collection_missing_renew_info)))
        }
        val normalizedPassword = password.trim()
        if (normalizedPassword.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException(context.getString(R.string.book_borrow_password_label)))
        }
        safeJsonResultCall {
            bookApi.renew(
                LibraryRenewDto(
                    sn = book.sn,
                    code = book.code,
                    password = normalizedPassword
                )
            )
        }
    }

    private fun mapSearchItem(dto: CollectionItemDto): CollectionSearchItem {
        val detailUrl = dto.detailURL.orEmpty().ifBlank { UUID.randomUUID().toString() }
        return CollectionSearchItem(
            id = detailUrl,
            title = dto.bookname.orEmpty().ifBlank { context.getString(R.string.book_collection_default_title) },
            author = dto.author.orEmpty().ifBlank { context.getString(R.string.book_collection_default_author) },
            publisher = dto.publishingHouse.orEmpty().ifBlank { context.getString(R.string.book_collection_default_publisher) },
            detailUrl = detailUrl
        )
    }

    private fun mapCollectionDetail(dto: CollectionDetailDto): CollectionDetailInfo {
        return CollectionDetailInfo(
            id = dto.bookname.orEmpty().ifBlank { UUID.randomUUID().toString() },
            title = dto.bookname.orEmpty().ifBlank { context.getString(R.string.book_collection_default_title) },
            author = dto.author.orEmpty().ifBlank { context.getString(R.string.book_collection_default_author) },
            principal = dto.principal.orEmpty()
                .ifBlank { dto.personalPrincipal.orEmpty() }
                .ifBlank { context.getString(R.string.book_collection_default_principal) },
            publisher = dto.publishingHouse.orEmpty().ifBlank { context.getString(R.string.book_collection_default_publisher) },
            price = dto.price.orEmpty().ifBlank { context.getString(R.string.book_collection_default_price) },
            physicalDescription = dto.physicalDescriptionArea.orEmpty()
                .ifBlank { context.getString(R.string.book_collection_default_description) },
            subjectTheme = dto.subjectTheme.orEmpty().ifBlank { context.getString(R.string.book_collection_default_subject) },
            classification = dto.chineseLibraryClassification.orEmpty()
                .ifBlank { context.getString(R.string.book_collection_default_classification) },
            distributions = dto.collectionDistributionList.orEmpty().map { distribution ->
                CollectionDistributionItem(
                    id = distribution.barcode.orEmpty().ifBlank { UUID.randomUUID().toString() },
                    location = distribution.location.orEmpty()
                        .ifBlank { context.getString(R.string.book_collection_distribution_default_location) },
                    callNumber = distribution.callNumber.orEmpty()
                        .ifBlank { context.getString(R.string.book_collection_distribution_default_call_number) },
                    barcode = distribution.barcode.orEmpty()
                        .ifBlank { context.getString(R.string.book_collection_distribution_default_barcode) },
                    state = distribution.state.orEmpty()
                        .ifBlank { context.getString(R.string.book_collection_distribution_default_state) }
                )
            }
        )
    }

    private fun mapBorrowItem(dto: CollectionBorrowDto): CollectionBorrowItem {
        return CollectionBorrowItem(
            id = dto.id.orEmpty().ifBlank { dto.sn.orEmpty().ifBlank { UUID.randomUUID().toString() } },
            sn = dto.sn.orEmpty(),
            code = dto.code.orEmpty(),
            title = dto.name.orEmpty().ifBlank { context.getString(R.string.book_collection_default_title) },
            author = dto.author.orEmpty().ifBlank { context.getString(R.string.book_collection_default_author) },
            borrowDate = dto.borrowDate.orEmpty().ifBlank { context.getString(R.string.book_collection_unknown_date) },
            returnDate = dto.returnDate.orEmpty().ifBlank { context.getString(R.string.book_collection_unknown_date) },
            renewCount = dto.renewTime ?: 0
        )
    }
}
