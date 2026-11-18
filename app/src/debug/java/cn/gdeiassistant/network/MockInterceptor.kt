package cn.gdeiassistant.network

import cn.gdeiassistant.data.SettingsRepository
import cn.gdeiassistant.network.mock.MockAcademicProvider
import cn.gdeiassistant.network.mock.MockAuthProvider
import cn.gdeiassistant.network.mock.MockCampusCredentialProvider
import cn.gdeiassistant.network.mock.MockCampusProvider
import cn.gdeiassistant.network.mock.MockCommunityProvider
import cn.gdeiassistant.network.mock.MockInfoProvider
import cn.gdeiassistant.network.mock.MockProfileProvider
import cn.gdeiassistant.network.mock.MockSocialProvider
import cn.gdeiassistant.network.mock.MockSocialRouteResult
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

/** 全场景 Mock 拦截器，仅负责按精确 (method, normalizedPath) 将请求路由到对应的 mock provider。 */
class MockInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        if (!SettingsRepository.isMockModeEnabledSync()) {
            return chain.proceed(chain.request())
        }

        val request = chain.request()
        MockSocialProvider.route(request)?.let { return mockSocial(request, it) }

        val method = request.method.uppercase()
        val normalizedPath = normalizeApiPath(request.url.encodedPath)
        val handler = exactRoutes[method to normalizedPath]
            ?: patternRoutes.firstOrNull { it.method == method && it.pattern.matches(normalizedPath) }?.handler
            ?: return chain.proceed(request)

        return mockOk(request, handler(request))
    }

    private fun mockOk(request: Request, json: String): Response {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(json.toResponseBody(mediaType))
            .build()
    }

    /** 社交 mock：HTTP 状态与 payload.code / success 对齐，并保留 errorCode。 */
    private fun mockSocial(request: Request, result: MockSocialRouteResult): Response {
        return result.toResponse(request)
    }

    private data class PatternRoute(
        val method: String,
        val pattern: Regex,
        val handler: (Request) -> String
    )

    internal companion object {
        internal fun normalizeApiPath(encodedPath: String): String {
            val trimmed = encodedPath.trimEnd('/')
            if (trimmed.isEmpty()) return "/"
            val apiIndex = trimmed.indexOf("/api/")
            return when {
                apiIndex >= 0 -> trimmed.substring(apiIndex)
                trimmed.endsWith("/api") -> "/api"
                trimmed.startsWith('/') -> trimmed
                else -> "/$trimmed"
            }
        }

        private val exactRoutes: Map<Pair<String, String>, (Request) -> String> = mapOf(
            // Auth
            ("POST" to "/api/auth/login") to MockAuthProvider::mockLogin,

            // Campus credential
            ("GET" to "/api/campus-credential/status") to MockCampusCredentialProvider::mockStatus,
            ("POST" to "/api/campus-credential/consent") to MockCampusCredentialProvider::mockConsent,
            ("POST" to "/api/campus-credential/revoke") to MockCampusCredentialProvider::mockRevoke,
            ("POST" to "/api/campus-credential/quick-auth") to MockCampusCredentialProvider::mockQuickAuth,
            ("DELETE" to "/api/campus-credential") to MockCampusCredentialProvider::mockDelete,

            // Profile & account center
            ("GET" to "/api/user/profile") to MockProfileProvider::mockUserProfile,
            ("PATCH" to "/api/profile") to MockProfileProvider::mockPatchProfile,
            ("GET" to "/api/profile/locations") to MockProfileProvider::mockLocationList,
            ("GET" to "/api/profile/options") to MockProfileProvider::mockProfileOptions,
            ("GET" to "/api/profile/avatar") to MockProfileProvider::mockAvatarState,
            ("POST" to "/api/profile/avatar") to MockProfileProvider::mockUploadAvatar,
            ("DELETE" to "/api/profile/avatar") to MockProfileProvider::mockDeleteAvatar,
            ("POST" to "/api/profile/nickname") to MockProfileProvider::mockUpdateNickname,
            ("POST" to "/api/profile/birthday") to MockProfileProvider::mockUpdateBirthday,
            ("POST" to "/api/profile/faculty") to MockProfileProvider::mockUpdateFaculty,
            ("POST" to "/api/profile/major") to MockProfileProvider::mockUpdateMajor,
            ("POST" to "/api/profile/enrollment") to MockProfileProvider::mockUpdateEnrollment,
            ("POST" to "/api/profile/location") to MockProfileProvider::mockUpdateLocation,
            ("POST" to "/api/profile/hometown") to MockProfileProvider::mockUpdateHometown,
            ("POST" to "/api/introduction") to MockProfileProvider::mockUpdateIntroduction,
            ("GET" to "/api/privacy") to MockProfileProvider::mockGetPrivacySettings,
            ("POST" to "/api/privacy") to MockProfileProvider::mockUpdatePrivacySettings,
            ("GET" to "/api/phone/attribution") to MockProfileProvider::mockPhoneAttributions,
            ("POST" to "/api/phone/verification") to MockProfileProvider::mockSendPhoneVerification,
            ("POST" to "/api/phone/attach") to MockProfileProvider::mockBindPhone,
            ("POST" to "/api/phone/unattach") to MockProfileProvider::mockUnbindPhone,
            ("POST" to "/api/email/verification") to MockProfileProvider::mockSendEmailVerification,
            ("POST" to "/api/email/bind") to MockProfileProvider::mockBindEmail,
            ("POST" to "/api/email/unbind") to MockProfileProvider::mockUnbindEmail,
            ("POST" to "/api/feedback") to MockProfileProvider::mockSubmitFeedback,
            ("POST" to "/api/close/submit") to MockProfileProvider::mockDeleteAccount,
            ("GET" to "/api/phone/status") to MockProfileProvider::mockPhoneStatus,
            ("GET" to "/api/email/status") to MockProfileProvider::mockEmailStatus,
            ("GET" to "/api/userdata/state") to MockProfileProvider::mockUserDataExportStateValue,
            ("POST" to "/api/userdata/export") to MockProfileProvider::mockUserDataExport,
            ("POST" to "/api/userdata/download") to MockProfileProvider::mockUserDataDownload,

            // Academic
            ("POST" to "/api/spare/query") to MockAcademicProvider::mockSpareRoomList,
            ("POST" to "/api/graduate-exam/query") to MockAcademicProvider::mockGraduateExam,
            ("GET" to "/api/grade") to MockAcademicProvider::mockGrade,
            ("GET" to "/api/schedule") to MockAcademicProvider::mockSchedule,
            ("GET" to "/api/cet/checkcode") to MockAcademicProvider::mockCetCheckCode,
            ("GET" to "/api/cet/query") to MockAcademicProvider::mockCetQuery,
            ("POST" to "/api/evaluate/submit") to MockAcademicProvider::mockEvaluateSubmit,

            // Campus
            ("POST" to "/api/data/electricfees") to MockCampusProvider::mockElectricityFees,
            ("GET" to "/api/data/yellowpage") to MockCampusProvider::mockYellowPages,
            ("GET" to "/api/card/info") to MockCampusProvider::mockCardInfo,
            ("POST" to "/api/card/query") to MockCampusProvider::mockCardQuery,
            ("POST" to "/api/card/lost") to MockCampusProvider::mockCardLost,
            ("GET" to "/api/library/search") to MockCampusProvider::mockCollectionSearch,
            ("GET" to "/api/library/detail") to MockCampusProvider::mockCollectionDetail,
            ("POST" to "/api/library/borrow") to MockCampusProvider::mockCollectionBorrow,
            ("POST" to "/api/library/renew") to MockCampusProvider::mockCollectionRenew,
            ("GET" to "/api/card/charge/orders") to MockCampusProvider::mockChargeOrders,
            ("POST" to "/api/card/charge") to MockCampusProvider::mockCharge,

            // Community exact routes
            ("POST" to "/api/marketplace/item") to MockCommunityProvider::mockMarketplacePublish,
            ("GET" to "/api/marketplace/profile") to MockCommunityProvider::mockMarketplaceProfile,
            ("POST" to "/api/lostandfound/item") to MockCommunityProvider::mockLostFoundPublish,
            ("GET" to "/api/lostandfound/profile") to MockCommunityProvider::mockLostFoundProfile,
            ("POST" to "/api/secret/info") to MockCommunityProvider::mockSecretPublish,
            ("GET" to "/api/dating/pick/my/received") to MockCommunityProvider::mockDatingReceivedPickList,
            ("GET" to "/api/dating/pick/my/sent") to MockCommunityProvider::mockDatingSentPickList,
            ("POST" to "/api/dating/pick") to MockCommunityProvider::mockDatingPickSubmit,
            ("GET" to "/api/dating/profile/my") to MockCommunityProvider::mockDatingMyPosts,
            ("POST" to "/api/dating/profile") to MockCommunityProvider::mockDatingPublish,
            ("POST" to "/api/express") to MockCommunityProvider::mockExpressPublish,
            ("POST" to "/api/topic") to MockCommunityProvider::mockTopicPublish,
            ("POST" to "/api/delivery/acceptorder") to MockCommunityProvider::mockDeliveryAcceptOrder,
            ("GET" to "/api/delivery/mine") to MockCommunityProvider::mockDeliveryMine,
            ("POST" to "/api/delivery/order") to MockCommunityProvider::mockDeliveryPublish,
            ("GET" to "/api/photograph/statistics/photos") to MockCommunityProvider::mockPhotographPhotoCount,
            ("GET" to "/api/photograph/statistics/comments") to MockCommunityProvider::mockPhotographCommentCount,
            ("GET" to "/api/photograph/statistics/likes") to MockCommunityProvider::mockPhotographLikeCount,
            ("POST" to "/api/photograph") to MockCommunityProvider::mockPhotographPublish,

            // Info exact routes
            ("GET" to "/api/information/overview") to MockInfoProvider::mockInformationOverview,
            ("POST" to "/api/information/message/readall") to MockInfoProvider::mockMessageReadAll,
            ("GET" to "/api/information/message/unread") to MockInfoProvider::mockMessageUnread
        )

        private val patternRoutes: List<PatternRoute> = listOf(
            // Profile
            PatternRoute("GET", Regex("^/api/ip/start/[^/]+/size/[^/]+$"), MockProfileProvider::mockLoginRecords),

            // Campus
            PatternRoute("GET", Regex("^/api/card/charge/orders/[^/]+$"), MockCampusProvider::mockChargeOrderDetail),

            // Marketplace
            PatternRoute("POST", Regex("^/api/marketplace/item/state/\\d+$"), MockCommunityProvider::mockMarketplaceStateUpdate),
            PatternRoute("POST", Regex("^/api/marketplace/item/\\d+$"), MockCommunityProvider::mockMarketplaceUpdate),
            PatternRoute("GET", Regex("^/api/marketplace/item/\\d+/preview$"), MockCommunityProvider::mockMarketplacePreview),
            PatternRoute("GET", Regex("^/api/marketplace/item/\\d+$"), MockCommunityProvider::mockMarketplaceDetail),
            PatternRoute("GET", Regex("^/api/marketplace/keyword/[^/]+/start/[^/]+$"), MockCommunityProvider::mockMarketplaceSearch),
            PatternRoute("GET", Regex("^/api/marketplace/item/type/[^/]+/start/[^/]+$"), MockCommunityProvider::mockMarketplaceItemsByType),
            PatternRoute("GET", Regex("^/api/marketplace/item/start/[^/]+$"), MockCommunityProvider::mockMarketplaceItemList),

            // Lost & Found
            PatternRoute("POST", Regex("^/api/lostandfound/item/\\d+/didfound$"), MockCommunityProvider::mockLostFoundDidFound),
            PatternRoute("POST", Regex("^/api/lostandfound/item/\\d+$"), MockCommunityProvider::mockLostFoundUpdate),
            PatternRoute("GET", Regex("^/api/lostandfound/item/\\d+/preview$"), MockCommunityProvider::mockLostFoundPreview),
            PatternRoute("GET", Regex("^/api/lostandfound/item/\\d+$"), MockCommunityProvider::mockLostFoundDetail),
            PatternRoute("GET", Regex("^/api/lostandfound/lostitem/start/[^/]+$"), MockCommunityProvider::mockLostFoundItemList),
            PatternRoute("GET", Regex("^/api/lostandfound/founditem/start/[^/]+$"), MockCommunityProvider::mockLostFoundItemList),

            // Secret
            PatternRoute("POST", Regex("^/api/secret/\\d+/comment$"), MockCommunityProvider::mockSecretCommentSubmit),
            PatternRoute("POST", Regex("^/api/secret/\\d+/like$"), MockCommunityProvider::mockSecretLikeUpdate),
            PatternRoute("GET", Regex("^/api/secret/\\d+/comments$"), MockCommunityProvider::mockSecretCommentList),
            PatternRoute("GET", Regex("^/api/secret/\\d+$"), MockCommunityProvider::mockSecretDetail),
            PatternRoute("GET", Regex("^/api/secret/info/start/[^/]+/size/[^/]+$"), MockCommunityProvider::mockSecretPostList),
            PatternRoute("GET", Regex("^/api/secret/profile/start/[^/]+/size/[^/]+$"), MockCommunityProvider::mockSecretMyPosts),

            // Dating
            PatternRoute("POST", Regex("^/api/dating/pick/\\d+$"), MockCommunityProvider::mockDatingPickStateUpdate),
            PatternRoute("POST", Regex("^/api/dating/profile/\\d+/state$"), MockCommunityProvider::mockDatingProfileHide),
            PatternRoute("GET", Regex("^/api/dating/profile/\\d+$"), MockCommunityProvider::mockDatingDetail),
            PatternRoute("GET", Regex("^/api/dating/profile/area/[^/]+/start/[^/]+$"), MockCommunityProvider::mockDatingProfileList),

            // Express
            PatternRoute("GET", Regex("^/api/express/\\d+/comment$"), MockCommunityProvider::mockExpressCommentList),
            PatternRoute("POST", Regex("^/api/express/\\d+/comment$"), MockCommunityProvider::mockExpressCommentSubmit),
            PatternRoute("POST", Regex("^/api/express/\\d+/like$"), MockCommunityProvider::mockExpressLike),
            PatternRoute("POST", Regex("^/api/express/\\d+/guess$"), MockCommunityProvider::mockExpressGuess),
            PatternRoute("GET", Regex("^/api/express/\\d+$"), MockCommunityProvider::mockExpressDetail),
            PatternRoute("GET", Regex("^/api/express/keyword/[^/]+/start/[^/]+/size/[^/]+$"), MockCommunityProvider::mockExpressSearch),
            PatternRoute("GET", Regex("^/api/express/profile/start/[^/]+/size/[^/]+$"), MockCommunityProvider::mockExpressMyPosts),
            PatternRoute("GET", Regex("^/api/express/start/[^/]+/size/[^/]+$"), MockCommunityProvider::mockExpressPostList),

            // Topic
            PatternRoute("GET", Regex("^/api/topic/\\d+/index/[^/]+/image$"), MockCommunityProvider::mockTopicImage),
            PatternRoute("POST", Regex("^/api/topic/\\d+/like$"), MockCommunityProvider::mockTopicLike),
            PatternRoute("GET", Regex("^/api/topic/\\d+$"), MockCommunityProvider::mockTopicDetail),
            PatternRoute("GET", Regex("^/api/topic/keyword/[^/]+/start/[^/]+/size/[^/]+$"), MockCommunityProvider::mockTopicSearch),
            PatternRoute("GET", Regex("^/api/topic/profile/start/[^/]+/size/[^/]+$"), MockCommunityProvider::mockTopicMyPosts),
            PatternRoute("GET", Regex("^/api/topic/start/[^/]+/size/[^/]+$"), MockCommunityProvider::mockTopicPostList),

            // Delivery
            PatternRoute("POST", Regex("^/api/delivery/trade/\\d+/finishtrade$"), MockCommunityProvider::mockDeliveryFinishTrade),
            PatternRoute("DELETE", Regex("^/api/delivery/order/\\d+$"), MockCommunityProvider::mockDeliveryDeleteOrder),
            PatternRoute("GET", Regex("^/api/delivery/order/\\d+$"), MockCommunityProvider::mockDeliveryOrderDetail),
            PatternRoute("GET", Regex("^/api/delivery/order/start/[^/]+/size/[^/]+$"), MockCommunityProvider::mockDeliveryOrders),

            // Photograph
            PatternRoute("GET", Regex("^/api/photograph/\\d+/comment$"), MockCommunityProvider::mockPhotographComments),
            PatternRoute("POST", Regex("^/api/photograph/\\d+/comment$"), MockCommunityProvider::mockPhotographCommentSubmit),
            PatternRoute("POST", Regex("^/api/photograph/\\d+/like$"), MockCommunityProvider::mockPhotographLike),
            PatternRoute("GET", Regex("^/api/photograph/\\d+/index/[^/]+/image$"), MockCommunityProvider::mockPhotographImage),
            PatternRoute("GET", Regex("^/api/photograph/\\d+$"), MockCommunityProvider::mockPhotographDetail),
            PatternRoute("GET", Regex("^/api/photograph/profile/start/[^/]+/size/[^/]+$"), MockCommunityProvider::mockPhotographMyPosts),
            PatternRoute("GET", Regex("^/api/photograph/type/[^/]+/start/[^/]+/size/[^/]+$"), MockCommunityProvider::mockPhotographPosts),

            // Info
            PatternRoute("GET", Regex("^/api/information/announcement/start/[^/]+/size/[^/]+$"), MockInfoProvider::mockAnnouncementPage),
            PatternRoute("GET", Regex("^/api/information/announcement/[^/]+$"), MockInfoProvider::mockAnnouncementDetail),
            PatternRoute("POST", Regex("^/api/information/message/[^/]+/read$"), MockInfoProvider::mockMessageRead),
            PatternRoute("GET", Regex("^/api/information/message/interaction/start/[^/]+/size/[^/]+$"), MockInfoProvider::mockInteractionMessages),
            PatternRoute("GET", Regex("^/api/information/news/type/[^/]+/start/[^/]+/size/[^/]+$"), MockInfoProvider::mockNews),
            PatternRoute("GET", Regex("^/api/information/news/[^/]+$"), MockInfoProvider::mockNewsDetail)
        )
    }
}
