package cn.gdeiassistant.network.mock

import cn.gdeiassistant.network.mock.MockUtils.formFields
import cn.gdeiassistant.network.mock.MockUtils.itemIdFromPath
import cn.gdeiassistant.network.mock.MockUtils.multipartField
import cn.gdeiassistant.network.mock.MockUtils.multipartImageCount
import cn.gdeiassistant.network.mock.MockUtils.pathValueAfter
import okhttp3.Request

/** Mock provider for marketplace, lostfound, secret, express, topic, delivery, photograph, dating. */
object MockCommunityProvider {

    // ── Data classes ────────────────────────────────────────────────────────

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

    // ── Payload converters ──────────────────────────────────────────────────

    private fun MockMarketplaceItemRecord.toMarketplaceItemPayload(): Map<String, Any?> {
        return linkedMapOf(
            "id" to id, "username" to username, "name" to name,
            "description" to description, "price" to price, "location" to location,
            "type" to type, "qq" to qq, "phone" to phone, "state" to state,
            "publishTime" to publishTime, "pictureURL" to pictureURL
        )
    }

    private fun MockMarketplaceProfileRecord.toMarketplaceProfilePayload(): Map<String, Any?> {
        return linkedMapOf(
            "avatarURL" to avatarURL, "username" to username, "nickname" to nickname,
            "faculty" to faculty, "enrollment" to enrollment, "major" to major
        )
    }

    private fun MockLostFoundItemRecord.toLostFoundItemPayload(): Map<String, Any?> {
        return linkedMapOf(
            "id" to id, "username" to username, "name" to name,
            "description" to description, "location" to location, "itemType" to itemType,
            "lostType" to lostType, "qq" to qq, "wechat" to wechat, "phone" to phone,
            "state" to state, "publishTime" to publishTime, "pictureURL" to pictureURL
        )
    }

    private fun MockLostFoundProfileRecord.toLostFoundProfilePayload(): Map<String, Any?> {
        return linkedMapOf("avatarURL" to avatarURL, "username" to username, "nickname" to nickname)
    }

    private fun MockSecretPostRecord.toSecretPayload(includeComments: Boolean): Map<String, Any?> {
        return linkedMapOf(
            "id" to id, "username" to username, "content" to content,
            "theme" to theme, "type" to type, "timer" to timer, "state" to state,
            "publishTime" to publishTime, "likeCount" to likeCount,
            "commentCount" to commentCount, "liked" to if (liked) 1 else 0,
            "voiceURL" to voiceURL,
            "secretCommentList" to if (includeComments) {
                mockSecretComments
                    .filter { it.contentId == id }
                    .sortedByDescending { it.publishTime }
                    .map { it.toSecretCommentPayload() }
            } else null
        )
    }

    private fun MockSecretCommentRecord.toSecretCommentPayload(): Map<String, Any?> {
        return linkedMapOf(
            "id" to id, "contentId" to contentId, "username" to username,
            "comment" to comment, "publishTime" to publishTime, "avatarTheme" to avatarTheme
        )
    }

    private fun MockDatingProfileRecord.toDatingProfilePayload(): Map<String, Any?> {
        return linkedMapOf(
            "profileId" to profileId, "username" to username, "nickname" to nickname,
            "grade" to grade, "faculty" to faculty, "hometown" to hometown,
            "content" to content, "qq" to qq, "wechat" to wechat,
            "area" to area, "state" to state, "pictureURL" to pictureURL
        )
    }

    private fun MockDatingPickRecord.toDatingPickPayload(): Map<String, Any?> {
        return linkedMapOf(
            "pickId" to pickId, "roommateProfile" to profile.toDatingProfilePayload(),
            "username" to username, "content" to content, "state" to state
        )
    }

    private fun MockExpressPostRecord.toExpressPayload(): Map<String, Any?> {
        return linkedMapOf(
            "id" to id, "username" to username, "nickname" to nickname,
            "realname" to realname, "selfGender" to selfGender, "name" to name,
            "content" to content, "personGender" to personGender,
            "publishTime" to publishTime, "likeCount" to likeCount, "liked" to liked,
            "commentCount" to commentCount, "guessCount" to guessCount,
            "guessSum" to guessSum, "canGuess" to canGuess
        )
    }

    private fun MockExpressCommentRecord.toExpressCommentPayload(): Map<String, Any?> {
        return linkedMapOf(
            "id" to id, "username" to username, "nickname" to nickname,
            "expressId" to expressId, "comment" to comment, "publishTime" to publishTime
        )
    }

    private fun MockTopicPostRecord.toTopicPayload(): Map<String, Any?> {
        return linkedMapOf(
            "id" to id, "username" to username, "topic" to topic,
            "content" to content, "count" to count, "publishTime" to publishTime,
            "likeCount" to likeCount, "liked" to liked,
            "firstImageUrl" to firstImageUrl, "imageUrls" to imageUrls
        )
    }

    private fun MockDeliveryOrderRecord.toDeliveryOrderPayload(): Map<String, Any?> {
        return linkedMapOf(
            "orderId" to orderId, "username" to username, "orderTime" to orderTime,
            "name" to name, "number" to number, "phone" to phone, "price" to price,
            "company" to company, "address" to address, "state" to state, "remarks" to remarks
        )
    }

    private fun MockDeliveryTradeRecord.toDeliveryTradePayload(): Map<String, Any?> {
        return linkedMapOf(
            "tradeId" to tradeId, "orderId" to orderId, "createTime" to createTime,
            "username" to username, "state" to state
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
            "id" to id, "title" to title, "content" to content,
            "count" to photoCount(), "type" to type, "username" to username,
            "createTime" to createTime, "likeCount" to likeCount,
            "commentCount" to commentCount, "liked" to if (liked) 1 else 0,
            "firstImageUrl" to (firstImageUrl ?: resolvedImageUrls().firstOrNull()),
            "imageUrls" to resolvedImageUrls(),
            "photographCommentList" to if (includeComments) {
                mockPhotographCommentRecords
                    .filter { it.photoId == id }
                    .sortedByDescending { it.createTime }
                    .map { it.toPhotographCommentPayload() }
            } else null
        )
    }

    private fun MockPhotographCommentRecord.toPhotographCommentPayload(): Map<String, Any?> {
        return linkedMapOf(
            "commentId" to commentId, "photoId" to photoId, "username" to username,
            "nickname" to nickname, "comment" to comment, "createTime" to createTime
        )
    }

    // ── findById helpers ────────────────────────────────────────────────────

    private fun MutableList<MockMarketplaceItemRecord>.findById(id: String?): MockMarketplaceItemRecord? =
        firstOrNull { it.id.toString() == id }

    private fun MutableList<MockLostFoundItemRecord>.findById(id: String?): MockLostFoundItemRecord? =
        firstOrNull { it.id.toString() == id }

    private fun MutableList<MockSecretPostRecord>.findById(id: String?): MockSecretPostRecord? =
        firstOrNull { it.id.toString() == id }

    private fun MutableList<MockDatingProfileRecord>.findById(id: String?): MockDatingProfileRecord? =
        firstOrNull { it.profileId.toString() == id }

    private fun MutableList<MockExpressPostRecord>.findById(id: String?): MockExpressPostRecord? =
        firstOrNull { it.id.toString() == id }

    private fun MutableList<MockTopicPostRecord>.findById(id: String?): MockTopicPostRecord? =
        firstOrNull { it.id.toString() == id }

    private fun MutableList<MockDeliveryOrderRecord>.findById(id: String?): MockDeliveryOrderRecord? =
        firstOrNull { it.orderId.toString() == id }

    private fun MutableList<MockPhotographPostRecord>.findById(id: String?): MockPhotographPostRecord? =
        firstOrNull { it.id.toString() == id }

    // ── Mock data ───────────────────────────────────────────────────────────

