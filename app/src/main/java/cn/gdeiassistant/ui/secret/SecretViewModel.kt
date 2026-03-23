package cn.gdeiassistant.ui.secret

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.R
import cn.gdeiassistant.data.SecretRepository
import cn.gdeiassistant.data.mapper.SecretDisplayMapper
import cn.gdeiassistant.model.SecretDraft
import cn.gdeiassistant.model.SecretDraftMode
import cn.gdeiassistant.model.SecretPost
import cn.gdeiassistant.model.SecretVoiceDraft
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

data class SecretUiState(
    val posts: List<SecretPost> = emptyList(),
    val myPosts: List<SecretPost> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

data class SecretPublishUiState(
    val isSubmitting: Boolean = false,
    val error: String? = null
)

sealed interface SecretPublishEvent {
    data class ShowMessage(val message: String) : SecretPublishEvent
    data object Submitted : SecretPublishEvent
}

@HiltViewModel
class SecretViewModel @Inject constructor(
    private val secretRepository: SecretRepository,
    private val displayMapper: SecretDisplayMapper
) : ViewModel() {

    private val _state = MutableStateFlow(SecretUiState())
    val state: StateFlow<SecretUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val postsDeferred = async { secretRepository.getPosts() }
            val mineDeferred = async { secretRepository.getMyPosts() }
            val postsResult = postsDeferred.await().map { displayMapper.applyPostDefaults(it) }
            val myPostsResult = mineDeferred.await().map { displayMapper.applyPostDefaults(it) }
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
class SecretPublishViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val secretRepository: SecretRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SecretPublishUiState())
    val state: StateFlow<SecretPublishUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<SecretPublishEvent>()
    val events: SharedFlow<SecretPublishEvent> = _events.asSharedFlow()

    fun submit(
        content: String,
        themeId: Int,
        timerEnabled: Boolean,
        mode: SecretDraftMode,
        voiceUri: Uri?
    ) {
        val normalizedContent = content.trim()
        if (mode == SecretDraftMode.TEXT && normalizedContent.isBlank()) {
            emitMessage(context.getString(R.string.secret_publish_content_required))
            return
        }
        if (mode == SecretDraftMode.VOICE && voiceUri == null) {
            emitMessage(context.getString(R.string.secret_publish_voice_required))
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null) }
            secretRepository.publish(
                SecretDraft(
                    title = normalizedContent.take(18),
                    content = normalizedContent.takeIf { mode == SecretDraftMode.TEXT },
                    themeId = themeId,
                    timerEnabled = timerEnabled,
                    mode = mode,
                    voice = voiceUri?.toVoiceDraft()
                )
            ).onSuccess {
                _state.update { it.copy(isSubmitting = false, error = null) }
                _events.emit(SecretPublishEvent.ShowMessage(context.getString(R.string.secret_publish_success)))
                _events.emit(SecretPublishEvent.Submitted)
            }.onFailure { error ->
                _state.update { it.copy(isSubmitting = false, error = error.message) }
                emitMessage(error.message ?: context.getString(R.string.secret_publish_failed))
            }
        }
    }

    private suspend fun Uri.toVoiceDraft(): SecretVoiceDraft {
        val bytes = context.contentResolver.openInputStream(this)?.use { it.readBytes() }
            ?: throw IllegalStateException(context.getString(R.string.secret_publish_voice_read_failed))
        val mimeType = context.contentResolver.getType(this)?.ifBlank { null } ?: "audio/mpeg"
        return SecretVoiceDraft(
            fileName = queryDisplayName(this).ifBlank { context.getString(R.string.secret_publish_default_voice_name) },
            mimeType = mimeType,
            bytes = bytes
        )
    }

    private fun queryDisplayName(uri: Uri): String {
        val cursor = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        cursor?.use {
            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && it.moveToFirst()) {
                return it.getString(index).orEmpty()
            }
        }
        return uri.lastPathSegment.orEmpty()
    }

    private fun emitMessage(message: String) {
        viewModelScope.launch {
            _events.emit(SecretPublishEvent.ShowMessage(message))
        }
    }
}
