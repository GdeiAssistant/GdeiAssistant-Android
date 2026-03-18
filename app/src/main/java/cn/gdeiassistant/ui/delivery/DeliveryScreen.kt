package cn.gdeiassistant.ui.delivery

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.NoteAdd
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import cn.gdeiassistant.model.DeliveryOrder
import cn.gdeiassistant.model.DeliveryOrderDetail
import cn.gdeiassistant.model.DeliveryOrderState
import cn.gdeiassistant.ui.components.EmptyState
import cn.gdeiassistant.ui.components.ActionTile
import cn.gdeiassistant.ui.components.BadgePill
import cn.gdeiassistant.ui.components.GhostButton
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.MetricChip
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.StatusBanner
import cn.gdeiassistant.ui.components.ShimmerBox
import cn.gdeiassistant.ui.components.TintButton
import cn.gdeiassistant.ui.navigation.Routes
import kotlinx.coroutines.flow.collectLatest

@Composable
fun DeliveryScreen(navController: NavHostController) {
    val viewModel: DeliveryViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyScreen(
        title = stringResource(R.string.delivery_title),
        onBack = navController::popBackStack,
        actions = {
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.delivery_refresh))
            }
        }
    ) {
        item {
            androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionTile(
                    title = stringResource(R.string.delivery_action_mine_title),
                    subtitle = stringResource(R.string.delivery_action_mine_subtitle),
                    icon = Icons.Rounded.LocalShipping,
                    onClick = { navController.navigate(Routes.DELIVERY_MINE) },
                    tint = MaterialTheme.colorScheme.primary,
                    emphasized = true,
                    modifier = Modifier.weight(1f)
                )
                ActionTile(
                    title = stringResource(R.string.delivery_action_publish_title),
                    subtitle = stringResource(R.string.delivery_action_publish_subtitle),
                    icon = Icons.AutoMirrored.Rounded.NoteAdd,
                    onClick = { navController.navigate(Routes.DELIVERY_PUBLISH) },
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DeliveryFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = state.selectedFilter == filter,
                        onClick = { viewModel.selectFilter(filter) },
                        label = { Text(text = deliveryFilterLabel(filter)) }
                    )
                }
            }
        }
        if (!state.error.isNullOrBlank()) {
            item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.LocalShipping
                )
            }
        }
        when {
            state.isLoading && state.orders.isEmpty() -> item { DeliveryLoadingPane() }
            state.visibleOrders.isEmpty() -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    EmptyState(
                        icon = Icons.Rounded.LocalShipping,
                        message = stringResource(R.string.delivery_empty),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            else -> {
                items(state.visibleOrders, key = { it.orderId }) { order ->
                    DeliveryOrderCard(
                        order = order,
                        onClick = { navController.navigate(Routes.deliveryDetail(order.orderId)) }
                    )
                }
            }
        }
    }
}

