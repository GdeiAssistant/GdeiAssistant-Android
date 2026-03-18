package cn.gdeiassistant.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import cn.gdeiassistant.R
import cn.gdeiassistant.model.PhotographCategory
import cn.gdeiassistant.model.PhotographCommentItem
import cn.gdeiassistant.model.PhotographPost
import cn.gdeiassistant.model.PhotographPostDetail
import cn.gdeiassistant.model.PhotographStats
import cn.gdeiassistant.network.api.PhotographApi
import cn.gdeiassistant.network.api.PhotographCommentDto
import cn.gdeiassistant.network.api.PhotographPostDto
import cn.gdeiassistant.network.safeApiCall
import cn.gdeiassistant.network.safeJsonResultCall
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotographRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val photographApi: PhotographApi
) {

    suspend fun getStats(): Result<PhotographStats> = withContext(Dispatchers.IO) {
        runCatching {
            coroutineScope {
                val photosDeferred = async { safeApiCall { photographApi.getPhotoCount() } }
                val commentsDeferred = async { safeApiCall { photographApi.getCommentCount() } }
                val likesDeferred = async { safeApiCall { photographApi.getLikeCount() } }
                PhotographStats(
                    photoCount = photosDeferred.await().getOrNull() ?: 0,
                    commentCount = commentsDeferred.await().getOrNull() ?: 0,
                    likeCount = likesDeferred.await().getOrNull() ?: 0
                )
            }
        }
    }

    suspend fun getPosts(category: PhotographCategory): Result<List<PhotographPost>> = withContext(Dispatchers.IO) {
        safeApiCall { photographApi.getPosts(type = category.rawValue, start = 0, size = 20) }
            .mapCatching { items ->
                items.orEmpty()
                    .map(::mapPost)
                    .sortedByDescending { it.createdAt }
            }
    }

    suspend fun getMyPosts(): Result<List<PhotographPost>> = withContext(Dispatchers.IO) {
        safeApiCall { photographApi.getMyPosts(start = 0, size = 50) }
            .mapCatching { items ->
                items.orEmpty()
                    .map(::mapPost)
                    .sortedByDescending { it.createdAt }
            }
    }

    suspend fun getDetail(postId: String): Result<PhotographPostDetail> = withContext(Dispatchers.IO) {
        safeApiCall { photographApi.getDetail(postId) }
            .mapCatching { dto ->
                val item = dto ?: throw IllegalStateException(context.getString(R.string.photograph_detail_missing))
                val detail = mapDetail(item)
                if (detail.imageUrls.isNotEmpty() || detail.post.photoCount <= 0) {
                    detail
                } else {
                    val resolvedImages = resolveImageUrls(postId = postId, count = detail.post.photoCount)
                    detail.copy(
                        post = detail.post.copy(firstImageUrl = detail.post.firstImageUrl ?: resolvedImages.firstOrNull()),
                        imageUrls = resolvedImages.ifEmpty { detail.imageUrls }
                    )
                }
            }
    }

    suspend fun getComments(postId: String): Result<List<PhotographCommentItem>> = withContext(Dispatchers.IO) {
        safeApiCall { photographApi.getComments(postId) }
            .mapCatching { items ->
                items.orEmpty()
                    .map(::mapComment)
                    .sortedByDescending { it.createdAt }
            }
    }

    suspend fun submitComment(postId: String, content: String): Result<Unit> = withContext(Dispatchers.IO) {
        safeJsonResultCall { photographApi.submitComment(id = postId, comment = content) }
    }

    suspend fun like(postId: String): Result<Unit> = withContext(Dispatchers.IO) {
        safeJsonResultCall { photographApi.like(postId) }
    }

    suspend fun publish(
        title: String,
        content: String,
        category: PhotographCategory,
        images: List<Uri>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        safeJsonResultCall {
            val parts = images.mapIndexed { index, uri -> uriToPart(uri = uri, index = index + 1) }
            photographApi.publish(
                title = title.toRequestBody(),
                content = content.toRequestBody(),
                count = images.size.toString().toRequestBody(),
                type = category.publishType.toString().toRequestBody(),
                images = parts
            )
        }
    }

    private suspend fun resolveImageUrls(postId: String, count: Int): List<String> {
        if (count <= 0) return emptyList()
        val urls = mutableListOf<String>()
        for (index in 1..count) {
            val url = safeApiCall { photographApi.getImage(id = postId, index = index) }.getOrNull()
            if (!url.isNullOrBlank()) {
                urls += url
            }
        }
        return urls
    }

    private fun mapPost(dto: PhotographPostDto): PhotographPost {
        val imageUrls = dto.imageUrls.orEmpty().filter { it.isNotBlank() }
        val firstImage = dto.firstImageUrl?.trim()?.ifBlank { null } ?: imageUrls.firstOrNull()
        val photoCount = maxOf(dto.count ?: 0, imageUrls.size, if (firstImage == null) 0 else 1)
        return PhotographPost(
            id = dto.id?.toString() ?: System.nanoTime().toString(),
            title = dto.title.orEmpty().ifBlank { context.getString(R.string.photograph_default_title) },
            contentPreview = dto.content.orEmpty().ifBlank {
                context.getString(R.string.photograph_default_content)
            }.take(60),
            authorName = dto.username.orEmpty().ifBlank { context.getString(R.string.photograph_default_author) },
            createdAt = dto.createTime.orEmpty().ifBlank { context.getString(R.string.common_just_now) },
            likeCount = dto.likeCount ?: 0,
            commentCount = dto.commentCount ?: 0,
            photoCount = photoCount,
            firstImageUrl = firstImage,
            isLiked = (dto.liked ?: 0) == 1,
            category = PhotographCategory.fromRemote(dto.type)
        )
    }

    private fun mapDetail(dto: PhotographPostDto): PhotographPostDetail {
        val post = mapPost(dto)
        val imageUrls = dto.imageUrls.orEmpty().filter { it.isNotBlank() }
        return PhotographPostDetail(
            post = post,
            content = dto.content.orEmpty().ifBlank { context.getString(R.string.photograph_default_content) },
            imageUrls = if (imageUrls.isEmpty()) listOfNotNull(post.firstImageUrl) else imageUrls,
            comments = dto.photographCommentList.orEmpty().map(::mapComment)
        )
    }

    private fun mapComment(dto: PhotographCommentDto): PhotographCommentItem {
        return PhotographCommentItem(
            id = dto.commentId?.toString() ?: System.nanoTime().toString(),
            photoId = dto.photoId?.toString(),
            authorName = dto.nickname.orEmpty()
                .ifBlank { dto.username.orEmpty().ifBlank { context.getString(R.string.photograph_default_comment_author) } },
            content = dto.comment.orEmpty().ifBlank { context.getString(R.string.photograph_default_comment) },
            createdAt = dto.createTime.orEmpty().ifBlank { context.getString(R.string.common_just_now) }
        )
    }

    private fun uriToPart(uri: Uri, index: Int): MultipartBody.Part {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri)?.ifBlank { null } ?: "image/jpeg"
        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalStateException(context.getString(R.string.photograph_publish_read_failed))
        val fileName = queryDisplayName(uri).ifBlank { "photograph-${UUID.randomUUID()}.jpg" }
        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(
            "image$index",
            fileName,
            requestBody
        )
    }

    private fun queryDisplayName(uri: Uri): String {
        val cursor = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        cursor?.use {
            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && it.moveToFirst()) {
                return it.getString(index).orEmpty()
            }
        }
        return uri.lastPathSegment.orEmpty()
    }

    private fun String.toRequestBody() = toRequestBody("text/plain".toMediaTypeOrNull())
}
