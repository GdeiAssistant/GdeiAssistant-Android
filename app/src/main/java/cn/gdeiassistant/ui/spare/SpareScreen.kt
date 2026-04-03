package cn.gdeiassistant.ui.spare

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Workspaces
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import cn.gdeiassistant.model.AppLocaleSupport
import cn.gdeiassistant.model.SpareOption
import cn.gdeiassistant.model.SpareRoomItem
import cn.gdeiassistant.model.spareClassNumberOptions
import cn.gdeiassistant.model.spareTypeOptions
import cn.gdeiassistant.model.spareWeekTypeOptions
import cn.gdeiassistant.model.spareWeekdayOptions
import cn.gdeiassistant.model.spareZoneOptions
import androidx.compose.foundation.text.KeyboardOptions
import cn.gdeiassistant.ui.components.BadgePill
import cn.gdeiassistant.ui.components.EmptyState
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.StatusBanner
import cn.gdeiassistant.ui.components.TintButton
import cn.gdeiassistant.ui.theme.AppShapes

private enum class SpareResultState {
    Loading,
    Empty,
    Ready
}

@Composable
fun SpareScreen(navController: NavHostController) {
    val viewModel: SpareViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val query = state.query
    val locale = AppLocaleSupport.normalizeLocale(
        LocalConfiguration.current.locales.get(0)?.toLanguageTag()
    )
    val zoneOptions = remember(locale) { spareZoneOptions(locale) }
    val weekdayOptions = remember(locale) { spareWeekdayOptions(locale) }
    val typeOptions = remember(locale) { spareTypeOptions(locale) }
    val weekTypeOptions = remember(locale) { spareWeekTypeOptions(locale) }
    val classNumberOptions = remember(locale) { spareClassNumberOptions(locale) }

    LazyScreen(
        title = stringResource(R.string.spare_title),
        onBack = navController::popBackStack
    ) {
        item {
            SectionCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.06f)
            ) {
                BadgePill(
                    text = stringResource(R.string.spare_hero_badge),
                    tint = MaterialTheme.colorScheme.primary
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = stringResource(R.string.spare_condition_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = stringResource(R.string.spare_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SpareDropdownField(
                        title = stringResource(R.string.spare_zone_label),
                        value = zoneOptions.labelFor(query.zone),
                        options = zoneOptions,
                        onSelect = viewModel::updateZone,
                        modifier = Modifier.weight(1f)
                    )
                    SpareDropdownField(
                        title = stringResource(R.string.spare_weekday_label),
                        value = weekdayOptions.labelFor(state.selectedWeekday),
                        options = weekdayOptions,
                        onSelect = viewModel::updateWeekday,
                        modifier = Modifier.weight(1f)
                    )
                }

                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(14.dp))

                SpareDropdownField(
                    title = stringResource(R.string.spare_type_label),
                    value = typeOptions.labelFor(query.type),
                    options = typeOptions,
                    onSelect = viewModel::updateType
                )

                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = query.minSeating?.toString().orEmpty(),
                        onValueChange = viewModel::updateMinSeating,
                        modifier = Modifier.weight(1f),
                        shape = AppShapes.button,
                        label = { Text(text = stringResource(R.string.spare_min_seating_label)) },
                        placeholder = { Text(text = stringResource(R.string.spare_optional_hint)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = query.maxSeating?.toString().orEmpty(),
                        onValueChange = viewModel::updateMaxSeating,
                        modifier = Modifier.weight(1f),
                        shape = AppShapes.button,
                        label = { Text(text = stringResource(R.string.spare_max_seating_label)) },
                        placeholder = { Text(text = stringResource(R.string.spare_optional_hint)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SpareDropdownField(
                        title = stringResource(R.string.spare_week_type_label),
                        value = weekTypeOptions.labelFor(query.weekType),
                        options = weekTypeOptions,
                        onSelect = viewModel::updateWeekType,
                        modifier = Modifier.weight(1f)
                    )
                    SpareDropdownField(
                        title = stringResource(R.string.spare_class_number_label),
                        value = classNumberOptions.labelFor(query.classNumber),
                        options = classNumberOptions,
                        onSelect = viewModel::updateClassNumber,
                        modifier = Modifier.weight(1f)
                    )
                }

                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SpareMetricCard(
                        label = stringResource(R.string.spare_metric_zone),
                        value = zoneOptions.labelFor(query.zone),
                        modifier = Modifier.weight(1f)
                    )
                    SpareMetricCard(
                        label = stringResource(R.string.spare_metric_time),
                        value = classNumberOptions.labelFor(query.classNumber),
                        modifier = Modifier.weight(1f)
                    )
                    SpareMetricCard(
                        label = stringResource(R.string.spare_metric_results),
                        value = state.items.size.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }

                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(18.dp))

                TintButton(
                    text = stringResource(R.string.spare_query_action),
                    onClick = viewModel::submitQuery,
                    enabled = !state.isLoading,
                    icon = Icons.Rounded.Search,
                    modifier = Modifier.fillMaxWidth()
                )
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

        item {
            AnimatedContent(
                targetState = when {
                    state.isLoading -> SpareResultState.Loading
                    state.items.isEmpty() -> SpareResultState.Empty
                    else -> SpareResultState.Ready
                },
                label = "spareResultState"
            ) { resultState ->
                when (resultState) {
                    SpareResultState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    SpareResultState.Empty -> {
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

                    SpareResultState.Ready -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            SpareResultHeader(
                                zoneLabel = zoneOptions.labelFor(query.zone),
                                typeLabel = typeOptions.labelFor(query.type),
                                count = state.items.size
                            )
                            state.items.forEach { item ->
                                SpareRoomCard(item = item)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpareMetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SpareResultHeader(
    zoneLabel: String,
    typeLabel: String,
    count: Int
) {
    SectionCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.spare_result_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = stringResource(R.string.spare_result_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            BadgePill(
                text = stringResource(R.string.spare_result_count_badge, count),
                tint = MaterialTheme.colorScheme.tertiary
            )
        }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SpareMetricCard(
                label = stringResource(R.string.spare_metric_zone),
                value = zoneLabel,
                modifier = Modifier.weight(1f)
            )
            SpareMetricCard(
                label = stringResource(R.string.spare_type_label),
                value = typeLabel,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SpareDropdownField(
    title: String,
    value: String,
    options: List<SpareOption>,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Box {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true },
                shape = AppShapes.button,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = option.label) },
                        onClick = {
                            onSelect(option.value)
                            expanded = false
                        }
                    )
                }
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
            fontWeight = FontWeight.ExtraBold
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = "${item.zoneName} · ${item.roomType}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SpareMetricCard(
                label = stringResource(R.string.spare_room_section_label),
                value = item.sectionText,
                modifier = Modifier.weight(1f)
            )
            SpareMetricCard(
                label = stringResource(R.string.spare_room_seating_label),
                value = item.classSeating,
                modifier = Modifier.weight(1f)
            )
        }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f))
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.roomNumber,
                style = MaterialTheme.typography.labelLarge,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.spare_room_exam_seating, item.examSeating),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun List<SpareOption>.labelFor(value: Int): String {
    return firstOrNull { it.value == value }?.label ?: firstOrNull()?.label.orEmpty()
}
