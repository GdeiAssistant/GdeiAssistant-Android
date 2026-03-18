package cn.gdeiassistant.ui.schedule

import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color.parseColor
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Image as ImageIcon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import cn.gdeiassistant.model.Schedule
import cn.gdeiassistant.ui.components.HeroCard
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.MetricChip
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.StatusBanner
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ──────────────────────────────────────────────────────────
// 入口
// ──────────────────────────────────────────────────────────

@Composable
fun ScheduleScreen(navController: NavHostController) {
    val viewModel: ScheduleViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val backgroundPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            viewModel.setBackgroundImageUri(uri.toString())
        }
    }

    ScheduleContent(
        state = state,
        onBack = navController::popBackStack,
        onWeekChange = viewModel::setWeek,
        onSelectBackground = { backgroundPicker.launch(arrayOf("image/*")) },
        onClearBackground = { viewModel.setBackgroundImageUri(null) }
    )
}

// ──────────────────────────────────────────────────────────
// 内容
// ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleContent(
    state: ScheduleUiState,
    onBack: () -> Unit,
    onWeekChange: (Int) -> Unit,
    onSelectBackground: () -> Unit,
    onClearBackground: () -> Unit
) {
    var selectedCourse by remember { mutableStateOf<Schedule?>(null) }
    var showWeekPicker by remember { mutableStateOf(false) }
    var showBackgroundDialog by remember { mutableStateOf(false) }
    val dayLabels = remember {
        listOf(
            R.string.schedule_weekday_monday, R.string.schedule_weekday_tuesday,
            R.string.schedule_weekday_wednesday, R.string.schedule_weekday_thursday,
            R.string.schedule_weekday_friday, R.string.schedule_weekday_saturday,
            R.string.schedule_weekday_sunday
        )
    }
    val currentWeek = remember { currentWeekNumber() }
    val currentDayIndex = remember(state.selectedWeek, currentWeek) {
        if (state.selectedWeek == currentWeek) LocalDate.now().dayOfWeek.value - 1 else null
    }
    val todayLabel = currentDayIndex?.let { stringResource(dayLabels[it]) }
    val busiestDayLabel = state.busiestDayIndex?.let { stringResource(dayLabels[it]) }
        ?: stringResource(R.string.schedule_busiest_day_none)

    LazyScreen(
        title = stringResource(R.string.schedule_title),
        onBack = onBack,
        actions = {
            IconButton(onClick = { showBackgroundDialog = true }) {
                Icon(Icons.Rounded.ImageIcon, contentDescription = stringResource(R.string.schedule_background_action))
            }
        },
        showLoadingPlaceholder = state.isLoading && state.scheduleList.isEmpty()
    ) {
        item { ScheduleHeroCard(state, todayLabel, busiestDayLabel) }
        item {
            WeekSelector(
                selectedWeek = state.selectedWeek,
                maxWeeks = state.maxWeeks,
                currentWeek = currentWeek.coerceIn(1, state.maxWeeks),
                onPrevious = { onWeekChange((state.selectedWeek - 1).coerceAtLeast(1)) },
                onNext = { onWeekChange((state.selectedWeek + 1).coerceAtMost(state.maxWeeks)) },
                onWeekSelected = onWeekChange,
                onOpenPicker = { showWeekPicker = true }
            )
        }
        if (!state.error.isNullOrBlank()) {
            item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.CalendarMonth
                )
            }
        }
        item {
            ScheduleGridCard(
                state = state,
                dayLabels = dayLabels,
                currentDayIndex = currentDayIndex,
                backgroundImageUri = state.backgroundImageUri,
                onCourseClick = { selectedCourse = it }
            )
        }
    }

    if (showWeekPicker) {
        WeekPickerDialog(
            selectedWeek = state.selectedWeek,
            maxWeeks = state.maxWeeks,
            onDismiss = { showWeekPicker = false },
            onWeekSelected = { showWeekPicker = false; onWeekChange(it) }
        )
    }
    if (showBackgroundDialog) {
        BackgroundDialog(
            hasBackground = state.hasCustomBackground,
            onDismiss = { showBackgroundDialog = false },
            onSelect = { showBackgroundDialog = false; onSelectBackground() },
            onClear = { showBackgroundDialog = false; onClearBackground() }
        )
    }
    selectedCourse?.let { course ->
        CourseDetailDialog(course = course, onDismiss = { selectedCourse = null })
    }
}

