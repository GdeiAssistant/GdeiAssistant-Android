package cn.gdeiassistant.data

import android.content.Context
import cn.gdeiassistant.R
import cn.gdeiassistant.model.DataJsonResult
import cn.gdeiassistant.network.api.InteractionMessageDto
import cn.gdeiassistant.network.api.MessageApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class MessagesRepositoryContractTest {

    @Test
    fun interactionMessagesDropEntriesWithoutRemoteIdAndKeepFallbackCopy() = runTest {
        val context = mock<Context>()
        whenever(context.getString(R.string.messages_interaction_title)).thenReturn("互动消息")
        whenever(context.getString(R.string.messages_interaction_default_content)).thenReturn("点击查看消息详情")
        whenever(context.getString(R.string.common_just_now)).thenReturn("刚刚")

        val messageApi = mock<MessageApi>()
        whenever(messageApi.getInteractionMessages(start = 0, size = 20)).thenReturn(
            DataJsonResult(
                success = true,
                code = 200,
                data = listOf(
                    InteractionMessageDto(
                        id = null,
                        title = "无主键消息",
                        content = "不应伪造 hash ID"
                    ),
                    InteractionMessageDto(
                        id = "   ",
                        title = "空白主键消息",
                        content = "应被过滤丢弃"
                    ),
                    InteractionMessageDto(
                        id = " msg-101 ",
                        module = " topic ",
                        type = " comment ",
                        title = " ",
                        content = "",
                        createdAt = "",
                        isRead = false,
                        targetType = " topic ",
                        targetId = " t-9 "
                    )
                )
            )
        )

        val items = MessagesRepository(context, messageApi).getInteractionMessages().getOrThrow()

        assertEquals(1, items.size)
        val item = items.single()
        assertEquals("msg-101", item.id)
        assertEquals("topic", item.module)
        assertEquals("comment", item.type)
        assertEquals("互动消息", item.title)
        assertEquals("点击查看消息详情", item.content)
        assertEquals("刚刚", item.createdAt)
        assertEquals("topic", item.targetType)
        assertEquals("t-9", item.targetId)
    }
}
