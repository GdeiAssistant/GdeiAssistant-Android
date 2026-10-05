package cn.gdeiassistant.data

import cn.gdeiassistant.model.ChatMessage
import cn.gdeiassistant.model.ChatMessageType
import cn.gdeiassistant.model.ChatSendStatus

/**
 * 会话消息去重与 pending 匹配。
 *
 * 同时维护 serverId 与 (conversationId, senderId, clientMessageId) 索引，
 * 使 WS 先于 HTTP 到达的已提交消息能替换同键本地 pending。
 * 已提交（真实 server id + SENT）优先，不可被 HTTP 超时 markFailed 降级。
 */
object ChatMessageMerge {

    fun serverIdKey(message: ChatMessage): String? {
        val serverId = message.id.trim()
        if (serverId.isBlank() || serverId.startsWith("local-")) return null
        return "id:$serverId"
    }

    fun clientKey(message: ChatMessage): String? {
        val conversationId = message.conversationId.trim()
        val senderId = message.senderId.trim()
        val clientMessageId = message.clientMessageId.trim()
        if (conversationId.isBlank() || senderId.isBlank() || clientMessageId.isBlank()) return null
        return "c:$conversationId:$senderId:$clientMessageId"
    }

    /** 兼容旧调用；优先 serverId，否则复合键。 */
    fun dedupeKey(message: ChatMessage): String {
        return serverIdKey(message) ?: clientKey(message)
            ?: "x:${message.content.hashCode()}:${message.createdAt}:${message.senderId.trim()}"
    }

    fun merge(
        existing: List<ChatMessage>,
        incoming: List<ChatMessage>,
        prepend: Boolean,
        replace: Boolean
    ): List<ChatMessage> {
        val base = if (replace) emptyList() else existing
        val ordered = if (prepend) incoming + base else base + incoming
        val slots = ArrayList<ChatMessage>()
        val byServerId = HashMap<String, Int>()
        val byClientKey = HashMap<String, Int>()

        ordered.forEach { message ->
            val serverKey = serverIdKey(message)
            val client = clientKey(message)
            val existingIndex = when {
                serverKey != null && byServerId.containsKey(serverKey) -> byServerId.getValue(serverKey)
                client != null && byClientKey.containsKey(client) -> byClientKey.getValue(client)
                else -> null
            }
            if (existingIndex == null) {
                val index = slots.size
                slots += message
                indexMessage(message, index, byServerId, byClientKey)
            } else {
                val preferred = choosePreferred(slots[existingIndex], message)
                clearIndexesFor(slots[existingIndex], existingIndex, byServerId, byClientKey)
                slots[existingIndex] = preferred
                indexMessage(preferred, existingIndex, byServerId, byClientKey)
            }
        }

        return slots.sortedWith(
            compareBy<ChatMessage> { it.seq.toLongOrNull() ?: Long.MAX_VALUE }
                .thenBy { it.createdAt }
                .thenBy { it.id }
        )
    }

    fun replacePending(
        messages: List<ChatMessage>,
        senderId: String,
        clientMessageId: String,
        sent: ChatMessage,
        conversationId: String? = null
    ): List<ChatMessage> {
        val conversation = conversationId?.trim()?.takeIf { it.isNotBlank() }
            ?: sent.conversationId.trim().takeIf { it.isNotBlank() }
        val self = senderId.trim()
        val clientId = clientMessageId.trim()
        val withoutLocal = messages.filterNot { message ->
            // 只移除本地 pending/failed 占位，不动已提交 server 条目
            !isCommittedServerMessage(message) &&
                isLocalPlaceholder(message) &&
                matchesClientIdentity(message, conversation, self, clientId)
        }
        // 经双索引 merge，避免与已存在的同 serverId / 同 clientKey 条目重复。
        return merge(
            existing = withoutLocal,
            incoming = listOf(sent.copy(sendStatus = ChatSendStatus.SENT)),
            prepend = false,
            replace = false
        )
    }