@Composable
fun DeliveryDetailScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: DeliveryDetailViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showCompleteConfirmation by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is DeliveryEvent.ShowMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
                DeliveryEvent.Published -> Unit
            }
        }
    }

    LazyScreen(
        title = stringResource(R.string.delivery_detail_title),
        onBack = navController::popBackStack,
        actions = {
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.delivery_refresh))
            }
        }
    ) {
        when {
            state.isLoading -> item { DeliveryLoadingPane() }
            !state.error.isNullOrBlank() && state.detail == null -> item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.LocalShipping
                )
            }
            state.detail == null -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    EmptyState(
                        icon = Icons.Rounded.LocalShipping,
                        message = stringResource(R.string.delivery_detail_missing),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            else -> {
                val detail = requireNotNull(state.detail)
                item { DeliveryDetailHero(detail = detail) }
                item {
                    SectionCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.delivery_section_order),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(14.dp))
                        DetailRow(
                            label = stringResource(R.string.delivery_info_role),
                            value = deliveryRoleTitle(detail.detailType)
                        )
                        DetailRow(
                            label = stringResource(R.string.delivery_info_publisher),
                            value = detail.order.username
                        )
                        DetailRow(
                            label = stringResource(R.string.delivery_info_pickup_place),
                            value = detail.order.company
                        )
                        DetailRow(
                            label = stringResource(R.string.delivery_info_address),
                            value = detail.order.address
                        )
                        DetailRow(
                            label = stringResource(R.string.delivery_info_phone),
                            value = if (detail.canViewSensitiveInfo) detail.order.contactPhone else maskSensitive(detail.order.contactPhone)
                        )
                        DetailRow(
                            label = stringResource(R.string.delivery_info_pickup_code),
                            value = if (detail.canViewSensitiveInfo) detail.order.pickupCode else maskSensitive(detail.order.pickupCode)
                        )
                        DetailRow(
                            label = stringResource(R.string.delivery_info_reward),
                            value = "¥%.2f".format(detail.order.price)
                        )
                        DetailRow(
                            label = stringResource(R.string.delivery_info_time),
                            value = detail.order.orderTime
                        )
                        if (detail.order.remarks.isNotBlank()) {
                            DetailRow(
                                label = stringResource(R.string.delivery_info_remarks),
                                value = detail.order.remarks
                            )
                        }
                    }
                }
                detail.trade?.let { trade ->
                    item {
                        SectionCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = stringResource(R.string.delivery_section_trade),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(14.dp))
                            DetailRow(
                                label = stringResource(R.string.delivery_info_runner),
                                value = trade.username
                            )
                            DetailRow(
                                label = stringResource(R.string.delivery_info_accept_time),
                                value = trade.createTime
                            )
                        }
                    }
                }
                item {
                    SectionCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.delivery_section_action),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(14.dp))
                        if (!detail.canViewSensitiveInfo) {
                            Text(
                                text = stringResource(R.string.delivery_sensitive_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
                        }
                        if (detail.canAccept) {
                            TintButton(
                                text = stringResource(R.string.delivery_accept_action),
                                onClick = viewModel::acceptOrder,
                                enabled = !state.isSubmitting,
                                modifier = Modifier.fillMaxWidth(),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                        if (detail.canComplete && detail.trade != null) {
                            if (detail.canAccept) {
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
                            }
                            GhostButton(
                                text = stringResource(R.string.delivery_complete_action),
                                onClick = { showCompleteConfirmation = true },
                                enabled = !state.isSubmitting,
                                modifier = Modifier.fillMaxWidth(),
                                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(10.dp))
                            Text(
                                text = stringResource(R.string.delivery_complete_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCompleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showCompleteConfirmation = false },
            title = { Text(text = stringResource(R.string.delivery_complete_action)) },
            text = { Text(text = stringResource(R.string.delivery_complete_hint)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCompleteConfirmation = false
                        state.detail?.trade?.let { trade ->
                            viewModel.finishTrade(trade.tradeId)
                        }
                    }
                ) {
                    Text(text = stringResource(R.string.delivery_complete_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompleteConfirmation = false }) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun DeliveryMineScreen(navController: NavHostController) {
    val viewModel: DeliveryMineViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyScreen(
        title = stringResource(R.string.delivery_mine_title),
        onBack = navController::popBackStack,
        actions = {
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.delivery_refresh))
            }
        }
    ) {
        item {
            SectionCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            ) {
                BadgePill(text = stringResource(R.string.delivery_mine_badge))
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(16.dp))
                Text(
                    text = stringResource(R.string.delivery_mine_subtitle),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
                androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricChip(
                        label = stringResource(R.string.delivery_mine_tab_published),
                        value = state.summary.published.size.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    MetricChip(
                        label = stringResource(R.string.delivery_mine_tab_accepted),
                        value = state.summary.accepted.size.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        item {
            androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DeliveryMineTab.entries.forEach { tab ->
                    FilterChip(
                        selected = state.selectedTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        label = { Text(text = deliveryMineTabLabel(tab)) }
                    )
                }
            }
        }
        item {
            androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DeliveryFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = state.selectedFilter == filter,
                        onClick = { viewModel.selectFilter(filter) },
                        label = { Text(text = deliveryFilterLabel(filter)) }
                    )
                }
            }
        }
        if (!state.error.isNullOrBlank()) {
            item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.LocalShipping
                )
            }
        }
        when {
            state.isLoading && state.summary.published.isEmpty() && state.summary.accepted.isEmpty() -> item { DeliveryLoadingPane() }
            state.visibleOrders.isEmpty() -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {
                    EmptyState(
                        icon = Icons.Rounded.LocalShipping,
                        message = stringResource(R.string.delivery_mine_empty),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            else -> {
                items(state.visibleOrders, key = { it.orderId }) { order ->
                    DeliveryOrderCard(
                        order = order,
                        onClick = { navController.navigate(Routes.deliveryDetail(order.orderId)) }
                    )
                }
            }
        }
    }
}

@Composable
fun DeliveryPublishScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: DeliveryPublishViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var pickupPlace by rememberSaveable { mutableStateOf("") }
    var pickupNumber by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var rewardText by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var remarks by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is DeliveryEvent.ShowMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
                DeliveryEvent.Published -> {
                    navController.popBackStack()
                }
            }
        }
    }

    LazyScreen(
        title = stringResource(R.string.delivery_publish_title),
        onBack = navController::popBackStack
    ) {
        item {
            SectionCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            ) {
                BadgePill(text = stringResource(R.string.delivery_publish_badge))
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(16.dp))
                Text(
                    text = stringResource(R.string.delivery_publish_subtitle),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        item {
            SectionCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = pickupPlace,
                    onValueChange = { pickupPlace = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = stringResource(R.string.delivery_publish_pickup_place)) }
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
                OutlinedTextField(
                    value = pickupNumber,
                    onValueChange = { pickupNumber = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = stringResource(R.string.delivery_publish_pickup_code)) }
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = stringResource(R.string.delivery_publish_phone)) }
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
                OutlinedTextField(
                    value = rewardText,
                    onValueChange = { rewardText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = stringResource(R.string.delivery_publish_reward)) }
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = stringResource(R.string.delivery_publish_address)) }
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    label = { Text(text = stringResource(R.string.delivery_publish_remarks)) }
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(16.dp))
                TintButton(
                    text = if (state.isSubmitting) {
                        stringResource(R.string.delivery_publish_submitting)
                    } else {
                        stringResource(R.string.delivery_publish_submit)
                    },
                    onClick = {
                        viewModel.publish(
                            pickupPlace = pickupPlace,
                            pickupNumber = pickupNumber,
                            phone = phone,
                            rewardText = rewardText,
                            address = address,
                            remarks = remarks
                        )
                    },
                    enabled = !state.isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun DeliveryDetailHero(detail: DeliveryOrderDetail) {
    SectionCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    ) {
        BadgePill(text = deliveryStateLabel(detail.order.state))
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = detail.order.taskName,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = deliveryStatusDescription(detail),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = detail.order.orderTime,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
        androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricChip(
                label = stringResource(R.string.delivery_info_reward),
                value = "¥%.2f".format(detail.order.price),
                modifier = Modifier.weight(1f)
            )
            MetricChip(
                label = stringResource(R.string.delivery_info_pickup_place),
                value = detail.order.company,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DeliveryOrderCard(
    order: DeliveryOrder,
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
            BadgePill(text = order.taskName)
            Text(
                text = "¥%.2f".format(order.price),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold
            )
        }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(10.dp))
        DeliveryRouteRow(
            marker = stringResource(R.string.delivery_route_pickup_marker),
            value = "${order.company} ${stringResource(R.string.delivery_route_pickup_suffix)}",
            tint = MaterialTheme.colorScheme.primary
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
        DeliveryRouteRow(
            marker = stringResource(R.string.delivery_route_delivery_marker),
            value = order.address,
            tint = MaterialTheme.colorScheme.tertiary
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
        androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BadgePill(text = deliveryStateLabel(order.state))
            BadgePill(text = order.orderTime, tint = MaterialTheme.colorScheme.tertiary)
        }
    }
}

@Composable
private fun DeliveryRouteRow(
    marker: String,
    value: String,
    tint: androidx.compose.ui.graphics.Color
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        androidx.compose.material3.Surface(
            modifier = Modifier.offset(y = (-1).dp),
            shape = MaterialTheme.shapes.small,
            color = tint.copy(alpha = 0.14f)
        ) {
            Text(
                text = marker,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = tint
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun deliveryStatusDescription(detail: DeliveryOrderDetail): String {
    return when (detail.order.state) {
        DeliveryOrderState.PENDING -> when (detail.detailType) {
            0 -> stringResource(R.string.delivery_status_pending_publisher)
            else -> stringResource(R.string.delivery_status_pending_visitor)
        }

        DeliveryOrderState.DELIVERING -> when (detail.detailType) {
            0 -> stringResource(R.string.delivery_status_delivering_publisher)
            3 -> stringResource(R.string.delivery_status_delivering_runner)
            else -> stringResource(R.string.delivery_status_delivering_desc)
        }

        DeliveryOrderState.COMPLETED -> stringResource(R.string.delivery_status_completed_any)
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(190.dp)
        )
    }
    androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(10.dp))
}

private fun maskSensitive(value: String): String {
    if (value.length <= 4) {
        return "*".repeat(value.length.coerceAtLeast(3))
    }
    return "*".repeat((value.length - 4).coerceAtLeast(3)) + value.takeLast(4)
}

@Composable
private fun deliveryFilterLabel(filter: DeliveryFilter): String {
    return when (filter) {
        DeliveryFilter.ALL -> stringResource(R.string.delivery_filter_all)
        DeliveryFilter.PENDING -> stringResource(R.string.delivery_filter_pending)
        DeliveryFilter.DELIVERING -> stringResource(R.string.delivery_filter_delivering)
        DeliveryFilter.COMPLETED -> stringResource(R.string.delivery_filter_completed)
    }
}

@Composable
private fun deliveryStateLabel(state: DeliveryOrderState): String {
    return when (state) {
        DeliveryOrderState.PENDING -> stringResource(R.string.delivery_state_pending)
        DeliveryOrderState.DELIVERING -> stringResource(R.string.delivery_state_delivering)
        DeliveryOrderState.COMPLETED -> stringResource(R.string.delivery_state_completed)
    }
}

@Composable
private fun deliveryMineTabLabel(tab: DeliveryMineTab): String {
    return when (tab) {
        DeliveryMineTab.PUBLISHED -> stringResource(R.string.delivery_mine_tab_published)
        DeliveryMineTab.ACCEPTED -> stringResource(R.string.delivery_mine_tab_accepted)
    }
}

@Composable
private fun deliveryRoleTitle(detailType: Int): String {
    return when (detailType) {
        0 -> stringResource(R.string.delivery_role_publisher)
        3 -> stringResource(R.string.delivery_role_runner)
        else -> stringResource(R.string.delivery_role_visitor)
    }
}

@Composable
private fun DeliveryLoadingPane() {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        ShimmerBox(modifier = Modifier.fillMaxWidth(0.4f), height = 28.dp)
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth(), height = 18.dp)
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(10.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth(0.88f), height = 18.dp)
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(10.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth(0.6f), height = 18.dp)
    }
}
