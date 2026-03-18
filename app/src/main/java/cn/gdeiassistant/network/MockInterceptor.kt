package cn.gdeiassistant.network

import android.util.Base64
import cn.gdeiassistant.data.ChinaRegionData
import cn.gdeiassistant.data.SettingsRepository
import cn.gdeiassistant.model.Charge
import cn.gdeiassistant.model.Cookie
import cn.gdeiassistant.model.ProfileFormSupport
import cn.gdeiassistant.util.ChargeCrypto
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.net.URLDecoder
import java.security.KeyPair

/** 全场景 Mock 拦截器。 */
class MockInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        if (!SettingsRepository.isMockModeEnabledSync()) {
            return chain.proceed(chain.request())
        }
        val request = chain.request()
        val path = request.url.encodedPath
        val json = when {
            path.contains("api/auth/login") -> mockLogin()
            path.contains("api/user/profile") -> mockUserProfile()
            path.contains("api/locationList") -> mockLocationList()
            path.contains("api/profile/avatar") && request.method == "GET" -> mockAvatarState()
            path.contains("api/profile/avatar") && request.method == "POST" -> mockUploadAvatar(request)
            path.contains("api/profile/avatar") && request.method == "DELETE" -> mockDeleteAvatar()
            path.contains("api/profile/nickname") -> mockUpdateNickname(request)
            path.contains("api/profile/birthday") -> mockUpdateBirthday(request)
            path.contains("api/profile/faculty") -> mockUpdateFaculty(request)
            path.contains("api/profile/major") -> mockUpdateMajor(request)
            path.contains("api/profile/enrollment") -> mockUpdateEnrollment(request)
            path.contains("api/profile/location") -> mockUpdateLocation(request)
            path.contains("api/profile/hometown") -> mockUpdateHometown(request)
            path.contains("api/introduction") -> mockUpdateIntroduction(request)
            path.contains("api/privacy") && request.method == "GET" -> mockGetPrivacySettings()
            path.contains("api/privacy") && request.method == "POST" -> mockUpdatePrivacySettings(request)
            path.contains("api/ip/start/") -> mockLoginRecords(request)
            path.contains("api/phone/attribution") -> mockPhoneAttributions()
            path.contains("api/phone/verification") -> mockSendPhoneVerification()
            path.contains("api/phone/unattach") -> mockUnbindPhone()
            path.contains("api/phone/attach") -> mockBindPhone(request)
            path.contains("api/email/verification") -> mockSendEmailVerification()
            path.contains("api/email/unbind") -> mockUnbindEmail()
            path.contains("api/email/bind") -> mockBindEmail(request)
            path.contains("api/feedback") -> mockSubmitFeedback()
            path.contains("api/close/submit") -> mockDeleteAccount(request)
            path.contains("api/phone/status") -> mockPhoneStatus()
            path.contains("api/email/status") -> mockEmailStatus()
            path.contains("api/userdata/state") -> mockUserDataExportState()
            path.contains("api/userdata/export") -> mockUserDataExport()
            path.contains("api/userdata/download") -> mockUserDataDownload()
            path.contains("api/spare/query") -> mockSpareRooms(request)
            path.contains("api/kaoyan/query") -> mockGraduateExam(request)
            path.contains("api/data/electricfees") -> mockElectricityFees(request)
            path.contains("api/data/yellowpage") -> mockYellowPages()
            path.contains("api/ershou/item/state/id/") -> mockMarketplaceStateUpdate(request)
            path.contains("api/ershou/item/id/") && path.endsWith("/preview") -> mockMarketplacePreview(request)
            path.contains("api/ershou/item/id/") -> mockMarketplaceDetail(request)
            path.contains("api/ershou/item/type/") -> mockMarketplaceItemsByType(request)
            path.contains("api/ershou/item/start/") -> mockMarketplaceItems()
            path.contains("api/ershou/profile") -> mockMarketplaceProfile()
            path.contains("api/lostandfound/item/id/") && path.endsWith("/didfound") -> mockLostFoundDidFound(request)
            path.contains("api/lostandfound/item/id/") && path.endsWith("/preview") -> mockLostFoundPreview(request)
            path.contains("api/lostandfound/item/id/") -> mockLostFoundDetail(request)
            path.contains("api/lostandfound/lostitem/start/") -> mockLostFoundItems(lostType = 0)
            path.contains("api/lostandfound/founditem/start/") -> mockLostFoundItems(lostType = 1)
            path.contains("api/lostandfound/profile") -> mockLostFoundProfile()
            path.contains("api/secret/id/") && path.endsWith("/comment") -> mockSecretCommentSubmit(request)
            path.contains("api/secret/id/") && path.endsWith("/like") -> mockSecretLikeUpdate(request)
            path.contains("api/secret/id/") && path.endsWith("/comments") -> mockSecretComments(request)
            path.contains("api/secret/id/") -> mockSecretDetail(request)
            path.contains("api/secret/info/start/") -> mockSecretPosts()
            path.contains("api/secret/profile") -> mockSecretMyPosts()
            path.contains("api/secret/info") && request.method == "POST" -> mockSecretPublish(request)
            path.contains("api/dating/pick/my/received") -> mockDatingReceivedPicks()
            path.contains("api/dating/pick/my/sent") -> mockDatingSentPicks()
            path.contains("api/dating/pick/id/") -> mockDatingPickStateUpdate(request)
            path.contains("api/dating/profile/id/") && path.endsWith("/state") -> mockDatingProfileHide(request)
            path.contains("api/dating/profile/my") -> mockDatingMyPosts()
            path.contains("api/express/id/") && path.endsWith("/comment") && request.method == "GET" -> mockExpressComments(request)
            path.contains("api/express/id/") && path.endsWith("/comment") -> mockExpressCommentSubmit(request)
            path.contains("api/express/id/") && path.endsWith("/like") -> mockExpressLike(request)
            path.contains("api/express/id/") && path.endsWith("/guess") -> mockExpressGuess(request)
            path.contains("api/express/id/") -> mockExpressDetail(request)
            path.contains("api/express/keyword/") -> mockExpressSearch(request)
            path.contains("api/express/profile/start/") -> mockExpressMyPosts()
            path.contains("api/express/start/") -> mockExpressPosts()
            path.contains("api/express") && request.method == "POST" -> mockExpressPublish(request)
            path.contains("api/topic/id/") && path.endsWith("/image") -> mockTopicImage(request)
            path.contains("api/topic/id/") && path.endsWith("/like") -> mockTopicLike(request)
            path.contains("api/topic/id/") -> mockTopicDetail(request)
            path.contains("api/topic/keyword/") -> mockTopicSearch(request)
            path.contains("api/topic/profile/start/") -> mockTopicMyPosts()
            path.contains("api/topic/start/") -> mockTopicPosts()
            path.contains("api/topic") && request.method == "POST" -> mockTopicPublish(request)
            path.contains("api/delivery/trade/id/") && path.endsWith("/finishtrade") -> mockDeliveryFinishTrade(request)
            path.contains("api/delivery/acceptorder") -> mockDeliveryAcceptOrder(request)
            path.contains("api/delivery/order/id/") && request.method == "DELETE" -> mockDeliveryDeleteOrder(request)
            path.contains("api/delivery/order/id/") -> mockDeliveryOrderDetail(request)
            path.contains("api/delivery/order/start/") -> mockDeliveryOrders(request)
            path.contains("api/delivery/mine") -> mockDeliveryMine()
            path.contains("api/delivery/order") && request.method == "POST" -> mockDeliveryPublish(request)
            path.contains("api/photograph/statistics/photos") -> mockPhotographPhotoCount()
            path.contains("api/photograph/statistics/comments") -> mockPhotographCommentCount()
            path.contains("api/photograph/statistics/likes") -> mockPhotographLikeCount()
            path.contains("api/photograph/id/") && path.endsWith("/comment") && request.method == "GET" -> mockPhotographComments(request)
            path.contains("api/photograph/id/") && path.endsWith("/comment") -> mockPhotographCommentSubmit(request)
            path.contains("api/photograph/id/") && path.endsWith("/like") -> mockPhotographLike(request)
            path.contains("api/photograph/id/") && path.endsWith("/image") -> mockPhotographImage(request)
            path.contains("api/photograph/id/") -> mockPhotographDetail(request)
            path.contains("api/photograph/profile/start/") -> mockPhotographMyPosts(request)
            path.contains("api/photograph/type/") -> mockPhotographPosts(request)
            path.contains("api/photograph") && request.method == "POST" -> mockPhotographPublish(request)
            path.contains("api/gradequery") || path.contains("api/grade/query") || path.contains("api/grade") -> mockGrade()
            path.contains("api/schedulequery") || path.contains("api/schedule/query") || path.contains("api/schedule") -> mockSchedule(request)
            path.contains("api/card/info") -> mockCardInfo()
            path.contains("api/card/query") -> mockCardQuery()
            path.contains("api/card/lost") -> mockCardLost()
            path.contains("api/bookquery") -> mockBookQuery()
            path.contains("api/bookrenew") -> mockBookRenew()
            path.contains("api/collection/search") -> mockCollectionSearch(request)
            path.contains("api/collection/detail") -> mockCollectionDetail(request)
            path.contains("api/collection/borrow") -> mockCollectionBorrow()
            path.contains("api/collection/renew") -> mockCollectionRenew()
            path.contains("api/cet/checkcode") -> mockCetCheckCode()
            path.contains("api/cet/query") -> mockCetQuery()
            path.contains("api/evaluate/submit") -> mockEvaluateSubmit()
            path.contains("api/announcement/start/") -> mockAnnouncementPage(request)
            path.contains("api/message/id/") && path.contains("/read") -> mockMessageRead(request)
            path.contains("api/message/readall") -> mockMessageReadAll()
            path.contains("api/message/unread") -> mockMessageUnread()
            path.contains("api/message/interaction/start/") -> mockInteractionMessages(request)
            path.contains("api/news/type/") -> mockNews(request)
            path.contains("api/encryption/rsa/publickey") -> mockServerPublicKey()
            path.contains("api/card/charge") -> mockCharge(request)
            path.contains("api/token/refresh") -> mockTokenRefresh()
            path.contains("api/token/expire") -> mockTokenExpire()
            path.contains("api/update/android") -> mockUpgrade()
            path.contains("api/upload/presignedUrl") -> mockPresignedUrl()
            path.contains("api/upload/mock") && request.method == "PUT" -> mockUploadFile()
            else -> return chain.proceed(request)
        }
        val mediaType = "application/json; charset=utf-8".toMediaType()
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(json.toResponseBody(mediaType))
            .build()
    }

    private fun mockLogin(): String = """
        {"success":true,"code":200,"message":"success","data":{
            "token":"mock_jwt_${System.currentTimeMillis()}"
        }}
    """.trimIndent()

    private fun mockGrade(): String = """
        {"success":true,"code":200,"message":"","data":{
            "year":0,
            "firstTermGPA":3.65,
            "secondTermGPA":3.72,
            "firstTermIGP":88.0,
            "secondTermIGP":90.0,
            "firstTermGradeList":[
                {"gradeYear":"2025-2026","gradeTerm":"第一学期","gradeId":"MATH001","gradeName":"高等数学","gradeCredit":"4","gradeType":"必修","gradeGpa":"3.0","gradeScore":"85"},
                {"gradeYear":"2025-2026","gradeTerm":"第一学期","gradeId":"ENG001","gradeName":"大学英语","gradeCredit":"3","gradeType":"必修","gradeGpa":"4.0","gradeScore":"92"},
                {"gradeYear":"2025-2026","gradeTerm":"第一学期","gradeId":"CS001","gradeName":"程序设计基础","gradeCredit":"4","gradeType":"必修","gradeGpa":"3.5","gradeScore":"88"}
            ],
            "secondTermGradeList":[
                {"gradeYear":"2025-2026","gradeTerm":"第二学期","gradeId":"MATH002","gradeName":"线性代数","gradeCredit":"3","gradeType":"必修","gradeGpa":"3.5","gradeScore":"87"},
                {"gradeYear":"2025-2026","gradeTerm":"第二学期","gradeId":"PHY001","gradeName":"大学物理","gradeCredit":"4","gradeType":"必修","gradeGpa":"3.0","gradeScore":"82"}
            ]
        }}
    """.trimIndent()

    private fun mockSchedule(request: Request): String {
        val week = request.url.queryParameter("week")
            ?.toIntOrNull()
            ?: request.formFields()["week"]?.toIntOrNull()
            ?: 1
        return """
        {"success":true,"code":200,"message":"","data":{
            "week":$week,
            "scheduleList":[
                {"id":"s1","scheduleLength":2,"scheduleName":"移动应用开发","scheduleType":"专业课","scheduleLesson":"1-2","scheduleTeacher":"张老师","scheduleLocation":"实验楼A301","colorCode":"#22C55E","position":0,"row":0,"column":0,"minScheduleWeek":1,"maxScheduleWeek":16,"scheduleWeek":"第1周至第16周"},
                {"id":"s2","scheduleLength":2,"scheduleName":"计算机网络","scheduleType":"专业课","scheduleLesson":"3-4","scheduleTeacher":"李老师","scheduleLocation":"教学楼B205","colorCode":"#3B82F6","position":16,"row":2,"column":2,"minScheduleWeek":1,"maxScheduleWeek":16,"scheduleWeek":"第1周至第16周"},
                {"id":"s3","scheduleLength":2,"scheduleName":"软件工程","scheduleType":"专业课","scheduleLesson":"5-6","scheduleTeacher":"王老师","scheduleLocation":"教学楼C102","colorCode":"#F97316","position":32,"row":4,"column":4,"minScheduleWeek":1,"maxScheduleWeek":16,"scheduleWeek":"第1周至第16周"},
                {"id":"s4","scheduleLength":3,"scheduleName":"大学英语","scheduleType":"公共课","scheduleLesson":"7-9","scheduleTeacher":"周老师","scheduleLocation":"教学楼A104","colorCode":"#8B5CF6","position":47,"row":6,"column":5,"minScheduleWeek":1,"maxScheduleWeek":18,"scheduleWeek":"第1周至第18周"}
            ]
        }}
    """.trimIndent()
    }

    private fun mockCardInfo(): String = """
        {"success":true,"code":200,"message":"","data":{
            "name":"Mock 用户",
            "number":"2024001001",
            "cardBalance":"52.50",
            "cardInterimBalance":"0.00",
            "cardNumber":"8888123456789012",
            "cardLostState":"0",
            "cardFreezeState":"0"
        }}
    """.trimIndent()

    private fun mockCardQuery(): String = """
        {"success":true,"code":200,"message":"","data":{
            "cardInfo":{"name":"Mock 用户","number":"2024001001","cardBalance":"52.50","cardInterimBalance":"0.00","cardNumber":"8888123456789012","cardLostState":"0","cardFreezeState":"0"},
            "cardList":[
                {"tradeTime":"2024-02-28 12:15:00","merchantName":"第一食堂","tradeName":"午餐消费","tradePrice":"-12.50","accountBalance":"52.50"},
                {"tradeTime":"2024-02-27 18:30:00","merchantName":"校园超市","tradeName":"日用品","tradePrice":"-28.00","accountBalance":"65.00"},
                {"tradeTime":"2024-02-27 07:45:00","merchantName":"第一食堂","tradeName":"早餐消费","tradePrice":"-5.00","accountBalance":"93.00"}
            ]
        }}
    """.trimIndent()

    private fun mockCardLost(): String =
        """{"success":true,"code":200,"message":"挂失成功","data":null}"""

    private fun mockBookQuery(): String = """
        {"success":true,"code":200,"message":"","data":[
            {"id":"b1","sn":"SN1001","code":"TP312.8-01","name":"Android 架构演进实践","author":"GDEI Labs","borrowDate":"2026-02-10","returnDate":"2026-03-20","renewTime":1},
            {"id":"b2","sn":"SN1002","code":"TP393-12","name":"现代移动网络编程","author":"Campus Net","borrowDate":"2026-02-14","returnDate":"2026-03-24","renewTime":0},
            {"id":"b3","sn":"SN1003","code":"I247.5-88","name":"岭南校园纪事","author":"图书馆编辑部","borrowDate":"2026-02-18","returnDate":"2026-03-28","renewTime":2}
        ]}
    """.trimIndent()

    private fun mockBookRenew(): String =
        """{"success":true,"code":200,"message":"续借成功"}"""

    private fun mockCetCheckCode(): String = """
        {"success":true,"code":200,"message":"","data":{
            "token":"mock_cet_token_${System.currentTimeMillis()}",
            "imageBase64":"$MOCK_CAPTCHA_BASE64"
        }}
    """.trimIndent()

    private fun mockCetQuery(): String = """
        {"success":true,"code":200,"message":"","data":{
            "name":"张三",
            "school":"广东第二师范学院",
            "type":"大学英语四级",
            "admissionCard":"CET202612345678",
            "totalScore":"568",
            "listeningScore":"189",
            "readingScore":"196",
            "writingAndTranslatingScore":"183"
        }}
    """.trimIndent()

    private fun mockEvaluateSubmit(): String =
        """{"success":true,"code":200,"message":"教学评价已提交"}"""

    private fun mockAnnouncementPage(request: Request): String {
        val size = request.url.pathSegments.lastOrNull()
            ?.toIntOrNull()
            ?.coerceIn(1, 20)
            ?: 5
        val payload = mockAnnouncements.take(size).joinToString(",") { announcement ->
            """{"id":"${announcement.id}","title":"${escapeJson(announcement.title)}","content":"${escapeJson(announcement.content)}","publishTime":"${escapeJson(announcement.publishTime)}"}"""
        }
        return """{"success":true,"code":200,"message":"","data":[$payload]}"""
    }

    private fun mockInteractionMessages(request: Request): String {
        val size = request.url.pathSegments.lastOrNull()
            ?.toIntOrNull()
            ?.coerceIn(1, 50)
            ?: 10
        val start = request.url.pathSegments.dropLast(2).lastOrNull()
            ?.toIntOrNull()
            ?.coerceAtLeast(0)
            ?: 0
        val payload = mockInteractionMessages.drop(start).take(size).joinToString(",") { item ->
            """{"id":"${item.id}","module":"${escapeJson(item.module)}","type":"${escapeJson(item.type)}","title":"${escapeJson(item.title)}","content":"${escapeJson(item.content)}","createdAt":"${escapeJson(item.createdAt)}","isRead":${item.isRead},"targetType":"${escapeJson(item.targetType)}","targetId":"${escapeJson(item.targetId)}","targetSubId":"${escapeJson(item.targetSubId)}"}"""
        }
        return """{"success":true,"code":200,"message":"","data":[$payload]}"""
    }

    private fun mockMessageUnread(): String {
        val unreadCount = mockInteractionMessages.count { !it.isRead }
        return """{"success":true,"code":200,"message":"","data":$unreadCount}"""
    }

    private fun mockMessageRead(request: Request): String {
        val id = request.url.pathSegments.dropLast(1).lastOrNull().orEmpty()
        mockInteractionMessages.replaceAll { item ->
            if (item.id == id) item.copy(isRead = true) else item
        }
        return """{"success":true,"code":200,"message":"已标记为已读"}"""
    }

    private fun mockMessageReadAll(): String {
        mockInteractionMessages.replaceAll { it.copy(isRead = true) }
        return """{"success":true,"code":200,"message":"全部消息已标记为已读"}"""
    }

    private fun mockNews(request: Request): String {
        val size = request.url.pathSegments.lastOrNull()
            ?.toIntOrNull()
            ?.coerceIn(1, 20)
            ?: 5
        val news = listOf(
            """{"id":"n1","title":"关于2026年上半年节假日放假安排的通知","publishDate":"2026-02-28","content":"<p>根据国务院办公厅通知精神，结合学校实际，现将2026年上半年节假日放假安排通知如下。</p><p>清明节为4月4日至6日，劳动节为5月1日至5日，请各单位妥善安排值班与安全保卫工作。</p><a href='https://gdeiassistant.cn/notice/1'>查看原文</a>"}""",
            """{"id":"n2","title":"教务处关于开展本学期期中教学检查的通知","publishDate":"2026-02-27","content":"<p>为加强教学过程管理，教务处定于第8至9周开展本学期期中教学检查。</p><p>检查内容包括课堂秩序、教案与作业批改、实验实训开展情况，各教学单位需按时提交自查报告。</p><a href='https://gdeiassistant.cn/notice/2'>查看原文</a>"}""",
            """{"id":"n3","title":"图书馆关于清明节期间开放时间调整的公告","publishDate":"2026-02-26","content":"<p>清明节假期图书馆开放时间将临时调整。</p><p>4月4日闭馆，4月5日至6日开放时间为9:00至17:00，4月7日起恢复正常开放。</p><a href='https://gdeiassistant.cn/notice/3'>查看原文</a>"}""",
            """{"id":"n4","title":"学生处关于2026届毕业生图像信息采集的通知","publishDate":"2026-02-25","content":"<p>2026届毕业生图像信息采集工作定于3月15日至16日进行。</p><p>地点位于教学楼A栋101，请各院系组织学生按时参加并携带身份证与学生证。</p><a href='https://gdeiassistant.cn/notice/4'>查看原文</a>"}""",
            """{"id":"n5","title":"后勤处关于校园一卡通系统升级维护的公告","publishDate":"2026-02-24","content":"<p>校园一卡通系统将于2月28日22:00至24:00进行升级维护。</p><p>维护期间食堂、门禁、图书馆等刷卡服务将暂停，请师生提前做好准备。</p><a href='https://gdeiassistant.cn/notice/5'>查看原文</a>"}"""
        )
        return """
            {"success":true,"code":200,"message":"","data":[${news.take(size).joinToString(",")}]}
        """.trimIndent()
    }

    private fun mockUserProfile(): String {
        return successDataJson(
            data = mockProfileSummary.toPayload(),
            message = "success"
        )
    }

    private fun mockLocationList(): String {
        return successDataJson(
            data = mockProfileLocationRegions.map { it.toPayload() },
            message = "success"
        )
    }

    private fun mockAvatarState(): String {
        val avatar = mockProfileSummary.avatar.trim().takeIf(String::isNotEmpty)
        return successDataJson(
            data = avatar,
            message = "success"
        )
    }

    private fun mockUploadAvatar(request: Request): String {
        val hasAvatar = request.hasMultipartPart("avatar")
        val hasAvatarHd = request.hasMultipartPart("avatar_hd")
        if (!hasAvatar || !hasAvatarHd) {
            return failureJson("缺少头像文件")
        }
        val seed = System.currentTimeMillis()
        mockProfileSummary = mockProfileSummary.copy(
            avatar = "https://picsum.photos/seed/gdei-avatar-$seed/720/720"
        )
        return successJson("头像已更新")
    }

    private fun mockDeleteAvatar(): String {
        mockProfileSummary = mockProfileSummary.copy(avatar = "")
        return successJson("已恢复默认头像")
    }

    private fun mockUpdateNickname(request: Request): String {
        val nickname = request.jsonObjectBody()?.getString("nickname")?.trim()
            ?: return failureJson("缺少昵称")
        if (nickname.isEmpty()) return failureJson("昵称不能为空")
        mockProfileSummary = mockProfileSummary.copy(nickname = nickname)
        return successJson("已保存")
    }

    private fun mockUpdateBirthday(request: Request): String {
        val body = request.jsonObjectBody() ?: return failureJson("缺少生日信息")
        val year = body.getInteger("year") ?: return failureJson("缺少年份")
        val month = body.getInteger("month") ?: return failureJson("缺少月份")
        val date = body.getInteger("date") ?: return failureJson("缺少日期")
        mockProfileSummary = mockProfileSummary.copy(
            birthday = "%04d-%02d-%02d".format(year, month, date)
        )
        return successJson("已保存")
    }

    private fun mockUpdateFaculty(request: Request): String {
        val facultyCode = request.jsonObjectBody()?.getInteger("faculty")
            ?: return failureJson("缺少院系信息")
        val facultyName = ProfileFormSupport.facultyOptions.getOrNull(facultyCode)
            ?: return failureJson("院系信息不存在")
        val majorOptions = ProfileFormSupport.majorOptionsFor(facultyName)
        mockProfileSummary = mockProfileSummary.copy(
            faculty = if (facultyName == ProfileFormSupport.UnselectedOption) "" else facultyName,
            major = mockProfileSummary.major.takeIf { majorOptions.contains(it) } ?: ""
        )
        return successJson("已保存")
    }

    private fun mockUpdateMajor(request: Request): String {
        val major = request.jsonObjectBody()?.getString("major")?.trim()
            ?: return failureJson("缺少专业信息")
        mockProfileSummary = mockProfileSummary.copy(major = major)
        return successJson("已保存")
    }

    private fun mockUpdateEnrollment(request: Request): String {
        val year = request.jsonObjectBody()?.getInteger("year")
        mockProfileSummary = mockProfileSummary.copy(enrollment = year?.toString().orEmpty())
        return successJson("已保存")
    }

    private fun mockUpdateLocation(request: Request): String {
        val displayName = locationDisplayNameFromRequest(request)
            ?: return failureJson("缺少所在地信息")
        mockProfileSummary = mockProfileSummary.copy(location = displayName)
        return successJson("已保存")
    }

    private fun mockUpdateHometown(request: Request): String {
        val displayName = locationDisplayNameFromRequest(request)
            ?: return failureJson("缺少家乡信息")
        mockProfileSummary = mockProfileSummary.copy(hometown = displayName)
        return successJson("已保存")
    }

    private fun mockUpdateIntroduction(request: Request): String {
        val introduction = request.jsonObjectBody()?.getString("introduction")?.trim().orEmpty()
        mockProfileSummary = mockProfileSummary.copy(introduction = introduction)
        return successJson("已保存")
    }

    private fun mockPhoneStatus(): String =
        """{"success":true,"code":200,"message":"","data":{"username":"2024001001","phone":"13800138000"}}"""

    private fun mockEmailStatus(): String =
        """{"success":true,"code":200,"message":"","data":"mock@gdeiassistant.cn"}"""

    private fun mockUserDataExportState(): String =
        """{"success":true,"code":200,"message":"","data":$mockUserDataExportState}"""

    private fun mockUserDataExport(): String {
        mockUserDataExportState = 2
        return """{"success":true,"code":200,"message":"已提交导出请求"}"""
    }

    private fun mockUserDataDownload(): String {
        return if (mockUserDataExportState == 2) {
            """{"success":true,"code":200,"message":"","data":"https://gdeiassistant.cn/download/mock-user-data.zip"}"""
        } else {
            """{"success":false,"code":400,"message":"请先提交用户数据导出请求","data":null}"""
        }
    }

    private fun mockSpareRooms(request: Request): String {
        val query = runCatching {
            JSON.parseObject(request.bodyUtf8(), MockSpareQueryRequest::class.java)
        }.getOrNull() ?: MockSpareQueryRequest()
        val zoneName = spareZones.getOrElse(query.zone ?: 0) { spareZones.first() }
        val roomType = spareTypes.getOrElse(query.type ?: 0) { spareTypes.first() }
        val section = "${query.startTime ?: 1}-${query.endTime ?: 2}节"
        val payload = mockSpareRooms.mapIndexed { index, room ->
            """{"number":"${room.number}","name":"${escapeJson(room.name)}","type":"${escapeJson(roomType)}","zone":"${escapeJson(zoneName)}","classSeating":"${room.classSeating + index * 10}","section":"${escapeJson(section)}","examSeating":"${room.examSeating + index * 8}"}"""
        }.joinToString(",")
        return """{"success":true,"code":200,"message":"","data":[$payload]}"""
    }

    private fun mockGraduateExam(request: Request): String {
        val query = runCatching {
            JSON.parseObject(request.bodyUtf8(), MockGraduateExamQuery::class.java)
        }.getOrNull() ?: MockGraduateExamQuery()
        return """
            {"success":true,"code":200,"message":"","data":{
                "name":"${escapeJson(query.name.ifBlank { "张三" })}",
                "signUpNumber":"2026${escapeJson(query.examNumber.ifBlank { "0011223344" }.takeLast(6))}",
                "examNumber":"${escapeJson(query.examNumber.ifBlank { "441526010203" })}",
                "totalScore":"389",
                "firstScore":"71",
                "secondScore":"74",
                "thirdScore":"121",
                "fourthScore":"123"
            }}
        """.trimIndent()
    }

    private fun mockElectricityFees(request: Request): String {
        val fields = request.formFields()
        val year = fields["year"]?.toIntOrNull() ?: 2026
        val studentNumber = fields["number"].orEmpty()
        val roomTail = studentNumber.takeLast(3).ifBlank { "318" }
        return """
            {"success":true,"code":200,"message":"","data":{
                "year":$year,
                "buildingNumber":"5栋",
                "roomNumber":$roomTail,
                "peopleNumber":4,
                "department":"计算机学院",
                "usedElectricAmount":126.50,
                "freeElectricAmount":35.00,
                "feeBasedElectricAmount":91.50,
                "electricPrice":0.68,
                "totalElectricBill":62.22,
                "averageElectricBill":15.56
            }}
        """.trimIndent()
    }

    private fun mockYellowPages(): String {
        val typePayload = mockYellowPageTypes.joinToString(",") { type ->
            """{"typeCode":${type.typeCode},"typeName":"${escapeJson(type.typeName)}"}"""
        }
        val dataPayload = mockYellowPageEntries.joinToString(",") { entry ->
            """{"id":${entry.id},"typeCode":${entry.typeCode},"typeName":"${escapeJson(entry.typeName)}","section":"${escapeJson(entry.section)}","campus":"${escapeJson(entry.campus)}","majorPhone":"${escapeJson(entry.majorPhone)}","minorPhone":"${escapeJson(entry.minorPhone)}","address":"${escapeJson(entry.address)}","email":"${escapeJson(entry.email)}","website":"${escapeJson(entry.website)}"}"""
        }
        return """{"success":true,"code":200,"message":"","data":{"data":[$dataPayload],"type":[$typePayload]}}"""
    }

    private fun mockMarketplaceItems(): String {
        val items = mockMarketplaceItems
            .filter { it.state == 1 }
            .sortedByDescending { it.publishTime }
            .map { it.toMarketplaceItemPayload() }
        return successDataJson(items)
    }

    private fun mockMarketplaceItemsByType(request: Request): String {
        val segments = request.url.pathSegments
        val typeIndex = segments.indexOf("type")
        val type = segments.getOrNull(typeIndex + 1)?.toIntOrNull()
        val items = mockMarketplaceItems
            .filter { it.state == 1 && (type == null || it.type == type) }
            .sortedByDescending { it.publishTime }
            .map { it.toMarketplaceItemPayload() }
        return successDataJson(items)
    }

    private fun mockMarketplaceDetail(request: Request): String {
        val item = mockMarketplaceItems.findById(request.itemIdFromPath())
            ?: return failureJson("未找到商品详情")
        val profile = mockMarketplaceProfiles.firstOrNull { it.username == item.username }
        return successDataJson(
            linkedMapOf(
                "profile" to profile?.toMarketplaceProfilePayload(),
                "secondhandItem" to item.toMarketplaceItemPayload()
            )
        )
    }

    private fun mockMarketplacePreview(request: Request): String {
        val preview = mockMarketplaceItems.findById(request.itemIdFromPath())
            ?.pictureURL
            ?.firstOrNull()
            .orEmpty()
        return successDataJson(preview)
    }

    private fun mockMarketplaceProfile(): String {
        val mine = mockMarketplaceItems.filter { it.username == MOCK_CURRENT_USERNAME }
        return successDataJson(
            linkedMapOf(
                "doing" to mine.filter { it.state == 1 }.sortedByDescending { it.publishTime }.map { it.toMarketplaceItemPayload() },
                "sold" to mine.filter { it.state == 2 }.sortedByDescending { it.publishTime }.map { it.toMarketplaceItemPayload() },
                "off" to mine.filter { it.state == 0 }.sortedByDescending { it.publishTime }.map { it.toMarketplaceItemPayload() }
            )
        )
    }

    private fun mockMarketplaceStateUpdate(request: Request): String {
        val itemId = request.itemIdFromPath() ?: return failureJson("缺少商品编号")
        val targetState = request.url.queryParameter("state")
            ?.toIntOrNull()
            ?: request.formFields()["state"]?.toIntOrNull()
            ?: return failureJson("缺少商品状态")
        val index = mockMarketplaceItems.indexOfFirst { it.id.toString() == itemId }
        if (index < 0) return failureJson("未找到商品")
        mockMarketplaceItems[index] = mockMarketplaceItems[index].copy(state = targetState)
        return successJson("状态已更新")
    }

    private fun mockLostFoundItems(lostType: Int): String {
        val items = mockLostFoundItems
            .filter { it.lostType == lostType && it.state == 0 }
            .sortedByDescending { it.publishTime }
            .map { it.toLostFoundItemPayload() }
        return successDataJson(items)
    }

    private fun mockLostFoundDetail(request: Request): String {
        val item = mockLostFoundItems.findById(request.itemIdFromPath())
            ?: return failureJson("未找到失物招领详情")
        val profile = mockLostFoundProfiles.firstOrNull { it.username == item.username }
        return successDataJson(
            linkedMapOf(
                "item" to item.toLostFoundItemPayload(),
                "profile" to profile?.toLostFoundProfilePayload()
            )
        )
    }

    private fun mockLostFoundPreview(request: Request): String {
        val preview = mockLostFoundItems.findById(request.itemIdFromPath())
            ?.pictureURL
            ?.firstOrNull()
            .orEmpty()
        return successDataJson(preview)
    }

    private fun mockLostFoundProfile(): String {
        val mine = mockLostFoundItems.filter { it.username == MOCK_CURRENT_USERNAME }
        return successDataJson(
            linkedMapOf(
                "lost" to mine.filter { it.lostType == 0 && it.state == 0 }.sortedByDescending { it.publishTime }.map { it.toLostFoundItemPayload() },
                "found" to mine.filter { it.lostType == 1 && it.state == 0 }.sortedByDescending { it.publishTime }.map { it.toLostFoundItemPayload() },
                "didfound" to mine.filter { it.state == 1 }.sortedByDescending { it.publishTime }.map { it.toLostFoundItemPayload() }
            )
        )
    }

    private fun mockLostFoundDidFound(request: Request): String {
        val itemId = request.itemIdFromPath() ?: return failureJson("缺少条目编号")
        val index = mockLostFoundItems.indexOfFirst { it.id.toString() == itemId }
        if (index < 0) return failureJson("未找到条目")
        mockLostFoundItems[index] = mockLostFoundItems[index].copy(state = 1)
        return successJson("已标记为找回")
    }

    private fun mockSecretPosts(): String {
        val items = mockSecretPosts
            .filter { it.state != 2 }
            .sortedByDescending { it.publishTime }
            .map { it.toSecretPayload(includeComments = false) }
        return successDataJson(items)
    }

    private fun mockSecretMyPosts(): String {
        val items = mockSecretPosts
            .filter { it.username == MOCK_CURRENT_USERNAME && it.state != 2 }
            .sortedByDescending { it.publishTime }
            .map { it.toSecretPayload(includeComments = false) }
        return successDataJson(items)
    }

    private fun mockSecretDetail(request: Request): String {
        val postId = request.itemIdFromPath() ?: return failureJson("缺少树洞编号")
        val post = mockSecretPosts.findById(postId) ?: return failureJson("查询的校园树洞信息不存在")
        return successDataJson(post.toSecretPayload(includeComments = true))
    }

    private fun mockSecretComments(request: Request): String {
        val postId = request.itemIdFromPath() ?: return failureJson("缺少树洞编号")
        val comments = mockSecretComments
            .filter { it.contentId.toString() == postId }
            .sortedByDescending { it.publishTime }
            .map { it.toSecretCommentPayload() }
        return successDataJson(comments)
    }

    private fun mockSecretCommentSubmit(request: Request): String {
        val postId = request.itemIdFromPath() ?: return failureJson("缺少树洞编号")
        val comment = request.url.queryParameter("comment")
            ?: request.formFields()["comment"]
            ?: return failureJson("评论内容不能为空")
        val trimmed = comment.trim()
        if (trimmed.isBlank()) return failureJson("评论内容不能为空")
        if (trimmed.length > 50) return failureJson("评论内容不能超过 50 个字")
        val postIndex = mockSecretPosts.indexOfFirst { it.id.toString() == postId }
        if (postIndex < 0) return failureJson("查询的校园树洞信息不存在")
        mockSecretComments.add(
            0,
            MockSecretCommentRecord(
                id = (mockSecretComments.maxOfOrNull { it.id } ?: 7000) + 1,
                contentId = postId.toInt(),
                username = "Mock 同学",
                comment = trimmed,
                publishTime = "刚刚",
                avatarTheme = 1
            )
        )
        val post = mockSecretPosts[postIndex]
        mockSecretPosts[postIndex] = post.copy(commentCount = post.commentCount + 1)
        return successJson("评论已发送")
    }

    private fun mockSecretLikeUpdate(request: Request): String {
        val postId = request.itemIdFromPath() ?: return failureJson("缺少树洞编号")
        val like = request.url.queryParameter("like")
            ?.toIntOrNull()
            ?: request.formFields()["like"]?.toIntOrNull()
            ?: return failureJson("缺少点赞状态")
        val postIndex = mockSecretPosts.indexOfFirst { it.id.toString() == postId }
        if (postIndex < 0) return failureJson("查询的校园树洞信息不存在")
        val post = mockSecretPosts[postIndex]
        val liked = like == 1
        mockSecretPosts[postIndex] = post.copy(
            liked = liked,
            likeCount = (post.likeCount + if (liked && !post.liked) 1 else if (!liked && post.liked) -1 else 0).coerceAtLeast(0)
        )
        return successJson("点赞状态已更新")
    }

    private fun mockDatingReceivedPicks(): String {
        val items = mockDatingReceivedPicks
            .sortedByDescending { it.updatedAt }
            .map { it.toDatingPickPayload() }
        return successDataJson(items)
    }

    private fun mockDatingSentPicks(): String {
        val items = mockDatingSentPicks
            .sortedByDescending { it.updatedAt }
            .map { it.toDatingPickPayload() }
        return successDataJson(items)
    }

    private fun mockDatingMyPosts(): String {
        val items = mockDatingProfiles
            .filter { it.username == MOCK_CURRENT_USERNAME && it.state != 0 }
            .map { it.toDatingProfilePayload() }
        return successDataJson(items)
    }

    private fun mockDatingPickStateUpdate(request: Request): String {
        val pickId = request.itemIdFromPath() ?: return failureJson("缺少请求编号")
        val state = request.url.queryParameter("state")
            ?.toIntOrNull()
            ?: request.formFields()["state"]?.toIntOrNull()
            ?: return failureJson("缺少状态参数")
        val index = mockDatingReceivedPicks.indexOfFirst { it.pickId.toString() == pickId }
        if (index < 0) return failureJson("未找到该请求")
        mockDatingReceivedPicks[index] = mockDatingReceivedPicks[index].copy(state = state)
        return successJson("状态已更新")
    }

    private fun mockDatingProfileHide(request: Request): String {
        val profileId = request.itemIdFromPath() ?: return failureJson("缺少发布编号")
        val index = mockDatingProfiles.indexOfFirst { it.profileId.toString() == profileId }
        if (index < 0) return failureJson("未找到该发布")
        mockDatingProfiles[index] = mockDatingProfiles[index].copy(state = 0)
        return successJson("已隐藏")
    }

    private fun mockExpressPosts(): String {
        val items = mockExpressPosts
            .sortedByDescending { it.publishTime }
            .map { it.toExpressPayload() }
        return successDataJson(items)
    }

    private fun mockExpressMyPosts(): String {
        val items = mockExpressPosts
            .filter { it.username == MOCK_CURRENT_USERNAME }
            .sortedByDescending { it.publishTime }
            .map { it.toExpressPayload() }
        return successDataJson(items)
    }

    private fun mockExpressDetail(request: Request): String {
        val postId = request.itemIdFromPath() ?: return failureJson("缺少表白编号")
        val item = mockExpressPosts.findById(postId) ?: return failureJson("查询的表白内容不存在")
        return successDataJson(item.toExpressPayload())
    }

    private fun mockExpressComments(request: Request): String {
        val postId = request.itemIdFromPath() ?: return failureJson("缺少表白编号")
        val items = mockExpressComments
            .filter { it.expressId.toString() == postId }
            .sortedByDescending { it.publishTime }
            .map { it.toExpressCommentPayload() }
        return successDataJson(items)
    }

    private fun mockExpressCommentSubmit(request: Request): String {
        val postId = request.itemIdFromPath() ?: return failureJson("缺少表白编号")
        val trimmed = request.url.queryParameter("comment")
            ?.trim()
            ?: request.formFields()["comment"]?.trim()
            ?: return failureJson("缺少评论内容")
        if (trimmed.isBlank()) return failureJson("评论内容不能为空")
        val postIndex = mockExpressPosts.indexOfFirst { it.id.toString() == postId }
        if (postIndex < 0) return failureJson("查询的表白内容不存在")
        mockExpressComments.add(
            0,
            MockExpressCommentRecord(
                id = (mockExpressComments.maxOfOrNull { it.id } ?: 9500) + 1,
                expressId = postId.toInt(),
                username = MOCK_CURRENT_USERNAME,
                nickname = "Mock 同学",
                comment = trimmed,
                publishTime = "刚刚"
            )
        )
        val item = mockExpressPosts[postIndex]
        mockExpressPosts[postIndex] = item.copy(commentCount = item.commentCount + 1)
        return successJson("评论已发送")
    }

    private fun mockExpressLike(request: Request): String {
        val postId = request.itemIdFromPath() ?: return failureJson("缺少表白编号")
        val postIndex = mockExpressPosts.indexOfFirst { it.id.toString() == postId }
        if (postIndex < 0) return failureJson("查询的表白内容不存在")
        val item = mockExpressPosts[postIndex]
        if (!item.liked) {
            mockExpressPosts[postIndex] = item.copy(liked = true, likeCount = item.likeCount + 1)
        }
        return successJson("点赞成功")
    }

    private fun mockExpressGuess(request: Request): String {
        val postId = request.itemIdFromPath() ?: return failureJson("缺少表白编号")
        val name = request.url.queryParameter("name")
            ?.trim()
            ?: request.formFields()["name"]?.trim()
            ?: return failureJson("缺少猜测姓名")
        val postIndex = mockExpressPosts.indexOfFirst { it.id.toString() == postId }
        if (postIndex < 0) return failureJson("查询的表白内容不存在")
        val item = mockExpressPosts[postIndex]
        val matched = item.realname.equals(name, ignoreCase = true)
        mockExpressPosts[postIndex] = item.copy(
            guessCount = item.guessCount + if (matched) 1 else 0,
            guessSum = item.guessSum + 1
        )
        return successDataJson(matched)
    }

    private fun mockTopicPosts(): String {
        val items = mockTopicPosts
            .sortedByDescending { it.publishTime }
            .map { it.toTopicPayload() }
        return successDataJson(items)
    }

    private fun mockTopicMyPosts(): String {
        val items = mockTopicPosts
            .filter { it.username == MOCK_CURRENT_USERNAME }
            .sortedByDescending { it.publishTime }
            .map { it.toTopicPayload() }
        return successDataJson(items)
    }

    private fun mockTopicDetail(request: Request): String {
        val postId = request.itemIdFromPath() ?: return failureJson("缺少话题编号")
        val item = mockTopicPosts.findById(postId) ?: return failureJson("查询的话题内容不存在")
        return successDataJson(item.toTopicPayload())
    }

    private fun mockTopicLike(request: Request): String {
        val postId = request.itemIdFromPath() ?: return failureJson("缺少话题编号")
        val postIndex = mockTopicPosts.indexOfFirst { it.id.toString() == postId }
        if (postIndex < 0) return failureJson("查询的话题内容不存在")
        val item = mockTopicPosts[postIndex]
        if (!item.liked) {
            mockTopicPosts[postIndex] = item.copy(liked = true, likeCount = item.likeCount + 1)
        }
        return successJson("点赞成功")
    }

    private fun mockTopicImage(request: Request): String {
        val postId = request.itemIdFromPath() ?: return failureJson("缺少话题编号")
        val index = request.pathValueAfter("index")?.toIntOrNull() ?: return failureJson("缺少图片序号")
        val item = mockTopicPosts.findById(postId) ?: return failureJson("查询的话题内容不存在")
        val image = item.imageUrls.getOrNull(index - 1) ?: return failureJson("图片不存在")
        return successDataJson(image)
    }

    private fun mockDeliveryOrders(request: Request): String {
        val start = request.pathValueAfter("start")?.toIntOrNull()?.coerceAtLeast(0) ?: 0
        val size = request.pathValueAfter("size")?.toIntOrNull()?.coerceIn(1, 50) ?: 20
        val items = mockDeliveryOrderRecords
            .sortedByDescending { it.orderTime }
            .drop(start)
            .take(size)
            .map { it.toDeliveryOrderPayload() }
        return successDataJson(items)
    }

    private fun mockDeliveryMine(): String {
        val published = mockDeliveryOrderRecords
            .filter { it.username == MOCK_CURRENT_USERNAME }
            .sortedByDescending { it.orderTime }
            .map { it.toDeliveryOrderPayload() }
        val accepted = mockDeliveryTrades
            .filter { it.username == MOCK_CURRENT_USERNAME }
            .mapNotNull { trade ->
                mockDeliveryOrderRecords.firstOrNull { it.orderId == trade.orderId }
            }
            .sortedByDescending { it.orderTime }
            .map { it.toDeliveryOrderPayload() }
        return successDataJson(
            linkedMapOf(
                "published" to published,
                "accepted" to accepted
            )
        )
    }

    private fun mockDeliveryOrderDetail(request: Request): String {
        val orderId = request.itemIdFromPath() ?: return failureJson("缺少跑腿编号")
        val order = mockDeliveryOrderRecords.findById(orderId) ?: return failureJson("查询的全民快递订单不存在")
        val trade = mockDeliveryTrades.firstOrNull { it.orderId == order.orderId }
        val detailType = when {
            order.username == MOCK_CURRENT_USERNAME -> 0
            trade?.username == MOCK_CURRENT_USERNAME -> 3
            else -> 1
        }
        return successDataJson(
            linkedMapOf(
                "order" to order.toDeliveryOrderPayload(),
                "detailType" to detailType,
                "trade" to trade?.toDeliveryTradePayload()
            )
        )
    }

    private fun mockDeliveryAcceptOrder(request: Request): String {
        val orderId = request.url.queryParameter("orderId")
            ?: request.formFields()["orderId"]
            ?: return failureJson("缺少跑腿编号")
        val orderIndex = mockDeliveryOrderRecords.indexOfFirst { it.orderId.toString() == orderId }
        if (orderIndex < 0) return failureJson("查询的全民快递订单不存在")
        val order = mockDeliveryOrderRecords[orderIndex]
        if (order.state != 0) return failureJson("该订单已被接单")
        mockDeliveryOrderRecords[orderIndex] = order.copy(state = 1)
        val exists = mockDeliveryTrades.any { it.orderId == order.orderId }
        if (!exists) {
            mockDeliveryTrades.add(
                0,
                MockDeliveryTradeRecord(
                    tradeId = (mockDeliveryTrades.maxOfOrNull { it.tradeId } ?: 12900) + 1,
                    orderId = order.orderId,
                    createTime = "刚刚",
                    username = MOCK_CURRENT_USERNAME,
                    state = 0
                )
            )
        }
        return successJson("接单成功")
    }

    private fun mockDeliveryFinishTrade(request: Request): String {
        val tradeId = request.itemIdFromPath() ?: return failureJson("缺少交易编号")
        val tradeIndex = mockDeliveryTrades.indexOfFirst { it.tradeId.toString() == tradeId }
        if (tradeIndex < 0) return failureJson("交易记录不存在")
        val trade = mockDeliveryTrades[tradeIndex]
        mockDeliveryTrades[tradeIndex] = trade.copy(state = 1)
        val orderIndex = mockDeliveryOrderRecords.indexOfFirst { it.orderId == trade.orderId }
        if (orderIndex >= 0) {
            mockDeliveryOrderRecords[orderIndex] = mockDeliveryOrderRecords[orderIndex].copy(state = 2)
        }
        return successJson("订单已完成")
    }

    private fun mockDeliveryDeleteOrder(request: Request): String {
        val orderId = request.itemIdFromPath() ?: return failureJson("缺少跑腿编号")
        val removed = mockDeliveryOrderRecords.removeIf { it.orderId.toString() == orderId }
        if (!removed) return failureJson("查询的全民快递订单不存在")
        mockDeliveryTrades.removeIf { it.orderId.toString() == orderId }
        return successJson("已删除")
    }

    private fun mockDeliveryPublish(request: Request): String {
        val fields = request.formFields()
        val pickupPlace = fields["company"].orEmpty().ifBlank { "白云校区快递站" }
        val order = MockDeliveryOrderRecord(
            orderId = (mockDeliveryOrderRecords.maxOfOrNull { it.orderId } ?: 12800) + 1,
            username = MOCK_CURRENT_USERNAME,
            orderTime = "刚刚",
            name = fields["name"].orEmpty().ifBlank { "取件任务" },
            number = fields["number"].orEmpty().ifBlank { "待补充" },
            phone = fields["phone"].orEmpty().ifBlank { "13800138000" },
            price = fields["price"]?.toDoubleOrNull() ?: 3.0,
            company = pickupPlace,
            address = fields["address"].orEmpty().ifBlank { "待补充地址" },
            state = 0,
            remarks = fields["remarks"].orEmpty()
        )
        mockDeliveryOrderRecords.add(0, order)
        return successJson("发布成功")
    }

    private fun mockPhotographPhotoCount(): String =
        successDataJson(mockPhotographPostRecords.sumOf { it.photoCount() })

    private fun mockPhotographCommentCount(): String =
        successDataJson(mockPhotographCommentRecords.size)

    private fun mockPhotographLikeCount(): String =
        successDataJson(mockPhotographPostRecords.sumOf { it.likeCount })

    private fun mockPhotographPosts(request: Request): String {
        val type = request.pathValueAfter("type")?.toIntOrNull() ?: 0
        val start = request.pathValueAfter("start")?.toIntOrNull()?.coerceAtLeast(0) ?: 0
        val size = request.pathValueAfter("size")?.toIntOrNull()?.coerceIn(1, 50) ?: 20
        val items = mockPhotographPostRecords
            .filter { it.type == type }
            .sortedByDescending { it.createTime }
            .drop(start)
            .take(size)
            .map { it.toPhotographPayload(includeComments = false) }
        return successDataJson(items)
    }

    private fun mockPhotographMyPosts(request: Request): String {
        val start = request.pathValueAfter("start")?.toIntOrNull()?.coerceAtLeast(0) ?: 0
        val size = request.pathValueAfter("size")?.toIntOrNull()?.coerceIn(1, 50) ?: 20
        val items = mockPhotographPostRecords
            .filter { it.username == MOCK_CURRENT_USERNAME }
            .sortedByDescending { it.createTime }
            .drop(start)
            .take(size)
            .map { it.toPhotographPayload(includeComments = false) }
        return successDataJson(items)
    }

    private fun mockPhotographDetail(request: Request): String {
        val postId = request.itemIdFromPath() ?: return failureJson("缺少作品编号")
        val item = mockPhotographPostRecords.findById(postId) ?: return failureJson("查询的拍好校园作品不存在")
        return successDataJson(item.toPhotographPayload(includeComments = true))
    }

    private fun mockPhotographImage(request: Request): String {
        val postId = request.itemIdFromPath() ?: return failureJson("缺少作品编号")
        val index = request.pathValueAfter("index")?.toIntOrNull() ?: return failureJson("缺少图片序号")
        val item = mockPhotographPostRecords.findById(postId) ?: return failureJson("查询的拍好校园作品不存在")
        val image = item.resolvedImageUrls().getOrNull(index - 1) ?: return failureJson("图片不存在")
        return successDataJson(image)
    }

    private fun mockPhotographComments(request: Request): String {
        val postId = request.itemIdFromPath() ?: return failureJson("缺少作品编号")
        val items = mockPhotographCommentRecords
            .filter { it.photoId.toString() == postId }
            .sortedByDescending { it.createTime }
            .map { it.toPhotographCommentPayload() }
        return successDataJson(items)
    }

    private fun mockPhotographCommentSubmit(request: Request): String {
        val postId = request.itemIdFromPath() ?: return failureJson("缺少作品编号")
        val comment = request.url.queryParameter("comment")
            ?: request.formFields()["comment"]
            ?: return failureJson("评论内容不能为空")
        val trimmed = comment.trim()
        if (trimmed.isBlank()) return failureJson("评论内容不能为空")
        val postIndex = mockPhotographPostRecords.indexOfFirst { it.id.toString() == postId }
        if (postIndex < 0) return failureJson("查询的拍好校园作品不存在")
        mockPhotographCommentRecords.add(
            0,
            MockPhotographCommentRecord(
                commentId = (mockPhotographCommentRecords.maxOfOrNull { it.commentId } ?: 14200) + 1,
                photoId = postId.toInt(),
                username = MOCK_CURRENT_USERNAME,
                nickname = "Mock 同学",
                comment = trimmed,
                createTime = "刚刚"
            )
        )
        val post = mockPhotographPostRecords[postIndex]
        mockPhotographPostRecords[postIndex] = post.copy(commentCount = post.commentCount + 1)
        return successJson("评论已发送")
    }

    private fun mockPhotographLike(request: Request): String {
        val postId = request.itemIdFromPath() ?: return failureJson("缺少作品编号")
        val postIndex = mockPhotographPostRecords.indexOfFirst { it.id.toString() == postId }
        if (postIndex < 0) return failureJson("查询的拍好校园作品不存在")
        val post = mockPhotographPostRecords[postIndex]
        if (!post.liked) {
            mockPhotographPostRecords[postIndex] = post.copy(liked = true, likeCount = post.likeCount + 1)
        }
        return successJson("点赞成功")
    }

    private fun mockPhotographPublish(request: Request): String {
        val title = request.multipartField("title").orEmpty().ifBlank { "新的校园瞬间" }
        val content = request.multipartField("content").orEmpty().ifBlank { "刚刚上传了一组校园照片。" }
        val publishType = request.multipartField("type")?.toIntOrNull() ?: 1
        val categoryType = if (publishType == 1) 1 else 0
        val imageCount = maxOf(
            request.multipartField("count")?.toIntOrNull() ?: 0,
            request.multipartImageCount(),
            1
        ).coerceAtMost(4)
        val id = (mockPhotographPostRecords.maxOfOrNull { it.id } ?: 14100) + 1
        val imageUrls = (1..imageCount).map { index ->
            "https://picsum.photos/seed/gdei-photograph-$id-$index/960/720"
        }
        mockPhotographPostRecords.add(
            0,
            MockPhotographPostRecord(
                id = id,
                title = title,
                content = content,
                count = imageCount,
                type = categoryType,
                username = MOCK_CURRENT_USERNAME,
                createTime = "刚刚",
                likeCount = 0,
                commentCount = 0,
                liked = false,
                firstImageUrl = imageUrls.firstOrNull(),
                imageUrls = imageUrls
            )
        )
        return successJson("发布成功")
    }

    private fun mockServerPublicKey(): String = """
        {"success":true,"code":200,"message":"","data":"${Base64.encodeToString(mockChargeKeyPair.public.encoded, Base64.NO_WRAP)}"}
    """.trimIndent()

    private fun mockCharge(request: Request): String {
        val fields = request.formFields()
        val encryptedAesKey = fields["clientAESKey"] ?: return failureJson("缺少密钥信息")
        return runCatching {
            val aesKey = ChargeCrypto.decryptWithPrivateKey(
                mockChargeKeyPair.private.encoded,
                Base64.decode(encryptedAesKey, Base64.NO_WRAP)
            )
            val amount = fields["amount"].orEmpty().ifBlank { "0" }
            val charge = Charge(
                alipayURL = "https://gdeiassistant.cn/?mockCharge=$amount",
                cookieList = listOf(
                    Cookie(
                        name = "mock_charge_session",
                        value = "session_${System.currentTimeMillis()}",
                        domain = "gdeiassistant.cn"
                    )
                )
            )
            val chargeJson = JSON.toJSONString(charge)
            val encryptedPayload = Base64.encodeToString(
                ChargeCrypto.encryptWithAes(aesKey, chargeJson.toByteArray(Charsets.UTF_8)),
                Base64.NO_WRAP
            )
            val signature = Base64.encodeToString(
                ChargeCrypto.encryptWithPrivateKey(
                    mockChargeKeyPair.private.encoded,
                    ChargeCrypto.sha1Hex(chargeJson).toByteArray(Charsets.UTF_8)
                ),
                Base64.NO_WRAP
            )
            """
                {"success":true,"code":200,"message":"","data":{"data":"$encryptedPayload","signature":"$signature"}}
            """.trimIndent()
        }.getOrElse {
            failureJson(it.message ?: "模拟充值失败")
        }
    }

    private fun mockTokenRefresh(): String = """
        {"success":true,"code":200,"message":"","data":{
            "accessToken":{"signature":"mock_access_${System.currentTimeMillis()}","createTime":${System.currentTimeMillis()},"expireTime":${System.currentTimeMillis() + 3600000}},
            "refreshToken":{"signature":"mock_refresh_${System.currentTimeMillis()}","createTime":${System.currentTimeMillis()},"expireTime":${System.currentTimeMillis() + 86400000}}
        }}
    """.trimIndent()

    private fun mockTokenExpire(): String =
        """{"success":true,"code":200,"message":"token 有效"}"""

    private fun mockUpgrade(): String = """
        {"success":true,"code":200,"message":"","data":{
            "downloadURL":"https://gdeiassistant.cn/download/android/mock-release.apk",
            "versionInfo":"Mock 模式下的升级说明：用于验证关于页、下载按钮与版本提示链路。",
            "versionCodeName":"2.1.0-dev",
            "fileSize":"18.2 MB",
            "versionCode":99
        }}
    """.trimIndent()

    // ── P0: 图书馆 ─────────────────────────────────────────────────────────

    private fun mockCollectionSearch(request: Request): String {
        val keyword = request.url.queryParameter("keyword").orEmpty()
        val page = request.url.queryParameter("page")?.toIntOrNull()?.coerceAtLeast(1) ?: 1
        val pageSize = 5
        val all = listOf(
            Triple("Android 架构演进实践", "GDEI Labs", "广东第二师范学院出版社"),
            Triple("现代移动网络编程", "Campus Net", "计算机科学出版社"),
            Triple("岭南校园纪事", "图书馆编辑部", "岭南出版社"),
            Triple("Kotlin 协程实战", "JetBrains 社区", "技术出版社"),
            Triple("数据结构与算法分析", "Mark Allen Weiss", "机械工业出版社"),
            Triple("计算机网络：自顶向下方法", "Kurose & Ross", "机械工业出版社"),
            Triple("软件工程：实践者的研究方法", "Roger Pressman", "机械工业出版社"),
            Triple("人工智能导论", "周志华", "清华大学出版社")
        )
        val matched = if (keyword.isBlank()) all
            else all.filter { (name, author, _) ->
                name.contains(keyword, ignoreCase = true) || author.contains(keyword, ignoreCase = true)
            }
        val sumPage = maxOf(1, (matched.size + pageSize - 1) / pageSize)
        val paged = matched.drop((page - 1) * pageSize).take(pageSize)
        val payload = paged.mapIndexed { i, (name, author, publisher) ->
            val url = "https://lib.gdeiassistant.cn/book/detail?id=${page * 100 + i + 1}"
            """{"bookname":"${escapeJson(name)}","author":"${escapeJson(author)}","publishingHouse":"${escapeJson(publisher)}","detailURL":"${escapeJson(url)}"}"""
        }.joinToString(",")
        return """{"success":true,"code":200,"message":"","data":{"sumPage":$sumPage,"collectionList":[$payload]}}"""
    }

    private fun mockCollectionDetail(request: Request): String {
        val detailUrl = request.url.queryParameter("detailURL").orEmpty()
        val id = detailUrl.substringAfterLast("id=").ifBlank { "001" }
        return """{"success":true,"code":200,"message":"","data":{
            "bookname":"Android 架构演进实践",
            "author":"GDEI Labs",
            "principal":"GDEI Labs 编",
            "personalPrincipal":"",
            "publishingHouse":"广东第二师范学院出版社",
            "price":"58.00",
            "physicalDescriptionArea":"320页；26cm",
            "subjectTheme":"Android 移动开发",
            "chineseLibraryClassification":"TP312.8",
            "collectionDistributionList":[
                {"location":"白云校区图书馆 三楼","callNumber":"TP312.8/G001","barcode":"LIB${id}A","state":"在馆"},
                {"location":"白云校区图书馆 三楼","callNumber":"TP312.8/G001","barcode":"LIB${id}B","state":"借出"},
                {"location":"龙洞校区图书馆","callNumber":"TP312.8/G001","barcode":"LIB${id}C","state":"在馆"}
            ]
        }}""".trimIndent()
    }

    private fun mockCollectionBorrow(): String {
        val payload = """[
            {"id":"b1","sn":"SN1001","code":"TP312.8-01","name":"Android 架构演进实践","author":"GDEI Labs","borrowDate":"2026-02-10","returnDate":"2026-03-20","renewTime":1},
            {"id":"b2","sn":"SN1002","code":"TP393-12","name":"现代移动网络编程","author":"Campus Net","borrowDate":"2026-02-14","returnDate":"2026-03-24","renewTime":0},
            {"id":"b3","sn":"SN1003","code":"I247.5-88","name":"岭南校园纪事","author":"图书馆编辑部","borrowDate":"2026-02-18","returnDate":"2026-03-28","renewTime":2}
        ]"""
        return """{"success":true,"code":200,"message":"","data":$payload}"""
    }

    private fun mockCollectionRenew(): String =
        """{"success":true,"code":200,"message":"续借成功"}"""

    // ── P0: 云存储上传 ──────────────────────────────────────────────────────

    private fun mockPresignedUrl(): String =
        """{"success":true,"code":200,"message":"","data":{"url":"https://api.gdeiassistant.cn/api/upload/mock/file","expiresIn":300}}"""

    private fun mockUploadFile(): String =
        successJson("上传成功")

    // ── P1: 社区发布 ────────────────────────────────────────────────────────

    private fun mockSecretPublish(request: Request): String {
        val theme = request.multipartField("theme")?.toIntOrNull() ?: 0
        val content = request.multipartField("content").orEmpty().ifBlank { "新的小纸条内容" }
        val type = request.multipartField("type")?.toIntOrNull() ?: 0
        val timer = request.multipartField("timer")?.toIntOrNull() ?: 0
        val id = (mockSecretPosts.maxOfOrNull { it.id } ?: 9999) + 1
        mockSecretPosts.add(0, MockSecretPostRecord(
            id = id, username = MOCK_CURRENT_USERNAME,
            content = content, theme = theme, type = type, timer = timer,
            state = 1, publishTime = "刚刚", likeCount = 0, commentCount = 0, liked = false
        ))
        return successJson("发布成功")
    }

    private fun mockExpressPublish(request: Request): String {
        val fields = request.formFields()
        val nickname = fields["nickname"].orEmpty().ifBlank { "Mock 同学" }
        val content = fields["content"].orEmpty().ifBlank { "新的表白内容" }
        val name = fields["name"].orEmpty().ifBlank { "你" }
        val selfGender = fields["selfGender"]?.toIntOrNull() ?: 0
        val personGender = fields["personGender"]?.toIntOrNull() ?: 0
        val id = (mockExpressPosts.maxOfOrNull { it.id } ?: 10999) + 1
        mockExpressPosts.add(0, MockExpressPostRecord(
            id = id, username = MOCK_CURRENT_USERNAME,
            nickname = nickname, realname = fields["realname"].orEmpty(),
            selfGender = selfGender, name = name, content = content,
            personGender = personGender, publishTime = "刚刚",
            likeCount = 0, liked = false, commentCount = 0,
            guessCount = 0, guessSum = 0, canGuess = false
        ))
        return successJson("发布成功")
    }

    private fun mockTopicPublish(request: Request): String {
        val topic = request.multipartField("topic").orEmpty().ifBlank { "校园生活" }
        val content = request.multipartField("content").orEmpty().ifBlank { "分享校园生活点滴。" }
        val count = maxOf(
            request.multipartField("count")?.toIntOrNull() ?: 0,
            request.multipartImageCount()
        ).coerceAtMost(9)
        val id = (mockTopicPosts.maxOfOrNull { it.id } ?: 12999) + 1
        val imageUrls = if (count > 0) {
            (1..count).map { "https://picsum.photos/seed/gdei-topic-$id-$it/960/720" }
        } else emptyList()
        mockTopicPosts.add(0, MockTopicPostRecord(
            id = id, username = MOCK_CURRENT_USERNAME,
            topic = topic, content = content, count = count,
            publishTime = "刚刚", likeCount = 0, liked = false,
            firstImageUrl = imageUrls.firstOrNull(),
            imageUrls = imageUrls
        ))
        return successJson("发布成功")
    }

    // ── P1: 搜索 ────────────────────────────────────────────────────────────

    private fun mockExpressSearch(request: Request): String {
        val segments = request.url.pathSegments
        val kwIdx = segments.indexOf("keyword")
        val keyword = segments.getOrNull(kwIdx + 1).orEmpty()
        val start = segments.getOrNull(kwIdx + 3)?.toIntOrNull() ?: 0
        val size = segments.lastOrNull()?.toIntOrNull()?.coerceIn(1, 20) ?: 10
        val matched = mockExpressPosts.filter {
            it.content.contains(keyword, ignoreCase = true) ||
            it.name.contains(keyword, ignoreCase = true) ||
            it.nickname.contains(keyword, ignoreCase = true)
        }
        return successDataJson(matched.drop(start).take(size).map { it.toExpressPayload() })
    }

    private fun mockTopicSearch(request: Request): String {
        val segments = request.url.pathSegments
        val kwIdx = segments.indexOf("keyword")
        val keyword = segments.getOrNull(kwIdx + 1).orEmpty()
        val start = segments.getOrNull(kwIdx + 3)?.toIntOrNull() ?: 0
        val size = segments.lastOrNull()?.toIntOrNull()?.coerceIn(1, 20) ?: 10
        val matched = mockTopicPosts.filter {
            it.topic.contains(keyword, ignoreCase = true) ||
            it.content.contains(keyword, ignoreCase = true)
        }
        return successDataJson(matched.drop(start).take(size).map { it.toTopicPayload() })
    }

    // ── P2: 用户账户管理 ────────────────────────────────────────────────────

    private fun mockGetPrivacySettings(): String {
        val s = mockPrivacySettings
        return """{"success":true,"code":200,"message":"","data":{
            "facultyOpen":${s.facultyOpen},"majorOpen":${s.majorOpen},
            "locationOpen":${s.locationOpen},"hometownOpen":${s.hometownOpen},
            "introductionOpen":${s.introductionOpen},"enrollmentOpen":${s.enrollmentOpen},
            "ageOpen":${s.ageOpen},"cacheAllow":${s.cacheAllow},
            "robotsIndexAllow":${s.robotsIndexAllow}
        }}""".trimIndent()
    }

    private fun mockUpdatePrivacySettings(request: Request): String {
        val body = request.jsonObjectBody() ?: return failureJson("请求体格式错误")
        mockPrivacySettings = mockPrivacySettings.copy(
            facultyOpen = body.getBoolean("facultyOpen") ?: mockPrivacySettings.facultyOpen,
            majorOpen = body.getBoolean("majorOpen") ?: mockPrivacySettings.majorOpen,
            locationOpen = body.getBoolean("locationOpen") ?: mockPrivacySettings.locationOpen,
            hometownOpen = body.getBoolean("hometownOpen") ?: mockPrivacySettings.hometownOpen,
            introductionOpen = body.getBoolean("introductionOpen") ?: mockPrivacySettings.introductionOpen,
            enrollmentOpen = body.getBoolean("enrollmentOpen") ?: mockPrivacySettings.enrollmentOpen,
            ageOpen = body.getBoolean("ageOpen") ?: mockPrivacySettings.ageOpen,
            cacheAllow = body.getBoolean("cacheAllow") ?: mockPrivacySettings.cacheAllow,
            robotsIndexAllow = body.getBoolean("robotsIndexAllow") ?: mockPrivacySettings.robotsIndexAllow
        )
        return successJson("隐私设置已更新")
    }

    private fun mockLoginRecords(request: Request): String {
        val segments = request.url.pathSegments
        val startIdx = segments.indexOf("start")
        val start = segments.getOrNull(startIdx + 1)?.toIntOrNull() ?: 0
        val sizeIdx = segments.indexOf("size")
        val size = segments.getOrNull(sizeIdx + 1)?.toIntOrNull()?.coerceIn(1, 20) ?: 10
        val paged = mockLoginRecordList.drop(start).take(size)
        val payload = paged.joinToString(",") { r ->
            """{"id":${r.id},"ip":"${r.ip}","area":"${escapeJson(r.area)}","country":"${escapeJson(r.country)}","province":"${escapeJson(r.province)}","city":"${escapeJson(r.city)}","network":"${escapeJson(r.network)}","time":"${escapeJson(r.time)}"}"""
        }
        return """{"success":true,"code":200,"message":"","data":[$payload]}"""
    }

    private fun mockPhoneAttributions(): String {
        val payload = listOf(
            """{"code":86,"flag":"🇨🇳","name":"中国大陆"}""",
            """{"code":852,"flag":"🇭🇰","name":"中国香港"}""",
            """{"code":853,"flag":"🇲🇴","name":"中国澳门"}""",
            """{"code":886,"flag":"🇹🇼","name":"中国台湾"}"""
        ).joinToString(",")
        return """{"success":true,"code":200,"message":"","data":[$payload]}"""
    }

    private fun mockSendPhoneVerification(): String =
        successJson("验证码已发送，请注意查收短信")

    private fun mockBindPhone(request: Request): String {
        val fields = request.formFields()
        val phone = fields["phone"].orEmpty()
        if (phone.isBlank()) return failureJson("手机号不能为空")
        val code = fields["code"]?.toIntOrNull() ?: return failureJson("验证码格式错误")
        if (code != 123456) return failureJson("验证码错误，请重新获取")
        return successJson("手机号绑定成功")
    }

    private fun mockUnbindPhone(): String =
        successJson("手机号已解绑")

    private fun mockSendEmailVerification(): String =
        successJson("验证邮件已发送，请查收收件箱")

    private fun mockBindEmail(request: Request): String {
        val fields = request.formFields()
        val email = fields["email"].orEmpty()
        if (email.isBlank()) return failureJson("邮箱地址不能为空")
        val code = fields["randomCode"].orEmpty()
        if (code != "MOCK123") return failureJson("验证码错误或已过期")
        return successJson("邮箱绑定成功")
    }

    private fun mockUnbindEmail(): String =
        successJson("邮箱已解绑")

    private fun mockSubmitFeedback(): String =
        successJson("感谢你的反馈，我们会认真查阅")

    private fun mockDeleteAccount(request: Request): String {
        val fields = request.formFields()
        val password = fields["password"].orEmpty()
        return if (password == "mock_password") {
            successJson("账号注销申请已提交，将在 7 天后生效")
        } else {
            failureJson("密码错误，注销失败")
        }
    }

    private fun successDataJson(data: Any?, message: String = ""): String {
        return JSON.toJSONString(
            linkedMapOf(
                "success" to true,
                "code" to 200,
                "message" to message,
                "data" to data
            )
        )
    }

    private fun successJson(message: String): String {
        return successDataJson(data = null, message = message)
    }

    private fun failureJson(message: String): String =
        """{"success":false,"code":500,"message":"$message","data":null}"""

    private fun Request.itemIdFromPath(): String? {
        val segments = url.pathSegments
        val idIndex = segments.indexOf("id")
        return segments.getOrNull(idIndex + 1)
    }

    private fun Request.pathValueAfter(key: String): String? {
        val segments = url.pathSegments
        val targetIndex = segments.indexOf(key)
        return segments.getOrNull(targetIndex + 1)
    }

    private fun MutableList<MockMarketplaceItemRecord>.findById(id: String?): MockMarketplaceItemRecord? {
        return firstOrNull { it.id.toString() == id }
    }

    private fun MutableList<MockLostFoundItemRecord>.findById(id: String?): MockLostFoundItemRecord? {
        return firstOrNull { it.id.toString() == id }
    }

    private fun MutableList<MockSecretPostRecord>.findById(id: String?): MockSecretPostRecord? {
        return firstOrNull { it.id.toString() == id }
    }

    private fun MutableList<MockExpressPostRecord>.findById(id: String?): MockExpressPostRecord? {
        return firstOrNull { it.id.toString() == id }
    }

    private fun MutableList<MockTopicPostRecord>.findById(id: String?): MockTopicPostRecord? {
        return firstOrNull { it.id.toString() == id }
    }

    private fun MutableList<MockDeliveryOrderRecord>.findById(id: String?): MockDeliveryOrderRecord? {
        return firstOrNull { it.orderId.toString() == id }
    }

    private fun MutableList<MockPhotographPostRecord>.findById(id: String?): MockPhotographPostRecord? {
        return firstOrNull { it.id.toString() == id }
    }

    private fun MockMarketplaceItemRecord.toMarketplaceItemPayload(): Map<String, Any?> {
        return linkedMapOf(
            "id" to id,
            "username" to username,
            "name" to name,
            "description" to description,
            "price" to price,
            "location" to location,
            "type" to type,
            "qq" to qq,
            "phone" to phone,
            "state" to state,
            "publishTime" to publishTime,
            "pictureURL" to pictureURL
        )
    }

    private fun MockMarketplaceProfileRecord.toMarketplaceProfilePayload(): Map<String, Any?> {
        return linkedMapOf(
            "avatarURL" to avatarURL,
            "username" to username,
            "nickname" to nickname,
            "faculty" to faculty,
            "enrollment" to enrollment,
            "major" to major
        )
    }

    private fun MockLostFoundItemRecord.toLostFoundItemPayload(): Map<String, Any?> {
        return linkedMapOf(
            "id" to id,
            "username" to username,
            "name" to name,
            "description" to description,
            "location" to location,
            "itemType" to itemType,
            "lostType" to lostType,
            "qq" to qq,
            "wechat" to wechat,
            "phone" to phone,
            "state" to state,
            "publishTime" to publishTime,
            "pictureURL" to pictureURL
        )
    }

    private fun MockLostFoundProfileRecord.toLostFoundProfilePayload(): Map<String, Any?> {
        return linkedMapOf(
            "avatarURL" to avatarURL,
            "username" to username,
            "nickname" to nickname
        )
    }

    private fun MockSecretPostRecord.toSecretPayload(includeComments: Boolean): Map<String, Any?> {
        return linkedMapOf(
            "id" to id,
            "username" to username,
            "content" to content,
            "theme" to theme,
            "type" to type,
            "timer" to timer,
            "state" to state,
            "publishTime" to publishTime,
            "likeCount" to likeCount,
            "commentCount" to commentCount,
            "liked" to if (liked) 1 else 0,
            "voiceURL" to voiceURL,
            "secretCommentList" to if (includeComments) {
                mockSecretComments
                    .filter { it.contentId == id }
                    .sortedByDescending { it.publishTime }
                    .map { it.toSecretCommentPayload() }
            } else {
                null
            }
        )
    }

    private fun MockSecretCommentRecord.toSecretCommentPayload(): Map<String, Any?> {
        return linkedMapOf(
            "id" to id,
            "contentId" to contentId,
            "username" to username,
            "comment" to comment,
            "publishTime" to publishTime,
            "avatarTheme" to avatarTheme
        )
    }

    private fun MockDatingProfileRecord.toDatingProfilePayload(): Map<String, Any?> {
        return linkedMapOf(
            "profileId" to profileId,
            "username" to username,
            "nickname" to nickname,
            "grade" to grade,
            "faculty" to faculty,
            "hometown" to hometown,
            "content" to content,
            "qq" to qq,
            "wechat" to wechat,
            "area" to area,
            "state" to state,
            "pictureURL" to pictureURL
        )
    }

    private fun MockDatingPickRecord.toDatingPickPayload(): Map<String, Any?> {
        return linkedMapOf(
            "pickId" to pickId,
            "roommateProfile" to profile.toDatingProfilePayload(),
            "username" to username,
            "content" to content,
            "state" to state
        )
    }

    private fun MockExpressPostRecord.toExpressPayload(): Map<String, Any?> {
        return linkedMapOf(
            "id" to id,
            "username" to username,
            "nickname" to nickname,
            "realname" to realname,
            "selfGender" to selfGender,
            "name" to name,
            "content" to content,
            "personGender" to personGender,
            "publishTime" to publishTime,
            "likeCount" to likeCount,
            "liked" to liked,
            "commentCount" to commentCount,
            "guessCount" to guessCount,
            "guessSum" to guessSum,
            "canGuess" to canGuess
        )
    }

    private fun MockExpressCommentRecord.toExpressCommentPayload(): Map<String, Any?> {
        return linkedMapOf(
            "id" to id,
            "username" to username,
            "nickname" to nickname,
            "expressId" to expressId,
            "comment" to comment,
            "publishTime" to publishTime
        )
    }

    private fun MockTopicPostRecord.toTopicPayload(): Map<String, Any?> {
        return linkedMapOf(
            "id" to id,
            "username" to username,
            "topic" to topic,
            "content" to content,
            "count" to count,
            "publishTime" to publishTime,
            "likeCount" to likeCount,
            "liked" to liked,
            "firstImageUrl" to firstImageUrl,
            "imageUrls" to imageUrls
        )
    }

    private fun MockDeliveryOrderRecord.toDeliveryOrderPayload(): Map<String, Any?> {
        return linkedMapOf(
            "orderId" to orderId,
            "username" to username,
            "orderTime" to orderTime,
            "name" to name,
            "number" to number,
            "phone" to phone,
            "price" to price,
            "company" to company,
            "address" to address,
            "state" to state,
            "remarks" to remarks
        )
    }

    private fun MockDeliveryTradeRecord.toDeliveryTradePayload(): Map<String, Any?> {
        return linkedMapOf(
            "tradeId" to tradeId,
            "orderId" to orderId,
            "createTime" to createTime,
            "username" to username,
            "state" to state
        )
    }

    private fun MockPhotographPostRecord.photoCount(): Int {
        return maxOf(count, imageUrls.size, if (firstImageUrl == null) 0 else 1)
    }

    private fun MockPhotographPostRecord.resolvedImageUrls(): List<String> {
        return if (imageUrls.isNotEmpty()) imageUrls else listOfNotNull(firstImageUrl)
    }

    private fun MockPhotographPostRecord.toPhotographPayload(includeComments: Boolean): Map<String, Any?> {
        return linkedMapOf(
            "id" to id,
            "title" to title,
            "content" to content,
            "count" to photoCount(),
            "type" to type,
            "username" to username,
            "createTime" to createTime,
            "likeCount" to likeCount,
            "commentCount" to commentCount,
            "liked" to if (liked) 1 else 0,
            "firstImageUrl" to (firstImageUrl ?: resolvedImageUrls().firstOrNull()),
            "imageUrls" to resolvedImageUrls(),
            "photographCommentList" to if (includeComments) {
                mockPhotographCommentRecords
                    .filter { it.photoId == id }
                    .sortedByDescending { it.createTime }
                    .map { it.toPhotographCommentPayload() }
            } else {
                null
            }
        )
    }

    private fun MockPhotographCommentRecord.toPhotographCommentPayload(): Map<String, Any?> {
        return linkedMapOf(
            "commentId" to commentId,
            "photoId" to photoId,
            "username" to username,
            "nickname" to nickname,
            "comment" to comment,
            "createTime" to createTime
        )
    }

    private fun MockProfileSummaryRecord.toPayload(): Map<String, Any?> {
        return linkedMapOf(
            "username" to username,
            "nickname" to nickname,
            "avatar" to avatar,
            "faculty" to faculty,
            "major" to major,
            "enrollment" to enrollment,
            "location" to location,
            "hometown" to hometown,
            "introduction" to introduction,
            "birthday" to birthday,
            "ipArea" to ipArea,
            "age" to age
        )
    }

    private fun MockProfileLocationRegionRecord.toPayload(): Map<String, Any?> {
        return linkedMapOf(
            "code" to code,
            "name" to name,
            "stateMap" to states.associateBy(MockProfileLocationStateRecord::code) { it.toPayload() }
        )
    }

    private fun MockProfileLocationStateRecord.toPayload(): Map<String, Any?> {
        return linkedMapOf(
            "code" to code,
            "name" to name,
            "cityMap" to cities.associateBy(MockProfileLocationCityRecord::code) { it.toPayload() }
        )
    }

    private fun MockProfileLocationCityRecord.toPayload(): Map<String, Any?> {
        return linkedMapOf(
            "code" to code,
            "name" to name
        )
    }

    private fun escapeJson(value: String?): String {
        return value.orEmpty()
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
    }

    private fun Request.formFields(): Map<String, String> {
        val body = body ?: return emptyMap()
        val buffer = Buffer()
        body.writeTo(buffer)
        return buffer.readUtf8()
            .split('&')
            .filter { it.contains('=') }
            .associate { entry ->
                val (key, value) = entry.split('=', limit = 2)
                URLDecoder.decode(key, "UTF-8") to URLDecoder.decode(value, "UTF-8")
            }
    }

    private fun Request.bodyUtf8(): String {
        val body = body ?: return ""
        val buffer = Buffer()
        body.writeTo(buffer)
        return buffer.readUtf8()
    }

    private fun Request.jsonObjectBody(): JSONObject? {
        val raw = bodyUtf8()
        if (raw.isBlank()) return null
        return runCatching { JSON.parseObject(raw) }.getOrNull()
    }

    private fun locationDisplayNameFromRequest(request: Request): String? {
        val body = request.jsonObjectBody() ?: return null
        val regionCode = body.getString("region")?.trim().orEmpty()
        if (regionCode.isEmpty()) return null
        val stateCode = body.getString("state")?.trim().orEmpty()
        val cityCode = body.getString("city")?.trim().orEmpty()

        val region = mockProfileLocationRegions.firstOrNull { it.code == regionCode }
            ?: return null
        val state = region.states.firstOrNull { it.code == stateCode }
        val city = state?.cities?.firstOrNull { it.code == cityCode }

        return ProfileFormSupport.makeLocationDisplay(
            region = region.name,
            state = state?.name.orEmpty(),
            city = city?.name.orEmpty()
        )
    }

    private fun Request.multipartField(name: String): String? {
        val raw = bodyUtf8()
        if (raw.isBlank()) return null
        val segments = raw.split("--")
        return segments.firstNotNullOfOrNull { segment ->
            if (!segment.contains("name=\"$name\"")) {
                null
            } else {
                segment.substringAfter("\r\n\r\n", "")
                    .substringBeforeLast("\r\n", "")
                    .trim()
                    .takeIf { it.isNotBlank() }
            }
        }
    }

    private fun Request.multipartImageCount(): Int {
        return Regex("name=\"image\\d+\"")
            .findAll(bodyUtf8())
            .count()
    }

    private fun Request.hasMultipartPart(name: String): Boolean {
        return bodyUtf8().contains("name=\"$name\"")
    }

    private companion object {
        data class MockSpareQueryRequest(
            val zone: Int? = 0,
            val type: Int? = 0,
            val startTime: Int? = 1,
            val endTime: Int? = 2
        )

        data class MockGraduateExamQuery(
            val name: String = "",
            val examNumber: String = "",
            val idNumber: String = ""
        )

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

        data class MockProfileSummaryRecord(
            val username: String,
            val nickname: String,
            val avatar: String,
            val faculty: String,
            val major: String,
            val enrollment: String,
            val location: String,
            val hometown: String,
            val introduction: String,
            val birthday: String,
            val ipArea: String,
            val age: Int
        )

        data class MockProfileLocationCityRecord(
            val code: String,
            val name: String
        )

        data class MockProfileLocationStateRecord(
            val code: String,
            val name: String,
            val cities: List<MockProfileLocationCityRecord>
        )

        data class MockProfileLocationRegionRecord(
            val code: String,
            val name: String,
            val states: List<MockProfileLocationStateRecord>
        )

        data class MockSpareRoomRecord(
            val number: String,
            val name: String,
            val classSeating: Int,
            val examSeating: Int
        )

        data class MockYellowPageTypeRecord(
            val typeCode: Int,
            val typeName: String
        )

        data class MockYellowPageEntryRecord(
            val id: Int,
            val typeCode: Int,
            val typeName: String,
            val section: String,
            val campus: String,
            val majorPhone: String,
            val minorPhone: String,
            val address: String,
            val email: String,
            val website: String
        )

        data class MockMarketplaceItemRecord(
            val id: Int,
            val username: String,
            val name: String,
            val description: String,
            val price: String,
            val location: String,
            val type: Int,
            val qq: String,
            val phone: String,
            val state: Int,
            val publishTime: String,
            val pictureURL: List<String>
        )

        data class MockMarketplaceProfileRecord(
            val avatarURL: String,
            val username: String,
            val nickname: String,
            val faculty: Int,
            val enrollment: Int,
            val major: String
        )

        data class MockLostFoundItemRecord(
            val id: Int,
            val username: String,
            val name: String,
            val description: String,
            val location: String,
            val itemType: Int,
            val lostType: Int,
            val qq: String,
            val wechat: String,
            val phone: String,
            val state: Int,
            val publishTime: String,
            val pictureURL: List<String>
        )

        data class MockLostFoundProfileRecord(
            val avatarURL: String,
            val username: String,
            val nickname: String
        )

        data class MockSecretPostRecord(
            val id: Int,
            val username: String,
            val content: String,
            val theme: Int,
            val type: Int,
            val timer: Int,
            val state: Int,
            val publishTime: String,
            val likeCount: Int,
            val commentCount: Int,
            val liked: Boolean,
            val voiceURL: String? = null
        )

        data class MockSecretCommentRecord(
            val id: Int,
            val contentId: Int,
            val username: String,
            val comment: String,
            val publishTime: String,
            val avatarTheme: Int
        )

        data class MockDatingProfileRecord(
            val profileId: Int,
            val username: String,
            val nickname: String,
            val grade: Int,
            val faculty: String,
            val hometown: String,
            val content: String,
            val qq: String,
            val wechat: String,
            val area: Int,
            val state: Int,
            val pictureURL: String = ""
        )

        data class MockDatingPickRecord(
            val pickId: Int,
            val profile: MockDatingProfileRecord,
            val username: String,
            val content: String,
            val state: Int,
            val updatedAt: String
        )

        data class MockExpressPostRecord(
            val id: Int,
            val username: String,
            val nickname: String,
            val realname: String,
            val selfGender: Int,
            val name: String,
            val content: String,
            val personGender: Int,
            val publishTime: String,
            val likeCount: Int,
            val liked: Boolean,
            val commentCount: Int,
            val guessCount: Int,
            val guessSum: Int,
            val canGuess: Boolean
        )

        data class MockExpressCommentRecord(
            val id: Int,
            val username: String,
            val nickname: String,
            val expressId: Int,
            val comment: String,
            val publishTime: String
        )

        data class MockTopicPostRecord(
            val id: Int,
            val username: String,
            val topic: String,
            val content: String,
            val count: Int,
            val publishTime: String,
            val likeCount: Int,
            val liked: Boolean,
            val firstImageUrl: String? = null,
            val imageUrls: List<String> = emptyList()
        )

        data class MockDeliveryOrderRecord(
            val orderId: Int,
            val username: String,
            val orderTime: String,
            val name: String,
            val number: String,
            val phone: String,
            val price: Double,
            val company: String,
            val address: String,
            val state: Int,
            val remarks: String
        )

        data class MockDeliveryTradeRecord(
            val tradeId: Int,
            val orderId: Int,
            val createTime: String,
            val username: String,
            val state: Int
        )

        data class MockPhotographPostRecord(
            val id: Int,
            val title: String,
            val content: String,
            val count: Int,
            val type: Int,
            val username: String,
            val createTime: String,
            val likeCount: Int,
            val commentCount: Int,
            val liked: Boolean,
            val firstImageUrl: String? = null,
            val imageUrls: List<String> = emptyList()
        )

        data class MockPhotographCommentRecord(
            val commentId: Int,
            val photoId: Int,
            val username: String,
            val nickname: String,
            val comment: String,
            val createTime: String
        )

        val mockChargeKeyPair: KeyPair by lazy { ChargeCrypto.generateRsaKeyPair() }
        const val MOCK_CURRENT_USERNAME = "2024001001"
        val spareZones = listOf("白云校区", "龙洞校区", "三水校区", "东莞校区", "外部教学点")
        val spareTypes = listOf("普通课室", "多媒体课室", "机房", "实验室", "阶梯教室", "智慧课室")
        val mockSpareRooms = listOf(
            MockSpareRoomRecord("A301", "实验楼 A301", 80, 60),
            MockSpareRoomRecord("B205", "教学楼 B205", 120, 90),
            MockSpareRoomRecord("C402", "综合楼 C402", 60, 45)
        )
        val mockYellowPageTypes = listOf(
            MockYellowPageTypeRecord(1, "教学服务"),
            MockYellowPageTypeRecord(2, "行政服务"),
            MockYellowPageTypeRecord(3, "后勤服务")
        )
        val mockYellowPageEntries = listOf(
            MockYellowPageEntryRecord(101, 1, "教学服务", "教务处", "白云校区", "020-12345678", "020-87654321", "行政楼 201", "jw@gdeiassistant.cn", "gdeiassistant.cn/jwc"),
            MockYellowPageEntryRecord(102, 1, "教学服务", "图书馆服务台", "白云校区", "020-23456789", "", "图书馆一层服务台", "library@gdeiassistant.cn", "gdeiassistant.cn/library"),
            MockYellowPageEntryRecord(201, 2, "行政服务", "学生工作部", "龙洞校区", "020-34567890", "", "学生事务中心 108", "student@gdeiassistant.cn", "gdeiassistant.cn/student"),
            MockYellowPageEntryRecord(301, 3, "后勤服务", "后勤报修中心", "三水校区", "020-45678901", "020-45678902", "后勤楼 101", "repair@gdeiassistant.cn", "gdeiassistant.cn/repair")
        )
        val mockAnnouncements = listOf(
            MockAnnouncementRecord("1001", "账号安全提醒", "本周将对登录态与设备识别策略进行升级，请留意异常登录提醒。", "2026年03月10日"),
            MockAnnouncementRecord("1002", "校园服务更新", "图书借阅与校园卡模块已完成接口升级，部分页面体验已同步优化。", "2026年03月08日"),
            MockAnnouncementRecord("1003", "系统维护通知", "今晚 23:00 至次日 01:00 进行例行维护，期间部分服务可能出现短暂波动。", "2026年03月06日")
        )
        val mockProfileLocationRegions = ChinaRegionData.regions.map { region ->
            MockProfileLocationRegionRecord(
                code = region,
                name = region,
                states = ChinaRegionData.statesByRegion[region].orEmpty().map { state ->
                    MockProfileLocationStateRecord(
                        code = state,
                        name = state,
                        cities = ChinaRegionData.citiesByState[state].orEmpty().map { city ->
                            MockProfileLocationCityRecord(
                                code = city,
                                name = city
                            )
                        }
                    )
                }
            )
        }
        var mockProfileSummary = MockProfileSummaryRecord(
            username = MOCK_CURRENT_USERNAME,
            nickname = "Mock 同学",
            avatar = "",
            faculty = "计算机科学系",
            major = "软件工程",
            enrollment = "2024",
            location = "中国大陆 广东省 广州市",
            hometown = "中国大陆 广东省 揭阳市",
            introduction = "保持好奇，持续重构，把校园服务做得更顺手。",
            birthday = "2005-04-16",
            ipArea = "广东省",
            age = 20
        )
        val mockMarketplaceProfiles = listOf(
            MockMarketplaceProfileRecord(
                avatarURL = "",
                username = MOCK_CURRENT_USERNAME,
                nickname = "Mock 同学",
                faculty = 12,
                enrollment = 2024,
                major = "软件工程"
            ),
            MockMarketplaceProfileRecord(
                avatarURL = "",
                username = "2023002003",
                nickname = "林学长",
                faculty = 12,
                enrollment = 2023,
                major = "计算机科学与技术"
            ),
            MockMarketplaceProfileRecord(
                avatarURL = "",
                username = "2022003018",
                nickname = "陈同学",
                faculty = 3,
                enrollment = 2022,
                major = "汉语言文学"
            )
        )
        val mockMarketplaceItems = mutableListOf(
            MockMarketplaceItemRecord(
                id = 4101,
                username = MOCK_CURRENT_USERNAME,
                name = "95 新蓝牙耳机",
                description = "日常通勤使用，续航稳定，附原装充电盒和备用耳帽。",
                price = "168.00",
                location = "白云校区图书馆门口",
                type = 4,
                qq = "214365870",
                phone = "13800138000",
                state = 1,
                publishTime = "2026-03-15 18:40",
                pictureURL = listOf("https://picsum.photos/seed/gdei-market-1/960/720")
            ),
            MockMarketplaceItemRecord(
                id = 4102,
                username = "2023002003",
                name = "公路自行车头盔",
                description = "买车时一起入的头盔，尺寸 M，适合日常校内骑行。",
                price = "96.00",
                location = "龙洞校区宿舍区",
                type = 6,
                qq = "33221144",
                phone = "",
                state = 1,
                publishTime = "2026-03-14 21:15",
                pictureURL = emptyList()
            ),
            MockMarketplaceItemRecord(
                id = 4103,
                username = "2022003018",
                name = "高数教材与笔记",
                description = "教材九成新，附期末整理笔记和章节错题总结，适合补基础。",
                price = "28.00",
                location = "白云校区教学楼 A 栋",
                type = 8,
                qq = "87651234",
                phone = "",
                state = 1,
                publishTime = "2026-03-13 11:05",
                pictureURL = listOf("https://picsum.photos/seed/gdei-market-2/960/720")
            ),
            MockMarketplaceItemRecord(
                id = 4104,
                username = MOCK_CURRENT_USERNAME,
                name = "折叠宿舍小桌",
                description = "搬宿舍后闲置，桌面无明显磕碰，适合床上学习和放平板。",
                price = "45.00",
                location = "三水校区生活区",
                type = 11,
                qq = "214365870",
                phone = "13800138000",
                state = 2,
                publishTime = "2026-03-11 16:20",
                pictureURL = emptyList()
            ),
            MockMarketplaceItemRecord(
                id = 4105,
                username = MOCK_CURRENT_USERNAME,
                name = "平板保护壳",
                description = "尺寸不匹配所以闲置，适合 10.9 寸设备，边角无破损。",
                price = "20.00",
                location = "校内快递站附近",
                type = 3,
                qq = "214365870",
                phone = "",
                state = 0,
                publishTime = "2026-03-09 13:50",
                pictureURL = listOf("https://picsum.photos/seed/gdei-market-3/960/720")
            )
        )
        val mockLostFoundProfiles = listOf(
            MockLostFoundProfileRecord("", MOCK_CURRENT_USERNAME, "Mock 同学"),
            MockLostFoundProfileRecord("", "2023002003", "林学长"),
            MockLostFoundProfileRecord("", "2022003018", "陈同学")
        )
        val mockLostFoundItems = mutableListOf(
            MockLostFoundItemRecord(
                id = 5101,
                username = MOCK_CURRENT_USERNAME,
                name = "遗失黑色校园卡卡套",
                description = "内含校园卡和一张图书馆借阅卡，今天中午在食堂附近丢失。",
                location = "白云校区第一食堂",
                itemType = 1,
                lostType = 0,
                qq = "214365870",
                wechat = "mock-student",
                phone = "13800138000",
                state = 0,
                publishTime = "2026-03-15 12:35",
                pictureURL = listOf("https://picsum.photos/seed/gdei-lost-1/960/720")
            ),
            MockLostFoundItemRecord(
                id = 5102,
                username = "2023002003",
                name = "拾到一把宿舍钥匙",
                description = "钥匙圈上有蓝色挂件，已先放在宿管阿姨处，可凭特征领取。",
                location = "龙洞校区 6 栋楼下",
                itemType = 5,
                lostType = 1,
                qq = "33221144",
                wechat = "",
                phone = "",
                state = 0,
                publishTime = "2026-03-14 20:10",
                pictureURL = emptyList()
            ),
            MockLostFoundItemRecord(
                id = 5103,
                username = MOCK_CURRENT_USERNAME,
                name = "寻找灰色水杯",
                description = "杯身贴有课程表贴纸，可能落在综合楼自习室或图书馆。",
                location = "综合楼 / 图书馆",
                itemType = 11,
                lostType = 0,
                qq = "214365870",
                wechat = "mock-student",
                phone = "",
                state = 1,
                publishTime = "2026-03-10 09:30",
                pictureURL = emptyList()
            ),
            MockLostFoundItemRecord(
                id = 5104,
                username = MOCK_CURRENT_USERNAME,
                name = "拾到学生证一本",
                description = "在操场看台附近拾到学生证，封皮略旧，请失主联系认领。",
                location = "白云校区操场看台",
                itemType = 2,
                lostType = 1,
                qq = "214365870",
                wechat = "",
                phone = "13800138000",
                state = 0,
                publishTime = "2026-03-13 17:20",
                pictureURL = listOf("https://picsum.photos/seed/gdei-lost-2/960/720")
            )
        )
        val mockSecretPosts = mutableListOf(
            MockSecretPostRecord(
                id = 6101,
                username = "匿名同学 A",
                content = "最近在赶课程设计，虽然每天都很忙，但在图书馆看到晚霞的时候还是会觉得这学期没白熬。",
                theme = 1,
                type = 0,
                timer = 0,
                state = 0,
                publishTime = "2026-03-15 20:30",
                likeCount = 12,
                commentCount = 2,
                liked = false
            ),
            MockSecretPostRecord(
                id = 6102,
                username = MOCK_CURRENT_USERNAME,
                content = "如果有一天真的能把校园里这些分散的服务都整理好，应该会很有成就感吧。",
                theme = 4,
                type = 0,
                timer = 1,
                state = 1,
                publishTime = "2026-03-14 23:10",
                likeCount = 18,
                commentCount = 1,
                liked = true
            ),
            MockSecretPostRecord(
                id = 6103,
                username = "匿名同学 B",
                content = "这是一条语音树洞，点进去可以听到完整内容。",
                theme = 7,
                type = 1,
                timer = 0,
                state = 0,
                publishTime = "2026-03-13 18:05",
                likeCount = 7,
                commentCount = 0,
                liked = false,
                voiceURL = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
            )
        )
        val mockSecretComments = mutableListOf(
            MockSecretCommentRecord(7101, 6101, "匿名同学 C", "晚霞真的很治愈，辛苦啦。", "2026-03-15 21:00", 3),
            MockSecretCommentRecord(7102, 6101, "匿名同学 D", "加油，课程设计做完一定会很有成就感。", "2026-03-15 21:18", 5),
            MockSecretCommentRecord(7103, 6102, "匿名同学 E", "会的，现在已经在慢慢发生了。", "2026-03-15 00:08", 2)
        )
        val mockDatingProfiles = mutableListOf(
            MockDatingProfileRecord(
                profileId = 8101,
                username = MOCK_CURRENT_USERNAME,
                nickname = "我自己",
                grade = 3,
                faculty = "计算机科学",
                hometown = "广州",
                content = "能聊天、会运动、作息稳定。",
                qq = "214365870",
                wechat = "mock-student",
                area = 1,
                state = 1
            ),
            MockDatingProfileRecord(
                profileId = 8102,
                username = "2023002003",
                nickname = "晴天",
                grade = 2,
                faculty = "英语教育",
                hometown = "佛山",
                content = "喜欢夜跑和音乐节。",
                qq = "55667788",
                wechat = "sunny-run",
                area = 0,
                state = 1
            ),
            MockDatingProfileRecord(
                profileId = 8103,
                username = "2022003018",
                nickname = "晚风",
                grade = 4,
                faculty = "汉语言文学",
                hometown = "深圳",
                content = "周末常去图书馆和咖啡馆。",
                qq = "88990011",
                wechat = "wanfeng_reading",
                area = 0,
                state = 1
            )
        )
        val mockDatingReceivedPicks = mutableListOf(
            MockDatingPickRecord(
                pickId = 9101,
                profile = mockDatingProfiles[1],
                username = "2023002003",
                content = "看你也喜欢夜跑，想认识一下。",
                state = 0,
                updatedAt = "2026-03-15 22:00"
            )
        )
        val mockDatingSentPicks = mutableListOf(
            MockDatingPickRecord(
                pickId = 9102,
                profile = mockDatingProfiles[2],
                username = MOCK_CURRENT_USERNAME,
                content = "周末一起去图书馆吗？",
                state = 1,
                updatedAt = "2026-03-14 19:20"
            )
        )
        val mockExpressPosts = mutableListOf(
            MockExpressPostRecord(
                id = 10101,
                username = "2023002003",
                nickname = "晴天",
                realname = "陈晴",
                selfGender = 1,
                name = "阿哲",
                content = "昨晚在操场散步的时候又遇到你了，想认真和你说一句：你笑起来的时候真的会让人记很久。",
                personGender = 0,
                publishTime = "2026-03-15 21:45",
                likeCount = 34,
                liked = false,
                commentCount = 2,
                guessCount = 1,
                guessSum = 6,
                canGuess = true
            ),
            MockExpressPostRecord(
                id = 10102,
                username = MOCK_CURRENT_USERNAME,
                nickname = "Mock 同学",
                realname = "李思怡",
                selfGender = 2,
                name = "图书馆四楼靠窗的你",
                content = "谢谢你上周把落下的耳机递给我，也谢谢你顺手提醒我别忘了借书。虽然不知道你叫什么，但还是想把这份好感留在这里。",
                personGender = 2,
                publishTime = "2026-03-14 18:20",
                likeCount = 19,
                liked = true,
                commentCount = 3,
                guessCount = 0,
                guessSum = 0,
                canGuess = false
            ),
            MockExpressPostRecord(
                id = 10103,
                username = "2022003018",
                nickname = "晚风",
                realname = "周岚",
                selfGender = 0,
                name = "小周",
                content = "你总是在早八前把实验室门口的灯打开，大家都还没完全醒的时候，你已经准备好一整天了。",
                personGender = 1,
                publishTime = "2026-03-13 08:10",
                likeCount = 11,
                liked = false,
                commentCount = 1,
                guessCount = 0,
                guessSum = 2,
                canGuess = true
            )
        )
        val mockExpressComments = mutableListOf(
            MockExpressCommentRecord(11101, "2023002005", "晚课同学", 10101, "这条好真诚，希望你能被看到。", "2026-03-15 22:03"),
            MockExpressCommentRecord(11102, "2022003018", "隔壁班同学", 10101, "操场的故事总是很浪漫。", "2026-03-15 22:26"),
            MockExpressCommentRecord(11103, "2023002003", "小林", 10102, "图书馆真的很容易发生一些温柔的小事。", "2026-03-14 19:10"),
            MockExpressCommentRecord(11104, MOCK_CURRENT_USERNAME, "Mock 同学", 10102, "如果真的再遇见，也许可以主动打个招呼。", "2026-03-14 20:02"),
            MockExpressCommentRecord(11105, "2023002019", "理工楼同学", 10102, "这条让我想起很多校园瞬间。", "2026-03-14 21:15")
        )
        val mockTopicPosts = mutableListOf(
            MockTopicPostRecord(
                id = 12101,
                username = "2023002003",
                topic = "春招准备",
                content = "最近大家都在怎么准备春招面试？除了简历和项目复盘，我感觉表达节奏也挺重要，想看看有没有人总结过面试时最容易卡住的点。",
                count = 2,
                publishTime = "2026-03-15 20:05",
                likeCount = 27,
                liked = false,
                firstImageUrl = "https://picsum.photos/seed/gdei-topic-1/960/720",
                imageUrls = listOf(
                    "https://picsum.photos/seed/gdei-topic-1/960/720",
                    "https://picsum.photos/seed/gdei-topic-1b/960/720"
                )
            ),
            MockTopicPostRecord(
                id = 12102,
                username = MOCK_CURRENT_USERNAME,
                topic = "教室查询",
                content = "新版教室查询你们更喜欢按时间段查还是按校区先筛？这两种方式我都试了几轮，感觉移动端入口可以再更直接一点。",
                count = 0,
                publishTime = "2026-03-14 16:40",
                likeCount = 15,
                liked = true
            ),
            MockTopicPostRecord(
                id = 12103,
                username = "2022003018",
                topic = "图书馆自习",
                content = "最近四楼靠窗区域人越来越多了，大家有没有自己固定的自习位？如果换楼层，你们更看重安静还是插座方便？",
                count = 1,
                publishTime = "2026-03-13 19:18",
                likeCount = 9,
                liked = false,
                firstImageUrl = "https://picsum.photos/seed/gdei-topic-2/960/720",
                imageUrls = listOf("https://picsum.photos/seed/gdei-topic-2/960/720")
            )
        )
        val mockDeliveryOrderRecords = mutableListOf(
            MockDeliveryOrderRecord(
                orderId = 13101,
                username = "2023002003",
                orderTime = "2026-03-15 19:40",
                name = "取件任务",
                number = "A0831",
                phone = "13800131001",
                price = 4.0,
                company = "白云校区快递驿站",
                address = "12 栋 402",
                state = 0,
                remarks = "到楼下给我发消息就好。"
            ),
            MockDeliveryOrderRecord(
                orderId = 13102,
                username = MOCK_CURRENT_USERNAME,
                orderTime = "2026-03-14 17:20",
                name = "取件任务",
                number = "SF2309",
                phone = "13800138000",
                price = 3.5,
                company = "龙洞校区菜鸟站",
                address = "8 栋 215",
                state = 1,
                remarks = "晚上 8 点后方便。"
            ),
            MockDeliveryOrderRecord(
                orderId = 13103,
                username = "2022003018",
                orderTime = "2026-03-13 12:10",
                name = "取件任务",
                number = "YT5620",
                phone = "13800132018",
                price = 5.0,
                company = "白云校区南门快递点",
                address = "图书馆门口",
                state = 2,
                remarks = "帮忙带一份冰美式更好。"
            ),
            MockDeliveryOrderRecord(
                orderId = 13104,
                username = MOCK_CURRENT_USERNAME,
                orderTime = "2026-03-12 15:55",
                name = "取件任务",
                number = "JD7788",
                phone = "13800138000",
                price = 2.5,
                company = "三水校区驿站",
                address = "7 栋大厅",
                state = 2,
                remarks = ""
            )
        )
        val mockDeliveryTrades = mutableListOf(
            MockDeliveryTradeRecord(
                tradeId = 13201,
                orderId = 13102,
                createTime = "2026-03-14 17:35",
                username = "2023002005",
                state = 0
            ),
            MockDeliveryTradeRecord(
                tradeId = 13202,
                orderId = 13103,
                createTime = "2026-03-13 12:22",
                username = MOCK_CURRENT_USERNAME,
                state = 1
            ),
            MockDeliveryTradeRecord(
                tradeId = 13203,
                orderId = 13104,
                createTime = "2026-03-12 16:10",
                username = "2023002003",
                state = 1
            )
        )
        val mockPhotographPostRecords = mutableListOf(
            MockPhotographPostRecord(
                id = 14101,
                title = "雨后教学楼的光",
                content = "下课后回头看了一眼，玻璃幕墙把晚霞和树影一起收进去了，顺手拍了两张。",
                count = 2,
                type = 0,
                username = "2023002003",
                createTime = "2026-03-15 18:12",
                likeCount = 21,
                commentCount = 2,
                liked = false,
                firstImageUrl = "https://picsum.photos/seed/gdei-photo-1/960/720",
                imageUrls = listOf(
                    "https://picsum.photos/seed/gdei-photo-1/960/720",
                    "https://picsum.photos/seed/gdei-photo-1b/960/720"
                )
            ),
            MockPhotographPostRecord(
                id = 14102,
                title = "食堂窗口前的人间烟火",
                content = "晚饭时间总是最有校园生活感的一刻，大家一边排队一边讨论今晚的作业。",
                count = 1,
                type = 1,
                username = MOCK_CURRENT_USERNAME,
                createTime = "2026-03-14 19:05",
                likeCount = 14,
                commentCount = 1,
                liked = true,
                firstImageUrl = "https://picsum.photos/seed/gdei-photo-2/960/720",
                imageUrls = listOf("https://picsum.photos/seed/gdei-photo-2/960/720")
            ),
            MockPhotographPostRecord(
                id = 14103,
                title = "图书馆四楼窗边",
                content = "复习周前夕，靠窗的位置总是很快坐满，但光线真的很好。",
                count = 3,
                type = 0,
                username = "2022003018",
                createTime = "2026-03-13 16:30",
                likeCount = 9,
                commentCount = 0,
                liked = false,
                firstImageUrl = "https://picsum.photos/seed/gdei-photo-3/960/720",
                imageUrls = listOf(
                    "https://picsum.photos/seed/gdei-photo-3/960/720",
                    "https://picsum.photos/seed/gdei-photo-3b/960/720",
                    "https://picsum.photos/seed/gdei-photo-3c/960/720"
                )
            )
        )
        val mockPhotographCommentRecords = mutableListOf(
            MockPhotographCommentRecord(14201, 14101, "2023002019", "实验楼同学", "这一组光影真的很好看。", "2026-03-15 18:36"),
            MockPhotographCommentRecord(14202, 14101, MOCK_CURRENT_USERNAME, "Mock 同学", "第二张的层次感特别舒服。", "2026-03-15 19:02"),
            MockPhotographCommentRecord(14203, 14102, "2023002005", "食堂搭子", "这张一下就把下课后的氛围拍出来了。", "2026-03-14 19:40")
        )
        val mockInteractionMessages = mutableListOf(
            MockInteractionRecord("msg_001", "secret", "comment", "你收到一条新的评论", "有同学回复了你发布的树洞内容。", "2026-03-10 09:20", false, "comment", "6102"),
            MockInteractionRecord("msg_002", "lostandfound", "like", "失物招领条目收到点赞", "你发布的失物招领信息获得了新的关注。", "2026-03-09 18:45", false, "like", "5101"),
            MockInteractionRecord("msg_003", "secondhand", "comment", "二手商品收到留言", "有人询问你发布商品的成色与自提时间。", "2026-03-09 12:10", true, "comment", "4101"),
            MockInteractionRecord("msg_004", "topic", "like", "话题动态更新", "你参与的话题有了新的点赞记录。", "2026-03-08 21:30", false, "like", "12102"),
            MockInteractionRecord("msg_005", "express", "comment", "表白墙收到留言", "有人给你发布的表白留下了新的评论。", "2026-03-15 23:10", false, "comment", "10102"),
            MockInteractionRecord("msg_006", "delivery", "accept", "全民快递有新进展", "你发布的快递订单已经被接单，记得留意配送动态。", "2026-03-15 21:15", false, "trade", "13104"),
            MockInteractionRecord("msg_007", "photograph", "like", "拍好校园收到点赞", "你上传的校园照片刚刚获得了一次新的点赞。", "2026-03-15 20:48", true, "like", "14102")
        )
        var mockUserDataExportState: Int = 0

        data class MockPrivacyRecord(
            val facultyOpen: Boolean = true,
            val majorOpen: Boolean = true,
            val locationOpen: Boolean = true,
            val hometownOpen: Boolean = true,
            val introductionOpen: Boolean = true,
            val enrollmentOpen: Boolean = true,
            val ageOpen: Boolean = false,
            val cacheAllow: Boolean = true,
            val robotsIndexAllow: Boolean = true
        )
        var mockPrivacySettings = MockPrivacyRecord()

        data class MockLoginRecordItem(
            val id: Int,
            val ip: String,
            val area: String,
            val country: String,
            val province: String,
            val city: String,
            val network: String,
            val time: String
        )
        val mockLoginRecordList = listOf(
            MockLoginRecordItem(1, "120.85.12.34", "广东省广州市", "中国", "广东省", "广州市", "中国电信", "2026-03-19 10:32:15"),
            MockLoginRecordItem(2, "120.85.12.34", "广东省广州市", "中国", "广东省", "广州市", "中国电信", "2026-03-18 22:10:04"),
            MockLoginRecordItem(3, "223.104.6.78", "广东省广州市", "中国", "广东省", "广州市", "中国移动", "2026-03-17 08:55:30"),
            MockLoginRecordItem(4, "114.247.9.12", "北京市", "中国", "北京市", "北京市", "中国联通", "2026-03-10 16:40:22"),
            MockLoginRecordItem(5, "120.85.12.34", "广东省广州市", "中国", "广东省", "广州市", "中国电信", "2026-03-05 09:18:47")
        )

        const val MOCK_CAPTCHA_BASE64 =
            "iVBORw0KGgoAAAANSUhEUgAAAMAAAAA8CAIAAACVO0mNAAAA/0lEQVR4nO3TsQ3DMBQFQdP9d7aBkYzWxExA0RduCMsZ/HXXIQK5vCk1vZ1tc665fb3e8DqgJoCaAmoCqAmgJoCaAmoCqAmgJoCaAmoCqAmgJoCaAmoCqAmgJoCaAmoCqAmgJoCaAmoCqAmgJoCaAmoCqAmgJoCaAmoCqAmgJoCaAmoCqAmgJoCaAmoCqAmgJoCaAmoCqAmgJoCaAmoCqAmgJoCaAmoCqAmgJoCaAmoCqAmgJoCaAmoCqAmgJoCaAmoCqAmgJoCaAmoCqAmgJoCaAmoCqAmgJoCaAmoCqAmgJoCaAmoCqAmgJoCaAmoCqAmgJqBv+B8C9ZV8o8AAAAASUVORK5CYII="
    }
}
