package cn.gdeiassistant.data

import androidx.annotation.StringRes
import cn.gdeiassistant.R
import cn.gdeiassistant.model.ArticleDetailContent
import cn.gdeiassistant.model.SchoolNews
import cn.gdeiassistant.model.newsSourceLabel
import cn.gdeiassistant.model.toArticleDetailContent
import cn.gdeiassistant.network.api.NoticeApi
import cn.gdeiassistant.network.safeApiCall
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
            items.orEmpty().mapNotNull { dto ->
                val title = dto.title?.trim().orEmpty()
                if (title.isBlank()) {
                    null
                } else {
                    val content = dto.content.orEmpty().trim()
                    SchoolNews(
                        id = dto.id.orEmpty().ifBlank { "${type}_${title.hashCode()}" },
                        type = dto.type ?: type,
                        title = title,
                        publishDate = dto.publishDate.orEmpty().ifBlank { "—" },
                        content = plainTextFromHtml(content),
                        link = extractHttpUrl(content)
                    )
                }
            }
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

    fun toDetailContent(news: SchoolNews): ArticleDetailContent {
        return news.toArticleDetailContent().copy(
            source = newsSourceLabel(news.type)
        )
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
}
