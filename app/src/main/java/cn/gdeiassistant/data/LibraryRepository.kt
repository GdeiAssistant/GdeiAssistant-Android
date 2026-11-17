package cn.gdeiassistant.data

import android.content.Context
import cn.gdeiassistant.R
import cn.gdeiassistant.model.CollectionBorrowItem
import cn.gdeiassistant.model.CollectionDetailInfo
import cn.gdeiassistant.model.CollectionDistributionItem
import cn.gdeiassistant.model.CollectionSearchItem
import cn.gdeiassistant.model.CollectionSearchPage
import cn.gdeiassistant.network.api.LibraryApi
import cn.gdeiassistant.network.api.CollectionBorrowDto
import cn.gdeiassistant.network.api.CollectionDetailDto
import cn.gdeiassistant.network.api.CollectionItemDto
import cn.gdeiassistant.network.api.LibraryRenewDto
import cn.gdeiassistant.network.safeApiCall
import cn.gdeiassistant.network.safeJsonResultCall
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val libraryApi: LibraryApi
) {

    suspend fun searchCollections(keyword: String, page: Int = 1): Result<CollectionSearchPage> = withContext(Dispatchers.IO) {
        safeApiCall {
            libraryApi.searchCollections(
                keyword = keyword,
                page = page.coerceAtLeast(1)
            )
        }.mapCatching { dto ->
            CollectionSearchPage(
                items = dto?.collectionList.orEmpty().mapNotNull {
                    mapCollectionSearchItem(
                        dto = it,
                        fallbackTitle = context.getString(R.string.library_collection_default_title),
                        fallbackAuthor = context.getString(R.string.library_collection_default_author),
                        fallbackPublisher = context.getString(R.string.library_collection_default_publisher)
                    )
                },
                sumPage = dto?.sumPage ?: 0
            )
        }
    }

    suspend fun getCollectionDetail(detailUrl: String): Result<CollectionDetailInfo> = withContext(Dispatchers.IO) {
        safeApiCall { libraryApi.getCollectionDetail(detailUrl = detailUrl) }
            .mapCatching { dto ->
                val item = dto ?: throw IllegalStateException(context.getString(R.string.library_collection_missing))
                mapCollectionDetailInfo(
                    detailUrl = detailUrl,
                    dto = item,
                    fallbacks = context.libraryTextFallbacks()
                )
            }
    }

    suspend fun getBorrowedBooks(password: String? = null): Result<List<CollectionBorrowItem>> = withContext(Dispatchers.IO) {
        safeApiCall { libraryApi.getBorrowedBooks(cn.gdeiassistant.network.api.PasswordDto(password)) }
            .mapCatching { items ->
                items.orEmpty().mapNotNull {
                    mapCollectionBorrowItem(
                        dto = it,
                        fallbackTitle = context.getString(R.string.library_collection_default_title),
                        fallbackAuthor = context.getString(R.string.library_collection_default_author),
                        fallbackDate = context.getString(R.string.library_collection_unknown_date)
                    )
                }
            }
    }

    suspend fun renewBook(book: CollectionBorrowItem, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        val payload = buildLibraryRenewRequest(
            book = book,
            password = password,
            missingRenewInfoMessage = context.getString(R.string.library_collection_missing_renew_info),
            passwordRequiredMessage = context.getString(R.string.library_borrow_password_label)
        ).getOrElse { error ->
            return@withContext Result.failure(error)
        }
        safeJsonResultCall {
            libraryApi.renew(payload)
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
    fallbackPublisher: String
): CollectionSearchItem? {
    val detailUrl = dto.detailURL?.trim()?.takeIf(String::isNotEmpty) ?: return null
    return CollectionSearchItem(
        id = detailUrl,
        title = dto.bookname.orEmpty().ifBlank { fallbackTitle },
        author = dto.author.orEmpty().ifBlank { fallbackAuthor },
        publisher = dto.publishingHouse.orEmpty().ifBlank { fallbackPublisher },
        detailUrl = detailUrl
    )
}

internal fun mapCollectionDetailInfo(
    detailUrl: String,
    dto: CollectionDetailDto,
    fallbacks: LibraryTextFallbacks
): CollectionDetailInfo {
    val detailId = detailUrl.trim().takeIf(String::isNotEmpty)
        ?: dto.bookname?.trim()?.takeIf(String::isNotEmpty)
        ?: throw IllegalStateException("Missing collection detail identifier")
    return CollectionDetailInfo(
        id = detailId,
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
        distributions = dto.collectionDistributionList.orEmpty().mapNotNull { distribution ->
            val barcode = distribution.barcode?.trim()?.takeIf(String::isNotEmpty) ?: return@mapNotNull null
            CollectionDistributionItem(
                id = barcode,
                location = distribution.location.orEmpty().ifBlank { fallbacks.defaultDistributionLocation },
                callNumber = distribution.callNumber.orEmpty().ifBlank { fallbacks.defaultDistributionCallNumber },
                barcode = barcode,
                state = distribution.state.orEmpty().ifBlank { fallbacks.defaultDistributionState }
            )
        }
    )
}

internal fun mapCollectionBorrowItem(
    dto: CollectionBorrowDto,
    fallbackTitle: String,
    fallbackAuthor: String,
    fallbackDate: String
): CollectionBorrowItem? {
    val remoteId = dto.id?.trim()?.takeIf(String::isNotEmpty)
        ?: dto.sn?.trim()?.takeIf(String::isNotEmpty)
        ?: return null
    return CollectionBorrowItem(
        id = remoteId,
        sn = dto.sn?.trim().orEmpty(),
        code = dto.code?.trim().orEmpty(),
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
    defaultTitle = getString(R.string.library_collection_default_title),
    defaultAuthor = getString(R.string.library_collection_default_author),
    defaultPublisher = getString(R.string.library_collection_default_publisher),
    defaultPrincipal = getString(R.string.library_collection_default_principal),
    defaultPrice = getString(R.string.library_collection_default_price),
    defaultDescription = getString(R.string.library_collection_default_description),
    defaultSubject = getString(R.string.library_collection_default_subject),
    defaultClassification = getString(R.string.library_collection_default_classification),
    defaultDistributionLocation = getString(R.string.library_collection_distribution_default_location),
    defaultDistributionCallNumber = getString(R.string.library_collection_distribution_default_call_number),
    defaultDistributionBarcode = getString(R.string.library_collection_distribution_default_barcode),
    defaultDistributionState = getString(R.string.library_collection_distribution_default_state),
    defaultDate = getString(R.string.library_collection_unknown_date)
)
