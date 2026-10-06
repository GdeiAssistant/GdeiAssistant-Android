package cn.gdeiassistant.data

import android.content.Context
import cn.gdeiassistant.R
import cn.gdeiassistant.model.DataJsonResult
import cn.gdeiassistant.model.DmPolicy
import cn.gdeiassistant.network.AppException
import cn.gdeiassistant.network.api.SendMessageRequest
import cn.gdeiassistant.network.api.SocialApi
import cn.gdeiassistant.network.api.SocialPrivacyDto
import cn.gdeiassistant.network.api.SocialUserDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SocialRepositoryContractTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var socialApi: SocialApi
    private lateinit var context: Context
    private lateinit var repository: SocialRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        socialApi = mock()
        context = mock()
        whenever(context.getString(R.string.social_default_nickname)).thenReturn("用户")
        whenever(context.getString(R.string.social_error_user_not_found)).thenReturn("not found")
        whenever(context.getString(R.string.social_error_invalid_message)).thenReturn("invalid message")
        whenever(context.getString(R.string.social_error_invalid_request)).thenReturn("invalid")
        whenever(context.getString(R.string.social_error_conversation_not_found)).thenReturn("missing")
        repository = SocialRepository(context, socialApi)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun getMeMapsSocialUserFields() = runTest(testDispatcher) {
        whenever(socialApi.getMe()).thenReturn(
            DataJsonResult(
                success = true,
                code = 200,
                data = SocialUserDto(
                    id = "u-1",
                    nickname = "测试",
                    followingCount = 2,
                    followerCount = 3,
                    friendCount = 1,
                    relationship = "SELF",
                    canMessage = false
                )
            )
        )

        val result = repository.getMe()
        assertTrue(result.isSuccess)
        assertEquals("u-1", result.getOrNull()?.id)
        assertEquals(2, result.getOrNull()?.followingCount)
    }

    @Test
    fun updatePrivacySendsEnumName() = runTest(testDispatcher) {
        whenever(socialApi.updatePrivacy(any())).thenReturn(
            DataJsonResult(success = true, code = 200, data = SocialPrivacyDto(dmPolicy = "NONE"))
        )

        val result = repository.updatePrivacy(DmPolicy.NONE)
        assertTrue(result.isSuccess)
        assertEquals(DmPolicy.NONE, result.getOrNull()?.dmPolicy)
        verify(socialApi).updatePrivacy(eq(SocialPrivacyDto(dmPolicy = "NONE")))
    }

    @Test
    fun sendMessageRejectsEmptyLocally() = runTest(testDispatcher) {
        val result = repository.sendMessage("c-1", "   ")
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull() as AppException
        assertEquals("INVALID_REQUEST", error.errorCode)
    }

    @Test
    fun sendMessageForwardsClientMessageId() = runTest(testDispatcher) {
        whenever(socialApi.sendMessage(any(), any())).thenReturn(
            DataJsonResult(
                success = true,
                code = 200,
                data = cn.gdeiassistant.network.api.ChatMessageDto(
                    id = "m-1",
                    conversationId = "c-1",
                    seq = "1",
                    senderId = "u-1",
                    clientMessageId = "cid-1",
                    content = "hi",
                    createdAt = "2026-10-05T00:00:00Z"
                )
            )
        )

        val result = repository.sendMessage("c-1", "hi", "cid-1")
        assertTrue(result.isSuccess)
        verify(socialApi).sendMessage(eq("c-1"), eq(SendMessageRequest("cid-1", "hi")))
    }
}
