package cn.gdeiassistant.ui.express

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.R
import cn.gdeiassistant.data.ExpressRepository
import cn.gdeiassistant.model.ExpressCommentItem
import cn.gdeiassistant.model.ExpressDraft
import cn.gdeiassistant.model.ExpressGender
import cn.gdeiassistant.model.ExpressPost
import cn.gdeiassistant.model.ExpressPostDetail
import cn.gdeiassistant.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExpressUiState(
    val query: String = "",
    val posts: List<ExpressPost> = emptyList(),
    val myPosts: List<ExpressPost> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

data class ExpressDetailUiState(
    val detail: ExpressPostDetail? = null,
    val comments: List<ExpressCommentItem> = emptyList(),
    val isLoading: Boolean = true,
    val isSubmittingComment: Boolean = false,
    val isSubmittingLike: Boolean = false,
    val isSubmittingGuess: Boolean = false,
    val error: String? = null
)

data class ExpressProfileUiState(
    val items: List<ExpressPost> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

data class ExpressPublishUiState(
    val isSubmitting: Boolean = false,
    val error: String? = null
)

sealed interface ExpressDetailEvent {
    data class ShowMessage(val message: String) : ExpressDetailEvent
}

sealed interface ExpressPublishEvent {
    data class ShowMessage(val message: String) : ExpressPublishEvent
    data object Submitted : ExpressPublishEvent
}

@HiltViewModel
class ExpressViewModel @Inject constructor(
    private val expressRepository: ExpressRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ExpressUiState())
    val state: StateFlow<ExpressUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun updateQuery(value: String) {
        _state.update { it.copy(query = value) }
    }

    fun clearQuery() {
        _state.update { it.copy(query = "") }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val keyword = _state.value.query.trim().takeIf { it.isNotBlank() }
            val postsDeferred = async { expressRepository.getPosts(keyword = keyword) }
            val mineDeferred = async { expressRepository.getMyPosts() }
            val postsResult = postsDeferred.await()
            val myPostsResult = mineDeferred.await()
            val error = postsResult.exceptionOrNull()?.message ?: myPostsResult.exceptionOrNull()?.message
            _state.update {
                it.copy(
                    posts = postsResult.getOrDefault(emptyList()),
                    myPosts = myPostsResult.getOrDefault(emptyList()),
                    isLoading = false,
                    error = error
                )
            }
        }
    }
}

