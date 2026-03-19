package cn.gdeiassistant.model

import androidx.compose.runtime.Immutable
import java.io.Serializable

@Immutable
data class SchoolNews(
    val id: String,
    val type: Int,
    val title: String,
    val publishDate: String,
    val content: String,
    val link: String? = null
) : Serializable

@Immutable
data class AnnouncementItem(
    val id: String,
    val title: String,
    val content: String,
    val publishTime: String
) : Serializable

@Immutable
data class InteractionMessage(
    val id: String,
    val module: String? = null,
    val type: String? = null,
    val title: String,
    val content: String,
    val createdAt: String,
    val isRead: Boolean = false,
    val targetType: String? = null,
    val targetId: String? = null,
    val targetSubId: String? = null
) : Serializable

@Immutable
data class UserProfileSummary(
    val username: String,
    val nickname: String? = null,
    val avatar: String? = null,
    val faculty: String? = null,
    val major: String? = null,
    val enrollment: String? = null,
    val location: String? = null,
    val hometown: String? = null,
    val introduction: String? = null,
    val birthday: String? = null,
    val ipArea: String? = null,
    val age: Int? = null
) : Serializable

@Immutable
enum class UserDataExportState(val code: Int) {
    NOT_EXPORTED(0),
    EXPORTING(1),
    EXPORTED(2);

    companion object {
        fun fromCode(code: Int?): UserDataExportState {
            return entries.firstOrNull { it.code == code } ?: NOT_EXPORTED
        }
    }
}

@Immutable
data class ArticleDetailContent(
    val title: String,
    val body: String,
    val date: String,
    val source: String,
    val externalUrl: String? = null
) : Serializable

fun SchoolNews.toArticleDetailContent(): ArticleDetailContent {
    return ArticleDetailContent(
        title = title,
        body = content.ifBlank { "暂无详细内容" },
        date = publishDate,
        source = newsSourceLabel(type),
        externalUrl = link
    )
}

fun AnnouncementItem.toArticleDetailContent(): ArticleDetailContent {
    return ArticleDetailContent(
        title = title,
        body = content.ifBlank { "暂无详细内容" },
        date = publishTime,
        source = "系统通知公告"
    )
}

fun newsSourceLabel(type: Int): String {
    return when (type) {
        1 -> "学校要闻"
        2 -> "院部通知"
        3 -> "通知公告"
        4 -> "学术动态"
        else -> "新闻通知"
    }
}
