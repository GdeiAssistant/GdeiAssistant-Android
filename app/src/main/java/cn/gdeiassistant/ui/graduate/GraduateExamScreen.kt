package cn.gdeiassistant.ui.graduate

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import cn.gdeiassistant.model.GraduateExamScore
import cn.gdeiassistant.ui.components.BadgePill
import cn.gdeiassistant.ui.components.EmptyState
import cn.gdeiassistant.ui.components.GhostButton
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.StatusBanner
import cn.gdeiassistant.ui.components.TintButton

private enum class GraduateResultState {
    Loading,
    Empty,
    Ready
}

@Composable
fun GraduateExamScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: GraduateExamViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyScreen(
        title = stringResource(R.string.graduate_exam_title),
        onBack = navController::popBackStack
    ) {
        if (!state.error.isNullOrBlank()) {
            item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.School
                )
            }
        }
        item {
            GraduateQueryCard(
                state = state,
                onNameChange = viewModel::updateName,
                onExamNumberChange = viewModel::updateExamNumber,
                onIdNumberChange = viewModel::updateIdNumber,
                onSubmit = viewModel::submit,
                onOpenBackup = {
                    val url = context.getString(R.string.graduate_exam_backup_url)
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            )
        }
        item {
            AnimatedContent(
                targetState = when {
                    state.isLoading -> GraduateResultState.Loading
                    state.score == null -> GraduateResultState.Empty
                    else -> GraduateResultState.Ready
                },
                label = "graduate_exam_result_state"
            ) { resultState ->
                when (resultState) {
                    GraduateResultState.Loading -> LoadingBlock()
                    GraduateResultState.Empty -> EmptyBlock()
                    GraduateResultState.Ready -> Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        state.score?.let { score ->
                            ScoreSummaryCard(score = score)
                            ScoreDetailCard(score = score)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GraduateQueryCard(
    state: GraduateExamUiState,
    onNameChange: (String) -> Unit,
    onExamNumberChange: (String) -> Unit,
    onIdNumberChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onOpenBackup: () -> Unit
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
                BadgePill(
                    text = stringResource(
                        if (state.score == null) R.string.graduate_exam_mode_idle else R.string.graduate_exam_mode_ready
                    ),
                    tint = if (state.score == null) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.graduate_exam_form_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.graduate_exam_form_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            GhostButton(
                text = stringResource(R.string.graduate_exam_backup_action),
                onClick = onOpenBackup,
                icon = Icons.Rounded.OpenInBrowser,
                borderColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.28f),
                contentColor = MaterialTheme.colorScheme.tertiary
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        OutlinedTextField(
            value = state.query.name,
            onValueChange = onNameChange,
            label = { Text(text = stringResource(R.string.graduate_exam_name_hint)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = state.query.examNumber,
            onValueChange = onExamNumberChange,
            label = { Text(text = stringResource(R.string.graduate_exam_exam_number_hint)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = state.query.idNumber,
            onValueChange = onIdNumberChange,
            label = { Text(text = stringResource(R.string.graduate_exam_id_number_hint)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        TintButton(
            text = if (state.isLoading) {
                stringResource(R.string.graduate_exam_loading_title)
            } else {
                stringResource(R.string.graduate_exam_query_action)
            },
            onClick = onSubmit,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Rounded.Search
        )
    }
}

@Composable
private fun LoadingBlock() {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            Column {
                Text(
                    text = stringResource(R.string.graduate_exam_loading_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.graduate_exam_loading_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyBlock() {
    SectionCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
    ) {
        EmptyState(
            icon = Icons.Rounded.School,
            message = stringResource(R.string.graduate_exam_empty),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun ScoreSummaryCard(score: GraduateExamScore) {
    SectionCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.06f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BadgePill(
                        text = stringResource(R.string.graduate_exam_result_title),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    BadgePill(
                        text = score.name,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = score.totalScore,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.graduate_exam_total_score_label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                    Text(
                        text = stringResource(R.string.graduate_exam_exam_number_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = score.examNumber,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ScoreDetailCard(score: GraduateExamScore) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.graduate_exam_detail_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(14.dp))
        ScoreInfoRow(stringResource(R.string.graduate_exam_name_label), score.name)
        ScoreInfoRow(stringResource(R.string.graduate_exam_signup_number_label), score.signupNumber)
        ScoreInfoRow(stringResource(R.string.graduate_exam_exam_number_label), score.examNumber)
        ScoreInfoRow(stringResource(R.string.graduate_exam_politics_label), score.politicsScore)
        ScoreInfoRow(stringResource(R.string.graduate_exam_foreign_label), score.foreignLanguageScore)
        ScoreInfoRow(stringResource(R.string.graduate_exam_business_one_label), score.businessOneScore)
        ScoreInfoRow(stringResource(R.string.graduate_exam_business_two_label), score.businessTwoScore)
    }
}

@Composable
private fun ScoreInfoRow(label: String, value: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
}
