package cn.gdeiassistant.ui.social

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.R
import cn.gdeiassistant.data.ChatMessageMerge
import cn.gdeiassistant.data.SocialChatImageCache
import cn.gdeiassistant.data.SessionManager
import cn.gdeiassistant.data.SocialRepository
import cn.gdeiassistant.data.SocialSessionCoordinator
import cn.gdeiassistant.data.SocialTextSupport
import cn.gdeiassistant.model.ChatImageMeta
import cn.gdeiassistant.model.ChatMessage
import cn.gdeiassistant.model.ChatMessageType
import cn.gdeiassistant.model.ChatSendStatus
import cn.gdeiassistant.model.Conversation
import cn.gdeiassistant.model.DmPolicy
import cn.gdeiassistant.model.SocialRealtimeEvent
import cn.gdeiassistant.model.SocialRelationship
import cn.gdeiassistant.model.SocialRelationshipKind
import cn.gdeiassistant.model.SocialUser
import cn.gdeiassistant.network.AppException
import cn.gdeiassistant.network.SocialRealtimeManager
import cn.gdeiassistant.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class SocialSearchUiState(
    val query: String = "",
    val items: List<SocialUser> = emptyList(),
    val nextCursor: String? = null,
    val hasMore: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface SocialUiEvent {
    data class ShowMessage(val message: String) : SocialUiEvent
    data class OpenConversation(val conversationId: String) : SocialUiEvent
}

@HiltViewModel
class SocialSearchViewModel @Inject constructor(
    private val socialRepository: SocialRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(SocialSearchUiState())
    val state: StateFlow<SocialSearchUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<SocialUiEvent>()
    val events: SharedFlow<SocialUiEvent> = _events.asSharedFlow()

    fun updateQuery(value: String) {
        _state.update { it.copy(query = value) }
    }

    fun search(reset: Boolean = true) {
        viewModelScope.launch {
            val query = _state.value.query.trim()
            _state.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    items = if (reset) emptyList() else it.items,
                    nextCursor = if (reset) null else it.nextCursor
                )
            }
            socialRepository.searchUsers(
                query = query.takeIf(String::isNotBlank),
                cursor = if (reset) null else _state.value.nextCursor
            ).onSuccess { page ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        items = if (reset) page.items else it.items + page.items,
                        nextCursor = page.nextCursor,
                        hasMore = page.hasMore
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(isLoading = false, error = error.message ?: context.getString(R.string.load_failed))
                }
            }
        }
    }

    fun loadMore() {
        if (_state.value.isLoading || !_state.value.hasMore) return
        search(reset = false)
    }
}

