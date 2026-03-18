package cn.gdeiassistant.data

import android.content.Context
import cn.gdeiassistant.R
import cn.gdeiassistant.model.ExpressCommentItem
import cn.gdeiassistant.model.ExpressDraft
import cn.gdeiassistant.model.ExpressGender
import cn.gdeiassistant.model.ExpressPost
import cn.gdeiassistant.model.ExpressPostDetail
import cn.gdeiassistant.network.api.ExpressApi
import cn.gdeiassistant.network.api.ExpressCommentDto
import cn.gdeiassistant.network.api.ExpressPostDto
import cn.gdeiassistant.network.safeApiCall
import cn.gdeiassistant.network.safeJsonResultCall
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpressRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val expressApi: ExpressApi
) {

    suspend fun getPosts(keyword: String? = null): Result<List<ExpressPost>> = withContext(Dispatchers.IO) {
        val normalizedKeyword = keyword?.trim().orEmpty()
        val result = if (normalizedKeyword.isBlank()) {
            safeApiCall { expressApi.getPosts(start = 0, size = 20) }
        } else {
            safeApiCall { expressApi.searchPosts(keyword = normalizedKeyword, start = 0, size = 20) }
        }
        result
            .mapCatching { items ->
                items.orEmpty()
                    .map(::mapPost)
                    .sortedByDescending { it.publishTime }
            }
    }

    suspend fun getMyPosts(): Result<List<ExpressPost>> = withContext(Dispatchers.IO) {
        safeApiCall { expressApi.getMyPosts(start = 0, size = 50) }
            .mapCatching { items ->
                items.orEmpty()
                    .map(::mapPost)
                    .sortedByDescending { it.publishTime }
            }
    }

    suspend fun getDetail(id: String): Result<ExpressPostDetail> = withContext(Dispatchers.IO) {
        safeApiCall { expressApi.getDetail(id) }
            .mapCatching { dto ->
                val item = dto ?: error(context.getString(R.string.express_detail_missing))
                ExpressPostDetail(
                    post = mapPost(item),
                    realName = item.realname?.trim()?.ifBlank { null },
                    content = item.content.orEmpty().ifBlank { context.getString(R.string.express_default_content) }
                )
            }
    }

    suspend fun getComments(id: String): Result<List<ExpressCommentItem>> = withContext(Dispatchers.IO) {
        safeApiCall { expressApi.getComments(id) }
            .mapCatching { items ->
                items.orEmpty()
                    .map(::mapComment)
                    .sortedByDescending { it.publishTime }
            }
    }

    suspend fun submitComment(id: String, content: String): Result<Unit> = withContext(Dispatchers.IO) {
        safeJsonResultCall { expressApi.submitComment(id = id, comment = content) }
    }

    suspend fun like(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        safeJsonResultCall { expressApi.like(id) }
    }

    suspend fun guess(id: String, name: String): Result<Boolean> = withContext(Dispatchers.IO) {
        safeApiCall { expressApi.guess(id = id, name = name) }
            .mapCatching { it == true }
    }

    suspend fun publish(draft: ExpressDraft): Result<Unit> = withContext(Dispatchers.IO) {
        safeJsonResultCall {
            expressApi.publish(
                nickname = draft.nickname.trim(),
                realName = draft.realName?.trim()?.takeIf(String::isNotBlank),
                selfGender = draft.selfGender.remoteValue,
                targetName = draft.targetName.trim(),
                content = draft.content.trim(),
                personGender = draft.targetGender.remoteValue
            )
        }
    }

    private fun mapPost(dto: ExpressPostDto): ExpressPost {
        val preview = dto.content.orEmpty()
            .ifBlank { context.getString(R.string.express_default_content) }
            .take(72)
        return ExpressPost(
            id = dto.id?.toString() ?: System.nanoTime().toString(),
            nickname = dto.nickname.orEmpty()
                .ifBlank { dto.username.orEmpty().ifBlank { context.getString(R.string.express_default_author) } },
            targetName = dto.name.orEmpty().ifBlank { context.getString(R.string.express_default_target) },
            contentPreview = preview,
            publishTime = dto.publishTime.orEmpty().ifBlank { context.getString(R.string.common_just_now) },
            likeCount = dto.likeCount ?: 0,
            commentCount = dto.commentCount ?: 0,
            guessCount = dto.guessSum ?: 0,
            correctGuessCount = dto.guessCount ?: 0,
            isLiked = dto.liked == true,
            canGuess = dto.canGuess == true,
            selfGender = ExpressGender.fromRemote(dto.selfGender),
            targetGender = ExpressGender.fromRemote(dto.personGender)
        )
    }

    private fun mapComment(dto: ExpressCommentDto): ExpressCommentItem {
        return ExpressCommentItem(
            id = dto.id?.toString() ?: System.nanoTime().toString(),
            authorName = dto.nickname.orEmpty()
                .ifBlank { dto.username.orEmpty().ifBlank { context.getString(R.string.express_default_comment_author) } },
            content = dto.comment.orEmpty().ifBlank { context.getString(R.string.express_default_comment) },
            publishTime = dto.publishTime.orEmpty().ifBlank { context.getString(R.string.common_just_now) }
        )
    }
}
