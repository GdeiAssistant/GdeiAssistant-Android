package cn.gdeiassistant.network

import cn.gdeiassistant.data.SettingsRepository
import cn.gdeiassistant.network.mock.MockAcademicProvider
import cn.gdeiassistant.network.mock.MockAuthProvider
import cn.gdeiassistant.network.mock.MockCampusProvider
import cn.gdeiassistant.network.mock.MockCommunityProvider
import cn.gdeiassistant.network.mock.MockInfoProvider
import cn.gdeiassistant.network.mock.MockProfileProvider
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

/** 全场景 Mock 拦截器，仅负责根据请求路由到对应的 mock provider。 */
class MockInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        if (!SettingsRepository.isMockModeEnabledSync()) {
            return chain.proceed(chain.request())
        }

        val request = chain.request()
        val json = routeAuth(request)
            ?: routeProfile(request)
            ?: routeAcademic(request)
            ?: routeCampus(request)
            ?: routeCommunity(request)
            ?: routeInfo(request)
            ?: return chain.proceed(request)

        return mockOk(request, json)
    }

    private fun routeAuth(request: Request): String? {
        val path = request.url.encodedPath
        return when {
            path.contains("api/auth/login") -> MockAuthProvider.mockLogin(request)
            path.contains("api/update/android") -> MockAuthProvider.mockUpgrade(request)
            else -> null
        }
    }

    private fun routeProfile(request: Request): String? {
        val path = request.url.encodedPath
        return when {
            path.contains("api/user/profile") -> MockProfileProvider.mockUserProfile(request)
            path.contains("api/locationList") -> MockProfileProvider.mockLocationList(request)
            path.contains("api/profile/avatar") && request.method == "GET" -> MockProfileProvider.mockAvatarState(request)
            path.contains("api/profile/avatar") && request.method == "POST" -> MockProfileProvider.mockUploadAvatar(request)
            path.contains("api/profile/avatar") && request.method == "DELETE" -> MockProfileProvider.mockDeleteAvatar(request)
            path.contains("api/profile/nickname") -> MockProfileProvider.mockUpdateNickname(request)
            path.contains("api/profile/birthday") -> MockProfileProvider.mockUpdateBirthday(request)
            path.contains("api/profile/faculty") -> MockProfileProvider.mockUpdateFaculty(request)
            path.contains("api/profile/major") -> MockProfileProvider.mockUpdateMajor(request)
            path.contains("api/profile/enrollment") -> MockProfileProvider.mockUpdateEnrollment(request)
            path.contains("api/profile/location") -> MockProfileProvider.mockUpdateLocation(request)
            path.contains("api/profile/hometown") -> MockProfileProvider.mockUpdateHometown(request)
            path.contains("api/introduction") -> MockProfileProvider.mockUpdateIntroduction(request)
            path.contains("api/privacy") && request.method == "GET" -> MockProfileProvider.mockGetPrivacySettings(request)
            path.contains("api/privacy") && request.method == "POST" -> MockProfileProvider.mockUpdatePrivacySettings(request)
            path.contains("api/ip/start/") -> MockProfileProvider.mockLoginRecords(request)
            path.contains("api/phone/attribution") -> MockProfileProvider.mockPhoneAttributions(request)
            path.contains("api/phone/verification") -> MockProfileProvider.mockSendPhoneVerification(request)
            path.contains("api/phone/unattach") -> MockProfileProvider.mockUnbindPhone(request)
            path.contains("api/phone/attach") -> MockProfileProvider.mockBindPhone(request)
            path.contains("api/email/verification") -> MockProfileProvider.mockSendEmailVerification(request)
            path.contains("api/email/unbind") -> MockProfileProvider.mockUnbindEmail(request)
            path.contains("api/email/bind") -> MockProfileProvider.mockBindEmail(request)
            path.contains("api/feedback") -> MockProfileProvider.mockSubmitFeedback(request)
            path.contains("api/close/submit") -> MockProfileProvider.mockDeleteAccount(request)
            path.contains("api/phone/status") -> MockProfileProvider.mockPhoneStatus(request)
            path.contains("api/email/status") -> MockProfileProvider.mockEmailStatus(request)
            path.contains("api/userdata/state") -> MockProfileProvider.mockUserDataExportStateValue(request)
            path.contains("api/userdata/export") -> MockProfileProvider.mockUserDataExport(request)
            path.contains("api/userdata/download") -> MockProfileProvider.mockUserDataDownload(request)
            else -> null
        }
    }

    private fun routeAcademic(request: Request): String? {
        val path = request.url.encodedPath
        return when {
            path.contains("api/spare/query") -> MockAcademicProvider.mockSpareRoomList(request)
            path.contains("api/kaoyan/query") -> MockAcademicProvider.mockGraduateExam(request)
            path.contains("api/gradequery") || path.contains("api/grade/query") || path.contains("api/grade") ->
                MockAcademicProvider.mockGrade(request)
            path.contains("api/schedulequery") || path.contains("api/schedule/query") || path.contains("api/schedule") ->
                MockAcademicProvider.mockSchedule(request)
            path.contains("api/cet/checkcode") -> MockAcademicProvider.mockCetCheckCode(request)
            path.contains("api/cet/query") -> MockAcademicProvider.mockCetQuery(request)
            path.contains("api/evaluate/submit") -> MockAcademicProvider.mockEvaluateSubmit(request)
            else -> null
        }
    }

    private fun routeCampus(request: Request): String? {
        val path = request.url.encodedPath
        return when {
            path.contains("api/data/electricfees") -> MockCampusProvider.mockElectricityFees(request)
            path.contains("api/data/yellowpage") -> MockCampusProvider.mockYellowPages(request)
            path.contains("api/card/info") -> MockCampusProvider.mockCardInfo(request)
            path.contains("api/card/query") -> MockCampusProvider.mockCardQuery(request)
            path.contains("api/card/lost") -> MockCampusProvider.mockCardLost(request)
            path.contains("api/bookquery") -> MockCampusProvider.mockBookQuery(request)
            path.contains("api/bookrenew") -> MockCampusProvider.mockBookRenew(request)
            path.contains("api/collection/search") -> MockCampusProvider.mockCollectionSearch(request)
            path.contains("api/collection/detail") -> MockCampusProvider.mockCollectionDetail(request)
            path.contains("api/collection/borrow") -> MockCampusProvider.mockCollectionBorrow(request)
            path.contains("api/collection/renew") -> MockCampusProvider.mockCollectionRenew(request)
            path.contains("api/encryption/rsa/publickey") -> MockCampusProvider.mockServerPublicKey(request)
            path.contains("api/card/charge") -> MockCampusProvider.mockCharge(request)
            else -> null
        }
    }

    private fun routeCommunity(request: Request): String? {
        val path = request.url.encodedPath
        return when {
            path.endsWith("/api/ershou/item") && request.method == "POST" -> MockCommunityProvider.mockMarketplacePublish(request)
            path.contains("api/ershou/item/state/id/") -> MockCommunityProvider.mockMarketplaceStateUpdate(request)
            path.contains("api/ershou/item/id/") && request.method == "POST" -> MockCommunityProvider.mockMarketplaceUpdate(request)
            path.contains("api/ershou/item/id/") && path.endsWith("/preview") -> MockCommunityProvider.mockMarketplacePreview(request)
            path.contains("api/ershou/item/id/") -> MockCommunityProvider.mockMarketplaceDetail(request)
            path.contains("api/ershou/item/type/") -> MockCommunityProvider.mockMarketplaceItemsByType(request)
            path.contains("api/ershou/item/start/") -> MockCommunityProvider.mockMarketplaceItemList(request)
            path.contains("api/ershou/profile") -> MockCommunityProvider.mockMarketplaceProfile(request)
            path.endsWith("/api/lostandfound/item") && request.method == "POST" -> MockCommunityProvider.mockLostFoundPublish(request)
            path.contains("api/lostandfound/item/id/") && path.endsWith("/didfound") -> MockCommunityProvider.mockLostFoundDidFound(request)
            path.contains("api/lostandfound/item/id/") && request.method == "POST" -> MockCommunityProvider.mockLostFoundUpdate(request)
            path.contains("api/lostandfound/item/id/") && path.endsWith("/preview") -> MockCommunityProvider.mockLostFoundPreview(request)
            path.contains("api/lostandfound/item/id/") -> MockCommunityProvider.mockLostFoundDetail(request)
            path.contains("api/lostandfound/lostitem/start/") -> MockCommunityProvider.mockLostFoundItemList(request)
            path.contains("api/lostandfound/founditem/start/") -> MockCommunityProvider.mockLostFoundItemList(request)
            path.contains("api/lostandfound/profile") -> MockCommunityProvider.mockLostFoundProfile(request)
            path.contains("api/secret/id/") && path.endsWith("/comment") -> MockCommunityProvider.mockSecretCommentSubmit(request)
            path.contains("api/secret/id/") && path.endsWith("/like") -> MockCommunityProvider.mockSecretLikeUpdate(request)
            path.contains("api/secret/id/") && path.endsWith("/comments") -> MockCommunityProvider.mockSecretCommentList(request)
            path.contains("api/secret/id/") -> MockCommunityProvider.mockSecretDetail(request)
            path.contains("api/secret/info/start/") -> MockCommunityProvider.mockSecretPostList(request)
            path.contains("api/secret/profile") -> MockCommunityProvider.mockSecretMyPosts(request)
            path.contains("api/secret/info") && request.method == "POST" -> MockCommunityProvider.mockSecretPublish(request)
            path.contains("api/dating/pick/my/received") -> MockCommunityProvider.mockDatingReceivedPickList(request)
            path.contains("api/dating/pick/my/sent") -> MockCommunityProvider.mockDatingSentPickList(request)
            path.contains("api/dating/pick/id/") -> MockCommunityProvider.mockDatingPickStateUpdate(request)
            path.contains("api/dating/profile/id/") && path.endsWith("/state") -> MockCommunityProvider.mockDatingProfileHide(request)
            path.contains("api/dating/profile/my") -> MockCommunityProvider.mockDatingMyPosts(request)
            path.contains("api/express/id/") && path.endsWith("/comment") && request.method == "GET" ->
                MockCommunityProvider.mockExpressCommentList(request)
            path.contains("api/express/id/") && path.endsWith("/comment") -> MockCommunityProvider.mockExpressCommentSubmit(request)
            path.contains("api/express/id/") && path.endsWith("/like") -> MockCommunityProvider.mockExpressLike(request)
            path.contains("api/express/id/") && path.endsWith("/guess") -> MockCommunityProvider.mockExpressGuess(request)
            path.contains("api/express/id/") -> MockCommunityProvider.mockExpressDetail(request)
            path.contains("api/express/keyword/") -> MockCommunityProvider.mockExpressSearch(request)
            path.contains("api/express/profile/start/") -> MockCommunityProvider.mockExpressMyPosts(request)
            path.contains("api/express/start/") -> MockCommunityProvider.mockExpressPostList(request)
            path.contains("api/express") && request.method == "POST" -> MockCommunityProvider.mockExpressPublish(request)
            path.contains("api/topic/id/") && path.endsWith("/image") -> MockCommunityProvider.mockTopicImage(request)
            path.contains("api/topic/id/") && path.endsWith("/like") -> MockCommunityProvider.mockTopicLike(request)
            path.contains("api/topic/id/") -> MockCommunityProvider.mockTopicDetail(request)
            path.contains("api/topic/keyword/") -> MockCommunityProvider.mockTopicSearch(request)
            path.contains("api/topic/profile/start/") -> MockCommunityProvider.mockTopicMyPosts(request)
            path.contains("api/topic/start/") -> MockCommunityProvider.mockTopicPostList(request)
            path.contains("api/topic") && request.method == "POST" -> MockCommunityProvider.mockTopicPublish(request)
            path.contains("api/delivery/trade/id/") && path.endsWith("/finishtrade") -> MockCommunityProvider.mockDeliveryFinishTrade(request)
            path.contains("api/delivery/acceptorder") -> MockCommunityProvider.mockDeliveryAcceptOrder(request)
            path.contains("api/delivery/order/id/") && request.method == "DELETE" -> MockCommunityProvider.mockDeliveryDeleteOrder(request)
            path.contains("api/delivery/order/id/") -> MockCommunityProvider.mockDeliveryOrderDetail(request)
            path.contains("api/delivery/order/start/") -> MockCommunityProvider.mockDeliveryOrders(request)
            path.contains("api/delivery/mine") -> MockCommunityProvider.mockDeliveryMine(request)
            path.contains("api/delivery/order") && request.method == "POST" -> MockCommunityProvider.mockDeliveryPublish(request)
            path.contains("api/photograph/statistics/photos") -> MockCommunityProvider.mockPhotographPhotoCount(request)
            path.contains("api/photograph/statistics/comments") -> MockCommunityProvider.mockPhotographCommentCount(request)
            path.contains("api/photograph/statistics/likes") -> MockCommunityProvider.mockPhotographLikeCount(request)
            path.contains("api/photograph/id/") && path.endsWith("/comment") && request.method == "GET" ->
                MockCommunityProvider.mockPhotographComments(request)
            path.contains("api/photograph/id/") && path.endsWith("/comment") -> MockCommunityProvider.mockPhotographCommentSubmit(request)
            path.contains("api/photograph/id/") && path.endsWith("/like") -> MockCommunityProvider.mockPhotographLike(request)
            path.contains("api/photograph/id/") && path.endsWith("/image") -> MockCommunityProvider.mockPhotographImage(request)
            path.contains("api/photograph/id/") -> MockCommunityProvider.mockPhotographDetail(request)
            path.contains("api/photograph/profile/start/") -> MockCommunityProvider.mockPhotographMyPosts(request)
            path.contains("api/photograph/type/") -> MockCommunityProvider.mockPhotographPosts(request)
            path.contains("api/photograph") && request.method == "POST" -> MockCommunityProvider.mockPhotographPublish(request)
            else -> null
        }
    }

    private fun routeInfo(request: Request): String? {
        val path = request.url.encodedPath
        return when {
            path.contains("api/announcement/start/") -> MockInfoProvider.mockAnnouncementPage(request)
            path.contains("api/message/id/") && path.contains("/read") -> MockInfoProvider.mockMessageRead(request)
            path.contains("api/message/readall") -> MockInfoProvider.mockMessageReadAll(request)
            path.contains("api/message/unread") -> MockInfoProvider.mockMessageUnread(request)
            path.contains("api/message/interaction/start/") -> MockInfoProvider.mockInteractionMessages(request)
            path.contains("api/news/type/") -> MockInfoProvider.mockNews(request)
            else -> null
        }
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
}
