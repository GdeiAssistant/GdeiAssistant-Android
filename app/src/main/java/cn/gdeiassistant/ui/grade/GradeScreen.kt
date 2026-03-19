package cn.gdeiassistant.ui.grade

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.SelectionPill
import cn.gdeiassistant.ui.components.StatusBanner
import cn.gdeiassistant.ui.components.TextTabSelector
import cn.gdeiassistant.ui.navigation.Routes
import cn.gdeiassistant.ui.util.asString

@Composable
fun GradeScreen(navController: NavHostController) {
    val viewModel: GradeViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    GradeContent(
        state = state,
        onBack = navController::popBackStack,
        onRefresh = viewModel::refresh,
        onYearChange = viewModel::setYear,
        onTermChange = viewModel::setTerm,
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

@Composable
private fun GradeContent(
    state: GradeUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onYearChange: (Int) -> Unit,
    onTermChange: (GradeTerm) -> Unit,
    onOpenDetail: (Grade, Int) -> Unit
) {
    LazyScreen(
        title = stringResource(R.string.grade_title),
        onBack = onBack,
        actions = {
            IconButton(onClick = onRefresh, enabled = !state.isLoading) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.grade_retry))
            }
        },
        showLoadingPlaceholder = state.isLoading && state.rawResult == null
    ) {
        item {
            GradeFilterCard(
                selectedYear = state.selectedYear,
                selectedTerm = state.selectedTerm,
                totalCourseCount = state.totalCourseCount,
                onYearChange = onYearChange,
                onTermChange = onTermChange
            )
        }
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
            AnimatedContent(targetState = state.selectedTerm, label = "grade_term_snapshot") { selectedTerm ->
                TermSnapshotCard(
                    yearLabelRes = yearOptions().firstOrNull { it.first == state.selectedYear }?.second
                        ?: R.string.grade_year_one,
                    term = selectedTerm,
                    gpa = fmtMetric(state.gpaFor(selectedTerm)),
                    igp = fmtMetric(state.igpFor(selectedTerm)),
                    grades = state.gradesFor(selectedTerm)
                )
            }
        }
        item {
            AnimatedContent(targetState = state.selectedTerm, label = "grade_term_list") { selectedTerm ->
                GradeListSection(
                    term = selectedTerm,
                    grades = state.gradesFor(selectedTerm),
                    onOpenDetail = { onOpenDetail(it, selectedTerm.labelResId()) }
                )
            }
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

@Composable
private fun GradeFilterCard(
    selectedYear: Int,
    selectedTerm: GradeTerm,
    totalCourseCount: Int,
    onYearChange: (Int) -> Unit,
    onTermChange: (GradeTerm) -> Unit
) {
    val termLabels = listOf(
        stringResource(R.string.grade_term_first),
        stringResource(R.string.grade_term_second)
    )
    SectionCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        Text(
            text = stringResource(R.string.grade_selector_year),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(yearOptions()) { (year, labelRes) ->
                SelectionPill(
                    text = stringResource(labelRes),
                    selected = year == selectedYear,
                    onClick = { onYearChange(year) }
                )
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = stringResource(R.string.grade_selector_term),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(10.dp))
        TextTabSelector(
            labels = termLabels,
            selectedIndex = if (selectedTerm == GradeTerm.FIRST) 0 else 1,
            onSelect = { index -> onTermChange(if (index == 0) GradeTerm.FIRST else GradeTerm.SECOND) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.grade_filter_hint, totalCourseCount),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TermSnapshotCard(
    yearLabelRes: Int,
    term: GradeTerm,
    gpa: String,
    igp: String,
    grades: List<Grade>
) {
    SectionCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BadgePill(
                        text = stringResource(yearLabelRes),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    BadgePill(
                        text = stringResource(term.labelResId()),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = stringResource(R.string.grade_summary_title, grades.size),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (grades.isEmpty()) {
                        stringResource(R.string.grade_term_empty)
                    } else {
                        stringResource(R.string.grade_summary_subtitle)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GradeMetricCard(
                label = stringResource(R.string.grade_metric_courses),
                value = grades.size.toString(),
                modifier = Modifier.weight(1f)
            )
            GradeMetricCard(
                label = stringResource(R.string.grade_gpa),
                value = gpa,
                modifier = Modifier.weight(1f)
            )
            GradeMetricCard(
                label = stringResource(R.string.grade_metric_igp),
                value = igp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun GradeMetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun GradeListSection(
    term: GradeTerm,
    grades: List<Grade>,
    onOpenDetail: (Grade) -> Unit
) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.grade_list_title, stringResource(term.labelResId())),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.grade_list_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(14.dp))
        if (grades.isEmpty()) {
            Text(
                text = stringResource(R.string.grade_term_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            grades.forEachIndexed { index, grade ->
                if (index > 0) HorizontalDivider()
                GradeRow(
                    grade = grade,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onOpenDetail(grade) }
                )
            }
        }
    }
}

@Composable
private fun GradeRow(
    grade: Grade,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val (badgeBg, badgeFg) = scoreBadgeStyle(grade.gradeScore)
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = grade.gradeName ?: stringResource(R.string.grade_no_data),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = listOfNotNull(grade.gradeId, grade.gradeType).joinToString(" · ").ifBlank {
                    stringResource(R.string.grade_row_meta_fallback)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(
                        R.string.grade_row_metric,
                        grade.gradeCredit ?: "—",
                        grade.gradeGpa ?: "—"
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Surface(shape = RoundedCornerShape(999.dp), color = badgeBg) {
            Text(
                text = grade.gradeScore ?: "—",
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = badgeFg
            )
        }
    }
}

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

private fun GradeUiState.gradesFor(term: GradeTerm): List<Grade> = when (term) {
    GradeTerm.FIRST -> firstTermGrades
    GradeTerm.SECOND -> secondTermGrades
}

private fun GradeUiState.gpaFor(term: GradeTerm): Double? = when (term) {
    GradeTerm.FIRST -> rawResult?.firstTermGPA
    GradeTerm.SECOND -> rawResult?.secondTermGPA
}

private fun GradeUiState.igpFor(term: GradeTerm): Double? = when (term) {
    GradeTerm.FIRST -> rawResult?.firstTermIGP
    GradeTerm.SECOND -> rawResult?.secondTermIGP
}

private fun GradeTerm.labelResId(): Int = when (this) {
    GradeTerm.FIRST -> R.string.grade_term_first
    GradeTerm.SECOND -> R.string.grade_term_second
}
