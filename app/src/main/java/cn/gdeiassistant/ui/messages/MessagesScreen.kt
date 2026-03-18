package cn.gdeiassistant.ui.messages

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Newspaper
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import cn.gdeiassistant.model.AnnouncementItem
import cn.gdeiassistant.model.InteractionMessage
import cn.gdeiassistant.model.SchoolNews
import cn.gdeiassistant.model.toArticleDetailContent
import cn.gdeiassistant.ui.components.BadgePill
import cn.gdeiassistant.ui.components.EmptyState
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.StatusBanner
import cn.gdeiassistant.ui.navigation.Routes
import cn.gdeiassistant.ui.theme.AppShapes
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MessagesScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: MessagesViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is MessagesEvent.ShowMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    LazyScreen(
        title = stringResource(R.string.messages_title),
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
            MessagesOverviewCard(state = state)
        }
        item {
            NewsSection(
                items = state.newsItems.take(3),
                error = state.newsError,
                onOpenAll = { navController.navigate(Routes.NEWS) },
                onOpenItem = { item ->
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set(Routes.ARTICLE_DETAIL_CONTENT, item.toArticleDetailContent())
                    navController.navigate(Routes.ARTICLE_DETAIL)
                }
            )
        }
        item {
            AnnouncementSection(
                items = state.announcementItems.take(3),
                error = state.announcementError,
                onOpenAll = { navController.navigate(Routes.NOTICE_LIST) },
                onOpenItem = { item ->
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set(Routes.ARTICLE_DETAIL_CONTENT, item.toArticleDetailContent())
                    navController.navigate(Routes.ARTICLE_DETAIL)
                }
            )
        }
        item {
            InteractionSection(
                items = state.interactionItems.take(3),
                error = state.interactionError,
                unreadCount = state.unreadCount,
                onMarkRead = viewModel::markMessageRead,
                onOpenItem = { item -> navController.openInteractionMessage(item) },
                onMarkAllRead = viewModel::markAllRead,
                onViewAll = { navController.navigate(Routes.INTERACTION_LIST) }
            )
        }
    }
}

