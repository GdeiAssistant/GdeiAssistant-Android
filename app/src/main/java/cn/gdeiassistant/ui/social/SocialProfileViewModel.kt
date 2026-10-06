package cn.gdeiassistant.ui.social

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.R
import cn.gdeiassistant.data.SocialRepository
import cn.gdeiassistant.model.SocialRealtimeEvent
import cn.gdeiassistant.model.SocialRelationship
import cn.gdeiassistant.model.SocialUser
import cn.gdeiassistant.network.SocialRealtimeManager
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