@HiltViewModel
class ExpressDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val expressRepository: ExpressRepository
) : ViewModel() {

    private val postId: String = savedStateHandle.get<String>(Routes.EXPRESS_POST_ID).orEmpty()

    private val _state = MutableStateFlow(ExpressDetailUiState())
    val state: StateFlow<ExpressDetailUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ExpressDetailEvent>()
    val events: SharedFlow<ExpressDetailEvent> = _events.asSharedFlow()

    init {
        refresh()
    }

    fun refresh() {
        if (postId.isBlank()) {
            _state.update { it.copy(isLoading = false, error = context.getString(R.string.express_missing_id)) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val detailDeferred = async { expressRepository.getDetail(postId) }
            val commentsDeferred = async { expressRepository.getComments(postId) }
            val detailResult = detailDeferred.await()
            val commentsResult = commentsDeferred.await()
            detailResult
                .onSuccess { detail ->
                    _state.update {
                        it.copy(
                            detail = detail,
                            comments = commentsResult.getOrDefault(emptyList()),
                            isLoading = false,
                            isSubmittingComment = false,
                            isSubmittingLike = false,
                            isSubmittingGuess = false,
                            error = commentsResult.exceptionOrNull()?.message
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            detail = null,
                            comments = emptyList(),
                            isLoading = false,
                            isSubmittingComment = false,
                            isSubmittingLike = false,
                            isSubmittingGuess = false,
                            error = error.message
                        )
                    }
                }
        }
    }

    fun submitComment(content: String) {
        val trimmed = content.trim()
        if (trimmed.isBlank()) {
            viewModelScope.launch {
                _events.emit(ExpressDetailEvent.ShowMessage(context.getString(R.string.express_toast_comment_required)))
            }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSubmittingComment = true) }
            expressRepository.submitComment(postId, trimmed)
                .onSuccess {
                    _events.emit(ExpressDetailEvent.ShowMessage(context.getString(R.string.express_toast_comment_sent)))
                    refresh()
                }
                .onFailure { error ->
                    _events.emit(
                        ExpressDetailEvent.ShowMessage(
                            error.message ?: context.getString(R.string.express_toast_comment_failed)
                        )
                    )
                    _state.update { it.copy(isSubmittingComment = false) }
                }
        }
    }

    fun like() {
        viewModelScope.launch {
            _state.update { it.copy(isSubmittingLike = true) }
            expressRepository.like(postId)
                .onSuccess { refresh() }
                .onFailure { error ->
                    _events.emit(
                        ExpressDetailEvent.ShowMessage(
                            error.message ?: context.getString(R.string.express_toast_like_failed)
                        )
                    )
                    _state.update { it.copy(isSubmittingLike = false) }
                }
        }
    }

    fun guess(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) {
            viewModelScope.launch {
                _events.emit(ExpressDetailEvent.ShowMessage(context.getString(R.string.express_toast_guess_required)))
            }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSubmittingGuess = true) }
            expressRepository.guess(postId, trimmed)
                .onSuccess { matched ->
                    _events.emit(
                        ExpressDetailEvent.ShowMessage(
                            if (matched) {
                                context.getString(R.string.express_toast_guess_success)
                            } else {
                                context.getString(R.string.express_toast_guess_failed)
                            }
                        )
                    )
                    refresh()
                }
                .onFailure { error ->
                    _events.emit(
                        ExpressDetailEvent.ShowMessage(
                            error.message ?: context.getString(R.string.express_toast_guess_request_failed)
                        )
                    )
                    _state.update { it.copy(isSubmittingGuess = false) }
                }
        }
    }
}

@HiltViewModel
class ExpressProfileViewModel @Inject constructor(
    private val expressRepository: ExpressRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ExpressProfileUiState())
    val state: StateFlow<ExpressProfileUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            expressRepository.getMyPosts()
                .onSuccess { items ->
                    _state.update { it.copy(items = items, isLoading = false, error = null) }
                }
                .onFailure { error ->
                    _state.update { it.copy(items = emptyList(), isLoading = false, error = error.message) }
                }
        }
    }
}

@HiltViewModel
class ExpressPublishViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val expressRepository: ExpressRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ExpressPublishUiState())
    val state: StateFlow<ExpressPublishUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ExpressPublishEvent>()
    val events: SharedFlow<ExpressPublishEvent> = _events.asSharedFlow()

    fun submit(
        nickname: String,
        realName: String,
        selfGender: ExpressGender,
        targetName: String,
        content: String,
        targetGender: ExpressGender
    ) {
        val normalizedNickname = nickname.trim()
        val normalizedTarget = targetName.trim()
        val normalizedContent = content.trim()
        if (normalizedNickname.isBlank()) {
            emitMessage(context.getString(R.string.express_publish_nickname_required))
            return
        }
        if (normalizedTarget.isBlank()) {
            emitMessage(context.getString(R.string.express_publish_target_required))
            return
        }
        if (normalizedContent.isBlank()) {
            emitMessage(context.getString(R.string.express_publish_content_required))
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null) }
            expressRepository.publish(
                ExpressDraft(
                    nickname = normalizedNickname,
                    realName = realName.trim().takeIf(String::isNotBlank),
                    selfGender = selfGender,
                    targetName = normalizedTarget,
                    content = normalizedContent,
                    targetGender = targetGender
                )
            ).onSuccess {
                _state.update { it.copy(isSubmitting = false, error = null) }
                _events.emit(ExpressPublishEvent.ShowMessage(context.getString(R.string.express_publish_success)))
                _events.emit(ExpressPublishEvent.Submitted)
            }.onFailure { error ->
                _state.update { it.copy(isSubmitting = false, error = error.message) }
                emitMessage(error.message ?: context.getString(R.string.express_publish_failed))
            }
        }
    }

    private fun emitMessage(message: String) {
        viewModelScope.launch {
            _events.emit(ExpressPublishEvent.ShowMessage(message))
        }
    }
}
