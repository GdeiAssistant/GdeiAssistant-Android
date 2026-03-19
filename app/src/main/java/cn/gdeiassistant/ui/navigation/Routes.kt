package cn.gdeiassistant.ui.navigation

import android.net.Uri

object Routes {
    const val HOME = "home"
    const val MESSAGES = "messages"
    const val PROFILE = "profile"
    const val DISCOVERY = "discovery"
    const val GRADE = "grade"
    const val SCHEDULE = "schedule"
    const val CET = "cet"
    const val EVALUATE = "evaluate"
    const val BOOK = "book"
    const val CARD = "card"
    const val CHARGE = "charge"
    const val LOST = "lost"
    const val ABOUT = "about"
    const val PROFILE_PRIVACY = "profile_privacy"
    const val PROFILE_LOGIN_RECORDS = "profile_login_records"
    const val PROFILE_BIND_PHONE = "profile_bind_phone"
    const val PROFILE_BIND_EMAIL = "profile_bind_email"
    const val PROFILE_AVATAR = "profile_avatar"
    const val PROFILE_DOWNLOAD_DATA = "profile_download_data"
    const val PROFILE_FEEDBACK = "profile_feedback"
    const val PROFILE_DELETE_ACCOUNT = "profile_delete_account"
    const val PROFILE_SETTINGS = "profile_settings"
    const val PROFILE_THEME = "profile_theme"
    const val NEWS = "news"
    const val SPARE = "spare"
    const val GRADUATE_EXAM = "graduate_exam"
    const val DATA_CENTER = "data_center"
    const val ELECTRICITY_FEES = "electricity_fees"
    const val YELLOW_PAGE = "yellow_page"
    const val MARKETPLACE = "marketplace"
    const val MARKETPLACE_PROFILE = "marketplace_profile"
    const val MARKETPLACE_PUBLISH = "marketplace_publish"
    const val MARKETPLACE_EDIT_ITEM_ID = "marketplaceEditItemId"
    const val MARKETPLACE_EDIT = "marketplace_edit/{$MARKETPLACE_EDIT_ITEM_ID}"
    const val LOST_FOUND = "lost_found"
    const val LOST_FOUND_PROFILE = "lost_found_profile"
    const val LOST_FOUND_PUBLISH = "lost_found_publish"
    const val LOST_FOUND_EDIT_ITEM_ID = "lostFoundEditItemId"
    const val LOST_FOUND_EDIT = "lost_found_edit/{$LOST_FOUND_EDIT_ITEM_ID}"
    const val SECRET = "secret"
    const val SECRET_PROFILE = "secret_profile"
    const val SECRET_PUBLISH = "secret_publish"
    const val DATING = "dating"
    const val DATING_TAB = "datingTab"
    const val DATING_ROUTE = "$DATING?$DATING_TAB={$DATING_TAB}"
    const val EXPRESS = "express"
    const val EXPRESS_PROFILE = "express_profile"
    const val EXPRESS_PUBLISH = "express_publish"
    const val TOPIC = "topic"
    const val TOPIC_PROFILE = "topic_profile"
    const val TOPIC_PUBLISH = "topic_publish"
    const val DELIVERY = "delivery"
    const val DELIVERY_MINE = "delivery_mine"
    const val DELIVERY_PUBLISH = "delivery_publish"
    const val PHOTOGRAPH = "photograph"
    const val PHOTOGRAPH_PROFILE = "photograph_profile"
    const val PHOTOGRAPH_PUBLISH = "photograph_publish"

