package cn.gdeiassistant.network.mock

import cn.gdeiassistant.model.spareClassNumberOptions
import cn.gdeiassistant.model.spareTypeOptions
import cn.gdeiassistant.model.spareZoneOptions
import cn.gdeiassistant.network.mock.MockUtils.bodyUtf8
import cn.gdeiassistant.network.mock.MockUtils.formFields
import cn.gdeiassistant.network.mock.MockUtils.localizedText
import cn.gdeiassistant.network.mock.MockUtils.requestLocale
import com.google.gson.Gson
import okhttp3.Request

/** Mock provider for grades, schedule, CET, evaluate, spare rooms, graduate exam. */
object MockAcademicProvider {

    // ── Data classes ────────────────────────────────────────────────────────

    data class MockSpareQueryRequest(
        val zone: Int? = 0,
        val type: Int? = 0,
        val startTime: Int? = 1,
        val endTime: Int? = 2,
        val classNumber: Int? = 0
    )

    data class MockGraduateExamQuery(
        val name: String = "",
        val examNumber: String = "",
        val idNumber: String = ""
    )

    data class MockSpareRoomRecord(
        val number: String,
        val name: String,
        val type: String,
        val zone: String,
        val classSeating: Int,
        val examSeating: Int
    )

    // ── Mock data ───────────────────────────────────────────────────────────

