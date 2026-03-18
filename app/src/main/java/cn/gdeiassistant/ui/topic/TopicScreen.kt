package cn.gdeiassistant.ui.topic

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.NoteAdd
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import cn.gdeiassistant.model.TopicPost
import cn.gdeiassistant.model.TopicPostDetail
import cn.gdeiassistant.ui.components.EmptyState
import cn.gdeiassistant.ui.components.ActionTile
import cn.gdeiassistant.ui.components.BadgePill
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.MetricChip
import cn.gdeiassistant.ui.components.NativeImageGallery
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.StatusBanner
import cn.gdeiassistant.ui.components.ShimmerBox
import cn.gdeiassistant.ui.navigation.Routes
import kotlinx.coroutines.flow.collectLatest

@Composable
fun TopicScreen(navController: NavHostController) {
    val viewModel: TopicViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle

    LaunchedEffect(savedStateHandle) {
        savedStateHandle?.getStateFlow(Routes.TOPIC_REFRESH_FLAG, false)?.collectLatest { needsRefresh ->
            if (needsRefresh) {
                viewModel.refresh()
                savedStateHandle[Routes.TOPIC_REFRESH_FLAG] = false
            }
        }
    }

    LazyScreen(
        title = stringResource(R.string.topic_title),
        onBack = navController::popBackStack,
        actions = {
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.topic_refresh))
            }
        }
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionTile(
                    title = stringResource(R.string.topic_my_posts_title),
                    subtitle = stringResource(R.string.topic_my_posts_subtitle),
                    icon = Icons.Rounded.Person,
                    onClick = { navController.navigate(Routes.TOPIC_PROFILE) },
                    tint = MaterialTheme.colorScheme.primary,
                    emphasized = true,
                    modifier = Modifier.weight(1f)
                )
                ActionTile(
                    title = stringResource(R.string.topic_publish_title),
                    subtitle = stringResource(R.string.topic_publish_subtitle),
                    icon = Icons.AutoMirrored.Rounded.NoteAdd,
                    onClick = { navController.navigate(Routes.TOPIC_PUBLISH) },
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            SectionCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = viewModel::updateQuery,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = stringResource(R.string.topic_search_label)) },
                    singleLine = true
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = viewModel::refresh,
                        enabled = !state.isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(R.string.topic_search_action))
                    }
                    Button(
                        onClick = viewModel::clearQuery,
                        enabled = state.query.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(R.string.topic_search_clear_action))
                    }
                }
            }
        }
        item {
            SectionCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = stringResource(R.string.topic_guide_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(10.dp))
                Text(
                    text = stringResource(R.string.topic_guide_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (!state.error.isNullOrBlank()) {
            item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.Tag
                )
            }
        }
        when {
            state.isLoading && state.posts.isEmpty() -> item { TopicLoadingPane() }
            state.posts.isEmpty() -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    EmptyState(
                        icon = Icons.Rounded.Tag,
                        message = stringResource(R.string.topic_empty),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            else -> {
                items(state.posts, key = { it.id }) { post ->
                    TopicPostCard(
                        post = post,
                        onClick = { navController.navigate(Routes.topicDetail(post.id)) }
                    )
                }
            }
        }
    }
}

@Composable
fun TopicDetailScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: TopicDetailViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is TopicDetailEvent.ShowMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    LazyScreen(
        title = stringResource(R.string.topic_detail_title),
        onBack = navController::popBackStack,
        actions = {
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.topic_refresh))
            }
        }
    ) {
        when {
            state.isLoading -> item { TopicLoadingPane() }
            !state.error.isNullOrBlank() && state.detail == null -> item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.Tag
                )
            }
            state.detail == null -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    EmptyState(
                        icon = Icons.Rounded.Tag,
                        message = stringResource(R.string.topic_detail_missing),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            else -> {
                val detail = requireNotNull(state.detail)
                item { TopicDetailHero(detail = detail) }
                item {
                    SectionCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.topic_content_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(14.dp))
                        Text(
                            text = detail.content,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                item {
                    SectionCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.topic_gallery_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(14.dp))
                        if (detail.imageUrls.isEmpty()) {
                            Text(
                                text = stringResource(R.string.topic_gallery_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            NativeImageGallery(
                                imageUrls = detail.imageUrls,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                item {
                    Button(
                        onClick = viewModel::like,
                        enabled = !state.isSubmittingLike && detail.post.isLiked.not(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (detail.post.isLiked) {
                                stringResource(R.string.topic_liked_action)
                            } else {
                                stringResource(R.string.topic_like_action)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopicProfileScreen(navController: NavHostController) {
    val viewModel: TopicProfileViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyScreen(
        title = stringResource(R.string.topic_profile_title),
        onBack = navController::popBackStack,
        actions = {
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.topic_refresh))
            }
        }
    ) {
        when {
            state.isLoading && state.items.isEmpty() -> item { TopicLoadingPane() }
            !state.error.isNullOrBlank() -> item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.Person
                )
            }
            state.items.isEmpty() -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    EmptyState(
                        icon = Icons.Rounded.Person,
                        message = stringResource(R.string.topic_profile_empty),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            else -> {
                item {
                    SectionCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    ) {
                        BadgePill(text = stringResource(R.string.topic_profile_badge))
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(16.dp))
                        Text(
                            text = stringResource(R.string.topic_profile_subtitle),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                items(state.items, key = { it.id }) { post ->
                    TopicPostCard(
                        post = post,
                        onClick = { navController.navigate(Routes.topicDetail(post.id)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TopicDetailHero(detail: TopicPostDetail) {
    SectionCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    ) {
        BadgePill(text = "#${detail.post.topic}")
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = detail.post.authorName,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(10.dp))
        Text(
            text = detail.post.publishedAt,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricChip(
                label = stringResource(R.string.topic_stat_like),
                value = detail.post.likeCount.toString(),
                modifier = Modifier.weight(1f)
            )
            MetricChip(
                label = stringResource(R.string.topic_stat_image),
                value = detail.imageUrls.size.toString(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TopicPostCard(
    post: TopicPost,
    onClick: () -> Unit
) {
    SectionCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "#${post.topic}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = post.publishedAt,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BadgePill(text = post.authorName, tint = MaterialTheme.colorScheme.secondary)
            if (post.imageCount > 0) {
                BadgePill(
                    text = "${stringResource(R.string.topic_stat_image)} ${post.imageCount}",
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
        }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
        Text(
            text = post.contentPreview,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
        Text(
            text = "${stringResource(R.string.topic_stat_like)} ${post.likeCount}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TopicLoadingPane() {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        ShimmerBox(modifier = Modifier.fillMaxWidth(0.45f), height = 28.dp)
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth(), height = 18.dp)
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(10.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth(0.88f), height = 18.dp)
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(10.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth(0.66f), height = 18.dp)
    }
}