// ──────────────────────────────────────────────────────────
// 英雄卡
// ──────────────────────────────────────────────────────────

@Composable
private fun ScheduleHeroCard(state: ScheduleUiState, todayLabel: String?, busiestDayLabel: String) {
    HeroCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.schedule_week_nth, state.selectedWeek),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = todayLabel?.let { stringResource(R.string.schedule_hero_current_week, it) }
                ?: stringResource(R.string.schedule_hero_other_week, state.selectedWeek),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.84f)
        )
        Spacer(modifier = Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricSlot { MetricChip(stringResource(R.string.schedule_metric_course_count), state.totalCourses.toString(), Modifier.fillMaxWidth(), true) }
            MetricSlot { MetricChip(stringResource(R.string.schedule_metric_today), todayLabel ?: stringResource(R.string.schedule_metric_non_current), Modifier.fillMaxWidth(), true) }
            MetricSlot { MetricChip(stringResource(R.string.schedule_metric_busiest), busiestDayLabel, Modifier.fillMaxWidth(), true) }
        }
    }
}

@Composable
private fun RowScope.MetricSlot(content: @Composable RowScope.() -> Unit) {
    Row(modifier = Modifier.weight(1f), content = content)
}

// ──────────────────────────────────────────────────────────
// 周选择器
// ──────────────────────────────────────────────────────────

