package cn.gdeiassistant.ui.graduate

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import cn.gdeiassistant.ui.components.EmptyState
import cn.gdeiassistant.ui.components.ActionTile
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.SectionCard

@Composable
fun GraduateExamScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: GraduateExamViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val score = state.score

    if (!state.error.isNullOrBlank()) {
        AlertDialog(
            onDismissRequest = viewModel::dismissError,
            title = { Text(stringResource(R.string.load_failed)) },
            text = { Text(state.error.orEmpty()) },
            confirmButton = {
                TextButton(onClick = viewModel::dismissError) {
                    Text(stringResource(R.string.back))
                }
            }
        )
    }

    LazyScreen(
        title = stringResource(R.string.graduate_exam_title),
        onBack = navController::popBackStack
    ) {
        item {
            ActionTile(
                title = stringResource(R.string.graduate_exam_backup_title),
                subtitle = stringResource(R.string.graduate_exam_backup_subtitle),
                icon = Icons.Rounded.OpenInBrowser,
                onClick = {
                    val url = context.getString(R.string.graduate_exam_backup_url)
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                },
                tint = MaterialTheme.colorScheme.tertiary,
                emphasized = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            SectionCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.graduate_exam_form_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(14.dp))
                OutlinedTextField(
                    value = state.query.name,
                    onValueChange = viewModel::updateName,
                    label = { Text(text = stringResource(R.string.graduate_exam_name_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
                OutlinedTextField(
                    value = state.query.examNumber,
                    onValueChange = viewModel::updateExamNumber,
                    label = { Text(text = stringResource(R.string.graduate_exam_exam_number_hint)) },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth()
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
                OutlinedTextField(
                    value = state.query.idNumber,
                    onValueChange = viewModel::updateIdNumber,
                    label = { Text(text = stringResource(R.string.graduate_exam_id_number_hint)) },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth()
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(16.dp))
                Button(
                    onClick = viewModel::submit,
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.graduate_exam_query_action))
                }
            }
        }
        when {
            state.isLoading -> item { LoadingBlock() }
            score != null -> {
                item { ScoreSummaryCard(score = score) }
                item { ScoreDetailCard(score = score) }
            }
            else -> item {
                Box(
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
        }
    }
}

@Composable
private fun LoadingBlock() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.CircularProgressIndicator()
    }
}

@Composable
private fun ScoreSummaryCard(score: GraduateExamScore) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.graduate_exam_result_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = stringResource(R.string.graduate_exam_total_score_value, score.totalScore),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = "${score.name} · ${score.examNumber}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(14.dp))
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(10.dp))
}
