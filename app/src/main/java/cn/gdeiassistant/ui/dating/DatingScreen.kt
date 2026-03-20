package cn.gdeiassistant.ui.dating

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Workspaces
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import cn.gdeiassistant.model.DatingArea
import cn.gdeiassistant.model.DatingMyPost
import cn.gdeiassistant.model.DatingPickStatus
import cn.gdeiassistant.model.DatingReceivedPick
import cn.gdeiassistant.model.DatingSentPick
import cn.gdeiassistant.ui.components.EmptyState
import cn.gdeiassistant.ui.components.BadgePill
import cn.gdeiassistant.ui.components.GhostButton
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.MetricChip
import cn.gdeiassistant.ui.components.RemoteAvatar
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.StatusBanner
import cn.gdeiassistant.ui.components.TintButton
import kotlinx.coroutines.flow.collectLatest

@Composable
fun DatingCenterScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: DatingViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is DatingEvent.ShowMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    LazyScreen(
        title = stringResource(R.string.dating_title),
        onBack = navController::popBackStack,
        actions = {
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.dating_refresh))
            }
        }
    ) {
        item {
            SectionCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
            ) {
                BadgePill(text = stringResource(R.string.dating_badge))
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(16.dp))
                Text(
                    text = stringResource(R.string.dating_subtitle),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricChip(
                        label = stringResource(R.string.dating_metric_received),
                        value = state.receivedItems.size.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    MetricChip(
                        label = stringResource(R.string.dating_metric_posts),
                        value = state.myPosts.size.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DatingCenterTab.entries.forEach { tab ->
                    FilterChip(
                        selected = state.selectedTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        label = { Text(text = datingTabLabel(tab)) }
                    )
                }
            }
        }
        if (!state.error.isNullOrBlank()) {
            item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.Workspaces
                )
            }
        }
        when {
            state.isLoading &&
                state.receivedItems.isEmpty() &&
                state.sentItems.isEmpty() &&
                state.myPosts.isEmpty() -> item { DatingLoadingPane() }
            state.selectedTab == DatingCenterTab.RECEIVED && state.receivedItems.isEmpty() -> item {
                DatingEmptyState(message = stringResource(R.string.dating_received_empty))
            }
            state.selectedTab == DatingCenterTab.SENT && state.sentItems.isEmpty() -> item {
                DatingEmptyState(message = stringResource(R.string.dating_sent_empty))
            }
            state.selectedTab == DatingCenterTab.POSTS && state.myPosts.isEmpty() -> item {
                DatingEmptyState(message = stringResource(R.string.dating_posts_empty))
            }
            else -> when (state.selectedTab) {
                DatingCenterTab.RECEIVED -> {
                    items(state.receivedItems, key = { it.id }) { item ->
                        DatingReceivedCard(
                            item = item,
                            onAccept = { viewModel.updatePickState(item.id, DatingPickStatus.ACCEPTED) },
                            onReject = { viewModel.updatePickState(item.id, DatingPickStatus.REJECTED) }
                        )
                    }
                }
                DatingCenterTab.SENT -> {
                    items(state.sentItems, key = { it.id }) { item ->
                        DatingSentCard(item = item)
                    }
                }
                DatingCenterTab.POSTS -> {
                    items(state.myPosts, key = { it.id }) { item ->
                        DatingPostCard(
                            item = item,
                            onHide = { viewModel.hideProfile(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DatingReceivedCard(
    item: DatingReceivedPick,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RemoteAvatar(
                    imageModel = item.avatarUrl,
                    fallbackLabel = item.senderName,
                    size = 44.dp
                )
                androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.senderName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = item.time,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (item.status != DatingPickStatus.PENDING) {
                BadgePill(text = datingStatusLabel(item.status))
            }
        }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = item.content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
        if (item.status == DatingPickStatus.PENDING) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TintButton(
                    text = stringResource(R.string.dating_accept_action),
                    onClick = onAccept,
                    tint = MaterialTheme.colorScheme.secondary
                )
                GhostButton(
                    text = stringResource(R.string.dating_reject_action),
                    onClick = onReject,
                    borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.24f),
                    contentColor = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun DatingSentCard(item: DatingSentPick) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RemoteAvatar(
                    imageModel = item.targetAvatarUrl,
                    fallbackLabel = item.targetName,
                    size = 44.dp
                )
                Text(
                    text = item.targetName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            BadgePill(text = datingStatusLabel(item.status))
        }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = item.content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (item.status == DatingPickStatus.ACCEPTED) {
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
            item.targetQq?.takeIf { it.isNotBlank() }?.let { qq ->
                Text(
                    text = stringResource(R.string.dating_contact_qq, qq),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item.targetWechat?.takeIf { it.isNotBlank() }?.let { wechat ->
                Text(
                    text = stringResource(R.string.dating_contact_wechat, wechat),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DatingPostCard(
    item: DatingMyPost,
    onHide: () -> Unit
) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RemoteAvatar(
                    imageModel = item.imageUrl,
                    fallbackLabel = item.name,
                    size = 44.dp
                )
                androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = "${item.grade} · ${item.faculty} · ${stringResource(R.string.dating_hometown_prefix, item.hometown)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = item.publishTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            GhostButton(
                text = stringResource(R.string.dating_hide_action),
                onClick = onHide,
                borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.24f),
                contentColor = MaterialTheme.colorScheme.error
            )
        }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BadgePill(text = datingAreaLabel(item.area))
            BadgePill(text = item.grade, tint = MaterialTheme.colorScheme.tertiary)
        }
    }
}

@Composable
private fun DatingEmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    ) {
        EmptyState(
            icon = Icons.Rounded.Person,
            message = message,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun DatingLoadingPane() {
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
private fun datingTabLabel(tab: DatingCenterTab): String {
    return when (tab) {
        DatingCenterTab.RECEIVED -> stringResource(R.string.dating_tab_received)
        DatingCenterTab.SENT -> stringResource(R.string.dating_tab_sent)
        DatingCenterTab.POSTS -> stringResource(R.string.dating_tab_posts)
    }
}

@Composable
private fun datingStatusLabel(status: DatingPickStatus): String {
    return when (status) {
        DatingPickStatus.PENDING -> stringResource(R.string.dating_status_pending)
        DatingPickStatus.ACCEPTED -> stringResource(R.string.dating_status_accepted)
        DatingPickStatus.REJECTED -> stringResource(R.string.dating_status_rejected)
    }
}

@Composable
private fun datingAreaLabel(area: DatingArea): String {
    return when (area) {
        DatingArea.GIRL -> stringResource(R.string.dating_area_girl)
        DatingArea.BOY -> stringResource(R.string.dating_area_boy)
    }
}