@Composable
private fun WeekSelector(
    selectedWeek: Int,
    maxWeeks: Int,
    currentWeek: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onWeekSelected: (Int) -> Unit,
    onOpenPicker: () -> Unit
) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevious, enabled = selectedWeek > 1) {
                    Icon(Icons.Rounded.ChevronLeft, contentDescription = stringResource(R.string.schedule_previous_week))
                }
                Row(
                    modifier = Modifier.clickable(onClick = onOpenPicker),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.schedule_week_nth, selectedWeek),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                IconButton(onClick = onNext, enabled = selectedWeek < maxWeeks) {
                    Icon(Icons.Rounded.ChevronRight, contentDescription = stringResource(R.string.schedule_next_week))
                }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items((1..maxWeeks).toList()) { week ->
                    FilterChip(
                        selected = week == selectedWeek,
                        onClick = { onWeekSelected(week) },
                        label = {
                            Text(
                                if (week == currentWeek) stringResource(R.string.schedule_week_chip_current, week)
                                else stringResource(R.string.schedule_week_chip, week)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = MaterialTheme.colorScheme.outlineVariant,
                            selectedBorderColor = MaterialTheme.colorScheme.primary,
                            enabled = true,
                            selected = week == selectedWeek
                        )
                    )
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────
// 课表网格
// ──────────────────────────────────────────────────────────

@Composable
private fun ScheduleGridCard(
    state: ScheduleUiState,
    dayLabels: List<Int>,
    currentDayIndex: Int?,
    backgroundImageUri: String?,
    onCourseClick: (Schedule) -> Unit
) {
    val sectionLabelWidth = 28.dp
    val cellHeight = 72.dp
    val backgroundImage = rememberScheduleBackgroundImage(backgroundImageUri)

    val gridLine = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val headerFill = MaterialTheme.colorScheme.surfaceContainerLow
    val rowAltFill = MaterialTheme.colorScheme.surfaceContainerLowest
    val todayHeaderFill = MaterialTheme.colorScheme.primaryContainer
    val todayColumnFill = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Text(
                text = stringResource(R.string.schedule_grid_title),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val dayColumnWidth = (maxWidth - sectionLabelWidth) / dayLabels.size
                val gridWidth = dayColumnWidth * dayLabels.size
                Column {
                    // 表头
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(sectionLabelWidth)
                                .height(36.dp)
                                .border(0.5.dp, gridLine)
                                .background(headerFill),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.schedule_section_header),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        dayLabels.forEachIndexed { index, labelRes ->
                            val isToday = index == currentDayIndex
                            Box(
                                modifier = Modifier
                                    .width(dayColumnWidth)
                                    .height(36.dp)
                                    .border(0.5.dp, gridLine)
                                    .background(if (isToday) todayHeaderFill else headerFill),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(labelRes),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Medium
                                )
                            }
                        }
                    }
                    // 网格主体
                    Row {
                        // 节次列
                        Column(modifier = Modifier.width(sectionLabelWidth)) {
                            repeat(state.totalSections) { rowIndex ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(cellHeight)
                                        .border(0.5.dp, gridLine)
                                        .background(headerFill),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = (rowIndex + 1).toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        // 课程网格
                        Box(
                            modifier = Modifier
                                .width(gridWidth)
                                .height(cellHeight * state.totalSections)
                        ) {
                            // 背景图
                            backgroundImage?.let { bitmap ->
                                Image(
                                    bitmap = bitmap,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .alpha(0.28f)
                                )
                                Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.32f)))
                            }
                            // 交替行背景 (无背景图时生效)
                            if (backgroundImage == null) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    repeat(state.totalSections) { rowIndex ->
                                        val isEven = rowIndex % 2 == 0
                                        Row(modifier = Modifier.width(gridWidth)) {
                                            repeat(dayLabels.size) {
                                                Box(
                                                    modifier = Modifier
                                                        .width(dayColumnWidth)
                                                        .height(cellHeight)
                                                        .background(if (isEven) rowAltFill else Color.Transparent)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            // 今日高亮列
                            currentDayIndex?.let { todayColumn ->
                                Box(
                                    modifier = Modifier
                                        .offset(x = dayColumnWidth * todayColumn)
                                        .width(dayColumnWidth)
                                        .fillMaxHeight()
                                        .background(todayColumnFill)
                                )
                            }
                            // 网格线
                            Column(modifier = Modifier.fillMaxSize()) {
                                repeat(state.totalSections) {
                                    Row(modifier = Modifier.width(gridWidth)) {
                                        repeat(dayLabels.size) {
                                            Box(modifier = Modifier.width(dayColumnWidth).height(cellHeight).border(0.5.dp, gridLine))
                                        }
                                    }
                                }
                            }
                            // 课程卡片
                            state.scheduleList.mapNotNull { course ->
                                val row = course.row ?: return@mapNotNull null
                                val column = course.column ?: return@mapNotNull null
                                if (row !in 0 until state.totalSections || column !in dayLabels.indices) return@mapNotNull null
                                val length = (course.scheduleLength ?: 1).coerceAtLeast(1).coerceAtMost(state.totalSections - row)
                                if (length <= 0) return@mapNotNull null
                                SchedulePlacement(course, row, column, length)
                            }.forEach { placement ->
                                ElevatedCard(
                                    modifier = Modifier
                                        .offset(x = dayColumnWidth * placement.column, y = cellHeight * placement.row)
                                        .width(dayColumnWidth)
                                        .height(cellHeight * placement.length)
                                        .padding(2.dp)
                                        .clickable { onCourseClick(placement.course) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = courseColor(placement.course.colorCode)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 4.dp, vertical = 5.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = placement.course.scheduleName ?: stringResource(R.string.schedule_course_unnamed),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = if (placement.length >= 2) 4 else 3,
                                            overflow = TextOverflow.Ellipsis,
                                            lineHeight = MaterialTheme.typography.labelSmall.fontSize * 1.3f
                                        )
                                        val location = placement.course.scheduleLocation
                                        if (placement.length >= 2 && !location.isNullOrBlank()) {
                                            Text(
                                                text = location,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White.copy(alpha = 0.82f),
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                lineHeight = MaterialTheme.typography.labelSmall.fontSize * 1.3f
                                            )
                                        }
                                    }
                                }
                            }
                            // 空状态
                            if (state.scheduleList.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = stringResource(R.string.schedule_empty_grid),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

private data class SchedulePlacement(val course: Schedule, val row: Int, val column: Int, val length: Int)

// ──────────────────────────────────────────────────────────
// 对话框
// ──────────────────────────────────────────────────────────

@Composable
private fun WeekPickerDialog(
    selectedWeek: Int,
    maxWeeks: Int,
    onDismiss: () -> Unit,
    onWeekSelected: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.schedule_select_week_title)) },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                items((1..maxWeeks).toList()) { week ->
                    Text(
                        text = stringResource(R.string.schedule_week_nth, week),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onWeekSelected(week) }
                            .padding(vertical = 12.dp),
                        color = if (week == selectedWeek) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (week == selectedWeek) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.back)) } }
    )
}