    const val GRADE_DETAIL = "grade_detail"
    const val BOOK_DETAIL = "book_detail"
    const val BOOK_COLLECTION_DETAIL_BASE = "book_collection_detail"
    const val BOOK_COLLECTION_DETAIL_URL = "detailUrl"
    const val BOOK_COLLECTION_DETAIL = "$BOOK_COLLECTION_DETAIL_BASE?detailUrl={$BOOK_COLLECTION_DETAIL_URL}"
    const val NEWS_DETAIL_ID = "newsId"
    const val NEWS_DETAIL = "news_detail/{$NEWS_DETAIL_ID}"
    const val YELLOW_PAGE_DETAIL = "yellow_page_detail"
    const val MARKETPLACE_ITEM_ID = "marketplaceItemId"
    const val LOST_FOUND_ITEM_ID = "lostFoundItemId"
    const val SECRET_POST_ID = "secretPostId"
    const val EXPRESS_POST_ID = "expressPostId"
    const val TOPIC_POST_ID = "topicPostId"
    const val DELIVERY_ORDER_ID = "deliveryOrderId"
    const val PHOTOGRAPH_POST_ID = "photographPostId"
    const val MARKETPLACE_DETAIL = "marketplace_detail/{$MARKETPLACE_ITEM_ID}"
    const val LOST_FOUND_DETAIL = "lost_found_detail/{$LOST_FOUND_ITEM_ID}"
    const val SECRET_DETAIL = "secret_detail/{$SECRET_POST_ID}"
    const val EXPRESS_DETAIL = "express_detail/{$EXPRESS_POST_ID}"
    const val TOPIC_DETAIL = "topic_detail/{$TOPIC_POST_ID}"
    const val DELIVERY_DETAIL = "delivery_detail/{$DELIVERY_ORDER_ID}"
    const val PHOTOGRAPH_DETAIL = "photograph_detail/{$PHOTOGRAPH_POST_ID}"
    const val GRADE_DETAIL_GRADE = "grade_detail_grade"
    const val GRADE_DETAIL_TERM_LABEL = "grade_detail_term_label"
    const val BOOK_DETAIL_BOOK = "book_detail_book"
    const val BOOK_REFRESH_FLAG = "book_refresh_flag"
    const val MARKETPLACE_REFRESH_FLAG = "marketplace_refresh_flag"
    const val MARKETPLACE_PROFILE_REFRESH_FLAG = "marketplace_profile_refresh_flag"
    const val LOST_FOUND_REFRESH_FLAG = "lost_found_refresh_flag"
    const val LOST_FOUND_PROFILE_REFRESH_FLAG = "lost_found_profile_refresh_flag"
    const val TOPIC_REFRESH_FLAG = "topic_refresh_flag"
    const val EXPRESS_REFRESH_FLAG = "express_refresh_flag"
    const val SECRET_REFRESH_FLAG = "secret_refresh_flag"
    const val PROFILE_AVATAR_REFRESH_FLAG = "profile_avatar_refresh_flag"
    const val YELLOW_PAGE_ENTRY = "yellow_page_entry"

    const val WEB_VIEW_BASE = "webview"
    const val WEB_VIEW = "$WEB_VIEW_BASE?title={title}&url={url}&allowJavaScript={allowJavaScript}"
    const val WEB_VIEW_TITLE = "title"
    const val WEB_VIEW_URL = "url"
    const val WEB_VIEW_ALLOW_JAVASCRIPT = "allowJavaScript"
    const val LOGIN = "login"
    const val NOTICE_DETAIL = "notice_detail/{noticeId}"
    const val NOTICE_LIST = "notice_list"
    const val INTERACTION_LIST = "interaction_list"

    fun webView(
        title: String = "",
        url: String = "about:blank",
        allowJavaScript: Boolean = false
    ): String {
        return "$WEB_VIEW_BASE?title=${Uri.encode(title)}&url=${Uri.encode(url)}&allowJavaScript=$allowJavaScript"
    }

    fun marketplaceDetail(itemId: String): String {
        return "marketplace_detail/${Uri.encode(itemId)}"
    }

    fun marketplaceEdit(itemId: String): String {
        return "marketplace_edit/${Uri.encode(itemId)}"
    }

    fun bookCollectionDetail(detailUrl: String): String {
        return "$BOOK_COLLECTION_DETAIL_BASE?detailUrl=${Uri.encode(detailUrl)}"
    }

    fun newsDetail(newsId: String): String {
        return "news_detail/${Uri.encode(newsId)}"
    }

    fun lostFoundDetail(itemId: String): String {
        return "lost_found_detail/${Uri.encode(itemId)}"
    }

    fun lostFoundEdit(itemId: String): String {
        return "lost_found_edit/${Uri.encode(itemId)}"
    }

    fun secretDetail(postId: String): String {
        return "secret_detail/${Uri.encode(postId)}"
    }

    fun dating(tab: String? = null): String {
        return if (tab.isNullOrBlank()) {
            DATING
        } else {
            "$DATING?$DATING_TAB=${Uri.encode(tab)}"
        }
    }

    fun expressDetail(postId: String): String {
        return "express_detail/${Uri.encode(postId)}"
    }

    fun topicDetail(postId: String): String {
        return "topic_detail/${Uri.encode(postId)}"
    }

    fun deliveryDetail(orderId: String): String {
        return "delivery_detail/${Uri.encode(orderId)}"
    }

    fun photographDetail(postId: String): String {
        return "photograph_detail/${Uri.encode(postId)}"
    }

    fun noticeDetail(noticeId: String): String {
        return "notice_detail/${Uri.encode(noticeId)}"
    }
}
