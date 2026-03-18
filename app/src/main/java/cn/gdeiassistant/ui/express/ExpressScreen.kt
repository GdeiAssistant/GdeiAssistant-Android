package cn.gdeiassistant.ui.express

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.NoteAdd
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Info
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import cn.gdeiassistant.model.ExpressCommentItem
import cn.gdeiassistant.model.ExpressGender
import cn.gdeiassistant.model.ExpressPost
import cn.gdeiassistant.model.ExpressPostDetail
import cn.gdeiassistant.ui.components.EmptyState
import cn.gdeiassistant.ui.components.ActionTile
import cn.gdeiassistant.ui.components.BadgePill
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.MetricChip
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.StatusBanner
import cn.gdeiassistant.ui.components.ShimmerBox
import cn.gdeiassistant.ui.navigation.Routes
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ExpressScreen(navController: NavHostController) {
    val viewModel: ExpressViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle

    LaunchedEffect(savedStateHandle) {
        savedStateHandle?.getStateFlow(Routes.EXPRESS_REFRESH_FLAG, false)?.collectLatest { needsRefresh ->
            if (needsRefresh) {
                viewModel.refresh()
                savedStateHandle[Routes.EXPRESS_REFRESH_FLAG] = false
            }
        }
    }

    LazyScreen(
        title = stringResource(R.string.express_title),
        onBack = navController::popBackStack,
        actions = {
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.express_refresh))
            }
        }
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionTile(
                    title = stringResource(R.string.express_my_posts_title),
                    subtitle = stringResource(R.string.express_my_posts_subtitle),
                    icon = Icons.Rounded.Person,
                    onClick = { navController.navigate(Routes.EXPRESS_PROFILE) },
                    tint = MaterialTheme.colorScheme.primary,
                    emphasized = true,
                    modifier = Modifier.weight(1f)
                )
                ActionTile(
                    title = stringResource(R.string.express_publish_title),
                    subtitle = stringResource(R.string.express_publish_subtitle),
                    icon = Icons.AutoMirrored.Rounded.NoteAdd,
                    onClick = { navController.navigate(Routes.EXPRESS_PUBLISH) },
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
                    label = { Text(text = stringResource(R.string.express_search_label)) },
                    singleLine = true
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = viewModel::refresh,
                        enabled = !state.isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(R.string.express_search_action))
                    }
                    Button(
                        onClick = viewModel::clearQuery,
                        enabled = state.query.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(R.string.express_search_clear_action))
                    }
                }
            }
        }
        item {
            SectionCard(
                modifier = Modifier.fillMaxWidth()
            ) {
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
                        text = stringResource(R.string.express_guide_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(10.dp))
                Text(
                    text = stringResource(R.string.express_guide_body),
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
                    icon = Icons.Rounded.Favorite,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
        when {
            state.isLoading && state.posts.isEmpty() -> item { ExpressLoadingPane() }
            state.posts.isEmpty() -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    EmptyState(
                        icon = Icons.Rounded.Favorite,
                        message = stringResource(R.string.express_empty),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            else -> {
                items(state.posts, key = { it.id }) { post ->
                    ExpressPostCard(
                        post = post,
                        onClick = { navController.navigate(Routes.expressDetail(post.id)) }
                    )
                }
            }
        }
    }
}

@Composable
fun ExpressDetailScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: ExpressDetailViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var commentText by rememberSaveable { mutableStateOf("") }
    var guessText by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ExpressDetailEvent.ShowMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    LazyScreen(
        title = stringResource(R.string.express_detail_title),
        onBack = navController::popBackStack,
        actions = {
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.express_refresh))
            }
        }
    ) {
        when {
            state.isLoading -> item { ExpressLoadingPane() }
            !state.error.isNullOrBlank() && state.detail == null -> item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.Favorite,
                    tint = MaterialTheme.colorScheme.error
                )
            }
            state.detail == null -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    EmptyState(
                        icon = Icons.Rounded.Favorite,
                        message = stringResource(R.string.express_detail_missing),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            else -> {
                val detail = requireNotNull(state.detail)
                item { ExpressDetailHero(detail = detail) }
                item {
                    SectionCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.express_content_title),
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
                if (!detail.realName.isNullOrBlank()) {
                    item {
                        SectionCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = stringResource(R.string.express_real_name_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(10.dp))
                            Text(
                                text = detail.realName.orEmpty(),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ActionTile(
                            title = if (detail.post.isLiked) {
                                stringResource(R.string.express_liked_action)
                            } else {
                                stringResource(R.string.express_like_action)
                            },
                            subtitle = stringResource(
                                R.string.secret_like_count_value,
                                detail.post.likeCount
                            ),
                            icon = Icons.Rounded.Favorite,
                            onClick = viewModel::like,
                            tint = MaterialTheme.colorScheme.error,
                            emphasized = true,
                            modifier = Modifier.width(176.dp)
                        )
                        if (detail.post.canGuess) {
                            SectionCard(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.express_guess_title),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
                                OutlinedTextField(
                                    value = guessText,
                                    onValueChange = { guessText = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    placeholder = {
                                        Text(text = stringResource(R.string.express_guess_placeholder))
                                    }
                                )
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(10.dp))
                                Button(
                                    onClick = {
                                        viewModel.guess(guessText)
                                        guessText = ""
                                    },
                                    enabled = !state.isSubmittingGuess && guessText.isNotBlank()
                                ) {
                                    Text(text = stringResource(R.string.express_guess_action))
                                }
                            }
                        }
                    }
                }
                item {
                    SectionCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.express_comment_title),
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
                                Text(text = stringResource(R.string.express_comment_placeholder))
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
                            Text(text = stringResource(R.string.express_comment_send))
                        }
                    }
                }
                item {
                    SectionCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.express_comment_list_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(14.dp))
                        if (state.comments.isEmpty()) {
                            Text(
                                text = stringResource(R.string.express_comment_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            state.comments.forEach { comment ->
                                ExpressCommentRow(comment = comment)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpressProfileScreen(navController: NavHostController) {
    val viewModel: ExpressProfileViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyScreen(
        title = stringResource(R.string.express_profile_title),
        onBack = navController::popBackStack,
        actions = {
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.express_refresh))
            }
        }
    ) {
        when {
            state.isLoading && state.items.isEmpty() -> item { ExpressLoadingPane() }
            !state.error.isNullOrBlank() -> item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.Person,
                    tint = MaterialTheme.colorScheme.error
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
                        message = stringResource(R.string.express_profile_empty),
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
                        BadgePill(text = stringResource(R.string.express_profile_badge))
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(16.dp))
                        Text(
                            text = stringResource(R.string.express_profile_subtitle),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                items(state.items, key = { it.id }) { post ->
                    ExpressPostCard(
                        post = post,
                        onClick = { navController.navigate(Routes.expressDetail(post.id)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpressDetailHero(detail: ExpressPostDetail) {
    SectionCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BadgePill(
                text = if (detail.post.canGuess) {
                    stringResource(R.string.express_can_guess)
                } else {
                    stringResource(R.string.express_cannot_guess)
                }
            )
            Text(
                text = detail.post.publishTime,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = "${detail.post.nickname}  ${stringResource(R.string.express_like_action)}  ${detail.post.targetName}",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricChip(
                label = stringResource(R.string.express_stat_like),
                value = detail.post.likeCount.toString(),
                modifier = Modifier.weight(1f)
            )
            MetricChip(
                label = stringResource(R.string.express_stat_comment),
                value = detail.post.commentCount.toString(),
                modifier = Modifier.weight(1f)
            )
            MetricChip(
                label = stringResource(R.string.express_stat_guess),
                value = detail.post.guessCount.toString(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ExpressPostCard(
    post: ExpressPost,
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
                text = post.nickname,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = post.publishTime,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BadgePill(text = expressGenderLabel(post.selfGender))
            BadgePill(text = expressGenderLabel(post.targetGender), tint = MaterialTheme.colorScheme.tertiary)
            BadgePill(
                text = if (post.canGuess) {
                    stringResource(R.string.express_can_guess)
                } else {
                    stringResource(R.string.express_cannot_guess)
                },
                tint = if (post.canGuess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
            )
        }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
        Text(
            text = post.targetName,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = post.contentPreview,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
        Text(
            text = "${stringResource(R.string.express_stat_like)} ${post.likeCount} · " +
                "${stringResource(R.string.express_stat_comment)} ${post.commentCount} · " +
                "${stringResource(R.string.express_stat_matched)} ${post.correctGuessCount}/${post.guessCount}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ExpressCommentRow(comment: ExpressCommentItem) {
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
                text = comment.publishTime,
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
private fun expressGenderLabel(gender: ExpressGender): String {
    return when (gender) {
        ExpressGender.MALE -> stringResource(R.string.express_gender_male)
        ExpressGender.FEMALE -> stringResource(R.string.express_gender_female)
        ExpressGender.SECRET -> stringResource(R.string.express_gender_secret)
    }
}

@Composable
private fun ExpressLoadingPane() {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        ShimmerBox(modifier = Modifier.fillMaxWidth(0.55f), height = 28.dp)
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth(), height = 18.dp)
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(10.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth(0.92f), height = 18.dp)
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(10.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth(0.72f), height = 18.dp)
    }
}
