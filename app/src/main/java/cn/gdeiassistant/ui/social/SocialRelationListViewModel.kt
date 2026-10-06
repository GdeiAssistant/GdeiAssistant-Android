package cn.gdeiassistant.ui.social

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.R
import cn.gdeiassistant.data.SocialRepository
import cn.gdeiassistant.model.SocialRelationshipKind
import cn.gdeiassistant.model.SocialUser
import cn.gdeiassistant.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

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
