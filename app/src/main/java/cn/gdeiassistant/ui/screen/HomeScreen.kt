package cn.gdeiassistant.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import cn.gdeiassistant.R
import cn.gdeiassistant.model.AnnouncementItem
import cn.gdeiassistant.model.Schedule
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.home.HomeViewModel
import cn.gdeiassistant.ui.navigation.AppFeature
import cn.gdeiassistant.ui.navigation.AppFeatureCatalog
import cn.gdeiassistant.ui.navigation.AppFeatureGroup
import cn.gdeiassistant.ui.navigation.Routes
import cn.gdeiassistant.ui.theme.AppShapes
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val SectionStartTimes = mapOf(
    0 to "08:00", 1 to "10:00", 2 to "14:00",
    3 to "16:00", 4 to "19:00", 5 to "20:50"
)

@Composable
fun HomeScreen(navController: NavController) {
    val viewModel: HomeViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    val greeting = when (LocalTime.now().hour) {
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
    val todayLabel = LocalDate.now()
        .format(DateTimeFormatter.ofPattern(datePattern))

    LazyScreen(
        title = greetingText,
        showLoadingPlaceholder = state.isLoading
    ) {
        item {
            GreetingHeader(
                dateLabel = todayLabel,
                courseCount = state.todayCourses.size,
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
    courseCount: Int = 0,
    cardBalance: String? = null
) {
    val courseSummary = if (courseCount > 0) {
        stringResource(R.string.home_stats_course_count, courseCount)
    } else {
        stringResource(R.string.home_stats_no_course)
    }
    val balanceSummary = cardBalance
        ?.takeIf { it.isNotBlank() }
        ?.let { stringResource(R.string.home_stats_card_balance, it) }
    val statsLine = listOfNotNull(courseSummary, balanceSummary).joinToString(" · ")
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AssistChip(onClick = {}, enabled = false, label = { Text(dateLabel) })
        Text(
            text = statsLine,
            style = MaterialTheme.typography.bodyMedium,
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.home_today_schedule_title)) },
                supportingContent = {
                    Text(
                        text = if (courses.isEmpty()) {
                            stringResource(R.string.home_today_schedule_empty_summary)
                        } else {
                            stringResource(R.string.home_today_schedule_count, courses.size)
                        }
                    )
                },
                trailingContent = {
                    TextButton(onClick = onOpenSchedule) {
                        Text(stringResource(R.string.home_view_schedule))
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
            AnimatedContent(
                targetState = error to courses,
                label = "schedule_grid",
                transitionSpec = { fadeIn() togetherWith fadeOut() }
            ) { (err, items) ->
                when {
                    !err.isNullOrBlank() -> {
                        Text(
                            text = err,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    items.isEmpty() -> {
                        ListItem(
                            headlineContent = {
                                Text(stringResource(R.string.home_today_schedule_empty_title))
                            },
                            supportingContent = {
                                Text(stringResource(R.string.home_today_schedule_empty_body))
                            },
                            leadingContent = {
                                Icon(Icons.Rounded.EventAvailable, contentDescription = null)
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                    else -> Column {
                        items.forEach { course -> CourseRow(course) }
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseRow(course: Schedule) {
    val slotLabel = remember(course.row, course.scheduleLength) {
        val r = course.row ?: 0
        val len = course.scheduleLength ?: 1
        val s = r * 2 + 1
        val e = (r + len - 1) * 2 + 2
        "$s-$e"
    }
    val startTime = SectionStartTimes[course.row ?: 0] ?: ""
    val subtitle = listOfNotNull(course.scheduleLocation, course.scheduleTeacher)
        .joinToString(" · ")
    ListItem(
        headlineContent = {
            Text(
                text = course.scheduleName ?: stringResource(R.string.schedule_course_unnamed),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            if (subtitle.isNotBlank()) {
                Text(text = subtitle, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        },
        leadingContent = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = startTime, style = MaterialTheme.typography.labelLarge)
                Text(text = slotLabel, style = MaterialTheme.typography.labelSmall)
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
private fun NoticeSection(
    notices: List<AnnouncementItem>,
    onOpenAll: () -> Unit,
    onOpenNotice: (AnnouncementItem) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
    ) {
        Column {
            ListItem(
                headlineContent = { Text(stringResource(R.string.home_system_notice_title)) },
                trailingContent = {
                    TextButton(onClick = onOpenAll) {
                        Text(stringResource(R.string.home_view_all))
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
            notices.take(5).forEachIndexed { index, notice ->
                if (index > 0) HorizontalDivider()
                ListItem(
                    headlineContent = {
                        Text(text = notice.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                    supportingContent = { Text(text = notice.publishTime) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenNotice(notice) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
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
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(group.titleRes),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(group.subtitleRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                features.chunked(4).forEach { rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
        modifier = modifier.testTag("home.entry.${feature.route}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        FilledTonalIconButton(onClick = onClick) {
            Icon(
                imageVector = feature.icon,
                contentDescription = stringResource(feature.titleRes)
            )
        }
        Text(
            text = stringResource(feature.titleRes),
            style = MaterialTheme.typography.labelMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
