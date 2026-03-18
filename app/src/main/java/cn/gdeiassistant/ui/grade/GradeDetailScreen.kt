package cn.gdeiassistant.ui.grade

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import cn.gdeiassistant.model.Grade
import cn.gdeiassistant.ui.components.LazyScreen

data class GradeDetailArgs(val grade: Grade, @StringRes val termLabelResId: Int)

@Composable
fun GradeDetailScreen(navController: NavHostController, args: GradeDetailArgs?) {
    GradeDetailContent(args = args, onBack = navController::popBackStack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GradeDetailContent(args: GradeDetailArgs?, onBack: () -> Unit) {
    val grade = args?.grade
    val termLabelResId = args?.termLabelResId
    val scoreText = grade?.gradeScore?.takeIf { it.isNotBlank() } ?: "—"
    val gpaText = grade?.gradeGpa?.takeIf { it.isNotBlank() } ?: "—"
    val creditText = grade?.gradeCredit?.takeIf { it.isNotBlank() } ?: "—"

    LazyScreen(
        title = stringResource(R.string.grade_detail_title),
        onBack = onBack
    ) {
        // 英雄卡
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)))
                        .padding(20.dp)
                ) {
                    Text(
                        grade?.gradeName?.takeIf { it.isNotBlank() } ?: stringResource(R.string.grade_no_data),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        termLabelResId?.let { stringResource(it) } ?: stringResource(R.string.grade_term_first),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.84f)
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        HeroMetric(Modifier.weight(1f), stringResource(R.string.grade_detail_score_label), scoreText)
                        HeroMetric(Modifier.weight(1f), stringResource(R.string.grade_credit), creditText)
                        HeroMetric(Modifier.weight(1f), stringResource(R.string.grade_gpa), gpaText)
                    }
                }
            }
        }
        // 详情
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
private fun HeroMetric(modifier: Modifier, label: String, value: String) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = Color.White.copy(alpha = 0.14f)) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
private fun DetailSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            content()
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(12.dp))
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}