data class SocialProfileUiState(
    val user: SocialUser? = null,
    val isLoading: Boolean = true,
    val isActing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SocialProfileViewModel @Inject constructor(
    private val socialRepository: SocialRepository,
    private val realtimeManager: SocialRealtimeManager,
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val userId: String = savedStateHandle.get<String>(Routes.SOCIAL_USER_ID).orEmpty()

    private val _state = MutableStateFlow(SocialProfileUiState())
    val state: StateFlow<SocialProfileUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<SocialUiEvent>()
    val events: SharedFlow<SocialUiEvent> = _events.asSharedFlow()

    init {
        refresh()
        viewModelScope.launch {
            realtimeManager.events.collect { event ->
                if (event is SocialRealtimeEvent.SocialChanged || event is SocialRealtimeEvent.Ready) {
                    refresh()
                }
            }
        }
    }

    fun refresh() {
        if (userId.isBlank()) {
            _state.update {
                it.copy(isLoading = false, error = context.getString(R.string.social_error_user_not_found))
            }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            socialRepository.getUser(userId)
                .onSuccess { user -> _state.update { it.copy(isLoading = false, user = user) } }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: context.getString(R.string.load_failed)
                        )
                    }
                }
        }
    }

    fun toggleFollow() {
        val user = _state.value.user ?: return
        if (user.relationship == SocialRelationship.SELF || _state.value.isActing) return
        viewModelScope.launch {
            _state.update { it.copy(isActing = true) }
            val result = if (
                user.relationship == SocialRelationship.FOLLOWING ||
                user.relationship == SocialRelationship.MUTUAL
            ) {
                socialRepository.unfollow(user.id)
            } else {
                socialRepository.follow(user.id)
            }
            result.onSuccess { updated ->
                _state.update { it.copy(isActing = false, user = updated) }
            }.onFailure { error ->
                _state.update { it.copy(isActing = false) }
                _events.emit(SocialUiEvent.ShowMessage(error.userMessage()))
            }
        }
    }

    fun toggleBlock() {
        val user = _state.value.user ?: return
        if (user.relationship == SocialRelationship.SELF || _state.value.isActing) return
        viewModelScope.launch {
            _state.update { it.copy(isActing = true) }
            val result = if (user.blockedByMe) {
                socialRepository.unblock(user.id)
            } else {
                socialRepository.block(user.id)
            }
            result.onSuccess {
                refresh()
                _state.update { it.copy(isActing = false) }
            }.onFailure { error ->
                _state.update { it.copy(isActing = false) }
                _events.emit(SocialUiEvent.ShowMessage(error.userMessage()))
            }
        }
    }

    fun openOrCreateConversation() {
        val user = _state.value.user ?: return
        if (!user.canMessage || _state.value.isActing) return
        viewModelScope.launch {
            _state.update { it.copy(isActing = true) }
            socialRepository.createConversation(user.id)
                .onSuccess { conversation ->
                    _state.update { it.copy(isActing = false) }
                    _events.emit(SocialUiEvent.OpenConversation(conversation.id))
                }
                .onFailure { error ->
                    _state.update { it.copy(isActing = false) }
                    _events.emit(SocialUiEvent.ShowMessage(error.userMessage()))
                }
        }
    }

    private fun Throwable.userMessage(): String =
        message?.takeIf(String::isNotBlank) ?: context.getString(R.string.messages_action_failed)
}

data class SocialRelationListUiState(
    val kind: SocialRelationshipKind = SocialRelationshipKind.FOLLOWING,
    val items: List<SocialUser> = emptyList(),
    val nextCursor: String? = null,
    val hasMore: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SocialRelationListViewModel @Inject constructor(
    private val socialRepository: SocialRepository,
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val userId: String = savedStateHandle.get<String>(Routes.SOCIAL_USER_ID).orEmpty()
    private val kind: SocialRelationshipKind = SocialRelationshipKind.entries.firstOrNull {
        it.remoteValue.equals(savedStateHandle.get<String>(Routes.SOCIAL_RELATION_KIND), ignoreCase = true)
    } ?: SocialRelationshipKind.FOLLOWING

    private val _state = MutableStateFlow(SocialRelationListUiState(kind = kind, isLoading = true))
    val state: StateFlow<SocialRelationListUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        load(reset = true)
    }

    fun loadMore() {
        if (_state.value.isLoading || !_state.value.hasMore) return
        load(reset = false)
    }

    private fun load(reset: Boolean) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    items = if (reset) emptyList() else it.items
                )
            }
            socialRepository.getRelationships(
                userId = userId,
                kind = kind,
                cursor = if (reset) null else _state.value.nextCursor
            ).onSuccess { page ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        items = if (reset) page.items else it.items + page.items,
                        nextCursor = page.nextCursor,
                        hasMore = page.hasMore
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(isLoading = false, error = error.message ?: context.getString(R.string.load_failed))
                }
            }
        }
    }
}

