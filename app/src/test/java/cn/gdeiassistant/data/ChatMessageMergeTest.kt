package cn.gdeiassistant.data

import cn.gdeiassistant.model.ChatImageMeta
import cn.gdeiassistant.model.ChatMessage
import cn.gdeiassistant.model.ChatMessageType
import cn.gdeiassistant.model.ChatSendStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatMessageMergeTest {

    @Test
    fun wsEchoBeforeHttpMergesPendingIntoSingleBubble() {
        val pending = message(
            id = "local-a",
            conversationId = "1",
            senderId = "me",
            clientMessageId = "a",
            content = "hello",
            sendStatus = ChatSendStatus.PENDING,
            seq = ""
        )
        val wsSent = pending.copy(id = "42", seq = "1", sendStatus = ChatSendStatus.SENT)

        val merged = ChatMessageMerge.merge(
            existing = listOf(pending),
            incoming = listOf(wsSent),
            prepend = false,
            replace = false
        )

        assertEquals(1, merged.size)
        assertEquals("42", merged.single().id)
        assertEquals(ChatSendStatus.SENT, merged.single().sendStatus)
    }

    @Test
    fun httpTimeoutAfterWsCommitDoesNotMarkFailed() {
        val sent = message(
            id = "42",
            conversationId = "1",
            senderId = "me",
            clientMessageId = "a",
            content = "hello",
            sendStatus = ChatSendStatus.SENT,
            seq = "1"
        )
        val afterTimeout = ChatMessageMerge.markFailed(
            messages = listOf(sent),
            senderId = "me",
            clientMessageId = "a"
        )
        assertEquals(1, afterTimeout.size)
        assertEquals(ChatSendStatus.SENT, afterTimeout.single().sendStatus)
    }

    @Test
    fun replacePendingDoesNotDuplicateSameServerIdWhenWsAlreadyMerged() {
        val pending = message(
            id = "local-a",
            conversationId = "conv1",
            senderId = "me",
            clientMessageId = "a",
            content = "hello",
            sendStatus = ChatSendStatus.PENDING,
            seq = ""
        )
        val wsSent = pending.copy(id = "42", seq = "1", sendStatus = ChatSendStatus.SENT)
        val afterWs = ChatMessageMerge.merge(listOf(pending), listOf(wsSent), false, false)
        val httpSent = wsSent.copy(content = "hello")
        val afterHttp = ChatMessageMerge.replacePending(
            messages = afterWs,
            senderId = "me",
            clientMessageId = "a",
            sent = httpSent,
            conversationId = "conv1"
        )
        assertEquals(1, afterHttp.size)
        assertEquals("42", afterHttp.single().id)
        assertEquals(ChatSendStatus.SENT, afterHttp.single().sendStatus)
    }

    @Test
    fun duplicateRestReturnKeepsSingleCommittedMessage() {
        val first = message(
            id = "srv-1",
            conversationId = "c",
            senderId = "me",
            clientMessageId = "a",
            content = "x",
            sendStatus = ChatSendStatus.SENT,
            seq = "1"
        )
        val duplicate = first.copy(content = "updated")
        val merged = ChatMessageMerge.merge(
            existing = listOf(first),
            incoming = listOf(duplicate),
            prepend = false,
            replace = false
        )
        assertEquals(1, merged.size)
        assertEquals("updated", merged.single().content)
    }

    @Test
    fun twoSendersWithSameClientMessageIdRemainDistinct() {
        val conversationId = "conv-1"
        val sharedClientId = "same-uuid"
        val minePending = message(
            id = "local-1",
            conversationId = conversationId,
            senderId = "me",
            clientMessageId = sharedClientId,
            content = "我的待发送",
            sendStatus = ChatSendStatus.PENDING,
            seq = "0"
        )
        val peerSent = message(
            id = "srv-peer-1",
            conversationId = conversationId,
            senderId = "peer",
            clientMessageId = sharedClientId,
            content = "对方消息",
            sendStatus = ChatSendStatus.SENT,
            seq = "2"
        )

        val merged = ChatMessageMerge.merge(
            existing = listOf(minePending),
            incoming = listOf(peerSent),
            prepend = false,
            replace = false
        )

        assertEquals(2, merged.size)
        assertTrue(
            merged.any {
                it.senderId == "me" && it.content == "我的待发送" && it.sendStatus == ChatSendStatus.PENDING
            }
        )
        assertTrue(
            merged.any {
                it.senderId == "peer" && it.content == "对方消息" && it.sendStatus == ChatSendStatus.SENT
            }
        )
    }

    @Test
    fun markFailedOnlyAffectsLocalPendingInSameConversation() {
        val pending = message(
            id = "local-a",
            conversationId = "conv1",
            senderId = "me",
            clientMessageId = "a",
            content = "hello",
            sendStatus = ChatSendStatus.PENDING,
            seq = ""
        )
        val otherConvPending = pending.copy(
            id = "local-b",
            conversationId = "conv2",
            clientMessageId = "a"
        )
        val failed = ChatMessageMerge.markFailed(
            messages = listOf(pending, otherConvPending),
            senderId = "me",
            clientMessageId = "a",
            conversationId = "conv1"
        )
        assertEquals(ChatSendStatus.FAILED, failed.first { it.conversationId == "conv1" }.sendStatus)
        assertEquals(ChatSendStatus.PENDING, failed.first { it.conversationId == "conv2" }.sendStatus)
    }

    @Test
    fun markPendingRetryDoesNotTouchCommittedSent() {
        val sent = message(
            id = "42",
            conversationId = "1",
            senderId = "me",
            clientMessageId = "a",
            content = "hello",
            sendStatus = ChatSendStatus.SENT,
            seq = "1"
        )
        val retried = ChatMessageMerge.markPendingRetry(
            messages = listOf(sent),
            senderId = "me",
            clientMessageId = "a",
            conversationId = "1"
        )
        assertEquals(ChatSendStatus.SENT, retried.single().sendStatus)
    }

    @Test
    fun mergePreservesImageTypeAndLocalPathWhenServerConfirms() {
        val pending = message(
            id = "local-a",
            conversationId = "c1",
            senderId = "me",
            clientMessageId = "a",
            content = "",
            sendStatus = ChatSendStatus.PENDING,
            seq = ""
        ).copy(
            type = ChatMessageType.IMAGE,
            localImagePath = "/tmp/a.jpg",
            image = ChatImageMeta(contentType = "image/jpeg", size = 12)
        )
        val sent = message(
            id = "m-1",
            conversationId = "c1",
            senderId = "me",
            clientMessageId = "a",
            content = "",
            sendStatus = ChatSendStatus.SENT,
            seq = "9"
        ).copy(
            type = ChatMessageType.IMAGE,
            image = ChatImageMeta(
                url = "/api/social/conversations/c1/messages/m-1/image",
                width = 1,
                height = 1,
                size = 12,
                contentType = "image/jpeg"
            )
        )
        val merged = ChatMessageMerge.merge(listOf(pending), listOf(sent), false, false)
        assertEquals(1, merged.size)
        assertEquals(ChatMessageType.IMAGE, merged.single().type)
        assertEquals("/tmp/a.jpg", merged.single().localImagePath)
        assertEquals("image/jpeg", merged.single().image?.contentType)
        assertEquals(ChatSendStatus.SENT, merged.single().sendStatus)
    }

    @Test
    fun refreshKeepsUnconfirmedFailedImageAndPendingTextButReplacesServerHistory() {
        val failed = message("local-image", "c1", "me", "image", "", ChatSendStatus.FAILED, "")
            .copy(type = ChatMessageType.IMAGE, localImagePath = "/tmp/original.jpg")
        val pending = message("local-text", "c1", "me", "text", "new", ChatSendStatus.PENDING, "")
        val previousServer = message("old", "c1", "peer", "old", "old", ChatSendStatus.SENT, "1")
        val refreshedServer = message("new", "c1", "peer", "new", "fresh", ChatSendStatus.SENT, "2")

        val refreshed = ChatMessageMerge.merge(
            listOf(previousServer, failed, pending), listOf(refreshedServer), prepend = false, replace = true
        )

        assertEquals(setOf("new", "local-image", "local-text"), refreshed.map { it.id }.toSet())
        assertEquals(failed, refreshed.first { it.id == failed.id })
        assertEquals(pending, refreshed.first { it.id == pending.id })
    }

    @Test
    fun refreshConfirmationReplacesFailedImageOnceAndKeepsOriginalPathForCleanup() {
        val failed = message("local-image", "c1", "me", "image", "", ChatSendStatus.FAILED, "")
            .copy(type = ChatMessageType.IMAGE, localImagePath = "/tmp/original.jpg")
        val confirmed = failed.copy(
            id = "server-image", seq = "3", sendStatus = ChatSendStatus.SENT, localImagePath = null,
            image = ChatImageMeta(url = "/api/social/conversations/c1/messages/server-image/image")
        )

        val refreshed = ChatMessageMerge.merge(
            listOf(failed), listOf(confirmed, confirmed), prepend = false, replace = true
        )

        assertEquals(1, refreshed.size)
        assertEquals("server-image", refreshed.single().id)
        assertEquals(ChatSendStatus.SENT, refreshed.single().sendStatus)
        assertEquals(ChatMessageType.IMAGE, refreshed.single().type)
        assertEquals("/tmp/original.jpg", refreshed.single().localImagePath)
        assertEquals(confirmed.image, refreshed.single().image)
    }

    private fun message(
        id: String,
        conversationId: String,
        senderId: String,
        clientMessageId: String,
        content: String,
        sendStatus: ChatSendStatus,
        seq: String
    ): ChatMessage {
        return ChatMessage(
            id = id,
            conversationId = conversationId,
            seq = seq,
            senderId = senderId,
            clientMessageId = clientMessageId,
            content = content,
            createdAt = "2026-01-01T00:00:00Z",
            sendStatus = sendStatus
        )
    }
}