@Composable
private fun BackgroundDialog(
    hasBackground: Boolean,
    onDismiss: () -> Unit,
    onSelect: () -> Unit,
    onClear: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.schedule_background_dialog_title)) },
        text = { Text(stringResource(R.string.schedule_background_dialog_message)) },
        confirmButton = {
            TextButton(onClick = onSelect) { Text(stringResource(R.string.schedule_background_choose)) }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (hasBackground) {
                    TextButton(onClick = onClear) { Text(stringResource(R.string.schedule_background_clear)) }
                }
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.back)) }
            }
        }
    )
}

@Composable
private fun CourseDetailDialog(course: Schedule, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(course.scheduleName ?: stringResource(R.string.schedule_course_unnamed)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailRow(stringResource(R.string.schedule_detail_type), course.scheduleType ?: "—")
                DetailRow(stringResource(R.string.schedule_detail_lesson), course.scheduleLesson ?: "—")
                DetailRow(stringResource(R.string.schedule_detail_teacher), course.scheduleTeacher ?: stringResource(R.string.schedule_teacher_pending))
                DetailRow(stringResource(R.string.schedule_detail_location), course.scheduleLocation ?: stringResource(R.string.schedule_location_pending))
                DetailRow(
                    stringResource(R.string.schedule_detail_weeks),
                    course.scheduleWeek ?: stringResource(R.string.schedule_week_range, course.minScheduleWeek ?: 1, course.maxScheduleWeek ?: 1)
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.schedule_dialog_close)) } }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(12.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

// ──────────────────────────────────────────────────────────
// 工具函数
// ──────────────────────────────────────────────────────────

private fun courseColor(raw: String?): Color {
    return runCatching { Color(parseColor(raw ?: "#2563EB")) }
        .getOrElse { Color(0xFF315A87) }
        .copy(alpha = 0.88f)
}

@Composable
private fun rememberScheduleBackgroundImage(uriString: String?): ImageBitmap? {
    val context = LocalContext.current
    val uri = remember(uriString) { uriString?.let(Uri::parse) }
    val image by produceState<ImageBitmap?>(initialValue = null, uri, context) {
        value = if (uri == null) null
        else withContext(Dispatchers.IO) {
            decodeSampledBitmap(context.contentResolver, uri, 1080, 1920)?.asImageBitmap()
        }
    }
    return image
}

private fun decodeSampledBitmap(resolver: ContentResolver, uri: Uri, reqW: Int, reqH: Int): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) } ?: return null
    val opts = BitmapFactory.Options().apply {
        inSampleSize = calcSampleSize(bounds, reqW, reqH)
        inPreferredConfig = Bitmap.Config.ARGB_8888
    }
    return resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
}

private fun calcSampleSize(options: BitmapFactory.Options, reqW: Int, reqH: Int): Int {
    val (h, w) = options.outHeight to options.outWidth
    var s = 1
    if (h > reqH || w > reqW) {
        var hh = h / 2; var ww = w / 2
        while (hh / s >= reqH && ww / s >= reqW) s *= 2
    }
    return s.coerceAtLeast(1)
}
