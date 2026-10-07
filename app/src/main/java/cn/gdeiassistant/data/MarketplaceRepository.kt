package cn.gdeiassistant.data

import cn.gdeiassistant.network.requireRemoteId
import cn.gdeiassistant.network.cancellableRunCatching
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import cn.gdeiassistant.model.MarketplaceDetail
import cn.gdeiassistant.model.MarketplaceDraft
import cn.gdeiassistant.model.MarketplaceEditableItem
import cn.gdeiassistant.model.MarketplaceItem
import cn.gdeiassistant.model.MarketplaceItemState
import cn.gdeiassistant.model.MarketplacePersonalSummary
import cn.gdeiassistant.model.MarketplaceTypeOption
import cn.gdeiassistant.model.MarketplaceUpdateDraft
import cn.gdeiassistant.network.api.MarketplaceApi
import cn.gdeiassistant.network.api.MarketplaceItemDto
import cn.gdeiassistant.network.safeApiCall
import cn.gdeiassistant.network.safeJsonResultCall
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarketplaceRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val marketplaceApi: MarketplaceApi,
    private val profileRepository: ProfileRepository,
    private val profileOptionsRepository: ProfileOptionsRepository
) {

    fun currentTypeOptions(): List<MarketplaceTypeOption> {
        return profileOptionsRepository.currentOptions().marketplaceTypeOptions()
    }

    suspend fun getTypeOptions(forceRefresh: Boolean = false): Result<List<MarketplaceTypeOption>> {
        return profileOptionsRepository.getOptions(forceRefresh = forceRefresh)
            .map { it.marketplaceTypeOptions() }
    }

    suspend fun getItems(typeId: Int? = null, keyword: String? = null): Result<List<MarketplaceItem>> = withContext(Dispatchers.IO) {
        val normalizedKeyword = keyword?.trim().orEmpty()
        val result = when {
            normalizedKeyword.isNotBlank() -> safeApiCall { marketplaceApi.searchItems(keyword = normalizedKeyword, start = 0) }
            typeId != null -> safeApiCall { marketplaceApi.getItemsByType(type = typeId, start = 0) }
            else -> safeApiCall { marketplaceApi.getItems(start = 0) }
        }
        result.mapCatching { items ->
            items.orEmpty()
                .map(::mapItem)
                .filter { it.state == MarketplaceItemState.SELLING }
                .sortedByDescending { it.postedAt }
        }
    }

    suspend fun getItemDetail(id: String): Result<MarketplaceDetail> = withContext(Dispatchers.IO) {
        safeApiCall { marketplaceApi.getItemDetail(id) }
            .mapCatching { dto ->
                val detail = dto ?: throw IllegalStateException("Item detail not found")
                val item = mapItem(
                    detail.item ?: throw IllegalStateException("Item detail not found")
                )
                val profile = detail.profile
                val images = detail.item.pictureURL.orEmpty().filter { it.isNotBlank() }.ifEmpty {
                    listOfNotNull(safeApiCall { marketplaceApi.getItemPreview(id) }.getOrNull())
                }
                MarketplaceDetail(
                    item = item,
                    condition = currentProfileOptions().marketplaceTypeTitle(detail.item.type),
                    description = detail.item.description.orEmpty(),
                    contactHint = "",
                    contactQQ = detail.item.qq?.trim()?.ifBlank { null },
                    contactPhone = detail.item.phone?.trim()?.ifBlank { null },
                    sellerDisplayName = profile?.displayName ?: detail.item.displayName,
                    sellerNickname = profile?.nickname?.trim()?.ifBlank { null },
                    sellerAuthorId = detail.item.authorId?.trim()?.takeIf(String::isNotBlank),
                    sellerCollege = currentProfileOptions().facultyNameFor(profile?.faculty),
                    sellerMajor = profile?.major?.trim()?.ifBlank { null },
                    sellerEnrollment = profile?.enrollment,
                    imageUrls = images,
                    typeId = detail.item.type,
                    sellerFacultyCode = profile?.faculty
                )
            }
    }

    private suspend fun loadProfilePages(): Result<cn.gdeiassistant.network.api.MarketplacePersonalSummaryDto> = cancellableRunCatching {
        var page = safeApiCall { marketplaceApi.getProfileSummary(0) }.getOrThrow()
            ?: throw IllegalStateException("Profile summary not found")
        var all = page
        var previousStart = 0
        while (page.hasMore) {
            val start = page.nextStart ?: throw IllegalStateException("Missing next page")
            if (start <= previousStart) throw IllegalStateException("Non-advancing next page")
            previousStart = start
            page = safeApiCall { marketplaceApi.getProfileSummary(start) }.getOrThrow()
                ?: throw IllegalStateException("Profile summary not found")
            all = all.copy(
                doing = all.doing.orEmpty() + page.doing.orEmpty(),
                sold = all.sold.orEmpty() + page.sold.orEmpty(),
                off = all.off.orEmpty() + page.off.orEmpty()
            )
        }
        all
    }

    suspend fun getProfileSummary(): Result<MarketplacePersonalSummary> = withContext(Dispatchers.IO) {
        cancellableRunCatching {
            coroutineScope {
                val summaryDeferred = async { loadProfilePages() }
                val profileDeferred = async { profileRepository.getProfile() }
                val dto = summaryDeferred.await().getOrThrow()
                val profile = profileDeferred.await().getOrNull()
                val summary = dto
                val header = buildCommunityProfileHeader(
                    profile = profile,
                    defaultDisplayName = "",
                    defaultHeadline = ""
                )
                MarketplacePersonalSummary(
                    nickname = header.displayName,
                    avatarUrl = header.avatarUrl,
                    introduction = header.headline,
                    doing = summary.doing.orEmpty().map(::mapItem),
                    sold = summary.sold.orEmpty().map(::mapItem),
                    off = summary.off.orEmpty().map(::mapItem)
                )
            }
        }
    }

    suspend fun getEditableItem(id: String): Result<MarketplaceEditableItem> = withContext(Dispatchers.IO) {
        safeApiCall { marketplaceApi.getItemDetail(id) }
            .mapCatching { detail ->
                val item = detail?.item ?: throw IllegalStateException("Editable item not found")
                MarketplaceEditableItem(
                    id = requireRemoteId(item.id),
                    title = item.name.orEmpty(),
                    price = item.price?.toDoubleOrNull() ?: 0.0,
                    description = item.description.orEmpty(),
                    location = item.location.orEmpty(),
                    typeId = item.type ?: 0,
                    qq = item.qq.orEmpty(),
                    phone = item.phone?.trim()?.ifBlank { null },
                    imageUrls = item.pictureURL.orEmpty().filter { it.isNotBlank() }
                )
            }
    }

    suspend fun publish(draft: MarketplaceDraft, images: List<Uri>): Result<Unit> = withContext(Dispatchers.IO) {
        safeJsonResultCall {
            marketplaceApi.publish(
                name = draft.title.trim().toPlainBody(),
                description = draft.description.trim().toPlainBody(),
                price = draft.price.toString().toPlainBody(),
                location = draft.location.trim().toPlainBody(),
                type = draft.typeId.toString().toPlainBody(),
                qq = draft.qq.trim().toPlainBody(),
                phone = draft.phone?.trim()?.takeIf { it.isNotBlank() }?.toPlainBody(),
                images = images.mapIndexed { index, uri -> uriToPart(uri = uri, index = index + 1) }
            )
        }
    }

    suspend fun updateItem(id: String, draft: MarketplaceUpdateDraft): Result<Unit> = withContext(Dispatchers.IO) {
        safeJsonResultCall {
            marketplaceApi.updateItem(
                id = id,
                name = draft.title.trim(),
                description = draft.description.trim(),
                price = draft.price,
                location = draft.location.trim(),
                type = draft.typeId,
                qq = draft.qq.trim(),
                phone = draft.phone?.trim()?.takeIf { it.isNotBlank() }
            )
        }
    }

    suspend fun updateItemState(id: String, state: MarketplaceItemState): Result<Unit> = withContext(Dispatchers.IO) {
        safeJsonResultCall { marketplaceApi.updateItemState(id, state.remoteValue) }
    }

    private fun mapItem(dto: MarketplaceItemDto): MarketplaceItem {
        return MarketplaceItem(
            id = requireRemoteId(dto.id),
            title = dto.name.orEmpty(),
            price = dto.price?.toDoubleOrNull() ?: 0.0,
            summary = dto.description.orEmpty().take(60),
            sellerName = dto.displayName.orEmpty(),
            postedAt = dto.publishTime.orEmpty(),
            location = dto.location.orEmpty(),
            state = MarketplaceItemState.fromRemote(dto.state),
            tags = listOf(currentProfileOptions().marketplaceTypeTitle(dto.type)),
            previewImageUrl = dto.pictureURL.orEmpty().firstOrNull { it.isNotBlank() }
        )
    }

    private fun buildContactHint(qq: String?, phone: String?): String {
        return listOfNotNull(
            qq?.trim()?.takeIf { it.isNotBlank() },
            phone?.trim()?.takeIf { it.isNotBlank() }
        ).joinToString(" / ")
    }

    private fun uriToPart(uri: Uri, index: Int): MultipartBody.Part {
        val mimeType = context.contentResolver.getType(uri)?.ifBlank { null } ?: "image/jpeg"
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalStateException("Failed to read image")
        val fileName = queryDisplayName(uri).ifBlank { "marketplace-${UUID.randomUUID()}.jpg" }
        return MultipartBody.Part.createFormData(
            "image$index",
            fileName,
            bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        )
    }

    private fun queryDisplayName(uri: Uri): String {
        val cursor = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        cursor?.use {
            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && it.moveToFirst()) {
                return it.getString(index).orEmpty()
            }
        }
        return uri.lastPathSegment.orEmpty()
    }

    private fun String.toPlainBody() = toRequestBody("text/plain".toMediaTypeOrNull())

    private fun currentProfileOptions() = profileOptionsRepository.currentOptions()
}