    val mockMarketplaceProfiles = listOf(
        MockMarketplaceProfileRecord(
            avatarURL = "",
            username = MockUtils.MOCK_CURRENT_USERNAME,
            nickname = MockUtils.MOCK_PROFILE_NICKNAME,
            faculty = 11,
            enrollment = 2023,
            major = "软件工程"
        ),
        MockMarketplaceProfileRecord(
            avatarURL = "",
            username = "2023002003",
            nickname = "林学长",
            faculty = 11,
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
            id = 4101, username = MockUtils.MOCK_CURRENT_USERNAME,
            name = "95 新蓝牙耳机",
            description = "日常通勤使用，续航稳定，附原装充电盒和备用耳帽。",
            price = "168.00", location = "白云校区图书馆门口", type = 4,
            qq = "214365870", phone = "13800138000", state = 1,
            publishTime = "2026-03-15 18:40",
            pictureURL = listOf("https://picsum.photos/seed/gdei-market-1/960/720")
        ),
        MockMarketplaceItemRecord(
            id = 4102, username = "2023002003",
            name = "公路自行车头盔",
            description = "买车时一起入的头盔，尺寸 M，适合日常校内骑行。",
            price = "96.00", location = "龙洞校区宿舍区", type = 6,
            qq = "33221144", phone = "", state = 1,
            publishTime = "2026-03-14 21:15",
            pictureURL = emptyList()
        ),
        MockMarketplaceItemRecord(
            id = 4103, username = "2022003018",
            name = "高数教材与笔记",
            description = "教材九成新，附期末整理笔记和章节错题总结，适合补基础。",
            price = "28.00", location = "白云校区教学楼 A 栋", type = 8,
            qq = "87651234", phone = "", state = 1,
            publishTime = "2026-03-13 11:05",
            pictureURL = listOf("https://picsum.photos/seed/gdei-market-2/960/720")
        ),
        MockMarketplaceItemRecord(
            id = 4104, username = MockUtils.MOCK_CURRENT_USERNAME,
            name = "折叠宿舍小桌",
            description = "搬宿舍后闲置，桌面无明显磕碰，适合床上学习和放平板。",
            price = "45.00", location = "三水校区生活区", type = 11,
            qq = "214365870", phone = "13800138000", state = 2,
            publishTime = "2026-03-11 16:20",
            pictureURL = emptyList()
        ),
        MockMarketplaceItemRecord(
            id = 4105, username = MockUtils.MOCK_CURRENT_USERNAME,
            name = "平板保护壳",
            description = "尺寸不匹配所以闲置，适合 10.9 寸设备，边角无破损。",
            price = "20.00", location = "校内快递站附近", type = 3,
            qq = "214365870", phone = "", state = 0,
            publishTime = "2026-03-09 13:50",
            pictureURL = listOf("https://picsum.photos/seed/gdei-market-3/960/720")
        )
    )

    val mockLostFoundProfiles = listOf(
        MockLostFoundProfileRecord("", MockUtils.MOCK_CURRENT_USERNAME, MockUtils.MOCK_PROFILE_NICKNAME),
        MockLostFoundProfileRecord("", "2023002003", "林学长"),
        MockLostFoundProfileRecord("", "2022003018", "陈同学")
    )

    val mockLostFoundItems = mutableListOf(
        MockLostFoundItemRecord(
            id = 5101, username = MockUtils.MOCK_CURRENT_USERNAME,
            name = "遗失黑色校园卡卡套",
            description = "内含校园卡和一张图书馆借阅卡，今天中午在食堂附近丢失。",
            location = "白云校区第一食堂", itemType = 1, lostType = 0,
            qq = "214365870", wechat = MockUtils.MOCK_PROFILE_WECHAT,
            phone = "13800138000", state = 0,
            publishTime = "2026-03-15 12:35",
            pictureURL = listOf("https://picsum.photos/seed/gdei-lost-1/960/720")
        ),
        MockLostFoundItemRecord(
            id = 5102, username = "2023002003",
            name = "拾到一把宿舍钥匙",
            description = "钥匙圈上有蓝色挂件，已先放在宿管阿姨处，可凭特征领取。",
            location = "龙洞校区 6 栋楼下", itemType = 5, lostType = 1,
            qq = "33221144", wechat = "", phone = "", state = 0,
            publishTime = "2026-03-14 20:10",
            pictureURL = emptyList()
        ),
        MockLostFoundItemRecord(
            id = 5103, username = MockUtils.MOCK_CURRENT_USERNAME,
            name = "寻找灰色水杯",
            description = "杯身贴有课程表贴纸，可能落在综合楼自习室或图书馆。",
            location = "综合楼 / 图书馆", itemType = 11, lostType = 0,
            qq = "214365870", wechat = MockUtils.MOCK_PROFILE_WECHAT,
            phone = "", state = 1,
            publishTime = "2026-03-10 09:30",
            pictureURL = emptyList()
        ),
        MockLostFoundItemRecord(
            id = 5104, username = MockUtils.MOCK_CURRENT_USERNAME,
            name = "拾到学生证一本",
            description = "在操场看台附近拾到学生证，封皮略旧，请失主联系认领。",
            location = "白云校区操场看台", itemType = 2, lostType = 1,
            qq = "214365870", wechat = "", phone = "13800138000", state = 0,
            publishTime = "2026-03-13 17:20",
            pictureURL = listOf("https://picsum.photos/seed/gdei-lost-2/960/720")
        )
    )

    val mockSecretPosts = mutableListOf(
        MockSecretPostRecord(
            id = 6101, username = "匿名同学 A",
            content = "最近在赶课程设计，虽然每天都很忙，但在图书馆看到晚霞的时候还是会觉得这学期没白熬。",
            theme = 1, type = 0, timer = 0, state = 0,
            publishTime = "2026-03-15 20:30", likeCount = 12, commentCount = 2, liked = false
        ),
        MockSecretPostRecord(
            id = 6102, username = MockUtils.MOCK_CURRENT_USERNAME,
            content = "如果有一天真的能把校园里这些分散的服务都整理好，应该会很有成就感吧。",
            theme = 4, type = 0, timer = 1, state = 1,
            publishTime = "2026-03-14 23:10", likeCount = 18, commentCount = 1, liked = true
        ),
        MockSecretPostRecord(
            id = 6103, username = "匿名同学 B",
            content = "这是一条语音树洞，点进去可以听到完整内容。",
            theme = 7, type = 1, timer = 0, state = 0,
            publishTime = "2026-03-13 18:05", likeCount = 7, commentCount = 0, liked = false,
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
            profileId = 8101, username = MockUtils.MOCK_CURRENT_USERNAME,
            nickname = MockUtils.MOCK_PROFILE_NICKNAME, grade = 3,
            faculty = "计算机科学系", hometown = "汕头",
            content = "能聊天、会运动、作息稳定。",
            qq = "214365870", wechat = MockUtils.MOCK_PROFILE_WECHAT,
            area = 1, state = 1
        ),
        MockDatingProfileRecord(
            profileId = 8102, username = "2023002003",
            nickname = "晴天", grade = 2,
            faculty = "英语教育", hometown = "佛山",
            content = "喜欢夜跑和音乐节。",
            qq = "55667788", wechat = "sunny-run",
            area = 0, state = 1
        ),
        MockDatingProfileRecord(
            profileId = 8103, username = "2022003018",
            nickname = "晚风", grade = 4,
            faculty = "汉语言文学", hometown = "深圳",
            content = "周末常去图书馆和咖啡馆。",
            qq = "88990011", wechat = "wanfeng_reading",
            area = 0, state = 1
        )
    )

    val mockDatingReceivedPicks = mutableListOf(
        MockDatingPickRecord(
            pickId = 9101, profile = mockDatingProfiles[1],
            username = "2023002003", content = "看你也喜欢夜跑，想认识一下。",
            state = 0, updatedAt = "2026-03-15 22:00"
        )
    )

    val mockDatingSentPicks = mutableListOf(
        MockDatingPickRecord(
            pickId = 9102, profile = mockDatingProfiles[2],
            username = MockUtils.MOCK_CURRENT_USERNAME, content = "周末一起去图书馆吗？",
            state = 1, updatedAt = "2026-03-14 19:20"
        )
    )