data class SocialBlockListUiState(
    val items: List<SocialUser> = emptyList(),
    val nextCursor: String? = null,
    val hasMore: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SocialBlockListViewModel @Inject constructor(
    private val socialRepository: SocialRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(SocialBlockListUiState(isLoading = true))
    val state: StateFlow<SocialBlockListUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<SocialUiEvent>()
    val events: SharedFlow<SocialUiEvent> = _events.asSharedFlow()

    init {
        refresh()
    }

    fun refresh() = load(true)

    fun loadMore() {
        if (_state.value.isLoading || !_state.value.hasMore) return
        load(false)
    }

    fun unblock(userId: String) {
        viewModelScope.launch {
            socialRepository.unblock(userId)
                .onSuccess { refresh() }
                .onFailure { error ->
                    _events.emit(
                        SocialUiEvent.ShowMessage(
                            error.message ?: context.getString(R.string.messages_action_failed)
                        )
                    )
                }
        }
    }

    private fun load(reset: Boolean) {
        viewModelScope.launch {
            _state.update {
                it.copy(isLoading = true, error = null, items = if (reset) emptyList() else it.items)
            }
            socialRepository.getBlocks(cursor = if (reset) null else _state.value.nextCursor)
                .onSuccess { page ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            items = if (reset) page.items else it.items + page.items,
                            nextCursor = page.nextCursor,
                            hasMore = page.hasMore
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(isLoading = false, error = error.message ?: context.getString(R.string.load_failed))
                    }
                }
        }
    }
}

data class ConversationListUiState(
    val items: List<Conversation> = emptyList(),
    val nextCursor: String? = null,
    val hasMore: Boolean = false,
    val unreadTotal: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ConversationListViewModel @Inject constructor(
    private val socialRepository: SocialRepository,
    private val socialSessionCoordinator: SocialSessionCoordinator,
    private val realtimeManager: SocialRealtimeManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(ConversationListUiState(isLoading = true))
    val state: StateFlow<ConversationListUiState> = _state.asStateFlow()

    private var pollJob: Job? = null

    init {
        refresh()
        viewModelScope.launch {
            socialSessionCoordinator.unreadTotal.collect { total ->
                _state.update { it.copy(unreadTotal = total) }
            }
        }
        viewModelScope.launch {
            realtimeManager.events.collect { event ->
                when (event) {
                    is SocialRealtimeEvent.MessageCreated,
                    is SocialRealtimeEvent.ConversationRead,
                    SocialRealtimeEvent.Ready,
                    SocialRealtimeEvent.SocialChanged -> refresh()
                }
            }
        }
        startPolling()
    }

    fun refresh() {
        load(true)
        socialSessionCoordinator.refreshUnread()
    }

    fun loadMore() {
        if (_state.value.isLoading || !_state.value.hasMore) return
        load(false)
    }

    fun onVisible(visible: Boolean) {
        if (visible) startPolling() else pollJob?.cancel()
    }

    private fun startPolling() {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (isActive) {
                delay(30_000)
                refresh()
            }
        }
    }

    private fun load(reset: Boolean) {
        viewModelScope.launch {
            _state.update {
                it.copy(isLoading = true, error = null, items = if (reset) emptyList() else it.items)
            }
            socialRepository.getConversations(cursor = if (reset) null else _state.value.nextCursor)
                .onSuccess { page ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            items = if (reset) page.items else it.items + page.items,
                            nextCursor = page.nextCursor,
                            hasMore = page.hasMore
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(isLoading = false, error = error.message ?: context.getString(R.string.load_failed))
                    }
                }
        }
    }
}

