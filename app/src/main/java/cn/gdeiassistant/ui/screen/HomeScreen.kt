package cn.gdeiassistant.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import cn.gdeiassistant.ui.components.ListDivider
import cn.gdeiassistant.ui.components.ListGroup
import cn.gdeiassistant.ui.components.ListRow
import cn.gdeiassistant.ui.components.SectionHeader
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
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
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.home.HomeViewModel
import cn.gdeiassistant.ui.home.HomeFeatureIcons
import cn.gdeiassistant.ui.navigation.AppFeature
import cn.gdeiassistant.ui.navigation.AppFeatureCatalog
import cn.gdeiassistant.ui.navigation.AppFeatureGroup
import cn.gdeiassistant.ui.navigation.Routes
import cn.gdeiassistant.ui.theme.AppShapes
import cn.gdeiassistant.ui.theme.AppSpacing
import cn.gdeiassistant.ui.theme.GeneratedDesignTokens
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

    LazyScreen(
        title = greetingText,
        showLoadingPlaceholder = state.isLoading
    ) {
        item {
            GreetingHeader(
                dateLabel = todayLabel,
                courseCount = state.todayCourses.size.takeIf { state.scheduleError == null },
                cardBalance = state.cardInfo?.cardBalance
            )
        }
        item {
            TodayScheduleCard(
                courses = state.todayCourses,
                error = state.scheduleError,
                onOpenSchedule = { navController.navigate(Routes.SCHEDULE) }
            )
        }
        if (state.notices.isNotEmpty() || !state.noticeError.isNullOrBlank()) {
            item {
                NoticeSection(
                    notices = state.notices,
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

@Composable
private fun GreetingHeader(
    modifier: Modifier = Modifier,
    dateLabel: String,
    courseCount: Int? = null,
    cardBalance: String? = null
) {
    val courseSummary = when {
        courseCount == null -> stringResource(R.string.service_unavailable)
        courseCount > 0 -> stringResource(R.string.home_stats_course_count, courseCount)
        else -> stringResource(R.string.home_stats_no_course)
    }
    val balanceSummary = cardBalance
        ?.takeIf { it.isNotBlank() }
        ?.let { stringResource(R.string.home_stats_card_balance, it) }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = dateLabel,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = GeneratedDesignTokens.captionSize),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = courseSummary,
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = GeneratedDesignTokens.headlineSize),
            color = MaterialTheme.colorScheme.onSurface
        )
        balanceSummary?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TodayScheduleCard(
    courses: List<Schedule>,
    error: String?,
    onOpenSchedule: () -> Unit
) {
    Surface(
        shape = AppShapes.card,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(
            modifier = Modifier.padding(GeneratedDesignTokens.pageSpacing),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionHeader(
                title = stringResource(R.string.home_today_schedule_title),
                supporting = if (error != null) {
                    stringResource(R.string.service_unavailable)
                } else if (courses.isEmpty()) {
                    stringResource(R.string.home_today_schedule_empty_summary)
                } else {
                    stringResource(R.string.home_today_schedule_count, courses.size)
                },
                action = {
                    TextButton(onClick = onOpenSchedule) {
                        Text(stringResource(R.string.home_view_schedule))
                    }
                }
            )
            Column {
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
                                if (index > 0) ListDivider(inset = 88.dp)
                                CourseRow(course)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseRow(course: Schedule) {
    val slotLabel = actualLessonLabel(course)
    val subtitle = listOfNotNull(course.scheduleLocation, course.scheduleTeacher)
        .joinToString(" · ")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (slotLabel != null) {
            Column(modifier = Modifier.width(64.dp)) {
                Text(
                    text = slotLabel,
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = GeneratedDesignTokens.calloutSize),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.primary, AppShapes.pill)
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = course.scheduleName ?: stringResource(R.string.schedule_course_unnamed),
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = GeneratedDesignTokens.titleSize),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = GeneratedDesignTokens.captionSize),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun NoticeSection(
    notices: List<AnnouncementItem>,
    onOpenAll: () -> Unit,
    onOpenNotice: (AnnouncementItem) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(
            title = stringResource(R.string.home_system_notice_title),
            action = {
                TextButton(onClick = onOpenAll) {
                    Text(stringResource(R.string.home_view_all))
                }
            }
        )
        ListGroup {
            notices.take(5).forEachIndexed { index, notice ->
                if (index > 0) ListDivider(inset = 16.dp)
                ListRow(
                    title = notice.title,
                    supporting = notice.publishTime,
                    onClick = { onOpenNotice(notice) }
                )
            }
        }
    }
}

@Composable
private fun FeatureGroupSection(
    group: AppFeatureGroup,
    features: List<AppFeature>,
    onNavigate: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(
            title = stringResource(group.titleRes),
            supporting = stringResource(group.subtitleRes)
        )
        ListGroup {
            Column(
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                features.chunked(4).forEach { rowItems ->
                    Row {
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
}

@Composable
private fun RowScope.SpacerWeight() {
    androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
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
            .heightIn(min = 84.dp)
            .padding(horizontal = 4.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = AppShapes.button,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = HomeFeatureIcons.forRoute(feature.route) ?: feature.icon,
                    contentDescription = stringResource(feature.titleRes),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Text(
            text = stringResource(feature.titleRes),
            style = MaterialTheme.typography.labelMedium.copy(fontSize = GeneratedDesignTokens.calloutSize),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
