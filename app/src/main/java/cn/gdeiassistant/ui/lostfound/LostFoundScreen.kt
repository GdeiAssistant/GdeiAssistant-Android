package cn.gdeiassistant.ui.lostfound

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.NoteAdd
import androidx.compose.material.icons.rounded.LockPerson
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import cn.gdeiassistant.model.LostFoundDetail
import cn.gdeiassistant.model.LostFoundItem
import cn.gdeiassistant.model.LostFoundItemState
import cn.gdeiassistant.model.LostFoundPersonalSummary
import cn.gdeiassistant.model.LostFoundType
import cn.gdeiassistant.ui.components.EmptyState
import cn.gdeiassistant.ui.components.ActionTile
import cn.gdeiassistant.ui.components.BadgePill
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.MetricChip
import cn.gdeiassistant.ui.components.NativeImageGallery
import cn.gdeiassistant.ui.components.GhostButton
import cn.gdeiassistant.ui.components.RemoteAvatar
import cn.gdeiassistant.ui.components.RemoteThumbnail
import cn.gdeiassistant.ui.components.SelectionPill
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.StatusBanner
import cn.gdeiassistant.ui.components.TextTabSelector
import cn.gdeiassistant.ui.components.TintButton
import cn.gdeiassistant.ui.navigation.Routes
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LostFoundScreen(navController: NavHostController) {
    val viewModel: LostFoundViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle

    LaunchedEffect(savedStateHandle) {
        savedStateHandle?.getStateFlow(Routes.LOST_FOUND_REFRESH_FLAG, false)?.collectLatest { needsRefresh ->
            if (needsRefresh) {
                viewModel.refresh()
                savedStateHandle[Routes.LOST_FOUND_REFRESH_FLAG] = false
            }
        }
    }

    LazyScreen(
        title = stringResource(R.string.lost_found_title),
        onBack = navController::popBackStack,
        actions = {
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.lost_found_refresh))
            }
        }
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionTile(
                    title = stringResource(R.string.lost_found_my_items_title),
                    subtitle = stringResource(R.string.lost_found_my_items_subtitle),
                    icon = Icons.Rounded.Person,
                    onClick = { navController.navigate(Routes.LOST_FOUND_PROFILE) },
                    tint = MaterialTheme.colorScheme.primary,
                    emphasized = true,
                    modifier = Modifier.weight(1f)
                )
                ActionTile(
                    title = stringResource(R.string.lost_found_publish_title),
                    subtitle = stringResource(R.string.lost_found_publish_subtitle),
                    icon = Icons.AutoMirrored.Rounded.NoteAdd,
                    onClick = { navController.navigate(Routes.LOST_FOUND_PUBLISH) },
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier.horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LostFoundFilter.entries.forEach { filter ->
                    SelectionPill(
                        text = lostFoundFilterLabel(filter),
                        selected = state.selectedFilter == filter,
                        onClick = { viewModel.selectFilter(filter) }
                    )
                }
            }
        }
        if (!state.error.isNullOrBlank()) {
            item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.LockPerson,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
        when {
            state.isLoading && state.items.isEmpty() -> item { LoadingPane() }
            state.visibleItems.isEmpty() -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    EmptyState(
                        icon = Icons.Rounded.LockPerson,
                        message = stringResource(R.string.lost_found_empty),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            else -> {
                items(state.visibleItems, key = { it.id }) { item ->
                    LostFoundItemCard(
                        item = item,
                        onClick = { navController.navigate(Routes.lostFoundDetail(item.id)) }
                    )
                }
            }
        }
    }
}

