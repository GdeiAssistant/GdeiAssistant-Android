package cn.gdeiassistant.model

import androidx.compose.runtime.Immutable
import java.io.Serializable

/** 关系状态，与共享契约 relationship 枚举一致。 */
enum class SocialRelationship {
    SELF,
    NONE,
    FOLLOWING,
    FOLLOWED_BY,
    MUTUAL;

    companion object {
        fun fromRemote(raw: String?): SocialRelationship {
            return when (raw?.trim()?.uppercase()) {
                "SELF" -> SELF
                "FOLLOWING" -> FOLLOWING
                "FOLLOWED_BY" -> FOLLOWED_BY
                "MUTUAL" -> MUTUAL
                else -> NONE
            }
        }
    }
}

/** 私信接收策略；未知/缺失不得落到 ALL。 */
enum class DmPolicy {
    ALL,
    FOLLOWING,
    MUTUAL,
    NONE;

    companion object {
        const val DEFAULT_REMOTE = "MUTUAL"

        fun fromRemote(raw: String?): DmPolicy {
            return when (raw?.trim()?.uppercase()) {
                "ALL" -> ALL
                "FOLLOWING" -> FOLLOWING
                "NONE" -> NONE
                "MUTUAL" -> MUTUAL
                else -> MUTUAL
            }
        }
    }
}

enum class SocialRelationshipKind(val remoteValue: String) {
    FOLLOWING("following"),
    FOLLOWERS("followers"),
    FRIENDS("friends");
}

enum class ChatSendStatus {
    PENDING,
    SENT,
    FAILED
}

/** 消息类型；旧数据/缺省为 TEXT。 */
enum class ChatMessageType {
    TEXT,
    IMAGE;

    companion object {
        fun fromRemote(raw: String?): ChatMessageType {
            return when (raw?.trim()?.uppercase()) {
                "IMAGE" -> IMAGE
                else -> TEXT
            }
        }
    }
}

@Immutable
data class SocialUser(
    val id: String,
    val nickname: String,
    val avatarUrl: String? = null,
    val introduction: String? = null,
    val followingCount: Int = 0,
    val followerCount: Int = 0,
    val friendCount: Int = 0,
    val relationship: SocialRelationship = SocialRelationship.NONE,
    val blockedByMe: Boolean = false,
    val canMessage: Boolean = false,
    val messagePermissionReason: String? = null
) : Serializable

@Immutable
data class ChatImageMeta(
    val url: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val size: Long? = null,
    val contentType: String? = null
) : Serializable

@Immutable
data class ChatMessage(
    val id: String,
    val conversationId: String,
    val seq: String,
    val senderId: String,
    val clientMessageId: String,
    val content: String,
    val createdAt: String,
    val sendStatus: ChatSendStatus = ChatSendStatus.SENT,
    val type: ChatMessageType = ChatMessageType.TEXT,
    val image: ChatImageMeta? = null,
    /** 客户端 pending/failed 本地 JPEG 路径，不入服务端契约。 */
    val localImagePath: String? = null
) : Serializable

@Immutable
data class Conversation(
    val id: String,
    val peer: SocialUser,
    val lastMessage: ChatMessage? = null,
    val updatedAt: String,
    val unreadCount: Int = 0,
    val lastReadSeq: String = "0",
    val canSend: Boolean = false,
    val sendPermissionReason: String? = null,
    /** 缺省 false；未配置私信图桶时服务端关闭。 */
    val imageMessagingEnabled: Boolean = false
) : Serializable

@Immutable
data class SocialPage<T>(
    val items: List<T>,
    val nextCursor: String? = null,
    val hasMore: Boolean = false
) : Serializable

@Immutable
data class SocialPrivacySettings(
    val dmPolicy: DmPolicy = DmPolicy.MUTUAL
) : Serializable

@Immutable
data class SocialUnreadCount(
    val total: Int = 0
) : Serializable

@Immutable
data class ConversationReadState(
    val lastReadSeq: String,
    val unreadCount: Int
) : Serializable

@Immutable
data class BlockState(
    val blocked: Boolean
) : Serializable

sealed interface SocialRealtimeEvent {
    data class MessageCreated(
        val conversationId: String,
        val messageId: String,
        val seq: String
    ) : SocialRealtimeEvent

    data class ConversationRead(
        val conversationId: String,
        val readerId: String,
        val lastReadSeq: String
    ) : SocialRealtimeEvent

    data object SocialChanged : SocialRealtimeEvent

    data object Ready : SocialRealtimeEvent
}
