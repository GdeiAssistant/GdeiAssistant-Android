package cn.gdeiassistant.ui.grade

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import cn.gdeiassistant.model.Grade
import cn.gdeiassistant.ui.components.BadgePill
import cn.gdeiassistant.ui.components.EmptyState
import cn.gdeiassistant.ui.components.HeroCard
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.MetricChip
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.StatusBanner
import cn.gdeiassistant.ui.navigation.Routes
import cn.gdeiassistant.ui.util.asString

@Composable
fun GradeScreen(navController: NavHostController) {
    val viewModel: GradeViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    GradeContent(
        state = state,
        onBack = navController::popBackStack,
        onYearChange = viewModel::setYear,
        onOpenDetail = { grade, termLabelResId ->
            navController.currentBackStackEntry?.savedStateHandle?.set(Routes.GRADE_DETAIL_GRADE, grade)
            navController.currentBackStackEntry?.savedStateHandle?.set(Routes.GRADE_DETAIL_TERM_LABEL, termLabelResId)
            navController.navigate(Routes.GRADE_DETAIL)
        }
    )
}

private fun yearOptions() = listOf(
    0 to R.string.grade_year_one,
    1 to R.string.grade_year_two,
    2 to R.string.grade_year_three,
    3 to R.string.grade_year_four
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GradeContent(
    state: GradeUiState,
    onBack: () -> Unit,
    onYearChange: (Int) -> Unit,
    onOpenDetail: (Grade, Int) -> Unit
) {
    LazyScreen(
        title = stringResource(R.string.grade_title),
        onBack = onBack,
        showLoadingPlaceholder = state.isLoading && state.rawResult == null
    ) {
        item { GradeSummaryHero(state) }
        item { YearSelector(state.selectedYear, onYearChange) }
        if (state.error != null) {
            item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.asString(),
                    icon = Icons.Rounded.School
                )
            }
        }
        item {
            TermSection(
                title = stringResource(R.string.grade_term_first),
                gpa = fmtMetric(state.rawResult?.firstTermGPA),
                igp = fmtMetric(state.rawResult?.firstTermIGP),
                grades = state.firstTermGrades,
                emptyText = stringResource(R.string.grade_term_empty),
                onOpenDetail = { onOpenDetail(it, R.string.grade_term_first) }
            )
        }
        item {
            TermSection(
                title = stringResource(R.string.grade_term_second),
                gpa = fmtMetric(state.rawResult?.secondTermGPA),
                igp = fmtMetric(state.rawResult?.secondTermIGP),
                grades = state.secondTermGrades,
                emptyText = stringResource(R.string.grade_term_empty),
                onOpenDetail = { onOpenDetail(it, R.string.grade_term_second) }
            )
        }
        if (state.totalCourseCount == 0 && state.error == null) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
                    EmptyState(
                        icon = Icons.Rounded.School,
                        message = stringResource(R.string.grade_empty_hint),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

// ── 成绩摘要英雄卡 ──

@Composable
private fun GradeSummaryHero(state: GradeUiState) {
    HeroCard(modifier = Modifier.fillMaxWidth()) {
        BadgePill(
            text = stringResource(
                yearOptions().firstOrNull { it.first == state.selectedYear }?.second
                    ?: R.string.grade_year_one
            ),
            onGradient = true
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.grade_course_count, state.totalCourseCount),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricChip(
                label = stringResource(R.string.grade_term_first),
                value = fmtMetric(state.rawResult?.firstTermGPA),
                modifier = Modifier.weight(1f),
                onGradient = true
            )
            MetricChip(
                label = stringResource(R.string.grade_term_second),
                value = fmtMetric(state.rawResult?.secondTermGPA),
                modifier = Modifier.weight(1f),
                onGradient = true
            )
        }
    }
}

// ── 学年选择 ──

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun YearSelector(selectedYear: Int, onYearChange: (Int) -> Unit) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(yearOptions()) { (year, labelRes) ->
                FilterChip(
                    selected = year == selectedYear,
                    onClick = { onYearChange(year) },
                    label = { Text(stringResource(labelRes)) },
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
                        selected = year == selectedYear
                    )
                )
            }
        }
    }
}

// ── 学期段落 ──

@Composable
private fun TermSection(
    title: String, gpa: String, igp: String,
    grades: List<Grade>, emptyText: String,
    onOpenDetail: (Grade) -> Unit
) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = stringResource(R.string.grade_term_metrics, gpa, igp),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        if (grades.isEmpty()) {
            Text(emptyText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            grades.forEachIndexed { index, grade ->
                if (index > 0) HorizontalDivider()
                GradeRow(grade, onClick = { onOpenDetail(grade) })
            }
        }
    }
}

@Composable
private fun GradeRow(grade: Grade, onClick: () -> Unit) {
    val (badgeBg, badgeFg) = scoreBadgeStyle(grade.gradeScore)
    ListItem(
        headlineContent = {
            Text(
                grade.gradeName ?: stringResource(R.string.grade_no_data),
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Text(listOfNotNull(grade.gradeId, grade.gradeType).joinToString(" · "))
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                Surface(shape = RoundedCornerShape(999.dp), color = badgeBg) {
                    Text(
                        grade.gradeScore ?: "—",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = badgeFg
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    stringResource(R.string.grade_row_metric, grade.gradeCredit ?: "—", grade.gradeGpa ?: "—"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        modifier = Modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

// ── 工具 ──

private fun fmtMetric(value: Double?): String {
    if (value == null) return "—"
    return if (value % 1.0 == 0.0) value.toInt().toString()
    else String.format(java.util.Locale.US, "%.2f", value)
}

@Composable
private fun scoreBadgeStyle(raw: String?): Pair<Color, Color> {
    val score = raw?.toDoubleOrNull()
        ?: return MaterialTheme.colorScheme.surfaceContainerHighest to MaterialTheme.colorScheme.onSurface
    return when {
        score >= 90 -> Color(0xFFE8FAF3) to Color(0xFF0E7A63)
        score >= 80 -> Color(0xFFEFF6FF) to Color(0xFF1764F6)
        score >= 60 -> Color(0xFFFFF4DE) to Color(0xFFB7791F)
        else        -> Color(0xFFFFECE8) to Color(0xFFC2412D)
    }
}
