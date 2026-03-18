package cn.gdeiassistant.data

/** 广东第二师范学院院系及专业数据 */
object GdeiFacultyData {

    val majorsByFaculty: LinkedHashMap<String, List<String>> = linkedMapOf(
        "教育学院" to listOf("教育学", "小学教育", "学前教育", "特殊教育"),
        "马克思主义学院" to listOf("思想政治教育"),
        "文学院" to listOf("汉语言文学", "汉语国际教育"),
        "外国语学院" to listOf("英语", "日语"),
        "数学学院" to listOf("数学与应用数学", "统计学", "信息与计算科学"),
        "物理与电气工程学院" to listOf("物理学", "电子信息工程", "电气工程及其自动化"),
        "化学与生命科学学院" to listOf("化学", "生物科学", "食品质量与安全"),
        "计算机科学学院" to listOf("计算机科学与技术", "软件工程", "数据科学与大数据技术", "物联网工程"),
        "政治与法学学院" to listOf("思想政治教育", "法学"),
        "音乐学院" to listOf("音乐学", "音乐表演"),
        "美术学院" to listOf("美术学", "视觉传达设计", "动画"),
        "体育学院" to listOf("体育教育", "运动训练", "社会体育指导与管理"),
        "地理与旅游学院" to listOf("地理科学", "旅游管理", "人文地理与城乡规划"),
        "经济管理学院" to listOf("国际经济与贸易", "工商管理", "会计学"),
        "教育技术与传播学院" to listOf("教育技术学", "网络与新媒体")
    )

    val faculties: List<String> get() = majorsByFaculty.keys.toList()

    fun getMajorsForFaculty(faculty: String): List<String> =
        majorsByFaculty[faculty] ?: emptyList()
}