    val mockExpressPosts = mutableListOf(
        MockExpressPostRecord(
            id = 10101, username = "2023002003", nickname = "晴天",
            realname = "陈晴", selfGender = 1, name = "阿哲",
            content = "昨晚在操场散步的时候又遇到你了，想认真和你说一句：你笑起来的时候真的会让人记很久。",
            personGender = 0, publishTime = "2026-03-15 21:45",
            likeCount = 34, liked = false, commentCount = 2,
            guessCount = 1, guessSum = 6, canGuess = true
        ),
        MockExpressPostRecord(
            id = 10102, username = MockUtils.MOCK_CURRENT_USERNAME,
            nickname = MockUtils.MOCK_PROFILE_NICKNAME, realname = "林知远",
            selfGender = 2, name = "图书馆四楼靠窗的你",
            content = "谢谢你上周把落下的耳机递给我，也谢谢你顺手提醒我别忘了借书。虽然不知道你叫什么，但还是想把这份好感留在这里。",
            personGender = 2, publishTime = "2026-03-14 18:20",
            likeCount = 19, liked = true, commentCount = 3,
            guessCount = 0, guessSum = 0, canGuess = false
        ),
        MockExpressPostRecord(
            id = 10103, username = "2022003018", nickname = "晚风",
            realname = "周岚", selfGender = 0, name = "小周",
            content = "你总是在早八前把实验室门口的灯打开，大家都还没完全醒的时候，你已经准备好一整天了。",
            personGender = 1, publishTime = "2026-03-13 08:10",
            likeCount = 11, liked = false, commentCount = 1,
            guessCount = 0, guessSum = 2, canGuess = true
        )
    )

    val mockExpressComments = mutableListOf(
        MockExpressCommentRecord(11101, "2023002005", "晚课同学", 10101, "这条好真诚，希望你能被看到。", "2026-03-15 22:03"),
        MockExpressCommentRecord(11102, "2022003018", "隔壁班同学", 10101, "操场的故事总是很浪漫。", "2026-03-15 22:26"),
        MockExpressCommentRecord(11103, "2023002003", "小林", 10102, "图书馆真的很容易发生一些温柔的小事。", "2026-03-14 19:10"),
        MockExpressCommentRecord(11104, MockUtils.MOCK_CURRENT_USERNAME, MockUtils.MOCK_PROFILE_NICKNAME, 10102, "如果真的再遇见，也许可以主动打个招呼。", "2026-03-14 20:02"),
        MockExpressCommentRecord(11105, "2023002019", "理工楼同学", 10102, "这条让我想起很多校园瞬间。", "2026-03-14 21:15")
    )

    val mockTopicPosts = mutableListOf(
        MockTopicPostRecord(
            id = 12101, username = "2023002003", topic = "春招准备",
            content = "最近大家都在怎么准备春招面试？除了简历和项目复盘，我感觉表达节奏也挺重要，想看看有没有人总结过面试时最容易卡住的点。",
            count = 2, publishTime = "2026-03-15 20:05", likeCount = 27, liked = false,
            firstImageUrl = "https://picsum.photos/seed/gdei-topic-1/960/720",
            imageUrls = listOf(
                "https://picsum.photos/seed/gdei-topic-1/960/720",
                "https://picsum.photos/seed/gdei-topic-1b/960/720"
            )
        ),
        MockTopicPostRecord(
            id = 12102, username = MockUtils.MOCK_CURRENT_USERNAME, topic = "教室查询",
            content = "新版教室查询你们更喜欢按时间段查还是按校区先筛？这两种方式我都试了几轮，感觉移动端入口可以再更直接一点。",
            count = 0, publishTime = "2026-03-14 16:40", likeCount = 15, liked = true
        ),
        MockTopicPostRecord(
            id = 12103, username = "2022003018", topic = "图书馆自习",
            content = "最近四楼靠窗区域人越来越多了，大家有没有自己固定的自习位？如果换楼层，你们更看重安静还是插座方便？",
            count = 1, publishTime = "2026-03-13 19:18", likeCount = 9, liked = false,
            firstImageUrl = "https://picsum.photos/seed/gdei-topic-2/960/720",
            imageUrls = listOf("https://picsum.photos/seed/gdei-topic-2/960/720")
        )
    )

    val mockDeliveryOrderRecords = mutableListOf(
        MockDeliveryOrderRecord(
            orderId = 13101, username = "2023002003", orderTime = "2026-03-15 19:40",
            name = "取件任务", number = "A0831", phone = "13800131001", price = 4.0,
            company = "白云校区快递驿站", address = "12 栋 402", state = 0,
            remarks = "到楼下给我发消息就好。"
        ),
        MockDeliveryOrderRecord(
            orderId = 13102, username = MockUtils.MOCK_CURRENT_USERNAME, orderTime = "2026-03-14 17:20",
            name = "取件任务", number = "SF2309", phone = "13800138000", price = 3.5,
            company = "龙洞校区菜鸟站", address = "8 栋 215", state = 1,
            remarks = "晚上 8 点后方便。"
        ),
        MockDeliveryOrderRecord(
            orderId = 13103, username = "2022003018", orderTime = "2026-03-13 12:10",
            name = "取件任务", number = "YT5620", phone = "13800132018", price = 5.0,
            company = "白云校区南门快递点", address = "图书馆门口", state = 2,
            remarks = "帮忙带一份冰美式更好。"
        ),
        MockDeliveryOrderRecord(
            orderId = 13104, username = MockUtils.MOCK_CURRENT_USERNAME, orderTime = "2026-03-12 15:55",
            name = "取件任务", number = "JD7788", phone = "13800138000", price = 2.5,
            company = "三水校区驿站", address = "7 栋大厅", state = 2,
            remarks = ""
        )
    )

    val mockDeliveryTrades = mutableListOf(
        MockDeliveryTradeRecord(
            tradeId = 13201, orderId = 13102, createTime = "2026-03-14 17:35",
            username = "2023002005", state = 0
        ),
        MockDeliveryTradeRecord(
            tradeId = 13202, orderId = 13103, createTime = "2026-03-13 12:22",
            username = MockUtils.MOCK_CURRENT_USERNAME, state = 1
        ),
        MockDeliveryTradeRecord(
            tradeId = 13203, orderId = 13104, createTime = "2026-03-12 16:10",
            username = "2023002003", state = 1
        )
    )

