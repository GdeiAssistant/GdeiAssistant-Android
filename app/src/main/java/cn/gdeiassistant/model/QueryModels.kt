package cn.gdeiassistant.model

import androidx.compose.runtime.Immutable
import java.io.Serializable
import java.util.Calendar

@Immutable
data class SpareRoomQuery(
    val zone: Int = 0,
    val type: Int = 0,
    val minSeating: Int? = null,
    val maxSeating: Int? = null,
    val startTime: Int = 1,
    val endTime: Int = 2,
    val minWeek: Int = 0,
    val maxWeek: Int = 6,
    val weekType: Int = 0,
    val classNumber: Int = 0
) : Serializable

@Immutable
data class SpareRoomItem(
    val id: String,
    val roomNumber: String,
    val roomName: String,
    val roomType: String,
    val zoneName: String,
    val classSeating: String,
    val sectionText: String,
    val examSeating: String
) : Serializable

@Immutable
data class GraduateExamQuery(
    val name: String = "",
    val examNumber: String = "",
    val idNumber: String = ""
) : Serializable

@Immutable
data class GraduateExamScore(
    val name: String,
    val signupNumber: String,
    val examNumber: String,
    val totalScore: String,
    val politicsScore: String,
    val foreignLanguageScore: String,
    val businessOneScore: String,
    val businessTwoScore: String
) : Serializable

@Immutable
data class ElectricityQuery(
    val year: Int = Calendar.getInstance().get(Calendar.YEAR),
    val name: String = "",
    val studentNumber: String = ""
) : Serializable {
    companion object {
        fun yearOptions(currentYear: Int = Calendar.getInstance().get(Calendar.YEAR)): List<Int> {
            return (currentYear downTo 2016).toList()
        }
    }
}

@Immutable
data class ElectricityBill(
    val year: Int,
    val buildingNumber: String,
    val roomNumber: String,
    val peopleNumber: String,
    val department: String,
    val usedElectricAmount: String,
    val freeElectricAmount: String,
    val feeBasedElectricAmount: String,
    val electricPrice: String,
    val totalElectricBill: String,
    val averageElectricBill: String
) : Serializable

@Immutable
data class YellowPageEntry(
    val id: String,
    val section: String,
    val campus: String,
    val majorPhone: String,
    val minorPhone: String,
    val address: String,
    val email: String,
    val website: String
) : Serializable

@Immutable
data class YellowPageCategory(
    val id: String,
    val name: String,
    val items: List<YellowPageEntry>
) : Serializable

@Immutable
data class SpareOption(
    val value: Int,
    val label: String
) : Serializable

private fun localizedSpareText(
    locale: String,
    simplifiedChinese: String,
    english: String,
    traditionalChineseHongKong: String = simplifiedChinese,
    traditionalChineseTaiwan: String = traditionalChineseHongKong,
    japanese: String = english,
    korean: String = english
): String {
    return when (AppLocaleSupport.normalizeLocale(locale)) {
        "zh-HK" -> traditionalChineseHongKong
        "zh-TW" -> traditionalChineseTaiwan
        "en" -> english
        "ja" -> japanese
        "ko" -> korean
        else -> simplifiedChinese
    }
}

fun spareZoneOptions(locale: String): List<SpareOption> = listOf(
    SpareOption(0, localizedSpareText(locale, "不限", "Any", "不限", "不限", "指定なし", "전체")),
    SpareOption(1, localizedSpareText(locale, "海珠校区", "Haizhu Campus", "海珠校區", "海珠校區", "海珠キャンパス", "하이주 캠퍼스")),
    SpareOption(2, localizedSpareText(locale, "花都校区", "Huadu Campus", "花都校區", "花都校區", "花都キャンパス", "화두 캠퍼스")),
    SpareOption(3, localizedSpareText(locale, "广东轻工南海校区", "Nanhai Campus", "廣東輕工南海校區", "廣東輕工南海校區", "南海キャンパス", "난하이 캠퍼스")),
    SpareOption(4, localizedSpareText(locale, "业余函授校区", "Correspondence Campus", "業餘函授校區", "業餘函授校區", "通信教育キャンパス", "통신교육 캠퍼스"))
)

