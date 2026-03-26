package cn.gdeiassistant.network.mock

import cn.gdeiassistant.network.mock.MockUtils.formFields
import cn.gdeiassistant.network.mock.MockUtils.getString
import cn.gdeiassistant.network.mock.MockUtils.jsonObjectBody
import cn.gdeiassistant.network.mock.MockUtils.localizedText
import cn.gdeiassistant.network.mock.MockUtils.requestLocale
import okhttp3.Request

/** Mock provider for campus card, charge, book, datacenter (electricity/yellowpages). */
object MockCampusProvider {

    // ── Data classes ────────────────────────────────────────────────────────

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

    // ── Mock data ───────────────────────────────────────────────────────────

    private fun campusText(
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

    private fun mockYellowPageTypes(locale: String) = listOf(
        MockYellowPageTypeRecord(1, campusText(locale, "教学服务", "Teaching Services", "教學服務", "教学サービス", "학사 서비스")),
        MockYellowPageTypeRecord(2, campusText(locale, "行政服务", "Administrative Services", "行政服務", "行政サービス", "행정 서비스")),
        MockYellowPageTypeRecord(3, campusText(locale, "后勤服务", "Logistics Services", "後勤服務", "後方支援サービス", "생활 지원 서비스"))
    )

    private fun mockYellowPageEntries(locale: String) = listOf(
        MockYellowPageEntryRecord(101, 1, campusText(locale, "教学服务", "Teaching Services", "教學服務", "教学サービス", "학사 서비스"), campusText(locale, "教务处", "Academic Affairs Office", "教務處", "教務課", "교무처"), campusText(locale, "白云校区", "Baiyun Campus", "白雲校區", "白雲キャンパス", "바이윈 캠퍼스"), "020-12345678", "020-87654321", campusText(locale, "行政楼 201", "Administration Building 201", "行政樓 201", "管理棟 201", "행정동 201"), "jw@gdeiassistant.cn", "gdeiassistant.cn/jwc"),
        MockYellowPageEntryRecord(102, 1, campusText(locale, "教学服务", "Teaching Services", "教學服務", "教学サービス", "학사 서비스"), campusText(locale, "图书馆服务台", "Library Service Desk", "圖書館服務台", "図書館サービスカウンター", "도서관 안내데스크"), campusText(locale, "白云校区", "Baiyun Campus", "白雲校區", "白雲キャンパス", "바이윈 캠퍼스"), "020-23456789", "", campusText(locale, "图书馆一层服务台", "Library 1F Service Desk", "圖書館一樓服務台", "図書館1階カウンター", "도서관 1층 안내데스크"), "library@gdeiassistant.cn", "gdeiassistant.cn/library"),
        MockYellowPageEntryRecord(201, 2, campusText(locale, "行政服务", "Administrative Services", "行政服務", "行政サービス", "행정 서비스"), campusText(locale, "学生工作部", "Student Affairs Office", "學生工作部", "学生支援課", "학생처"), campusText(locale, "龙洞校区", "Longdong Campus", "龍洞校區", "龍洞キャンパス", "룽둥 캠퍼스"), "020-34567890", "", campusText(locale, "学生事务中心 108", "Student Center 108", "學生事務中心 108", "学生センター 108", "학생지원센터 108"), "student@gdeiassistant.cn", "gdeiassistant.cn/student"),
        MockYellowPageEntryRecord(301, 3, campusText(locale, "后勤服务", "Logistics Services", "後勤服務", "後方支援サービス", "생활 지원 서비스"), campusText(locale, "后勤报修中心", "Maintenance Center", "後勤報修中心", "修繕センター", "시설 수리센터"), campusText(locale, "三水校区", "Sanshui Campus", "三水校區", "三水キャンパス", "싼수이 캠퍼스"), "020-45678901", "020-45678902", campusText(locale, "后勤楼 101", "Logistics Building 101", "後勤樓 101", "後方支援棟 101", "생활지원동 101"), "repair@gdeiassistant.cn", "gdeiassistant.cn/repair")
    )

    // ── Mock endpoints ──────────────────────────────────────────────────────

    fun mockCardInfo(request: Request): String = """
        {"success":true,"code":200,"message":"","data":{
            "name":"${MockUtils.MOCK_PROFILE_NICKNAME}",
            "number":"${MockUtils.MOCK_STUDENT_NUMBER}",
            "cardBalance":"52.50",
            "cardInterimBalance":"0.00",
            "cardNumber":"8888123456789012",
            "cardLostState":"0",
            "cardFreezeState":"0"
        }}
    """.trimIndent()

    fun mockCardQuery(request: Request): String {
        val locale = request.requestLocale()
        return """
        {"success":true,"code":200,"message":"","data":{
            "cardInfo":{"name":"${MockUtils.MOCK_PROFILE_NICKNAME}","number":"${MockUtils.MOCK_STUDENT_NUMBER}","cardBalance":"52.50","cardInterimBalance":"0.00","cardNumber":"8888123456789012","cardLostState":"0","cardFreezeState":"0"},
            "cardList":[
                {"tradeTime":"2024-02-28 12:15:00","merchantName":"${MockUtils.escapeJson(campusText(locale, "第一食堂", "Cafeteria No. 1", "第一食堂", "第一食堂", "제1식당"))}","tradeName":"${MockUtils.escapeJson(campusText(locale, "午餐消费", "Lunch", "午餐消費", "昼食", "점심"))}","tradePrice":"-12.50","accountBalance":"52.50"},
                {"tradeTime":"2024-02-27 18:30:00","merchantName":"${MockUtils.escapeJson(campusText(locale, "校园超市", "Campus Store", "校園超市", "学内ストア", "캠퍼스 마트"))}","tradeName":"${MockUtils.escapeJson(campusText(locale, "日用品", "Daily Supplies", "日用品", "日用品", "생활용품"))}","tradePrice":"-28.00","accountBalance":"65.00"},
                {"tradeTime":"2024-02-27 07:45:00","merchantName":"${MockUtils.escapeJson(campusText(locale, "第一食堂", "Cafeteria No. 1", "第一食堂", "第一食堂", "제1식당"))}","tradeName":"${MockUtils.escapeJson(campusText(locale, "早餐消费", "Breakfast", "早餐消費", "朝食", "아침 식사"))}","tradePrice":"-5.00","accountBalance":"93.00"}
            ]
        }}
    """.trimIndent()
    }

    fun mockCardLost(request: Request): String =
        """{"success":true,"code":200,"message":"${MockUtils.escapeJson(campusText(request.requestLocale(), "挂失成功", "Card reported as lost", "掛失成功", "利用停止にしました", "분실 신고가 완료되었습니다"))}","data":null}"""

    fun mockLibraryBorrowQuery(request: Request): String {
        val locale = request.requestLocale()
        return """
        {"success":true,"code":200,"message":"","data":[
            {"id":"b1","sn":"SN1001","code":"TP312.8-01","name":"${MockUtils.escapeJson(campusText(locale, "Android 架构演进实践", "Android Architecture in Practice", "Android 架構演進實踐", "Android アーキテクチャ実践", "Android 아키텍처 실전"))}","author":"GDEI Labs","borrowDate":"2026-02-10","returnDate":"2026-03-20","renewTime":1},
            {"id":"b2","sn":"SN1002","code":"TP393-12","name":"${MockUtils.escapeJson(campusText(locale, "现代移动网络编程", "Modern Mobile Networking", "現代移動網路編程", "現代モバイルネットワーク", "현대 모바일 네트워크 프로그래밍"))}","author":"Campus Net","borrowDate":"2026-02-14","returnDate":"2026-03-24","renewTime":0},
            {"id":"b3","sn":"SN1003","code":"I247.5-88","name":"${MockUtils.escapeJson(campusText(locale, "岭南校园纪事", "Lingnan Campus Stories", "嶺南校園紀事", "嶺南キャンパス物語", "영남 캠퍼스 이야기"))}","author":"${MockUtils.escapeJson(campusText(locale, "图书馆编辑部", "Library Editorial Team", "圖書館編輯部", "図書館編集部", "도서관 편집부"))}","borrowDate":"2026-02-18","returnDate":"2026-03-28","renewTime":2}
        ]}
    """.trimIndent()
    }

    fun mockLibraryRenew(request: Request): String =
        """{"success":true,"code":200,"message":"${MockUtils.escapeJson(campusText(request.requestLocale(), "续借成功", "Renewed successfully", "續借成功", "延長に成功しました", "연장에 성공했습니다"))}"}"""

    fun mockCollectionSearch(request: Request): String {
        val locale = request.requestLocale()
        val keyword = request.url.queryParameter("keyword").orEmpty()
        val page = request.url.queryParameter("page")?.toIntOrNull()?.coerceAtLeast(1) ?: 1
        val pageSize = 5
        val all = listOf(
            Triple(campusText(locale, "Android 架构演进实践", "Android Architecture in Practice", "Android 架構演進實踐", "Android アーキテクチャ実践", "Android 아키텍처 실전"), "GDEI Labs", campusText(locale, "广东第二师范学院出版社", "Guangdong University of Education Press", "廣東第二師範學院出版社", "広東第二師範学院出版", "광둥 제2사범대학교 출판사")),
            Triple(campusText(locale, "现代移动网络编程", "Modern Mobile Networking", "現代移動網路編程", "現代モバイルネットワーク", "현대 모바일 네트워크 프로그래밍"), "Campus Net", campusText(locale, "计算机科学出版社", "Computer Science Press", "計算機科學出版社", "コンピュータ科学出版社", "컴퓨터과학 출판사")),
            Triple(campusText(locale, "岭南校园纪事", "Lingnan Campus Stories", "嶺南校園紀事", "嶺南キャンパス物語", "영남 캠퍼스 이야기"), campusText(locale, "图书馆编辑部", "Library Editorial Team", "圖書館編輯部", "図書館編集部", "도서관 편집부"), campusText(locale, "岭南出版社", "Lingnan Press", "嶺南出版社", "嶺南出版社", "영남 출판사")),
            Triple(campusText(locale, "Kotlin 协程实战", "Kotlin Coroutines in Practice", "Kotlin 協程實戰", "Kotlin Coroutine 実践", "Kotlin 코루틴 실전"), campusText(locale, "JetBrains 社区", "JetBrains Community", "JetBrains 社群", "JetBrains コミュニティ", "JetBrains 커뮤니티"), campusText(locale, "技术出版社", "Tech Press", "技術出版社", "技術出版社", "기술 출판사")),
            Triple(campusText(locale, "数据结构与算法分析", "Data Structures and Algorithm Analysis", "資料結構與算法分析", "データ構造とアルゴリズム解析", "자료구조와 알고리즘 분석"), "Mark Allen Weiss", campusText(locale, "机械工业出版社", "China Machine Press", "機械工業出版社", "機械工業出版社", "기계공업 출판사")),
            Triple(campusText(locale, "计算机网络：自顶向下方法", "Computer Networking: A Top-Down Approach", "計算機網路：自頂向下方法", "コンピュータネットワーク: トップダウン方式", "컴퓨터 네트워킹: 탑다운 접근"), "Kurose & Ross", campusText(locale, "机械工业出版社", "China Machine Press", "機械工業出版社", "機械工業出版社", "기계공업 출판사")),
            Triple(campusText(locale, "软件工程：实践者的研究方法", "Software Engineering: A Practitioners Approach", "軟體工程：實踐者的研究方法", "ソフトウェア工学: 実践者のアプローチ", "소프트웨어 공학: 실무자 접근"), "Roger Pressman", campusText(locale, "机械工业出版社", "China Machine Press", "機械工業出版社", "機械工業出版社", "기계공업 출판사")),
            Triple(campusText(locale, "人工智能导论", "Introduction to Artificial Intelligence", "人工智能導論", "人工知能入門", "인공지능 입문"), campusText(locale, "周志华", "Zhou Zhihua", "周志華", "周志華", "저우 즈화"), campusText(locale, "清华大学出版社", "Tsinghua University Press", "清華大學出版社", "清華大学出版社", "칭화대 출판사"))
        )
        val matched = if (keyword.isBlank()) all
            else all.filter { (name, author, _) ->
                name.contains(keyword, ignoreCase = true) || author.contains(keyword, ignoreCase = true)
            }
        val sumPage = maxOf(1, (matched.size + pageSize - 1) / pageSize)
        val paged = matched.drop((page - 1) * pageSize).take(pageSize)
        val payload = paged.mapIndexed { i, (name, author, publisher) ->
            val url = "https://lib.gdeiassistant.cn/book/detail?id=${page * 100 + i + 1}"
            """{"bookname":"${MockUtils.escapeJson(name)}","author":"${MockUtils.escapeJson(author)}","publishingHouse":"${MockUtils.escapeJson(publisher)}","detailURL":"${MockUtils.escapeJson(url)}"}"""
        }.joinToString(",")
        return """{"success":true,"code":200,"message":"","data":{"sumPage":$sumPage,"collectionList":[$payload]}}"""
    }

    fun mockCollectionDetail(request: Request): String {
        val locale = request.requestLocale()
        val detailUrl = request.url.queryParameter("detailURL").orEmpty()
        val id = detailUrl.substringAfterLast("id=").ifBlank { "001" }
        return """{"success":true,"code":200,"message":"","data":{
            "bookname":"${MockUtils.escapeJson(campusText(locale, "Android 架构演进实践", "Android Architecture in Practice", "Android 架構演進實踐", "Android アーキテクチャ実践", "Android 아키텍처 실전"))}",
            "author":"GDEI Labs",
            "principal":"${MockUtils.escapeJson(campusText(locale, "GDEI Labs 编", "Edited by GDEI Labs", "GDEI Labs 編", "GDEI Labs 編", "GDEI Labs 편"))}",
            "personalPrincipal":"",
            "publishingHouse":"${MockUtils.escapeJson(campusText(locale, "广东第二师范学院出版社", "Guangdong University of Education Press", "廣東第二師範學院出版社", "広東第二師範学院出版", "광둥 제2사범대학교 출판사"))}",
            "price":"58.00",
            "physicalDescriptionArea":"${MockUtils.escapeJson(campusText(locale, "320页；26cm", "320 pages; 26 cm", "320頁；26cm", "320ページ; 26cm", "320쪽; 26cm"))}",
            "subjectTheme":"${MockUtils.escapeJson(campusText(locale, "Android 移动开发", "Android Mobile Development", "Android 移動開發", "Android モバイル開発", "Android 모바일 개발"))}",
            "chineseLibraryClassification":"TP312.8",
            "collectionDistributionList":[
                {"location":"${MockUtils.escapeJson(campusText(locale, "白云校区图书馆 三楼", "Baiyun Campus Library 3F", "白雲校區圖書館 三樓", "白雲キャンパス図書館 3F", "바이윈 캠퍼스 도서관 3층"))}","callNumber":"TP312.8/G001","barcode":"LIB${id}A","state":"${MockUtils.escapeJson(campusText(locale, "在馆", "Available", "在館", "所蔵中", "비치중"))}"},
                {"location":"${MockUtils.escapeJson(campusText(locale, "白云校区图书馆 三楼", "Baiyun Campus Library 3F", "白雲校區圖書館 三樓", "白雲キャンパス図書館 3F", "바이윈 캠퍼스 도서관 3층"))}","callNumber":"TP312.8/G001","barcode":"LIB${id}B","state":"${MockUtils.escapeJson(campusText(locale, "借出", "Checked Out", "借出", "貸出中", "대출중"))}"},
                {"location":"${MockUtils.escapeJson(campusText(locale, "龙洞校区图书馆", "Longdong Campus Library", "龍洞校區圖書館", "龍洞キャンパス図書館", "룽둥 캠퍼스 도서관"))}","callNumber":"TP312.8/G001","barcode":"LIB${id}C","state":"${MockUtils.escapeJson(campusText(locale, "在馆", "Available", "在館", "所蔵中", "비치중"))}"}
            ]
        }}""".trimIndent()
    }

    fun mockCollectionBorrow(request: Request): String {
        if (request.url.queryParameter("password").orEmpty().trim().isEmpty()) {
            return MockUtils.failureJson(campusText(request.requestLocale(), "请输入图书馆密码后再查询借阅", "Please enter your library password before checking borrowing records", "請輸入圖書館密碼後再查詢借閱", "貸出情報を確認する前に図書館パスワードを入力してください", "대출 내역을 조회하려면 도서관 비밀번호를 입력해 주세요"))
        }
        return mockLibraryBorrowQuery(request)
    }

    fun mockCollectionRenew(request: Request): String =
        if (request.jsonObjectBody()?.getString("password")?.trim().isNullOrEmpty()) {
            MockUtils.failureJson(campusText(request.requestLocale(), "请提供图书馆密码", "Please provide your library password", "請提供圖書館密碼", "図書館パスワードを入力してください", "도서관 비밀번호를 입력해 주세요"))
        } else {
            """{"success":true,"code":200,"message":"${MockUtils.escapeJson(campusText(request.requestLocale(), "续借成功", "Renewed successfully", "續借成功", "延長に成功しました", "연장에 성공했습니다"))}"}"""
        }

    fun mockElectricityFees(request: Request): String {
        val locale = request.requestLocale()
        val fields = request.formFields()
        val year = fields["year"]?.toIntOrNull() ?: 2026
        val studentNumber = fields["number"].orEmpty()
        val roomTail = studentNumber.takeLast(3).ifBlank { "318" }
        return """
            {"success":true,"code":200,"message":"","data":{
                "year":$year,
                "buildingNumber":"${MockUtils.escapeJson(campusText(locale, "5栋", "Building 5", "5棟", "5号館", "5동"))}",
                "roomNumber":$roomTail,
                "peopleNumber":4,
                "department":"${MockUtils.escapeJson(campusText(locale, "计算机学院", "School of Computer Science", "計算機學院", "情報科学学院", "컴퓨터학부"))}",
                "usedElectricAmount":126.50,
                "freeElectricAmount":35.00,
                "feeBasedElectricAmount":91.50,
                "electricPrice":0.68,
                "totalElectricBill":62.22,
                "averageElectricBill":15.56
            }}
        """.trimIndent()
    }

    fun mockYellowPages(request: Request): String {
        val locale = request.requestLocale()
        val typePayload = mockYellowPageTypes(locale).joinToString(",") { type ->
            """{"typeCode":${type.typeCode},"typeName":"${MockUtils.escapeJson(type.typeName)}"}"""
        }
        val dataPayload = mockYellowPageEntries(locale).joinToString(",") { entry ->
            """{"id":${entry.id},"typeCode":${entry.typeCode},"typeName":"${MockUtils.escapeJson(entry.typeName)}","section":"${MockUtils.escapeJson(entry.section)}","campus":"${MockUtils.escapeJson(entry.campus)}","majorPhone":"${MockUtils.escapeJson(entry.majorPhone)}","minorPhone":"${MockUtils.escapeJson(entry.minorPhone)}","address":"${MockUtils.escapeJson(entry.address)}","email":"${MockUtils.escapeJson(entry.email)}","website":"${MockUtils.escapeJson(entry.website)}"}"""
        }
        return """{"success":true,"code":200,"message":"","data":{"data":[$dataPayload],"type":[$typePayload]}}"""
    }

    fun mockCharge(request: Request): String {
        val fields = request.formFields()
        val amount = fields["amount"].orEmpty().ifBlank { "0" }
        val password = fields["password"].orEmpty()
        if (password.isBlank()) return MockUtils.failureJson(campusText(request.requestLocale(), "密码不能为空", "Password cannot be empty", "密碼不能為空", "パスワードを入力してください", "비밀번호를 입력해 주세요"))
        return """{"success":true,"code":200,"message":"","data":{"alipayURL":"https://gdeiassistant.cn/?mockCharge=$amount","cookieList":[{"name":"mock_charge_session","value":"session_${System.currentTimeMillis()}","domain":"gdeiassistant.cn"}]}}"""
    }
}
