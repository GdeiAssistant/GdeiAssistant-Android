package cn.gdeiassistant.ui.marketplace

import android.widget.Toast
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.automirrored.rounded.NoteAdd
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
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
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
import cn.gdeiassistant.model.MarketplaceDetail
import cn.gdeiassistant.model.MarketplaceItem
import cn.gdeiassistant.model.MarketplaceItemState
import cn.gdeiassistant.ui.components.EmptyState
import cn.gdeiassistant.ui.components.ActionTile
import cn.gdeiassistant.ui.components.BadgePill
import cn.gdeiassistant.ui.components.GhostButton
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.MetricChip
import cn.gdeiassistant.ui.components.NativeImageGallery
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
fun MarketplaceScreen(navController: NavHostController) {
    val viewModel: MarketplaceViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle

    LaunchedEffect(savedStateHandle) {
        savedStateHandle?.getStateFlow(Routes.MARKETPLACE_REFRESH_FLAG, false)?.collectLatest { needsRefresh ->
            if (needsRefresh) {
                viewModel.refresh()
                savedStateHandle[Routes.MARKETPLACE_REFRESH_FLAG] = false
            }
        }
    }

    LazyScreen(
        title = stringResource(R.string.marketplace_title),
        onBack = navController::popBackStack,
        actions = {
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.marketplace_refresh))
            }
        }
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionTile(
                    title = stringResource(R.string.marketplace_my_items_title),
                    subtitle = stringResource(R.string.marketplace_my_items_subtitle),
                    icon = Icons.Rounded.Person,
                    onClick = { navController.navigate(Routes.MARKETPLACE_PROFILE) },
                    tint = MaterialTheme.colorScheme.primary,
                    emphasized = true,
                    modifier = Modifier.weight(1f)
                )
                ActionTile(
                    title = stringResource(R.string.marketplace_publish_title),
                    subtitle = stringResource(R.string.marketplace_publish_subtitle),
                    icon = Icons.AutoMirrored.Rounded.NoteAdd,
                    onClick = { navController.navigate(Routes.MARKETPLACE_PUBLISH) },
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
                SelectionPill(
                    text = stringResource(R.string.marketplace_filter_all),
                    selected = state.selectedTypeId == null,
                    onClick = { viewModel.selectType(null) }
                )
                state.typeOptions.forEach { option ->
                    SelectionPill(
                        text = option.title,
                        selected = state.selectedTypeId == option.id,
                        onClick = { viewModel.selectType(option.id) }
                    )
                }
            }
        }
        if (!state.error.isNullOrBlank()) {
            item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.AccountBalanceWallet,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
        when {
            state.isLoading && state.items.isEmpty() -> item { LoadingPane() }
            state.items.isEmpty() -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    EmptyState(
                        icon = Icons.Rounded.AccountBalanceWallet,
                        message = stringResource(R.string.marketplace_empty),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            else -> {
                items(state.items, key = { it.id }) { item ->
                    MarketplaceItemCard(
                        item = item,
                        onClick = { navController.navigate(Routes.marketplaceDetail(item.id)) }
                    )
                }
            }
        }
    }
}

@Composable
fun MarketplaceDetailScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: MarketplaceDetailViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val detail = state.detail

    LazyScreen(
        title = stringResource(R.string.marketplace_detail_title),
        onBack = navController::popBackStack,
        actions = {
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.marketplace_refresh))
            }
        }
    ) {
        when {
            state.isLoading -> item { LoadingPane() }
            !state.error.isNullOrBlank() -> item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.AccountBalanceWallet,
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
                        icon = Icons.Rounded.AccountBalanceWallet,
                        message = stringResource(R.string.marketplace_detail_missing),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            else -> {
                item { MarketplaceDetailHero(detail = detail) }
                if (detail.imageUrls.isNotEmpty()) {
                    item {
                        SectionCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = stringResource(R.string.marketplace_images_title),
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
                        title = stringResource(R.string.marketplace_contact_title),
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
                            text = stringResource(R.string.marketplace_description_title),
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
                            text = stringResource(R.string.marketplace_seller_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(14.dp))
                        DetailRow(stringResource(R.string.marketplace_seller_name_label), detail.sellerNickname ?: detail.item.sellerName)
                        DetailRow(stringResource(R.string.marketplace_seller_college_label), detail.sellerCollege ?: "—")
                        DetailRow(stringResource(R.string.marketplace_seller_major_label), detail.sellerMajor ?: "—")
                        DetailRow(stringResource(R.string.marketplace_seller_grade_label), detail.sellerGrade ?: "—")
                    }
                }
            }
        }
    }
}