fun spareTypeOptions(locale: String): List<SpareOption> = listOf(
    SpareOption(0, localizedSpareText(locale, "不限", "Any", "不限", "不限", "指定なし", "전체")),
    SpareOption(1, localizedSpareText(locale, "不用课室的课程", "No-room Course", "不用課室的課程", "不用教室的課程", "教室不要の授業", "강의실 불필요 과목")),
    SpareOption(2, localizedSpareText(locale, "操场", "Playground", "操場", "操場", "グラウンド", "운동장")),
    SpareOption(3, localizedSpareText(locale, "大多媒体", "Large Multimedia", "大多媒體", "大型多媒體", "大マルチメディア", "대형 멀티미디어")),
    SpareOption(4, localizedSpareText(locale, "电脑专业机房", "Computer Lab", "電腦專業機房", "電腦專業機房", "コンピュータ専用機房", "컴퓨터 전용 실습실")),
    SpareOption(5, localizedSpareText(locale, "雕塑室", "Sculpture Room", "雕塑室", "雕塑室", "彫刻室", "조각실")),
    SpareOption(6, localizedSpareText(locale, "多媒体教室", "Multimedia Room", "多媒體教室", "多媒體教室", "マルチメディア教室", "멀티미디어 강의실")),
    SpareOption(7, localizedSpareText(locale, "翻译室", "Translation Room", "翻譯室", "翻譯室", "翻訳室", "번역실")),
    SpareOption(8, localizedSpareText(locale, "服装实验室", "Fashion Lab", "服裝實驗室", "服裝實驗室", "ファッション実験室", "패션 실험실")),
    SpareOption(9, localizedSpareText(locale, "钢琴室", "Piano Room", "鋼琴室", "鋼琴室", "ピアノ室", "피아노실")),
    SpareOption(10, localizedSpareText(locale, "钢琴室", "Piano Room", "鋼琴室", "鋼琴室", "ピアノ室", "피아노실")),
    SpareOption(11, localizedSpareText(locale, "公共机房", "Public Computer Lab", "公共機房", "公用機房", "公共機房", "공용 컴퓨터실")),
    SpareOption(12, localizedSpareText(locale, "国画临摹室", "Chinese Painting Room", "國畫臨摹室", "國畫臨摹室", "国画臨模室", "국화 임모실")),
    SpareOption(13, localizedSpareText(locale, "画室", "Art Studio", "畫室", "畫室", "アトリエ", "화실")),
    SpareOption(14, localizedSpareText(locale, "化学实验室", "Chemistry Lab", "化學實驗室", "化學實驗室", "化学実験室", "화학 실험실")),
    SpareOption(15, localizedSpareText(locale, "机房", "Computer Room", "機房", "機房", "機房", "컴퓨터실")),
    SpareOption(16, localizedSpareText(locale, "教具室", "Teaching Aids Room", "教具室", "教具室", "教具室", "교구실")),
    SpareOption(17, localizedSpareText(locale, "教育实验室", "Education Lab", "教育實驗室", "教育實驗室", "教育実験室", "교육 실험실")),
    SpareOption(18, localizedSpareText(locale, "解剖实验室", "Anatomy Lab", "解剖實驗室", "解剖實驗室", "解剖実験室", "해부학 실험실")),
    SpareOption(19, localizedSpareText(locale, "金融数学实验室", "Financial Math Lab", "金融數學實驗室", "金融數學實驗室", "金融数学実験室", "금융수학 실험실")),
    SpareOption(20, localizedSpareText(locale, "美术课室", "Art Classroom", "美術課室", "美術教室", "美術教室", "미술 강의실")),
    SpareOption(21, localizedSpareText(locale, "蒙氏教学法专用课室", "Montessori Room", "蒙氏教學法專用課室", "蒙特梭利教學法專用教室", "モンテッソーリ教室", "몬테소리 교실")),
    SpareOption(22, localizedSpareText(locale, "模型制作实验室", "Model Making Lab", "模型製作實驗室", "模型製作實驗室", "模型製作実験室", "모형 제작 실험실")),
    SpareOption(23, localizedSpareText(locale, "平面制作实验室", "Graphic Design Lab", "平面製作實驗室", "平面製作實驗室", "グラフィック制作実験室", "그래픽 디자인 실험실")),
    SpareOption(24, localizedSpareText(locale, "琴房", "Practice Room", "琴房", "琴房", "練習室", "연습실")),
    SpareOption(25, localizedSpareText(locale, "摄影实验室", "Photography Lab", "攝影實驗室", "攝影實驗室", "写真実験室", "사진 실험실")),
    SpareOption(26, localizedSpareText(locale, "声乐课室", "Vocal Room", "聲樂課室", "聲樂教室", "声楽教室", "성악 강의실")),
    SpareOption(27, localizedSpareText(locale, "生物实验室", "Biology Lab", "生物實驗室", "生物實驗室", "生物実験室", "생물 실험실")),
    SpareOption(28, localizedSpareText(locale, "实训室", "Training Room", "實訓室", "實習室", "実習室", "실습실")),
    SpareOption(29, localizedSpareText(locale, "视唱练耳室", "Sight-singing Room", "視唱練耳室", "視唱練耳室", "視唱練耳室", "시창청음실")),
    SpareOption(30, localizedSpareText(locale, "陶艺室", "Pottery Room", "陶藝室", "陶藝室", "陶芸室", "도예실")),
    SpareOption(31, localizedSpareText(locale, "体操房", "Gymnastics Room", "體操房", "體操房", "体操室", "체조실")),
    SpareOption(32, localizedSpareText(locale, "网络实验室", "Network Lab", "網絡實驗室", "網路實驗室", "ネットワーク実験室", "네트워크 실험실")),
    SpareOption(33, localizedSpareText(locale, "微格课室", "Micro-teaching Room", "微格課室", "微型教學教室", "マイクロティーチング教室", "마이크로티칭 교실")),
    SpareOption(34, localizedSpareText(locale, "无须课室", "No Room Needed", "無須課室", "無須教室", "教室不要", "강의실 불필요")),
    SpareOption(35, localizedSpareText(locale, "舞蹈室", "Dance Room", "舞蹈室", "舞蹈室", "ダンス室", "무용실")),
    SpareOption(36, localizedSpareText(locale, "舞蹈室", "Dance Room", "舞蹈室", "舞蹈室", "ダンス室", "무용실")),
    SpareOption(37, localizedSpareText(locale, "物理实验室", "Physics Lab", "物理實驗室", "物理實驗室", "物理実験室", "물리 실험실")),
    SpareOption(38, localizedSpareText(locale, "小多媒体", "Small Multimedia", "小多媒體", "小型多媒體", "小マルチメディア", "소형 멀티미디어")),
    SpareOption(39, localizedSpareText(locale, "小多媒体(<70)", "Small Multimedia (<70)", "小多媒體(<70)", "小型多媒體(<70)", "小マルチメディア(<70)", "소형 멀티미디어(<70)")),
    SpareOption(40, localizedSpareText(locale, "小普通课室(<70)", "Small Classroom (<70)", "小普通課室(<70)", "小型普通教室(<70)", "小教室(<70)", "소형 일반 강의실(<70)")),
    SpareOption(41, localizedSpareText(locale, "小组课室", "Group Room", "小組課室", "小組教室", "グループ教室", "그룹 강의실")),
    SpareOption(42, localizedSpareText(locale, "形体房", "Fitness Room", "形體房", "形體房", "フィットネス室", "피트니스실")),
    SpareOption(43, localizedSpareText(locale, "音乐室", "Music Room", "音樂室", "音樂室", "音楽室", "음악실")),
    SpareOption(44, localizedSpareText(locale, "音乐专业课室", "Music Major Room", "音樂專業課室", "音樂專業教室", "音楽専門教室", "음악 전공 강의실")),
    SpareOption(45, localizedSpareText(locale, "语音室", "Language Lab", "語音室", "語音室", "語学実験室", "어학 실험실")),
    SpareOption(46, localizedSpareText(locale, "智能录像室", "Smart Recording Room", "智能錄像室", "智慧錄影室", "スマート録画室", "스마트 녹화실")),
    SpareOption(47, localizedSpareText(locale, "中多媒体(70-100)", "Medium Multimedia (70-100)", "中多媒體(70-100)", "中型多媒體(70-100)", "中マルチメディア(70-100)", "중형 멀티미디어(70-100)")),
    SpareOption(48, localizedSpareText(locale, "专业课教室", "Major Classroom", "專業課教室", "專業課教室", "専門教室", "전공 강의실")),
    SpareOption(49, localizedSpareText(locale, "专业理论课室", "Major Theory Room", "專業理論課室", "專業理論教室", "専門理論教室", "전공 이론 강의실")),
    SpareOption(50, localizedSpareText(locale, "专业实验室", "Major Lab", "專業實驗室", "專業實驗室", "専門実験室", "전공 실험실")),
    SpareOption(51, localizedSpareText(locale, "咨询室", "Counseling Room", "諮詢室", "諮詢室", "相談室", "상담실")),
    SpareOption(52, localizedSpareText(locale, "综合绘画实验室", "Comprehensive Painting Lab", "綜合繪畫實驗室", "綜合繪畫實驗室", "総合絵画実験室", "종합 회화 실험실"))
)

