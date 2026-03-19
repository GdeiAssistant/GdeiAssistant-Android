package cn.gdeiassistant.ui.news

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.data.NewsRepository
import cn.gdeiassistant.model.SchoolNews
import cn.gdeiassistant.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NewsDetailUiState(
    val isLoading: Boolean = false,
    val detail: SchoolNews? = null,
    val error: String? = null
)

@HiltViewModel
class NewsDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val newsRepository: NewsRepository
) : ViewModel() {

    private val newsId: String = savedStateHandle.get<String>(Routes.NEWS_DETAIL_ID).orEmpty()

    private val _state = MutableStateFlow(NewsDetailUiState(isLoading = true))
    val state: StateFlow<NewsDetailUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        if (newsId.isBlank()) {
            _state.value = NewsDetailUiState(
                isLoading = false,
                detail = null,
                error = "缺少新闻编号"
            )
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            newsRepository.getNewsDetail(newsId)
                .onSuccess { detail ->
                    _state.value = NewsDetailUiState(
                        isLoading = false,
                        detail = detail,
                        error = null
                    )
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            detail = null,
                            error = error.message
                        )
                    }
                }
        }
    }
}
