package cn.gdeiassistant.data

import cn.gdeiassistant.model.InteractionMessage
import cn.gdeiassistant.network.api.MessageApi
import cn.gdeiassistant.network.safeApiCall
import cn.gdeiassistant.network.safeJsonResultCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessagesRepository @Inject constructor(
    private val messageApi: MessageApi
) {

    suspend fun getInteractionMessages(start: Int = 0, size: Int = 20): Result<List<InteractionMessage>> = withContext(Dispatchers.IO) {
        safeApiCall {
            messageApi.getInteractionMessages(start = start.coerceAtLeast(0), size = size.coerceAtLeast(1))
        }.mapCatching { items ->
            items.orEmpty().mapNotNull { dto ->
                val id = dto.id?.trim().orEmpty()
                val title = dto.title?.trim().orEmpty()
                val content = dto.content?.trim().orEmpty()
                if (id.isBlank() && title.isBlank() && content.isBlank()) {
                    null
                } else {
                    InteractionMessage(
                        id = id.ifBlank { "${title.hashCode()}-${content.hashCode()}" },
                        module = dto.module?.trim(),
                        type = dto.type?.trim(),
                        title = title.ifBlank { "互动消息" },
                        content = content.ifBlank { "你有一条新的互动消息" },
                        createdAt = dto.createdAt?.trim().orEmpty().ifBlank { "刚刚" },
                        isRead = dto.isRead == true,
                        targetType = dto.targetType?.trim(),
                        targetId = dto.targetId?.trim(),
                        targetSubId = dto.targetSubId?.trim()
                    )
                }
            }
        }
    }

    suspend fun getUnreadCount(): Result<Int> = withContext(Dispatchers.IO) {
        safeApiCall { messageApi.getUnreadCount() }
            .map { (it ?: 0).coerceAtLeast(0) }
    }

    suspend fun markMessageRead(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        safeJsonResultCall { messageApi.markMessageRead(id) }
    }

    suspend fun markAllRead(): Result<Unit> = withContext(Dispatchers.IO) {
        safeJsonResultCall { messageApi.markAllRead() }
    }
}