@Composable
private fun MessagesOverviewCard(state: MessagesUiState) {
    val metrics = listOf(
        Triple(
            stringResource(R.string.messages_metric_news),
            state.newsItems.size.toString(),
            MaterialTheme.colorScheme.primary
        ),
        Triple(
            stringResource(R.string.messages_system_notice_badge),
            state.announcementItems.size.toString(),
            MaterialTheme.colorScheme.secondary
        ),
        Triple(
            stringResource(R.string.messages_metric_unread),
            state.unreadCount.toString(),
            MaterialTheme.colorScheme.error
        )
    )

    SectionCard(modifier = Modifier.fillMaxWidth()) {
        BadgePill(text = stringResource(R.string.messages_badge))
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.messages_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            metrics.forEach { (label, value, tint) ->
                Surface(
                    color = tint.copy(alpha = 0.12f),
                    shape = AppShapes.small
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            color = tint
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InteractionSection(
    items: List<InteractionMessage>,
    error: String?,
    unreadCount: Int,
    onMarkRead: (String) -> Unit,
    onOpenItem: (InteractionMessage) -> Unit,
    onMarkAllRead: () -> Unit,
    onViewAll: () -> Unit
) {
    InfoSectionCard(
        title = stringResource(R.string.messages_interaction_title),
        subtitle = stringResource(R.string.messages_interaction_badge),
        icon = Icons.Rounded.Bolt,
        tint = MaterialTheme.colorScheme.primary,
        action = if (unreadCount > 0) stringResource(R.string.messages_mark_all_read) else null,
        onAction = if (unreadCount > 0) onMarkAllRead else null,
        secondAction = stringResource(R.string.home_view_all),
        onSecondAction = onViewAll
    ) {
        SectionContent(
            isEmpty = items.isEmpty(),
            error = error,
            errorIcon = Icons.Rounded.Bolt,
            emptyIcon = Icons.Rounded.Bolt,
            emptyMessage = stringResource(R.string.messages_interaction_empty)
        ) {
            items.forEachIndexed { index, item ->
                if (index > 0) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                }
                MessageRow(
                    title = item.title,
                    body = item.content,
                    date = item.createdAt,
                    badge = interactionModuleLabel(item.module),
                    trailing = if (item.isRead) {
                        stringResource(R.string.messages_read)
                    } else {
                        stringResource(R.string.messages_unread)
                    },
                    onClick = {
                        onMarkRead(item.id)
                        onOpenItem(item)
                    }
                )
            }
        }
    }
}

@Composable
private fun NewsSection(
    items: List<SchoolNews>,
    error: String?,
    onOpenAll: () -> Unit,
    onOpenItem: (SchoolNews) -> Unit
) {
    InfoSectionCard(
        title = stringResource(R.string.news_title),
        subtitle = stringResource(R.string.messages_news_action_subtitle),
        icon = Icons.Rounded.Newspaper,
        tint = MaterialTheme.colorScheme.primary,
        action = stringResource(R.string.home_view_all),
        onAction = onOpenAll
    ) {
        SectionContent(
            isEmpty = items.isEmpty(),
            error = error,
            errorIcon = Icons.Rounded.Newspaper,
            emptyIcon = Icons.Rounded.Newspaper,
            emptyMessage = stringResource(R.string.news_empty)
        ) {
            items.forEachIndexed { index, item ->
                if (index > 0) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                }
                MessageRow(
                    title = item.title,
                    body = item.content.ifBlank { stringResource(R.string.article_detail_empty_body) },
                    date = item.publishDate,
                    badge = stringResource(R.string.news_title),
                    onClick = { onOpenItem(item) }
                )
            }
        }
    }
}

@Composable
private fun AnnouncementSection(
    items: List<AnnouncementItem>,
    error: String?,
    onOpenAll: () -> Unit,
    onOpenItem: (AnnouncementItem) -> Unit
) {
    InfoSectionCard(
        title = stringResource(R.string.messages_system_notice_title),
        subtitle = stringResource(R.string.messages_system_notice_badge),
        icon = Icons.Rounded.Campaign,
        tint = MaterialTheme.colorScheme.secondary,
        action = stringResource(R.string.home_view_all),
        onAction = onOpenAll
    ) {
        SectionContent(
            isEmpty = items.isEmpty(),
            error = error,
            errorIcon = Icons.Rounded.Campaign,
            emptyIcon = Icons.Rounded.Campaign,
            emptyMessage = stringResource(R.string.messages_system_notice_empty)
        ) {
            items.forEachIndexed { index, item ->
                if (index > 0) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                }
                MessageRow(
                    title = item.title,
                    body = item.content.ifBlank { stringResource(R.string.article_detail_empty_body) },
                    date = item.publishTime,
                    badge = stringResource(R.string.messages_system_notice_badge),
                    onClick = { onOpenItem(item) }
                )
            }
        }
    }
}

@Composable
private fun interactionModuleLabel(module: String?): String {
    return when (module) {
        "secret" -> stringResource(R.string.messages_module_secret)
        "secondhand" -> stringResource(R.string.messages_module_marketplace)
        "lostandfound" -> stringResource(R.string.messages_module_lost_found)
        "topic" -> stringResource(R.string.messages_module_topic)
        "express" -> stringResource(R.string.messages_module_express)
        "dating" -> stringResource(R.string.messages_module_dating)
        "delivery" -> stringResource(R.string.messages_module_delivery)
        "photograph" -> stringResource(R.string.messages_module_photograph)
        else -> stringResource(R.string.messages_interaction_badge)
    }
}

@Composable
private fun InfoSectionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    action: String? = null,
    onAction: (() -> Unit)? = null,
    secondAction: String? = null,
    onSecondAction: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = tint.copy(alpha = 0.12f),
                    shape = AppShapes.small
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = tint)
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (action != null && onAction != null) {
                    Text(
                        text = action,
                        style = MaterialTheme.typography.labelLarge,
                        color = tint,
                        modifier = Modifier.clickable(onClick = onAction)
                    )
                }
                if (secondAction != null && onSecondAction != null) {
                    Text(
                        text = secondAction,
                        style = MaterialTheme.typography.labelLarge,
                        color = tint,
                        modifier = Modifier.clickable(onClick = onSecondAction)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        content()
    }
}

@Composable
private fun SectionContent(
    isEmpty: Boolean,
    error: String?,
    errorIcon: ImageVector,
    emptyIcon: ImageVector,
    emptyMessage: String,
    content: @Composable ColumnScope.() -> Unit
) {
    AnimatedContent(
        targetState = when {
            !error.isNullOrBlank() && isEmpty -> "error"
            isEmpty -> "empty"
            else -> "content"
        },
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "messages_section_state"
    ) { state ->
        when (state) {
            "error" -> ErrorBanner(error = error.orEmpty(), icon = errorIcon)
            "empty" -> EmptyMiniState(icon = emptyIcon, message = emptyMessage)
            else -> Column(content = content)
        }
    }
}

@Composable
private fun MessageRow(
    title: String,
    body: String,
    date: String,
    badge: String,
    trailing: String? = null,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BadgePill(text = badge)
            trailing?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = date,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorBanner(
    error: String,
    icon: ImageVector
) {
    StatusBanner(
        title = stringResource(R.string.load_failed),
        body = error,
        icon = icon
    )
}

@Composable
private fun EmptyMiniState(
    icon: ImageVector,
    message: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        EmptyState(
            icon = icon,
            message = message,
            modifier = Modifier.fillMaxSize()
        )
    }
}
