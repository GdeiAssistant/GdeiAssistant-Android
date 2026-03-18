package cn.gdeiassistant.ui.book

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import cn.gdeiassistant.data.BookRepository
import cn.gdeiassistant.model.CollectionDetailInfo
import cn.gdeiassistant.ui.components.EmptyState
import cn.gdeiassistant.ui.components.BadgePill
import cn.gdeiassistant.ui.components.HeroCard
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.StatusBanner
import cn.gdeiassistant.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.content.Context
import javax.inject.Inject

data class CollectionDetailUiState(
    val detail: CollectionDetailInfo? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class CollectionDetailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
    private val repository: BookRepository
) : ViewModel() {

    private val detailUrl: String = savedStateHandle.get<String>(Routes.BOOK_COLLECTION_DETAIL_URL).orEmpty()

    private val _state = MutableStateFlow(CollectionDetailUiState())
    val state: StateFlow<CollectionDetailUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        if (detailUrl.isBlank()) {
            _state.update { it.copy(isLoading = false, error = context.getString(R.string.book_collection_detail_missing_url)) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getCollectionDetail(detailUrl)
                .onSuccess { detail ->
                    _state.update { it.copy(detail = detail, isLoading = false, error = null) }
                }
                .onFailure { error ->
                    _state.update { it.copy(detail = null, isLoading = false, error = error.message) }
                }
        }
    }
}

@Composable
fun CollectionDetailScreen(navController: NavHostController) {
    val viewModel: CollectionDetailViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyScreen(
        title = stringResource(R.string.book_collection_detail_title),
        onBack = navController::popBackStack,
        actions = {
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.schedule_refresh))
            }
        }
    ) {
        when {
            state.isLoading -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    EmptyState(
                        icon = Icons.Rounded.AutoStories,
                        message = stringResource(R.string.book_collection_detail_loading),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            !state.error.isNullOrBlank() && state.detail == null -> item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.AutoStories
                )
            }
            state.detail == null -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    EmptyState(
                        icon = Icons.Rounded.AutoStories,
                        message = stringResource(R.string.book_collection_missing),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            else -> {
                val detail = requireNotNull(state.detail)
                item {
                    HeroCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        BadgePill(text = stringResource(R.string.book_collection_detail_badge), onGradient = true)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = detail.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = detail.author,
                            style = MaterialTheme.typography.bodyLarge,
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.92f)
                        )
                    }
                }
                item {
                    SectionCard(modifier = Modifier.fillMaxWidth()) {
                        DetailText(label = stringResource(R.string.book_collection_detail_principal), value = detail.principal)
                        DetailText(label = stringResource(R.string.book_collection_detail_publisher), value = detail.publisher)
                        DetailText(label = stringResource(R.string.book_collection_detail_price), value = detail.price)
                        DetailText(label = stringResource(R.string.book_collection_detail_physical), value = detail.physicalDescription)
                        DetailText(label = stringResource(R.string.book_collection_detail_subject), value = detail.subjectTheme)
                        DetailText(label = stringResource(R.string.book_collection_detail_classification), value = detail.classification)
                    }
                }
                item {
                    SectionCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.book_collection_distribution_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        if (detail.distributions.isEmpty()) {
                            Text(
                                text = stringResource(R.string.book_collection_distribution_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            detail.distributions.forEach { distribution ->
                                DetailText(
                                    label = distribution.location,
                                    value = "${distribution.callNumber} · ${distribution.state} · ${distribution.barcode}"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailText(label: String, value: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.tertiary
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = value,
        style = MaterialTheme.typography.bodyMedium
    )
    Spacer(modifier = Modifier.height(12.dp))
}
