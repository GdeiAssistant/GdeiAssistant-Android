package cn.gdeiassistant.data

import cn.gdeiassistant.network.cancellableRunCatching
import android.content.Context
import android.net.Uri
import cn.gdeiassistant.R
import cn.gdeiassistant.model.BlockState
import cn.gdeiassistant.model.ChatImageMeta
import cn.gdeiassistant.model.ChatMessage
import cn.gdeiassistant.model.ChatMessageType
import cn.gdeiassistant.model.ChatSendStatus
import cn.gdeiassistant.model.Conversation
import cn.gdeiassistant.model.ConversationReadState
import cn.gdeiassistant.model.DmPolicy
import cn.gdeiassistant.model.SocialPage
import cn.gdeiassistant.model.SocialPrivacySettings
import cn.gdeiassistant.model.SocialRelationship
import cn.gdeiassistant.model.SocialRelationshipKind
import cn.gdeiassistant.model.SocialUnreadCount
import cn.gdeiassistant.model.SocialUser
import cn.gdeiassistant.network.AppException
import cn.gdeiassistant.network.api.ChatImageDto
import cn.gdeiassistant.network.api.ChatMessageDto
import cn.gdeiassistant.network.api.ConversationDto
import cn.gdeiassistant.network.api.CreateConversationRequest
import cn.gdeiassistant.network.api.MarkReadRequest
import cn.gdeiassistant.network.api.SendMessageRequest
import cn.gdeiassistant.network.api.SocialApi
import cn.gdeiassistant.network.api.SocialPageDto
import cn.gdeiassistant.network.api.SocialPrivacyDto
import cn.gdeiassistant.network.api.SocialUserDto
import cn.gdeiassistant.network.safeApiCall
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocialRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val socialApi: SocialApi
) {

    suspend fun getMe(): Result<SocialUser> = withContext(Dispatchers.IO) {
        safeApiCall { socialApi.getMe() }.mapCatching { requireUser(it) }
    }

    suspend fun searchUsers(
        query: String? = null,
        cursor: String? = null,
        limit: Int = 20
    ): Result<SocialPage<SocialUser>> = withContext(Dispatchers.IO) {
        safeApiCall {
            socialApi.searchUsers(
                query = query?.trim()?.takeIf(String::isNotBlank),
                cursor = cursor?.takeIf(String::isNotBlank),
                limit = limit.coerceIn(1, 50)
            )
        }.mapCatching { mapUserPage(it) }
    }

    suspend fun getUser(id: String): Result<SocialUser> = withContext(Dispatchers.IO) {
        safeApiCall { socialApi.getUser(id.trim()) }.mapCatching { requireUser(it) }
    }

    suspend fun getRelationships(
        userId: String,
        kind: SocialRelationshipKind,
        cursor: String? = null,
        limit: Int = 20
    ): Result<SocialPage<SocialUser>> = withContext(Dispatchers.IO) {
        safeApiCall {
            socialApi.getRelationships(
                id = userId.trim(),
                kind = kind.remoteValue,
                cursor = cursor?.takeIf(String::isNotBlank),
                limit = limit.coerceIn(1, 50)
            )
        }.mapCatching { mapUserPage(it) }
    }

    suspend fun follow(userId: String): Result<SocialUser> = withContext(Dispatchers.IO) {
        safeApiCall { socialApi.follow(userId.trim()) }.mapCatching { requireUser(it) }
    }

    suspend fun unfollow(userId: String): Result<SocialUser> = withContext(Dispatchers.IO) {
        safeApiCall { socialApi.unfollow(userId.trim()) }.mapCatching { requireUser(it) }
    }

    suspend fun block(userId: String): Result<BlockState> = withContext(Dispatchers.IO) {
        safeApiCall { socialApi.block(userId.trim()) }.mapCatching {
            BlockState(blocked = it?.blocked == true)
        }
    }

    suspend fun unblock(userId: String): Result<BlockState> = withContext(Dispatchers.IO) {
        safeApiCall { socialApi.unblock(userId.trim()) }.mapCatching {
            BlockState(blocked = it?.blocked == true)
        }
    }

    suspend fun getBlocks(
        cursor: String? = null,
        limit: Int = 20
    ): Result<SocialPage<SocialUser>> = withContext(Dispatchers.IO) {
        safeApiCall {
            socialApi.getBlocks(
                cursor = cursor?.takeIf(String::isNotBlank),
                limit = limit.coerceIn(1, 50)
            )
        }.mapCatching { mapUserPage(it) }
    }

    suspend fun getPrivacy(): Result<SocialPrivacySettings> = withContext(Dispatchers.IO) {
        safeApiCall { socialApi.getPrivacy() }.mapCatching {
            SocialPrivacySettings(dmPolicy = DmPolicy.fromRemote(it?.dmPolicy))
        }
    }

    suspend fun updatePrivacy(dmPolicy: DmPolicy): Result<SocialPrivacySettings> =
        withContext(Dispatchers.IO) {
            safeApiCall {
                socialApi.updatePrivacy(SocialPrivacyDto(dmPolicy = dmPolicy.name))
            }.mapCatching {
                SocialPrivacySettings(dmPolicy = DmPolicy.fromRemote(it?.dmPolicy ?: dmPolicy.name))
            }
        }

    suspend fun getUnread(): Result<SocialUnreadCount> = withContext(Dispatchers.IO) {
        safeApiCall { socialApi.getUnread() }.mapCatching {
            SocialUnreadCount(total = (it?.total ?: 0).coerceAtLeast(0))
        }
    }

    suspend fun createConversation(peerId: String): Result<Conversation> =
        withContext(Dispatchers.IO) {
            safeApiCall {
                socialApi.createConversation(CreateConversationRequest(peerId = peerId.trim()))
            }.mapCatching { requireConversation(it) }
        }

    suspend fun getConversations(
        cursor: String? = null,
        limit: Int = 20
    ): Result<SocialPage<Conversation>> = withContext(Dispatchers.IO) {
        safeApiCall {
            socialApi.getConversations(
                cursor = cursor?.takeIf(String::isNotBlank),
                limit = limit.coerceIn(1, 50)
            )
        }.mapCatching { mapConversationPage(it) }
    }

    suspend fun getConversation(id: String): Result<Conversation> = withContext(Dispatchers.IO) {
        safeApiCall { socialApi.getConversation(id.trim()) }.mapCatching { requireConversation(it) }
    }

    suspend fun getMessages(
        conversationId: String,
        beforeSeq: String? = null,
        afterSeq: String? = null,
        limit: Int = 20
    ): Result<SocialPage<ChatMessage>> = withContext(Dispatchers.IO) {
        if (!beforeSeq.isNullOrBlank() && !afterSeq.isNullOrBlank()) {
            return@withContext Result.failure(
                AppException(
                    message = context.getString(R.string.social_error_invalid_request),
                    code = 400,
                    errorCode = "INVALID_REQUEST"
                )
            )
        }
        safeApiCall {
            socialApi.getMessages(
                id = conversationId.trim(),
                beforeSeq = beforeSeq?.takeIf(String::isNotBlank),
                afterSeq = afterSeq?.takeIf(String::isNotBlank),
                limit = limit.coerceIn(1, 50)
            )
        }.mapCatching { mapMessagePage(it) }
    }

    /**
     * 发送文字私信。重试时必须传入同一 [clientMessageId] 保持幂等。
     */
    suspend fun sendMessage(
        conversationId: String,
        content: String,
        clientMessageId: String = UUID.randomUUID().toString()
    ): Result<ChatMessage> = withContext(Dispatchers.IO) {
        val normalized = SocialTextSupport.normalizeMessageContent(content)
        if (!SocialTextSupport.isValidMessageContent(normalized)) {
            return@withContext Result.failure(
                AppException(
                    message = context.getString(R.string.social_error_invalid_message),
                    code = 400,
                    errorCode = "INVALID_REQUEST"
                )
            )
        }
        safeApiCall {
            socialApi.sendMessage(
                id = conversationId.trim(),
                body = SendMessageRequest(
                    clientMessageId = clientMessageId.trim(),
                    content = normalized
                )
            )
        }.mapCatching { dto ->
            requireMessage(dto).copy(sendStatus = ChatSendStatus.SENT)
        }
    }

    /**
     * 发送图片私信（multipart image + clientMessageId）。
     * 重试必须使用同一 [clientMessageId] 与同一规范化 JPEG 字节。
     */
    suspend fun sendImageMessage(
        conversationId: String,
        imageBytes: ByteArray,
        clientMessageId: String = UUID.randomUUID().toString(),
        contentType: String = SocialChatImageSupport.JPEG_MIME,
        fileName: String = "image.jpg"
    ): Result<ChatMessage> = withContext(Dispatchers.IO) {
        if (imageBytes.isEmpty() || imageBytes.size > SocialChatImageSupport.MAX_BYTES) {
            return@withContext Result.failure(
                AppException(
                    message = context.getString(R.string.social_error_invalid_image),
                    code = 400,
                    errorCode = "INVALID_REQUEST"
                )
            )
        }
        val mime = contentType.trim().ifBlank { SocialChatImageSupport.JPEG_MIME }
        val part = MultipartBody.Part.createFormData(
            "image",
            fileName,
            imageBytes.toRequestBody(mime.toMediaTypeOrNull())
        )
        val clientIdBody = clientMessageId.trim()
            .toRequestBody("text/plain; charset=utf-8".toMediaTypeOrNull())
        safeApiCall {
            socialApi.sendImageMessage(
                id = conversationId.trim(),
                clientMessageId = clientIdBody,
                image = part
            )
        }.mapCatching { dto ->
            requireMessage(dto).copy(sendStatus = ChatSendStatus.SENT)
        }
    }

    suspend fun prepareChatImage(uri: Uri): Result<SocialChatImageSupport.PreparedImage> =
        withContext(Dispatchers.IO) {
            cancellableRunCatching { SocialChatImageSupport.prepareJpeg(context, uri) }
                .fold(
                    onSuccess = { Result.success(it) },
                    onFailure = {
                        Result.failure(
                            AppException(
                                message = context.getString(R.string.social_error_invalid_image),
                                code = 400,
                                errorCode = "INVALID_REQUEST"
                            )
                        )
                    }
                )
        }

    suspend fun markRead(
        conversationId: String,
        lastReadSeq: String
    ): Result<ConversationReadState> = withContext(Dispatchers.IO) {
        safeApiCall {
            socialApi.markRead(
                id = conversationId.trim(),
                body = MarkReadRequest(lastReadSeq = lastReadSeq.trim())
            )
        }.mapCatching { dto ->
            ConversationReadState(
                lastReadSeq = dto?.lastReadSeq?.trim().orEmpty().ifBlank { lastReadSeq.trim() },
                unreadCount = (dto?.unreadCount ?: 0).coerceAtLeast(0)
            )
        }
    }

    private fun mapUserPage(page: SocialPageDto<SocialUserDto>?): SocialPage<SocialUser> {
        return SocialPage(
            items = page?.items.orEmpty().mapNotNull(::mapUserOrNull),
            nextCursor = page?.nextCursor?.trim()?.takeIf(String::isNotBlank),
            hasMore = page?.hasMore == true
        )
    }

    private fun mapConversationPage(page: SocialPageDto<ConversationDto>?): SocialPage<Conversation> {
        return SocialPage(
            items = page?.items.orEmpty().mapNotNull(::mapConversationOrNull),
            nextCursor = page?.nextCursor?.trim()?.takeIf(String::isNotBlank),
            hasMore = page?.hasMore == true
        )
    }

    private fun mapMessagePage(page: SocialPageDto<ChatMessageDto>?): SocialPage<ChatMessage> {
        return SocialPage(
            items = page?.items.orEmpty().mapNotNull(::mapMessageOrNull),
            nextCursor = page?.nextCursor?.trim()?.takeIf(String::isNotBlank),
            hasMore = page?.hasMore == true
        )
    }

    private fun requireUser(dto: SocialUserDto?): SocialUser {
        return mapUserOrNull(dto)
            ?: throw AppException(context.getString(R.string.social_error_user_not_found), 404, "USER_NOT_FOUND")
    }

    private fun requireConversation(dto: ConversationDto?): Conversation {
        return mapConversationOrNull(dto)
            ?: throw AppException(
                context.getString(R.string.social_error_conversation_not_found),
                404,
                "CONVERSATION_NOT_FOUND"
            )
    }

    private fun requireMessage(dto: ChatMessageDto?): ChatMessage {
        return mapMessageOrNull(dto)
            ?: throw AppException(context.getString(R.string.social_error_invalid_request), 400, "INVALID_REQUEST")
    }

    private fun mapUserOrNull(dto: SocialUserDto?): SocialUser? {
        val id = dto?.id?.trim().orEmpty()
        if (id.isBlank()) return null
        return SocialUser(
            id = id,
            nickname = dto?.nickname?.trim().orEmpty().ifBlank {
                context.getString(R.string.social_default_nickname)
            },
            avatarUrl = dto?.avatarUrl?.trim()?.takeIf(String::isNotBlank),
            introduction = dto?.introduction?.trim()?.takeIf(String::isNotBlank),
            followingCount = (dto?.followingCount ?: 0).coerceAtLeast(0),
            followerCount = (dto?.followerCount ?: 0).coerceAtLeast(0),
            friendCount = (dto?.friendCount ?: 0).coerceAtLeast(0),
            relationship = SocialRelationship.fromRemote(dto?.relationship),
            blockedByMe = dto?.blockedByMe == true,
            canMessage = dto?.canMessage == true,
            messagePermissionReason = dto?.messagePermissionReason?.trim()?.takeIf(String::isNotBlank)
        )
    }

    private fun mapConversationOrNull(dto: ConversationDto?): Conversation? {
        val id = dto?.id?.trim().orEmpty()
        val peer = mapUserOrNull(dto?.peer) ?: return null
        if (id.isBlank()) return null
        return Conversation(
            id = id,
            peer = peer,
            lastMessage = mapMessageOrNull(dto?.lastMessage),
            updatedAt = dto?.updatedAt?.trim().orEmpty(),
            unreadCount = (dto?.unreadCount ?: 0).coerceAtLeast(0),
            lastReadSeq = dto?.lastReadSeq?.trim().orEmpty().ifBlank { "0" },
            canSend = dto?.canSend == true,
            sendPermissionReason = dto?.sendPermissionReason?.trim()?.takeIf(String::isNotBlank),
            imageMessagingEnabled = dto?.imageMessagingEnabled == true
        )
    }

    private fun mapMessageOrNull(dto: ChatMessageDto?): ChatMessage? {
        val id = dto?.id?.trim().orEmpty()
        val conversationId = dto?.conversationId?.trim().orEmpty()
        val seq = dto?.seq?.trim().orEmpty()
        val senderId = dto?.senderId?.trim().orEmpty()
        val clientMessageId = dto?.clientMessageId?.trim().orEmpty()
        val type = ChatMessageType.fromRemote(dto?.type)
        val content = dto?.content.orEmpty()
        if (id.isBlank() || conversationId.isBlank() || seq.isBlank() || senderId.isBlank()) {
            return null
        }
        return ChatMessage(
            id = id,
            conversationId = conversationId,
            seq = seq,
            senderId = senderId,
            clientMessageId = clientMessageId,
            content = content,
            createdAt = dto?.createdAt?.trim().orEmpty(),
            sendStatus = ChatSendStatus.SENT,
            type = type,
            image = mapImageOrNull(dto?.image)
        )
    }

    private fun mapImageOrNull(dto: ChatImageDto?): ChatImageMeta? {
        if (dto == null) return null
        return ChatImageMeta(
            url = dto.url?.trim()?.takeIf(String::isNotBlank),
            width = dto.width?.takeIf { it > 0 },
            height = dto.height?.takeIf { it > 0 },
            size = dto.size?.takeIf { it > 0 },
            contentType = dto.contentType?.trim()?.takeIf(String::isNotBlank)
        )
    }
}
