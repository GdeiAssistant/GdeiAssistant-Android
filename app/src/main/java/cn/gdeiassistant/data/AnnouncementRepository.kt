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

    suspend fun getAnnouncements(limit: Int = 5): Result<List<AnnouncementItem>> = withContext(Dispatchers.IO) {
        val safeLimit = limit.coerceIn(1, 20)
        safeApiCall {
            announcementApi.queryAnnouncementPage(start = 0, size = safeLimit)
        }.mapCatching { items ->
            items.orEmpty().mapNotNull { dto ->
                val title = dto.title?.trim().orEmpty()
                if (title.isBlank()) {
                    null
                } else {
                    AnnouncementItem(
                        id = dto.id.orEmpty().ifBlank { title.hashCode().toString() },
                        title = title,
                        content = dto.content.orEmpty().trim(),
                        publishTime = dto.publishTime.orEmpty().ifBlank { "—" }
                    )
                }
            }
        }
    }
}

