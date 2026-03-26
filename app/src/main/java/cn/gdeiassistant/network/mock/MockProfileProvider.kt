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

    private fun mockProfileLocationRegions(locale: String): List<MockProfileLocationRegionRecord> {
        return ProfileLocationCatalog.regionsForLocale(locale).map { region ->
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

    private fun localizedDefaultNickname(locale: String): String {
        return localizedMockText(
            simplifiedChinese = MockUtils.MOCK_PROFILE_NICKNAME,
            traditionalChinese = "林知遠",
            english = "Lin Zhiyuan",
            japanese = "リン・ジーユエン",
            korean = "린즈위안",
            locale = locale
        )
    }

    fun currentMockNickname(locale: String = AppLocaleSupport.currentLocale()): String {
        val nickname = mockProfileSummary.nickname.trim()
        return when (nickname) {
            "",
            MockUtils.MOCK_PROFILE_NICKNAME,
            "林知遠",
            "Lin Zhiyuan",
            "リン・ジーユエン",
            "린즈위안" -> localizedDefaultNickname(locale)

            else -> nickname
        }
    }

    fun currentMockAnonymousName(locale: String = AppLocaleSupport.currentLocale()): String {
        return localizedMockText(
            simplifiedChinese = "匿名同学 F",
            traditionalChinese = "匿名同學 F",
            english = "Anonymous Student F",
            japanese = "匿名の学生 F",
            korean = "익명 학생 F",
            locale = locale
        )
    }

    private fun loginRecordText(locale: String, value: String): String {
        return when (value) {
            "广东省广州市" -> localizedMockText("广东省广州市", "廣東省廣州市", "Guangzhou, Guangdong", "広州市・広東省", "광둥성 광저우시", locale)
            "中国" -> localizedMockText("中国", "中國", "China", "中国", "중국", locale)
            "广东省" -> localizedMockText("广东省", "廣東省", "Guangdong", "広東省", "광둥성", locale)
            "广州市" -> localizedMockText("广州市", "廣州市", "Guangzhou", "広州市", "광저우시", locale)
            "中国电信" -> localizedMockText("中国电信", "中國電信", "China Telecom", "中国電信", "차이나 텔레콤", locale)
            "中国移动" -> localizedMockText("中国移动", "中國移動", "China Mobile", "中国移動", "차이나 모바일", locale)
            "北京市" -> localizedMockText("北京市", "北京市", "Beijing", "北京市", "베이징시", locale)
            "中国联通" -> localizedMockText("中国联通", "中國聯通", "China Unicom", "中国聯通", "차이나 유니콤", locale)
            else -> value
        }
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
        return ProfileLocationCatalog.displayName(regionCode, stateCode, cityCode, request.requestLocale())
            .takeIf(String::isNotBlank)
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
        val locale = request.requestLocale()
        return MockUtils.successDataJson(
            data = mockProfileLocationRegions(locale).map { it.toPayload() },
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
        val locale = request.requestLocale()
        val hasAvatar = request.hasMultipartPart("avatar")
        val hasAvatarHd = request.hasMultipartPart("avatar_hd")
        if (!hasAvatar || !hasAvatarHd) {
            return failure(locale, "missingAvatarFile")
        }
        val seed = System.currentTimeMillis()
        mockProfileSummary = mockProfileSummary.copy(
            avatar = "https://picsum.photos/seed/gdei-avatar-$seed/720/720"
        )
        return success(locale, "avatarUpdated")
    }

    fun mockDeleteAvatar(request: Request): String {
        val locale = request.requestLocale()
        mockProfileSummary = mockProfileSummary.copy(avatar = "")
        return success(locale, "avatarRestored")
    }

    fun mockUpdateNickname(request: Request): String {
        val locale = request.requestLocale()
        val nickname = request.jsonObjectBody()?.getString("nickname")?.trim()
            ?: return failure(locale, "missingNickname")
        if (nickname.isEmpty()) return failure(locale, "nicknameEmpty")
        mockProfileSummary = mockProfileSummary.copy(nickname = nickname)
        return success(locale, "saved")
    }

    fun mockUpdateBirthday(request: Request): String {
        val locale = request.requestLocale()
        val body = request.jsonObjectBody() ?: return failure(locale, "missingBirthday")
        val year = body.getInteger("year") ?: return failure(locale, "missingYear")
        val month = body.getInteger("month") ?: return failure(locale, "missingMonth")
        val date = body.getInteger("date") ?: return failure(locale, "missingDate")
        mockProfileSummary = mockProfileSummary.copy(
            birthday = "%04d-%02d-%02d".format(year, month, date)
        )
        return success(locale, "saved")
    }

    fun mockUpdateFaculty(request: Request): String {
        val locale = request.requestLocale()
        val facultyCode = request.jsonObjectBody()?.getInteger("faculty")
            ?: return failure(locale, "missingFaculty")
        val options = LocalizedProfileCatalog.catalogForLocale(locale).defaultOptions
        val facultyName = options.facultyNameFor(facultyCode) ?: return failure(locale, "facultyNotFound")
        val majorOptions = options.majorOptionsFor(facultyName)
        mockProfileSummary = mockProfileSummary.copy(
            faculty = if (facultyName == ProfileFormSupport.UnselectedOption) "" else facultyName,
            facultyCode = facultyCode,
            major = mockProfileSummary.major.takeIf { majorOptions.contains(it) } ?: "",
            majorCode = mockProfileSummary.majorCode.takeIf { code ->
                options.majorLabelFor(facultyName, code)?.let(majorOptions::contains) == true
            } ?: ""
        )
        return success(locale, "saved")
    }

    fun mockUpdateMajor(request: Request): String {
        val locale = request.requestLocale()
        val majorCode = request.jsonObjectBody()?.getString("major")?.trim()
            ?: return failure(locale, "missingMajor")
        val options = LocalizedProfileCatalog.catalogForLocale(locale).defaultOptions
        val currentFacultyLabel = options.facultyNameFor(mockProfileSummary.facultyCode).orEmpty()
        val majorLabel = options
            .majorLabelFor(currentFacultyLabel, majorCode)
            ?: return failure(locale, "majorNotFound")
        mockProfileSummary = mockProfileSummary.copy(
            major = majorLabel,
            majorCode = majorCode
        )
        return success(locale, "saved")
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
            location = ProfileLocationCatalog.displayName(
                regionCode = mockProfileSummary.locationRegion,
                stateCode = mockProfileSummary.locationState,
                cityCode = mockProfileSummary.locationCity,
                locale = locale
            ),
            hometown = ProfileLocationCatalog.displayName(
                regionCode = mockProfileSummary.hometownRegion,
                stateCode = mockProfileSummary.hometownState,
                cityCode = mockProfileSummary.hometownCity,
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

    private fun profileMessage(locale: String, key: String): String {
        return when (key) {
            "missingAvatarFile" -> localizedMockText("缺少头像文件", "缺少頭像檔案", "Missing avatar files", "アバター画像がありません", "아바타 파일이 없습니다", locale)
            "avatarUpdated" -> localizedMockText("头像已更新", "頭像已更新", "Avatar updated", "アバターを更新しました", "아바타를 업데이트했습니다", locale)
            "avatarRestored" -> localizedMockText("已恢复默认头像", "已恢復預設頭像", "Default avatar restored", "デフォルトのアバターに戻しました", "기본 아바타로 복원했습니다", locale)
            "missingNickname" -> localizedMockText("缺少昵称", "缺少暱稱", "Missing nickname", "ニックネームがありません", "닉네임이 없습니다", locale)
            "nicknameEmpty" -> localizedMockText("昵称不能为空", "暱稱不能為空", "Nickname cannot be empty", "ニックネームは空にできません", "닉네임은 비워 둘 수 없습니다", locale)
            "saved" -> localizedMockText("已保存", "已儲存", "Saved", "保存しました", "저장했습니다", locale)
            "missingBirthday" -> localizedMockText("缺少生日信息", "缺少生日資訊", "Missing birthday information", "誕生日情報がありません", "생일 정보가 없습니다", locale)
            "missingYear" -> localizedMockText("缺少年份", "缺少年份", "Missing year", "年がありません", "연도가 없습니다", locale)
            "missingMonth" -> localizedMockText("缺少月份", "缺少月份", "Missing month", "月がありません", "월이 없습니다", locale)
            "missingDate" -> localizedMockText("缺少日期", "缺少日期", "Missing day", "日がありません", "일 정보가 없습니다", locale)
            "missingFaculty" -> localizedMockText("缺少院系信息", "缺少院系資訊", "Missing department information", "学部情報がありません", "학과 정보가 없습니다", locale)
            "facultyNotFound" -> localizedMockText("院系信息不存在", "院系資訊不存在", "Department information was not found", "学部情報が見つかりません", "학과 정보를 찾을 수 없습니다", locale)
            "missingMajor" -> localizedMockText("缺少专业信息", "缺少專業資訊", "Missing major information", "専攻情報がありません", "전공 정보가 없습니다", locale)
            "majorNotFound" -> localizedMockText("专业信息不存在", "專業資訊不存在", "Major information was not found", "専攻情報が見つかりません", "전공 정보를 찾을 수 없습니다", locale)
            "missingLocation" -> localizedMockText("缺少所在地信息", "缺少所在地資訊", "Missing current location", "所在地情報がありません", "현재 위치 정보가 없습니다", locale)
            "missingHometown" -> localizedMockText("缺少家乡信息", "缺少家鄉資訊", "Missing hometown information", "出身地情報がありません", "고향 정보가 없습니다", locale)
            "invalidRequestBody" -> localizedMockText("请求体格式错误", "請求體格式錯誤", "Invalid request body", "リクエスト本文の形式が正しくありません", "요청 본문 형식이 올바르지 않습니다", locale)
            "privacyUpdated" -> localizedMockText("隐私设置已更新", "隱私設定已更新", "Privacy settings updated", "プライバシー設定を更新しました", "개인정보 설정을 업데이트했습니다", locale)
            "smsSent" -> localizedMockText("验证码已发送，请注意查收短信", "驗證碼已發送，請留意短信", "Verification code sent. Please check your SMS messages.", "認証コードを送信しました。SMSをご確認ください。", "인증 코드를 보냈습니다. 문자메시지를 확인해 주세요.", locale)
            "phoneEmpty" -> localizedMockText("手机号不能为空", "手機號不能為空", "Phone number cannot be empty", "電話番号は空にできません", "전화번호는 비워 둘 수 없습니다", locale)
            "codeFormatInvalid" -> localizedMockText("验证码格式错误", "驗證碼格式錯誤", "Invalid verification code format", "認証コードの形式が正しくありません", "인증 코드 형식이 올바르지 않습니다", locale)
            "verificationCodeInvalid" -> localizedMockText("验证码错误，请重新获取", "驗證碼錯誤，請重新獲取", "Incorrect verification code. Please request a new one.", "認証コードが正しくありません。もう一度取得してください。", "인증 코드가 올바르지 않습니다. 새 코드를 받아 주세요.", locale)
            "phoneBound" -> localizedMockText("手机号绑定成功", "手機號綁定成功", "Phone number linked successfully", "電話番号を連携しました", "전화번호를 연결했습니다", locale)
            "phoneUnbound" -> localizedMockText("手机号已解绑", "手機號已解綁", "Phone number unlinked", "電話番号の連携を解除しました", "전화번호 연결을 해제했습니다", locale)
            "emailSent" -> localizedMockText("验证邮件已发送，请查收收件箱", "驗證郵件已發送，請查收收件箱", "Verification email sent. Please check your inbox.", "認証メールを送信しました。受信箱をご確認ください。", "인증 메일을 보냈습니다. 받은편지함을 확인해 주세요.", locale)
            "emailEmpty" -> localizedMockText("邮箱地址不能为空", "郵箱地址不能為空", "Email address cannot be empty", "メールアドレスは空にできません", "이메일 주소는 비워 둘 수 없습니다", locale)
            "codeExpiredOrInvalid" -> localizedMockText("验证码错误或已过期", "驗證碼錯誤或已過期", "The verification code is incorrect or expired", "認証コードが正しくないか、有効期限が切れています", "인증 코드가 올바르지 않거나 만료되었습니다", locale)
            "emailBound" -> localizedMockText("邮箱绑定成功", "郵箱綁定成功", "Email linked successfully", "メールアドレスを連携しました", "이메일을 연결했습니다", locale)
            "emailUnbound" -> localizedMockText("邮箱已解绑", "郵箱已解綁", "Email unlinked", "メールアドレスの連携を解除しました", "이메일 연결을 해제했습니다", locale)
            "feedbackReceived" -> localizedMockText("感谢你的反馈，我们会认真查阅", "感謝你的反饋，我們會認真查閱", "Thanks for your feedback. We'll review it carefully.", "フィードバックありがとうございます。しっかり確認します。", "피드백 감사합니다. 꼼꼼히 확인하겠습니다.", locale)
            "deleteAccountSubmitted" -> localizedMockText("账号注销申请已提交，将在 7 天后生效", "賬號註銷申請已提交，將在 7 天後生效", "Account deletion request submitted. It will take effect in 7 days.", "アカウント削除申請を受け付けました。7日後に反映されます。", "계정 삭제 요청을 접수했습니다. 7일 후 적용됩니다.", locale)
            "deleteAccountFailed" -> localizedMockText("密码错误，注销失败", "密碼錯誤，註銷失敗", "Incorrect password. Account deletion failed.", "パスワードが正しくないため、削除できませんでした。", "비밀번호가 올바르지 않아 계정 삭제에 실패했습니다.", locale)
            "exportRequested" -> localizedMockText("已提交导出请求", "已提交匯出請求", "Export request submitted", "エクスポート申請を送信しました", "내보내기 요청을 제출했습니다", locale)
            "exportRequestRequired" -> localizedMockText("请先提交用户数据导出请求", "請先提交用戶資料匯出請求", "Please submit a user data export request first", "先にユーザーデータのエクスポート申請を送信してください", "먼저 사용자 데이터 내보내기 요청을 제출해 주세요", locale)
            else -> key
        }
    }

    private fun success(locale: String, key: String): String =
        MockUtils.successJson(profileMessage(locale, key))

    private fun failure(locale: String, key: String): String =
        MockUtils.failureJson(profileMessage(locale, key))

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
        val locale = request.requestLocale()
        val year = request.jsonObjectBody()?.getInteger("year")
        mockProfileSummary = mockProfileSummary.copy(enrollment = year?.toString().orEmpty())
        return success(locale, "saved")
    }

    fun mockUpdateLocation(request: Request): String {
        val locale = request.requestLocale()
        val displayName = locationDisplayNameFromRequest(request)
            ?: return failure(locale, "missingLocation")
        val body = request.jsonObjectBody()
        mockProfileSummary = mockProfileSummary.copy(location = displayName)
            .copy(
                locationRegion = body?.getString("region")?.trim().orEmpty(),
                locationState = body?.getString("state")?.trim().orEmpty(),
                locationCity = body?.getString("city")?.trim().orEmpty()
            )
        return success(locale, "saved")
    }

    fun mockUpdateHometown(request: Request): String {
        val locale = request.requestLocale()
        val displayName = locationDisplayNameFromRequest(request)
            ?: return failure(locale, "missingHometown")
        val body = request.jsonObjectBody()
        mockProfileSummary = mockProfileSummary.copy(hometown = displayName)
            .copy(
                hometownRegion = body?.getString("region")?.trim().orEmpty(),
                hometownState = body?.getString("state")?.trim().orEmpty(),
                hometownCity = body?.getString("city")?.trim().orEmpty()
            )
        return success(locale, "saved")
    }

    fun mockUpdateIntroduction(request: Request): String {
        val locale = request.requestLocale()
        val introduction = request.jsonObjectBody()?.getString("introduction")?.trim().orEmpty()
        mockProfileSummary = mockProfileSummary.copy(introduction = introduction)
        return success(locale, "saved")
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
        val locale = request.requestLocale()
        val body = request.jsonObjectBody() ?: return failure(locale, "invalidRequestBody")
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
        return success(locale, "privacyUpdated")
    }

    fun mockLoginRecords(request: Request): String {
        val locale = request.requestLocale()
        val segments = request.url.pathSegments
        val startIdx = segments.indexOf("start")
        val start = segments.getOrNull(startIdx + 1)?.toIntOrNull() ?: 0
        val sizeIdx = segments.indexOf("size")
        val size = segments.getOrNull(sizeIdx + 1)?.toIntOrNull()?.coerceIn(1, 20) ?: 10
        val paged = mockLoginRecordList.drop(start).take(size)
        val payload = paged.joinToString(",") { r ->
            """{"id":${r.id},"ip":"${r.ip}","area":"${MockUtils.escapeJson(loginRecordText(locale, r.area))}","country":"${MockUtils.escapeJson(loginRecordText(locale, r.country))}","province":"${MockUtils.escapeJson(loginRecordText(locale, r.province))}","city":"${MockUtils.escapeJson(loginRecordText(locale, r.city))}","network":"${MockUtils.escapeJson(loginRecordText(locale, r.network))}","time":"${MockUtils.escapeJson(r.time)}"}"""
        }
        return """{"success":true,"code":200,"message":"","data":[$payload]}"""
    }

    fun mockPhoneAttributions(request: Request): String {
        val locale = request.requestLocale()
        val payload = listOf(
            """{"code":86,"flag":"🇨🇳","name":"${MockUtils.escapeJson(localizedMockText("中国大陆", "中國大陸", "Mainland China", "中国本土", "중국 본토", locale))}"}""",
            """{"code":852,"flag":"🇭🇰","name":"${MockUtils.escapeJson(localizedMockText("中国香港", "中國香港", "Hong Kong, China", "中国香港", "중국 홍콩", locale))}"}""",
            """{"code":853,"flag":"🇲🇴","name":"${MockUtils.escapeJson(localizedMockText("中国澳门", "中國澳門", "Macao, China", "中国マカオ", "중국 마카오", locale))}"}""",
            """{"code":886,"flag":"🇹🇼","name":"${MockUtils.escapeJson(localizedMockText("中国台湾", "中國台灣", "Taiwan, China", "中国台湾", "중국 타이완", locale))}"}"""
        ).joinToString(",")
        return """{"success":true,"code":200,"message":"","data":[$payload]}"""
    }

    fun mockSendPhoneVerification(request: Request): String =
        success(request.requestLocale(), "smsSent")

    fun mockBindPhone(request: Request): String {
        val locale = request.requestLocale()
        val fields = request.formFields()
        val phone = fields["phone"].orEmpty()
        if (phone.isBlank()) return failure(locale, "phoneEmpty")
        val code = fields["code"]?.toIntOrNull() ?: return failure(locale, "codeFormatInvalid")
        if (code != 123456) return failure(locale, "verificationCodeInvalid")
        return success(locale, "phoneBound")
    }

    fun mockUnbindPhone(request: Request): String =
        success(request.requestLocale(), "phoneUnbound")

    fun mockSendEmailVerification(request: Request): String =
        success(request.requestLocale(), "emailSent")

    fun mockBindEmail(request: Request): String {
        val locale = request.requestLocale()
        val fields = request.formFields()
        val email = fields["email"].orEmpty()
        if (email.isBlank()) return failure(locale, "emailEmpty")
        val code = fields["randomCode"].orEmpty()
        if (code != "MOCK123") return failure(locale, "codeExpiredOrInvalid")
        return success(locale, "emailBound")
    }

    fun mockUnbindEmail(request: Request): String =
        success(request.requestLocale(), "emailUnbound")

    fun mockSubmitFeedback(request: Request): String =
        success(request.requestLocale(), "feedbackReceived")

    fun mockDeleteAccount(request: Request): String {
        val locale = request.requestLocale()
        val fields = request.formFields()
        val password = fields["password"].orEmpty()
        return if (password == "mock_password") {
            success(locale, "deleteAccountSubmitted")
        } else {
            failure(locale, "deleteAccountFailed")
        }
    }

    fun mockPhoneStatus(request: Request): String =
        """{"success":true,"code":200,"message":"","data":{"username":"${MockUtils.MOCK_CURRENT_USERNAME}","phone":"13800138000"}}"""

    fun mockEmailStatus(request: Request): String =
        """{"success":true,"code":200,"message":"","data":"mock@gdeiassistant.cn"}"""

    fun mockUserDataExportStateValue(request: Request): String =
        """{"success":true,"code":200,"message":"","data":$mockUserDataExportState}"""

    fun mockUserDataExport(request: Request): String {
        val locale = request.requestLocale()
        mockUserDataExportState = 2
        return """{"success":true,"code":200,"message":"${MockUtils.escapeJson(profileMessage(locale, "exportRequested"))}"}"""
    }

    fun mockUserDataDownload(request: Request): String {
        val locale = request.requestLocale()
        return if (mockUserDataExportState == 2) {
            """{"success":true,"code":200,"message":"","data":"https://gdeiassistant.cn/download/mock-user-data.zip"}"""
        } else {
            """{"success":false,"code":400,"message":"${MockUtils.escapeJson(profileMessage(locale, "exportRequestRequired"))}","data":null}"""
        }
    }
}
