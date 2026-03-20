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

    data class MockNewsRecord(
        val id: String,
        val type: Int,
        val title: String,
        val publishDate: String,
        val content: String,
        val sourceUrl: String
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

    private val mockNewsRecords = listOf(
        MockNewsRecord("news_1324_1", 1, "学校高质量发展大会暨 2026 年工作会议召开", "2026-03-03", "学校召开高质量发展大会，系统总结上一阶段重点工作，并对 2026 年改革发展目标与重点任务作出部署。", "https://www.gdei.edu.cn/1324/1001.htm"),
        MockNewsRecord("news_1324_2", 1, "学校启动春季学期教学巡查工作", "2026-02-26", "为进一步提升课堂教学质量，学校启动春季学期教学巡查，持续关注课堂秩序、课程建设与教学保障。", "https://www.gdei.edu.cn/1324/1002.htm"),
        MockNewsRecord("news_1325_1", 2, "管理学院召开教职工思想教育会议", "2026-03-19", "管理学院围绕师德师风建设、年度重点工作与学生成长支持召开专题会议，进一步明确本学期工作方向。", "https://www.gdei.edu.cn/1325/2001.htm"),
        MockNewsRecord("news_1325_2", 2, "外国语学院举办专业建设专题研讨", "2026-03-12", "外国语学院组织开展专业建设专题研讨，聚焦课程优化、实践教学与人才培养方案迭代。", "https://www.gdei.edu.cn/1325/2002.htm"),
        MockNewsRecord("news_1376_1", 3, "关于网站群管理平台升级切换的通知", "2026-01-22", "因学校网站群管理平台升级切换，学校主页及部分二级网站访问将在指定时间窗口内短暂停止，请各单位提前做好工作安排。", "https://www.gdei.edu.cn/1376/3001.htm"),
        MockNewsRecord("news_1376_2", 3, "关于清明节假期校园服务安排的通知", "2026-03-01", "清明节假期期间，图书馆、食堂、校园巴士与安保值班安排将按节假日模式执行，请师生留意各部门发布的具体通知。", "https://www.gdei.edu.cn/1376/3002.htm"),
        MockNewsRecord("news_3981_1", 4, "美术学院举办高层次科研项目申报学术讲座", "2026-03-09", "讲座围绕高层次科研项目选题、申报书撰写和团队协作展开，帮助青年教师提升项目申报质量。", "https://www.gdei.edu.cn/3981/4001.htm"),
        MockNewsRecord("news_3981_2", 4, "教育学院开展人工智能赋能教学专题分享", "2026-02-24", "专题分享聚焦生成式人工智能在教学设计、课堂反馈与学习评价中的应用场景与实践方法。", "https://www.gdei.edu.cn/3981/4002.htm")
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

    fun mockAnnouncementDetail(request: Request): String {
        val announcement = mockAnnouncements.firstOrNull { it.id == request.itemIdFromPath().orEmpty() }
            ?: return MockUtils.failureJson("查询的系统公告不存在")
        return MockUtils.successDataJson(
            linkedMapOf(
                "id" to announcement.id,
                "title" to announcement.title,
                "content" to announcement.content,
                "publishTime" to announcement.publishTime
            )
        )
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
        val type = request.pathValueAfter("type")
            ?.toIntOrNull()
            ?: 1
        val start = request.pathValueAfter("start")
            ?.toIntOrNull()
            ?.coerceAtLeast(0)
            ?: 0
        val size = request.pathValueAfter("size")
            ?.toIntOrNull()
            ?.coerceIn(1, 20)
            ?: 5
        val payload = mockNewsRecords
            .filter { it.type == type }
            .drop(start)
            .take(size)
            .map { news ->
                linkedMapOf(
                    "id" to news.id,
                    "type" to news.type,
                    "title" to news.title,
                    "publishDate" to news.publishDate,
                    "content" to news.content,
                    "sourceUrl" to news.sourceUrl
                )
            }
        return MockUtils.successDataJson(payload)
    }

    fun mockNewsDetail(request: Request): String {
        val news = mockNewsRecords.firstOrNull { it.id == request.itemIdFromPath().orEmpty() }
            ?: return MockUtils.failureJson("查询的新闻通知不存在")
        return MockUtils.successDataJson(
            linkedMapOf(
                "id" to news.id,
                "type" to news.type,
                "title" to news.title,
                "publishDate" to news.publishDate,
                "content" to news.content,
                "sourceUrl" to news.sourceUrl
            )
        )
    }
}
