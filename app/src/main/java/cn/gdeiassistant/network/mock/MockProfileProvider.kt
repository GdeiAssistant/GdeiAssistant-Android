package cn.gdeiassistant.network.mock

import cn.gdeiassistant.data.ProfileLocationMockCatalog
import cn.gdeiassistant.model.ProfileFormSupport
import cn.gdeiassistant.model.lostFoundItemTypeTitles
import cn.gdeiassistant.model.marketplaceTypeTitles
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

    val mockProfileLocationRegions = ProfileLocationMockCatalog.regions.map { region ->
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

    var mockProfileSummary = MockProfileSummaryRecord(
        username = MockUtils.MOCK_CURRENT_USERNAME,
        nickname = MockUtils.MOCK_PROFILE_NICKNAME,
        avatar = "",
        faculty = "计算机科学系",
        major = "软件工程",
        enrollment = "2023",
        location = "中国 广东 广州",
        hometown = "中国 广东 汕头",
        introduction = "喜欢做实用的小工具，也在准备 iOS 开发实习。",
        birthday = "2004-09-16",
        ipArea = "广东",
        age = 21
    )

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

    fun currentMockAnonymousName(): String = "匿名同学 F"

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
        return MockUtils.successDataJson(
            data = mockProfileSummary.toPayload(),
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
        val faculties = ProfileFormSupport.defaultOptions.faculties.map { option ->
            linkedMapOf(
                "code" to option.code,
                "label" to option.label,
                "majors" to option.majors
            )
        }
        val marketplaceItemTypes = marketplaceTypeTitles.mapIndexed { index, label ->
            linkedMapOf("code" to index, "label" to label)
        }
        val lostFoundItemTypes = lostFoundItemTypeTitles.mapIndexed { index, label ->
            linkedMapOf("code" to index, "label" to label)
        }
        val lostFoundModes = listOf("寻物启事", "失物招领").mapIndexed { index, label ->
            linkedMapOf("code" to index, "label" to label)
        }
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
        val options = ProfileFormSupport.defaultOptions
        val facultyName = options.facultyNameFor(facultyCode)
            ?: return MockUtils.failureJson("院系信息不存在")
        val majorOptions = options.majorOptionsFor(facultyName)
        mockProfileSummary = mockProfileSummary.copy(
            faculty = if (facultyName == ProfileFormSupport.UnselectedOption) "" else facultyName,
            major = mockProfileSummary.major.takeIf { majorOptions.contains(it) } ?: ""
        )
        return MockUtils.successJson("已保存")
    }

    fun mockUpdateMajor(request: Request): String {
        val major = request.jsonObjectBody()?.getString("major")?.trim()
            ?: return MockUtils.failureJson("缺少专业信息")
        mockProfileSummary = mockProfileSummary.copy(major = major)
        return MockUtils.successJson("已保存")
    }

    fun mockUpdateEnrollment(request: Request): String {
        val year = request.jsonObjectBody()?.getInteger("year")
        mockProfileSummary = mockProfileSummary.copy(enrollment = year?.toString().orEmpty())
        return MockUtils.successJson("已保存")
    }

    fun mockUpdateLocation(request: Request): String {
        val displayName = locationDisplayNameFromRequest(request)
            ?: return MockUtils.failureJson("缺少所在地信息")
        mockProfileSummary = mockProfileSummary.copy(location = displayName)
        return MockUtils.successJson("已保存")
    }

    fun mockUpdateHometown(request: Request): String {
        val displayName = locationDisplayNameFromRequest(request)
            ?: return MockUtils.failureJson("缺少家乡信息")
        mockProfileSummary = mockProfileSummary.copy(hometown = displayName)
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
