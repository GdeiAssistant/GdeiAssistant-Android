package cn.gdeiassistant.network.mock

import cn.gdeiassistant.network.mock.MockUtils.formFields
import cn.gdeiassistant.network.mock.MockUtils.itemIdFromPath
import cn.gdeiassistant.network.mock.MockUtils.localizedText
import cn.gdeiassistant.network.mock.MockUtils.multipartField
import cn.gdeiassistant.network.mock.MockUtils.multipartImageCount
import cn.gdeiassistant.network.mock.MockUtils.pathValueAfter
import cn.gdeiassistant.network.mock.MockUtils.requestLocale
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

    private fun communityText(
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

    private fun communityMessage(locale: String, key: String): String {
        return when (key) {
            "just_now" -> communityText(locale, "刚刚", "Just now", "剛剛", "たった今", "방금")
            "published" -> communityText(locale, "发布成功", "Published successfully", "發布成功", "投稿しました", "게시했습니다")
            "status_updated" -> communityText(locale, "状态已更新", "Status updated", "狀態已更新", "状態を更新しました", "상태를 업데이트했습니다")
            "comment_sent" -> communityText(locale, "评论已发送", "Comment sent", "評論已發送", "コメントを送信しました", "댓글을 보냈습니다")
            "like_status_updated" -> communityText(locale, "点赞状态已更新", "Like status updated", "點讚狀態已更新", "いいね状態を更新しました", "좋아요 상태를 업데이트했습니다")
            "like_success" -> communityText(locale, "点赞成功", "Liked successfully", "點讚成功", "いいねしました", "좋아요했습니다")
            "request_sent" -> communityText(locale, "请求已发送", "Request sent", "請求已發送", "リクエストを送信しました", "요청을 보냈습니다")
            "hidden" -> communityText(locale, "已隐藏", "Hidden", "已隱藏", "非表示にしました", "숨겼습니다")
            "deleted" -> communityText(locale, "已删除", "Deleted", "已刪除", "削除しました", "삭제했습니다")
            "lostfound_marked_found" -> communityText(locale, "已标记为找回", "Marked as recovered", "已標記為找回", "発見済みにしました", "찾음으로 표시했습니다")
            "delivery_accept_success" -> communityText(locale, "接单成功", "Order accepted", "接單成功", "受注しました", "주문을 수락했습니다")
            "delivery_order_finished" -> communityText(locale, "订单已完成", "Order completed", "訂單已完成", "注文が完了しました", "주문이 완료되었습니다")
            "marketplace_detail_not_found" -> communityText(locale, "未找到商品详情", "Item details not found", "未找到商品詳情", "商品詳細が見つかりません", "상품 상세를 찾을 수 없습니다")
            "marketplace_incomplete" -> communityText(locale, "请完整填写商品信息", "Please complete the item information", "請完整填寫商品信息", "商品情報をすべて入力してください", "상품 정보를 모두 입력해 주세요")
            "marketplace_need_image" -> communityText(locale, "请至少上传一张商品图片", "Please upload at least one item image", "請至少上傳一張商品圖片", "商品画像を1枚以上アップロードしてください", "상품 이미지를 한 장 이상 올려 주세요")
            "marketplace_item_id_required" -> communityText(locale, "缺少商品编号", "Missing item ID", "缺少商品編號", "商品IDがありません", "상품 ID가 없습니다")
            "marketplace_state_required" -> communityText(locale, "缺少商品状态", "Missing item status", "缺少商品狀態", "商品状態がありません", "상품 상태가 없습니다")
            "marketplace_item_not_found" -> communityText(locale, "未找到商品", "Item not found", "未找到商品", "商品が見つかりません", "상품을 찾을 수 없습니다")
            "marketplace_item_updated" -> communityText(locale, "商品信息已更新", "Item updated", "商品信息已更新", "商品情報を更新しました", "상품 정보를 업데이트했습니다")
            "lostfound_detail_not_found" -> communityText(locale, "未找到失物招领详情", "Lost-and-found details not found", "未找到失物招領詳情", "遺失物詳細が見つかりません", "분실물 상세를 찾을 수 없습니다")
            "lostfound_default_name" -> communityText(locale, "新的失物招领", "New lost-and-found post", "新的失物招領", "新しい遺失物投稿", "새 분실물 게시글")
            "lostfound_default_description" -> communityText(locale, "新的失物招领描述", "Details about the item", "新的失物招領描述", "遺失物の説明", "분실물 설명")
            "lostfound_default_location" -> communityText(locale, "校内待确认地点", "Location to be confirmed", "校內待確認地點", "学内の未確認場所", "교내 확인 예정 위치")
            "lostfound_item_id_required" -> communityText(locale, "缺少条目编号", "Missing entry ID", "缺少條目編號", "項目IDがありません", "항목 ID가 없습니다")
            "lostfound_item_not_found" -> communityText(locale, "未找到条目", "Item not found", "未找到條目", "項目が見つかりません", "항목을 찾을 수 없습니다")
            "lostfound_item_updated" -> communityText(locale, "失物招领信息已更新", "Lost-and-found post updated", "失物招領信息已更新", "遺失物情報を更新しました", "분실물 정보를 업데이트했습니다")
            "secret_post_id_required" -> communityText(locale, "缺少树洞编号", "Missing post ID", "缺少樹洞編號", "投稿IDがありません", "게시글 ID가 없습니다")
            "secret_post_not_found" -> communityText(locale, "查询的校园树洞信息不存在", "The secret post was not found", "查詢的校園樹洞信息不存在", "ツリーホール投稿が見つかりません", "비밀 게시글을 찾을 수 없습니다")
            "comment_required" -> communityText(locale, "评论内容不能为空", "Comment cannot be empty", "評論內容不能為空", "コメントを入力してください", "댓글을 입력해 주세요")
            "secret_comment_too_long" -> communityText(locale, "评论内容不能超过 50 个字", "Comment cannot exceed 50 characters", "評論內容不能超過 50 個字", "コメントは50文字以内にしてください", "댓글은 50자 이내여야 합니다")
            "like_state_required" -> communityText(locale, "缺少点赞状态", "Missing like state", "缺少點讚狀態", "いいね状態がありません", "좋아요 상태가 없습니다")
            "secret_default_content" -> communityText(locale, "新的小纸条内容", "A new note for the wall", "新的小紙條內容", "新しいメッセージ", "새 비밀글 내용")
            "dating_profile_id_required" -> communityText(locale, "缺少资料编号", "Missing profile ID", "缺少資料編號", "プロフィールIDがありません", "프로필 ID가 없습니다")
            "dating_profile_not_found" -> communityText(locale, "未找到卖室友资料", "Roommate profile not found", "未找到賣室友資料", "プロフィールが見つかりません", "룸메이트 프로필을 찾을 수 없습니다")
            "dating_default_faculty" -> communityText(locale, "计算机科学系", "Computer Science Department", "計算機科學系", "コンピュータサイエンス学科", "컴퓨터과학과")
            "dating_default_hometown" -> communityText(locale, "广州", "Guangzhou", "廣州", "広州", "광저우")
            "dating_default_content" -> communityText(locale, "新发布的卖室友资料", "New roommate profile", "新發布的賣室友資料", "新しいルームメイト募集プロフィール", "새 룸메이트 프로필")
            "dating_pick_content_required" -> communityText(locale, "撩一下内容不能为空", "Message cannot be empty", "撩一下內容不能為空", "メッセージを入力してください", "메시지를 입력해 주세요")
            "text_too_long" -> communityText(locale, "文本内容超过限制", "Text exceeds the limit", "文本內容超過限制", "テキストが長すぎます", "텍스트가 너무 깁니다")
            "dating_pick_self_forbidden" -> communityText(locale, "不能向自己发送请求", "You cannot send a request to yourself", "不能向自己發送請求", "自分にはリクエストできません", "자신에게는 요청할 수 없습니다")
            "dating_pick_duplicate" -> communityText(locale, "请勿重复发送请求", "Please do not send duplicate requests", "請勿重複發送請求", "重複してリクエストしないでください", "중복 요청은 할 수 없습니다")
            "dating_pick_id_required" -> communityText(locale, "缺少请求编号", "Missing request ID", "缺少請求編號", "リクエストIDがありません", "요청 ID가 없습니다")
            "dating_state_required" -> communityText(locale, "缺少状态参数", "Missing status parameter", "缺少狀態參數", "状態パラメータがありません", "상태 파라미터가 없습니다")
            "dating_request_not_found" -> communityText(locale, "未找到该请求", "Request not found", "未找到該請求", "リクエストが見つかりません", "요청을 찾을 수 없습니다")
            "dating_post_id_required" -> communityText(locale, "缺少发布编号", "Missing post ID", "缺少發布編號", "投稿IDがありません", "게시글 ID가 없습니다")
            "dating_post_not_found" -> communityText(locale, "未找到该发布", "Post not found", "未找到該發布", "投稿が見つかりません", "게시글을 찾을 수 없습니다")
            "express_post_id_required" -> communityText(locale, "缺少表白编号", "Missing confession ID", "缺少表白編號", "告白IDがありません", "고백 ID가 없습니다")
            "express_post_not_found" -> communityText(locale, "查询的表白内容不存在", "The confession post was not found", "查詢的表白內容不存在", "告白投稿が見つかりません", "고백 게시글을 찾을 수 없습니다")
            "express_comment_missing" -> communityText(locale, "缺少评论内容", "Missing comment content", "缺少評論內容", "コメント内容がありません", "댓글 내용이 없습니다")
            "express_guess_name_required" -> communityText(locale, "缺少猜测姓名", "Missing guessed name", "缺少猜測姓名", "推測した名前がありません", "추측한 이름이 없습니다")
            "express_default_content" -> communityText(locale, "新的表白内容", "New confession", "新的表白內容", "新しい告白", "새 고백 내용")
            "express_default_name" -> communityText(locale, "你", "You", "你", "あなた", "당신")
            "topic_post_id_required" -> communityText(locale, "缺少话题编号", "Missing topic ID", "缺少話題編號", "話題IDがありません", "토픽 ID가 없습니다")
            "topic_post_not_found" -> communityText(locale, "查询的话题内容不存在", "The topic post was not found", "查詢的話題內容不存在", "話題投稿が見つかりません", "토픽 게시글을 찾을 수 없습니다")
            "image_index_required" -> communityText(locale, "缺少图片序号", "Missing image index", "缺少圖片序號", "画像番号がありません", "이미지 번호가 없습니다")
            "image_not_found" -> communityText(locale, "图片不存在", "Image not found", "圖片不存在", "画像が見つかりません", "이미지를 찾을 수 없습니다")
            "topic_default_topic" -> communityText(locale, "校园生活", "Campus life", "校園生活", "キャンパスライフ", "캠퍼스 라이프")
            "topic_default_content" -> communityText(locale, "分享校园生活点滴。", "Sharing little moments from campus life.", "分享校園生活點滴。", "キャンパスの日常をシェアします。", "캠퍼스 일상을 나눠요.")
            "delivery_order_id_required" -> communityText(locale, "缺少跑腿编号", "Missing order ID", "缺少跑腿編號", "注文IDがありません", "주문 ID가 없습니다")
            "delivery_order_not_found" -> communityText(locale, "查询的全民快递订单不存在", "The errand order was not found", "查詢的全民快遞訂單不存在", "配送注文が見つかりません", "배달 주문을 찾을 수 없습니다")
            "delivery_order_taken" -> communityText(locale, "该订单已被接单", "This order has already been accepted", "該訂單已被接單", "この注文はすでに受注されています", "이 주문은 이미 수락되었습니다")
            "delivery_trade_id_required" -> communityText(locale, "缺少交易编号", "Missing trade ID", "缺少交易編號", "取引IDがありません", "거래 ID가 없습니다")
            "delivery_trade_not_found" -> communityText(locale, "交易记录不存在", "Trade record not found", "交易記錄不存在", "取引記録が見つかりません", "거래 기록을 찾을 수 없습니다")
            "delivery_default_company" -> communityText(locale, "白云校区快递站", "Baiyun Campus Parcel Station", "白雲校區快遞站", "白雲キャンパス宅配ステーション", "바이윈 캠퍼스 택배 보관소")
            "delivery_default_name" -> communityText(locale, "取件任务", "Pickup task", "取件任務", "荷物受け取り依頼", "택배 수령 요청")
            "delivery_default_number" -> communityText(locale, "待补充", "To be added", "待補充", "後で追加", "추후 입력")
            "delivery_default_address" -> communityText(locale, "待补充地址", "Address to be added", "待補充地址", "後で追加する住所", "추후 입력 주소")
            "photograph_post_id_required" -> communityText(locale, "缺少作品编号", "Missing photo ID", "缺少作品編號", "作品IDがありません", "작품 ID가 없습니다")
            "photograph_post_not_found" -> communityText(locale, "查询的拍好校园作品不存在", "The photo post was not found", "查詢的拍好校園作品不存在", "作品が見つかりません", "작품을 찾을 수 없습니다")
            "photograph_default_title" -> communityText(locale, "新的校园瞬间", "New campus moments", "新的校園瞬間", "新しいキャンパスの一瞬", "새로운 캠퍼스 순간")
            "photograph_default_content" -> communityText(locale, "刚刚上传了一组校园照片。", "Just uploaded a new set of campus photos.", "剛剛上傳了一組校園照片。", "キャンパス写真をアップしました。", "방금 캠퍼스 사진을 올렸습니다.")
            else -> key
        }
    }

    private fun success(request: Request, key: String): String =
        MockUtils.successJson(communityMessage(request.requestLocale(), key))

    private fun failure(request: Request, key: String): String =
        MockUtils.failureJson(communityMessage(request.requestLocale(), key))

    private fun communitySeedText(locale: String, value: String): String {
        return when (value) {
            MockUtils.MOCK_PROFILE_NICKNAME,
            "林知远" -> communityText(locale, "林知远", "Lin Zhiyuan")
            "林学长" -> communityText(locale, "林学长", "Senior Lin")
            "陈同学" -> communityText(locale, "陈同学", "Student Chen")
            "匿名同学 A" -> communityText(locale, "匿名同学 A", "Anonymous Student A")
            "匿名同学 B" -> communityText(locale, "匿名同学 B", "Anonymous Student B")
            "匿名同学 C" -> communityText(locale, "匿名同学 C", "Anonymous Student C")
            "匿名同学 D" -> communityText(locale, "匿名同学 D", "Anonymous Student D")
            "匿名同学 E" -> communityText(locale, "匿名同学 E", "Anonymous Student E")
            "晴天" -> communityText(locale, "晴天", "Sunny")
            "晚风" -> communityText(locale, "晚风", "Evening Breeze")
            "晚课同学" -> communityText(locale, "晚课同学", "Evening Class Student")
            "隔壁班同学" -> communityText(locale, "隔壁班同学", "Student Next Door")
            "小林" -> communityText(locale, "小林", "Xiao Lin")
            "理工楼同学" -> communityText(locale, "理工楼同学", "Engineering Building Student")
            "实验楼同学" -> communityText(locale, "实验楼同学", "Lab Building Student")
            "食堂搭子" -> communityText(locale, "食堂搭子", "Cafeteria Buddy")
            "陈晴" -> communityText(locale, "陈晴", "Chen Qing")
            "周岚" -> communityText(locale, "周岚", "Zhou Lan")
            "阿哲" -> communityText(locale, "阿哲", "A-Zhe")
            "图书馆四楼靠窗的你" -> communityText(locale, "图书馆四楼靠窗的你", "You by the fourth-floor library window")
            "小周" -> communityText(locale, "小周", "Xiao Zhou")
            "软件工程" -> communityText(locale, "软件工程", "Software Engineering")
            "计算机科学与技术" -> communityText(locale, "计算机科学与技术", "Computer Science and Technology")
            "汉语言文学" -> communityText(locale, "汉语言文学", "Chinese Language and Literature")
            "95 新蓝牙耳机" -> communityText(locale, "95 新蓝牙耳机", "95%-new Bluetooth Earbuds")
            "日常通勤使用，续航稳定，附原装充电盒和备用耳帽。" -> communityText(locale, "日常通勤使用，续航稳定，附原装充电盒和备用耳帽。", "Used for daily commuting. Battery life is steady, and it comes with the original charging case and spare ear tips.")
            "白云校区图书馆门口" -> communityText(locale, "白云校区图书馆门口", "Outside the Baiyun Campus library")
            "公路自行车头盔" -> communityText(locale, "公路自行车头盔", "Road Bike Helmet")
            "买车时一起入的头盔，尺寸 M，适合日常校内骑行。" -> communityText(locale, "买车时一起入的头盔，尺寸 M，适合日常校内骑行。", "Bought with my bike. Size M and great for everyday rides around campus.")
            "龙洞校区宿舍区" -> communityText(locale, "龙洞校区宿舍区", "Longdong Campus dorm area")
            "高数教材与笔记" -> communityText(locale, "高数教材与笔记", "Calculus Textbook and Notes")
            "教材九成新，附期末整理笔记和章节错题总结，适合补基础。" -> communityText(locale, "教材九成新，附期末整理笔记和章节错题总结，适合补基础。", "Textbook is in very good condition, with final-review notes and chapter mistake summaries. Good for rebuilding the basics.")
            "白云校区教学楼 A 栋" -> communityText(locale, "白云校区教学楼 A 栋", "Teaching Building A, Baiyun Campus")
            "折叠宿舍小桌" -> communityText(locale, "折叠宿舍小桌", "Foldable Dorm Desk")
            "搬宿舍后闲置，桌面无明显磕碰，适合床上学习和放平板。" -> communityText(locale, "搬宿舍后闲置，桌面无明显磕碰，适合床上学习和放平板。", "No longer needed after moving dorms. The surface has no obvious damage and works well for studying in bed or holding a tablet.")
            "三水校区生活区" -> communityText(locale, "三水校区生活区", "Sanshui Campus living area")
            "平板保护壳" -> communityText(locale, "平板保护壳", "Tablet Case")
            "尺寸不匹配所以闲置，适合 10.9 寸设备，边角无破损。" -> communityText(locale, "尺寸不匹配所以闲置，适合 10.9 寸设备，边角无破损。", "Unused because the size did not match. Fits 10.9-inch devices and the corners are intact.")
            "校内快递站附近" -> communityText(locale, "校内快递站附近", "Near the on-campus parcel station")
            "遗失黑色校园卡卡套" -> communityText(locale, "遗失黑色校园卡卡套", "Lost Black Campus Card Holder")
            "内含校园卡和一张图书馆借阅卡，今天中午在食堂附近丢失。" -> communityText(locale, "内含校园卡和一张图书馆借阅卡，今天中午在食堂附近丢失。", "It contains a campus card and a library card. It was lost near the cafeteria at noon today.")
            "白云校区第一食堂" -> communityText(locale, "白云校区第一食堂", "Baiyun Campus First Cafeteria")
            "拾到一把宿舍钥匙" -> communityText(locale, "拾到一把宿舍钥匙", "Found a Dorm Key")
            "钥匙圈上有蓝色挂件，已先放在宿管阿姨处，可凭特征领取。" -> communityText(locale, "钥匙圈上有蓝色挂件，已先放在宿管阿姨处，可凭特征领取。", "There is a blue keychain on it. It has been left with the dorm manager and can be claimed by describing it.")
            "龙洞校区 6 栋楼下" -> communityText(locale, "龙洞校区 6 栋楼下", "Downstairs of Building 6, Longdong Campus")
            "寻找灰色水杯" -> communityText(locale, "寻找灰色水杯", "Looking for a Gray Water Bottle")
            "杯身贴有课程表贴纸，可能落在综合楼自习室或图书馆。" -> communityText(locale, "杯身贴有课程表贴纸，可能落在综合楼自习室或图书馆。", "There is a class-schedule sticker on it. It may have been left in a study room in the main building or in the library.")
            "综合楼 / 图书馆" -> communityText(locale, "综合楼 / 图书馆", "Main Building / Library")
            "拾到学生证一本" -> communityText(locale, "拾到学生证一本", "Found a Student ID")
            "在操场看台附近拾到学生证，封皮略旧，请失主联系认领。" -> communityText(locale, "在操场看台附近拾到学生证，封皮略旧，请失主联系认领。", "A student ID was found near the stadium stand. The cover is slightly worn. Please contact me to claim it.")
            "白云校区操场看台" -> communityText(locale, "白云校区操场看台", "Stadium stand, Baiyun Campus")
            "最近在赶课程设计，虽然每天都很忙，但在图书馆看到晚霞的时候还是会觉得这学期没白熬。" -> communityText(locale, "最近在赶课程设计，虽然每天都很忙，但在图书馆看到晚霞的时候还是会觉得这学期没白熬。", "I've been rushing my course project lately. Every day is busy, but when I see the sunset from the library, it still feels like this semester has been worth it.")
            "如果有一天真的能把校园里这些分散的服务都整理好，应该会很有成就感吧。" -> communityText(locale, "如果有一天真的能把校园里这些分散的服务都整理好，应该会很有成就感吧。", "If I can really organize all these scattered campus services one day, that would feel pretty rewarding.")
            "这是一条语音树洞，点进去可以听到完整内容。" -> communityText(locale, "这是一条语音树洞，点进去可以听到完整内容。", "This is a voice post. Open it to hear the full message.")
            "晚霞真的很治愈，辛苦啦。" -> communityText(locale, "晚霞真的很治愈，辛苦啦。", "Sunsets really are healing. Hang in there.")
            "加油，课程设计做完一定会很有成就感。" -> communityText(locale, "加油，课程设计做完一定会很有成就感。", "Keep going. Finishing the project will feel great.")
            "会的，现在已经在慢慢发生了。" -> communityText(locale, "会的，现在已经在慢慢发生了。", "It will. It's already happening little by little.")
            "计算机科学系" -> communityText(locale, "计算机科学系", "Computer Science Department")
            "汕头" -> communityText(locale, "汕头", "Shantou")
            "能聊天、会运动、作息稳定。" -> communityText(locale, "能聊天、会运动、作息稳定。", "Easy to talk to, enjoys sports, and keeps a steady routine.")
            "英语教育" -> communityText(locale, "英语教育", "English Education")
            "佛山" -> communityText(locale, "佛山", "Foshan")
            "喜欢夜跑和音乐节。" -> communityText(locale, "喜欢夜跑和音乐节。", "Likes night running and music festivals.")
            "深圳" -> communityText(locale, "深圳", "Shenzhen")
            "周末常去图书馆和咖啡馆。" -> communityText(locale, "周末常去图书馆和咖啡馆。", "Often goes to the library and cafes on weekends.")
            "看你也喜欢夜跑，想认识一下。" -> communityText(locale, "看你也喜欢夜跑，想认识一下。", "You seem to like night running too. I'd love to get to know you.")
            "周末一起去图书馆吗？" -> communityText(locale, "周末一起去图书馆吗？", "Want to go to the library together this weekend?")
            "昨晚在操场散步的时候又遇到你了，想认真和你说一句：你笑起来的时候真的会让人记很久。" -> communityText(locale, "昨晚在操场散步的时候又遇到你了，想认真和你说一句：你笑起来的时候真的会让人记很久。", "I ran into you again while walking on the track last night, and I want to tell you seriously: your smile really stays in people's minds for a long time.")
            "谢谢你上周把落下的耳机递给我，也谢谢你顺手提醒我别忘了借书。虽然不知道你叫什么，但还是想把这份好感留在这里。" -> communityText(locale, "谢谢你上周把落下的耳机递给我，也谢谢你顺手提醒我别忘了借书。虽然不知道你叫什么，但还是想把这份好感留在这里。", "Thank you for handing me the earbuds I dropped last week, and for reminding me not to forget my library book. I still don't know your name, but I wanted to leave this little crush here.")
            "你总是在早八前把实验室门口的灯打开，大家都还没完全醒的时候，你已经准备好一整天了。" -> communityText(locale, "你总是在早八前把实验室门口的灯打开，大家都还没完全醒的时候，你已经准备好一整天了。", "You always turn on the light outside the lab before the 8 a.m. class. While everyone else is still waking up, you're already ready for the whole day.")
            "这条好真诚，希望你能被看到。" -> communityText(locale, "这条好真诚，希望你能被看到。", "This is so sincere. I hope they see it.")
            "操场的故事总是很浪漫。" -> communityText(locale, "操场的故事总是很浪漫。", "Stories from the track are always romantic.")
            "图书馆真的很容易发生一些温柔的小事。" -> communityText(locale, "图书馆真的很容易发生一些温柔的小事。", "Little gentle moments happen so easily in the library.")
            "如果真的再遇见，也许可以主动打个招呼。" -> communityText(locale, "如果真的再遇见，也许可以主动打个招呼。", "If you meet again, maybe you can say hi first.")
            "这条让我想起很多校园瞬间。" -> communityText(locale, "这条让我想起很多校园瞬间。", "This reminded me of a lot of campus moments.")
            "春招准备" -> communityText(locale, "春招准备", "Spring Recruitment Prep")
            "最近大家都在怎么准备春招面试？除了简历和项目复盘，我感觉表达节奏也挺重要，想看看有没有人总结过面试时最容易卡住的点。" -> communityText(locale, "最近大家都在怎么准备春招面试？除了简历和项目复盘，我感觉表达节奏也挺重要，想看看有没有人总结过面试时最容易卡住的点。", "How is everyone preparing for spring recruitment interviews lately? Beyond resumes and project reviews, I feel pacing in how you speak matters too. Has anyone summarized the parts where interviews most often get stuck?")
            "教室查询" -> communityText(locale, "教室查询", "Classroom Search")
            "新版教室查询你们更喜欢按时间段查还是按校区先筛？这两种方式我都试了几轮，感觉移动端入口可以再更直接一点。" -> communityText(locale, "新版教室查询你们更喜欢按时间段查还是按校区先筛？这两种方式我都试了几轮，感觉移动端入口可以再更直接一点。", "For the new classroom search, do you prefer starting with time slots or filtering by campus first? I've tried both a few rounds, and the mobile entry still feels like it could be more direct.")
            "图书馆自习" -> communityText(locale, "图书馆自习", "Library Study")
            "最近四楼靠窗区域人越来越多了，大家有没有自己固定的自习位？如果换楼层，你们更看重安静还是插座方便？" -> communityText(locale, "最近四楼靠窗区域人越来越多了，大家有没有自己固定的自习位？如果换楼层，你们更看重安静还是插座方便？", "The window seats on the fourth floor have been getting crowded lately. Do you all have your own regular study spots? If you switch floors, do you care more about quiet or easy access to power outlets?")
            "取件任务" -> communityText(locale, "取件任务", "Pickup Task")
            "白云校区快递驿站" -> communityText(locale, "白云校区快递驿站", "Baiyun Campus Parcel Station")
            "12 栋 402" -> communityText(locale, "12 栋 402", "Building 12, Room 402")
            "到楼下给我发消息就好。" -> communityText(locale, "到楼下给我发消息就好。", "Just message me when you get downstairs.")
            "龙洞校区菜鸟站" -> communityText(locale, "龙洞校区菜鸟站", "Longdong Campus Cainiao Station")
            "8 栋 215" -> communityText(locale, "8 栋 215", "Building 8, Room 215")
            "晚上 8 点后方便。" -> communityText(locale, "晚上 8 点后方便。", "After 8 p.m. works best.")
            "白云校区南门快递点" -> communityText(locale, "白云校区南门快递点", "South Gate Parcel Point, Baiyun Campus")
            "图书馆门口" -> communityText(locale, "图书馆门口", "At the library entrance")
            "帮忙带一份冰美式更好。" -> communityText(locale, "帮忙带一份冰美式更好。", "Even better if you can bring an iced Americano.")
            "三水校区驿站" -> communityText(locale, "三水校区驿站", "Sanshui Campus Parcel Station")
            "7 栋大厅" -> communityText(locale, "7 栋大厅", "Lobby of Building 7")
            "雨后教学楼的光" -> communityText(locale, "雨后教学楼的光", "The Light on the Teaching Building After Rain")
            "下课后回头看了一眼，玻璃幕墙把晚霞和树影一起收进去了，顺手拍了两张。" -> communityText(locale, "下课后回头看了一眼，玻璃幕墙把晚霞和树影一起收进去了，顺手拍了两张。", "After class I glanced back and saw the glass facade catching both the sunset and the tree shadows, so I took a couple of shots.")
            "食堂窗口前的人间烟火" -> communityText(locale, "食堂窗口前的人间烟火", "The Warm Scene in Front of the Cafeteria Window")
            "晚饭时间总是最有校园生活感的一刻，大家一边排队一边讨论今晚的作业。" -> communityText(locale, "晚饭时间总是最有校园生活感的一刻，大家一边排队一边讨论今晚的作业。", "Dinner time always feels most like campus life. Everyone lines up while talking about tonight's assignments.")
            "图书馆四楼窗边" -> communityText(locale, "图书馆四楼窗边", "By the Fourth-Floor Library Window")
            "复习周前夕，靠窗的位置总是很快坐满，但光线真的很好。" -> communityText(locale, "复习周前夕，靠窗的位置总是很快坐满，但光线真的很好。", "Right before review week, the window seats fill up fast, but the light there is really great.")
            "这一组光影真的很好看。" -> communityText(locale, "这一组光影真的很好看。", "The light and shadows in this set look great.")
            "第二张的层次感特别舒服。" -> communityText(locale, "第二张的层次感特别舒服。", "The depth in the second shot feels especially nice.")
            "这张一下就把下课后的氛围拍出来了。" -> communityText(locale, "这张一下就把下课后的氛围拍出来了。", "This shot instantly captures the atmosphere after class.")
            else -> value
        }
    }

    // ── Payload converters ──────────────────────────────────────────────────

    private fun MockMarketplaceItemRecord.toMarketplaceItemPayload(locale: String): Map<String, Any?> {
        return linkedMapOf(
            "id" to id, "username" to username, "name" to communitySeedText(locale, name),
            "description" to communitySeedText(locale, description), "price" to price, "location" to communitySeedText(locale, location),
            "type" to type, "qq" to qq, "phone" to phone, "state" to state,
            "publishTime" to publishTime, "pictureURL" to pictureURL
        )
    }

    private fun MockMarketplaceProfileRecord.toMarketplaceProfilePayload(locale: String): Map<String, Any?> {
        return linkedMapOf(
            "avatarURL" to avatarURL, "username" to username, "nickname" to communitySeedText(locale, nickname),
            "faculty" to faculty, "enrollment" to enrollment, "major" to communitySeedText(locale, major)
        )
    }

    private fun MockLostFoundItemRecord.toLostFoundItemPayload(locale: String): Map<String, Any?> {
        return linkedMapOf(
            "id" to id, "username" to username, "name" to communitySeedText(locale, name),
            "description" to communitySeedText(locale, description), "location" to communitySeedText(locale, location), "itemType" to itemType,
            "lostType" to lostType, "qq" to qq, "wechat" to wechat, "phone" to phone,
            "state" to state, "publishTime" to publishTime, "pictureURL" to pictureURL
        )
    }

    private fun MockLostFoundProfileRecord.toLostFoundProfilePayload(locale: String): Map<String, Any?> {
        return linkedMapOf("avatarURL" to avatarURL, "username" to username, "nickname" to communitySeedText(locale, nickname))
    }

    private fun MockSecretPostRecord.toSecretPayload(locale: String, includeComments: Boolean): Map<String, Any?> {
        return linkedMapOf(
            "id" to id, "username" to communitySeedText(locale, username), "content" to communitySeedText(locale, content),
            "theme" to theme, "type" to type, "timer" to timer, "state" to state,
            "publishTime" to publishTime, "likeCount" to likeCount,
            "commentCount" to commentCount, "liked" to if (liked) 1 else 0,
            "voiceURL" to voiceURL,
            "secretCommentList" to if (includeComments) {
                mockSecretComments
                    .filter { it.contentId == id }
                    .sortedByDescending { it.publishTime }
                    .map { it.toSecretCommentPayload(locale) }
            } else null
        )
    }

    private fun MockSecretCommentRecord.toSecretCommentPayload(locale: String): Map<String, Any?> {
        return linkedMapOf(
            "id" to id, "contentId" to contentId, "username" to communitySeedText(locale, username),
            "comment" to communitySeedText(locale, comment), "publishTime" to publishTime, "avatarTheme" to avatarTheme
        )
    }

    private fun MockDatingProfileRecord.toDatingProfilePayload(locale: String): Map<String, Any?> {
        return linkedMapOf(
            "profileId" to profileId, "username" to username, "nickname" to communitySeedText(locale, nickname),
            "grade" to grade, "faculty" to communitySeedText(locale, faculty), "hometown" to communitySeedText(locale, hometown),
            "content" to communitySeedText(locale, content), "qq" to qq, "wechat" to wechat,
            "area" to area, "state" to state, "pictureURL" to pictureURL
        )
    }

    private fun MockDatingPickRecord.toDatingPickPayload(locale: String): Map<String, Any?> {
        return linkedMapOf(
            "pickId" to pickId, "roommateProfile" to profile.toDatingProfilePayload(locale),
            "username" to username, "content" to communitySeedText(locale, content), "state" to state
        )
    }

    private fun MockExpressPostRecord.toExpressPayload(locale: String): Map<String, Any?> {
        return linkedMapOf(
            "id" to id, "username" to username, "nickname" to communitySeedText(locale, nickname),
            "realname" to communitySeedText(locale, realname), "selfGender" to selfGender, "name" to communitySeedText(locale, name),
            "content" to communitySeedText(locale, content), "personGender" to personGender,
            "publishTime" to publishTime, "likeCount" to likeCount, "liked" to liked,
            "commentCount" to commentCount, "guessCount" to guessCount,
            "guessSum" to guessSum, "canGuess" to canGuess
        )
    }

    private fun MockExpressCommentRecord.toExpressCommentPayload(locale: String): Map<String, Any?> {
        return linkedMapOf(
            "id" to id, "username" to username, "nickname" to communitySeedText(locale, nickname),
            "expressId" to expressId, "comment" to communitySeedText(locale, comment), "publishTime" to publishTime
        )
    }

    private fun MockTopicPostRecord.toTopicPayload(locale: String): Map<String, Any?> {
        return linkedMapOf(
            "id" to id, "username" to username, "topic" to communitySeedText(locale, topic),
            "content" to communitySeedText(locale, content), "count" to count, "publishTime" to publishTime,
            "likeCount" to likeCount, "liked" to liked,
            "firstImageUrl" to firstImageUrl, "imageUrls" to imageUrls
        )
    }

    private fun MockDeliveryOrderRecord.toDeliveryOrderPayload(locale: String): Map<String, Any?> {
        return linkedMapOf(
            "orderId" to orderId, "username" to username, "orderTime" to orderTime,
            "name" to communitySeedText(locale, name), "number" to number, "phone" to phone, "price" to price,
            "company" to communitySeedText(locale, company), "address" to communitySeedText(locale, address), "state" to state, "remarks" to communitySeedText(locale, remarks)
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

    private fun MockPhotographPostRecord.toPhotographPayload(locale: String, includeComments: Boolean): Map<String, Any?> {
        return linkedMapOf(
            "id" to id, "title" to communitySeedText(locale, title), "content" to communitySeedText(locale, content),
            "count" to photoCount(), "type" to type, "username" to username,
            "createTime" to createTime, "likeCount" to likeCount,
            "commentCount" to commentCount, "liked" to if (liked) 1 else 0,
            "firstImageUrl" to (firstImageUrl ?: resolvedImageUrls().firstOrNull()),
            "imageUrls" to resolvedImageUrls(),
            "photographCommentList" to if (includeComments) {
                mockPhotographCommentRecords
                    .filter { it.photoId == id }
                    .sortedByDescending { it.createTime }
                    .map { it.toPhotographCommentPayload(locale) }
            } else null
        )
    }

    private fun MockPhotographCommentRecord.toPhotographCommentPayload(locale: String): Map<String, Any?> {
        return linkedMapOf(
            "commentId" to commentId, "photoId" to photoId, "username" to username,
            "nickname" to communitySeedText(locale, nickname), "comment" to communitySeedText(locale, comment), "createTime" to createTime
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
        val locale = request.requestLocale()
        val items = mockMarketplaceItems
            .filter { it.state == 1 }
            .sortedByDescending { it.publishTime }
            .map { it.toMarketplaceItemPayload(locale) }
        return MockUtils.successDataJson(items)
    }

    fun mockMarketplaceItemsByType(request: Request): String {
        val locale = request.requestLocale()
        val segments = request.url.pathSegments
        val typeIndex = segments.indexOf("type")
        val type = segments.getOrNull(typeIndex + 1)?.toIntOrNull()
        val items = mockMarketplaceItems
            .filter { it.state == 1 && (type == null || it.type == type) }
            .sortedByDescending { it.publishTime }
            .map { it.toMarketplaceItemPayload(locale) }
        return MockUtils.successDataJson(items)
    }

    fun mockMarketplaceDetail(request: Request): String {
        val locale = request.requestLocale()
        val item = mockMarketplaceItems.findById(request.itemIdFromPath())
            ?: return failure(request, "marketplace_detail_not_found")
        val profile = mockMarketplaceProfiles.firstOrNull { it.username == item.username }
        return MockUtils.successDataJson(
            linkedMapOf(
                "profile" to profile?.toMarketplaceProfilePayload(locale),
                "secondhandItem" to item.toMarketplaceItemPayload(locale)
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
        val locale = request.requestLocale()
        val mine = mockMarketplaceItems.filter { it.username == MockUtils.MOCK_CURRENT_USERNAME }
        return MockUtils.successDataJson(
            linkedMapOf(
                "doing" to mine.filter { it.state == 1 }.sortedByDescending { it.publishTime }.map { it.toMarketplaceItemPayload(locale) },
                "sold" to mine.filter { it.state == 2 }.sortedByDescending { it.publishTime }.map { it.toMarketplaceItemPayload(locale) },
                "off" to mine.filter { it.state == 0 }.sortedByDescending { it.publishTime }.map { it.toMarketplaceItemPayload(locale) }
            )
        )
    }

    fun mockMarketplacePublish(request: Request): String {
        val locale = request.requestLocale()
        val name = request.multipartField("name").orEmpty().trim()
        val description = request.multipartField("description").orEmpty().trim()
        val price = request.multipartField("price").orEmpty().trim()
        val location = request.multipartField("location").orEmpty().trim()
        val type = request.multipartField("type")?.toIntOrNull() ?: 11
        val qq = request.multipartField("qq").orEmpty().trim()
        val phone = request.multipartField("phone").orEmpty().trim()
        if (name.isBlank() || description.isBlank() || location.isBlank() || qq.isBlank()) {
            return failure(request, "marketplace_incomplete")
        }
        if (request.multipartImageCount() <= 0) {
            return failure(request, "marketplace_need_image")
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
                publishTime = communityMessage(locale, "just_now"),
                pictureURL = pictureUrls
            )
        )
        return success(request, "published")
    }

    fun mockMarketplaceStateUpdate(request: Request): String {
        val itemId = request.itemIdFromPath() ?: return failure(request, "marketplace_item_id_required")
        val targetState = request.url.queryParameter("state")
            ?.toIntOrNull()
            ?: request.formFields()["state"]?.toIntOrNull()
            ?: return failure(request, "marketplace_state_required")
        val index = mockMarketplaceItems.indexOfFirst { it.id.toString() == itemId }
        if (index < 0) return failure(request, "marketplace_item_not_found")
        mockMarketplaceItems[index] = mockMarketplaceItems[index].copy(state = targetState)
        return success(request, "status_updated")
    }

    fun mockMarketplaceUpdate(request: Request): String {
        val itemId = request.itemIdFromPath() ?: return failure(request, "marketplace_item_id_required")
        val fields = request.formFields()
        val index = mockMarketplaceItems.indexOfFirst { it.id.toString() == itemId }
        if (index < 0) return failure(request, "marketplace_item_not_found")
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
        return success(request, "marketplace_item_updated")
    }

    // ── Lost & Found endpoints ──────────────────────────────────────────────

    fun mockLostFoundItemList(request: Request): String {
        val locale = request.requestLocale()
        val lostType = if (request.url.encodedPath.contains("/founditem/")) 1 else 0
        val items = mockLostFoundItems
            .filter { it.lostType == lostType && it.state == 0 }
            .sortedByDescending { it.publishTime }
            .map { it.toLostFoundItemPayload(locale) }
        return MockUtils.successDataJson(items)
    }

    fun mockLostFoundDetail(request: Request): String {
        val locale = request.requestLocale()
        val item = mockLostFoundItems.findById(request.itemIdFromPath())
            ?: return failure(request, "lostfound_detail_not_found")
        val profile = mockLostFoundProfiles.firstOrNull { it.username == item.username }
        return MockUtils.successDataJson(
            linkedMapOf(
                "item" to item.toLostFoundItemPayload(locale),
                "profile" to profile?.toLostFoundProfilePayload(locale)
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
        val locale = request.requestLocale()
        val mine = mockLostFoundItems.filter { it.username == MockUtils.MOCK_CURRENT_USERNAME }
        return MockUtils.successDataJson(
            linkedMapOf(
                "lost" to mine.filter { it.lostType == 0 && it.state == 0 }.sortedByDescending { it.publishTime }.map { it.toLostFoundItemPayload(locale) },
                "found" to mine.filter { it.lostType == 1 && it.state == 0 }.sortedByDescending { it.publishTime }.map { it.toLostFoundItemPayload(locale) },
                "didfound" to mine.filter { it.state == 1 }.sortedByDescending { it.publishTime }.map { it.toLostFoundItemPayload(locale) }
            )
        )
    }

    fun mockLostFoundPublish(request: Request): String {
        val locale = request.requestLocale()
        val name = request.multipartField("name").orEmpty().ifBlank { communityMessage(locale, "lostfound_default_name") }
        val description = request.multipartField("description").orEmpty().ifBlank { communityMessage(locale, "lostfound_default_description") }
        val location = request.multipartField("location").orEmpty().ifBlank { communityMessage(locale, "lostfound_default_location") }
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
                publishTime = communityMessage(locale, "just_now"),
                pictureURL = imageUrls
            )
        )
        return success(request, "published")
    }

    fun mockLostFoundUpdate(request: Request): String {
        val itemId = request.itemIdFromPath() ?: return failure(request, "lostfound_item_id_required")
        val fields = request.formFields()
        val index = mockLostFoundItems.indexOfFirst { it.id.toString() == itemId }
        if (index < 0) return failure(request, "lostfound_item_not_found")
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
        return success(request, "lostfound_item_updated")
    }

    fun mockLostFoundDidFound(request: Request): String {
        val itemId = request.itemIdFromPath() ?: return failure(request, "lostfound_item_id_required")
        val index = mockLostFoundItems.indexOfFirst { it.id.toString() == itemId }
        if (index < 0) return failure(request, "lostfound_item_not_found")
        mockLostFoundItems[index] = mockLostFoundItems[index].copy(state = 1)
        return success(request, "lostfound_marked_found")
    }

    // ── Secret endpoints ────────────────────────────────────────────────────

    fun mockSecretPostList(request: Request): String {
        val locale = request.requestLocale()
        val items = mockSecretPosts
            .filter { it.state != 2 }
            .sortedByDescending { it.publishTime }
            .map { it.toSecretPayload(locale, includeComments = false) }
        return MockUtils.successDataJson(items)
    }

    fun mockSecretMyPosts(request: Request): String {
        val locale = request.requestLocale()
        val items = mockSecretPosts
            .filter { it.username == MockUtils.MOCK_CURRENT_USERNAME && it.state != 2 }
            .sortedByDescending { it.publishTime }
            .map { it.toSecretPayload(locale, includeComments = false) }
        return MockUtils.successDataJson(items)
    }

    fun mockSecretDetail(request: Request): String {
        val locale = request.requestLocale()
        val postId = request.itemIdFromPath() ?: return failure(request, "secret_post_id_required")
        val post = mockSecretPosts.findById(postId) ?: return failure(request, "secret_post_not_found")
        return MockUtils.successDataJson(post.toSecretPayload(locale, includeComments = true))
    }

    fun mockSecretCommentList(request: Request): String {
        val locale = request.requestLocale()
        val postId = request.itemIdFromPath() ?: return failure(request, "secret_post_id_required")
        val comments = mockSecretComments
            .filter { it.contentId.toString() == postId }
            .sortedByDescending { it.publishTime }
            .map { it.toSecretCommentPayload(locale) }
        return MockUtils.successDataJson(comments)
    }

    fun mockSecretCommentSubmit(request: Request): String {
        val locale = request.requestLocale()
        val postId = request.itemIdFromPath() ?: return failure(request, "secret_post_id_required")
        val comment = request.url.queryParameter("comment")
            ?: request.formFields()["comment"]
            ?: return failure(request, "comment_required")
        val trimmed = comment.trim()
        if (trimmed.isBlank()) return failure(request, "comment_required")
        if (trimmed.length > 50) return failure(request, "secret_comment_too_long")
        val postIndex = mockSecretPosts.indexOfFirst { it.id.toString() == postId }
        if (postIndex < 0) return failure(request, "secret_post_not_found")
        mockSecretComments.add(
            0,
            MockSecretCommentRecord(
                id = (mockSecretComments.maxOfOrNull { it.id } ?: 7000) + 1,
                contentId = postId.toInt(),
                username = MockProfileProvider.currentMockAnonymousName(locale),
                comment = trimmed,
                publishTime = communityMessage(locale, "just_now"),
                avatarTheme = 1
            )
        )
        val post = mockSecretPosts[postIndex]
        mockSecretPosts[postIndex] = post.copy(commentCount = post.commentCount + 1)
        return success(request, "comment_sent")
    }

    fun mockSecretLikeUpdate(request: Request): String {
        val postId = request.itemIdFromPath() ?: return failure(request, "secret_post_id_required")
        val like = request.url.queryParameter("like")
            ?.toIntOrNull()
            ?: request.formFields()["like"]?.toIntOrNull()
            ?: return failure(request, "like_state_required")
        val postIndex = mockSecretPosts.indexOfFirst { it.id.toString() == postId }
        if (postIndex < 0) return failure(request, "secret_post_not_found")
        val post = mockSecretPosts[postIndex]
        val liked = like == 1
        mockSecretPosts[postIndex] = post.copy(
            liked = liked,
            likeCount = (post.likeCount + if (liked && !post.liked) 1 else if (!liked && post.liked) -1 else 0).coerceAtLeast(0)
        )
        return success(request, "like_status_updated")
    }

    fun mockSecretPublish(request: Request): String {
        val locale = request.requestLocale()
        val theme = request.multipartField("theme")?.toIntOrNull() ?: 0
        val content = request.multipartField("content").orEmpty().ifBlank { communityMessage(locale, "secret_default_content") }
        val type = request.multipartField("type")?.toIntOrNull() ?: 0
        val timer = request.multipartField("timer")?.toIntOrNull() ?: 0
        val id = (mockSecretPosts.maxOfOrNull { it.id } ?: 9999) + 1
        mockSecretPosts.add(0, MockSecretPostRecord(
            id = id, username = MockUtils.MOCK_CURRENT_USERNAME,
            content = content, theme = theme, type = type, timer = timer,
            state = 1, publishTime = communityMessage(locale, "just_now"), likeCount = 0, commentCount = 0, liked = false
        ))
        return success(request, "published")
    }

    // ── Dating endpoints ────────────────────────────────────────────────────

    fun mockDatingReceivedPickList(request: Request): String {
        val locale = request.requestLocale()
        val items = mockDatingReceivedPicks
            .sortedByDescending { it.updatedAt }
            .map { it.toDatingPickPayload(locale) }
        return MockUtils.successDataJson(items)
    }

    fun mockDatingProfileList(request: Request): String {
        val locale = request.requestLocale()
        val area = request.pathValueAfter("area")?.toIntOrNull()
        val items = mockDatingProfiles
            .filter { it.state != 0 && (area == null || it.area == area) && it.username != MockUtils.MOCK_CURRENT_USERNAME }
            .sortedByDescending { it.profileId }
            .map { it.toDatingProfilePayload(locale) }
        return MockUtils.successDataJson(items)
    }

    fun mockDatingDetail(request: Request): String {
        val locale = request.requestLocale()
        val profileId = request.itemIdFromPath() ?: return failure(request, "dating_profile_id_required")
        val profile = mockDatingProfiles.findById(profileId) ?: return failure(request, "dating_profile_not_found")
        val sentPick = mockDatingSentPicks.firstOrNull { it.profile.profileId.toString() == profileId }
        val isMine = profile.username == MockUtils.MOCK_CURRENT_USERNAME
        return MockUtils.successDataJson(
            linkedMapOf(
                "profile" to profile.toDatingProfilePayload(locale),
                "pictureURL" to profile.pictureURL,
                "isContactVisible" to (sentPick?.state == 1),
                "isPickNotAvailable" to (isMine || sentPick != null)
            )
        )
    }

    fun mockDatingSentPickList(request: Request): String {
        val locale = request.requestLocale()
        val items = mockDatingSentPicks
            .sortedByDescending { it.updatedAt }
            .map { it.toDatingPickPayload(locale) }
        return MockUtils.successDataJson(items)
    }

    fun mockDatingMyPosts(request: Request): String {
        val locale = request.requestLocale()
        val items = mockDatingProfiles
            .filter { it.username == MockUtils.MOCK_CURRENT_USERNAME && it.state != 0 }
            .map { it.toDatingProfilePayload(locale) }
        return MockUtils.successDataJson(items)
    }

    fun mockDatingPublish(request: Request): String {
        val locale = request.requestLocale()
        val nickname = request.multipartField("nickname").orEmpty().ifBlank { MockUtils.MOCK_PROFILE_NICKNAME }
        val grade = request.multipartField("grade")?.toIntOrNull() ?: 3
        val faculty = request.multipartField("faculty").orEmpty().ifBlank { communityMessage(locale, "dating_default_faculty") }
        val hometown = request.multipartField("hometown").orEmpty().ifBlank { communityMessage(locale, "dating_default_hometown") }
        val content = request.multipartField("content").orEmpty().ifBlank { communityMessage(locale, "dating_default_content") }
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
        return success(request, "published")
    }

    fun mockDatingPickSubmit(request: Request): String {
        val fields = request.formFields()
        val profileId = request.url.queryParameter("profileId")
            ?: fields["profileId"]
            ?: return failure(request, "dating_profile_id_required")
        val content = request.url.queryParameter("content")
            ?: fields["content"]
            ?: return failure(request, "dating_pick_content_required")
        val trimmedContent = content.trim()
        if (trimmedContent.isBlank()) return failure(request, "dating_pick_content_required")
        if (trimmedContent.length > 50) return failure(request, "text_too_long")
        val profile = mockDatingProfiles.findById(profileId) ?: return failure(request, "dating_profile_not_found")
        if (profile.username == MockUtils.MOCK_CURRENT_USERNAME) {
            return failure(request, "dating_pick_self_forbidden")
        }
        if (mockDatingSentPicks.any { it.profile.profileId.toString() == profileId }) {
            return failure(request, "dating_pick_duplicate")
        }
        val locale = request.requestLocale()
        mockDatingSentPicks.add(
            0,
            MockDatingPickRecord(
                pickId = (mockDatingSentPicks.maxOfOrNull { it.pickId } ?: 9100) + 1,
                profile = profile,
                username = MockUtils.MOCK_CURRENT_USERNAME,
                content = trimmedContent,
                state = 0,
                updatedAt = communityMessage(locale, "just_now")
            )
        )
        return success(request, "request_sent")
    }

    fun mockDatingPickStateUpdate(request: Request): String {
        val pickId = request.itemIdFromPath() ?: return failure(request, "dating_pick_id_required")
        val state = request.url.queryParameter("state")
            ?.toIntOrNull()
            ?: request.formFields()["state"]?.toIntOrNull()
            ?: return failure(request, "dating_state_required")
        val index = mockDatingReceivedPicks.indexOfFirst { it.pickId.toString() == pickId }
        if (index < 0) return failure(request, "dating_request_not_found")
        mockDatingReceivedPicks[index] = mockDatingReceivedPicks[index].copy(state = state)
        return success(request, "status_updated")
    }

    fun mockDatingProfileHide(request: Request): String {
        val profileId = request.itemIdFromPath() ?: return failure(request, "dating_post_id_required")
        val index = mockDatingProfiles.indexOfFirst { it.profileId.toString() == profileId }
        if (index < 0) return failure(request, "dating_post_not_found")
        mockDatingProfiles[index] = mockDatingProfiles[index].copy(state = 0)
        return success(request, "hidden")
    }

    // ── Express endpoints ───────────────────────────────────────────────────

    fun mockExpressPostList(request: Request): String {
        val locale = request.requestLocale()
        val items = mockExpressPosts
            .sortedByDescending { it.publishTime }
            .map { it.toExpressPayload(locale) }
        return MockUtils.successDataJson(items)
    }

    fun mockExpressMyPosts(request: Request): String {
        val locale = request.requestLocale()
        val items = mockExpressPosts
            .filter { it.username == MockUtils.MOCK_CURRENT_USERNAME }
            .sortedByDescending { it.publishTime }
            .map { it.toExpressPayload(locale) }
        return MockUtils.successDataJson(items)
    }

    fun mockExpressDetail(request: Request): String {
        val locale = request.requestLocale()
        val postId = request.itemIdFromPath() ?: return failure(request, "express_post_id_required")
        val item = mockExpressPosts.findById(postId) ?: return failure(request, "express_post_not_found")
        return MockUtils.successDataJson(item.toExpressPayload(locale))
    }

    fun mockExpressCommentList(request: Request): String {
        val locale = request.requestLocale()
        val postId = request.itemIdFromPath() ?: return failure(request, "express_post_id_required")
        val items = mockExpressComments
            .filter { it.expressId.toString() == postId }
            .sortedByDescending { it.publishTime }
            .map { it.toExpressCommentPayload(locale) }
        return MockUtils.successDataJson(items)
    }

    fun mockExpressCommentSubmit(request: Request): String {
        val locale = request.requestLocale()
        val postId = request.itemIdFromPath() ?: return failure(request, "express_post_id_required")
        val trimmed = request.url.queryParameter("comment")
            ?.trim()
            ?: request.formFields()["comment"]?.trim()
            ?: return failure(request, "express_comment_missing")
        if (trimmed.isBlank()) return failure(request, "comment_required")
        val postIndex = mockExpressPosts.indexOfFirst { it.id.toString() == postId }
        if (postIndex < 0) return failure(request, "express_post_not_found")
        mockExpressComments.add(
            0,
            MockExpressCommentRecord(
                id = (mockExpressComments.maxOfOrNull { it.id } ?: 9500) + 1,
                expressId = postId.toInt(),
                username = MockUtils.MOCK_CURRENT_USERNAME,
                nickname = MockProfileProvider.currentMockNickname(locale),
                comment = trimmed,
                publishTime = communityMessage(locale, "just_now")
            )
        )
        val item = mockExpressPosts[postIndex]
        mockExpressPosts[postIndex] = item.copy(commentCount = item.commentCount + 1)
        return success(request, "comment_sent")
    }

    fun mockExpressLike(request: Request): String {
        val postId = request.itemIdFromPath() ?: return failure(request, "express_post_id_required")
        val postIndex = mockExpressPosts.indexOfFirst { it.id.toString() == postId }
        if (postIndex < 0) return failure(request, "express_post_not_found")
        val item = mockExpressPosts[postIndex]
        if (!item.liked) {
            mockExpressPosts[postIndex] = item.copy(liked = true, likeCount = item.likeCount + 1)
        }
        return success(request, "like_success")
    }

    fun mockExpressGuess(request: Request): String {
        val postId = request.itemIdFromPath() ?: return failure(request, "express_post_id_required")
        val name = request.url.queryParameter("name")
            ?.trim()
            ?: request.formFields()["name"]?.trim()
            ?: return failure(request, "express_guess_name_required")
        val postIndex = mockExpressPosts.indexOfFirst { it.id.toString() == postId }
        if (postIndex < 0) return failure(request, "express_post_not_found")
        val item = mockExpressPosts[postIndex]
        val matched = item.realname.equals(name, ignoreCase = true)
        mockExpressPosts[postIndex] = item.copy(
            guessCount = item.guessCount + if (matched) 1 else 0,
            guessSum = item.guessSum + 1
        )
        return MockUtils.successDataJson(matched)
    }

    fun mockExpressPublish(request: Request): String {
        val locale = request.requestLocale()
        val fields = request.formFields()
        val nickname = fields["nickname"].orEmpty().ifBlank { MockProfileProvider.currentMockNickname(locale) }
        val content = fields["content"].orEmpty().ifBlank { communityMessage(locale, "express_default_content") }
        val name = fields["name"].orEmpty().ifBlank { communityMessage(locale, "express_default_name") }
        val selfGender = fields["selfGender"]?.toIntOrNull() ?: 0
        val personGender = fields["personGender"]?.toIntOrNull() ?: 0
        val id = (mockExpressPosts.maxOfOrNull { it.id } ?: 10999) + 1
        mockExpressPosts.add(0, MockExpressPostRecord(
            id = id, username = MockUtils.MOCK_CURRENT_USERNAME,
            nickname = nickname, realname = fields["realname"].orEmpty(),
            selfGender = selfGender, name = name, content = content,
            personGender = personGender, publishTime = communityMessage(locale, "just_now"),
            likeCount = 0, liked = false, commentCount = 0,
            guessCount = 0, guessSum = 0, canGuess = false
        ))
        return success(request, "published")
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
        val locale = request.requestLocale()
        return MockUtils.successDataJson(matched.drop(start).take(size).map { it.toExpressPayload(locale) })
    }

    // ── Topic endpoints ─────────────────────────────────────────────────────

    fun mockTopicPostList(request: Request): String {
        val locale = request.requestLocale()
        val items = mockTopicPosts
            .sortedByDescending { it.publishTime }
            .map { it.toTopicPayload(locale) }
        return MockUtils.successDataJson(items)
    }

    fun mockTopicMyPosts(request: Request): String {
        val locale = request.requestLocale()
        val items = mockTopicPosts
            .filter { it.username == MockUtils.MOCK_CURRENT_USERNAME }
            .sortedByDescending { it.publishTime }
            .map { it.toTopicPayload(locale) }
        return MockUtils.successDataJson(items)
    }

    fun mockTopicDetail(request: Request): String {
        val locale = request.requestLocale()
        val postId = request.itemIdFromPath() ?: return failure(request, "topic_post_id_required")
        val item = mockTopicPosts.findById(postId) ?: return failure(request, "topic_post_not_found")
        return MockUtils.successDataJson(item.toTopicPayload(locale))
    }

    fun mockTopicLike(request: Request): String {
        val postId = request.itemIdFromPath() ?: return failure(request, "topic_post_id_required")
        val postIndex = mockTopicPosts.indexOfFirst { it.id.toString() == postId }
        if (postIndex < 0) return failure(request, "topic_post_not_found")
        val item = mockTopicPosts[postIndex]
        if (!item.liked) {
            mockTopicPosts[postIndex] = item.copy(liked = true, likeCount = item.likeCount + 1)
        }
        return success(request, "like_success")
    }

    fun mockTopicImage(request: Request): String {
        val postId = request.itemIdFromPath() ?: return failure(request, "topic_post_id_required")
        val index = request.pathValueAfter("index")?.toIntOrNull() ?: return failure(request, "image_index_required")
        val item = mockTopicPosts.findById(postId) ?: return failure(request, "topic_post_not_found")
        val image = item.imageUrls.getOrNull(index - 1) ?: return failure(request, "image_not_found")
        return MockUtils.successDataJson(image)
    }

    fun mockTopicPublish(request: Request): String {
        val locale = request.requestLocale()
        val topic = request.multipartField("topic").orEmpty().ifBlank { communityMessage(locale, "topic_default_topic") }
        val content = request.multipartField("content").orEmpty().ifBlank { communityMessage(locale, "topic_default_content") }
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
            publishTime = communityMessage(locale, "just_now"), likeCount = 0, liked = false,
            firstImageUrl = imageUrls.firstOrNull(),
            imageUrls = imageUrls
        ))
        return success(request, "published")
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
        val locale = request.requestLocale()
        return MockUtils.successDataJson(matched.drop(start).take(size).map { it.toTopicPayload(locale) })
    }

    // ── Delivery endpoints ──────────────────────────────────────────────────

    fun mockDeliveryOrders(request: Request): String {
        val locale = request.requestLocale()
        val start = request.pathValueAfter("start")?.toIntOrNull()?.coerceAtLeast(0) ?: 0
        val size = request.pathValueAfter("size")?.toIntOrNull()?.coerceIn(1, 50) ?: 20
        val items = mockDeliveryOrderRecords
            .sortedByDescending { it.orderTime }
            .drop(start)
            .take(size)
            .map { it.toDeliveryOrderPayload(locale) }
        return MockUtils.successDataJson(items)
    }

    fun mockDeliveryMine(request: Request): String {
        val locale = request.requestLocale()
        val published = mockDeliveryOrderRecords
            .filter { it.username == MockUtils.MOCK_CURRENT_USERNAME }
            .sortedByDescending { it.orderTime }
            .map { it.toDeliveryOrderPayload(locale) }
        val accepted = mockDeliveryTrades
            .filter { it.username == MockUtils.MOCK_CURRENT_USERNAME }
            .mapNotNull { trade ->
                mockDeliveryOrderRecords.firstOrNull { it.orderId == trade.orderId }
            }
            .sortedByDescending { it.orderTime }
            .map { it.toDeliveryOrderPayload(locale) }
        return MockUtils.successDataJson(
            linkedMapOf(
                "published" to published,
                "accepted" to accepted
            )
        )
    }

    fun mockDeliveryOrderDetail(request: Request): String {
        val locale = request.requestLocale()
        val orderId = request.itemIdFromPath() ?: return failure(request, "delivery_order_id_required")
        val order = mockDeliveryOrderRecords.findById(orderId) ?: return failure(request, "delivery_order_not_found")
        val trade = mockDeliveryTrades.firstOrNull { it.orderId == order.orderId }
        val detailType = when {
            order.username == MockUtils.MOCK_CURRENT_USERNAME -> 0
            trade?.username == MockUtils.MOCK_CURRENT_USERNAME -> 3
            else -> 1
        }
        return MockUtils.successDataJson(
            linkedMapOf(
                "order" to order.toDeliveryOrderPayload(locale),
                "detailType" to detailType,
                "trade" to trade?.toDeliveryTradePayload()
            )
        )
    }

    fun mockDeliveryAcceptOrder(request: Request): String {
        val orderId = request.url.queryParameter("orderId")
            ?: request.formFields()["orderId"]
            ?: return failure(request, "delivery_order_id_required")
        val orderIndex = mockDeliveryOrderRecords.indexOfFirst { it.orderId.toString() == orderId }
        if (orderIndex < 0) return failure(request, "delivery_order_not_found")
        val order = mockDeliveryOrderRecords[orderIndex]
        if (order.state != 0) return failure(request, "delivery_order_taken")
        val locale = request.requestLocale()
        mockDeliveryOrderRecords[orderIndex] = order.copy(state = 1)
        val exists = mockDeliveryTrades.any { it.orderId == order.orderId }
        if (!exists) {
            mockDeliveryTrades.add(
                0,
                MockDeliveryTradeRecord(
                    tradeId = (mockDeliveryTrades.maxOfOrNull { it.tradeId } ?: 12900) + 1,
                    orderId = order.orderId,
                    createTime = communityMessage(locale, "just_now"),
                    username = MockUtils.MOCK_CURRENT_USERNAME,
                    state = 0
                )
            )
        }
        return success(request, "delivery_accept_success")
    }

    fun mockDeliveryFinishTrade(request: Request): String {
        val tradeId = request.itemIdFromPath() ?: return failure(request, "delivery_trade_id_required")
        val tradeIndex = mockDeliveryTrades.indexOfFirst { it.tradeId.toString() == tradeId }
        if (tradeIndex < 0) return failure(request, "delivery_trade_not_found")
        val trade = mockDeliveryTrades[tradeIndex]
        mockDeliveryTrades[tradeIndex] = trade.copy(state = 1)
        val orderIndex = mockDeliveryOrderRecords.indexOfFirst { it.orderId == trade.orderId }
        if (orderIndex >= 0) {
            mockDeliveryOrderRecords[orderIndex] = mockDeliveryOrderRecords[orderIndex].copy(state = 2)
        }
        return success(request, "delivery_order_finished")
    }

    fun mockDeliveryDeleteOrder(request: Request): String {
        val orderId = request.itemIdFromPath() ?: return failure(request, "delivery_order_id_required")
        val removed = mockDeliveryOrderRecords.removeIf { it.orderId.toString() == orderId }
        if (!removed) return failure(request, "delivery_order_not_found")
        mockDeliveryTrades.removeIf { it.orderId.toString() == orderId }
        return success(request, "deleted")
    }

    fun mockDeliveryPublish(request: Request): String {
        val locale = request.requestLocale()
        val fields = request.formFields()
        val pickupPlace = fields["company"].orEmpty().ifBlank { communityMessage(locale, "delivery_default_company") }
        val order = MockDeliveryOrderRecord(
            orderId = (mockDeliveryOrderRecords.maxOfOrNull { it.orderId } ?: 12800) + 1,
            username = MockUtils.MOCK_CURRENT_USERNAME,
            orderTime = communityMessage(locale, "just_now"),
            name = fields["name"].orEmpty().ifBlank { communityMessage(locale, "delivery_default_name") },
            number = fields["number"].orEmpty().ifBlank { communityMessage(locale, "delivery_default_number") },
            phone = fields["phone"].orEmpty().ifBlank { "13800138000" },
            price = fields["price"]?.toDoubleOrNull() ?: 3.0,
            company = pickupPlace,
            address = fields["address"].orEmpty().ifBlank { communityMessage(locale, "delivery_default_address") },
            state = 0,
            remarks = fields["remarks"].orEmpty()
        )
        mockDeliveryOrderRecords.add(0, order)
        return success(request, "published")
    }

    // ── Photograph endpoints ────────────────────────────────────────────────

    fun mockPhotographPhotoCount(request: Request): String =
        MockUtils.successDataJson(mockPhotographPostRecords.sumOf { it.photoCount() })

    fun mockPhotographCommentCount(request: Request): String =
        MockUtils.successDataJson(mockPhotographCommentRecords.size)

    fun mockPhotographLikeCount(request: Request): String =
        MockUtils.successDataJson(mockPhotographPostRecords.sumOf { it.likeCount })

    fun mockPhotographPosts(request: Request): String {
        val locale = request.requestLocale()
        val type = request.pathValueAfter("type")?.toIntOrNull() ?: 0
        val start = request.pathValueAfter("start")?.toIntOrNull()?.coerceAtLeast(0) ?: 0
        val size = request.pathValueAfter("size")?.toIntOrNull()?.coerceIn(1, 50) ?: 20
        val items = mockPhotographPostRecords
            .filter { it.type == type }
            .sortedByDescending { it.createTime }
            .drop(start)
            .take(size)
            .map { it.toPhotographPayload(locale, includeComments = false) }
        return MockUtils.successDataJson(items)
    }

    fun mockPhotographMyPosts(request: Request): String {
        val locale = request.requestLocale()
        val start = request.pathValueAfter("start")?.toIntOrNull()?.coerceAtLeast(0) ?: 0
        val size = request.pathValueAfter("size")?.toIntOrNull()?.coerceIn(1, 50) ?: 20
        val items = mockPhotographPostRecords
            .filter { it.username == MockUtils.MOCK_CURRENT_USERNAME }
            .sortedByDescending { it.createTime }
            .drop(start)
            .take(size)
            .map { it.toPhotographPayload(locale, includeComments = false) }
        return MockUtils.successDataJson(items)
    }

    fun mockPhotographDetail(request: Request): String {
        val locale = request.requestLocale()
        val postId = request.itemIdFromPath() ?: return failure(request, "photograph_post_id_required")
        val item = mockPhotographPostRecords.findById(postId) ?: return failure(request, "photograph_post_not_found")
        return MockUtils.successDataJson(item.toPhotographPayload(locale, includeComments = true))
    }

    fun mockPhotographImage(request: Request): String {
        val postId = request.itemIdFromPath() ?: return failure(request, "photograph_post_id_required")
        val index = request.pathValueAfter("index")?.toIntOrNull() ?: return failure(request, "image_index_required")
        val item = mockPhotographPostRecords.findById(postId) ?: return failure(request, "photograph_post_not_found")
        val image = item.resolvedImageUrls().getOrNull(index - 1) ?: return failure(request, "image_not_found")
        return MockUtils.successDataJson(image)
    }

    fun mockPhotographComments(request: Request): String {
        val locale = request.requestLocale()
        val postId = request.itemIdFromPath() ?: return failure(request, "photograph_post_id_required")
        val items = mockPhotographCommentRecords
            .filter { it.photoId.toString() == postId }
            .sortedByDescending { it.createTime }
            .map { it.toPhotographCommentPayload(locale) }
        return MockUtils.successDataJson(items)
    }

    fun mockPhotographCommentSubmit(request: Request): String {
        val locale = request.requestLocale()
        val postId = request.itemIdFromPath() ?: return failure(request, "photograph_post_id_required")
        val comment = request.url.queryParameter("comment")
            ?: request.formFields()["comment"]
            ?: return failure(request, "comment_required")
        val trimmed = comment.trim()
        if (trimmed.isBlank()) return failure(request, "comment_required")
        val postIndex = mockPhotographPostRecords.indexOfFirst { it.id.toString() == postId }
        if (postIndex < 0) return failure(request, "photograph_post_not_found")
        mockPhotographCommentRecords.add(
            0,
            MockPhotographCommentRecord(
                commentId = (mockPhotographCommentRecords.maxOfOrNull { it.commentId } ?: 14200) + 1,
                photoId = postId.toInt(),
                username = MockUtils.MOCK_CURRENT_USERNAME,
                nickname = MockProfileProvider.currentMockNickname(locale),
                comment = trimmed,
                createTime = communityMessage(locale, "just_now")
            )
        )
        val post = mockPhotographPostRecords[postIndex]
        mockPhotographPostRecords[postIndex] = post.copy(commentCount = post.commentCount + 1)
        return success(request, "comment_sent")
    }

    fun mockPhotographLike(request: Request): String {
        val postId = request.itemIdFromPath() ?: return failure(request, "photograph_post_id_required")
        val postIndex = mockPhotographPostRecords.indexOfFirst { it.id.toString() == postId }
        if (postIndex < 0) return failure(request, "photograph_post_not_found")
        val post = mockPhotographPostRecords[postIndex]
        if (!post.liked) {
            mockPhotographPostRecords[postIndex] = post.copy(liked = true, likeCount = post.likeCount + 1)
        }
        return success(request, "like_success")
    }

    fun mockPhotographPublish(request: Request): String {
        val locale = request.requestLocale()
        val title = request.multipartField("title").orEmpty().ifBlank { communityMessage(locale, "photograph_default_title") }
        val content = request.multipartField("content").orEmpty().ifBlank { communityMessage(locale, "photograph_default_content") }
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
                createTime = communityMessage(locale, "just_now"),
                likeCount = 0,
                commentCount = 0,
                liked = false,
                firstImageUrl = imageUrls.firstOrNull(),
                imageUrls = imageUrls
            )
        )
        return success(request, "published")
    }
}
