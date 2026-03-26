package cn.gdeiassistant.network.mock

import cn.gdeiassistant.network.mock.MockUtils.itemIdFromPath
import cn.gdeiassistant.network.mock.MockUtils.localizedText
import cn.gdeiassistant.network.mock.MockUtils.pathValueAfter
import cn.gdeiassistant.network.mock.MockUtils.requestLocale
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

    private fun infoText(
        locale: String,
        simplifiedChinese: String,
        english: String,
        traditionalChinese: String = simplifiedChinese,
        japanese: String = english,
        korean: String = english
    ): String {
        return localizedText(
            locale = locale,
            simplifiedChinese = simplifiedChinese,
            traditionalChinese = traditionalChinese,
            english = english,
            japanese = japanese,
            korean = korean
        )
    }

    private fun mockAnnouncements(locale: String) = listOf(
        MockAnnouncementRecord(
            id = "1001",
            title = infoText(locale, "账号安全提醒", "Account Security Reminder", "帳號安全提醒", "アカウントセキュリティのお知らせ", "계정 보안 알림"),
            content = infoText(
                locale,
                "本周将对登录态与设备识别策略进行升级，请留意异常登录提醒。",
                "Login sessions and device recognition rules will be upgraded this week. Please watch for unusual sign-in alerts.",
                "本週將對登入狀態與裝置識別策略進行升級，請留意異常登入提醒。",
                "今週はログイン状態と端末認識の仕組みを更新します。異常なログイン通知にご注意ください。",
                "이번 주에는 로그인 상태와 기기 인식 정책이 업그레이드됩니다. 비정상 로그인 알림을 확인해 주세요."
            ),
            publishTime = "2026-03-10"
        ),
        MockAnnouncementRecord(
            id = "1002",
            title = infoText(locale, "校园服务更新", "Campus Service Update", "校園服務更新", "キャンパスサービス更新", "캠퍼스 서비스 업데이트"),
            content = infoText(
                locale,
                "图书借阅与校园卡模块已完成接口升级，部分页面体验已同步优化。",
                "Library borrowing and campus card modules have completed an API upgrade, and several pages have been improved as well.",
                "圖書借閱與校園卡模組已完成介面升級，部分頁面體驗亦同步優化。",
                "図書貸出とキャンパスカード機能の API 更新が完了し、一部ページの体験も改善されました。",
                "도서 대출과 캠퍼스 카드 모듈의 API 업그레이드가 완료되었고 일부 화면 경험도 함께 개선되었습니다."
            ),
            publishTime = "2026-03-08"
        ),
        MockAnnouncementRecord(
            id = "1003",
            title = infoText(locale, "系统维护通知", "System Maintenance Notice", "系統維護通知", "システムメンテナンスのお知らせ", "시스템 점검 안내"),
            content = infoText(
                locale,
                "今晚 23:00 至次日 01:00 进行例行维护，期间部分服务可能出现短暂波动。",
                "Routine maintenance is scheduled from 23:00 tonight to 01:00 tomorrow. Some services may be briefly unstable during that time.",
                "今晚 23:00 至翌日 01:00 將進行例行維護，期間部分服務可能短暫波動。",
                "今夜 23:00 から翌日 01:00 まで定期メンテナンスを行います。一部サービスが一時的に不安定になる場合があります。",
                "오늘 밤 23:00부터 다음 날 01:00까지 정기 점검이 진행됩니다. 이 시간 동안 일부 서비스가 일시적으로 불안정할 수 있습니다."
            ),
            publishTime = "2026-03-06"
        )
    )

    private fun mockInteractionRecords(locale: String) = mutableListOf(
        MockInteractionRecord("msg_001", "secret", "comment", infoText(locale, "你收到一条新的评论", "You received a new comment", "你收到一條新的評論", "新しいコメントが届きました", "새 댓글이 도착했습니다"), infoText(locale, "有同学回复了你发布的树洞内容。", "Someone replied to your secret post.", "有同學回覆了你發布的樹洞內容。", "あなたのツリーホール投稿に返信がありました。", "누군가 당신의 비밀 게시물에 답글을 남겼습니다."), "2026-03-10 09:20", false, "comment", "6102"),
        MockInteractionRecord("msg_002", "lostandfound", "like", infoText(locale, "失物招领条目收到点赞", "A lost-and-found post got a like", "失物招領條目收到讚", "落とし物投稿にいいねが付きました", "분실물 게시글에 좋아요가 달렸습니다"), infoText(locale, "你发布的失物招领信息获得了新的关注。", "Your lost-and-found post received new attention.", "你發布的失物招領資訊獲得了新的關注。", "あなたの落とし物投稿に新しい反応がありました。", "당신의 분실물 게시글에 새로운 관심이 생겼습니다."), "2026-03-09 18:45", false, "like", "5101"),
        MockInteractionRecord("msg_003", "secondhand", "comment", infoText(locale, "二手商品收到留言", "A second-hand item got a message", "二手商品收到留言", "中古商品にメッセージが届きました", "중고 상품에 메시지가 도착했습니다"), infoText(locale, "有人询问你发布商品的成色与自提时间。", "Someone asked about the condition of your item and the pickup time.", "有人詢問你發布商品的成色與自取時間。", "商品の状態と受け渡し時間について問い合わせがありました。", "상품 상태와 픽업 시간에 대한 문의가 왔습니다."), "2026-03-09 12:10", true, "comment", "4101"),
        MockInteractionRecord("msg_004", "topic", "like", infoText(locale, "话题动态更新", "Topic activity update", "話題動態更新", "トピック更新", "토픽 활동 업데이트"), infoText(locale, "你参与的话题有了新的点赞记录。", "A topic you joined received another like.", "你參與的話題有了新的按讚記錄。", "参加中のトピックに新しいいいねが付きました。", "참여 중인 토픽에 새로운 좋아요가 생겼습니다."), "2026-03-08 21:30", false, "like", "12102"),
        MockInteractionRecord("msg_005", "express", "comment", infoText(locale, "表白墙收到留言", "A confession post got a comment", "表白牆收到留言", "告白投稿にコメントが届きました", "고백 게시물에 댓글이 달렸습니다"), infoText(locale, "有人给你发布的表白留下了新的评论。", "Someone left a new comment on your confession post.", "有人給你發布的表白留下了新的評論。", "あなたの告白投稿に新しいコメントが付きました。", "당신의 고백 게시물에 새 댓글이 달렸습니다."), "2026-03-15 23:10", false, "comment", "10102"),
        MockInteractionRecord("msg_006", "delivery", "accept", infoText(locale, "全民快递有新进展", "Delivery order update", "全民快遞有新進展", "配送依頼に進展があります", "배달 주문에 새 진행 상황이 있습니다"), infoText(locale, "你发布的快递订单已经被接单，记得留意配送动态。", "Your delivery order has been accepted. Keep an eye on the delivery status.", "你發布的快遞訂單已被接單，記得留意配送動態。", "配送依頼が受注されました。進捗をご確認ください。", "당신의 배달 주문이 수락되었습니다. 배송 진행 상황을 확인해 주세요."), "2026-03-15 21:15", false, "trade", "13104"),
        MockInteractionRecord("msg_007", "photograph", "like", infoText(locale, "拍好校园收到点赞", "A campus photo got a like", "拍好校園收到讚", "キャンパス写真にいいねが付きました", "캠퍼스 사진에 좋아요가 달렸습니다"), infoText(locale, "你上传的校园照片刚刚获得了一次新的点赞。", "Your uploaded campus photo just received a new like.", "你上傳的校園照片剛剛獲得了一次新的按讚。", "アップロードしたキャンパス写真に新しいいいねが付きました。", "업로드한 캠퍼스 사진에 새 좋아요가 달렸습니다."), "2026-03-15 20:48", true, "like", "14102")
    )

    private fun mockNewsRecords(locale: String) = listOf(
        MockNewsRecord("news_1324_1", 1, infoText(locale, "学校高质量发展大会暨 2026 年工作会议召开", "University High-Quality Development Conference and 2026 Work Meeting Held", "學校高質量發展大會暨 2026 年工作會議召開", "大学の高品質発展大会および 2026 年度会議を開催", "대학 고품질 발전대회 및 2026 업무회의 개최"), "2026-03-03", infoText(locale, "学校召开高质量发展大会，系统总结上一阶段重点工作，并对 2026 年改革发展目标与重点任务作出部署。", "The university held a development conference to review recent priorities and outline reform goals and key tasks for 2026.", "學校召開高質量發展大會，系統總結上一階段重點工作，並對 2026 年改革發展目標與重點任務作出部署。", "大学は発展大会を開き、前段階の重点業務を整理したうえで 2026 年の改革目標と主要任務を示しました。", "대학은 발전대회를 열어 최근 핵심 업무를 정리하고 2026년 개혁 목표와 주요 과제를 발표했습니다."), "https://www.gdei.edu.cn/1324/1001.htm"),
        MockNewsRecord("news_1324_2", 1, infoText(locale, "学校启动春季学期教学巡查工作", "University Launches Spring Teaching Inspection", "學校啟動春季學期教學巡查工作", "春学期の授業巡回を開始", "대학, 봄학기 수업 점검 시작"), "2026-02-26", infoText(locale, "为进一步提升课堂教学质量，学校启动春季学期教学巡查，持续关注课堂秩序、课程建设与教学保障。", "To improve classroom quality, the university launched spring teaching inspections focusing on order, course development, and teaching support.", "為進一步提升課堂教學質量，學校啟動春季學期教學巡查，持續關注課堂秩序、課程建設與教學保障。", "授業の質向上に向け、大学は春学期の授業巡回を開始し、授業運営やカリキュラム、支援体制を確認します。", "수업 품질 향상을 위해 대학은 봄학기 수업 점검을 시작하고 수업 질서, 교과 운영, 지원 체계를 점검합니다."), "https://www.gdei.edu.cn/1324/1002.htm"),
        MockNewsRecord("news_1325_1", 2, infoText(locale, "管理学院召开教职工思想教育会议", "School of Management Holds Faculty Education Meeting", "管理學院召開教職工思想教育會議", "経営学院が教職員会議を開催", "경영대학, 교직원 교육회의 개최"), "2026-03-19", infoText(locale, "管理学院围绕师德师风建设、年度重点工作与学生成长支持召开专题会议，进一步明确本学期工作方向。", "The School of Management held a meeting on faculty conduct, annual priorities, and student support to clarify this semester's direction.", "管理學院圍繞師德師風建設、年度重點工作與學生成長支持召開專題會議，進一步明確本學期工作方向。", "経営学院は教職員の姿勢、年間重点業務、学生支援について会議を開き、今学期の方向性を整理しました。", "경영대학은 교직원 윤리, 연간 핵심 업무, 학생 지원을 주제로 회의를 열어 이번 학기 방향을 정리했습니다."), "https://www.gdei.edu.cn/1325/2001.htm"),
        MockNewsRecord("news_1325_2", 2, infoText(locale, "外国语学院举办专业建设专题研讨", "Foreign Languages School Hosts Program Development Seminar", "外國語學院舉辦專業建設專題研討", "外国語学院が専門教育セミナーを開催", "외국어대학, 전공 개발 세미나 개최"), "2026-03-12", infoText(locale, "外国语学院组织开展专业建设专题研讨，聚焦课程优化、实践教学与人才培养方案迭代。", "The Foreign Languages School hosted a seminar on curriculum optimization, practical teaching, and program design updates.", "外國語學院組織開展專業建設專題研討，聚焦課程優化、實踐教學與人才培養方案迭代。", "外国語学院は、カリキュラム改善や実践教育、人材育成計画の更新に関する研究会を開催しました。", "외국어대학은 교과 최적화, 실습 교육, 인재 양성 계획 개편을 주제로 세미나를 열었습니다."), "https://www.gdei.edu.cn/1325/2002.htm"),
        MockNewsRecord("news_1376_1", 3, infoText(locale, "关于网站群管理平台升级切换的通知", "Notice on Website Platform Upgrade", "關於網站群管理平台升級切換的通知", "Web 管理基盤アップグレードのお知らせ", "웹 플랫폼 업그레이드 안내"), "2026-01-22", infoText(locale, "因学校网站群管理平台升级切换，学校主页及部分二级网站访问将在指定时间窗口内短暂停止，请各单位提前做好工作安排。", "Due to an upgrade of the website management platform, the university homepage and some sub-sites will be unavailable for a short period. Please plan ahead.", "因學校網站群管理平台升級切換，學校主頁及部分二級網站訪問將在指定時間窗口內短暫停止，請各單位提前做好工作安排。", "Web 管理基盤の更新に伴い、大学トップページと一部サイトは指定時間中に一時停止します。事前の調整をお願いします。", "웹 관리 플랫폼 업그레이드로 인해 대학 홈페이지와 일부 사이트가 지정 시간에 잠시 중단됩니다. 미리 업무를 조정해 주세요."), "https://www.gdei.edu.cn/1376/3001.htm"),
        MockNewsRecord("news_1376_2", 3, infoText(locale, "关于清明节假期校园服务安排的通知", "Notice on Qingming Holiday Campus Services", "關於清明節假期校園服務安排的通知", "清明節休暇中の学内サービス案内", "청명절 연휴 캠퍼스 서비스 안내"), "2026-03-01", infoText(locale, "清明节假期期间，图书馆、食堂、校园巴士与安保值班安排将按节假日模式执行，请师生留意各部门发布的具体通知。", "During the Qingming holiday, the library, cafeterias, campus buses, and security shifts will operate on the holiday schedule. Please check detailed notices from each department.", "清明節假期期間，圖書館、食堂、校園巴士與安保值班安排將按節假日模式執行，請師生留意各部門發布的具體通知。", "清明節休暇中は図書館、食堂、キャンパスバス、警備体制が休日スケジュールになります。各部門の案内をご確認ください。", "청명절 연휴 기간에는 도서관, 식당, 캠퍼스 버스, 보안 근무가 휴일 일정으로 운영됩니다. 각 부서 공지를 확인해 주세요."), "https://www.gdei.edu.cn/1376/3002.htm"),
        MockNewsRecord("news_3981_1", 4, infoText(locale, "美术学院举办高层次科研项目申报学术讲座", "Art School Hosts Research Grant Lecture", "美術學院舉辦高層次科研項目申報學術講座", "美術学院が研究費申請講座を開催", "미술대학, 연구과제 신청 특강 개최"), "2026-03-09", infoText(locale, "讲座围绕高层次科研项目选题、申报书撰写和团队协作展开，帮助青年教师提升项目申报质量。", "The lecture covered topic selection, proposal writing, and teamwork for major research grants to help young faculty improve applications.", "講座圍繞高層次科研項目選題、申報書撰寫和團隊協作展開，幫助青年教師提升項目申報質量。", "講座では大型研究費のテーマ設定、申請書作成、チーム連携を扱い、若手教員の申請力向上を支援しました。", "특강은 대형 연구과제 주제 선정, 신청서 작성, 팀 협업을 다뤄 젊은 교원의 과제 신청 역량 향상을 도왔습니다."), "https://www.gdei.edu.cn/3981/4001.htm"),
        MockNewsRecord("news_3981_2", 4, infoText(locale, "教育学院开展人工智能赋能教学专题分享", "Education School Shares AI in Teaching Practices", "教育學院開展人工智能賦能教學專題分享", "教育学院が AI 活用授業の共有会を開催", "교육대학, AI 활용 수업 사례 공유"), "2026-02-24", infoText(locale, "专题分享聚焦生成式人工智能在教学设计、课堂反馈与学习评价中的应用场景与实践方法。", "The session focused on how generative AI can support lesson design, classroom feedback, and learning assessment.", "專題分享聚焦生成式人工智能在教學設計、課堂反饋與學習評價中的應用場景與實踐方法。", "共有会では、生成 AI を授業設計、授業フィードバック、学習評価にどう活用するかを紹介しました。", "공유 세션은 생성형 AI를 수업 설계, 수업 피드백, 학습 평가에 활용하는 방법에 초점을 맞췄습니다."), "https://www.gdei.edu.cn/3981/4002.htm")
    )

    fun mockAnnouncementPage(request: Request): String {
        val locale = request.requestLocale()
        val size = request.url.pathSegments.lastOrNull()
            ?.toIntOrNull()
            ?.coerceIn(1, 20)
            ?: 5
        val payload = mockAnnouncements(locale).take(size).map { announcement ->
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
        val locale = request.requestLocale()
        val announcement = mockAnnouncements(locale).firstOrNull { it.id == request.itemIdFromPath().orEmpty() }
            ?: return MockUtils.failureJson(infoText(locale, "查询的系统公告不存在", "Announcement not found", "查詢的系統公告不存在", "お知らせが見つかりません", "공지사항을 찾을 수 없습니다"))
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
        val locale = request.requestLocale()
        val start = request.pathValueAfter("start")
            ?.toIntOrNull()
            ?.coerceAtLeast(0)
            ?: 0
        val size = request.pathValueAfter("size")
            ?.toIntOrNull()
            ?.coerceIn(1, 50)
            ?: 10
        val payload = mockInteractionRecords(locale)
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
        MockUtils.successDataJson(mockInteractionRecords(request.requestLocale()).count { !it.isRead })

    fun mockMessageRead(request: Request): String {
        val locale = request.requestLocale()
        val messageId = request.itemIdFromPath().orEmpty()
        val updated = mockInteractionRecords(locale).map { record ->
            if (record.id == messageId) record.copy(isRead = true) else record
        }
        return MockUtils.successDataJson(
            data = updated.firstOrNull { it.id == messageId }?.let { linkedMapOf("id" to it.id, "isRead" to true) },
            message = infoText(locale, "已标记为已读", "Marked as read", "已標記為已讀", "既読にしました", "읽음 처리되었습니다")
        )
    }

    fun mockMessageReadAll(request: Request): String {
        val locale = request.requestLocale()
        return MockUtils.successJson(infoText(locale, "全部消息已标记为已读", "All messages marked as read", "全部消息已標記為已讀", "すべて既読にしました", "모든 메시지를 읽음 처리했습니다"))
    }

    fun mockNews(request: Request): String {
        val locale = request.requestLocale()
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
        val payload = mockNewsRecords(locale)
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
        val locale = request.requestLocale()
        val news = mockNewsRecords(locale).firstOrNull { it.id == request.itemIdFromPath().orEmpty() }
            ?: return MockUtils.failureJson(infoText(locale, "查询的新闻不存在", "News not found", "查詢的新聞不存在", "ニュースが見つかりません", "뉴스를 찾을 수 없습니다"))
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
