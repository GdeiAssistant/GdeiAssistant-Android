package cn.gdeiassistant.network.mock

import cn.gdeiassistant.model.AppLocaleSupport
import cn.gdeiassistant.model.LocalizedProfileCatalog
import cn.gdeiassistant.model.ProfileFormSupport
import cn.gdeiassistant.model.ProfileLocationCatalog
import cn.gdeiassistant.network.mock.MockUtils.formFields
import cn.gdeiassistant.network.mock.MockUtils.getBoolean
import cn.gdeiassistant.network.mock.MockUtils.getInteger
import cn.gdeiassistant.network.mock.MockUtils.getString
import cn.gdeiassistant.network.mock.MockUtils.hasMultipartPart
import cn.gdeiassistant.network.mock.MockUtils.jsonObjectBody
import okhttp3.Request

/** Mock provider for user profile, privacy, account management endpoints. */
object MockProfileProvider {

    // ── Data classes ────────────────────────────────────────────────────────

    data class MockProfileSummaryRecord(
        val username: String,
        val nickname: String,
        val avatar: String,
        val faculty: String,
        val facultyCode: Int,
        val major: String,
        val majorCode: String,
        val enrollment: String,
        val location: String,
        val locationRegion: String,
        val locationState: String,
        val locationCity: String,
        val hometown: String,
        val hometownRegion: String,
        val hometownState: String,
        val hometownCity: String,
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

    // ── Mock data ───────────────────────────────────────────────────────────

    val mockProfileLocationRegions = ProfileLocationCatalog.regions.map { region ->
        MockProfileLocationRegionRecord(
            code = region.code,
            name = region.name,
            states = region.states.map { state ->
                MockProfileLocationStateRecord(
                    code = state.code,
                    name = state.name,
                    cities = state.cities.map { city ->
                        MockProfileLocationCityRecord(
                            code = city.code,
                            name = city.name
                        )
                    }
                )
            }
        )
    }

    private val baseMockProfileSummary = MockProfileSummaryRecord(
        username = MockUtils.MOCK_CURRENT_USERNAME,
        nickname = MockUtils.MOCK_PROFILE_NICKNAME,
        avatar = "",
        faculty = "计算机科学系",
        facultyCode = 11,
        major = "软件工程",
        majorCode = "software_engineering",
        enrollment = "2023",
        location = "中国 广东 广州",
        locationRegion = "CN",
        locationState = "44",
        locationCity = "1",
        hometown = "中国 广东 汕头",
        hometownRegion = "CN",
        hometownState = "44",
        hometownCity = "5",
        introduction = "喜欢做实用的小工具，也在准备 iOS 开发实习。",
        birthday = "2004-09-16",
        ipArea = "广东",
        age = 21
    )

    var mockProfileSummary = baseMockProfileSummary

    var mockPrivacySettings = MockPrivacyRecord()

    var mockUserDataExportState: Int = 0

    val mockLoginRecordList = listOf(
        MockLoginRecordItem(1, "120.85.12.34", "广东省广州市", "中国", "广东省", "广州市", "中国电信", "2026-03-19 10:32:15"),
        MockLoginRecordItem(2, "120.85.12.34", "广东省广州市", "中国", "广东省", "广州市", "中国电信", "2026-03-18 22:10:04"),
        MockLoginRecordItem(3, "223.104.6.78", "广东省广州市", "中国", "广东省", "广州市", "中国移动", "2026-03-17 08:55:30"),
        MockLoginRecordItem(4, "114.247.9.12", "北京市", "中国", "北京市", "北京市", "中国联通", "2026-03-10 16:40:22"),
        MockLoginRecordItem(5, "120.85.12.34", "广东省广州市", "中国", "广东省", "广州市", "中国电信", "2026-03-05 09:18:47")
    )

    // ── Helpers ──────────────────────────────────────────────────────────────

    fun currentMockNickname(): String {
        return mockProfileSummary.nickname.trim().ifBlank { MockUtils.MOCK_PROFILE_NICKNAME }
    }

    fun currentMockAnonymousName(): String {
        return localizedMockText(
            simplifiedChinese = "匿名同学 F",
            traditionalChinese = "匿名同學 F",
            english = "Anonymous Student F",
            japanese = "匿名の学生 F",
            korean = "익명 학생 F",
            locale = AppLocaleSupport.currentLocale()
        )
    }

    private fun MockProfileSummaryRecord.toPayload(): Map<String, Any?> {
        return linkedMapOf(
            "username" to username,
            "nickname" to nickname,
            "avatar" to avatar,
            "facultyCode" to facultyCode,
            "majorCode" to majorCode,
            "enrollment" to enrollment,
            "location" to linkedMapOf(
                "regionCode" to locationRegion,
                "stateCode" to locationState,
                "cityCode" to locationCity
            ),
            "hometown" to linkedMapOf(
                "regionCode" to hometownRegion,
                "stateCode" to hometownState,
                "cityCode" to hometownCity
            ),
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

    // ── Mock endpoints ──────────────────────────────────────────────────────

    fun mockUserProfile(request: Request): String {
        val locale = request.requestLocale()
        return MockUtils.successDataJson(
            data = localizedProfileSummary(locale).toPayload(),
            message = "success"
        )
    }

    fun mockLocationList(request: Request): String {
        return MockUtils.successDataJson(
            data = mockProfileLocationRegions.map { it.toPayload() },
            message = "success"
        )
    }

    fun mockProfileOptions(request: Request): String {
        val options = LocalizedProfileCatalog.catalogForLocale(request.requestLocale()).defaultOptions
        val faculties = options.faculties.map { option ->
            linkedMapOf(
                "code" to option.code,
                "majors" to option.majors.map { major -> major.code }
            )
        }
        val marketplaceItemTypes = options.marketplaceItemTypes.map { option -> option.code }
        val lostFoundItemTypes = options.lostFoundItemTypes.map { option -> option.code }
        val lostFoundModes = options.lostFoundModes.map { option -> option.code }
        return MockUtils.successDataJson(
            data = linkedMapOf(
                "faculties" to faculties,
                "marketplaceItemTypes" to marketplaceItemTypes,
                "lostFoundItemTypes" to lostFoundItemTypes,
                "lostFoundModes" to lostFoundModes
            ),
            message = "success"
        )
    }

    fun mockAvatarState(request: Request): String {
        val avatar = mockProfileSummary.avatar.trim().takeIf(String::isNotEmpty)
        return MockUtils.successDataJson(
            data = avatar,
            message = "success"
        )
    }

    fun mockUploadAvatar(request: Request): String {
        val hasAvatar = request.hasMultipartPart("avatar")
        val hasAvatarHd = request.hasMultipartPart("avatar_hd")
        if (!hasAvatar || !hasAvatarHd) {
            return MockUtils.failureJson("缺少头像文件")
        }
        val seed = System.currentTimeMillis()
        mockProfileSummary = mockProfileSummary.copy(
            avatar = "https://picsum.photos/seed/gdei-avatar-$seed/720/720"
        )
        return MockUtils.successJson("头像已更新")
    }

    fun mockDeleteAvatar(request: Request): String {
        mockProfileSummary = mockProfileSummary.copy(avatar = "")
        return MockUtils.successJson("已恢复默认头像")
    }

    fun mockUpdateNickname(request: Request): String {
        val nickname = request.jsonObjectBody()?.getString("nickname")?.trim()
            ?: return MockUtils.failureJson("缺少昵称")
        if (nickname.isEmpty()) return MockUtils.failureJson("昵称不能为空")
        mockProfileSummary = mockProfileSummary.copy(nickname = nickname)
        return MockUtils.successJson("已保存")
    }

    fun mockUpdateBirthday(request: Request): String {
        val body = request.jsonObjectBody() ?: return MockUtils.failureJson("缺少生日信息")
        val year = body.getInteger("year") ?: return MockUtils.failureJson("缺少年份")
        val month = body.getInteger("month") ?: return MockUtils.failureJson("缺少月份")
        val date = body.getInteger("date") ?: return MockUtils.failureJson("缺少日期")
        mockProfileSummary = mockProfileSummary.copy(
            birthday = "%04d-%02d-%02d".format(year, month, date)
        )
        return MockUtils.successJson("已保存")
    }

    fun mockUpdateFaculty(request: Request): String {
        val facultyCode = request.jsonObjectBody()?.getInteger("faculty")
            ?: return MockUtils.failureJson("缺少院系信息")
        val locale = request.requestLocale()
        val options = LocalizedProfileCatalog.catalogForLocale(locale).defaultOptions
        val facultyName = options.facultyNameFor(facultyCode) ?: return MockUtils.failureJson("院系信息不存在")
        val majorOptions = options.majorOptionsFor(facultyName)
        mockProfileSummary = mockProfileSummary.copy(
            faculty = if (facultyName == ProfileFormSupport.UnselectedOption) "" else facultyName,
            facultyCode = facultyCode,
            major = mockProfileSummary.major.takeIf { majorOptions.contains(it) } ?: "",
            majorCode = mockProfileSummary.majorCode.takeIf { code ->
                options.majorLabelFor(facultyName, code)?.let(majorOptions::contains) == true
            } ?: ""
        )
        return MockUtils.successJson("已保存")
    }

    fun mockUpdateMajor(request: Request): String {
        val majorCode = request.jsonObjectBody()?.getString("major")?.trim()
            ?: return MockUtils.failureJson("缺少专业信息")
        val locale = request.requestLocale()
        val options = LocalizedProfileCatalog.catalogForLocale(locale).defaultOptions
        val currentFacultyLabel = options.facultyNameFor(mockProfileSummary.facultyCode).orEmpty()
        val majorLabel = options
            .majorLabelFor(currentFacultyLabel, majorCode)
            ?: return MockUtils.failureJson("专业信息不存在")
        mockProfileSummary = mockProfileSummary.copy(
            major = majorLabel,
            majorCode = majorCode
        )
        return MockUtils.successJson("已保存")
    }

    private fun localizedProfileSummary(locale: String): MockProfileSummaryRecord {
        val options = LocalizedProfileCatalog.catalogForLocale(locale).defaultOptions
        val facultyLabel = options.facultyNameFor(mockProfileSummary.facultyCode).orEmpty()
        val majorLabel = options.majorLabelFor(facultyLabel, mockProfileSummary.majorCode).orEmpty()
        return mockProfileSummary.copy(
            faculty = facultyLabel,
            major = majorLabel,
            nickname = localizedMockText(
                simplifiedChinese = MockUtils.MOCK_PROFILE_NICKNAME,
                traditionalChinese = "林知遠",
                english = "Lin Zhiyuan",
                japanese = "リン・ジーユエン",
                korean = "린즈위안",
                locale = locale
            ),
            location = localizedMockText(
                simplifiedChinese = "中国 广东 广州",
                traditionalChinese = "中國 廣東 廣州",
                english = "China Guangdong Guangzhou",
                japanese = "中国 広東 広州",
                korean = "중국 광둥 광저우",
                locale = locale
            ),
            hometown = localizedMockText(
                simplifiedChinese = "中国 广东 汕头",
                traditionalChinese = "中國 廣東 汕頭",
                english = "China Guangdong Shantou",
                japanese = "中国 広東 汕頭",
                korean = "중국 광둥 산터우",
                locale = locale
            ),
            introduction = localizedMockText(
                simplifiedChinese = "喜欢做实用的小工具，也在准备 iOS 开发实习。",
                traditionalChinese = "喜歡做實用的小工具，也在準備 iOS 開發實習。",
                english = "Enjoys building practical tools and is preparing for an iOS development internship.",
                japanese = "実用的な小さなツールを作るのが好きで、iOS 開発インターンの準備もしています。",
                korean = "실용적인 작은 도구를 만드는 것을 좋아하고, iOS 개발 인턴을 준비하고 있습니다.",
                locale = locale
            ),
            ipArea = localizedMockText(
                simplifiedChinese = "广东",
                traditionalChinese = "廣東",
                english = "Guangdong",
                japanese = "広東",
                korean = "광둥",
                locale = locale
            )
        )
    }

    private fun Request.requestLocale(): String {
        return AppLocaleSupport.normalizeLocale(header("Accept-Language"))
    }

    private fun localizedMockText(
        simplifiedChinese: String,
        traditionalChinese: String,
        english: String,
        japanese: String,
        korean: String,
        locale: String
    ): String {
        return when (AppLocaleSupport.normalizeLocale(locale)) {
            "zh-HK", "zh-TW" -> traditionalChinese
            "en" -> english
            "ja" -> japanese
            "ko" -> korean
            else -> simplifiedChinese
        }
    }

    fun mockUpdateEnrollment(request: Request): String {
        val year = request.jsonObjectBody()?.getInteger("year")
        mockProfileSummary = mockProfileSummary.copy(enrollment = year?.toString().orEmpty())
        return MockUtils.successJson("已保存")
    }

    fun mockUpdateLocation(request: Request): String {
        val displayName = locationDisplayNameFromRequest(request)
            ?: return MockUtils.failureJson("缺少所在地信息")
        val body = request.jsonObjectBody()
        mockProfileSummary = mockProfileSummary.copy(location = displayName)
            .copy(
                locationRegion = body?.getString("region")?.trim().orEmpty(),
                locationState = body?.getString("state")?.trim().orEmpty(),
                locationCity = body?.getString("city")?.trim().orEmpty()
            )
        return MockUtils.successJson("已保存")
    }

    fun mockUpdateHometown(request: Request): String {
        val displayName = locationDisplayNameFromRequest(request)
            ?: return MockUtils.failureJson("缺少家乡信息")
        val body = request.jsonObjectBody()
        mockProfileSummary = mockProfileSummary.copy(hometown = displayName)
            .copy(
                hometownRegion = body?.getString("region")?.trim().orEmpty(),
                hometownState = body?.getString("state")?.trim().orEmpty(),
                hometownCity = body?.getString("city")?.trim().orEmpty()
            )
        return MockUtils.successJson("已保存")
    }

    fun mockUpdateIntroduction(request: Request): String {
        val introduction = request.jsonObjectBody()?.getString("introduction")?.trim().orEmpty()
        mockProfileSummary = mockProfileSummary.copy(introduction = introduction)
        return MockUtils.successJson("已保存")
    }

    fun mockGetPrivacySettings(request: Request): String {
        val s = mockPrivacySettings
        return """{"success":true,"code":200,"message":"","data":{
            "facultyOpen":${s.facultyOpen},"majorOpen":${s.majorOpen},
            "locationOpen":${s.locationOpen},"hometownOpen":${s.hometownOpen},
            "introductionOpen":${s.introductionOpen},"enrollmentOpen":${s.enrollmentOpen},
            "ageOpen":${s.ageOpen},"cacheAllow":${s.cacheAllow},
            "robotsIndexAllow":${s.robotsIndexAllow}
        }}""".trimIndent()
    }

    fun mockUpdatePrivacySettings(request: Request): String {
        val body = request.jsonObjectBody() ?: return MockUtils.failureJson("请求体格式错误")
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
        return MockUtils.successJson("隐私设置已更新")
    }

    fun mockLoginRecords(request: Request): String {
        val segments = request.url.pathSegments
        val startIdx = segments.indexOf("start")
        val start = segments.getOrNull(startIdx + 1)?.toIntOrNull() ?: 0
        val sizeIdx = segments.indexOf("size")
        val size = segments.getOrNull(sizeIdx + 1)?.toIntOrNull()?.coerceIn(1, 20) ?: 10
        val paged = mockLoginRecordList.drop(start).take(size)
        val payload = paged.joinToString(",") { r ->
            """{"id":${r.id},"ip":"${r.ip}","area":"${MockUtils.escapeJson(r.area)}","country":"${MockUtils.escapeJson(r.country)}","province":"${MockUtils.escapeJson(r.province)}","city":"${MockUtils.escapeJson(r.city)}","network":"${MockUtils.escapeJson(r.network)}","time":"${MockUtils.escapeJson(r.time)}"}"""
        }
        return """{"success":true,"code":200,"message":"","data":[$payload]}"""
    }

    fun mockPhoneAttributions(request: Request): String {
        val payload = listOf(
            """{"code":86,"flag":"🇨🇳","name":"中国大陆"}""",
            """{"code":852,"flag":"🇭🇰","name":"中国香港"}""",
            """{"code":853,"flag":"🇲🇴","name":"中国澳门"}""",
            """{"code":886,"flag":"🇹🇼","name":"中国台湾"}"""
        ).joinToString(",")
        return """{"success":true,"code":200,"message":"","data":[$payload]}"""
    }

    fun mockSendPhoneVerification(request: Request): String =
        MockUtils.successJson("验证码已发送，请注意查收短信")

    fun mockBindPhone(request: Request): String {
        val fields = request.formFields()
        val phone = fields["phone"].orEmpty()
        if (phone.isBlank()) return MockUtils.failureJson("手机号不能为空")
        val code = fields["code"]?.toIntOrNull() ?: return MockUtils.failureJson("验证码格式错误")
        if (code != 123456) return MockUtils.failureJson("验证码错误，请重新获取")
        return MockUtils.successJson("手机号绑定成功")
    }

    fun mockUnbindPhone(request: Request): String =
        MockUtils.successJson("手机号已解绑")

    fun mockSendEmailVerification(request: Request): String =
        MockUtils.successJson("验证邮件已发送，请查收收件箱")

    fun mockBindEmail(request: Request): String {
        val fields = request.formFields()
        val email = fields["email"].orEmpty()
        if (email.isBlank()) return MockUtils.failureJson("邮箱地址不能为空")
        val code = fields["randomCode"].orEmpty()
        if (code != "MOCK123") return MockUtils.failureJson("验证码错误或已过期")
        return MockUtils.successJson("邮箱绑定成功")
    }

    fun mockUnbindEmail(request: Request): String =
        MockUtils.successJson("邮箱已解绑")

    fun mockSubmitFeedback(request: Request): String =
        MockUtils.successJson("感谢你的反馈，我们会认真查阅")

    fun mockDeleteAccount(request: Request): String {
        val fields = request.formFields()
        val password = fields["password"].orEmpty()
        return if (password == "mock_password") {
            MockUtils.successJson("账号注销申请已提交，将在 7 天后生效")
        } else {
            MockUtils.failureJson("密码错误，注销失败")
        }
    }

    fun mockPhoneStatus(request: Request): String =
        """{"success":true,"code":200,"message":"","data":{"username":"${MockUtils.MOCK_CURRENT_USERNAME}","phone":"13800138000"}}"""

    fun mockEmailStatus(request: Request): String =
        """{"success":true,"code":200,"message":"","data":"mock@gdeiassistant.cn"}"""

    fun mockUserDataExportStateValue(request: Request): String =
        """{"success":true,"code":200,"message":"","data":$mockUserDataExportState}"""

    fun mockUserDataExport(request: Request): String {
        mockUserDataExportState = 2
        return """{"success":true,"code":200,"message":"已提交导出请求"}"""
    }

    fun mockUserDataDownload(request: Request): String {
        return if (mockUserDataExportState == 2) {
            """{"success":true,"code":200,"message":"","data":"https://gdeiassistant.cn/download/mock-user-data.zip"}"""
        } else {
            """{"success":false,"code":400,"message":"请先提交用户数据导出请求","data":null}"""
        }
    }
}
