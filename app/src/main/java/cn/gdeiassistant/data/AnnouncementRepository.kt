package cn.gdeiassistant.data

import cn.gdeiassistant.model.AnnouncementItem
import cn.gdeiassistant.network.api.AnnouncementApi
import cn.gdeiassistant.network.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnnouncementRepository @Inject constructor(
    private val announcementApi: AnnouncementApi
) {

    suspend fun getAnnouncements(limit: Int = 5): Result<List<AnnouncementItem>> {
        return getAnnouncementPage(start = 0, size = limit)
    }

    suspend fun getAnnouncementPage(start: Int = 0, size: Int = 5): Result<List<AnnouncementItem>> = withContext(Dispatchers.IO) {
        val safeStart = start.coerceAtLeast(0)
        val safeSize = size.coerceIn(1, 20)
        safeApiCall {
            announcementApi.queryAnnouncementPage(start = safeStart, size = safeSize)
        }.mapCatching { items ->
            items.orEmpty().mapNotNull(::mapAnnouncementItem)
        }
    }

    suspend fun getAnnouncementDetail(id: String): Result<AnnouncementItem> = withContext(Dispatchers.IO) {
        safeApiCall {
            announcementApi.queryAnnouncementDetail(id = id)
        }.mapCatching { dto ->
            mapAnnouncementItem(dto)
                ?: throw IllegalStateException("未找到该公告")
        }
    }

    private fun mapAnnouncementItem(dto: cn.gdeiassistant.network.api.AnnouncementDto?): AnnouncementItem? {
        val title = dto?.title?.trim().orEmpty()
        if (title.isBlank()) {
            return null
        }
        return AnnouncementItem(
            id = dto?.id.orEmpty().ifBlank { title.hashCode().toString() },
            title = title,
            content = dto?.content.orEmpty().trim(),
            publishTime = dto?.publishTime.orEmpty().ifBlank { "—" }
        )
    }
}
