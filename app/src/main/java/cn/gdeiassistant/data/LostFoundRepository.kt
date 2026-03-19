package cn.gdeiassistant.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import cn.gdeiassistant.R
import cn.gdeiassistant.model.LostFoundDetail
import cn.gdeiassistant.model.LostFoundDraft
import cn.gdeiassistant.model.LostFoundEditableItem
import cn.gdeiassistant.model.LostFoundItem
import cn.gdeiassistant.model.LostFoundItemTypeOption
import cn.gdeiassistant.model.LostFoundItemState
import cn.gdeiassistant.model.LostFoundPersonalSummary
import cn.gdeiassistant.model.LostFoundType
import cn.gdeiassistant.model.LostFoundUpdateDraft
import cn.gdeiassistant.model.lostFoundItemTypeTitles
import cn.gdeiassistant.network.api.LostFoundApi
import cn.gdeiassistant.network.api.LostFoundItemDto
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
class LostFoundRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lostFoundApi: LostFoundApi
) {

    val itemTypeOptions: List<LostFoundItemTypeOption> = lostFoundItemTypeTitles.mapIndexed { index, title ->
        LostFoundItemTypeOption(id = index, title = title)
    }

    suspend fun getItems(): Result<List<LostFoundItem>> = withContext(Dispatchers.IO) {
        runCatching {
            coroutineScope {
                val lostDeferred = async { safeApiCall { lostFoundApi.getLostItems(0) } }
                val foundDeferred = async { safeApiCall { lostFoundApi.getFoundItems(0) } }
                val lost = lostDeferred.await().getOrThrow().orEmpty().map(::mapItem)
                val found = foundDeferred.await().getOrThrow().orEmpty().map(::mapItem)
                (lost + found)
                    .filter { it.state == LostFoundItemState.ACTIVE }
                    .sortedByDescending { it.createdAt }
            }
        }
    }

    suspend fun getItemDetail(id: String): Result<LostFoundDetail> = withContext(Dispatchers.IO) {
        safeApiCall { lostFoundApi.getItemDetail(id) }
            .mapCatching { dto ->
                val detail = dto ?: throw IllegalStateException(context.getString(R.string.lost_found_detail_missing))
                val itemDto = detail.item ?: throw IllegalStateException(context.getString(R.string.lost_found_detail_missing))
                val item = mapItem(itemDto)
                val images = itemDto.pictureURL.orEmpty().filter { it.isNotBlank() }.ifEmpty {
                    listOfNotNull(safeApiCall { lostFoundApi.getItemPreview(id) }.getOrNull())
                }
                LostFoundDetail(
                    item = item,
                    description = itemDto.description.orEmpty().ifBlank {
                        context.getString(R.string.lost_found_description_empty)
                    },
                    contactHint = buildContactHint(itemDto.qq, itemDto.wechat, itemDto.phone).ifBlank {
                        context.getString(R.string.lost_found_contact_private)
                    },
                    statusText = when (item.state) {
                        LostFoundItemState.ACTIVE -> context.getString(R.string.lost_found_status_active)
                        LostFoundItemState.RESOLVED -> context.getString(R.string.lost_found_status_resolved)
                        LostFoundItemState.SYSTEM_DELETED -> context.getString(R.string.lost_found_status_deleted)
                    },
                    ownerUsername = detail.profile?.username ?: itemDto.username,
                    ownerNickname = detail.profile?.nickname?.trim()?.ifBlank { null },
                    ownerAvatarUrl = detail.profile?.avatarURL?.trim()?.ifBlank { null },
                    imageUrls = images
                )
            }
    }

    suspend fun getProfileSummary(): Result<LostFoundPersonalSummary> = withContext(Dispatchers.IO) {
        safeApiCall { lostFoundApi.getProfileSummary() }
            .mapCatching { dto ->
                val summary = dto ?: throw IllegalStateException(context.getString(R.string.lost_found_profile_empty))
                LostFoundPersonalSummary(
                    nickname = context.getString(R.string.lost_found_profile_nickname),
                    introduction = context.getString(R.string.lost_found_profile_intro),
                    lost = summary.lost.orEmpty().map(::mapItem),
                    found = summary.found.orEmpty().map(::mapItem),
                    didFound = summary.didfound.orEmpty().map(::mapItem)
                )
            }
    }

    suspend fun publish(draft: LostFoundDraft, images: List<Uri>): Result<Unit> = withContext(Dispatchers.IO) {
        safeJsonResultCall {
            lostFoundApi.publish(
                name = draft.title.trim().toPlainBody(),
                description = draft.description.trim().toPlainBody(),
                location = draft.location.trim().toPlainBody(),
                itemType = draft.itemTypeId.toString().toPlainBody(),
                lostType = draft.type.remoteValue.toString().toPlainBody(),
                qq = draft.qq?.trim()?.takeIf { it.isNotBlank() }?.toPlainBody(),
                wechat = draft.wechat?.trim()?.takeIf { it.isNotBlank() }?.toPlainBody(),
                phone = draft.phone?.trim()?.takeIf { it.isNotBlank() }?.toPlainBody(),
                images = images.mapIndexed { index, uri -> uriToPart(uri = uri, index = index + 1) }
            )
        }
    }

    suspend fun getEditableItem(id: String): Result<LostFoundEditableItem> = withContext(Dispatchers.IO) {
        safeApiCall { lostFoundApi.getProfileSummary() }
            .mapCatching { dto ->
                val summary = dto ?: throw IllegalStateException(context.getString(R.string.lost_found_profile_empty))
                val item = sequenceOf(summary.lost, summary.found, summary.didfound)
                    .flatMap { it.orEmpty().asSequence() }
                    .firstOrNull { it.id?.toString() == id }
                    ?: throw IllegalStateException(context.getString(R.string.lost_found_edit_missing))
                LostFoundEditableItem(
                    id = (item.id ?: id.toLongOrNull() ?: System.nanoTime()).toString(),
                    title = item.name.orEmpty(),
                    type = LostFoundType.fromRemote(item.lostType),
                    itemTypeId = item.itemType ?: 0,
                    description = item.description.orEmpty(),
                    location = item.location.orEmpty(),
                    qq = item.qq?.trim()?.ifBlank { null },
                    wechat = item.wechat?.trim()?.ifBlank { null },
                    phone = item.phone?.trim()?.ifBlank { null },
                    imageUrls = item.pictureURL.orEmpty().filter { it.isNotBlank() }
                )
            }
    }

    suspend fun updateItem(id: String, draft: LostFoundUpdateDraft): Result<Unit> = withContext(Dispatchers.IO) {
        safeJsonResultCall {
            lostFoundApi.updateItem(
                id = id,
                name = draft.title.trim(),
                description = draft.description.trim(),
                location = draft.location.trim(),
                itemType = draft.itemTypeId,
                lostType = draft.type.remoteValue,
                qq = draft.qq?.trim()?.takeIf { it.isNotBlank() },
                wechat = draft.wechat?.trim()?.takeIf { it.isNotBlank() },
                phone = draft.phone?.trim()?.takeIf { it.isNotBlank() }
            )
        }
    }

    suspend fun markDidFound(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        safeJsonResultCall { lostFoundApi.markDidFound(id) }
    }

    private fun mapItem(dto: LostFoundItemDto): LostFoundItem {
        return LostFoundItem(
            id = (dto.id ?: System.nanoTime()).toString(),
            title = dto.name.orEmpty().ifBlank { context.getString(R.string.lost_found_default_title) },
            type = LostFoundType.fromRemote(dto.lostType),
            itemTypeId = dto.itemType ?: 0,
            summary = dto.description.orEmpty().ifBlank { context.getString(R.string.lost_found_default_summary) }.take(56),
            location = dto.location.orEmpty().ifBlank { context.getString(R.string.lost_found_default_location) },
            createdAt = dto.publishTime.orEmpty().ifBlank { context.getString(R.string.common_just_now) },
            state = LostFoundItemState.fromRemote(dto.state),
            previewImageUrl = dto.pictureURL.orEmpty().firstOrNull { it.isNotBlank() }
        )
    }

    private fun buildContactHint(qq: String?, wechat: String?, phone: String?): String {
        return listOfNotNull(
            qq?.trim()?.takeIf { it.isNotBlank() }?.let { context.getString(R.string.lost_found_contact_qq, it) },
            wechat?.trim()?.takeIf { it.isNotBlank() }?.let { context.getString(R.string.lost_found_contact_wechat, it) },
            phone?.trim()?.takeIf { it.isNotBlank() }?.let { context.getString(R.string.lost_found_contact_phone, it) }
        ).joinToString(" / ")
    }

    private fun uriToPart(uri: Uri, index: Int): MultipartBody.Part {
        val mimeType = context.contentResolver.getType(uri)?.ifBlank { null } ?: "image/jpeg"
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalStateException(context.getString(R.string.lost_found_publish_read_failed))
        val fileName = queryDisplayName(uri).ifBlank { "lost-found-${UUID.randomUUID()}.jpg" }
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
}
