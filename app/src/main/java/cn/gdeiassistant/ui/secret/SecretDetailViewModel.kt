package cn.gdeiassistant.ui.secret

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.R
import cn.gdeiassistant.data.SecretRepository
import cn.gdeiassistant.model.SecretDetail
import cn.gdeiassistant.ui.navigation.Routes
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SecretDetailUiState(
    val detail: SecretDetail? = null,
    val isLoading: Boolean = true,
    val isSubmittingComment: Boolean = false,
    val isSubmittingLike: Boolean = false,
    val error: String? = null
)

sealed interface SecretDetailEvent {
    data class ShowMessage(val message: String) : SecretDetailEvent
}

@HiltViewModel
class SecretDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val secretRepository: SecretRepository
) : ViewModel() {

    private val postId: String = savedStateHandle.get<String>(Routes.SECRET_POST_ID).orEmpty()

    private val _state = MutableStateFlow(SecretDetailUiState())
    val state: StateFlow<SecretDetailUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<SecretDetailEvent>()
    val events: SharedFlow<SecretDetailEvent> = _events.asSharedFlow()

    init {
        refresh()
    }

    fun refresh() {
        if (postId.isBlank()) {
            _state.update { it.copy(isLoading = false, error = context.getString(R.string.secret_missing_id)) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            secretRepository.getDetail(postId)
                .onSuccess { detail ->
                    _state.update {
                        it.copy(
                            detail = detail,
                            isLoading = false,
                            isSubmittingComment = false,
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
                            isSubmittingComment = false,
                            isSubmittingLike = false,
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
                _events.emit(SecretDetailEvent.ShowMessage(context.getString(R.string.secret_toast_comment_required)))
            }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSubmittingComment = true) }
            secretRepository.submitComment(postId, trimmed)
                .onSuccess {
                    _events.emit(SecretDetailEvent.ShowMessage(context.getString(R.string.secret_toast_comment_sent)))
                    refresh()
                }
                .onFailure { error ->
                    _events.emit(
                        SecretDetailEvent.ShowMessage(
                            error.message ?: context.getString(R.string.secret_toast_comment_failed)
                        )
                    )
                    _state.update { it.copy(isSubmittingComment = false) }
                }
        }
    }

    fun toggleLike() {
        val detail = _state.value.detail ?: return
        viewModelScope.launch {
            _state.update { it.copy(isSubmittingLike = true) }
            secretRepository.setLike(postId, liked = !detail.post.isLiked)
                .onSuccess {
                    refresh()
                }
                .onFailure { error ->
                    _events.emit(
                        SecretDetailEvent.ShowMessage(
                            error.message ?: context.getString(R.string.secret_toast_like_failed)
                        )
                    )
                    _state.update { it.copy(isSubmittingLike = false) }
                }
        }
    }
}
