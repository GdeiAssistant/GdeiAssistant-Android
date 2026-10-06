package cn.gdeiassistant.ui.discovery

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TextButton
import cn.gdeiassistant.ui.components.ListGroup
import cn.gdeiassistant.ui.components.SectionHeader
import cn.gdeiassistant.ui.components.StatItem
import cn.gdeiassistant.ui.components.StatStrip
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import cn.gdeiassistant.data.DiscoveryRepository
import cn.gdeiassistant.model.DiscoverySummary
import cn.gdeiassistant.ui.components.BadgePill
import cn.gdeiassistant.ui.components.EmptyState
import cn.gdeiassistant.ui.components.GhostButton
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.StatusBanner
import cn.gdeiassistant.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiscoveryUiState(
    val summary: DiscoverySummary = DiscoverySummary(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class DiscoveryViewModel @Inject constructor(
    private val repository: DiscoveryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DiscoveryUiState())
    val state: StateFlow<DiscoveryUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getSummary()
                .onSuccess { summary ->
                    _state.update { it.copy(summary = summary, isLoading = false, error = null) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }
}

@Composable
fun DiscoveryScreen(navController: NavHostController) {
    val viewModel: DiscoveryViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyScreen(
        title = stringResource(R.string.discovery_title),
        onBack = navController::popBackStack,
        actions = {
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = stringResource(R.string.schedule_refresh)
                )
            }
        }
    ) {
        item {
            DiscoveryOverviewCard(summary = state.summary)
        }
        if (!state.error.isNullOrBlank()) {
            item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.Campaign
                )
            }
        }
        if (!state.isLoading && state.summary.secretPosts.isEmpty() && state.summary.expressPosts.isEmpty() && state.summary.topicPosts.isEmpty()) {
            item {
                SectionCard(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    ) {
                        EmptyState(
                            icon = Icons.Rounded.Campaign,
                            message = stringResource(R.string.discovery_empty),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        } else {
            item {
                DiscoverySection(
                    title = stringResource(R.string.discovery_secret_title),
                    subtitle = stringResource(R.string.discovery_secret_subtitle),
                    actionTitle = stringResource(R.string.discovery_open_secret),
                    icon = Icons.Rounded.Campaign,
                    tint = MaterialTheme.colorScheme.primary,
                    onOpen = { navController.navigate(Routes.SECRET) }
                ) {
                    state.summary.secretPosts.forEach { post ->
                        DiscoveryPreviewRow(
                            title = post.title,
                            subtitle = post.summary,
                            meta = post.createdAt,
                            onClick = { navController.navigate(Routes.secretDetail(post.id)) }
                        )
                    }
                }
            }
            item {
                DiscoverySection(
                    title = stringResource(R.string.discovery_express_title),
                    subtitle = stringResource(R.string.discovery_express_subtitle),
                    actionTitle = stringResource(R.string.discovery_open_express),
                    icon = Icons.Rounded.Favorite,
                    tint = MaterialTheme.colorScheme.tertiary,
                    onOpen = { navController.navigate(Routes.EXPRESS) }
                ) {
                    state.summary.expressPosts.forEach { post ->
                        DiscoveryPreviewRow(
                            title = "${post.nickname} -> ${post.targetName}",
                            subtitle = post.contentPreview,
                            meta = post.publishTime,
                            onClick = { navController.navigate(Routes.expressDetail(post.id)) }
                        )
                    }
                }
            }
            item {
                DiscoverySection(
                    title = stringResource(R.string.discovery_topic_title),
                    subtitle = stringResource(R.string.discovery_topic_subtitle),
                    actionTitle = stringResource(R.string.discovery_open_topic),
                    icon = Icons.Rounded.Tag,
                    tint = MaterialTheme.colorScheme.secondary,
                    onOpen = { navController.navigate(Routes.TOPIC) }
                ) {
                    state.summary.topicPosts.forEach { post ->
                        DiscoveryPreviewRow(
                            title = "#${post.topic}",
                            subtitle = post.contentPreview,
                            meta = post.publishedAt,
                            onClick = { navController.navigate(Routes.topicDetail(post.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiscoveryOverviewCard(summary: DiscoverySummary) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BadgePill(text = stringResource(R.string.discovery_badge))
        Text(
            text = stringResource(R.string.discovery_subtitle),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        StatStrip(
            items = listOf(
                StatItem(stringResource(R.string.discovery_metric_secret), summary.secretPosts.size.toString()),
                StatItem(stringResource(R.string.discovery_metric_express), summary.expressPosts.size.toString()),
                StatItem(stringResource(R.string.discovery_metric_topic), summary.topicPosts.size.toString())
            )
        )
    }
}

@Composable
private fun DiscoverySection(
    title: String,
    subtitle: String,
    actionTitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    onOpen: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(
            title = title,
            supporting = subtitle,
            action = {
                TextButton(onClick = onOpen) {
                    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(text = actionTitle)
                }
            }
        )
        ListGroup(content = content)
    }
}

@Composable
private fun DiscoveryPreviewRow(
    title: String,
    subtitle: String,
    meta: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = meta,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
}

@Composable
private fun DiscoveryMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    cn.gdeiassistant.ui.components.MetricFigure(label = label, value = value, modifier = modifier)
}
