package cn.gdeiassistant.ui.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.R
import cn.gdeiassistant.data.ProfileRepository
import cn.gdeiassistant.data.SessionManager
import cn.gdeiassistant.model.AvatarState
import cn.gdeiassistant.model.UserProfileSummary
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

data class AvatarEditUiState(
    val isLoading: Boolean = false,
    val profile: UserProfileSummary? = null,
    val avatarState: AvatarState = AvatarState(),
    val selectedImageUri: Uri? = null,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val submitError: String? = null
) {
    val previewModel: Any?
        get() = selectedImageUri ?: avatarState.url

    val fallbackLabel: String
        get() = profile?.nickname?.takeIf(String::isNotBlank)
            ?: profile?.username?.takeIf(String::isNotBlank)
            ?: "G"

    val hasRemoteAvatar: Boolean
        get() = !avatarState.url.isNullOrBlank()

    val canUpload: Boolean
        get() = selectedImageUri != null && !isSubmitting

    val canRestoreDefault: Boolean
        get() = (hasRemoteAvatar || selectedImageUri != null) && !isSubmitting
}

sealed interface AvatarEditEvent {
    data class AvatarChanged(val message: String) : AvatarEditEvent
}

@HiltViewModel
class AvatarEditViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val sessionManager: SessionManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(AvatarEditUiState(isLoading = true))
    val state: StateFlow<AvatarEditUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<AvatarEditEvent>()
    val events: SharedFlow<AvatarEditEvent> = _events.asSharedFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val profileDeferred = async { profileRepository.getProfile() }
            val avatarDeferred = async { profileRepository.getAvatarState() }

            val profileResult = profileDeferred.await()
            val avatarResult = avatarDeferred.await()
            val fallbackUsername = sessionManager.currentUsername().orEmpty()
            val profile = profileResult.getOrNull() ?: UserProfileSummary(
                username = fallbackUsername.ifBlank { context.getString(R.string.profile_default_username) }
            )

            _state.update {
                it.copy(
                    isLoading = false,
                    profile = profile,
                    avatarState = avatarResult.getOrDefault(AvatarState(profile.avatar?.trim()?.takeIf(String::isNotBlank))),
                    error = avatarResult.exceptionOrNull()?.message ?: profileResult.exceptionOrNull()?.message
                )
            }
        }
    }

    fun selectImage(uri: Uri?) {
        _state.update {
            it.copy(
                selectedImageUri = uri,
                submitError = null
            )
        }
    }

    fun uploadSelectedAvatar() {
        val selectedUri = _state.value.selectedImageUri
        if (selectedUri == null) {
            _state.update { it.copy(submitError = context.getString(R.string.profile_avatar_pick_first)) }
            return
        }
        if (_state.value.isSubmitting) return

        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, submitError = null) }
            profileRepository.uploadAvatar(selectedUri)
                .onSuccess { avatar ->
                    _state.update { current ->
                        current.copy(
                            avatarState = avatar,
                            selectedImageUri = null,
                            isSubmitting = false,
                            submitError = null,
                            profile = current.profile?.copy(avatar = avatar.url)
                        )
                    }
                    _events.emit(AvatarEditEvent.AvatarChanged(context.getString(R.string.profile_avatar_upload_success)))
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            submitError = error.message ?: context.getString(R.string.profile_avatar_upload_failed)
                        )
                    }
                }
        }
    }

    fun restoreDefaultAvatar() {
        if (_state.value.isSubmitting) return
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, submitError = null) }
            profileRepository.deleteAvatar()
                .onSuccess { avatar ->
                    _state.update { current ->
                        current.copy(
                            avatarState = avatar,
                            selectedImageUri = null,
                            isSubmitting = false,
                            submitError = null,
                            profile = current.profile?.copy(avatar = avatar.url)
                        )
                    }
                    _events.emit(AvatarEditEvent.AvatarChanged(context.getString(R.string.profile_avatar_reset_success)))
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            submitError = error.message ?: context.getString(R.string.profile_avatar_reset_failed)
                        )
                    }
                }
        }
    }
}