    /**
     * 仅将仍为本地 pending/failed 的条目标为失败。
     * 已通过 WS/REST 确认的 SENT（真实 server id）保持不变。
     */
    fun markFailed(
        messages: List<ChatMessage>,
        senderId: String,
        clientMessageId: String,
        conversationId: String? = null
    ): List<ChatMessage> {
        val self = senderId.trim()
        val clientId = clientMessageId.trim()
        val conversation = conversationId?.trim()?.takeIf { it.isNotBlank() }
        return messages.map { message ->
            if (!matchesClientIdentity(message, conversation, self, clientId)) {
                message
            } else if (isCommittedServerMessage(message)) {
                message
            } else {
                message.copy(sendStatus = ChatSendStatus.FAILED)
            }
        }
    }

    /** 同客户端重试只动本地 pending/failed，不动已提交 SENT。 */
    fun markPendingRetry(
        messages: List<ChatMessage>,
        senderId: String,
        clientMessageId: String,
        conversationId: String? = null
    ): List<ChatMessage> {
        val self = senderId.trim()
        val clientId = clientMessageId.trim()
        val conversation = conversationId?.trim()?.takeIf { it.isNotBlank() }
        return messages.map { message ->
            if (
                matchesClientIdentity(message, conversation, self, clientId) &&
                message.sendStatus == ChatSendStatus.FAILED &&
                !isCommittedServerMessage(message)
            ) {
                message.copy(sendStatus = ChatSendStatus.PENDING)
            } else {
                message
            }
        }
    }

    fun isCommittedServerMessage(message: ChatMessage): Boolean {
        return serverIdKey(message) != null && message.sendStatus == ChatSendStatus.SENT
    }

    fun isLocalPlaceholder(message: ChatMessage): Boolean {
        return message.id.trim().startsWith("local-") ||
            message.sendStatus == ChatSendStatus.PENDING ||
            (message.sendStatus == ChatSendStatus.FAILED && serverIdKey(message) == null)
    }

    private fun matchesClientIdentity(
        message: ChatMessage,
        conversationId: String?,
        senderId: String,
        clientMessageId: String
    ): Boolean {
        if (message.senderId.trim() != senderId) return false
        if (message.clientMessageId.trim() != clientMessageId) return false
        if (conversationId != null && message.conversationId.trim() != conversationId) return false
        return true
    }

    private fun choosePreferred(previous: ChatMessage?, incoming: ChatMessage): ChatMessage {
        if (previous == null) return incoming
        val preferred = when {
            isCommittedServerMessage(previous) && !isCommittedServerMessage(incoming) -> previous
            isCommittedServerMessage(incoming) && !isCommittedServerMessage(previous) -> incoming
            previous.sendStatus == ChatSendStatus.SENT && incoming.sendStatus != ChatSendStatus.SENT -> previous
            incoming.sendStatus == ChatSendStatus.SENT && previous.sendStatus != ChatSendStatus.SENT -> incoming
            serverIdKey(incoming) != null && serverIdKey(previous) == null -> incoming
            serverIdKey(previous) != null && serverIdKey(incoming) == null -> previous
            else -> incoming
        }
        val other = if (preferred === incoming) previous else incoming
        return preferred.copy(
            type = when {
                preferred.type == ChatMessageType.IMAGE || other.type == ChatMessageType.IMAGE ->
                    ChatMessageType.IMAGE
                else -> preferred.type
            },
            image = preferred.image ?: other.image,
            localImagePath = preferred.localImagePath ?: other.localImagePath,
            content = if (preferred.type == ChatMessageType.IMAGE || other.type == ChatMessageType.IMAGE) {
                preferred.content.ifBlank { other.content }
            } else {
                preferred.content.ifBlank { other.content }
            }
        )
    }

    private fun indexMessage(
        message: ChatMessage,
        index: Int,
        byServerId: MutableMap<String, Int>,
        byClientKey: MutableMap<String, Int>
    ) {
        serverIdKey(message)?.let { byServerId[it] = index }
        clientKey(message)?.let { byClientKey[it] = index }
    }

    private fun clearIndexesFor(
        message: ChatMessage,
        index: Int,
        byServerId: MutableMap<String, Int>,
        byClientKey: MutableMap<String, Int>
    ) {
        serverIdKey(message)?.let { key ->
            if (byServerId[key] == index) byServerId.remove(key)
        }
        clientKey(message)?.let { key ->
            if (byClientKey[key] == index) byClientKey.remove(key)
        }
    }
}
