package cn.gdeiassistant.network.mock

import cn.gdeiassistant.network.mock.MockUtils.itemIdFromPath
import cn.gdeiassistant.network.mock.MockUtils.pathValueAfter
import okhttp3.Request

/** Mock provider for announcements, news, and interaction messages. */
object MockInfoProvider {

    data class MockInteractionRecord(
        val id: String,
        val module: String,
        val type: String,
        val title: String,
        val content: String,
        val createdAt: String,
        val isRead: Boolean,
        val targetType: String = "",
        val targetId: String = "",
        val targetSubId: String = ""
    )

    data class MockAnnouncementRecord(
        val id: String,
        val title: String,
        val content: String,
        val publishTime: String
    )

    private val mockAnnouncements = listOf(
        MockAnnouncementRecord(
            id = "1001",
            title = "账号安全提醒",
            content = "本周将对登录态与设备识别策略进行升级，请留意异常登录提醒。",
            publishTime = "2026年03月10日"
        ),
        MockAnnouncementRecord(
            id = "1002",
            title = "校园服务更新",
            content = "图书借阅与校园卡模块已完成接口升级，部分页面体验已同步优化。",
            publishTime = "2026年03月08日"
        ),
        MockAnnouncementRecord(
            id = "1003",
            title = "系统维护通知",
            content = "今晚 23:00 至次日 01:00 进行例行维护，期间部分服务可能出现短暂波动。",
            publishTime = "2026年03月06日"
        )
    )

    private val mockInteractionRecords = mutableListOf(
        MockInteractionRecord("msg_001", "secret", "comment", "你收到一条新的评论", "有同学回复了你发布的树洞内容。", "2026-03-10 09:20", false, "comment", "6102"),
        MockInteractionRecord("msg_002", "lostandfound", "like", "失物招领条目收到点赞", "你发布的失物招领信息获得了新的关注。", "2026-03-09 18:45", false, "like", "5101"),
        MockInteractionRecord("msg_003", "secondhand", "comment", "二手商品收到留言", "有人询问你发布商品的成色与自提时间。", "2026-03-09 12:10", true, "comment", "4101"),
        MockInteractionRecord("msg_004", "topic", "like", "话题动态更新", "你参与的话题有了新的点赞记录。", "2026-03-08 21:30", false, "like", "12102"),
        MockInteractionRecord("msg_005", "express", "comment", "表白墙收到留言", "有人给你发布的表白留下了新的评论。", "2026-03-15 23:10", false, "comment", "10102"),
        MockInteractionRecord("msg_006", "delivery", "accept", "全民快递有新进展", "你发布的快递订单已经被接单，记得留意配送动态。", "2026-03-15 21:15", false, "trade", "13104"),
        MockInteractionRecord("msg_007", "photograph", "like", "拍好校园收到点赞", "你上传的校园照片刚刚获得了一次新的点赞。", "2026-03-15 20:48", true, "like", "14102")
    )

    fun mockAnnouncementPage(request: Request): String {
        val size = request.url.pathSegments.lastOrNull()
            ?.toIntOrNull()
            ?.coerceIn(1, 20)
            ?: 5
        val payload = mockAnnouncements.take(size).map { announcement ->
            linkedMapOf(
                "id" to announcement.id,
                "title" to announcement.title,
                "content" to announcement.content,
                "publishTime" to announcement.publishTime
            )
        }
        return MockUtils.successDataJson(payload)
    }

    fun mockInteractionMessages(request: Request): String {
        val start = request.pathValueAfter("start")
            ?.toIntOrNull()
            ?.coerceAtLeast(0)
            ?: 0
        val size = request.pathValueAfter("size")
            ?.toIntOrNull()
            ?.coerceIn(1, 50)
            ?: 10
        val payload = mockInteractionRecords
            .drop(start)
            .take(size)
            .map { record ->
                linkedMapOf(
                    "id" to record.id,
                    "module" to record.module,
                    "type" to record.type,
                    "title" to record.title,
                    "content" to record.content,
                    "createdAt" to record.createdAt,
                    "isRead" to record.isRead,
                    "targetType" to record.targetType,
                    "targetId" to record.targetId,
                    "targetSubId" to record.targetSubId
                )
            }
        return MockUtils.successDataJson(payload)
    }

    fun mockMessageUnread(request: Request): String =
        MockUtils.successDataJson(mockInteractionRecords.count { !it.isRead })

    fun mockMessageRead(request: Request): String {
        val messageId = request.itemIdFromPath().orEmpty()
        mockInteractionRecords.replaceAll { record ->
            if (record.id == messageId) {
                record.copy(isRead = true)
            } else {
                record
            }
        }
        return MockUtils.successJson("已标记为已读")
    }

    fun mockMessageReadAll(request: Request): String {
        mockInteractionRecords.replaceAll { it.copy(isRead = true) }
        return MockUtils.successJson("全部消息已标记为已读")
    }

    fun mockNews(request: Request): String {
        val size = request.url.pathSegments.lastOrNull()
            ?.toIntOrNull()
            ?.coerceIn(1, 20)
            ?: 5
        val payload = listOf(
            linkedMapOf(
                "id" to "n1",
                "title" to "关于2026年上半年节假日放假安排的通知",
                "publishDate" to "2026-02-28",
                "content" to "<p>根据国务院办公厅通知精神，结合学校实际，现将2026年上半年节假日放假安排通知如下。</p><p>清明节为4月4日至6日，劳动节为5月1日至5日，请各单位妥善安排值班与安全保卫工作。</p><a href='https://gdeiassistant.cn/notice/1'>查看原文</a>"
            ),
            linkedMapOf(
                "id" to "n2",
                "title" to "教务处关于开展本学期期中教学检查的通知",
                "publishDate" to "2026-02-27",
                "content" to "<p>为加强教学过程管理，教务处定于第8至9周开展本学期期中教学检查。</p><p>检查内容包括课堂秩序、教案与作业批改、实验实训开展情况，各教学单位需按时提交自查报告。</p><a href='https://gdeiassistant.cn/notice/2'>查看原文</a>"
            ),
            linkedMapOf(
                "id" to "n3",
                "title" to "图书馆关于清明节期间开放时间调整的公告",
                "publishDate" to "2026-02-26",
                "content" to "<p>清明节假期图书馆开放时间将临时调整。</p><p>4月4日闭馆，4月5日至6日开放时间为9:00至17:00，4月7日起恢复正常开放。</p><a href='https://gdeiassistant.cn/notice/3'>查看原文</a>"
            ),
            linkedMapOf(
                "id" to "n4",
                "title" to "学生处关于2026届毕业生图像信息采集的通知",
                "publishDate" to "2026-02-25",
                "content" to "<p>2026届毕业生图像信息采集工作定于3月15日至16日进行。</p><p>地点位于教学楼A栋101，请各院系组织学生按时参加并携带身份证与学生证。</p><a href='https://gdeiassistant.cn/notice/4'>查看原文</a>"
            ),
            linkedMapOf(
                "id" to "n5",
                "title" to "后勤处关于校园一卡通系统升级维护的公告",
                "publishDate" to "2026-02-24",
                "content" to "<p>校园一卡通系统将于2月28日22:00至24:00进行升级维护。</p><p>维护期间食堂、门禁、图书馆等刷卡服务将暂停，请师生提前做好准备。</p><a href='https://gdeiassistant.cn/notice/5'>查看原文</a>"
            )
        )
        return MockUtils.successDataJson(payload.take(size))
    }
}
