package cn.gdeiassistant.data

import android.content.Context
import androidx.annotation.StringRes
import cn.gdeiassistant.R
import cn.gdeiassistant.model.SchoolNews
import cn.gdeiassistant.network.api.NoticeApi
import cn.gdeiassistant.network.api.NewsItemDto
import cn.gdeiassistant.network.safeApiCall
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class NewsCategory(
    val type: Int,
    @StringRes val titleRes: Int
)

@Singleton
class NewsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val noticeApi: NoticeApi
) {

    val categories: List<NewsCategory> = listOf(
        NewsCategory(type = 1, titleRes = R.string.news_category_school_headlines),
        NewsCategory(type = 2, titleRes = R.string.news_category_college_notice),
        NewsCategory(type = 3, titleRes = R.string.news_category_campus_notice),
        NewsCategory(type = 4, titleRes = R.string.news_category_academic_update)
    )

    suspend fun getNewsList(type: Int = 1, start: Int = 0, size: Int = 20): Result<List<SchoolNews>> = withContext(Dispatchers.IO) {
        safeApiCall {
            noticeApi.queryNewsList(type = type, start = start.coerceAtLeast(0), size = size.coerceIn(1, 50))
        }.mapCatching { items ->
            items.orEmpty().mapNotNull { dto -> dto.toSchoolNews(fallbackType = type) }
        }
    }

    suspend fun getNewsDetail(id: String): Result<SchoolNews> = withContext(Dispatchers.IO) {
        safeApiCall {
            noticeApi.queryNewsDetail(id)
        }.mapCatching { dto ->
            dto?.toSchoolNews()
                ?: throw IllegalStateException(context.getString(R.string.news_detail_not_found))
        }
    }

    suspend fun getLatestNews(size: Int = 5): Result<List<SchoolNews>> = withContext(Dispatchers.IO) {
        val perCategorySize = size.coerceIn(1, 20)
        val results = coroutineScope {
            categories.map { category ->
                async {
                    getNewsList(type = category.type, start = 0, size = perCategorySize)
                }
            }.awaitAll()
        }

        val merged = results
            .flatMap { it.getOrNull().orEmpty() }
            .sortedByDescending { it.publishDate }
            .distinctBy { it.id }
            .take(size.coerceAtLeast(1))

        when {
            merged.isNotEmpty() -> Result.success(merged)
            results.any { it.isFailure } -> {
                Result.failure(results.firstNotNullOf { it.exceptionOrNull() })
            }
            else -> Result.success(emptyList())
        }
    }

    private fun plainTextFromHtml(raw: String): String {
        return raw
            .replace(Regex("<[^>]*>"), " ")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun extractHttpUrl(content: String): String? {
        return Regex("https?://[^\\s'\"<>]+", RegexOption.IGNORE_CASE)
            .find(content)
            ?.value
    }

    private fun NewsItemDto.toSchoolNews(fallbackType: Int = 1): SchoolNews? {
        val title = title?.trim().orEmpty()
        if (title.isBlank()) {
            return null
        }
        val normalizedContent = plainTextFromHtml(content.orEmpty().trim())
        return SchoolNews(
            id = id.orEmpty().ifBlank { "${fallbackType}_${title.hashCode()}" },
            type = type ?: fallbackType,
            title = title,
            publishDate = publishDate.orEmpty().ifBlank { "—" },
            content = normalizedContent,
            link = sourceUrl?.trim()?.takeIf(String::isNotEmpty) ?: extractHttpUrl(normalizedContent)
        )
    }
}
