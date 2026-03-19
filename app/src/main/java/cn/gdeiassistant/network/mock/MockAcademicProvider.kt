package cn.gdeiassistant.network.mock

import cn.gdeiassistant.model.spareClassNumberOptions
import cn.gdeiassistant.model.spareTypeOptions
import cn.gdeiassistant.model.spareZoneOptions
import cn.gdeiassistant.network.mock.MockUtils.bodyUtf8
import cn.gdeiassistant.network.mock.MockUtils.formFields
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

    val mockSpareRooms = listOf(
        MockSpareRoomRecord("A301", "教学楼 A301", "多媒体教室", "海珠", 120, 96),
        MockSpareRoomRecord("B205", "实验楼 B205", "机房", "花都", 72, 54),
        MockSpareRoomRecord("C402", "艺术楼 C402", "综合绘画实验室", "广东轻工南海校区", 48, 36)
    )

    // ── Mock endpoints ──────────────────────────────────────────────────────

    fun mockGrade(request: Request): String = """
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

    fun mockSchedule(request: Request): String {
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

    fun mockCetCheckCode(request: Request): String =
        """{"success":true,"code":200,"message":"","data":"${MockUtils.MOCK_CAPTCHA_BASE64}"}"""

    fun mockCetQuery(request: Request): String = """
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

    fun mockEvaluateSubmit(request: Request): String =
        """{"success":true,"code":200,"message":"教学评价已提交"}"""

    fun mockSpareRoomList(request: Request): String {
        val query = runCatching {
            Gson().fromJson(request.bodyUtf8(), MockSpareQueryRequest::class.java)
        }.getOrNull() ?: MockSpareQueryRequest()
        val payload = mockSpareRooms.mapIndexed { index, room ->
            val zoneName = if ((query.zone ?: 0) == 0) {
                room.zone
            } else {
                spareZoneOptions.firstOrNull { it.value == query.zone }?.label ?: room.zone
            }
            val roomType = if ((query.type ?: 0) == 0) {
                room.type
            } else {
                spareTypeOptions.firstOrNull { it.value == query.type }?.label ?: room.type
            }
            val section = spareClassNumberOptions.firstOrNull { it.value == (query.classNumber ?: 0) }?.label
                ?: "第1,2节"
            """{"number":"${room.number}","name":"${MockUtils.escapeJson(room.name)}","type":"${MockUtils.escapeJson(roomType)}","zone":"${MockUtils.escapeJson(zoneName)}","classSeating":"${room.classSeating + index * 6}","section":"${MockUtils.escapeJson(section)}","examSeating":"${room.examSeating + index * 4}"}"""
        }.joinToString(",")
        return """{"success":true,"code":200,"message":"","data":[$payload]}"""
    }

    fun mockGraduateExam(request: Request): String {
        val query = runCatching {
            Gson().fromJson(request.bodyUtf8(), MockGraduateExamQuery::class.java)
        }.getOrNull() ?: MockGraduateExamQuery()
        return """
            {"success":true,"code":200,"message":"","data":{
                "name":"${MockUtils.escapeJson(query.name.ifBlank { "张三" })}",
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
