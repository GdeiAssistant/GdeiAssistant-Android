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
                items = dto?.collectionList.orEmpty().map {
                    mapCollectionSearchItem(
                        dto = it,
                        fallbackTitle = context.getString(R.string.book_collection_default_title),
                        fallbackAuthor = context.getString(R.string.book_collection_default_author),
                        fallbackPublisher = context.getString(R.string.book_collection_default_publisher)
                    )
                },
                sumPage = dto?.sumPage ?: 0
            )
        }
    }

    suspend fun getCollectionDetail(detailUrl: String): Result<CollectionDetailInfo> = withContext(Dispatchers.IO) {
        safeApiCall { bookApi.getCollectionDetail(detailUrl = detailUrl) }
            .mapCatching { dto ->
                val item = dto ?: throw IllegalStateException(context.getString(R.string.book_collection_missing))
                mapCollectionDetailInfo(
                    dto = item,
                    fallbacks = context.libraryTextFallbacks()
                )
            }
    }

    suspend fun getBorrowedBooks(password: String? = null): Result<List<CollectionBorrowItem>> = withContext(Dispatchers.IO) {
        safeApiCall { bookApi.getBorrowedBooks(password = password?.takeIf(String::isNotBlank)) }
            .mapCatching { items ->
                items.orEmpty().map {
                    mapCollectionBorrowItem(
                        dto = it,
                        fallbackTitle = context.getString(R.string.book_collection_default_title),
                        fallbackAuthor = context.getString(R.string.book_collection_default_author),
                        fallbackDate = context.getString(R.string.book_collection_unknown_date)
                    )
                }
            }
    }

    suspend fun renewBook(book: CollectionBorrowItem, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        val payload = buildLibraryRenewRequest(
            book = book,
            password = password,
            missingRenewInfoMessage = context.getString(R.string.book_collection_missing_renew_info),
            passwordRequiredMessage = context.getString(R.string.book_borrow_password_label)
        ).getOrElse { error ->
            return@withContext Result.failure(error)
        }
        safeJsonResultCall {
            bookApi.renew(payload)
        }
    }
}

internal data class LibraryTextFallbacks(
    val defaultTitle: String,
    val defaultAuthor: String,
    val defaultPublisher: String,
    val defaultPrincipal: String,
    val defaultPrice: String,
    val defaultDescription: String,
    val defaultSubject: String,
    val defaultClassification: String,
    val defaultDistributionLocation: String,
    val defaultDistributionCallNumber: String,
    val defaultDistributionBarcode: String,
    val defaultDistributionState: String,
    val defaultDate: String
)

internal fun mapCollectionSearchItem(
    dto: CollectionItemDto,
    fallbackTitle: String,
    fallbackAuthor: String,
    fallbackPublisher: String,
    idFactory: () -> String = { UUID.randomUUID().toString() }
): CollectionSearchItem {
    val detailUrl = dto.detailURL.orEmpty().ifBlank(idFactory)
    return CollectionSearchItem(
        id = detailUrl,
        title = dto.bookname.orEmpty().ifBlank { fallbackTitle },
        author = dto.author.orEmpty().ifBlank { fallbackAuthor },
        publisher = dto.publishingHouse.orEmpty().ifBlank { fallbackPublisher },
        detailUrl = detailUrl
    )
}