@Composable
fun LostFoundDetailScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: LostFoundDetailViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val detail = state.detail

    LazyScreen(
        title = stringResource(R.string.lost_found_detail_title),
        onBack = navController::popBackStack,
        actions = {
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.lost_found_refresh))
            }
        }
    ) {
        when {
            state.isLoading -> item { LoadingPane() }
            !state.error.isNullOrBlank() -> item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.LockPerson,
                    tint = MaterialTheme.colorScheme.error
                )
            }
            detail == null -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    EmptyState(
                        icon = Icons.Rounded.LockPerson,
                        message = stringResource(R.string.lost_found_detail_missing),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            else -> {
                item { LostFoundDetailHero(detail = detail) }
                if (detail.imageUrls.isNotEmpty()) {
                    item {
                        SectionCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = stringResource(R.string.lost_found_images_title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(14.dp))
                            NativeImageGallery(
                                imageUrls = detail.imageUrls,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                item {
                    ActionTile(
                        title = stringResource(R.string.lost_found_contact_title),
                        subtitle = detail.contactHint,
                        icon = Icons.Rounded.Person,
                        onClick = {
                            Toast.makeText(context, detail.contactHint, Toast.LENGTH_LONG).show()
                        },
                        tint = MaterialTheme.colorScheme.tertiary,
                        emphasized = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    SectionCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.lost_found_description_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(14.dp))
                        Text(
                            text = detail.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                item {
                    SectionCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.lost_found_owner_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RemoteAvatar(
                                imageModel = detail.ownerAvatarUrl,
                                fallbackLabel = detail.ownerNickname ?: detail.ownerUsername ?: "G",
                                size = 52.dp
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = detail.ownerNickname ?: detail.ownerUsername ?: "—",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(4.dp))
                                Text(
                                    text = detail.ownerUsername ?: detail.statusText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(14.dp))
                        DetailRow(stringResource(R.string.lost_found_owner_name_label), detail.ownerNickname ?: detail.ownerUsername ?: "—")
                        DetailRow(stringResource(R.string.lost_found_status_label), detail.statusText)
                    }
                }
            }
        }
    }
}

@Composable
fun LostFoundProfileScreen(navController: NavHostController) {
    val viewModel: LostFoundProfileViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var actionMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingMarkItemId by remember { mutableStateOf<String?>(null) }
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle

    androidx.compose.runtime.LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is LostFoundProfileEvent.ShowMessage -> {
                    actionMessage = event.message
                }
            }
        }
    }

    LaunchedEffect(savedStateHandle) {
        savedStateHandle?.getStateFlow(Routes.LOST_FOUND_PROFILE_REFRESH_FLAG, false)?.collectLatest { needsRefresh ->
            if (needsRefresh) {
                viewModel.refresh()
                savedStateHandle[Routes.LOST_FOUND_PROFILE_REFRESH_FLAG] = false
            }
        }
    }

    LazyScreen(
        title = stringResource(R.string.lost_found_profile_title),
        onBack = navController::popBackStack,
        actions = {
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.lost_found_refresh))
            }
        }
    ) {
        when {
            state.isLoading && state.summary == null -> item { LoadingPane() }
            !state.error.isNullOrBlank() -> item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.Person,
                    tint = MaterialTheme.colorScheme.error
                )
            }
            state.summary == null -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    EmptyState(
                        icon = Icons.Rounded.Person,
                        message = stringResource(R.string.lost_found_profile_empty),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            else -> {
                val summary = requireNotNull(state.summary)
                item {
                    SectionCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RemoteAvatar(
                                imageModel = summary.avatarUrl,
                                fallbackLabel = summary.nickname,
                                size = 60.dp
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                BadgePill(text = summary.nickname)
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(10.dp))
                                Text(
                                    text = summary.introduction,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MetricChip(
                                label = stringResource(R.string.lost_found_profile_tab_lost),
                                value = summary.lost.size.toString(),
                                modifier = Modifier.weight(1f)
                            )
                            MetricChip(
                                label = stringResource(R.string.lost_found_profile_tab_found),
                                value = summary.found.size.toString(),
                                modifier = Modifier.weight(1f)
                            )
                            MetricChip(
                                label = stringResource(R.string.lost_found_profile_tab_did_found),
                                value = summary.didFound.size.toString(),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                actionMessage?.let { message ->
                    item {
                        SectionCard(
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.07f)
                        ) {
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                item {
                    TextTabSelector(
                        labels = listOf(
                            stringResource(R.string.lost_found_profile_tab_lost),
                            stringResource(R.string.lost_found_profile_tab_found),
                            stringResource(R.string.lost_found_profile_tab_did_found)
                        ),
                        selectedIndex = state.selectedTab.ordinal,
                        onSelect = { index -> viewModel.selectTab(LostFoundProfileTab.entries[index]) },
                        modifier = Modifier.fillMaxWidth(),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                item {
                    AnimatedContent(
                        targetState = state.selectedTab to state.visibleItems,
                        label = "lost_found_profile_tab"
                    ) { contentState ->
                        val selectedTab = contentState.first
                        val visibleItems = contentState.second
                        if (visibleItems.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp)
                            ) {
                                EmptyState(
                                    icon = Icons.Rounded.LockPerson,
                                    message = stringResource(R.string.lost_found_profile_tab_empty),
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                visibleItems.forEach { item ->
                                    LostFoundProfileCard(
                                        item = item,
                                        tab = selectedTab,
                                        onOpen = { navController.navigate(Routes.lostFoundDetail(item.id)) },
                                        onEdit = { navController.navigate(Routes.lostFoundEdit(item.id)) },
                                        onRequestMarkDidFound = { pendingMarkItemId = item.id }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    pendingMarkItemId?.let { itemId ->
        AlertDialog(
            onDismissRequest = { pendingMarkItemId = null },
            title = { Text(text = stringResource(R.string.lost_found_action_mark_found)) },
            text = { Text(text = stringResource(R.string.lost_found_mark_found_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingMarkItemId = null
                        viewModel.markDidFound(itemId)
                    }
                ) {
                    Text(text = stringResource(R.string.lost_found_action_mark_found))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingMarkItemId = null }) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun LostFoundDetailHero(detail: LostFoundDetail) {
    SectionCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    ) {
        BadgePill(
            text = if (detail.item.type == LostFoundType.LOST) stringResource(R.string.lost_found_filter_lost) else stringResource(R.string.lost_found_filter_found)
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = detail.item.title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricChip(
                label = stringResource(R.string.lost_found_location_label),
                value = detail.item.location,
                modifier = Modifier.weight(1f)
            )
            MetricChip(
                label = stringResource(R.string.lost_found_status_label),
                value = detail.statusText,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LostFoundItemCard(
    item: LostFoundItem,
    onClick: () -> Unit
) {
    SectionCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            if (item.previewImageUrl != null) {
                RemoteThumbnail(
                    imageModel = item.previewImageUrl,
                    fallbackLabel = item.title,
                    width = 84.dp,
                    height = 84.dp
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
                    BadgePill(
                        text = if (item.type == LostFoundType.LOST) stringResource(R.string.lost_found_filter_lost) else stringResource(R.string.lost_found_filter_found)
                    )
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = item.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BadgePill(text = item.createdAt)
                    Text(
                        text = item.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun LostFoundProfileCard(
    item: LostFoundItem,
    tab: LostFoundProfileTab,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onRequestMarkDidFound: () -> Unit
) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpen),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            RemoteThumbnail(
                imageModel = item.previewImageUrl,
                fallbackLabel = item.title
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = item.createdAt,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BadgePill(
                        text = if (item.type == LostFoundType.LOST) stringResource(R.string.lost_found_filter_lost) else stringResource(R.string.lost_found_filter_found)
                    )
                    BadgePill(text = item.location, tint = MaterialTheme.colorScheme.tertiary)
                }
            }
        }
        if (tab != LostFoundProfileTab.DID_FOUND) {
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GhostButton(
                    text = stringResource(R.string.lost_found_action_edit),
                    onClick = onEdit
                )
                TintButton(
                    text = stringResource(R.string.lost_found_action_mark_found),
                    onClick = onRequestMarkDidFound,
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun LoadingPane() {
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
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(10.dp))
}

@Composable
private fun lostFoundFilterLabel(filter: LostFoundFilter): String {
    return when (filter) {
        LostFoundFilter.ALL -> stringResource(R.string.lost_found_filter_all)
        LostFoundFilter.LOST -> stringResource(R.string.lost_found_filter_lost)
        LostFoundFilter.FOUND -> stringResource(R.string.lost_found_filter_found)
    }
}

@Composable
private fun lostFoundProfileTabLabel(tab: LostFoundProfileTab): String {
    return when (tab) {
        LostFoundProfileTab.LOST -> stringResource(R.string.lost_found_profile_tab_lost)
        LostFoundProfileTab.FOUND -> stringResource(R.string.lost_found_profile_tab_found)
        LostFoundProfileTab.DID_FOUND -> stringResource(R.string.lost_found_profile_tab_did_found)
    }
}