data class ChatUiState(
    val conversation: Conversation? = null,
    val messages: List<ChatMessage> = emptyList(),
    val draft: String = "",
    val nextOlderCursor: String? = null,
    val hasOlder: Boolean = false,
    val isLoading: Boolean = true,
    val isSending: Boolean = false,
    val isPreparingImage: Boolean = false,
    val imagePreviewPath: String? = null,
    val imagePreviewClientId: String? = null,
    val error: String? = null,
    val selfId: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val socialRepository: SocialRepository,
    private val socialSessionCoordinator: SocialSessionCoordinator,
    private val chatImageCache: SocialChatImageCache,
    private val sessionManager: SessionManager,
    private val realtimeManager: SocialRealtimeManager,
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val conversationId: String =
        savedStateHandle.get<String>(Routes.SOCIAL_CONVERSATION_ID).orEmpty()

    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<SocialUiEvent>()
    val events: SharedFlow<SocialUiEvent> = _events.asSharedFlow()

    private var pollJob: Job? = null
    private var pageEpoch = 0L
    private var pageVisible = true
    private val sessionToken = sessionManager.currentToken()
    private val sessionUsername = sessionManager.currentUsername()
    private val sessionVersion = socialSessionCoordinator.sessionVersion.value
    private val ownedImageIds = mutableSetOf<String>()

    private fun isCurrent(epoch: Long): Boolean =
        pageVisible && pageEpoch == epoch && !sessionToken.isNullOrBlank() &&
            sessionManager.currentToken() == sessionToken &&
            sessionManager.currentUsername() == sessionUsername &&
            socialSessionCoordinator.sessionVersion.value == sessionVersion

    private fun clearLocalImages() {
        pageEpoch += 1
        ownedImageIds.forEach(chatImageCache::delete)
        ownedImageIds.clear()
        _state.update {
            it.copy(
                imagePreviewPath = null,
                imagePreviewClientId = null,
                isPreparingImage = false,
                isSending = false,
                messages = it.messages.filterNot { message ->
                    message.type == ChatMessageType.IMAGE &&
                        !ChatMessageMerge.isCommittedServerMessage(message)
                }.map { message -> message.copy(localImagePath = null) }
            )
        }
    }

    private fun isConfirmed(senderId: String, clientMessageId: String): Boolean =
        _state.value.messages.any {
            it.conversationId == conversationId && it.senderId == senderId &&
                it.clientMessageId == clientMessageId && ChatMessageMerge.isCommittedServerMessage(it)
        }

    private fun completeLocalImage(clientMessageId: String) {
        chatImageCache.delete(clientMessageId)
        ownedImageIds -= clientMessageId
        _state.update { state ->
            state.copy(messages = state.messages.map { message ->
                if (message.clientMessageId == clientMessageId && message.senderId == state.selfId &&
                    ChatMessageMerge.isCommittedServerMessage(message)) {
                    message.copy(localImagePath = null)
                } else message
            })
        }
    }

    init {
        viewModelScope.launch {
            socialSessionCoordinator.sessionVersion.collect { version ->
                if (version != sessionVersion) {
                    pageVisible = false
                    pollJob?.cancel()
                    clearLocalImages()
                    _state.value = ChatUiState(isLoading = false)
                }
            }
        }
        refreshAll()
        viewModelScope.launch {
            val epoch = pageEpoch
            socialRepository.getMe().onSuccess { me ->
                if (!isCurrent(epoch)) return@onSuccess
                _state.update { it.copy(selfId = me.id) }
            }
        }
        viewModelScope.launch {
            realtimeManager.events.collect { event ->
                when (event) {
                    is SocialRealtimeEvent.MessageCreated -> {
                        if (event.conversationId == conversationId) {
                            pullNewer()
                        }
                    }
                    is SocialRealtimeEvent.ConversationRead -> {
                        if (event.conversationId == conversationId) {
                            refreshConversation()
                        }
                    }
                    SocialRealtimeEvent.Ready, SocialRealtimeEvent.SocialChanged -> refreshConversation()
                }
            }
        }
        startPolling()
    }

    fun updateDraft(value: String) {
        _state.update { it.copy(draft = value) }
    }

    fun refreshAll() {
        if (!isCurrent(pageEpoch)) return
        viewModelScope.launch {
            val epoch = pageEpoch
            _state.update { it.copy(isLoading = true, error = null) }
            val conversationResult = socialRepository.getConversation(conversationId)
            if (!isCurrent(epoch)) return@launch
            val messagesResult = socialRepository.getMessages(conversationId)
            conversationResult.onSuccess { conversation ->
                if (!isCurrent(epoch)) return@onSuccess
                _state.update { it.copy(conversation = conversation) }
            }
            messagesResult.onSuccess { page ->
                if (!isCurrent(epoch)) return@onSuccess
                mergeMessages(page.items, prepend = false, replace = true)
                _state.update {
                    it.copy(
                        isLoading = false,
                        nextOlderCursor = page.nextCursor,
                        hasOlder = page.hasMore
                    )
                }
                markLatestRead()
            }.onFailure { error ->
                if (!isCurrent(epoch)) return@onFailure
                _state.update {
                    it.copy(isLoading = false, error = error.message ?: context.getString(R.string.load_failed))
                }
            }
        }
    }

    fun loadOlder() {
        if (!isCurrent(pageEpoch)) return
        val cursor = _state.value.nextOlderCursor ?: return
        if (_state.value.isLoading || !_state.value.hasOlder) return
        viewModelScope.launch {
            val epoch = pageEpoch
            socialRepository.getMessages(conversationId, beforeSeq = cursor)
                .onSuccess { page ->
                    if (!isCurrent(epoch)) return@onSuccess
                    mergeMessages(page.items, prepend = true, replace = false)
                    _state.update {
                        it.copy(nextOlderCursor = page.nextCursor, hasOlder = page.hasMore)
                    }
                }
        }
    }

    fun send() {
        if (!isCurrent(pageEpoch)) return
        val draft = SocialTextSupport.normalizeMessageContent(_state.value.draft)
        val conversation = _state.value.conversation
        if (conversation == null || !conversation.canSend) {
            viewModelScope.launch {
                val epoch = pageEpoch
                _events.emit(
                    SocialUiEvent.ShowMessage(
                        conversation?.sendPermissionReason
                            ?: context.getString(R.string.social_error_privacy_restricted)
                    )
                )
            }
            return
        }
        if (_state.value.selfId.isNullOrBlank() || !SocialTextSupport.isValidMessageContent(draft) || _state.value.isSending || _state.value.isLoading) return
        val clientMessageId = UUID.randomUUID().toString()
        val pending = ChatMessage(
            id = "local-$clientMessageId",
            conversationId = conversationId,
            seq = "",
            senderId = _state.value.selfId.orEmpty(),
            clientMessageId = clientMessageId,
            content = draft,
            createdAt = "",
            sendStatus = ChatSendStatus.PENDING,
            type = ChatMessageType.TEXT
        )
        _state.update {
            it.copy(
                draft = "",
                isSending = true,
                messages = it.messages + pending
            )
        }
        viewModelScope.launch {
            val epoch = pageEpoch
            val selfId = _state.value.selfId.orEmpty()
            socialRepository.sendMessage(conversationId, draft, clientMessageId)
                .onSuccess { sent ->
                    if (!isCurrent(epoch)) return@onSuccess
                    replacePending(selfId, clientMessageId, sent.copy(sendStatus = ChatSendStatus.SENT))
                    _state.update { it.copy(isSending = false) }
                    refreshConversation()
                    socialSessionCoordinator.refreshUnread()
                }
                .onFailure { error ->
                    if (!isCurrent(epoch)) return@onFailure
                    markFailed(selfId, clientMessageId)
                    _state.update { it.copy(isSending = false) }
                    if ((error as? AppException)?.errorCode == "PRIVACY_RESTRICTED") {
                        refreshConversation()
                        _state.update { it.copy(draft = draft) }
                    }
                    _events.emit(SocialUiEvent.ShowMessage(error.message ?: context.getString(R.string.social_send_failed)))
                }
        }
    }

    fun onImagePicked(uri: Uri) {
        if (!isCurrent(pageEpoch)) return
        val conversation = _state.value.conversation
        if (conversation == null || !conversation.canSend || !conversation.imageMessagingEnabled) {
            viewModelScope.launch {
                val epoch = pageEpoch
                _events.emit(
                    SocialUiEvent.ShowMessage(
                        if (conversation?.imageMessagingEnabled != true) {
                            context.getString(R.string.social_image_disabled)
                        } else {
                            conversation.sendPermissionReason
                                ?: context.getString(R.string.social_error_privacy_restricted)
                        }
                    )
                )
            }
            return
        }
        if (_state.value.isSending || _state.value.isPreparingImage || _state.value.isLoading) return
        viewModelScope.launch {
            val epoch = pageEpoch
            _state.update { it.copy(isPreparingImage = true) }
            socialRepository.prepareChatImage(uri)
                .onSuccess { prepared ->
                    if (!isCurrent(epoch)) return@onSuccess
                    val clientMessageId = UUID.randomUUID().toString()
                    val file = chatImageCache.write(clientMessageId, prepared.bytes)
                    ownedImageIds += clientMessageId
                    _state.update {
                        it.copy(
                            isPreparingImage = false,
                            imagePreviewPath = file.absolutePath,
                            imagePreviewClientId = clientMessageId
                        )
                    }
                }
                .onFailure { error ->
                    if (!isCurrent(epoch)) return@onFailure
                    _state.update { it.copy(isPreparingImage = false) }
                    _events.emit(
                        SocialUiEvent.ShowMessage(
                            error.message ?: context.getString(R.string.social_error_invalid_image)
                        )
                    )
                }
        }
    }

    fun cancelImagePreview() {
        val clientId = _state.value.imagePreviewClientId
        if (!clientId.isNullOrBlank()) {
            chatImageCache.delete(clientId)
            ownedImageIds -= clientId
        }
        _state.update { it.copy(imagePreviewPath = null, imagePreviewClientId = null) }
    }

    fun confirmSendImage() {
        if (!isCurrent(pageEpoch)) return
        val previewPath = _state.value.imagePreviewPath ?: return
        val clientMessageId = _state.value.imagePreviewClientId ?: return
        val conversation = _state.value.conversation
        if (conversation == null || !conversation.canSend || !conversation.imageMessagingEnabled) {
            viewModelScope.launch {
                val epoch = pageEpoch
                _events.emit(
                    SocialUiEvent.ShowMessage(
                        if (conversation?.imageMessagingEnabled != true) {
                            context.getString(R.string.social_image_disabled)
                        } else {
                            conversation.sendPermissionReason
                                ?: context.getString(R.string.social_error_privacy_restricted)
                        }
                    )
                )
            }
            return
        }
        if (_state.value.selfId.isNullOrBlank() || _state.value.isSending || _state.value.isLoading) return
        val bytes = chatImageCache.read(clientMessageId) ?: return
        val pending = ChatMessage(
            id = "local-$clientMessageId",
            conversationId = conversationId,
            seq = "",
            senderId = _state.value.selfId.orEmpty(),
            clientMessageId = clientMessageId,
            content = "",
            createdAt = "",
            sendStatus = ChatSendStatus.PENDING,
            type = ChatMessageType.IMAGE,
            image = ChatImageMeta(
                width = null,
                height = null,
                size = bytes.size.toLong(),
                contentType = "image/jpeg"
            ),
            localImagePath = previewPath
        )
        _state.update {
            it.copy(
                imagePreviewPath = null,
                imagePreviewClientId = null,
                isSending = true,
                messages = it.messages + pending
            )
        }
        viewModelScope.launch {
            val epoch = pageEpoch
            uploadImage(clientMessageId, bytes, epoch)
        }
    }

    fun retry(message: ChatMessage) {
        if (!isCurrent(pageEpoch)) return
        if (message.sendStatus != ChatSendStatus.FAILED) return
        // 已提交不可重试降级路径；仅本地 failed 可重试
        if (ChatMessageMerge.isCommittedServerMessage(message)) return
        val clientMessageId = message.clientMessageId.ifBlank { return }
        val senderId = message.senderId.ifBlank { _state.value.selfId.orEmpty() }
        if (senderId.isBlank()) return
        _state.update { state ->
            state.copy(
                messages = ChatMessageMerge.markPendingRetry(
                    messages = state.messages,
                    senderId = senderId,
                    clientMessageId = clientMessageId,
                    conversationId = conversationId
                )
            )
        }
        viewModelScope.launch {
            val epoch = pageEpoch
            if (message.type == ChatMessageType.IMAGE) {
                val bytes = message.localImagePath?.let { path ->
                    runCatching { java.io.File(path).readBytes() }.getOrNull()
                } ?: chatImageCache.read(clientMessageId)
                if (bytes == null || bytes.isEmpty()) {
                    markFailed(senderId, clientMessageId)
                    _events.emit(SocialUiEvent.ShowMessage(context.getString(R.string.social_error_invalid_image)))
                    return@launch
                }
                // 同 ID 原 bytes 重试：不因当前 canSend/imageMessagingEnabled 误挡已提交确认
                socialRepository.sendImageMessage(conversationId, bytes, clientMessageId)
                    .onSuccess { sent ->
                        if (!isCurrent(epoch)) return@onSuccess
                        replacePending(senderId, clientMessageId, sent.copy(sendStatus = ChatSendStatus.SENT))
                        completeLocalImage(clientMessageId)
                        refreshConversation()
                    }
                    .onFailure { error ->
                        if (!isCurrent(epoch)) return@onFailure
                        if (isConfirmed(senderId, clientMessageId)) {
                            completeLocalImage(clientMessageId)
                            return@onFailure
                        }
                        markFailed(senderId, clientMessageId)
                        _events.emit(
                            SocialUiEvent.ShowMessage(
                                error.message ?: context.getString(R.string.social_send_failed)
                            )
                        )
                    }
            } else {
                socialRepository.sendMessage(conversationId, message.content, clientMessageId)
                    .onSuccess { sent ->
                        if (!isCurrent(epoch)) return@onSuccess
                        replacePending(senderId, clientMessageId, sent.copy(sendStatus = ChatSendStatus.SENT))
                        refreshConversation()
                    }
                    .onFailure { error ->
                        if (!isCurrent(epoch)) return@onFailure
                        markFailed(senderId, clientMessageId)
                        _events.emit(
                            SocialUiEvent.ShowMessage(
                                error.message ?: context.getString(R.string.social_send_failed)
                            )
                        )
                    }
            }
        }
    }

    private suspend fun uploadImage(clientMessageId: String, bytes: ByteArray, epoch: Long) {
        if (!isCurrent(epoch)) return
        val selfId = _state.value.selfId.orEmpty()
        socialRepository.sendImageMessage(conversationId, bytes, clientMessageId)
            .onSuccess { sent ->
                if (!isCurrent(epoch)) return@onSuccess
                replacePending(selfId, clientMessageId, sent.copy(sendStatus = ChatSendStatus.SENT))
                completeLocalImage(clientMessageId)
                _state.update { it.copy(isSending = false) }
                refreshConversation()
                socialSessionCoordinator.refreshUnread()
            }
            .onFailure { error ->
                if (!isCurrent(epoch)) return@onFailure
                if (isConfirmed(selfId, clientMessageId)) {
                    completeLocalImage(clientMessageId)
                    _state.update { it.copy(isSending = false) }
                    return@onFailure
                }
                markFailed(selfId, clientMessageId)
                _state.update { it.copy(isSending = false) }
                if ((error as? AppException)?.errorCode == "PRIVACY_RESTRICTED") {
                    refreshConversation()
                }
                _events.emit(SocialUiEvent.ShowMessage(error.message ?: context.getString(R.string.social_send_failed)))
            }
    }

    fun onVisible(visible: Boolean) {
        pageVisible = visible
        if (visible) {
            if (!isCurrent(pageEpoch)) return
            startPolling()
            pullNewer()
        } else {
            pollJob?.cancel()
            clearLocalImages()
        }
    }

    override fun onCleared() {
        pageVisible = false
        pollJob?.cancel()
        clearLocalImages()
        super.onCleared()
    }

    private fun startPolling() {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (isActive) {
                delay(10_000)
                pullNewer()
            }
        }
    }

    private fun pullNewer() {
        if (!isCurrent(pageEpoch)) return
        viewModelScope.launch {
            val epoch = pageEpoch
            val afterSeq = _state.value.messages
                .filter { it.sendStatus == ChatSendStatus.SENT && it.seq.isNotBlank() }
                .maxByOrNull { it.seq.toLongOrNull() ?: -1L }
                ?.seq
            socialRepository.getMessages(conversationId, afterSeq = afterSeq)
                .onSuccess { page ->
                    if (!isCurrent(epoch)) return@onSuccess
                    mergeMessages(page.items, prepend = false, replace = false)
                    markLatestRead()
                    refreshConversation()
                    socialSessionCoordinator.refreshUnread()
                }
        }
    }

    private fun refreshConversation() {
        if (!isCurrent(pageEpoch)) return
        viewModelScope.launch {
            val epoch = pageEpoch
            socialRepository.getConversation(conversationId).onSuccess { conversation ->
                if (!isCurrent(epoch)) return@onSuccess
                _state.update { it.copy(conversation = conversation) }
            }
        }
    }

    private fun mergeMessages(incoming: List<ChatMessage>, prepend: Boolean, replace: Boolean) {
        _state.update { state ->
            state.copy(
                messages = ChatMessageMerge.merge(
                    existing = state.messages,
                    incoming = incoming,
                    prepend = prepend,
                    replace = replace
                )
            )
        }
    }

    private fun replacePending(senderId: String, clientMessageId: String, sent: ChatMessage) {
        _state.update { state ->
            state.copy(
                messages = ChatMessageMerge.replacePending(
                    messages = state.messages,
                    senderId = senderId,
                    clientMessageId = clientMessageId,
                    sent = sent,
                    conversationId = conversationId
                )
            )
        }
    }

    private fun markFailed(senderId: String, clientMessageId: String) {
        _state.update { state ->
            state.copy(
                messages = ChatMessageMerge.markFailed(
                    messages = state.messages,
                    senderId = senderId,
                    clientMessageId = clientMessageId,
                    conversationId = conversationId
                )
            )
        }
    }

    private fun markLatestRead() {
        if (!isCurrent(pageEpoch)) return
        val latestPeerSeq = _state.value.messages
            .filter {
                it.sendStatus == ChatSendStatus.SENT &&
                    it.senderId.isNotBlank() &&
                    it.senderId != _state.value.selfId &&
                    it.seq.isNotBlank()
            }
            .maxByOrNull { it.seq.toLongOrNull() ?: -1L }
            ?.seq
            ?: return
        viewModelScope.launch {
            val epoch = pageEpoch
            socialRepository.markRead(conversationId, latestPeerSeq).onSuccess { read ->
                if (!isCurrent(epoch)) return@onSuccess
                _state.update { state ->
                    state.copy(
                        conversation = state.conversation?.copy(
                            lastReadSeq = read.lastReadSeq,
                            unreadCount = read.unreadCount
                        )
                    )
                }
                socialSessionCoordinator.refreshUnread()
            }
        }
    }
}

