package cn.gdeiassistant.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import cn.gdeiassistant.R
import cn.gdeiassistant.model.TopicDraft
import cn.gdeiassistant.model.TopicPost
import cn.gdeiassistant.model.TopicPostDetail
import cn.gdeiassistant.network.api.TopicApi
import cn.gdeiassistant.network.api.TopicPostDto
import cn.gdeiassistant.network.safeApiCall
import cn.gdeiassistant.network.safeJsonResultCall
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TopicRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val topicApi: TopicApi
) {

    suspend fun getPosts(keyword: String? = null): Result<List<TopicPost>> = withContext(Dispatchers.IO) {
        val normalizedKeyword = keyword?.trim().orEmpty()
        val result = if (normalizedKeyword.isBlank()) {
            safeApiCall { topicApi.getPosts(start = 0, size = 20) }
        } else {
            safeApiCall { topicApi.searchPosts(keyword = normalizedKeyword, start = 0, size = 20) }
        }
        result
            .mapCatching { items ->
                items.orEmpty()
                    .map(::mapPost)
                    .sortedByDescending { it.publishedAt }
            }
    }

    suspend fun getMyPosts(): Result<List<TopicPost>> = withContext(Dispatchers.IO) {
        safeApiCall { topicApi.getMyPosts(start = 0, size = 50) }
            .mapCatching { items ->
                items.orEmpty()
                    .map(::mapPost)
                    .sortedByDescending { it.publishedAt }
            }
    }

    suspend fun getDetail(id: String): Result<TopicPostDetail> = withContext(Dispatchers.IO) {
        safeApiCall { topicApi.getDetail(id) }
            .mapCatching { dto ->
                val item = dto ?: error("Topic detail not found")
                val post = mapPost(item)
                val explicitImages = item.imageUrls.orEmpty().filter { it.isNotBlank() }
                val imageUrls = if (explicitImages.isNotEmpty()) {
                    explicitImages
                } else {
                    resolveImageUrls(id, post.imageCount)
                }
                TopicPostDetail(
                    post = post.copy(firstImageUrl = post.firstImageUrl ?: imageUrls.firstOrNull()),
                    content = item.content.orEmpty(),
                    imageUrls = if (imageUrls.isEmpty()) listOfNotNull(post.firstImageUrl) else imageUrls
                )
            }
    }

    suspend fun like(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        safeJsonResultCall { topicApi.like(id) }
    }

    suspend fun publish(draft: TopicDraft, images: List<Uri>): Result<Unit> = withContext(Dispatchers.IO) {
        safeJsonResultCall {
            topicApi.publish(
                topic = draft.topic.trim().toPlainBody(),
                content = draft.content.trim().toPlainBody(),
                images = images.mapIndexed { index, uri -> uriToPart(uri = uri, index = index + 1) }
            )
        }
    }

    private suspend fun resolveImageUrls(id: String, count: Int): List<String> {
        if (count <= 0) return emptyList()
        val result = mutableListOf<String>()
        for (index in 1..count) {
            val url = safeApiCall { topicApi.getImage(id = id, index = index) }.getOrNull()
            if (!url.isNullOrBlank()) {
                result += url
            }
        }
        return result
    }

    private fun mapPost(dto: TopicPostDto): TopicPost {
        val content = dto.content.orEmpty()
        val imageUrls = dto.imageUrls.orEmpty().filter { it.isNotBlank() }
        val firstImage = dto.firstImageUrl?.trim()?.ifBlank { null } ?: imageUrls.firstOrNull()
        val imageCount = maxOf(dto.count ?: 0, imageUrls.size, if (firstImage == null) 0 else 1)
        return TopicPost(
            id = dto.id?.toString() ?: System.nanoTime().toString(),
            topic = dto.topic.orEmpty(),
            contentPreview = content.take(64),
            authorName = dto.username.orEmpty(),
            publishedAt = dto.publishTime.orEmpty(),
            likeCount = dto.likeCount ?: 0,
            imageCount = imageCount,
            firstImageUrl = firstImage,
            isLiked = dto.liked == true
        )
    }

    private fun uriToPart(uri: Uri, index: Int): MultipartBody.Part {
        val mimeType = context.contentResolver.getType(uri)?.ifBlank { null } ?: "image/jpeg"
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalStateException(context.getString(R.string.topic_publish_read_failed))
        val fileName = queryDisplayName(uri).ifBlank { "topic-${UUID.randomUUID()}.jpg" }
        return MultipartBody.Part.createFormData(
            "images",
            fileName,
            bytes.toRequestBody(mimeType.toMediaTypeOrNull())
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

    private fun String.toPlainBody() = toRequestBody("text/plain".toMediaTypeOrNull())
}
