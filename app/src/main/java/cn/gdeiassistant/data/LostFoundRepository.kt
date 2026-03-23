package cn.gdeiassistant.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import cn.gdeiassistant.model.LostFoundDetail
import cn.gdeiassistant.model.LostFoundDraft
import cn.gdeiassistant.model.LostFoundEditableItem
import cn.gdeiassistant.model.LostFoundItem
import cn.gdeiassistant.model.LostFoundItemTypeOption
import cn.gdeiassistant.model.LostFoundItemState
import cn.gdeiassistant.model.LostFoundPersonalSummary
import cn.gdeiassistant.model.LostFoundType
import cn.gdeiassistant.model.LostFoundUpdateDraft
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
    private val lostFoundApi: LostFoundApi,
    private val profileRepository: ProfileRepository,
    private val profileOptionsRepository: ProfileOptionsRepository
) {

    fun currentItemTypeOptions(): List<LostFoundItemTypeOption> {
        return profileOptionsRepository.currentOptions().lostFoundItemTypeOptions()
    }

    suspend fun getItemTypeOptions(forceRefresh: Boolean = false): Result<List<LostFoundItemTypeOption>> {
        return profileOptionsRepository.getOptions(forceRefresh = forceRefresh)
            .map { it.lostFoundItemTypeOptions() }
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
                val detail = dto ?: throw IllegalStateException("Item detail not found")
                val itemDto = detail.item ?: throw IllegalStateException("Item detail not found")
                val item = mapItem(itemDto)
                val images = itemDto.pictureURL.orEmpty().filter { it.isNotBlank() }.ifEmpty {
                    listOfNotNull(safeApiCall { lostFoundApi.getItemPreview(id) }.getOrNull())
                }
                LostFoundDetail(
                    item = item,
                    description = itemDto.description.orEmpty(),
                    contactHint = "",
                    contactQQ = itemDto.qq?.trim()?.ifBlank { null },
                    contactWechat = itemDto.wechat?.trim()?.ifBlank { null },
                    contactPhone = itemDto.phone?.trim()?.ifBlank { null },
                    statusText = "",
                    ownerUsername = detail.profile?.username ?: itemDto.username,
                    ownerNickname = detail.profile?.nickname?.trim()?.ifBlank { null },
                    ownerAvatarUrl = detail.profile?.avatarURL?.trim()?.ifBlank { null },
                    imageUrls = images
                )
            }
    }

    suspend fun getProfileSummary(): Result<LostFoundPersonalSummary> = withContext(Dispatchers.IO) {
        runCatching {
            coroutineScope {
                val summaryDeferred = async { safeApiCall { lostFoundApi.getProfileSummary() } }
                val profileDeferred = async { profileRepository.getProfile() }
                val dto = summaryDeferred.await().getOrThrow()
                val profile = profileDeferred.await().getOrNull()
                val summary = dto ?: throw IllegalStateException("Profile summary not found")
                val header = buildCommunityProfileHeader(
                    profile = profile,
                    defaultDisplayName = "",
                    defaultHeadline = ""
                )
                LostFoundPersonalSummary(
                    nickname = header.displayName,
                    avatarUrl = header.avatarUrl,
                    introduction = header.headline,
                    lost = summary.lost.orEmpty().map(::mapItem),
                    found = summary.found.orEmpty().map(::mapItem),
                    didFound = summary.didfound.orEmpty().map(::mapItem)
                )
            }
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
                val summary = dto ?: throw IllegalStateException("Profile summary not found")
                val item = sequenceOf(summary.lost, summary.found, summary.didfound)
                    .flatMap { it.orEmpty().asSequence() }
                    .firstOrNull { it.id?.toString() == id }
                    ?: throw IllegalStateException("Editable item not found")
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
            title = dto.name.orEmpty(),
            type = LostFoundType.fromRemote(dto.lostType),
            itemTypeId = dto.itemType ?: 0,
            summary = dto.description.orEmpty().take(56),
            location = dto.location.orEmpty(),
            createdAt = dto.publishTime.orEmpty(),
            state = LostFoundItemState.fromRemote(dto.state),
            previewImageUrl = dto.pictureURL.orEmpty().firstOrNull { it.isNotBlank() }
        )
    }

    private fun buildContactHint(qq: String?, wechat: String?, phone: String?): String {
        return listOfNotNull(
            qq?.trim()?.takeIf { it.isNotBlank() },
            wechat?.trim()?.takeIf { it.isNotBlank() },
            phone?.trim()?.takeIf { it.isNotBlank() }
        ).joinToString(" / ")
    }

    private fun uriToPart(uri: Uri, index: Int): MultipartBody.Part {
        val mimeType = context.contentResolver.getType(uri)?.ifBlank { null } ?: "image/jpeg"
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalStateException("Failed to read image")
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

    private fun currentProfileOptions() = profileOptionsRepository.currentOptions()
}
