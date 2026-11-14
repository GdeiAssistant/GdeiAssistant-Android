package cn.gdeiassistant.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import cn.gdeiassistant.R
import cn.gdeiassistant.data.CampusZone
import cn.gdeiassistant.data.actualLessonLabel
import cn.gdeiassistant.model.AnnouncementItem
import cn.gdeiassistant.model.Schedule
import cn.gdeiassistant.ui.components.ListRow
import cn.gdeiassistant.ui.components.SectionHeader
import cn.gdeiassistant.ui.components.ShimmerScreen
import cn.gdeiassistant.ui.components.hairline
import cn.gdeiassistant.ui.home.HomeFeatureIcons
import cn.gdeiassistant.ui.home.HomeViewModel
import cn.gdeiassistant.ui.navigation.AppFeature
import cn.gdeiassistant.ui.navigation.AppFeatureCatalog
import cn.gdeiassistant.ui.navigation.AppFeatureGroup
import cn.gdeiassistant.ui.navigation.Routes
import cn.gdeiassistant.ui.theme.AppShapes
import cn.gdeiassistant.ui.theme.AppSpacing
import cn.gdeiassistant.ui.theme.GeneratedDesignTokens
import cn.gdeiassistant.ui.theme.withTabularNumbers
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(navController: NavController) {
    val viewModel: HomeViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    val greeting = when (LocalTime.now(CampusZone).hour) {
        in 5..11 -> stringResource(R.string.home_greeting_morning)
        in 12..13 -> stringResource(R.string.home_greeting_noon)
        in 14..17 -> stringResource(R.string.home_greeting_afternoon)
        in 18..23 -> stringResource(R.string.home_greeting_evening)
        else -> stringResource(R.string.home_greeting_night)
    }
    val studentSuffix = stringResource(R.string.home_student_suffix)
    val nameText = state.displayName
        ?.ifBlank { null }
        ?.removeSuffix(studentSuffix)
        ?.ifBlank { null }
    val greetingText = if (nameText != null) {
        stringResource(R.string.home_greeting_with_name, greeting, nameText)
    } else {
        stringResource(R.string.home_greeting_without_name, greeting)
    }
    val datePattern = stringResource(R.string.home_date_format)
    val todayLabel = LocalDate.now(CampusZone)
        .format(DateTimeFormatter.ofPattern(datePattern))

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top)
    ) { innerPadding ->
        if (state.isLoading) {
            ShimmerScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(
                    start = AppSpacing.lg,
                    end = AppSpacing.lg,
                    top = AppSpacing.lg,
                    bottom = AppSpacing.xxxl
                ),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                item {
                    GreetingMasthead(
                        dateLabel = todayLabel,
                        greetingText = greetingText,
                        courseCount = state.todayCourses.size.takeIf { state.scheduleError == null }
                    )
                }
                item {
                    TodayScheduleCard(
                        courses = state.todayCourses,
                        error = state.scheduleError,
                        onOpenSchedule = { navController.navigate(Routes.SCHEDULE) }
                    )
                }
                val cardBalance = state.cardInfo?.cardBalance?.takeIf { it.isNotBlank() }
                val todayCount = state.todayCourses.size.takeIf { state.scheduleError == null }
                val noticeCount = state.notices.size.takeIf { state.notices.isNotEmpty() }
                if (cardBalance != null || todayCount != null || noticeCount != null) {
                    item {
                        QuickStatusStrip(
                            cardBalance = cardBalance,
                            courseCount = todayCount,
                            noticeCount = noticeCount,
                            onOpenCard = { navController.navigate(Routes.CARD) },
                            onOpenSchedule = { navController.navigate(Routes.SCHEDULE) },
                            onOpenNotices = { navController.navigate(Routes.NOTICE_LIST) }
                        )
                    }
                }
                if (state.notices.isNotEmpty() || !state.noticeError.isNullOrBlank()) {
                    item {
                        NoticeSection(
                            notices = state.notices,
                            noticeError = state.noticeError,
                            onOpenAll = { navController.navigate(Routes.NOTICE_LIST) },
                            onOpenNotice = { notice -> navController.navigate(Routes.noticeDetail(notice.id)) }
                        )
                    }
                }
                AppFeatureGroup.entries.forEach { group ->
                    val features = AppFeatureCatalog.featuresFor(group)
                    if (features.isNotEmpty()) {
                        item {
                            FeatureGroupSection(
                                group = group,
                                features = features,
                                onNavigate = { navController.navigate(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GreetingMasthead(
    dateLabel: String,
    greetingText: String,
    courseCount: Int?,
    modifier: Modifier = Modifier
) {
    val courseSummary = when {
        courseCount == null -> stringResource(R.string.service_unavailable)
        courseCount > 0 -> stringResource(R.string.home_stats_course_count, courseCount)
        else -> stringResource(R.string.home_stats_no_course)
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.labelMedium.withTabularNumbers(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = greetingText,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = courseSummary,
            style = MaterialTheme.typography.bodyMedium.withTabularNumbers(),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TodayScheduleCard(
    courses: List<Schedule>,
    error: String?,
    onOpenSchedule: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = hairline()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.home_today_schedule_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = when {
                            error != null -> stringResource(R.string.service_unavailable)
                            courses.isEmpty() -> stringResource(R.string.home_today_schedule_empty_summary)
                            else -> stringResource(R.string.home_today_schedule_count, courses.size)
                        },
                        style = MaterialTheme.typography.bodySmall.withTabularNumbers(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = onOpenSchedule) {
                    Text(
                        text = stringResource(R.string.home_view_schedule),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
            HorizontalDivider(thickness = 0.75.dp, color = MaterialTheme.colorScheme.outlineVariant)
            AnimatedContent(
                targetState = error to courses,
                label = "schedule_grid",
                transitionSpec = { fadeIn() togetherWith fadeOut() }
            ) { (err, items) ->
                when {
                    err != null -> {
                        Text(
                            text = err.ifBlank { stringResource(R.string.service_unavailable) },
                            modifier = Modifier.padding(AppSpacing.lg),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    items.isEmpty() -> {
                        ListRow(
                            title = stringResource(R.string.home_today_schedule_empty_title),
                            supporting = stringResource(R.string.home_today_schedule_empty_body),
                            icon = Icons.Rounded.EventAvailable
                        )
                    }
                    else -> Column {
                        items.forEachIndexed { index, course ->
                            if (index > 0) {
                                HorizontalDivider(
                                    thickness = 0.75.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                            CourseRow(course = course, isLead = index == 0)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseRow(course: Schedule, isLead: Boolean) {
    val slotLabel = actualLessonLabel(course)
    val subtitle = listOfNotNull(course.scheduleLocation, course.scheduleTeacher)
        .joinToString(" · ")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(
                    color = if (isLead) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                    shape = AppShapes.pill
                )
        )
        if (slotLabel != null) {
            Column(modifier = Modifier.width(64.dp)) {
                Text(
                    text = slotLabel,
                    style = MaterialTheme.typography.titleSmall.withTabularNumbers(),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = course.scheduleName ?: stringResource(R.string.schedule_course_unnamed),
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = GeneratedDesignTokens.titleSize),
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.withTabularNumbers(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun QuickStatusStrip(
    cardBalance: String?,
    courseCount: Int?,
    noticeCount: Int?,
    onOpenCard: () -> Unit,
    onOpenSchedule: () -> Unit,
    onOpenNotices: () -> Unit
) {
    val items = buildList {
        if (cardBalance != null) {
            add(Triple(stringResource(R.string.card_balance), "¥$cardBalance", onOpenCard))
        }
        if (courseCount != null) {
            add(Triple(stringResource(R.string.home_today_schedule_title), courseCount.toString(), onOpenSchedule))
        }
        if (noticeCount != null) {
            add(Triple(stringResource(R.string.home_system_notice_title), noticeCount.toString(), onOpenNotices))
        }
    }
    if (items.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(thickness = 0.75.dp, color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            items.forEachIndexed { index, (label, value, onClick) ->
                if (index > 0) {
                    VerticalDivider(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(vertical = 12.dp),
                        thickness = 0.75.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(role = Role.Button, onClick = onClick)
                        .padding(horizontal = 10.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleLarge.withTabularNumbers(),
                        fontWeight = FontWeight.SemiBold,
                        color = if (index == 0 && cardBalance != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        HorizontalDivider(thickness = 0.75.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun NoticeSection(
    notices: List<AnnouncementItem>,
    noticeError: String?,
    onOpenAll: () -> Unit,
    onOpenNotice: (AnnouncementItem) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        SectionHeader(
            title = stringResource(R.string.home_system_notice_title),
            action = {
                TextButton(onClick = onOpenAll) {
                    Text(stringResource(R.string.home_view_all))
                }
            }
        )
        HorizontalDivider(thickness = 0.75.dp, color = MaterialTheme.colorScheme.outlineVariant)
        if (notices.isEmpty() && !noticeError.isNullOrBlank()) {
            Text(
                text = noticeError,
                modifier = Modifier.padding(vertical = 12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
            HorizontalDivider(thickness = 0.75.dp, color = MaterialTheme.colorScheme.outlineVariant)
        } else {
            notices.take(4).forEachIndexed { index, notice ->
                if (index > 0) {
                    HorizontalDivider(thickness = 0.75.dp, color = MaterialTheme.colorScheme.outlineVariant)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 54.dp)
                        .clickable(role = Role.Button, onClick = { onOpenNotice(notice) })
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = notice.title,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (notice.publishTime.isNotBlank()) {
                            Text(
                                text = notice.publishTime,
                                style = MaterialTheme.typography.bodySmall.withTabularNumbers(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            HorizontalDivider(thickness = 0.75.dp, color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
private fun FeatureGroupSection(
    group: AppFeatureGroup,
    features: List<AppFeature>,
    onNavigate: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader(
            title = stringResource(group.titleRes),
            supporting = stringResource(group.subtitleRes)
        )
        HorizontalDivider(thickness = 0.75.dp, color = MaterialTheme.colorScheme.outlineVariant)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            features.chunked(4).forEach { rowItems ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    rowItems.forEach { feature ->
                        FeatureGridItem(
                            feature = feature,
                            onClick = { onNavigate(feature.route) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(4 - rowItems.size) { SpacerWeight() }
                }
            }
        }
    }
}

@Composable
private fun RowScope.SpacerWeight() {
    Spacer(Modifier.weight(1f))
}

@Composable
private fun FeatureGridItem(
    feature: AppFeature,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .testTag("home.entry.${feature.route}")
            .clip(AppShapes.button)
            .clickable(role = Role.Button, onClick = onClick)
            .heightIn(min = 82.dp)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = AppShapes.button,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            border = hairline()
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = HomeFeatureIcons.forRoute(feature.route) ?: feature.icon,
                    contentDescription = stringResource(feature.titleRes),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(21.dp)
                )
            }
        }
        Text(
            text = stringResource(feature.titleRes),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