data class SocialDmPrivacyUiState(
    val dmPolicy: DmPolicy = DmPolicy.MUTUAL,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SocialDmPrivacyViewModel @Inject constructor(
    private val socialRepository: SocialRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(SocialDmPrivacyUiState())
    val state: StateFlow<SocialDmPrivacyUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<SocialUiEvent>()
    val events: SharedFlow<SocialUiEvent> = _events.asSharedFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            socialRepository.getPrivacy()
                .onSuccess { privacy ->
                    _state.update { it.copy(isLoading = false, dmPolicy = privacy.dmPolicy) }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(isLoading = false, error = error.message ?: context.getString(R.string.load_failed))
                    }
                }
        }
    }

    fun select(policy: DmPolicy) {
        _state.update { it.copy(dmPolicy = policy) }
    }

    fun save() {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            socialRepository.updatePrivacy(_state.value.dmPolicy)
                .onSuccess { privacy ->
                    _state.update { it.copy(isSaving = false, dmPolicy = privacy.dmPolicy) }
                    _events.emit(SocialUiEvent.ShowMessage(context.getString(R.string.social_dm_privacy_saved)))
                }
                .onFailure { error ->
                    _state.update { it.copy(isSaving = false) }
                    _events.emit(
                        SocialUiEvent.ShowMessage(
                            error.message ?: context.getString(R.string.profile_privacy_save_failed)
                        )
                    )
                }
        }
    }
}
