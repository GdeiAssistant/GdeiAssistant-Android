package cn.gdeiassistant.network.api

import cn.gdeiassistant.model.DataJsonResult
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface SocialApi {

    @GET("api/social/me")
    suspend fun getMe(): DataJsonResult<SocialUserDto>

    @GET("api/social/users")
    suspend fun searchUsers(
        @Query("query") query: String? = null,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int? = null
    ): DataJsonResult<SocialPageDto<SocialUserDto>>

    @GET("api/social/users/{id}")
    suspend fun getUser(
        @Path("id") id: String
    ): DataJsonResult<SocialUserDto>

    @GET("api/social/users/{id}/relationships")
    suspend fun getRelationships(
        @Path("id") id: String,
        @Query("kind") kind: String,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int? = null
    ): DataJsonResult<SocialPageDto<SocialUserDto>>

    @PUT("api/social/users/{id}/follow")
    suspend fun follow(
        @Path("id") id: String
    ): DataJsonResult<SocialUserDto>

    @DELETE("api/social/users/{id}/follow")
    suspend fun unfollow(
        @Path("id") id: String
    ): DataJsonResult<SocialUserDto>

    @PUT("api/social/users/{id}/block")
    suspend fun block(
        @Path("id") id: String
    ): DataJsonResult<BlockStateDto>

    @DELETE("api/social/users/{id}/block")
    suspend fun unblock(
        @Path("id") id: String
    ): DataJsonResult<BlockStateDto>

    @GET("api/social/blocks")
    suspend fun getBlocks(
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int? = null
    ): DataJsonResult<SocialPageDto<SocialUserDto>>

    @GET("api/social/privacy")
    suspend fun getPrivacy(): DataJsonResult<SocialPrivacyDto>

    @PUT("api/social/privacy")
    suspend fun updatePrivacy(
        @Body body: SocialPrivacyDto
    ): DataJsonResult<SocialPrivacyDto>

    @GET("api/social/unread")
    suspend fun getUnread(): DataJsonResult<SocialUnreadDto>

    @POST("api/social/conversations")
    suspend fun createConversation(
        @Body body: CreateConversationRequest
    ): DataJsonResult<ConversationDto>

    @GET("api/social/conversations")
    suspend fun getConversations(
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int? = null
    ): DataJsonResult<SocialPageDto<ConversationDto>>

    @GET("api/social/conversations/{id}")
    suspend fun getConversation(
        @Path("id") id: String
    ): DataJsonResult<ConversationDto>

    @GET("api/social/conversations/{id}/messages")
    suspend fun getMessages(
        @Path("id") id: String,
        @Query("beforeSeq") beforeSeq: String? = null,
        @Query("afterSeq") afterSeq: String? = null,
        @Query("limit") limit: Int? = null
    ): DataJsonResult<SocialPageDto<ChatMessageDto>>

    @POST("api/social/conversations/{id}/messages")
    suspend fun sendMessage(
        @Path("id") id: String,
        @Body body: SendMessageRequest
    ): DataJsonResult<ChatMessageDto>

    @Multipart
    @POST("api/social/conversations/{id}/messages/image")
    suspend fun sendImageMessage(
        @Path("id") id: String,
        @Part("clientMessageId") clientMessageId: RequestBody,
        @Part image: MultipartBody.Part
    ): DataJsonResult<ChatMessageDto>

    @PUT("api/social/conversations/{id}/read")
    suspend fun markRead(
        @Path("id") id: String,
        @Body body: MarkReadRequest
    ): DataJsonResult<ConversationReadDto>
}

data class SocialUserDto(
    val id: String? = null,
    val nickname: String? = null,
    val avatarUrl: String? = null,
    val introduction: String? = null,
    val followingCount: Int? = null,
    val followerCount: Int? = null,
    val friendCount: Int? = null,
    val relationship: String? = null,
    val blockedByMe: Boolean? = null,
    val canMessage: Boolean? = null,
    val messagePermissionReason: String? = null
)

data class ChatImageDto(
    val url: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val size: Long? = null,
    val contentType: String? = null
)

data class ChatMessageDto(
    val id: String? = null,
    val conversationId: String? = null,
    val seq: String? = null,
    val senderId: String? = null,
    val clientMessageId: String? = null,
    val content: String? = null,
    val createdAt: String? = null,
    val type: String? = null,
    val image: ChatImageDto? = null
)

data class ConversationDto(
    val id: String? = null,
    val peer: SocialUserDto? = null,
    val lastMessage: ChatMessageDto? = null,
    val updatedAt: String? = null,
    val unreadCount: Int? = null,
    val lastReadSeq: String? = null,
    val canSend: Boolean? = null,
    val sendPermissionReason: String? = null,
    val imageMessagingEnabled: Boolean? = null
)

data class SocialPageDto<T>(
    val items: List<T>? = null,
    val nextCursor: String? = null,
    val hasMore: Boolean? = null
)

data class SocialPrivacyDto(
    val dmPolicy: String? = null
)

data class SocialUnreadDto(
    val total: Int? = null
)

data class ConversationReadDto(
    val lastReadSeq: String? = null,
    val unreadCount: Int? = null
)

data class BlockStateDto(
    val blocked: Boolean? = null
)

data class CreateConversationRequest(
    val peerId: String
)

data class SendMessageRequest(
    val clientMessageId: String,
    val content: String
)

data class MarkReadRequest(
    val lastReadSeq: String
)
