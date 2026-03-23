package cn.gdeiassistant.ui.dating

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.R
import cn.gdeiassistant.data.DatingRepository
import cn.gdeiassistant.data.mapper.DatingDisplayMapper
import cn.gdeiassistant.model.DatingMyPost
import cn.gdeiassistant.model.DatingPickStatus
import cn.gdeiassistant.model.DatingReceivedPick
import cn.gdeiassistant.model.DatingSentPick
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

enum class DatingCenterTab {
    RECEIVED, SENT, POSTS
}

data class DatingUiState(
    val selectedTab: DatingCenterTab = DatingCenterTab.RECEIVED,
    val receivedItems: List<DatingReceivedPick> = emptyList(),
    val sentItems: List<DatingSentPick> = emptyList(),
    val myPosts: List<DatingMyPost> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

sealed interface DatingEvent {
    data class ShowMessage(val message: String) : DatingEvent
}

@HiltViewModel
class DatingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val datingRepository: DatingRepository,
    private val datingDisplayMapper: DatingDisplayMapper,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(
        DatingUiState(selectedTab = datingTabFrom(savedStateHandle.get<String>(Routes.DATING_CENTER_TAB)))
    )
    val state: StateFlow<DatingUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<DatingEvent>()
    val events: SharedFlow<DatingEvent> = _events.asSharedFlow()

    init {
        refresh()
    }

    fun selectTab(tab: DatingCenterTab) {
        _state.update { it.copy(selectedTab = tab) }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            datingRepository.getCenterData()
                .onSuccess { (received, sent, posts) ->
                    _state.update {
                        it.copy(
                            receivedItems = datingDisplayMapper.applyReceivedPickDefaults(received),
                            sentItems = datingDisplayMapper.applySentPickDefaults(sent),
                            myPosts = datingDisplayMapper.applyMyPostDefaults(posts),
                            isLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            receivedItems = emptyList(),
                            sentItems = emptyList(),
                            myPosts = emptyList(),
                            isLoading = false,
                            error = error.message
                        )
                    }
                }
        }
    }

    fun updatePickState(id: String, state: DatingPickStatus) {
        viewModelScope.launch {
            datingRepository.updatePickState(id, state)
                .onSuccess {
                    _events.emit(
                        DatingEvent.ShowMessage(
                            if (state == DatingPickStatus.ACCEPTED) {
                                context.getString(R.string.dating_toast_accepted)
                            } else {
                                context.getString(R.string.dating_toast_rejected)
                            }
                        )
                    )
                    refresh()
                }
                .onFailure { _ ->
                    _events.emit(
                        DatingEvent.ShowMessage(context.getString(R.string.dating_toast_failed))
                    )
                }
        }
    }

    fun hideProfile(id: String) {
        viewModelScope.launch {
            datingRepository.hideProfile(id)
                .onSuccess {
                    _events.emit(DatingEvent.ShowMessage(context.getString(R.string.dating_toast_hidden)))
                    refresh()
                }
                .onFailure { _ ->
                    _events.emit(
                        DatingEvent.ShowMessage(context.getString(R.string.dating_toast_hide_failed))
                    )
            }
        }
    }

    private fun datingTabFrom(rawValue: String?): DatingCenterTab {
        return when (rawValue?.trim()?.lowercase()) {
            "sent" -> DatingCenterTab.SENT
            "posts", "published" -> DatingCenterTab.POSTS
            else -> DatingCenterTab.RECEIVED
        }
    }
}
