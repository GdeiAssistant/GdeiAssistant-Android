package cn.gdeiassistant.ui.photograph

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.NoteAdd
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import cn.gdeiassistant.model.PhotographCategory
import cn.gdeiassistant.model.PhotographPost
import cn.gdeiassistant.model.PhotographPostDetail
import cn.gdeiassistant.model.SelectedLocalImage
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
fun PhotographScreen(navController: NavHostController) {
    val viewModel: PhotographViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyScreen(
        title = stringResource(R.string.photograph_title),
        onBack = navController::popBackStack,
        actions = {
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.photograph_refresh))
            }
        }
    ) {
        item {
            androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionTile(
                    title = stringResource(R.string.photograph_action_mine_title),
                    subtitle = stringResource(R.string.photograph_action_mine_subtitle),
                    icon = Icons.Rounded.PhotoCamera,
                    onClick = { navController.navigate(Routes.PHOTOGRAPH_PROFILE) },
                    tint = MaterialTheme.colorScheme.primary,
                    emphasized = true,
                    modifier = Modifier.weight(1f)
                )
                ActionTile(
                    title = stringResource(R.string.photograph_action_publish_title),
                    subtitle = stringResource(R.string.photograph_action_publish_subtitle),
                    icon = Icons.AutoMirrored.Rounded.NoteAdd,
                    onClick = { navController.navigate(Routes.PHOTOGRAPH_PUBLISH) },
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PhotographCategory.entries.forEach { category ->
                    FilterChip(
                        selected = state.selectedCategory == category,
                        onClick = { viewModel.selectCategory(category) },
                        label = { Text(text = photographCategoryLabel(category)) }
                    )
                }
            }
        }
        if (!state.error.isNullOrBlank()) {
            item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.PhotoCamera
                )
            }
        }
        when {
            state.isLoading && state.posts.isEmpty() -> item { PhotographLoadingPane() }
            state.posts.isEmpty() -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    EmptyState(
                        icon = Icons.Rounded.PhotoCamera,
                        message = stringResource(R.string.photograph_empty),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            else -> {
                items(state.posts, key = { it.id }) { post ->
                    PhotographPostCard(
                        post = post,
                        onClick = { navController.navigate(Routes.photographDetail(post.id)) }
                    )
                }
            }
        }
    }
}

@Composable
fun PhotographDetailScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: PhotographDetailViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var commentText by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is PhotographEvent.ShowMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
                PhotographEvent.Published -> Unit
            }
        }
    }

    LazyScreen(
        title = stringResource(R.string.photograph_detail_title),
        onBack = navController::popBackStack,
        actions = {
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.photograph_refresh))
            }
        }
    ) {
        when {
            state.isLoading -> item { PhotographLoadingPane() }
            !state.error.isNullOrBlank() && state.detail == null -> item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.PhotoCamera
                )
            }
            state.detail == null -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    EmptyState(
                        icon = Icons.Rounded.PhotoCamera,
                        message = stringResource(R.string.photograph_detail_missing),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            else -> {
                val detail = requireNotNull(state.detail)
                item { PhotographDetailHero(detail = detail) }
                item {
                    SectionCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.photograph_section_info),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
                        Text(text = detail.content, style = MaterialTheme.typography.bodyLarge)
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(10.dp))
                        Text(
                            text = "${detail.post.authorName} · ${photographCategoryLabel(detail.post.category)} · ${detail.post.createdAt}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                item {
                    SectionCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.photograph_section_gallery),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
                        if (detail.imageUrls.isEmpty()) {
                            Text(
                                text = stringResource(R.string.photograph_gallery_empty),
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
                        enabled = !state.isSubmitting && detail.post.isLiked.not(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (detail.post.isLiked) {
                                stringResource(R.string.photograph_liked_action)
                            } else {
                                stringResource(R.string.photograph_like_action)
                            }
                        )
                    }
                }
                item {
                    SectionCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.photograph_section_comments),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            placeholder = { Text(text = stringResource(R.string.photograph_comment_placeholder)) }
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
                        Button(
                            onClick = {
                                viewModel.submitComment(commentText)
                                commentText = ""
                            },
                            enabled = !state.isSubmitting && commentText.isNotBlank()
                        ) {
                            Text(text = stringResource(R.string.photograph_comment_send))
                        }
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(14.dp))
                        if (state.comments.isEmpty()) {
                            Text(
                                text = stringResource(R.string.photograph_comment_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            state.comments.forEach { comment ->
                                PhotographCommentRow(
                                    author = comment.authorName,
                                    content = comment.content,
                                    createdAt = comment.createdAt
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
fun PhotographProfileScreen(navController: NavHostController) {
    val viewModel: PhotographProfileViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyScreen(
        title = stringResource(R.string.photograph_profile_title),
        onBack = navController::popBackStack,
        actions = {
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.photograph_refresh))
            }
        }
    ) {
        item {
            SectionCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            ) {
                BadgePill(text = stringResource(R.string.photograph_profile_badge))
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(16.dp))
                Text(
                    text = stringResource(R.string.photograph_profile_subtitle),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        item {
            androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                    selected = state.selectedCategory == null,
                    onClick = { viewModel.selectCategory(null) },
                    label = { Text(text = stringResource(R.string.delivery_filter_all)) }
                )
                PhotographCategory.entries.forEach { category ->
                    FilterChip(
                        selected = state.selectedCategory == category,
                        onClick = { viewModel.selectCategory(category) },
                        label = { Text(text = photographCategoryLabel(category)) }
                    )
                }
            }
        }
        if (!state.error.isNullOrBlank()) {
            item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.PhotoCamera
                )
            }
        }
        when {
            state.isLoading && state.items.isEmpty() -> item { PhotographLoadingPane() }
            state.visibleItems.isEmpty() -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    EmptyState(
                        icon = Icons.Rounded.PhotoCamera,
                        message = stringResource(R.string.photograph_profile_empty),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            else -> {
                items(state.visibleItems, key = { it.id }) { post ->
                    PhotographPostCard(
                        post = post,
                        onClick = { navController.navigate(Routes.photographDetail(post.id)) }
                    )
                }
            }
        }
    }
}