    private fun academicText(
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

    private fun mockSpareRooms(locale: String): List<MockSpareRoomRecord> = listOf(
        MockSpareRoomRecord(
            number = "A301",
            name = academicText(locale, "教学楼 A301", "Teaching Building A301", "教學樓 A301", "教学棟 A301", "강의동 A301"),
            type = academicText(locale, "多媒体教室", "Multimedia Classroom", "多媒體教室", "マルチメディア教室", "멀티미디어 강의실"),
            zone = academicText(locale, "海珠", "Haizhu", "海珠", "海珠", "하이주"),
            classSeating = 120,
            examSeating = 96
        ),
        MockSpareRoomRecord(
            number = "B205",
            name = academicText(locale, "实验楼 B205", "Lab Building B205", "實驗樓 B205", "実験棟 B205", "실험동 B205"),
            type = academicText(locale, "机房", "Computer Lab", "機房", "PC 室", "컴퓨터실"),
            zone = academicText(locale, "花都", "Huadu", "花都", "花都", "화두"),
            classSeating = 72,
            examSeating = 54
        ),
        MockSpareRoomRecord(
            number = "C402",
            name = academicText(locale, "艺术楼 C402", "Arts Building C402", "藝術樓 C402", "芸術棟 C402", "예술동 C402"),
            type = academicText(locale, "综合绘画实验室", "Integrated Painting Studio", "綜合繪畫實驗室", "総合絵画実験室", "종합 회화 실습실"),
            zone = academicText(
                locale,
                "广东轻工南海校区",
                "Nanhai Campus of Guangdong Industry Polytechnic",
                "廣東輕工南海校區",
                "広東軽工南海キャンパス",
                "광둥경공업 난하이 캠퍼스"
            ),
            classSeating = 48,
            examSeating = 36
        )
    )

    // ── Mock endpoints ──────────────────────────────────────────────────────

    fun mockGrade(request: Request): String {
        val locale = request.requestLocale()
        return """
        {"success":true,"code":200,"message":"","data":{
            "year":0,
            "firstTermGPA":3.65,
            "secondTermGPA":3.72,
            "firstTermIGP":88.0,
            "secondTermIGP":90.0,
            "firstTermGradeList":[
                {"gradeYear":"2025-2026","gradeTerm":"${MockUtils.escapeJson(academicText(locale, "第一学期", "First Semester", "第一學期", "前期", "1학기"))}","gradeId":"MATH001","gradeName":"${MockUtils.escapeJson(academicText(locale, "高等数学", "Advanced Mathematics", "高等數學", "高等数学", "고등수학"))}","gradeCredit":"4","gradeType":"${MockUtils.escapeJson(academicText(locale, "必修", "Required", "必修", "必修", "필수"))}","gradeGpa":"3.0","gradeScore":"85"},
                {"gradeYear":"2025-2026","gradeTerm":"${MockUtils.escapeJson(academicText(locale, "第一学期", "First Semester", "第一學期", "前期", "1학기"))}","gradeId":"ENG001","gradeName":"${MockUtils.escapeJson(academicText(locale, "大学英语", "College English", "大學英語", "大学英語", "대학영어"))}","gradeCredit":"3","gradeType":"${MockUtils.escapeJson(academicText(locale, "必修", "Required", "必修", "必修", "필수"))}","gradeGpa":"4.0","gradeScore":"92"},
                {"gradeYear":"2025-2026","gradeTerm":"${MockUtils.escapeJson(academicText(locale, "第一学期", "First Semester", "第一學期", "前期", "1학기"))}","gradeId":"CS001","gradeName":"${MockUtils.escapeJson(academicText(locale, "程序设计基础", "Programming Fundamentals", "程式設計基礎", "プログラミング基礎", "프로그래밍 기초"))}","gradeCredit":"4","gradeType":"${MockUtils.escapeJson(academicText(locale, "必修", "Required", "必修", "必修", "필수"))}","gradeGpa":"3.5","gradeScore":"88"}
            ],
            "secondTermGradeList":[
                {"gradeYear":"2025-2026","gradeTerm":"${MockUtils.escapeJson(academicText(locale, "第二学期", "Second Semester", "第二學期", "後期", "2학기"))}","gradeId":"MATH002","gradeName":"${MockUtils.escapeJson(academicText(locale, "线性代数", "Linear Algebra", "線性代數", "線形代数", "선형대수"))}","gradeCredit":"3","gradeType":"${MockUtils.escapeJson(academicText(locale, "必修", "Required", "必修", "必修", "필수"))}","gradeGpa":"3.5","gradeScore":"87"},
                {"gradeYear":"2025-2026","gradeTerm":"${MockUtils.escapeJson(academicText(locale, "第二学期", "Second Semester", "第二學期", "後期", "2학기"))}","gradeId":"PHY001","gradeName":"${MockUtils.escapeJson(academicText(locale, "大学物理", "College Physics", "大學物理", "大学物理", "대학물리"))}","gradeCredit":"4","gradeType":"${MockUtils.escapeJson(academicText(locale, "必修", "Required", "必修", "必修", "필수"))}","gradeGpa":"3.0","gradeScore":"82"}
            ]
        }}
    """.trimIndent()
    }

    fun mockSchedule(request: Request): String {
        val locale = request.requestLocale()
        val week = request.url.queryParameter("week")
            ?.toIntOrNull()
            ?: request.formFields()["week"]?.toIntOrNull()
            ?: 1
        return """
        {"success":true,"code":200,"message":"","data":{
            "week":$week,
            "scheduleList":[
                {"id":"s1","scheduleLength":2,"scheduleName":"${MockUtils.escapeJson(academicText(locale, "移动应用开发", "Mobile App Development", "移動應用開發", "モバイルアプリ開発", "모바일 앱 개발"))}","scheduleType":"${MockUtils.escapeJson(academicText(locale, "专业课", "Major Course", "專業課", "専門科目", "전공 과목"))}","scheduleLesson":"1-2","scheduleTeacher":"${MockUtils.escapeJson(academicText(locale, "张老师", "Prof. Zhang", "張老師", "張先生", "장 교수"))}","scheduleLocation":"${MockUtils.escapeJson(academicText(locale, "实验楼A301", "Lab Building A301", "實驗樓 A301", "実験棟 A301", "실험동 A301"))}","colorCode":"#22C55E","position":0,"row":0,"column":0,"minScheduleWeek":1,"maxScheduleWeek":16,"scheduleWeek":"${MockUtils.escapeJson(academicText(locale, "第1周至第16周", "Weeks 1-16", "第1週至第16週", "第1週から第16週", "1주차-16주차"))}"},
                {"id":"s2","scheduleLength":2,"scheduleName":"${MockUtils.escapeJson(academicText(locale, "计算机网络", "Computer Networks", "計算機網路", "コンピュータネットワーク", "컴퓨터 네트워크"))}","scheduleType":"${MockUtils.escapeJson(academicText(locale, "专业课", "Major Course", "專業課", "専門科目", "전공 과목"))}","scheduleLesson":"3-4","scheduleTeacher":"${MockUtils.escapeJson(academicText(locale, "李老师", "Prof. Li", "李老師", "李先生", "이 교수"))}","scheduleLocation":"${MockUtils.escapeJson(academicText(locale, "教学楼B205", "Teaching Building B205", "教學樓 B205", "教学棟 B205", "강의동 B205"))}","colorCode":"#3B82F6","position":16,"row":2,"column":2,"minScheduleWeek":1,"maxScheduleWeek":16,"scheduleWeek":"${MockUtils.escapeJson(academicText(locale, "第1周至第16周", "Weeks 1-16", "第1週至第16週", "第1週から第16週", "1주차-16주차"))}"},
                {"id":"s3","scheduleLength":2,"scheduleName":"${MockUtils.escapeJson(academicText(locale, "软件工程", "Software Engineering", "軟體工程", "ソフトウェア工学", "소프트웨어 공학"))}","scheduleType":"${MockUtils.escapeJson(academicText(locale, "专业课", "Major Course", "專業課", "専門科目", "전공 과목"))}","scheduleLesson":"5-6","scheduleTeacher":"${MockUtils.escapeJson(academicText(locale, "王老师", "Prof. Wang", "王老師", "王先生", "왕 교수"))}","scheduleLocation":"${MockUtils.escapeJson(academicText(locale, "教学楼C102", "Teaching Building C102", "教學樓 C102", "教学棟 C102", "강의동 C102"))}","colorCode":"#F97316","position":32,"row":4,"column":4,"minScheduleWeek":1,"maxScheduleWeek":16,"scheduleWeek":"${MockUtils.escapeJson(academicText(locale, "第1周至第16周", "Weeks 1-16", "第1週至第16週", "第1週から第16週", "1주차-16주차"))}"},
                {"id":"s4","scheduleLength":3,"scheduleName":"${MockUtils.escapeJson(academicText(locale, "大学英语", "College English", "大學英語", "大学英語", "대학영어"))}","scheduleType":"${MockUtils.escapeJson(academicText(locale, "公共课", "General Course", "公共課", "共通科目", "교양 과목"))}","scheduleLesson":"7-9","scheduleTeacher":"${MockUtils.escapeJson(academicText(locale, "周老师", "Prof. Zhou", "周老師", "周先生", "주 교수"))}","scheduleLocation":"${MockUtils.escapeJson(academicText(locale, "教学楼A104", "Teaching Building A104", "教學樓 A104", "教学棟 A104", "강의동 A104"))}","colorCode":"#8B5CF6","position":47,"row":6,"column":5,"minScheduleWeek":1,"maxScheduleWeek":18,"scheduleWeek":"${MockUtils.escapeJson(academicText(locale, "第1周至第18周", "Weeks 1-18", "第1週至第18週", "第1週から第18週", "1주차-18주차"))}"}
            ]
        }}
    """.trimIndent()
    }

    fun mockCetCheckCode(request: Request): String =
        """{"success":true,"code":200,"message":"","data":"${MockUtils.MOCK_CAPTCHA_BASE64}"}"""

    fun mockCetQuery(request: Request): String {
        val locale = request.requestLocale()
        return """
        {"success":true,"code":200,"message":"","data":{
            "name":"${MockUtils.escapeJson(academicText(locale, "张三", "Alex Chen", "張三", "チェン アレックス", "첸 알렉스"))}",
            "school":"${MockUtils.escapeJson(academicText(locale, "广东第二师范学院", "Guangdong University of Education", "廣東第二師範學院", "広東第二師範学院", "광둥 제2사범대학교"))}",
            "type":"${MockUtils.escapeJson(academicText(locale, "大学英语四级", "College English Test Band 4", "大學英語四級", "大学英語四級", "대학영어 4급"))}",
            "admissionCard":"CET202612345678",
            "totalScore":"568",
            "listeningScore":"189",
            "readingScore":"196",
            "writingAndTranslatingScore":"183"
        }}
    """.trimIndent()
    }

    fun mockEvaluateSubmit(request: Request): String =
        """{"success":true,"code":200,"message":"${MockUtils.escapeJson(academicText(request.requestLocale(), "教学评价已提交", "Teaching evaluation submitted", "教學評價已提交", "授業評価を送信しました", "강의 평가가 제출되었습니다"))}"}"""

    fun mockSpareRoomList(request: Request): String {
        val locale = request.requestLocale()
        val zoneOptions = spareZoneOptions(locale)
        val typeOptions = spareTypeOptions(locale)
        val classOptions = spareClassNumberOptions(locale)
        val query = runCatching {
            Gson().fromJson(request.bodyUtf8(), MockSpareQueryRequest::class.java)
        }.getOrNull() ?: MockSpareQueryRequest()
        val payload = mockSpareRooms(locale).mapIndexed { index, room ->
            val zoneName = if ((query.zone ?: 0) == 0) {
                room.zone
            } else {
                zoneOptions.firstOrNull { it.value == query.zone }?.label ?: room.zone
            }
            val roomType = if ((query.type ?: 0) == 0) {
                room.type
            } else {
                typeOptions.firstOrNull { it.value == query.type }?.label ?: room.type
            }
            val section = classOptions.firstOrNull { it.value == (query.classNumber ?: 0) }?.label
                ?: academicText(locale, "第1,2节", "Periods 1-2", "第1,2節", "1-2限", "1-2교시")
            """{"number":"${room.number}","name":"${MockUtils.escapeJson(room.name)}","type":"${MockUtils.escapeJson(roomType)}","zone":"${MockUtils.escapeJson(zoneName)}","classSeating":"${room.classSeating + index * 6}","section":"${MockUtils.escapeJson(section)}","examSeating":"${room.examSeating + index * 4}"}"""
        }.joinToString(",")
        return """{"success":true,"code":200,"message":"","data":[$payload]}"""
    }

    fun mockGraduateExam(request: Request): String {
        val locale = request.requestLocale()
        val query = runCatching {
            Gson().fromJson(request.bodyUtf8(), MockGraduateExamQuery::class.java)
        }.getOrNull() ?: MockGraduateExamQuery()
        return """
            {"success":true,"code":200,"message":"","data":{
                "name":"${MockUtils.escapeJson(query.name.ifBlank { academicText(locale, "张三", "Alex Chen", "張三", "チェン アレックス", "첸 알렉스") })}",
                "signUpNumber":"2026${MockUtils.escapeJson(query.examNumber.ifBlank { "0011223344" }.takeLast(6))}",
                "examNumber":"${MockUtils.escapeJson(query.examNumber.ifBlank { "441526010203" })}",
                "totalScore":"389",
                "firstScore":"71",
                "secondScore":"74",
                "thirdScore":"121",
                "fourthScore":"123"
            }}
        """.trimIndent()
    }
}