fun spareWeekTypeOptions(locale: String): List<SpareOption> = listOf(
    SpareOption(0, localizedSpareText(locale, "不限", "Any", "不限", "不限", "指定なし", "전체")),
    SpareOption(1, localizedSpareText(locale, "单周", "Odd Week", "單週", "單週", "奇数週", "홀수 주")),
    SpareOption(2, localizedSpareText(locale, "双周", "Even Week", "雙週", "雙週", "偶数週", "짝수 주"))
)

fun spareWeekdayOptions(locale: String): List<SpareOption> = listOf(
    SpareOption(-1, localizedSpareText(locale, "不限", "Any", "不限", "不限", "指定なし", "전체")),
    SpareOption(0, localizedSpareText(locale, "周一", "Mon", "週一", "週一", "月", "월")),
    SpareOption(1, localizedSpareText(locale, "周二", "Tue", "週二", "週二", "火", "화")),
    SpareOption(2, localizedSpareText(locale, "周三", "Wed", "週三", "週三", "水", "수")),
    SpareOption(3, localizedSpareText(locale, "周四", "Thu", "週四", "週四", "木", "목")),
    SpareOption(4, localizedSpareText(locale, "周五", "Fri", "週五", "週五", "金", "금")),
    SpareOption(5, localizedSpareText(locale, "周六", "Sat", "週六", "週六", "土", "토")),
    SpareOption(6, localizedSpareText(locale, "周日", "Sun", "週日", "週日", "日", "일"))
)