internal fun mapCollectionDetailInfo(
    dto: CollectionDetailDto,
    fallbacks: LibraryTextFallbacks,
    idFactory: () -> String = { UUID.randomUUID().toString() }
): CollectionDetailInfo {
    return CollectionDetailInfo(
        id = dto.bookname.orEmpty().ifBlank(idFactory),
        title = dto.bookname.orEmpty().ifBlank { fallbacks.defaultTitle },
        author = dto.author.orEmpty().ifBlank { fallbacks.defaultAuthor },
        principal = dto.principal.orEmpty()
            .ifBlank { dto.personalPrincipal.orEmpty() }
            .ifBlank { fallbacks.defaultPrincipal },
        publisher = dto.publishingHouse.orEmpty().ifBlank { fallbacks.defaultPublisher },
        price = dto.price.orEmpty().ifBlank { fallbacks.defaultPrice },
        physicalDescription = dto.physicalDescriptionArea.orEmpty()
            .ifBlank { fallbacks.defaultDescription },
        subjectTheme = dto.subjectTheme.orEmpty().ifBlank { fallbacks.defaultSubject },
        classification = dto.chineseLibraryClassification.orEmpty()
            .ifBlank { fallbacks.defaultClassification },
        distributions = dto.collectionDistributionList.orEmpty().map { distribution ->
            CollectionDistributionItem(
                id = distribution.barcode.orEmpty().ifBlank(idFactory),
                location = distribution.location.orEmpty().ifBlank { fallbacks.defaultDistributionLocation },
                callNumber = distribution.callNumber.orEmpty().ifBlank { fallbacks.defaultDistributionCallNumber },
                barcode = distribution.barcode.orEmpty().ifBlank { fallbacks.defaultDistributionBarcode },
                state = distribution.state.orEmpty().ifBlank { fallbacks.defaultDistributionState }
            )
        }
    )
}

internal fun mapCollectionBorrowItem(
    dto: CollectionBorrowDto,
    fallbackTitle: String,
    fallbackAuthor: String,
    fallbackDate: String,
    idFactory: () -> String = { UUID.randomUUID().toString() }
): CollectionBorrowItem {
    return CollectionBorrowItem(
        id = dto.id.orEmpty().ifBlank { dto.sn.orEmpty().ifBlank(idFactory) },
        sn = dto.sn.orEmpty(),
        code = dto.code.orEmpty(),
        title = dto.name.orEmpty().ifBlank { fallbackTitle },
        author = dto.author.orEmpty().ifBlank { fallbackAuthor },
        borrowDate = dto.borrowDate.orEmpty().ifBlank { fallbackDate },
        returnDate = dto.returnDate.orEmpty().ifBlank { fallbackDate },
        renewCount = dto.renewTime ?: 0
    )
}

internal fun buildLibraryRenewRequest(
    book: CollectionBorrowItem,
    password: String,
    missingRenewInfoMessage: String,
    passwordRequiredMessage: String
): Result<LibraryRenewDto> {
    if (book.sn.isBlank() || book.code.isBlank()) {
        return Result.failure(IllegalArgumentException(missingRenewInfoMessage))
    }
    val normalizedPassword = password.trim()
    if (normalizedPassword.isEmpty()) {
        return Result.failure(IllegalArgumentException(passwordRequiredMessage))
    }
    return Result.success(
        LibraryRenewDto(
            sn = book.sn,
            code = book.code,
            password = normalizedPassword
        )
    )
}

private fun Context.libraryTextFallbacks() = LibraryTextFallbacks(
    defaultTitle = getString(R.string.book_collection_default_title),
    defaultAuthor = getString(R.string.book_collection_default_author),
    defaultPublisher = getString(R.string.book_collection_default_publisher),
    defaultPrincipal = getString(R.string.book_collection_default_principal),
    defaultPrice = getString(R.string.book_collection_default_price),
    defaultDescription = getString(R.string.book_collection_default_description),
    defaultSubject = getString(R.string.book_collection_default_subject),
    defaultClassification = getString(R.string.book_collection_default_classification),
    defaultDistributionLocation = getString(R.string.book_collection_distribution_default_location),
    defaultDistributionCallNumber = getString(R.string.book_collection_distribution_default_call_number),
    defaultDistributionBarcode = getString(R.string.book_collection_distribution_default_barcode),
    defaultDistributionState = getString(R.string.book_collection_distribution_default_state),
    defaultDate = getString(R.string.book_collection_unknown_date)
)