    val mockPhotographPostRecords = mutableListOf(
        MockPhotographPostRecord(
            id = 14101, title = "雨后教学楼的光",
            content = "下课后回头看了一眼，玻璃幕墙把晚霞和树影一起收进去了，顺手拍了两张。",
            count = 2, type = 0, username = "2023002003", createTime = "2026-03-15 18:12",
            likeCount = 21, commentCount = 2, liked = false,
            firstImageUrl = "https://picsum.photos/seed/gdei-photo-1/960/720",
            imageUrls = listOf(
                "https://picsum.photos/seed/gdei-photo-1/960/720",
                "https://picsum.photos/seed/gdei-photo-1b/960/720"
            )
        ),
        MockPhotographPostRecord(
            id = 14102, title = "食堂窗口前的人间烟火",
            content = "晚饭时间总是最有校园生活感的一刻，大家一边排队一边讨论今晚的作业。",
            count = 1, type = 1, username = MockUtils.MOCK_CURRENT_USERNAME,
            createTime = "2026-03-14 19:05",
            likeCount = 14, commentCount = 1, liked = true,
            firstImageUrl = "https://picsum.photos/seed/gdei-photo-2/960/720",
            imageUrls = listOf("https://picsum.photos/seed/gdei-photo-2/960/720")
        ),
        MockPhotographPostRecord(
            id = 14103, title = "图书馆四楼窗边",
            content = "复习周前夕，靠窗的位置总是很快坐满，但光线真的很好。",
            count = 3, type = 0, username = "2022003018", createTime = "2026-03-13 16:30",
            likeCount = 9, commentCount = 0, liked = false,
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
        MockPhotographCommentRecord(14202, 14101, MockUtils.MOCK_CURRENT_USERNAME, MockUtils.MOCK_PROFILE_NICKNAME, "第二张的层次感特别舒服。", "2026-03-15 19:02"),
        MockPhotographCommentRecord(14203, 14102, "2023002005", "食堂搭子", "这张一下就把下课后的氛围拍出来了。", "2026-03-14 19:40")
    )

    // ── Marketplace endpoints ───────────────────────────────────────────────

    fun mockMarketplaceItemList(request: Request): String {
        val items = mockMarketplaceItems
            .filter { it.state == 1 }
            .sortedByDescending { it.publishTime }
            .map { it.toMarketplaceItemPayload() }
        return MockUtils.successDataJson(items)
    }

    fun mockMarketplaceItemsByType(request: Request): String {
        val segments = request.url.pathSegments
        val typeIndex = segments.indexOf("type")
        val type = segments.getOrNull(typeIndex + 1)?.toIntOrNull()
        val items = mockMarketplaceItems
            .filter { it.state == 1 && (type == null || it.type == type) }
            .sortedByDescending { it.publishTime }
            .map { it.toMarketplaceItemPayload() }
        return MockUtils.successDataJson(items)
    }

    fun mockMarketplaceDetail(request: Request): String {
        val item = mockMarketplaceItems.findById(request.itemIdFromPath())
            ?: return MockUtils.failureJson("未找到商品详情")
        val profile = mockMarketplaceProfiles.firstOrNull { it.username == item.username }
        return MockUtils.successDataJson(
            linkedMapOf(
                "profile" to profile?.toMarketplaceProfilePayload(),
                "secondhandItem" to item.toMarketplaceItemPayload()
            )
        )
    }

    fun mockMarketplacePreview(request: Request): String {
        val preview = mockMarketplaceItems.findById(request.itemIdFromPath())
            ?.pictureURL
            ?.firstOrNull()
            .orEmpty()
        return MockUtils.successDataJson(preview)
    }

    fun mockMarketplaceProfile(request: Request): String {
        val mine = mockMarketplaceItems.filter { it.username == MockUtils.MOCK_CURRENT_USERNAME }
        return MockUtils.successDataJson(
            linkedMapOf(
                "doing" to mine.filter { it.state == 1 }.sortedByDescending { it.publishTime }.map { it.toMarketplaceItemPayload() },
                "sold" to mine.filter { it.state == 2 }.sortedByDescending { it.publishTime }.map { it.toMarketplaceItemPayload() },
                "off" to mine.filter { it.state == 0 }.sortedByDescending { it.publishTime }.map { it.toMarketplaceItemPayload() }
            )
        )
    }

    fun mockMarketplacePublish(request: Request): String {
        val name = request.multipartField("name").orEmpty().trim()
        val description = request.multipartField("description").orEmpty().trim()
        val price = request.multipartField("price").orEmpty().trim()
        val location = request.multipartField("location").orEmpty().trim()
        val type = request.multipartField("type")?.toIntOrNull() ?: 11
        val qq = request.multipartField("qq").orEmpty().trim()
        val phone = request.multipartField("phone").orEmpty().trim()
        if (name.isBlank() || description.isBlank() || location.isBlank() || qq.isBlank()) {
            return MockUtils.failureJson("请完整填写商品信息")
        }
        if (request.multipartImageCount() <= 0) {
            return MockUtils.failureJson("请至少上传一张商品图片")
        }
        val id = (mockMarketplaceItems.maxOfOrNull { it.id } ?: 4100) + 1
        val imageCount = request.multipartImageCount().coerceAtMost(4)
        val pictureUrls = (1..imageCount).map { index ->
            "https://picsum.photos/seed/gdei-market-$id-$index/960/720"
        }
        mockMarketplaceItems.add(
            0,
            MockMarketplaceItemRecord(
                id = id,
                username = MockUtils.MOCK_CURRENT_USERNAME,
                name = name,
                description = description,
                price = price.ifBlank { "0.00" },
                location = location,
                type = type,
                qq = qq,
                phone = phone,
                state = 1,
                publishTime = "刚刚",
                pictureURL = pictureUrls
            )
        )
        return MockUtils.successJson("发布成功")
    }

    fun mockMarketplaceStateUpdate(request: Request): String {
        val itemId = request.itemIdFromPath() ?: return MockUtils.failureJson("缺少商品编号")
        val targetState = request.url.queryParameter("state")
            ?.toIntOrNull()
            ?: request.formFields()["state"]?.toIntOrNull()
            ?: return MockUtils.failureJson("缺少商品状态")
        val index = mockMarketplaceItems.indexOfFirst { it.id.toString() == itemId }
        if (index < 0) return MockUtils.failureJson("未找到商品")
        mockMarketplaceItems[index] = mockMarketplaceItems[index].copy(state = targetState)
        return MockUtils.successJson("状态已更新")
    }

    fun mockMarketplaceUpdate(request: Request): String {
        val itemId = request.itemIdFromPath() ?: return MockUtils.failureJson("缺少商品编号")
        val fields = request.formFields()
        val index = mockMarketplaceItems.indexOfFirst { it.id.toString() == itemId }
        if (index < 0) return MockUtils.failureJson("未找到商品")
        val current = mockMarketplaceItems[index]
        mockMarketplaceItems[index] = current.copy(
            name = fields["name"].orEmpty().ifBlank { current.name },
            description = fields["description"].orEmpty().ifBlank { current.description },
            price = fields["price"].orEmpty().ifBlank { current.price },
            location = fields["location"].orEmpty().ifBlank { current.location },
            type = fields["type"]?.toIntOrNull() ?: current.type,
            qq = fields["qq"].orEmpty().ifBlank { current.qq },
            phone = fields["phone"].orEmpty()
        )
        return MockUtils.successJson("商品信息已更新")
    }

    // ── Lost & Found endpoints ──────────────────────────────────────────────

    fun mockLostFoundItemList(request: Request): String {
        val lostType = if (request.url.encodedPath.contains("/founditem/")) 1 else 0
        val items = mockLostFoundItems
            .filter { it.lostType == lostType && it.state == 0 }
            .sortedByDescending { it.publishTime }
            .map { it.toLostFoundItemPayload() }
        return MockUtils.successDataJson(items)
    }

    fun mockLostFoundDetail(request: Request): String {
        val item = mockLostFoundItems.findById(request.itemIdFromPath())
            ?: return MockUtils.failureJson("未找到失物招领详情")
        val profile = mockLostFoundProfiles.firstOrNull { it.username == item.username }
        return MockUtils.successDataJson(
            linkedMapOf(
                "item" to item.toLostFoundItemPayload(),
                "profile" to profile?.toLostFoundProfilePayload()
            )
        )
    }

    fun mockLostFoundPreview(request: Request): String {
        val preview = mockLostFoundItems.findById(request.itemIdFromPath())
            ?.pictureURL
            ?.firstOrNull()
            .orEmpty()
        return MockUtils.successDataJson(preview)
    }

    fun mockLostFoundProfile(request: Request): String {
        val mine = mockLostFoundItems.filter { it.username == MockUtils.MOCK_CURRENT_USERNAME }
        return MockUtils.successDataJson(
            linkedMapOf(
                "lost" to mine.filter { it.lostType == 0 && it.state == 0 }.sortedByDescending { it.publishTime }.map { it.toLostFoundItemPayload() },
                "found" to mine.filter { it.lostType == 1 && it.state == 0 }.sortedByDescending { it.publishTime }.map { it.toLostFoundItemPayload() },
                "didfound" to mine.filter { it.state == 1 }.sortedByDescending { it.publishTime }.map { it.toLostFoundItemPayload() }
            )
        )
    }

    fun mockLostFoundPublish(request: Request): String {
        val name = request.multipartField("name").orEmpty().ifBlank { "新的失物招领" }
        val description = request.multipartField("description").orEmpty().ifBlank { "新的失物招领描述" }
        val location = request.multipartField("location").orEmpty().ifBlank { "校内待确认地点" }
        val itemType = request.multipartField("itemType")?.toIntOrNull() ?: 11
        val lostType = request.multipartField("lostType")?.toIntOrNull() ?: 0
        val qq = request.multipartField("qq").orEmpty()
        val wechat = request.multipartField("wechat").orEmpty()
        val phone = request.multipartField("phone").orEmpty()
        val imageCount = request.multipartImageCount().coerceIn(1, 4)
        val id = (mockLostFoundItems.maxOfOrNull { it.id } ?: 5199) + 1
        val imageUrls = (1..imageCount).map { index ->
            "https://picsum.photos/seed/gdei-lost-$id-$index/960/720"
        }
        mockLostFoundItems.add(
            0,
            MockLostFoundItemRecord(
                id = id,
                username = MockUtils.MOCK_CURRENT_USERNAME,
                name = name,
                description = description,
                location = location,
                itemType = itemType,
                lostType = lostType,
                qq = qq,
                wechat = wechat,
                phone = phone,
                state = 0,
                publishTime = "刚刚",
                pictureURL = imageUrls
            )
        )
        return MockUtils.successJson("发布成功")
    }

    fun mockLostFoundUpdate(request: Request): String {
        val itemId = request.itemIdFromPath() ?: return MockUtils.failureJson("缺少条目编号")
        val fields = request.formFields()
        val index = mockLostFoundItems.indexOfFirst { it.id.toString() == itemId }
        if (index < 0) return MockUtils.failureJson("未找到条目")
        val current = mockLostFoundItems[index]
        mockLostFoundItems[index] = current.copy(
            name = fields["name"].orEmpty().ifBlank { current.name },
            description = fields["description"].orEmpty().ifBlank { current.description },
            location = fields["location"].orEmpty().ifBlank { current.location },
            itemType = fields["itemType"]?.toIntOrNull() ?: current.itemType,
            lostType = fields["lostType"]?.toIntOrNull() ?: current.lostType,
            qq = fields["qq"].orEmpty(),
            wechat = fields["wechat"].orEmpty(),
            phone = fields["phone"].orEmpty()
        )
        return MockUtils.successJson("失物招领信息已更新")
    }

    fun mockLostFoundDidFound(request: Request): String {
        val itemId = request.itemIdFromPath() ?: return MockUtils.failureJson("缺少条目编号")
        val index = mockLostFoundItems.indexOfFirst { it.id.toString() == itemId }
        if (index < 0) return MockUtils.failureJson("未找到条目")
        mockLostFoundItems[index] = mockLostFoundItems[index].copy(state = 1)
        return MockUtils.successJson("已标记为找回")
    }

    // ── Secret endpoints ────────────────────────────────────────────────────

    fun mockSecretPostList(request: Request): String {
        val items = mockSecretPosts
            .filter { it.state != 2 }
            .sortedByDescending { it.publishTime }
            .map { it.toSecretPayload(includeComments = false) }
        return MockUtils.successDataJson(items)
    }

    fun mockSecretMyPosts(request: Request): String {
        val items = mockSecretPosts
            .filter { it.username == MockUtils.MOCK_CURRENT_USERNAME && it.state != 2 }
            .sortedByDescending { it.publishTime }
            .map { it.toSecretPayload(includeComments = false) }
        return MockUtils.successDataJson(items)
    }

    fun mockSecretDetail(request: Request): String {
        val postId = request.itemIdFromPath() ?: return MockUtils.failureJson("缺少树洞编号")
        val post = mockSecretPosts.findById(postId) ?: return MockUtils.failureJson("查询的校园树洞信息不存在")
        return MockUtils.successDataJson(post.toSecretPayload(includeComments = true))
    }

    fun mockSecretCommentList(request: Request): String {
        val postId = request.itemIdFromPath() ?: return MockUtils.failureJson("缺少树洞编号")
        val comments = mockSecretComments
            .filter { it.contentId.toString() == postId }
            .sortedByDescending { it.publishTime }
            .map { it.toSecretCommentPayload() }
        return MockUtils.successDataJson(comments)
    }

    fun mockSecretCommentSubmit(request: Request): String {
        val postId = request.itemIdFromPath() ?: return MockUtils.failureJson("缺少树洞编号")
        val comment = request.url.queryParameter("comment")
            ?: request.formFields()["comment"]
            ?: return MockUtils.failureJson("评论内容不能为空")
        val trimmed = comment.trim()
        if (trimmed.isBlank()) return MockUtils.failureJson("评论内容不能为空")
        if (trimmed.length > 50) return MockUtils.failureJson("评论内容不能超过 50 个字")
        val postIndex = mockSecretPosts.indexOfFirst { it.id.toString() == postId }
        if (postIndex < 0) return MockUtils.failureJson("查询的校园树洞信息不存在")
        mockSecretComments.add(
            0,
            MockSecretCommentRecord(
                id = (mockSecretComments.maxOfOrNull { it.id } ?: 7000) + 1,
                contentId = postId.toInt(),
                username = MockProfileProvider.currentMockAnonymousName(),
                comment = trimmed,
                publishTime = "刚刚",
                avatarTheme = 1
            )
        )
        val post = mockSecretPosts[postIndex]
        mockSecretPosts[postIndex] = post.copy(commentCount = post.commentCount + 1)
        return MockUtils.successJson("评论已发送")
    }

    fun mockSecretLikeUpdate(request: Request): String {
        val postId = request.itemIdFromPath() ?: return MockUtils.failureJson("缺少树洞编号")
        val like = request.url.queryParameter("like")
            ?.toIntOrNull()
            ?: request.formFields()["like"]?.toIntOrNull()
            ?: return MockUtils.failureJson("缺少点赞状态")
        val postIndex = mockSecretPosts.indexOfFirst { it.id.toString() == postId }
        if (postIndex < 0) return MockUtils.failureJson("查询的校园树洞信息不存在")
        val post = mockSecretPosts[postIndex]
        val liked = like == 1
        mockSecretPosts[postIndex] = post.copy(
            liked = liked,
            likeCount = (post.likeCount + if (liked && !post.liked) 1 else if (!liked && post.liked) -1 else 0).coerceAtLeast(0)
        )
        return MockUtils.successJson("点赞状态已更新")
    }

    fun mockSecretPublish(request: Request): String {
        val theme = request.multipartField("theme")?.toIntOrNull() ?: 0
        val content = request.multipartField("content").orEmpty().ifBlank { "新的小纸条内容" }
        val type = request.multipartField("type")?.toIntOrNull() ?: 0
        val timer = request.multipartField("timer")?.toIntOrNull() ?: 0
        val id = (mockSecretPosts.maxOfOrNull { it.id } ?: 9999) + 1
        mockSecretPosts.add(0, MockSecretPostRecord(
            id = id, username = MockUtils.MOCK_CURRENT_USERNAME,
            content = content, theme = theme, type = type, timer = timer,
            state = 1, publishTime = "刚刚", likeCount = 0, commentCount = 0, liked = false
        ))
        return MockUtils.successJson("发布成功")
    }

    // ── Dating endpoints ────────────────────────────────────────────────────

    fun mockDatingReceivedPickList(request: Request): String {
        val items = mockDatingReceivedPicks
            .sortedByDescending { it.updatedAt }
            .map { it.toDatingPickPayload() }
        return MockUtils.successDataJson(items)
    }

    fun mockDatingProfileList(request: Request): String {
        val area = request.pathValueAfter("area")?.toIntOrNull()
        val items = mockDatingProfiles
            .filter { it.state != 0 && (area == null || it.area == area) && it.username != MockUtils.MOCK_CURRENT_USERNAME }
            .sortedByDescending { it.profileId }
            .map { it.toDatingProfilePayload() }
        return MockUtils.successDataJson(items)
    }

    fun mockDatingDetail(request: Request): String {
        val profileId = request.itemIdFromPath() ?: return MockUtils.failureJson("缺少资料编号")
        val profile = mockDatingProfiles.findById(profileId) ?: return MockUtils.failureJson("未找到卖室友资料")
        val sentPick = mockDatingSentPicks.firstOrNull { it.profile.profileId.toString() == profileId }
        val isMine = profile.username == MockUtils.MOCK_CURRENT_USERNAME
        return MockUtils.successDataJson(
            linkedMapOf(
                "profile" to profile.toDatingProfilePayload(),
                "pictureURL" to profile.pictureURL,
                "isContactVisible" to (sentPick?.state == 1),
                "isPickNotAvailable" to (isMine || sentPick != null)
            )
        )
    }

    fun mockDatingSentPickList(request: Request): String {
        val items = mockDatingSentPicks
            .sortedByDescending { it.updatedAt }
            .map { it.toDatingPickPayload() }
        return MockUtils.successDataJson(items)
    }

    fun mockDatingMyPosts(request: Request): String {
        val items = mockDatingProfiles
            .filter { it.username == MockUtils.MOCK_CURRENT_USERNAME && it.state != 0 }
            .map { it.toDatingProfilePayload() }
        return MockUtils.successDataJson(items)
    }

    fun mockDatingPublish(request: Request): String {
        val nickname = request.multipartField("nickname").orEmpty().ifBlank { MockUtils.MOCK_PROFILE_NICKNAME }
        val grade = request.multipartField("grade")?.toIntOrNull() ?: 3
        val faculty = request.multipartField("faculty").orEmpty().ifBlank { "计算机科学系" }
        val hometown = request.multipartField("hometown").orEmpty().ifBlank { "广州" }
        val content = request.multipartField("content").orEmpty().ifBlank { "新发布的卖室友资料" }
        val area = request.multipartField("area")?.toIntOrNull() ?: 0
        val qq = request.multipartField("qq").orEmpty()
        val wechat = request.multipartField("wechat").orEmpty()
        val id = (mockDatingProfiles.maxOfOrNull { it.profileId } ?: 8100) + 1
        val pictureUrl = if (request.multipartImageCount() > 0) {
            "https://picsum.photos/seed/gdei-dating-$id/960/1280"
        } else {
            ""
        }
        mockDatingProfiles.add(
            0,
            MockDatingProfileRecord(
                profileId = id,
                username = MockUtils.MOCK_CURRENT_USERNAME,
                nickname = nickname,
                grade = grade,
                faculty = faculty,
                hometown = hometown,
                content = content,
                qq = qq,
                wechat = wechat,
                area = area,
                state = 1,
                pictureURL = pictureUrl
            )
        )
        return MockUtils.successJson("发布成功")
    }

    fun mockDatingPickSubmit(request: Request): String {
        val fields = request.formFields()
        val profileId = request.url.queryParameter("profileId")
            ?: fields["profileId"]
            ?: return MockUtils.failureJson("缺少资料编号")
        val content = request.url.queryParameter("content")
            ?: fields["content"]
            ?: return MockUtils.failureJson("撩一下内容不能为空")
        val trimmedContent = content.trim()
        if (trimmedContent.isBlank()) return MockUtils.failureJson("撩一下内容不能为空")
        if (trimmedContent.length > 50) return MockUtils.failureJson("文本内容超过限制")
        val profile = mockDatingProfiles.findById(profileId) ?: return MockUtils.failureJson("未找到卖室友资料")
        if (profile.username == MockUtils.MOCK_CURRENT_USERNAME) {
            return MockUtils.failureJson("不能向自己发送请求")
        }
        if (mockDatingSentPicks.any { it.profile.profileId.toString() == profileId }) {
            return MockUtils.failureJson("请勿重复发送请求")
        }
        mockDatingSentPicks.add(
            0,
            MockDatingPickRecord(
                pickId = (mockDatingSentPicks.maxOfOrNull { it.pickId } ?: 9100) + 1,
                profile = profile,
                username = MockUtils.MOCK_CURRENT_USERNAME,
                content = trimmedContent,
                state = 0,
                updatedAt = "刚刚"
            )
        )
        return MockUtils.successJson("请求已发送")
    }

    fun mockDatingPickStateUpdate(request: Request): String {
        val pickId = request.itemIdFromPath() ?: return MockUtils.failureJson("缺少请求编号")
        val state = request.url.queryParameter("state")
            ?.toIntOrNull()
            ?: request.formFields()["state"]?.toIntOrNull()
            ?: return MockUtils.failureJson("缺少状态参数")
        val index = mockDatingReceivedPicks.indexOfFirst { it.pickId.toString() == pickId }
        if (index < 0) return MockUtils.failureJson("未找到该请求")
        mockDatingReceivedPicks[index] = mockDatingReceivedPicks[index].copy(state = state)
        return MockUtils.successJson("状态已更新")
    }

    fun mockDatingProfileHide(request: Request): String {
        val profileId = request.itemIdFromPath() ?: return MockUtils.failureJson("缺少发布编号")
        val index = mockDatingProfiles.indexOfFirst { it.profileId.toString() == profileId }
        if (index < 0) return MockUtils.failureJson("未找到该发布")
        mockDatingProfiles[index] = mockDatingProfiles[index].copy(state = 0)
        return MockUtils.successJson("已隐藏")
    }

    // ── Express endpoints ───────────────────────────────────────────────────

    fun mockExpressPostList(request: Request): String {
        val items = mockExpressPosts
            .sortedByDescending { it.publishTime }
            .map { it.toExpressPayload() }
        return MockUtils.successDataJson(items)
    }

    fun mockExpressMyPosts(request: Request): String {
        val items = mockExpressPosts
            .filter { it.username == MockUtils.MOCK_CURRENT_USERNAME }
            .sortedByDescending { it.publishTime }
            .map { it.toExpressPayload() }
        return MockUtils.successDataJson(items)
    }

    fun mockExpressDetail(request: Request): String {
        val postId = request.itemIdFromPath() ?: return MockUtils.failureJson("缺少表白编号")
        val item = mockExpressPosts.findById(postId) ?: return MockUtils.failureJson("查询的表白内容不存在")
        return MockUtils.successDataJson(item.toExpressPayload())
    }

    fun mockExpressCommentList(request: Request): String {
        val postId = request.itemIdFromPath() ?: return MockUtils.failureJson("缺少表白编号")
        val items = mockExpressComments
            .filter { it.expressId.toString() == postId }
            .sortedByDescending { it.publishTime }
            .map { it.toExpressCommentPayload() }
        return MockUtils.successDataJson(items)
    }

    fun mockExpressCommentSubmit(request: Request): String {
        val postId = request.itemIdFromPath() ?: return MockUtils.failureJson("缺少表白编号")
        val trimmed = request.url.queryParameter("comment")
            ?.trim()
            ?: request.formFields()["comment"]?.trim()
            ?: return MockUtils.failureJson("缺少评论内容")
        if (trimmed.isBlank()) return MockUtils.failureJson("评论内容不能为空")
        val postIndex = mockExpressPosts.indexOfFirst { it.id.toString() == postId }
        if (postIndex < 0) return MockUtils.failureJson("查询的表白内容不存在")
        mockExpressComments.add(
            0,
            MockExpressCommentRecord(
                id = (mockExpressComments.maxOfOrNull { it.id } ?: 9500) + 1,
                expressId = postId.toInt(),
                username = MockUtils.MOCK_CURRENT_USERNAME,
                nickname = MockProfileProvider.currentMockNickname(),
                comment = trimmed,
                publishTime = "刚刚"
            )
        )
        val item = mockExpressPosts[postIndex]
        mockExpressPosts[postIndex] = item.copy(commentCount = item.commentCount + 1)
        return MockUtils.successJson("评论已发送")
    }

    fun mockExpressLike(request: Request): String {
        val postId = request.itemIdFromPath() ?: return MockUtils.failureJson("缺少表白编号")
        val postIndex = mockExpressPosts.indexOfFirst { it.id.toString() == postId }
        if (postIndex < 0) return MockUtils.failureJson("查询的表白内容不存在")
        val item = mockExpressPosts[postIndex]
        if (!item.liked) {
            mockExpressPosts[postIndex] = item.copy(liked = true, likeCount = item.likeCount + 1)
        }
        return MockUtils.successJson("点赞成功")
    }

    fun mockExpressGuess(request: Request): String {
        val postId = request.itemIdFromPath() ?: return MockUtils.failureJson("缺少表白编号")
        val name = request.url.queryParameter("name")
            ?.trim()
            ?: request.formFields()["name"]?.trim()
            ?: return MockUtils.failureJson("缺少猜测姓名")
        val postIndex = mockExpressPosts.indexOfFirst { it.id.toString() == postId }
        if (postIndex < 0) return MockUtils.failureJson("查询的表白内容不存在")
        val item = mockExpressPosts[postIndex]
        val matched = item.realname.equals(name, ignoreCase = true)
        mockExpressPosts[postIndex] = item.copy(
            guessCount = item.guessCount + if (matched) 1 else 0,
            guessSum = item.guessSum + 1
        )
        return MockUtils.successDataJson(matched)
    }

    fun mockExpressPublish(request: Request): String {
        val fields = request.formFields()
        val nickname = fields["nickname"].orEmpty().ifBlank { MockProfileProvider.currentMockNickname() }
        val content = fields["content"].orEmpty().ifBlank { "新的表白内容" }
        val name = fields["name"].orEmpty().ifBlank { "你" }
        val selfGender = fields["selfGender"]?.toIntOrNull() ?: 0
        val personGender = fields["personGender"]?.toIntOrNull() ?: 0
        val id = (mockExpressPosts.maxOfOrNull { it.id } ?: 10999) + 1
        mockExpressPosts.add(0, MockExpressPostRecord(
            id = id, username = MockUtils.MOCK_CURRENT_USERNAME,
            nickname = nickname, realname = fields["realname"].orEmpty(),
            selfGender = selfGender, name = name, content = content,
            personGender = personGender, publishTime = "刚刚",
            likeCount = 0, liked = false, commentCount = 0,
            guessCount = 0, guessSum = 0, canGuess = false
        ))
        return MockUtils.successJson("发布成功")
    }

    fun mockExpressSearch(request: Request): String {
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
        return MockUtils.successDataJson(matched.drop(start).take(size).map { it.toExpressPayload() })
    }

    // ── Topic endpoints ─────────────────────────────────────────────────────

    fun mockTopicPostList(request: Request): String {
        val items = mockTopicPosts
            .sortedByDescending { it.publishTime }
            .map { it.toTopicPayload() }
        return MockUtils.successDataJson(items)
    }

    fun mockTopicMyPosts(request: Request): String {
        val items = mockTopicPosts
            .filter { it.username == MockUtils.MOCK_CURRENT_USERNAME }
            .sortedByDescending { it.publishTime }
            .map { it.toTopicPayload() }
        return MockUtils.successDataJson(items)
    }

    fun mockTopicDetail(request: Request): String {
        val postId = request.itemIdFromPath() ?: return MockUtils.failureJson("缺少话题编号")
        val item = mockTopicPosts.findById(postId) ?: return MockUtils.failureJson("查询的话题内容不存在")
        return MockUtils.successDataJson(item.toTopicPayload())
    }

    fun mockTopicLike(request: Request): String {
        val postId = request.itemIdFromPath() ?: return MockUtils.failureJson("缺少话题编号")
        val postIndex = mockTopicPosts.indexOfFirst { it.id.toString() == postId }
        if (postIndex < 0) return MockUtils.failureJson("查询的话题内容不存在")
        val item = mockTopicPosts[postIndex]
        if (!item.liked) {
            mockTopicPosts[postIndex] = item.copy(liked = true, likeCount = item.likeCount + 1)
        }
        return MockUtils.successJson("点赞成功")
    }

    fun mockTopicImage(request: Request): String {
        val postId = request.itemIdFromPath() ?: return MockUtils.failureJson("缺少话题编号")
        val index = request.pathValueAfter("index")?.toIntOrNull() ?: return MockUtils.failureJson("缺少图片序号")
        val item = mockTopicPosts.findById(postId) ?: return MockUtils.failureJson("查询的话题内容不存在")
        val image = item.imageUrls.getOrNull(index - 1) ?: return MockUtils.failureJson("图片不存在")
        return MockUtils.successDataJson(image)
    }

    fun mockTopicPublish(request: Request): String {
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
            id = id, username = MockUtils.MOCK_CURRENT_USERNAME,
            topic = topic, content = content, count = count,
            publishTime = "刚刚", likeCount = 0, liked = false,
            firstImageUrl = imageUrls.firstOrNull(),
            imageUrls = imageUrls
        ))
        return MockUtils.successJson("发布成功")
    }

    fun mockTopicSearch(request: Request): String {
        val segments = request.url.pathSegments
        val kwIdx = segments.indexOf("keyword")
        val keyword = segments.getOrNull(kwIdx + 1).orEmpty()
        val start = segments.getOrNull(kwIdx + 3)?.toIntOrNull() ?: 0
        val size = segments.lastOrNull()?.toIntOrNull()?.coerceIn(1, 20) ?: 10
        val matched = mockTopicPosts.filter {
            it.topic.contains(keyword, ignoreCase = true) ||
            it.content.contains(keyword, ignoreCase = true)
        }
        return MockUtils.successDataJson(matched.drop(start).take(size).map { it.toTopicPayload() })
    }

    // ── Delivery endpoints ──────────────────────────────────────────────────

    fun mockDeliveryOrders(request: Request): String {
        val start = request.pathValueAfter("start")?.toIntOrNull()?.coerceAtLeast(0) ?: 0
        val size = request.pathValueAfter("size")?.toIntOrNull()?.coerceIn(1, 50) ?: 20
        val items = mockDeliveryOrderRecords
            .sortedByDescending { it.orderTime }
            .drop(start)
            .take(size)
            .map { it.toDeliveryOrderPayload() }
        return MockUtils.successDataJson(items)
    }

    fun mockDeliveryMine(request: Request): String {
        val published = mockDeliveryOrderRecords
            .filter { it.username == MockUtils.MOCK_CURRENT_USERNAME }
            .sortedByDescending { it.orderTime }
            .map { it.toDeliveryOrderPayload() }
        val accepted = mockDeliveryTrades
            .filter { it.username == MockUtils.MOCK_CURRENT_USERNAME }
            .mapNotNull { trade ->
                mockDeliveryOrderRecords.firstOrNull { it.orderId == trade.orderId }
            }
            .sortedByDescending { it.orderTime }
            .map { it.toDeliveryOrderPayload() }
        return MockUtils.successDataJson(
            linkedMapOf(
                "published" to published,
                "accepted" to accepted
            )
        )
    }

    fun mockDeliveryOrderDetail(request: Request): String {
        val orderId = request.itemIdFromPath() ?: return MockUtils.failureJson("缺少跑腿编号")
        val order = mockDeliveryOrderRecords.findById(orderId) ?: return MockUtils.failureJson("查询的全民快递订单不存在")
        val trade = mockDeliveryTrades.firstOrNull { it.orderId == order.orderId }
        val detailType = when {
            order.username == MockUtils.MOCK_CURRENT_USERNAME -> 0
            trade?.username == MockUtils.MOCK_CURRENT_USERNAME -> 3
            else -> 1
        }
        return MockUtils.successDataJson(
            linkedMapOf(
                "order" to order.toDeliveryOrderPayload(),
                "detailType" to detailType,
                "trade" to trade?.toDeliveryTradePayload()
            )
        )
    }

    fun mockDeliveryAcceptOrder(request: Request): String {
        val orderId = request.url.queryParameter("orderId")
            ?: request.formFields()["orderId"]
            ?: return MockUtils.failureJson("缺少跑腿编号")
        val orderIndex = mockDeliveryOrderRecords.indexOfFirst { it.orderId.toString() == orderId }
        if (orderIndex < 0) return MockUtils.failureJson("查询的全民快递订单不存在")
        val order = mockDeliveryOrderRecords[orderIndex]
        if (order.state != 0) return MockUtils.failureJson("该订单已被接单")
        mockDeliveryOrderRecords[orderIndex] = order.copy(state = 1)
        val exists = mockDeliveryTrades.any { it.orderId == order.orderId }
        if (!exists) {
            mockDeliveryTrades.add(
                0,
                MockDeliveryTradeRecord(
                    tradeId = (mockDeliveryTrades.maxOfOrNull { it.tradeId } ?: 12900) + 1,
                    orderId = order.orderId,
                    createTime = "刚刚",
                    username = MockUtils.MOCK_CURRENT_USERNAME,
                    state = 0
                )
            )
        }
        return MockUtils.successJson("接单成功")
    }

    fun mockDeliveryFinishTrade(request: Request): String {
        val tradeId = request.itemIdFromPath() ?: return MockUtils.failureJson("缺少交易编号")
        val tradeIndex = mockDeliveryTrades.indexOfFirst { it.tradeId.toString() == tradeId }
        if (tradeIndex < 0) return MockUtils.failureJson("交易记录不存在")
        val trade = mockDeliveryTrades[tradeIndex]
        mockDeliveryTrades[tradeIndex] = trade.copy(state = 1)
        val orderIndex = mockDeliveryOrderRecords.indexOfFirst { it.orderId == trade.orderId }
        if (orderIndex >= 0) {
            mockDeliveryOrderRecords[orderIndex] = mockDeliveryOrderRecords[orderIndex].copy(state = 2)
        }
        return MockUtils.successJson("订单已完成")
    }

    fun mockDeliveryDeleteOrder(request: Request): String {
        val orderId = request.itemIdFromPath() ?: return MockUtils.failureJson("缺少跑腿编号")
        val removed = mockDeliveryOrderRecords.removeIf { it.orderId.toString() == orderId }
        if (!removed) return MockUtils.failureJson("查询的全民快递订单不存在")
        mockDeliveryTrades.removeIf { it.orderId.toString() == orderId }
        return MockUtils.successJson("已删除")
    }

    fun mockDeliveryPublish(request: Request): String {
        val fields = request.formFields()
        val pickupPlace = fields["company"].orEmpty().ifBlank { "白云校区快递站" }
        val order = MockDeliveryOrderRecord(
            orderId = (mockDeliveryOrderRecords.maxOfOrNull { it.orderId } ?: 12800) + 1,
            username = MockUtils.MOCK_CURRENT_USERNAME,
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
        return MockUtils.successJson("发布成功")
    }

    // ── Photograph endpoints ────────────────────────────────────────────────

    fun mockPhotographPhotoCount(request: Request): String =
        MockUtils.successDataJson(mockPhotographPostRecords.sumOf { it.photoCount() })

    fun mockPhotographCommentCount(request: Request): String =
        MockUtils.successDataJson(mockPhotographCommentRecords.size)

    fun mockPhotographLikeCount(request: Request): String =
        MockUtils.successDataJson(mockPhotographPostRecords.sumOf { it.likeCount })

    fun mockPhotographPosts(request: Request): String {
        val type = request.pathValueAfter("type")?.toIntOrNull() ?: 0
        val start = request.pathValueAfter("start")?.toIntOrNull()?.coerceAtLeast(0) ?: 0
        val size = request.pathValueAfter("size")?.toIntOrNull()?.coerceIn(1, 50) ?: 20
        val items = mockPhotographPostRecords
            .filter { it.type == type }
            .sortedByDescending { it.createTime }
            .drop(start)
            .take(size)
            .map { it.toPhotographPayload(includeComments = false) }
        return MockUtils.successDataJson(items)
    }

    fun mockPhotographMyPosts(request: Request): String {
        val start = request.pathValueAfter("start")?.toIntOrNull()?.coerceAtLeast(0) ?: 0
        val size = request.pathValueAfter("size")?.toIntOrNull()?.coerceIn(1, 50) ?: 20
        val items = mockPhotographPostRecords
            .filter { it.username == MockUtils.MOCK_CURRENT_USERNAME }
            .sortedByDescending { it.createTime }
            .drop(start)
            .take(size)
            .map { it.toPhotographPayload(includeComments = false) }
        return MockUtils.successDataJson(items)
    }

    fun mockPhotographDetail(request: Request): String {
        val postId = request.itemIdFromPath() ?: return MockUtils.failureJson("缺少作品编号")
        val item = mockPhotographPostRecords.findById(postId) ?: return MockUtils.failureJson("查询的拍好校园作品不存在")
        return MockUtils.successDataJson(item.toPhotographPayload(includeComments = true))
    }

    fun mockPhotographImage(request: Request): String {
        val postId = request.itemIdFromPath() ?: return MockUtils.failureJson("缺少作品编号")
        val index = request.pathValueAfter("index")?.toIntOrNull() ?: return MockUtils.failureJson("缺少图片序号")
        val item = mockPhotographPostRecords.findById(postId) ?: return MockUtils.failureJson("查询的拍好校园作品不存在")
        val image = item.resolvedImageUrls().getOrNull(index - 1) ?: return MockUtils.failureJson("图片不存在")
        return MockUtils.successDataJson(image)
    }

    fun mockPhotographComments(request: Request): String {
        val postId = request.itemIdFromPath() ?: return MockUtils.failureJson("缺少作品编号")
        val items = mockPhotographCommentRecords
            .filter { it.photoId.toString() == postId }
            .sortedByDescending { it.createTime }
            .map { it.toPhotographCommentPayload() }
        return MockUtils.successDataJson(items)
    }

    fun mockPhotographCommentSubmit(request: Request): String {
        val postId = request.itemIdFromPath() ?: return MockUtils.failureJson("缺少作品编号")
        val comment = request.url.queryParameter("comment")
            ?: request.formFields()["comment"]
            ?: return MockUtils.failureJson("评论内容不能为空")
        val trimmed = comment.trim()
        if (trimmed.isBlank()) return MockUtils.failureJson("评论内容不能为空")
        val postIndex = mockPhotographPostRecords.indexOfFirst { it.id.toString() == postId }
        if (postIndex < 0) return MockUtils.failureJson("查询的拍好校园作品不存在")
        mockPhotographCommentRecords.add(
            0,
            MockPhotographCommentRecord(
                commentId = (mockPhotographCommentRecords.maxOfOrNull { it.commentId } ?: 14200) + 1,
                photoId = postId.toInt(),
                username = MockUtils.MOCK_CURRENT_USERNAME,
                nickname = MockProfileProvider.currentMockNickname(),
                comment = trimmed,
                createTime = "刚刚"
            )
        )
        val post = mockPhotographPostRecords[postIndex]
        mockPhotographPostRecords[postIndex] = post.copy(commentCount = post.commentCount + 1)
        return MockUtils.successJson("评论已发送")
    }

    fun mockPhotographLike(request: Request): String {
        val postId = request.itemIdFromPath() ?: return MockUtils.failureJson("缺少作品编号")
        val postIndex = mockPhotographPostRecords.indexOfFirst { it.id.toString() == postId }
        if (postIndex < 0) return MockUtils.failureJson("查询的拍好校园作品不存在")
        val post = mockPhotographPostRecords[postIndex]
        if (!post.liked) {
            mockPhotographPostRecords[postIndex] = post.copy(liked = true, likeCount = post.likeCount + 1)
        }
        return MockUtils.successJson("点赞成功")
    }

    fun mockPhotographPublish(request: Request): String {
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
                username = MockUtils.MOCK_CURRENT_USERNAME,
                createTime = "刚刚",
                likeCount = 0,
                commentCount = 0,
                liked = false,
                firstImageUrl = imageUrls.firstOrNull(),
                imageUrls = imageUrls
            )
        )
        return MockUtils.successJson("发布成功")
    }
}
