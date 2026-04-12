package cn.gdeiassistant.ui.photograph

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.R
import cn.gdeiassistant.data.PhotographRepository
import cn.gdeiassistant.model.PhotographCategory
import cn.gdeiassistant.model.PhotographCommentItem
import cn.gdeiassistant.model.PhotographPost
import cn.gdeiassistant.model.PhotographPostDetail
import cn.gdeiassistant.model.PhotographStats
import cn.gdeiassistant.model.SelectedLocalImage
import cn.gdeiassistant.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PHOTOGRAPH_CONTENT_MAX_LENGTH = 50

data class PhotographUiState(
    val selectedCategory: PhotographCategory = PhotographCategory.CAMPUS,
    val stats: PhotographStats = PhotographStats(0, 0, 0),
    val posts: List<PhotographPost> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

data class PhotographDetailUiState(
    val detail: PhotographPostDetail? = null,
    val comments: List<PhotographCommentItem> = emptyList(),
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val error: String? = null
)

data class PhotographProfileUiState(
    val items: List<PhotographPost> = emptyList(),
    val selectedCategory: PhotographCategory? = null,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val visibleItems: List<PhotographPost>
        get() = selectedCategory?.let { category -> items.filter { it.category == category } } ?: items
}

data class PhotographPublishUiState(
    val images: List<SelectedLocalImage> = emptyList(),
    val isSubmitting: Boolean = false
)

sealed interface PhotographEvent {
    data class ShowMessage(val message: String) : PhotographEvent
    data object Published : PhotographEvent
}

@HiltViewModel
class PhotographViewModel @Inject constructor(
    private val photographRepository: PhotographRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PhotographUiState())
    val state: StateFlow<PhotographUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun selectCategory(category: PhotographCategory) {
        if (_state.value.selectedCategory == category) return
        _state.update { it.copy(selectedCategory = category) }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val category = _state.value.selectedCategory
            val statsResult = photographRepository.getStats()
            val postsResult = photographRepository.getPosts(category)
            _state.update {
                it.copy(
                    stats = statsResult.getOrDefault(PhotographStats(0, 0, 0)),
                    posts = postsResult.getOrDefault(emptyList()),
                    isLoading = false,
                    error = postsResult.exceptionOrNull()?.message ?: statsResult.exceptionOrNull()?.message
                )
            }
        }
    }
}

@HiltViewModel
class PhotographDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val photographRepository: PhotographRepository
) : ViewModel() {

    private val postId: String = savedStateHandle.get<String>(Routes.PHOTOGRAPH_POST_ID).orEmpty()

    private val _state = MutableStateFlow(PhotographDetailUiState())
    val state: StateFlow<PhotographDetailUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<PhotographEvent>()
    val events: SharedFlow<PhotographEvent> = _events.asSharedFlow()

    init {
        refresh()
    }

    fun refresh() {
        if (postId.isBlank()) {
            _state.update { it.copy(isLoading = false, error = context.getString(R.string.photograph_missing_id)) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            photographRepository.getDetail(postId)
                .onSuccess { detail ->
                    val comments = if (detail.comments.isNotEmpty()) {
                        detail.comments
                    } else {
                        photographRepository.getComments(postId).getOrDefault(emptyList())
                    }
                    _state.update {
                        it.copy(
                            detail = detail,
                            comments = comments,
                            isLoading = false,
                            isSubmitting = false,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            detail = null,
                            comments = emptyList(),
                            isLoading = false,
                            isSubmitting = false,
                            error = error.message
                        )
                    }
                }
        }
    }

    fun like() {
        if (postId.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }
            photographRepository.like(postId)
                .onSuccess { refresh() }
                .onFailure { error ->
                    _events.emit(
                        PhotographEvent.ShowMessage(
                            error.message ?: context.getString(R.string.photograph_toast_like_failed)
                        )
                    )
                    _state.update { it.copy(isSubmitting = false) }
                }
        }
    }

    fun submitComment(content: String) {
        val trimmed = content.trim()
        if (trimmed.isBlank()) {
            viewModelScope.launch {
                _events.emit(PhotographEvent.ShowMessage(context.getString(R.string.photograph_toast_comment_required)))
            }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }
            photographRepository.submitComment(postId, trimmed)
                .onSuccess {
                    _events.emit(PhotographEvent.ShowMessage(context.getString(R.string.photograph_toast_comment_sent)))
                    refresh()
                }
                .onFailure { error ->
                    _events.emit(
                        PhotographEvent.ShowMessage(
                            error.message ?: context.getString(R.string.photograph_toast_comment_failed)
                        )
                    )
                    _state.update { it.copy(isSubmitting = false) }
                }
        }
    }
}

@HiltViewModel
class PhotographProfileViewModel @Inject constructor(
    private val photographRepository: PhotographRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PhotographProfileUiState())
    val state: StateFlow<PhotographProfileUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun selectCategory(category: PhotographCategory?) {
        _state.update { it.copy(selectedCategory = category) }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            photographRepository.getMyPosts()
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
class PhotographPublishViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val photographRepository: PhotographRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PhotographPublishUiState())
    val state: StateFlow<PhotographPublishUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<PhotographEvent>()
    val events: SharedFlow<PhotographEvent> = _events.asSharedFlow()

    fun setImages(images: List<SelectedLocalImage>) {
        _state.update { it.copy(images = images.take(4)) }
    }

    fun publish(
        title: String,
        content: String,
        category: PhotographCategory
    ) {
        val trimmedTitle = title.trim()
        val trimmedContent = content.trim()
        when {
            trimmedTitle.isBlank() -> emitMessage(context.getString(R.string.photograph_publish_title_required))
            trimmedTitle.length > 25 -> emitMessage(context.getString(R.string.photograph_publish_title_limit))
            trimmedContent.length > PHOTOGRAPH_CONTENT_MAX_LENGTH -> emitMessage(context.getString(R.string.photograph_publish_content_limit))
            _state.value.images.isEmpty() -> emitMessage(context.getString(R.string.photograph_publish_image_required))
            _state.value.images.size > 4 -> emitMessage(context.getString(R.string.photograph_publish_image_limit))
            else -> {
                viewModelScope.launch {
                    _state.update { it.copy(isSubmitting = true) }
                    photographRepository.publish(
                        title = trimmedTitle,
                        content = trimmedContent,
                        category = category,
                        images = _state.value.images.map { it.uri }
                    )
                        .onSuccess {
                            _state.update { it.copy(isSubmitting = false, images = emptyList()) }
                            _events.emit(PhotographEvent.ShowMessage(context.getString(R.string.photograph_publish_success)))
                            _events.emit(PhotographEvent.Published)
                        }
                        .onFailure { error ->
                            _state.update { it.copy(isSubmitting = false) }
                            _events.emit(
                                PhotographEvent.ShowMessage(
                                    error.message ?: context.getString(R.string.photograph_publish_failed)
                                )
                            )
                        }
                }
            }
        }
    }

    private fun emitMessage(message: String) {
        viewModelScope.launch {
            _events.emit(PhotographEvent.ShowMessage(message))
        }
    }
}
