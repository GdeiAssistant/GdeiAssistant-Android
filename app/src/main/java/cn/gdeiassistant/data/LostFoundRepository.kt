package cn.gdeiassistant.data

import android.content.Context
import cn.gdeiassistant.R
import cn.gdeiassistant.model.LostFoundDetail
import cn.gdeiassistant.model.LostFoundItem
import cn.gdeiassistant.model.LostFoundItemState
import cn.gdeiassistant.model.LostFoundPersonalSummary
import cn.gdeiassistant.model.LostFoundType
import cn.gdeiassistant.network.api.LostFoundApi
import cn.gdeiassistant.network.api.LostFoundItemDto
import cn.gdeiassistant.network.safeApiCall
import cn.gdeiassistant.network.safeJsonResultCall
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LostFoundRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lostFoundApi: LostFoundApi
) {

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
}
