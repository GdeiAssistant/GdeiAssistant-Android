package cn.gdeiassistant.ui.evaluate

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.FactCheck
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import cn.gdeiassistant.ui.components.BadgePill
import cn.gdeiassistant.ui.components.GhostButton
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.StatusBanner
import cn.gdeiassistant.ui.components.TintButton
import cn.gdeiassistant.ui.util.asString
import kotlinx.coroutines.flow.collectLatest

@Composable
fun EvaluateScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: EvaluateViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is EvaluateEvent.ShowMessage -> Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    EvaluateContent(
        state = state,
        onBack = navController::popBackStack,
        onDirectSubmitChange = viewModel::setDirectSubmit,
        onSubmit = viewModel::submit
    )
}

@Composable
private fun EvaluateContent(
    state: EvaluateUiState,
    onBack: () -> Unit,
    onDirectSubmitChange: (Boolean) -> Unit,
    onSubmit: () -> Unit
) {
    val errorText = state.error?.asString()
    val successText = state.status?.asString()
    val showConfirmDialog = remember { mutableStateOf(false) }

    LazyScreen(
        title = stringResource(R.string.evaluate_title),
        onBack = onBack
    ) {
        if (!errorText.isNullOrBlank()) {
            item {
                StatusBanner(
                    title = stringResource(R.string.evaluate_submit_failed_title),
                    body = errorText,
                    icon = Icons.AutoMirrored.Rounded.FactCheck
                )
            }
        }
        if (!successText.isNullOrBlank()) {
            item {
                StatusBanner(
                    title = stringResource(R.string.evaluate_submit_success_title),
                    body = successText,
                    icon = Icons.Rounded.CheckCircle,
                    surface = MaterialTheme.colorScheme.secondaryContainer,
                    border = MaterialTheme.colorScheme.secondary.copy(alpha = 0.28f),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }
        item {
            EvaluateModeCard(
                state = state,
                onDirectSubmitChange = onDirectSubmitChange,
                onRequestSubmit = { showConfirmDialog.value = true }
            )
        }
        item {
            EvaluateNoticeCard()
        }
    }

    if (showConfirmDialog.value) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog.value = false },
            title = { Text(text = stringResource(R.string.evaluate_confirm_title)) },
            text = {
                Text(
                    text = if (state.directSubmit) {
                        stringResource(R.string.evaluate_confirm_direct_message)
                    } else {
                        stringResource(R.string.evaluate_confirm_saved_message)
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog.value = false
                        onSubmit()
                    }
                ) {
                    Text(text = stringResource(R.string.evaluate_confirm_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog.value = false }) {
                    Text(text = stringResource(R.string.back))
                }
            }
        )
    }
}

@Composable
private fun EvaluateModeCard(
    state: EvaluateUiState,
    onDirectSubmitChange: (Boolean) -> Unit,
    onRequestSubmit: () -> Unit
) {
    SectionCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.06f)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    BadgePill(
                        text = stringResource(
                            if (state.directSubmit) R.string.evaluate_mode_direct_badge else R.string.evaluate_mode_saved_badge
                        ),
                        tint = if (state.directSubmit) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.tertiary
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.evaluate_form_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.evaluate_form_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = state.directSubmit,
                    onCheckedChange = onDirectSubmitChange,
                    enabled = !state.isSubmitting
                )
            }

            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Rounded.TaskAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.evaluate_direct_submit_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        AnimatedContent(targetState = state.directSubmit, label = "evaluate_mode_description") { direct ->
                            Text(
                                text = if (direct) {
                                    stringResource(R.string.evaluate_mode_direct_hint)
                                } else {
                                    stringResource(R.string.evaluate_mode_saved_hint)
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (state.isSubmitting) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.evaluate_submitting),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TintButton(
                    text = stringResource(R.string.evaluate_submit),
                    onClick = onRequestSubmit,
                    modifier = Modifier.weight(1f),
                    enabled = !state.isSubmitting,
                    icon = Icons.AutoMirrored.Rounded.Send
                )
                GhostButton(
                    text = stringResource(R.string.evaluate_toggle_action),
                    onClick = { onDirectSubmitChange(!state.directSubmit) },
                    modifier = Modifier.weight(1f),
                    enabled = !state.isSubmitting,
                    icon = Icons.Rounded.TaskAlt,
                    borderColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.28f),
                    contentColor = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun EvaluateNoticeCard() {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.evaluate_notice_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.evaluate_notice_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
        ) {
            Text(
                text = stringResource(R.string.evaluate_notice_tip),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}
