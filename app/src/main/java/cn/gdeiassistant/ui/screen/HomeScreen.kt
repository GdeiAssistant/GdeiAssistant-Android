package cn.gdeiassistant.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import cn.gdeiassistant.R
import cn.gdeiassistant.model.AnnouncementItem
import cn.gdeiassistant.model.Schedule
import cn.gdeiassistant.ui.components.*
import cn.gdeiassistant.ui.home.HomeViewModel
import cn.gdeiassistant.ui.navigation.AppFeature
import cn.gdeiassistant.ui.navigation.AppFeatureCatalog
import cn.gdeiassistant.ui.navigation.AppFeatureGroup
import cn.gdeiassistant.ui.navigation.Routes
import cn.gdeiassistant.ui.theme.AppShapes
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

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
    val todayLabel = LocalDate.now()
        .format(DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.SIMPLIFIED_CHINESE))

    LazyScreen(title = greetingText) {
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
                NoticeBento(
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
    val timeEmoji = remember {
        when (LocalTime.now().hour) {
            in 5..11  -> "☀️"
            in 12..13 -> "🌤️"
            in 14..17 -> "🌅"
            in 18..23 -> "🌙"
            else      -> "✨"
        }
    }
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
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        BadgePill(text = dateLabel)
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = timeEmoji, fontSize = 13.sp)
            Text(
                text = statsLine,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
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
    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.home_today_schedule_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = if (courses.isEmpty()) {
                            stringResource(R.string.home_today_schedule_empty_summary)
                        } else {
                            stringResource(R.string.home_today_schedule_count, courses.size)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
                GhostButton(
                    text = stringResource(R.string.home_view_schedule),
                    onClick = onOpenSchedule,
                    borderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            AnimatedContent(
                targetState = error to courses,
                label = "schedule_grid",
                transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(180)) }
            ) { (err, items) ->
                when {
                    !err.isNullOrBlank() -> {
                        Text(
                            text = err,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                    items.isEmpty() -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text("🏖️", fontSize = 30.sp)
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = stringResource(R.string.home_today_schedule_empty_title),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Text(
                                    text = stringResource(R.string.home_today_schedule_empty_body),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                    else -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items.forEach { course -> CourseRow(course) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseRow(course: Schedule) {
    val isDark = isSystemInDarkTheme()
    val onPrimary = MaterialTheme.colorScheme.onPrimary
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isDark) Color.Black.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.15f),
                AppShapes.small
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.width(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = startTime,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = onPrimary
            )
            Text(
                text = slotLabel,
                style = MaterialTheme.typography.labelSmall,
                color = onPrimary.copy(alpha = 0.7f)
            )
        }
        Box(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .width(2.dp)
                .height(36.dp)
                .background(onPrimary.copy(alpha = 0.4f), CircleShape)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = course.scheduleName ?: stringResource(R.string.schedule_course_unnamed),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = onPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = onPrimary.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun NoticeBento(
    notices: List<AnnouncementItem>,
    onOpenAll: () -> Unit,
    onOpenNotice: (AnnouncementItem) -> Unit
) {
    SectionCard(modifier = Modifier.fillMaxWidth().animateContentSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.home_system_notice_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
            GhostButton(text = stringResource(R.string.home_view_all), onClick = onOpenAll)
        }
        Spacer(modifier = Modifier.height(12.dp))
        AnimatedContent(
            targetState = notices.take(5),
            label = "notice_state",
            transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(180)) }
        ) { items ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items.forEachIndexed { index, notice ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenNotice(notice) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(
                                    if (index == 0) MaterialTheme.colorScheme.secondary
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                    CircleShape
                                )
                        )
                        Text(
                            text = notice.title,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = notice.publishTime,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
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
    SectionCard(modifier = Modifier.fillMaxWidth().animateContentSize()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(group.titleRes),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = stringResource(group.subtitleRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column {
                features.chunked(4).forEach { rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                        rowItems.forEach { feature ->
                            FeatureGridItem(
                                feature = feature,
                                onClick = { onNavigate(feature.route) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(4 - rowItems.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureGridItem(
    feature: AppFeature,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.12f else 0.08f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = feature.icon,
                contentDescription = stringResource(feature.titleRes),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = stringResource(feature.titleRes),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