fun spareClassNumberOptions(locale: String): List<SpareOption> = listOf(
    SpareOption(0, localizedSpareText(locale, "第 1-2 节", "Period 1-2", "第 1-2 節", "第 1-2 節", "第1-2限", "1-2교시")),
    SpareOption(1, localizedSpareText(locale, "第 3 节", "Period 3", "第 3 節", "第 3 節", "第3限", "3교시")),
    SpareOption(2, localizedSpareText(locale, "第 4-5 节", "Period 4-5", "第 4-5 節", "第 4-5 節", "第4-5限", "4-5교시")),
    SpareOption(3, localizedSpareText(locale, "第 6-7 节", "Period 6-7", "第 6-7 節", "第 6-7 節", "第6-7限", "6-7교시")),
    SpareOption(4, localizedSpareText(locale, "第 8-9 节", "Period 8-9", "第 8-9 節", "第 8-9 節", "第8-9限", "8-9교시")),
    SpareOption(5, localizedSpareText(locale, "第 10-12 节", "Period 10-12", "第 10-12 節", "第 10-12 節", "第10-12限", "10-12교시")),
    SpareOption(6, localizedSpareText(locale, "上午", "Morning", "上午", "上午", "午前", "오전")),
    SpareOption(7, localizedSpareText(locale, "下午", "Afternoon", "下午", "下午", "午後", "오후")),
    SpareOption(8, localizedSpareText(locale, "晚上", "Evening", "晚上", "晚上", "夜", "저녁")),
    SpareOption(9, localizedSpareText(locale, "白天", "Daytime", "白天", "白天", "日中", "낮")),
    SpareOption(10, localizedSpareText(locale, "整天", "All Day", "整天", "整天", "終日", "종일"))
)

fun sparePeriodRange(classNumber: Int): Pair<Int, Int> {
    return when (classNumber) {
        0 -> 1 to 2
        1 -> 3 to 3
        2 -> 4 to 5
        3 -> 6 to 7
        4 -> 8 to 9
        5 -> 10 to 12
        6 -> 1 to 4
        7 -> 6 to 9
        8 -> 10 to 12
        9 -> 1 to 9
        10 -> 1 to 12
        else -> 1 to 2
    }
}

fun SpareRoomQuery.selectedWeekdayValue(): Int {
    return if (minWeek == 0 && maxWeek == 6) -1 else minWeek.coerceIn(0, 6)
}
