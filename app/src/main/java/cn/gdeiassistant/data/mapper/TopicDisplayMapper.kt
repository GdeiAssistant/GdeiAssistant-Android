package cn.gdeiassistant.data.mapper

import cn.gdeiassistant.R
import cn.gdeiassistant.data.text.DisplayTextResolver
import cn.gdeiassistant.model.TopicPost
import cn.gdeiassistant.model.TopicPostDetail
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Applies display-layer formatting to raw topic domain objects returned by
 * TopicRepository. Resolves localized fallback strings that were previously
 * baked into the repository layer.
 */
@Singleton
class TopicDisplayMapper @Inject constructor(
    private val textResolver: DisplayTextResolver
) {

    fun applyPostDefaults(post: TopicPost): TopicPost {
        return post.copy(
            topic = post.topic.ifBlank { textResolver.getString(R.string.topic_default_name) },
            contentPreview = post.contentPreview.ifBlank { textResolver.getString(R.string.topic_default_content) },
            authorName = post.authorName.ifBlank { textResolver.getString(R.string.topic_default_author) },
            publishedAt = post.publishedAt.ifBlank { textResolver.getString(R.string.common_just_now) }
        )
    }

    fun applyPostDefaults(posts: List<TopicPost>): List<TopicPost> {
        return posts.map(::applyPostDefaults)
    }

    fun applyDetailDefaults(detail: TopicPostDetail): TopicPostDetail {
        return detail.copy(
            post = applyPostDefaults(detail.post),
            content = detail.content.ifBlank { textResolver.getString(R.string.topic_default_content) }
        )
    }
}
