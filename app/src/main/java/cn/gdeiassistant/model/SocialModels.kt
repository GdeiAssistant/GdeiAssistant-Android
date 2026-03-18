package cn.gdeiassistant.model

import androidx.compose.runtime.Immutable
import java.io.Serializable

@Immutable
data class SecretPost(
    val id: String,
    val username: String,
    val themeId: Int,
    val title: String,
    val summary: String,
    val createdAt: String,
    val likeCount: Int,
    val commentCount: Int,
    val isLiked: Boolean,
    val type: Int,
    val timer: Int,
    val state: Int,
    val voiceUrl: String? = null
) : Serializable {
    val isVoice: Boolean
        get() = type != 0
}

@Immutable
data class SecretComment(
    val id: String,
    val authorName: String,
    val content: String,
    val createdAt: String,
    val avatarTheme: Int
) : Serializable

@Immutable
data class SecretDetail(
    val post: SecretPost,
    val content: String,
    val comments: List<SecretComment>
) : Serializable

@Immutable
data class SecretDraft(
    val title: String,
    val content: String?,
    val themeId: Int,
    val timerEnabled: Boolean,
    val mode: SecretDraftMode,
    val voice: SecretVoiceDraft? = null
) : Serializable

@Immutable
enum class SecretDraftMode {
    TEXT,
    VOICE
}

@Immutable
data class SecretVoiceDraft(
    val fileName: String,
    val mimeType: String,
    val bytes: ByteArray
) : Serializable

@Immutable
enum class DatingPickStatus(val remoteValue: Int) {
    PENDING(0),
    ACCEPTED(1),
    REJECTED(-1);

    companion object {
        fun fromRemote(value: Int?): DatingPickStatus {
            return when (value) {
                1 -> ACCEPTED
                -1, 2 -> REJECTED
                else -> PENDING
            }
        }
    }
}

@Immutable
enum class DatingArea(val remoteValue: Int) {
    GIRL(0),
    BOY(1);

    companion object {
        fun fromRemote(value: Int?): DatingArea {
            return if (value == 1) BOY else GIRL
        }
    }
}

@Immutable
data class DatingReceivedPick(
    val id: String,
    val senderName: String,
    val content: String,
    val time: String,
    val status: DatingPickStatus,
    val avatarUrl: String? = null
) : Serializable

@Immutable
data class DatingSentPick(
    val id: String,
    val targetName: String,
    val content: String,
    val status: DatingPickStatus,
    val targetQq: String? = null,
    val targetWechat: String? = null,
    val targetAvatarUrl: String? = null
) : Serializable

@Immutable
data class DatingMyPost(
    val id: String,
    val name: String,
    val imageUrl: String? = null,
    val publishTime: String,
    val grade: String,
    val faculty: String,
    val hometown: String,
    val area: DatingArea,
    val state: Int
) : Serializable
