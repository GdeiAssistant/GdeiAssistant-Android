package cn.gdeiassistant.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.stringResource
import cn.gdeiassistant.R
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
data class Festival(
    val name: String?,
    val description: List<String>?
) : Serializable

@Composable
fun newsSourceLabel(type: Int): String {
    return when (type) {
        1 -> stringResource(R.string.news_source_important)
        2 -> stringResource(R.string.news_source_department)
        3 -> stringResource(R.string.news_source_announcement)
        4 -> stringResource(R.string.news_source_academic)
        else -> stringResource(R.string.news_source_default)
    }
}
