package cn.gdeiassistant.ui.grade

import androidx.annotation.StringRes
import cn.gdeiassistant.ui.theme.AppShapes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import cn.gdeiassistant.model.Grade
import cn.gdeiassistant.ui.components.BadgePill
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.SectionCard

data class GradeDetailArgs(val grade: Grade, @StringRes val termLabelResId: Int)

@Composable
fun GradeDetailScreen(navController: NavHostController, args: GradeDetailArgs?) {
    GradeDetailContent(args = args, onBack = navController::popBackStack)
}

@Composable
private fun GradeDetailContent(args: GradeDetailArgs?, onBack: () -> Unit) {
    val grade = args?.grade
    val termLabelResId = args?.termLabelResId
    val scoreText = grade?.gradeScore?.takeIf { it.isNotBlank() } ?: "—"
    val gpaText = grade?.gradeGpa?.takeIf { it.isNotBlank() } ?: "—"
    val creditText = grade?.gradeCredit?.takeIf { it.isNotBlank() } ?: "—"
    val (scoreBg, scoreFg) = gradeTone(scoreText)

    LazyScreen(
        title = stringResource(R.string.grade_detail_title),
        onBack = onBack
    ) {
        item {
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
                                text = termLabelResId?.let { stringResource(it) }
                                    ?: stringResource(R.string.grade_term_first),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            BadgePill(
                                text = grade?.gradeYear?.takeIf { it.isNotBlank() }
                                    ?: stringResource(R.string.grade_detail_year_placeholder),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = grade?.gradeName?.takeIf { it.isNotBlank() } ?: stringResource(R.string.grade_no_data),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = grade?.gradeType?.takeIf { it.isNotBlank() }
                                ?: stringResource(R.string.grade_row_meta_fallback),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Surface(shape = AppShapes.pill, color = scoreBg) {
                        Text(
                            text = scoreText,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = scoreFg
                        )
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DetailMetric(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.grade_detail_score_label),
                        value = scoreText
                    )
                    DetailMetric(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.grade_credit),
                        value = creditText
                    )
                    DetailMetric(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.grade_gpa),
                        value = gpaText
                    )
                }
            }
        }
        item {
            DetailSection(stringResource(R.string.grade_detail_course_info)) {
                DetailRow(stringResource(R.string.grade_course_id), grade?.gradeId ?: "—")
                DetailRow(stringResource(R.string.grade_course_type), grade?.gradeType ?: "—")
                DetailRow(stringResource(R.string.grade_detail_year), grade?.gradeYear ?: "—")
                DetailRow(
                    stringResource(R.string.grade_detail_term),
                    grade?.gradeTerm ?: termLabelResId?.let { stringResource(it) }.orEmpty().ifBlank { "—" }
                )
            }
        }
    }
}

@Composable
private fun DetailMetric(
    modifier: Modifier,
    label: String,
    value: String
) {
    Surface(
        modifier = modifier,
        shape = AppShapes.card,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DetailSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(14.dp))
        content()
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(12.dp))
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}
