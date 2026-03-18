package cn.gdeiassistant.model

import androidx.compose.runtime.Immutable
import java.io.Serializable

@Immutable
enum class ExpressGender(val remoteValue: Int) {
    MALE(0),
    FEMALE(1),
    SECRET(2);

    companion object {
        fun fromRemote(value: Int?): ExpressGender {
            return when (value) {
                0 -> MALE
                1 -> FEMALE
                else -> SECRET
            }
        }
    }
}

@Immutable
data class ExpressPost(
    val id: String,
    val nickname: String,
    val targetName: String,
    val contentPreview: String,
    val publishTime: String,
    val likeCount: Int,
    val commentCount: Int,
    val guessCount: Int,
    val correctGuessCount: Int,
    val isLiked: Boolean,
    val canGuess: Boolean,
    val selfGender: ExpressGender,
    val targetGender: ExpressGender
) : Serializable

@Immutable
data class ExpressCommentItem(
    val id: String,
    val authorName: String,
    val content: String,
    val publishTime: String
) : Serializable

@Immutable
data class ExpressPostDetail(
    val post: ExpressPost,
    val realName: String? = null,
    val content: String
) : Serializable

@Immutable
data class TopicPost(
    val id: String,
    val topic: String,
    val contentPreview: String,
    val authorName: String,
    val publishedAt: String,
    val likeCount: Int,
    val imageCount: Int,
    val firstImageUrl: String? = null,
    val isLiked: Boolean
) : Serializable

@Immutable
data class TopicPostDetail(
    val post: TopicPost,
    val content: String,
    val imageUrls: List<String>
) : Serializable

@Immutable
data class ExpressDraft(
    val nickname: String,
    val realName: String?,
    val selfGender: ExpressGender,
    val targetName: String,
    val content: String,
    val targetGender: ExpressGender
) : Serializable

@Immutable
data class TopicDraft(
    val topic: String,
    val content: String
) : Serializable
