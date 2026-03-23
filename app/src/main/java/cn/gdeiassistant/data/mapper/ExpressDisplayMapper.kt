package cn.gdeiassistant.data.mapper

import cn.gdeiassistant.R
import cn.gdeiassistant.data.text.DisplayTextResolver
import cn.gdeiassistant.model.ExpressCommentItem
import cn.gdeiassistant.model.ExpressPost
import cn.gdeiassistant.model.ExpressPostDetail
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Applies display-layer formatting to raw express domain objects returned by
 * ExpressRepository. Resolves localized fallback strings that were previously
 * baked into the repository layer.
 */
@Singleton
class ExpressDisplayMapper @Inject constructor(
    private val textResolver: DisplayTextResolver
) {

    fun applyPostDefaults(post: ExpressPost): ExpressPost {
        return post.copy(
            nickname = post.nickname.ifBlank { textResolver.getString(R.string.express_default_author) },
            targetName = post.targetName.ifBlank { textResolver.getString(R.string.express_default_target) },
            contentPreview = post.contentPreview.ifBlank { textResolver.getString(R.string.express_default_content) },
            publishTime = post.publishTime.ifBlank { textResolver.getString(R.string.common_just_now) }
        )
    }

    fun applyPostDefaults(posts: List<ExpressPost>): List<ExpressPost> {
        return posts.map(::applyPostDefaults)
    }

    fun applyDetailDefaults(detail: ExpressPostDetail): ExpressPostDetail {
        return detail.copy(
            post = applyPostDefaults(detail.post),
            content = detail.content.ifBlank { textResolver.getString(R.string.express_default_content) }
        )
    }

    fun applyCommentDefaults(comment: ExpressCommentItem): ExpressCommentItem {
        return comment.copy(
            authorName = comment.authorName.ifBlank { textResolver.getString(R.string.express_default_comment_author) },
            content = comment.content.ifBlank { textResolver.getString(R.string.express_default_comment) },
            publishTime = comment.publishTime.ifBlank { textResolver.getString(R.string.common_just_now) }
        )
    }

    fun applyCommentDefaults(comments: List<ExpressCommentItem>): List<ExpressCommentItem> {
        return comments.map(::applyCommentDefaults)
    }
}
