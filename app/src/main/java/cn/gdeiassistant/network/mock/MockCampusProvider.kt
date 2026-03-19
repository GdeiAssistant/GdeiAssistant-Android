package cn.gdeiassistant.network.mock

import android.util.Base64
import cn.gdeiassistant.model.Charge
import cn.gdeiassistant.model.Cookie
import cn.gdeiassistant.network.mock.MockUtils.formFields
import cn.gdeiassistant.util.ChargeCrypto
import com.google.gson.Gson
import okhttp3.Request
import java.security.KeyPair

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

    val mockChargeKeyPair: KeyPair by lazy { ChargeCrypto.generateRsaKeyPair() }

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

    fun mockCardQuery(request: Request): String = """
        {"success":true,"code":200,"message":"","data":{
            "cardInfo":{"name":"${MockUtils.MOCK_PROFILE_NICKNAME}","number":"${MockUtils.MOCK_STUDENT_NUMBER}","cardBalance":"52.50","cardInterimBalance":"0.00","cardNumber":"8888123456789012","cardLostState":"0","cardFreezeState":"0"},
            "cardList":[
                {"tradeTime":"2024-02-28 12:15:00","merchantName":"第一食堂","tradeName":"午餐消费","tradePrice":"-12.50","accountBalance":"52.50"},
                {"tradeTime":"2024-02-27 18:30:00","merchantName":"校园超市","tradeName":"日用品","tradePrice":"-28.00","accountBalance":"65.00"},
                {"tradeTime":"2024-02-27 07:45:00","merchantName":"第一食堂","tradeName":"早餐消费","tradePrice":"-5.00","accountBalance":"93.00"}
            ]
        }}
    """.trimIndent()

    fun mockCardLost(request: Request): String =
        """{"success":true,"code":200,"message":"挂失成功","data":null}"""

    fun mockBookQuery(request: Request): String = """
        {"success":true,"code":200,"message":"","data":[
            {"id":"b1","sn":"SN1001","code":"TP312.8-01","name":"Android 架构演进实践","author":"GDEI Labs","borrowDate":"2026-02-10","returnDate":"2026-03-20","renewTime":1},
            {"id":"b2","sn":"SN1002","code":"TP393-12","name":"现代移动网络编程","author":"Campus Net","borrowDate":"2026-02-14","returnDate":"2026-03-24","renewTime":0},
            {"id":"b3","sn":"SN1003","code":"I247.5-88","name":"岭南校园纪事","author":"图书馆编辑部","borrowDate":"2026-02-18","returnDate":"2026-03-28","renewTime":2}
        ]}
    """.trimIndent()

    fun mockBookRenew(request: Request): String =
        """{"success":true,"code":200,"message":"续借成功"}"""

    fun mockCollectionSearch(request: Request): String {
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
            """{"bookname":"${MockUtils.escapeJson(name)}","author":"${MockUtils.escapeJson(author)}","publishingHouse":"${MockUtils.escapeJson(publisher)}","detailURL":"${MockUtils.escapeJson(url)}"}"""
        }.joinToString(",")
        return """{"success":true,"code":200,"message":"","data":{"sumPage":$sumPage,"collectionList":[$payload]}}"""
    }

    fun mockCollectionDetail(request: Request): String {
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

    fun mockCollectionBorrow(request: Request): String {
        val payload = """[
            {"id":"b1","sn":"SN1001","code":"TP312.8-01","name":"Android 架构演进实践","author":"GDEI Labs","borrowDate":"2026-02-10","returnDate":"2026-03-20","renewTime":1},
            {"id":"b2","sn":"SN1002","code":"TP393-12","name":"现代移动网络编程","author":"Campus Net","borrowDate":"2026-02-14","returnDate":"2026-03-24","renewTime":0},
            {"id":"b3","sn":"SN1003","code":"I247.5-88","name":"岭南校园纪事","author":"图书馆编辑部","borrowDate":"2026-02-18","returnDate":"2026-03-28","renewTime":2}
        ]"""
        return """{"success":true,"code":200,"message":"","data":$payload}"""
    }

    fun mockCollectionRenew(request: Request): String =
        """{"success":true,"code":200,"message":"续借成功"}"""

    fun mockElectricityFees(request: Request): String {
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

    fun mockYellowPages(request: Request): String {
        val typePayload = mockYellowPageTypes.joinToString(",") { type ->
            """{"typeCode":${type.typeCode},"typeName":"${MockUtils.escapeJson(type.typeName)}"}"""
        }
        val dataPayload = mockYellowPageEntries.joinToString(",") { entry ->
            """{"id":${entry.id},"typeCode":${entry.typeCode},"typeName":"${MockUtils.escapeJson(entry.typeName)}","section":"${MockUtils.escapeJson(entry.section)}","campus":"${MockUtils.escapeJson(entry.campus)}","majorPhone":"${MockUtils.escapeJson(entry.majorPhone)}","minorPhone":"${MockUtils.escapeJson(entry.minorPhone)}","address":"${MockUtils.escapeJson(entry.address)}","email":"${MockUtils.escapeJson(entry.email)}","website":"${MockUtils.escapeJson(entry.website)}"}"""
        }
        return """{"success":true,"code":200,"message":"","data":{"data":[$dataPayload],"type":[$typePayload]}}"""
    }

    fun mockServerPublicKey(request: Request): String = """
        {"success":true,"code":200,"message":"","data":"${Base64.encodeToString(mockChargeKeyPair.public.encoded, Base64.NO_WRAP)}"}
    """.trimIndent()

    fun mockCharge(request: Request): String {
        val fields = request.formFields()
        val encryptedAesKey = fields["clientAESKey"] ?: return MockUtils.failureJson("缺少密钥信息")
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
            val chargeJson = Gson().toJson(charge)
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
            MockUtils.failureJson(it.message ?: "模拟充值失败")
        }
    }
}
