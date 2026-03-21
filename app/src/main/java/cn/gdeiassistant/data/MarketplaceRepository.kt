package cn.gdeiassistant.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import cn.gdeiassistant.R
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
import cn.gdeiassistant.network.api.MarketplaceProfileDto
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
                val detail = dto ?: throw IllegalStateException(context.getString(R.string.marketplace_detail_missing))
                val item = mapItem(
                    detail.secondhandItem ?: throw IllegalStateException(context.getString(R.string.marketplace_detail_missing))
                )
                val profile = detail.profile
                val images = detail.secondhandItem.pictureURL.orEmpty().filter { it.isNotBlank() }.ifEmpty {
                    listOfNotNull(safeApiCall { marketplaceApi.getItemPreview(id) }.getOrNull())
                }
                MarketplaceDetail(
                    item = item,
                    condition = currentProfileOptions().marketplaceTypeTitle(detail.secondhandItem.type),
                    description = detail.secondhandItem.description.orEmpty().ifBlank {
                        context.getString(R.string.marketplace_description_empty)
                    },
                    contactHint = buildContactHint(
                        qq = detail.secondhandItem.qq,
                        phone = detail.secondhandItem.phone
                    ).ifBlank { context.getString(R.string.marketplace_contact_private) },
                    sellerUsername = profile?.username ?: detail.secondhandItem.username,
                    sellerNickname = profile?.nickname?.trim()?.ifBlank { null },
                    sellerCollege = currentProfileOptions().facultyNameFor(profile?.faculty),
                    sellerMajor = profile?.major?.trim()?.ifBlank { null },
                    sellerGrade = profile?.enrollment?.let {
                        context.getString(R.string.marketplace_seller_grade_suffix, it)
                    },
                    imageUrls = images
                )
            }
    }

    suspend fun getProfileSummary(): Result<MarketplacePersonalSummary> = withContext(Dispatchers.IO) {
        runCatching {
            coroutineScope {
                val summaryDeferred = async { safeApiCall { marketplaceApi.getProfileSummary() } }
                val profileDeferred = async { profileRepository.getProfile() }
                val dto = summaryDeferred.await().getOrThrow()
                val profile = profileDeferred.await().getOrNull()
                val summary = dto ?: throw IllegalStateException(context.getString(R.string.marketplace_profile_empty))
                val header = context.toCommunityProfileHeader(profile)
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
        safeApiCall { marketplaceApi.getProfileSummary() }
            .mapCatching { dto ->
                val summary = dto ?: throw IllegalStateException(context.getString(R.string.marketplace_profile_empty))
                val item = sequenceOf(summary.doing, summary.sold, summary.off)
                    .flatMap { it.orEmpty().asSequence() }
                    .firstOrNull { it.id?.toString() == id }
                    ?: throw IllegalStateException(context.getString(R.string.marketplace_edit_missing))
                MarketplaceEditableItem(
                    id = (item.id ?: id.toLongOrNull() ?: System.nanoTime()).toString(),
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
            id = (dto.id ?: System.nanoTime()).toString(),
            title = dto.name.orEmpty().ifBlank { context.getString(R.string.marketplace_default_title) },
            price = dto.price?.toDoubleOrNull() ?: 0.0,
            summary = dto.description.orEmpty().ifBlank { context.getString(R.string.marketplace_default_summary) }.take(60),
            sellerName = dto.username.orEmpty().ifBlank { context.getString(R.string.marketplace_default_seller) },
            postedAt = dto.publishTime.orEmpty().ifBlank { context.getString(R.string.marketplace_default_posted_at) },
            location = dto.location.orEmpty().ifBlank { context.getString(R.string.marketplace_default_location) },
            state = MarketplaceItemState.fromRemote(dto.state),
            tags = listOf(currentProfileOptions().marketplaceTypeTitle(dto.type)),
            previewImageUrl = dto.pictureURL.orEmpty().firstOrNull { it.isNotBlank() }
        )
    }

    private fun buildContactHint(qq: String?, phone: String?): String {
        return listOfNotNull(
            qq?.trim()?.takeIf { it.isNotBlank() }?.let { context.getString(R.string.marketplace_contact_qq, it) },
            phone?.trim()?.takeIf { it.isNotBlank() }?.let { context.getString(R.string.marketplace_contact_phone, it) }
        ).joinToString(" / ")
    }

    private fun uriToPart(uri: Uri, index: Int): MultipartBody.Part {
        val mimeType = context.contentResolver.getType(uri)?.ifBlank { null } ?: "image/jpeg"
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalStateException(context.getString(R.string.marketplace_publish_read_failed))
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
