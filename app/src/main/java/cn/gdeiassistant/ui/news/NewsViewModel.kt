package cn.gdeiassistant.ui.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.data.NewsCategory
import cn.gdeiassistant.data.NewsRepository
import cn.gdeiassistant.model.SchoolNews
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NewsUiState(
    val isLoading: Boolean = false,
    val selectedType: Int = 5,
    val categories: List<NewsCategory> = emptyList(),
    val items: List<SchoolNews> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val newsRepository: NewsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(
        NewsUiState(
            isLoading = true,
            categories = newsRepository.categories
        )
    )
    val state: StateFlow<NewsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun selectCategory(type: Int) {
        if (type == _state.value.selectedType) return
        _state.update { it.copy(selectedType = type, isLoading = true, error = null) }
        refresh()
    }

    fun refresh() {
        val targetType = _state.value.selectedType
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            newsRepository.getNewsList(type = targetType, size = 20)
                .onSuccess { items ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            items = items,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            items = emptyList(),
                            error = error.message
                        )
                    }
                }
        }
    }
}

