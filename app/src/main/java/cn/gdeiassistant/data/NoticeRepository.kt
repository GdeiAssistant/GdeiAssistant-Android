package cn.gdeiassistant.data

import cn.gdeiassistant.BuildConfig
import cn.gdeiassistant.model.ArticleDetailContent
import cn.gdeiassistant.network.api.NoticeApi
import cn.gdeiassistant.network.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class NoticeItem(
    val id: Int,
    val title: String,
    val date: String,
    val body: String = "",
    val detailUrl: String? = null,
    val isNew: Boolean = false
)

data class NoticeDetail(
    val id: Int,
    val title: String,
    val date: String,
    val body: String
)

@Singleton
class NoticeRepository @Inject constructor(
    private val noticeApi: NoticeApi
) {

    suspend fun getLatestNoticeList(limit: Int = 5): Result<List<NoticeItem>> = withContext(Dispatchers.IO) {
        val safeLimit = limit.coerceIn(1, 20)
        safeApiCall {
            noticeApi.queryNewsList(type = NEWS_TYPE_ADMIN, start = 0, size = safeLimit)
        }.fold(
            onSuccess = { news ->
                val list = news.orEmpty()
                    .mapIndexedNotNull { index, item ->
                        val title = item.title?.trim().orEmpty()
                        if (title.isBlank()) {
                            return@mapIndexedNotNull null
                        }
                        NoticeItem(
                            id = index + 1,
                            title = title,
                            date = item.publishDate?.trim().orEmpty().ifBlank { "—" },
                            body = extractBodyText(item.content, fallbackId = index + 1),
                            detailUrl = extractDetailUrl(item.content),
                            isNew = index == 0
                        )
                    }
                    .take(safeLimit)
                if (list.isNotEmpty()) {
                    Result.success(list)
                } else {
                    Result.failure(IllegalStateException("暂无公告数据"))
                }
            },
            onFailure = { Result.failure(it) }
        )
    }

    private fun extractDetailUrl(content: String?): String? {
        val raw = content?.trim()?.takeIf { it.isNotBlank() } ?: return null
        val href = HREF_REGEX.find(raw)?.groupValues?.getOrNull(1)
        val direct = URL_REGEX.find(raw)?.value
        return normalizeHttpUrl(href ?: direct)
    }

    private fun normalizeHttpUrl(raw: String?): String? {
        val trimmed = raw?.trim()?.takeIf { it.isNotBlank() } ?: return null
        return when {
            trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true) -> trimmed
            trimmed.startsWith("/") -> "${BuildConfig.BASE_URL.trimEnd('/')}$trimmed"
            else -> null
        }
    }

    private fun extractBodyText(content: String?, fallbackId: Int): String {
        val normalized = content.orEmpty()
            .replace(Regex("(?i)<br\\s*/?>"), "\n")
            .replace(Regex("(?i)</p>"), "\n\n")
            .replace(Regex("(?i)</div>"), "\n")
            .replace(Regex("<[^>]*>"), " ")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace(Regex("[ \\t\\x0B\\f\\r]+"), " ")
            .replace(Regex("\\n\\s+"), "\n")
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()

        return normalized
            .takeIf { it.isNotBlank() && it != "查看详情" && it != "查看原文" }
            ?: getNoticeDetail(fallbackId)?.body.orEmpty()
    }

    private fun localNoticeList(): List<NoticeItem> = listOf(
        NoticeItem(1, "关于2026年上半年节假日放假安排的通知", "02-28", isNew = true),
        NoticeItem(2, "教务处关于开展本学期期中教学检查的通知", "02-27", isNew = false),
        NoticeItem(3, "图书馆关于清明节期间开放时间调整的公告", "02-26", isNew = false),
        NoticeItem(4, "学生处关于2026届毕业生图像信息采集的通知", "02-25", isNew = false),
        NoticeItem(5, "后勤处关于校园一卡通系统升级维护的公告", "02-24", isNew = false)
    )

    fun getNoticeDetail(id: Int): NoticeDetail? {
        val item = localNoticeList().find { it.id == id } ?: return null
        val body = when (id) {
            1 -> "根据国务院办公厅通知精神，结合学校实际，现将2026年上半年节假日放假安排通知如下：\n\n一、清明节：4月4日至6日放假调休，共3天。4月7日（星期日）上班。\n\n二、劳动节：5月1日至5日放假调休，共5天。4月26日（星期日）、5月9日（星期六）上班。\n\n请各单位妥善安排好值班与安全保卫工作。"
            2 -> "为加强教学过程管理，教务处定于第8-9周开展本学期期中教学检查。检查内容包括：课堂教学秩序、教案与作业批改、实验实训开展情况等。各教学单位请于第7周前提交自查报告。"
            3 -> "清明节假期（4月4日-6日）图书馆开放时间调整：4月4日闭馆，4月5日-6日开放时间为 9:00-17:00。4月7日起恢复正常开放。"
            4 -> "2026届毕业生图像信息采集工作定于3月15日-16日进行，地点：教学楼A栋101。请各院系组织学生按时参加，携带身份证与学生证。"
            5 -> "校园一卡通系统将于2月28日 22:00-24:00 进行升级维护，期间食堂、门禁、图书馆等刷卡服务将暂停。请师生提前做好准备。"
            else -> "暂无详细内容。"
        }
        return NoticeDetail(id = item.id, title = item.title, date = item.date, body = body)
    }

    private companion object {
        private const val NEWS_TYPE_ADMIN = 4
        private val HREF_REGEX = Regex("href\\s*=\\s*['\"]([^'\"]+)['\"]", RegexOption.IGNORE_CASE)
        private val URL_REGEX = Regex("https?://[^\\s'\"<>]+", RegexOption.IGNORE_CASE)
    }
}

fun NoticeItem.toArticleDetailContent(): ArticleDetailContent {
    return ArticleDetailContent(
        title = title,
        body = body.ifBlank { "暂无详细内容" },
        date = date,
        source = "系统通知公告",
        externalUrl = detailUrl
    )
}