@Composable
fun PhotographPublishScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: PhotographPublishViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var title by rememberSaveable { mutableStateOf("") }
    var content by rememberSaveable { mutableStateOf("") }
    var categoryRaw by rememberSaveable { mutableStateOf(PhotographCategory.LIFE.rawValue) }
    val category = remember(categoryRaw) { PhotographCategory.fromRemote(categoryRaw) }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(4)
    ) { uris ->
        viewModel.setImages(
            uris.map { uri ->
                SelectedLocalImage(
                    id = uri.toString(),
                    uri = uri,
                    displayName = queryDisplayName(context, uri)
                )
            }
        )
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is PhotographEvent.ShowMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
                PhotographEvent.Published -> {
                    navController.popBackStack()
                }
            }
        }
    }

    LazyScreen(
        title = stringResource(R.string.photograph_action_publish_title),
        onBack = navController::popBackStack
    ) {
        item {
            SectionCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            ) {
                BadgePill(text = stringResource(R.string.photograph_action_publish_title))
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(16.dp))
                Text(
                    text = stringResource(R.string.photograph_action_publish_subtitle),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        item {
            SectionCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = stringResource(R.string.photograph_publish_title_text)) }
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    label = { Text(text = stringResource(R.string.photograph_publish_content_text)) }
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
                androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PhotographCategory.entries.forEach { item ->
                        FilterChip(
                            selected = category == item,
                            onClick = { categoryRaw = item.rawValue },
                            label = { Text(text = photographCategoryLabel(item)) }
                        )
                    }
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
                Button(
                    onClick = {
                        pickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    enabled = !state.isSubmitting
                ) {
                    Text(text = stringResource(R.string.photograph_publish_pick_images))
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = stringResource(R.string.photograph_publish_selected_count, state.images.size),
                    style = MaterialTheme.typography.bodyMedium
                )
                if (state.images.isNotEmpty()) {
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
                    state.images.forEach { item ->
                        Text(
                            text = item.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(16.dp))
                Button(
                    onClick = { viewModel.publish(title = title, content = content, category = category) },
                    enabled = !state.isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (state.isSubmitting) {
                            stringResource(R.string.photograph_publish_submitting)
                        } else {
                            stringResource(R.string.photograph_publish_submit)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotographDetailHero(detail: PhotographPostDetail) {
    SectionCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    ) {
        BadgePill(text = photographCategoryLabel(detail.post.category))
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = detail.post.title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
        androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricChip(
                label = stringResource(R.string.photograph_stat_photo),
                value = detail.imageUrls.size.toString(),
                modifier = Modifier.weight(1f)
            )
            MetricChip(
                label = stringResource(R.string.photograph_stat_comment),
                value = maxOf(detail.comments.size, detail.post.commentCount).toString(),
                modifier = Modifier.weight(1f)
            )
            MetricChip(
                label = stringResource(R.string.photograph_stat_like),
                value = detail.post.likeCount.toString(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PhotographPostCard(
    post: PhotographPost,
    onClick: () -> Unit
) {
    SectionCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = post.createdAt,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(10.dp))
        Text(
            text = post.contentPreview,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
        androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BadgePill(text = photographCategoryLabel(post.category))
            if (post.photoCount > 0) {
                BadgePill(text = "${stringResource(R.string.photograph_stat_photo)} ${post.photoCount}", tint = MaterialTheme.colorScheme.tertiary)
            }
        }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(10.dp))
        Text(
            text = "${post.authorName} · ${stringResource(R.string.photograph_stat_like)} ${post.likeCount} · ${stringResource(R.string.photograph_stat_comment)} ${post.commentCount}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PhotographCommentRow(
    author: String,
    content: String,
    createdAt: String
) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = author,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = createdAt,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun photographCategoryLabel(category: PhotographCategory): String {
    return when (category) {
        PhotographCategory.CAMPUS -> stringResource(R.string.photograph_category_campus)
        PhotographCategory.LIFE -> stringResource(R.string.photograph_category_life)
    }
}

private fun queryDisplayName(context: Context, uri: Uri): String {
    val cursor = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
    cursor?.use {
        val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && it.moveToFirst()) {
            return it.getString(index).orEmpty()
        }
    }
    return uri.lastPathSegment.orEmpty()
}

@Composable
private fun PhotographLoadingPane() {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        ShimmerBox(modifier = Modifier.fillMaxWidth(0.5f), height = 28.dp)
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth(), height = 18.dp)
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(10.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth(0.84f), height = 18.dp)
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(10.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth(0.6f), height = 18.dp)
    }
}