@Composable
fun MarketplaceProfileScreen(navController: NavHostController) {
    val viewModel: MarketplaceProfileViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var actionMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingStateChange by remember { mutableStateOf<MarketplaceStateChangeRequest?>(null) }
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle

    androidx.compose.runtime.LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is MarketplaceProfileEvent.ShowMessage -> {
                    actionMessage = event.message
                }
            }
        }
    }

    LaunchedEffect(savedStateHandle) {
        savedStateHandle?.getStateFlow(Routes.MARKETPLACE_PROFILE_REFRESH_FLAG, false)?.collectLatest { needsRefresh ->
            if (needsRefresh) {
                viewModel.refresh()
                savedStateHandle[Routes.MARKETPLACE_PROFILE_REFRESH_FLAG] = false
            }
        }
    }

    LazyScreen(
        title = stringResource(R.string.marketplace_profile_title),
        onBack = navController::popBackStack,
        actions = {
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.marketplace_refresh))
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
                        message = stringResource(R.string.marketplace_profile_empty),
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
                                label = stringResource(R.string.marketplace_profile_tab_doing),
                                value = summary.doing.size.toString(),
                                modifier = Modifier.weight(1f)
                            )
                            MetricChip(
                                label = stringResource(R.string.marketplace_profile_tab_sold),
                                value = summary.sold.size.toString(),
                                modifier = Modifier.weight(1f)
                            )
                            MetricChip(
                                label = stringResource(R.string.marketplace_profile_tab_off),
                                value = summary.off.size.toString(),
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
                            stringResource(R.string.marketplace_profile_tab_doing),
                            stringResource(R.string.marketplace_profile_tab_sold),
                            stringResource(R.string.marketplace_profile_tab_off)
                        ),
                        selectedIndex = state.selectedTab.ordinal,
                        onSelect = { index -> viewModel.selectTab(MarketplaceProfileTab.entries[index]) },
                        modifier = Modifier.fillMaxWidth(),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                item {
                    AnimatedContent(
                        targetState = state.selectedTab,
                        label = "marketplace_profile_tab"
                    ) {
                        if (state.visibleItems.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp)
                            ) {
                                EmptyState(
                                    icon = Icons.Rounded.AccountBalanceWallet,
                                    message = stringResource(R.string.marketplace_profile_tab_empty),
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                state.visibleItems.forEach { item ->
                                    MarketplaceProfileCard(
                                        item = item,
                                        tab = state.selectedTab,
                                        onOpen = { navController.navigate(Routes.marketplaceDetail(item.id)) },
                                        onEdit = { navController.navigate(Routes.marketplaceEdit(item.id)) },
                                        onRequestStateChange = { request ->
                                            pendingStateChange = request
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    pendingStateChange?.let { request ->
        AlertDialog(
            onDismissRequest = { pendingStateChange = null },
            title = { Text(text = request.actionText) },
            text = { Text(text = stringResource(R.string.marketplace_state_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingStateChange = null
                        viewModel.updateItemState(request.itemId, request.targetState)
                    }
                ) {
                    Text(text = request.actionText)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingStateChange = null }) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun MarketplaceDetailHero(detail: MarketplaceDetail) {
    SectionCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    ) {
        BadgePill(
            text = detail.condition
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
                label = stringResource(R.string.marketplace_price_label),
                value = String.format("¥%.2f", detail.item.price),
                modifier = Modifier.weight(1f)
            )
            MetricChip(
                label = stringResource(R.string.marketplace_location_label),
                value = detail.item.location,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MarketplaceItemCard(
    item: MarketplaceItem,
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
                    Text(
                        text = String.format("¥%.2f", item.price),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
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
                    BadgePill(text = item.tags.firstOrNull() ?: stringResource(R.string.marketplace_filter_all))
                    Text(
                        text = "${item.sellerName} · ${item.location} · ${item.postedAt}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MarketplaceProfileCard(
    item: MarketplaceItem,
    tab: MarketplaceProfileTab,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onRequestStateChange: (MarketplaceStateChangeRequest) -> Unit
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
                    text = String.format("¥%.2f", item.price),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = item.postedAt,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BadgePill(text = item.tags.firstOrNull() ?: stringResource(R.string.marketplace_filter_all))
                    BadgePill(text = item.location, tint = MaterialTheme.colorScheme.tertiary)
                }
            }
        }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            when (tab) {
                MarketplaceProfileTab.DOING -> {
                    val soldActionText = stringResource(R.string.marketplace_action_sold)
                    val offShelfActionText = stringResource(R.string.marketplace_action_off_shelf)
                    GhostButton(
                        text = stringResource(R.string.marketplace_action_edit),
                        onClick = onEdit
                    )
                    TintButton(
                        text = soldActionText,
                        onClick = {
                            onRequestStateChange(
                                MarketplaceStateChangeRequest(
                                    itemId = item.id,
                                    targetState = MarketplaceItemState.SOLD,
                                    actionText = soldActionText
                                )
                            )
                        },
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    GhostButton(
                        text = offShelfActionText,
                        onClick = {
                            onRequestStateChange(
                                MarketplaceStateChangeRequest(
                                    itemId = item.id,
                                    targetState = MarketplaceItemState.OFF_SHELF,
                                    actionText = offShelfActionText
                                )
                            )
                        },
                        borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.24f),
                        contentColor = MaterialTheme.colorScheme.error
                    )
                }
                MarketplaceProfileTab.SOLD -> {
                    Text(
                        text = stringResource(R.string.marketplace_sold_readonly_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                MarketplaceProfileTab.OFF -> {
                    val putBackActionText = stringResource(R.string.marketplace_action_put_back)
                    GhostButton(
                        text = stringResource(R.string.marketplace_action_edit),
                        onClick = onEdit
                    )
                    TintButton(
                        text = putBackActionText,
                        onClick = {
                            onRequestStateChange(
                                MarketplaceStateChangeRequest(
                                    itemId = item.id,
                                    targetState = MarketplaceItemState.SELLING,
                                    actionText = putBackActionText
                                )
                            )
                        },
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
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
private fun marketplaceTabLabel(tab: MarketplaceProfileTab): String {
    return when (tab) {
        MarketplaceProfileTab.DOING -> stringResource(R.string.marketplace_profile_tab_doing)
        MarketplaceProfileTab.SOLD -> stringResource(R.string.marketplace_profile_tab_sold)
        MarketplaceProfileTab.OFF -> stringResource(R.string.marketplace_profile_tab_off)
    }
}

private data class MarketplaceStateChangeRequest(
    val itemId: String,
    val targetState: MarketplaceItemState,
    val actionText: String
)
