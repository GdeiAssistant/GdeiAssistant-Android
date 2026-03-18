package cn.gdeiassistant.ui.secret

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.NoteAdd
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
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
import cn.gdeiassistant.model.SecretComment
import cn.gdeiassistant.model.SecretDetail
import cn.gdeiassistant.model.SecretPost
import cn.gdeiassistant.ui.components.EmptyState
import cn.gdeiassistant.ui.components.ActionTile
import cn.gdeiassistant.ui.components.BadgePill
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.MetricChip
import cn.gdeiassistant.ui.components.NativeAudioPlayerCard
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.StatusBanner
import cn.gdeiassistant.ui.navigation.Routes
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SecretScreen(navController: NavHostController) {
    val viewModel: SecretViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle

    LaunchedEffect(savedStateHandle) {
        savedStateHandle?.getStateFlow(Routes.SECRET_REFRESH_FLAG, false)?.collectLatest { needsRefresh ->
            if (needsRefresh) {
                viewModel.refresh()
                savedStateHandle[Routes.SECRET_REFRESH_FLAG] = false
            }
        }
    }

    LazyScreen(
        title = stringResource(R.string.secret_title),
        onBack = navController::popBackStack,
        actions = {
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.secret_refresh))
            }
        }
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionTile(
                    title = stringResource(R.string.secret_my_posts_title),
                    subtitle = stringResource(R.string.secret_my_posts_subtitle),
                    icon = Icons.Rounded.Person,
                    onClick = { navController.navigate(Routes.SECRET_PROFILE) },
                    tint = MaterialTheme.colorScheme.primary,
                    emphasized = true,
                    modifier = Modifier.weight(1f)
                )
                ActionTile(
                    title = stringResource(R.string.secret_publish_title),
                    subtitle = stringResource(R.string.secret_publish_subtitle),
                    icon = Icons.AutoMirrored.Rounded.NoteAdd,
                    onClick = { navController.navigate(Routes.SECRET_PUBLISH) },
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (!state.error.isNullOrBlank()) {
            item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.AutoStories,
                )
            }
        }
        when {
            state.isLoading && state.posts.isEmpty() -> item { SecretLoadingPane() }
            state.posts.isEmpty() -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    EmptyState(
                        icon = Icons.Rounded.AutoStories,
                        message = stringResource(R.string.secret_empty),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            else -> {
                items(state.posts, key = { it.id }) { post ->
                    SecretPostCard(
                        post = post,
                        onClick = { navController.navigate(Routes.secretDetail(post.id)) }
                    )
                }
            }
        }
    }
}

@Composable
fun SecretDetailScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: SecretDetailViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var commentText by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is SecretDetailEvent.ShowMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    LazyScreen(
        title = stringResource(R.string.secret_detail_title),
        onBack = navController::popBackStack,
        actions = {
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.secret_refresh))
            }
        }
    ) {
        when {
            state.isLoading -> item { SecretLoadingPane() }
            !state.error.isNullOrBlank() -> item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.AutoStories,
                )
            }
            state.detail == null -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    EmptyState(
                        icon = Icons.Rounded.AutoStories,
                        message = stringResource(R.string.secret_detail_missing),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            else -> {
                val detail = requireNotNull(state.detail)
                item { SecretDetailHero(detail = detail) }
                if (!detail.post.voiceUrl.isNullOrBlank()) {
                    item {
                        NativeAudioPlayerCard(
                            title = stringResource(R.string.secret_voice_section_title),
                            subtitle = stringResource(R.string.secret_voice_section_subtitle),
                            url = detail.post.voiceUrl.orEmpty(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                item {
                    ActionTile(
                        title = if (detail.post.isLiked) {
                            stringResource(R.string.secret_unlike_action)
                        } else {
                            stringResource(R.string.secret_like_action)
                        },
                        subtitle = stringResource(
                            R.string.secret_like_count_value,
                            detail.post.likeCount
                        ),
                        icon = Icons.Rounded.Favorite,
                        onClick = viewModel::toggleLike,
                        tint = MaterialTheme.colorScheme.tertiary,
                        emphasized = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    SectionCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.secret_content_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(14.dp))
                        Text(
                            text = detail.content,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                item {
                    SectionCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.secret_comment_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(14.dp))
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4,
                            placeholder = {
                                Text(text = stringResource(R.string.secret_comment_placeholder))
                            }
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
                        Button(
                            onClick = {
                                viewModel.submitComment(commentText)
                                commentText = ""
                            },
                            enabled = !state.isSubmittingComment && commentText.isNotBlank()
                        ) {
                            Text(text = stringResource(R.string.secret_comment_send))
                        }
                    }
                }
                item {
                    SectionCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.secret_comment_list_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(14.dp))
                        if (detail.comments.isEmpty()) {
                            Text(
                                text = stringResource(R.string.secret_comment_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            detail.comments.forEach { comment ->
                                SecretCommentRow(comment = comment)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SecretProfileScreen(navController: NavHostController) {
    val viewModel: SecretProfileViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyScreen(
        title = stringResource(R.string.secret_profile_title),
        onBack = navController::popBackStack,
        actions = {
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.secret_refresh))
            }
        }
    ) {
        when {
            state.isLoading && state.items.isEmpty() -> item { SecretLoadingPane() }
            !state.error.isNullOrBlank() -> item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.Person,
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
                        message = stringResource(R.string.secret_profile_empty),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            else -> {
                item {
                    SectionCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
                    ) {
                        BadgePill(text = stringResource(R.string.secret_profile_badge))
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(16.dp))
                        Text(
                            text = stringResource(R.string.secret_profile_subtitle),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                items(state.items, key = { it.id }) { post ->
                    SecretPostCard(
                        post = post,
                        onClick = { navController.navigate(Routes.secretDetail(post.id)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SecretDetailHero(detail: SecretDetail) {
    SectionCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
    ) {
        BadgePill(
            text = if (detail.post.isVoice) {
                stringResource(R.string.secret_voice_badge)
            } else {
                stringResource(R.string.secret_text_badge)
            }
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = detail.post.title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricChip(
                label = stringResource(R.string.secret_like_metric),
                value = detail.post.likeCount.toString(),
                modifier = Modifier.weight(1f)
            )
            MetricChip(
                label = stringResource(R.string.secret_comment_metric),
                value = detail.post.commentCount.toString(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SecretPostCard(
    post: SecretPost,
    onClick: () -> Unit
) {
    SectionCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f),
        borderColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.20f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BadgePill(
                text = if (post.isVoice) {
                    stringResource(R.string.secret_voice_badge)
                } else {
                    stringResource(R.string.secret_text_badge)
                },
                tint = MaterialTheme.colorScheme.tertiary
            )
            secretTimerText(post)?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
        Text(
            text = post.summary,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
        Text(
            text = "${post.createdAt} · ${stringResource(R.string.secret_like_count_value, post.likeCount)} · ${stringResource(R.string.secret_comment_count_value, post.commentCount)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SecretCommentRow(comment: SecretComment) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = comment.authorName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = comment.createdAt,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = comment.content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SecretLoadingPane() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.CircularProgressIndicator()
    }
}

@Composable
private fun secretTimerText(post: SecretPost): String? {
    return if (post.timer == 1) {
        stringResource(R.string.secret_timer_hint)
    } else {
        null
    }
}
