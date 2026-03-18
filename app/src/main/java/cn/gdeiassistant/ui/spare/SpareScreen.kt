package cn.gdeiassistant.ui.spare

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Workspaces
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import cn.gdeiassistant.model.SpareRoomItem
import cn.gdeiassistant.model.spareTypeTitles
import cn.gdeiassistant.model.spareWeekTypeTitles
import cn.gdeiassistant.model.spareZoneTitles
import cn.gdeiassistant.ui.components.EmptyState
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.MetricChip
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.StatusBanner

@Composable
fun SpareScreen(navController: NavHostController) {
    val viewModel: SpareViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val query = state.query

    LazyScreen(
        title = stringResource(R.string.spare_title),
        onBack = navController::popBackStack
    ) {
        item {
            SectionCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.spare_condition_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(14.dp))
                OptionRow(
                    title = stringResource(R.string.spare_zone_label),
                    options = spareZoneTitles,
                    selectedIndex = query.zone,
                    onSelect = viewModel::updateZone
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(14.dp))
                OptionRow(
                    title = stringResource(R.string.spare_type_label),
                    options = spareTypeTitles,
                    selectedIndex = query.type,
                    onSelect = viewModel::updateType
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CounterCard(
                        title = stringResource(R.string.spare_min_week_label),
                        value = stringResource(R.string.spare_week_value, query.minWeek),
                        onDecrease = { viewModel.adjustMinWeek(-1) },
                        onIncrease = { viewModel.adjustMinWeek(1) },
                        modifier = Modifier.weight(1f)
                    )
                    CounterCard(
                        title = stringResource(R.string.spare_max_week_label),
                        value = stringResource(R.string.spare_week_value, query.maxWeek),
                        onDecrease = { viewModel.adjustMaxWeek(-1) },
                        onIncrease = { viewModel.adjustMaxWeek(1) },
                        modifier = Modifier.weight(1f)
                    )
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(14.dp))
                OptionRow(
                    title = stringResource(R.string.spare_week_type_label),
                    options = spareWeekTypeTitles,
                    selectedIndex = query.weekType,
                    onSelect = viewModel::updateWeekType
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CounterCard(
                        title = stringResource(R.string.spare_start_time_label),
                        value = query.startTime.toString(),
                        onDecrease = { viewModel.adjustStartTime(-1) },
                        onIncrease = { viewModel.adjustStartTime(1) },
                        modifier = Modifier.weight(1f)
                    )
                    CounterCard(
                        title = stringResource(R.string.spare_end_time_label),
                        value = query.endTime.toString(),
                        onDecrease = { viewModel.adjustEndTime(-1) },
                        onIncrease = { viewModel.adjustEndTime(1) },
                        modifier = Modifier.weight(1f)
                    )
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
                CounterCard(
                    title = stringResource(R.string.spare_class_number_label),
                    value = stringResource(R.string.spare_class_number_value, query.classNumber),
                    onDecrease = { viewModel.adjustClassNumber(-1) },
                    onIncrease = { viewModel.adjustClassNumber(1) },
                    modifier = Modifier.fillMaxWidth()
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(16.dp))
                Button(
                    onClick = viewModel::submitQuery,
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.Search, contentDescription = null)
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
                    Text(text = stringResource(R.string.spare_query_action))
                }
            }
        }
        if (!state.error.isNullOrBlank()) {
            item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.Workspaces,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
        when {
            state.isLoading -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            state.items.isEmpty() -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    EmptyState(
                        icon = Icons.Rounded.Workspaces,
                        message = stringResource(R.string.spare_empty),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            else -> {
                items(state.items, key = { it.id }) { item ->
                    SpareRoomCard(item = item)
                }
            }
        }
    }
}

@Composable
private fun OptionRow(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    val scrollState = rememberScrollState()
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold
    )
    androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
    Row(
        modifier = Modifier.horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        options.forEachIndexed { index, label ->
            FilterChip(
                selected = selectedIndex == index,
                onClick = { onSelect(index) },
                label = { Text(text = label) }
            )
        }
    }
}

@Composable
private fun CounterCard(
    title: String,
    value: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    SectionCard(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDecrease) {
                Icon(Icons.Rounded.Remove, contentDescription = null)
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onIncrease) {
                Icon(Icons.Rounded.Add, contentDescription = null)
            }
        }
    }
}

@Composable
private fun SpareRoomCard(item: SpareRoomItem) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = item.roomName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = "${item.zoneName} · ${item.roomType}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricChip(
                label = stringResource(R.string.spare_room_section_label),
                value = item.sectionText,
                modifier = Modifier.weight(1f)
            )
            MetricChip(
                label = stringResource(R.string.spare_room_seating_label),
                value = item.classSeating,
                modifier = Modifier.weight(1f)
            )
        }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(10.dp))
        Text(
            text = stringResource(R.string.spare_room_exam_seating, item.examSeating),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
