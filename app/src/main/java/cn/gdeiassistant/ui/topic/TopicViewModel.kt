package cn.gdeiassistant.ui.topic

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.R
import cn.gdeiassistant.data.TopicRepository
import cn.gdeiassistant.data.mapper.TopicDisplayMapper
import cn.gdeiassistant.model.TopicDraft
import cn.gdeiassistant.model.TopicPost
import cn.gdeiassistant.model.TopicPostDetail
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

data class TopicUiState(
    val query: String = "",
    val posts: List<TopicPost> = emptyList(),
    val myPosts: List<TopicPost> = emptyList(),
    val imageCount: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)

data class TopicDetailUiState(
    val detail: TopicPostDetail? = null,
    val isLoading: Boolean = true,
    val isSubmittingLike: Boolean = false,
    val error: String? = null
)

data class TopicProfileUiState(
    val items: List<TopicPost> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

data class TopicPublishUiState(
    val isSubmitting: Boolean = false,
    val error: String? = null
)

sealed interface TopicDetailEvent {
    data class ShowMessage(val message: String) : TopicDetailEvent
}

sealed interface TopicPublishEvent {
    data class ShowMessage(val message: String) : TopicPublishEvent
    data object Submitted : TopicPublishEvent
}

@HiltViewModel
class TopicViewModel @Inject constructor(
    private val topicRepository: TopicRepository,
    private val displayMapper: TopicDisplayMapper
) : ViewModel() {

    private val _state = MutableStateFlow(TopicUiState())
    val state: StateFlow<TopicUiState> = _state.asStateFlow()

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
            val postsDeferred = async { topicRepository.getPosts(keyword = keyword) }
            val mineDeferred = async { topicRepository.getMyPosts() }
            val postsResult = postsDeferred.await()
            val myPostsResult = mineDeferred.await()
            val posts = displayMapper.applyPostDefaults(postsResult.getOrDefault(emptyList()))
            val myPosts = displayMapper.applyPostDefaults(myPostsResult.getOrDefault(emptyList()))
            val error = postsResult.exceptionOrNull()?.message ?: myPostsResult.exceptionOrNull()?.message
            _state.update {
                it.copy(
                    posts = posts,
                    myPosts = myPosts,
                    imageCount = posts.sumOf { item -> item.imageCount },
                    isLoading = false,
                    error = error
                )
            }
        }
    }
}

@HiltViewModel
class TopicDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val topicRepository: TopicRepository,
    private val displayMapper: TopicDisplayMapper
) : ViewModel() {

    private val postId: String = savedStateHandle.get<String>(Routes.TOPIC_POST_ID).orEmpty()

    private val _state = MutableStateFlow(TopicDetailUiState())
    val state: StateFlow<TopicDetailUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<TopicDetailEvent>()
    val events: SharedFlow<TopicDetailEvent> = _events.asSharedFlow()

    init {
        refresh()
    }

    fun refresh() {
        if (postId.isBlank()) {
            _state.update { it.copy(isLoading = false, error = context.getString(R.string.topic_missing_id)) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            topicRepository.getDetail(postId)
                .onSuccess { detail ->
                    _state.update {
                        it.copy(
                            detail = displayMapper.applyDetailDefaults(detail),
                            isLoading = false,
                            isSubmittingLike = false,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            detail = null,
                            isLoading = false,
                            isSubmittingLike = false,
                            error = error.message
                        )
                    }
                }
        }
    }

    fun like() {
        viewModelScope.launch {
            _state.update { it.copy(isSubmittingLike = true) }
            topicRepository.like(postId)
                .onSuccess { refresh() }
                .onFailure { error ->
                    _events.emit(
                        TopicDetailEvent.ShowMessage(
                            error.message ?: context.getString(R.string.topic_toast_like_failed)
                        )
                    )
                    _state.update { it.copy(isSubmittingLike = false) }
                }
        }
    }
}

@HiltViewModel
class TopicProfileViewModel @Inject constructor(
    private val topicRepository: TopicRepository,
    private val displayMapper: TopicDisplayMapper
) : ViewModel() {

    private val _state = MutableStateFlow(TopicProfileUiState())
    val state: StateFlow<TopicProfileUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            topicRepository.getMyPosts()
                .onSuccess { items ->
                    _state.update { it.copy(items = displayMapper.applyPostDefaults(items), isLoading = false, error = null) }
                }
                .onFailure { error ->
                    _state.update { it.copy(items = emptyList(), isLoading = false, error = error.message) }
                }
        }
    }
}

@HiltViewModel
class TopicPublishViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val topicRepository: TopicRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TopicPublishUiState())
    val state: StateFlow<TopicPublishUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<TopicPublishEvent>()
    val events: SharedFlow<TopicPublishEvent> = _events.asSharedFlow()

    fun submit(topic: String, content: String, images: List<Uri>) {
        val normalizedTopic = topic.trim()
        val normalizedContent = content.trim()
        if (normalizedTopic.isBlank()) {
            emitMessage(context.getString(R.string.topic_publish_topic_required))
            return
        }
        if (normalizedContent.isBlank()) {
            emitMessage(context.getString(R.string.topic_publish_content_required))
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null) }
            topicRepository.publish(
                draft = TopicDraft(
                    topic = normalizedTopic,
                    content = normalizedContent
                ),
                images = images
            ).onSuccess {
                _state.update { it.copy(isSubmitting = false, error = null) }
                _events.emit(TopicPublishEvent.ShowMessage(context.getString(R.string.topic_publish_success)))
                _events.emit(TopicPublishEvent.Submitted)
            }.onFailure { error ->
                _state.update { it.copy(isSubmitting = false, error = error.message) }
                emitMessage(error.message ?: context.getString(R.string.topic_publish_failed))
            }
        }
    }

    private fun emitMessage(message: String) {
        viewModelScope.launch {
            _events.emit(TopicPublishEvent.ShowMessage(message))
        }
    }
}
