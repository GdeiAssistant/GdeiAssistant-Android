package cn.gdeiassistant.data

import android.content.Context
import cn.gdeiassistant.R
import cn.gdeiassistant.model.SecretComment
import cn.gdeiassistant.model.SecretDetail
import cn.gdeiassistant.model.SecretDraft
import cn.gdeiassistant.model.SecretDraftMode
import cn.gdeiassistant.model.SecretPost
import cn.gdeiassistant.model.SecretVoiceDraft
import cn.gdeiassistant.network.api.SecretApi
import cn.gdeiassistant.network.api.SecretCommentDto
import cn.gdeiassistant.network.api.SecretPostDto
import cn.gdeiassistant.network.safeApiCall
import cn.gdeiassistant.network.safeJsonResultCall
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecretRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val secretApi: SecretApi
) {

    suspend fun getPosts(): Result<List<SecretPost>> = withContext(Dispatchers.IO) {
        safeApiCall { secretApi.getPosts(start = 0, size = 20) }
            .mapCatching { items ->
                items.orEmpty()
                    .filter { (it.state ?: 0) != 2 }
                    .map(::mapPost)
                    .sortedByDescending { it.createdAt }
            }
    }

    suspend fun getMyPosts(): Result<List<SecretPost>> = withContext(Dispatchers.IO) {
        safeApiCall { secretApi.getMyPosts() }
            .mapCatching { items ->
                items.orEmpty()
                    .filter { (it.state ?: 0) != 2 }
                    .map(::mapPost)
                    .sortedByDescending { it.createdAt }
            }
    }

    suspend fun getDetail(id: String): Result<SecretDetail> = withContext(Dispatchers.IO) {
        runCatching {
            coroutineScope {
                val detailDeferred = async { safeApiCall { secretApi.getDetail(id) } }
                val commentsDeferred = async { safeApiCall { secretApi.getComments(id) } }
                val dto = detailDeferred.await().getOrThrow()
                    ?: error(context.getString(R.string.secret_detail_missing))
                val comments = commentsDeferred.await().getOrNull()
                    ?: dto.secretCommentList.orEmpty()
                SecretDetail(
                    post = mapPost(dto),
                    content = dto.content.orEmpty().ifBlank {
                        if (dto.type == 0) {
                            context.getString(R.string.secret_default_content_empty)
                        } else {
                            context.getString(R.string.secret_default_voice_hint)
                        }
                    },
                    comments = comments.map(::mapComment)
                )
            }
        }
    }

    suspend fun submitComment(id: String, content: String): Result<Unit> = withContext(Dispatchers.IO) {
        safeJsonResultCall { secretApi.submitComment(id = id, comment = content) }
    }

    suspend fun setLike(id: String, liked: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        safeJsonResultCall { secretApi.setLike(id = id, like = if (liked) 1 else 0) }
    }

    suspend fun publish(draft: SecretDraft): Result<Unit> = withContext(Dispatchers.IO) {
        safeJsonResultCall {
            secretApi.publish(
                theme = draft.themeId.toString().toPlainBody(),
                content = draft.content?.trim()?.takeIf(String::isNotBlank)?.toPlainBody(),
                type = (if (draft.mode == SecretDraftMode.TEXT) "0" else "1").toPlainBody(),
                timer = (if (draft.timerEnabled) "1" else "0").toPlainBody(),
                voice = draft.voice?.toVoicePart()
            )
        }
    }

    private fun mapPost(dto: SecretPostDto): SecretPost {
        val type = dto.type ?: 0
        val content = dto.content.orEmpty().ifBlank {
            dto.voiceURL.orEmpty().ifBlank { context.getString(R.string.secret_default_content_empty) }
        }
        return SecretPost(
            id = (dto.id ?: System.nanoTime()).toString(),
            username = dto.username.orEmpty().ifBlank { context.getString(R.string.secret_default_anonymous) },
            themeId = (dto.theme ?: 1).coerceIn(1, 12),
            title = if (type == 0) {
                content.take(18).ifBlank { context.getString(R.string.secret_text_badge) }
            } else {
                context.getString(R.string.secret_voice_badge)
            },
            summary = if (type == 0) content.take(48) else context.getString(R.string.secret_default_summary_voice),
            createdAt = buildCreatedAt(dto.publishTime.orEmpty(), dto.timer ?: 0),
            likeCount = dto.likeCount ?: 0,
            commentCount = dto.commentCount ?: 0,
            isLiked = (dto.liked ?: 0) == 1,
            type = type,
            timer = dto.timer ?: 0,
            state = dto.state ?: 0,
            voiceUrl = dto.voiceURL?.trim()?.ifBlank { null }
        )
    }

    private fun mapComment(dto: SecretCommentDto): SecretComment {
        return SecretComment(
            id = (dto.id ?: System.nanoTime()).toString(),
            authorName = dto.username.orEmpty().ifBlank { context.getString(R.string.secret_default_anonymous) },
            content = dto.comment.orEmpty().ifBlank { context.getString(R.string.secret_default_comment_empty) },
            createdAt = dto.publishTime.orEmpty().ifBlank { context.getString(R.string.common_just_now) },
            avatarTheme = (dto.avatarTheme ?: 1).coerceIn(1, 12)
        )
    }

    private fun buildCreatedAt(publishTime: String, timer: Int): String {
        val base = publishTime.ifBlank { context.getString(R.string.common_just_now) }
        return if (timer == 1) "$base · ${context.getString(R.string.secret_timer_hint)}" else base
    }

    private fun String.toPlainBody(): RequestBody = toRequestBody("text/plain".toMediaTypeOrNull())

    private fun SecretVoiceDraft.toVoicePart(): MultipartBody.Part {
        return MultipartBody.Part.createFormData(
            "voice",
            fileName.ifBlank { context.getString(R.string.secret_publish_default_voice_name) },
            bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        )
    }
}
