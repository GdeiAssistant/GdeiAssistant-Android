package cn.gdeiassistant.data

import android.content.Context
import cn.gdeiassistant.R
import cn.gdeiassistant.model.AnnouncementItem
import cn.gdeiassistant.model.Festival
import cn.gdeiassistant.network.api.AnnouncementApi
import cn.gdeiassistant.network.safeApiCall
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnnouncementRepository @Inject constructor(
    @ApplicationContext private val context: Context,
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
                ?: throw IllegalStateException(context.getString(R.string.notice_detail_not_found))
        }
    }

    suspend fun getFestival(): Result<Festival?> = withContext(Dispatchers.IO) {
        safeApiCall {
            announcementApi.getFestival()
        }.map { dto ->
            val name = dto?.name?.trim()
            if (name.isNullOrBlank()) {
                null
            } else {
                Festival(
                    name = name,
                    description = dto.description?.map { it.trim() }?.filter { it.isNotEmpty() }
                )
            }
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
