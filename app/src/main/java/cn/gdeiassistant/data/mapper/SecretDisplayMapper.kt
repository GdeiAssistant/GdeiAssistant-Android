package cn.gdeiassistant.data.mapper

import cn.gdeiassistant.R
import cn.gdeiassistant.data.text.DisplayTextResolver
import cn.gdeiassistant.model.SecretComment
import cn.gdeiassistant.model.SecretDetail
import cn.gdeiassistant.model.SecretPost
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Applies display-layer formatting to raw secret domain objects returned by
 * SecretRepository. Resolves localized fallback strings that were previously
 * baked into the repository layer.
 */
@Singleton
class SecretDisplayMapper @Inject constructor(
    private val textResolver: DisplayTextResolver
) {

    fun applyPostDefaults(post: SecretPost): SecretPost {
        val content = post.summary.ifBlank { post.voiceUrl.orEmpty() }
        return post.copy(
            username = post.username.ifBlank { textResolver.getString(R.string.secret_default_anonymous) },
            title = if (post.title.isNotBlank()) {
                post.title
            } else if (post.type == 0) {
                content.take(18).ifBlank { textResolver.getString(R.string.secret_text_badge) }
            } else {
                textResolver.getString(R.string.secret_voice_badge)
            },
            summary = if (post.summary.isNotBlank()) {
                post.summary
            } else if (post.type == 0) {
                content.take(48)
            } else {
                textResolver.getString(R.string.secret_default_summary_voice)
            },
            createdAt = buildCreatedAt(post.createdAt, post.timer)
        )
    }

    fun applyPostDefaults(posts: List<SecretPost>): List<SecretPost> {
        return posts.map(::applyPostDefaults)
    }

    fun applyDetailDefaults(detail: SecretDetail): SecretDetail {
        return detail.copy(
            post = applyPostDefaults(detail.post),
            content = detail.content.ifBlank {
                if (detail.post.type == 0) {
                    textResolver.getString(R.string.secret_default_content_empty)
                } else {
                    textResolver.getString(R.string.secret_default_voice_hint)
                }
            },
            comments = applyCommentDefaults(detail.comments)
        )
    }

    fun applyCommentDefaults(comment: SecretComment): SecretComment {
        return comment.copy(
            authorName = comment.authorName.ifBlank { textResolver.getString(R.string.secret_default_anonymous) },
            content = comment.content.ifBlank { textResolver.getString(R.string.secret_default_comment_empty) },
            createdAt = comment.createdAt.ifBlank { textResolver.getString(R.string.common_just_now) }
        )
    }

    fun applyCommentDefaults(comments: List<SecretComment>): List<SecretComment> {
        return comments.map(::applyCommentDefaults)
    }

    private fun buildCreatedAt(publishTime: String, timer: Int): String {
        val base = publishTime.ifBlank { textResolver.getString(R.string.common_just_now) }
        return if (timer == 1) "$base · ${textResolver.getString(R.string.secret_timer_hint)}" else base
    }
}
