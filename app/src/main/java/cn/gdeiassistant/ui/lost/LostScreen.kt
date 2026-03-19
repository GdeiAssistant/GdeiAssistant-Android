package cn.gdeiassistant.ui.lost

import android.widget.Toast
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.rounded.GppMaybe
import androidx.compose.material.icons.rounded.Report
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import cn.gdeiassistant.ui.components.BadgePill
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.StatusBanner
import cn.gdeiassistant.ui.components.TintButton
import cn.gdeiassistant.ui.util.asString
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LostScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: LostViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is LostEvent.ShowMessage -> Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    LostContent(
        state = state,
        onBack = navController::popBackStack,
        onPasswordChange = viewModel::updatePassword,
        onSubmit = viewModel::submit
    )
}

@Composable
private fun LostContent(
    state: LostUiState,
    onBack: () -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    val errorText = state.error?.asString()

    LazyScreen(
        title = stringResource(R.string.lost_title),
        onBack = onBack
    ) {
        item {
            LostOverviewCard()
        }
        item {
            LostProcessCard()
        }
        item {
            LostNoticeCard()
        }
        item {
            AnimatedContent(
                targetState = when {
                    !errorText.isNullOrBlank() -> "error"
                    state.hasSucceeded -> "success"
                    else -> "none"
                },
                label = "lost-feedback"
            ) { status ->
                when (status) {
                    "error" -> StatusBanner(
                        title = stringResource(R.string.load_failed),
                        body = errorText.orEmpty(),
                        icon = Icons.Rounded.Report
                    )
                    "success" -> StatusBanner(
                        title = stringResource(R.string.lost_success),
                        body = stringResource(R.string.lost_success_hint),
                        icon = Icons.Rounded.TaskAlt,
                        surface = MaterialTheme.colorScheme.primaryContainer,
                        border = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    else -> Spacer(modifier = Modifier.height(0.dp))
                }
            }
        }
        item {
            PasswordFormCard(
                state = state,
                onPasswordChange = onPasswordChange,
                onSubmit = onSubmit
            )
        }
    }
}

@Composable
private fun LostOverviewCard() {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        BadgePill(text = stringResource(R.string.lost_warning_title), tint = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.lost_subtitle),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun LostProcessCard() {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.lost_process_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.lost_action_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ProcessStep(
                icon = Icons.Outlined.Lock,
                title = stringResource(R.string.lost_step_verify),
                modifier = Modifier.weight(1f)
            )
            ProcessStep(
                icon = Icons.Rounded.GppMaybe,
                title = stringResource(R.string.lost_step_report),
                modifier = Modifier.weight(1f)
            )
            ProcessStep(
                icon = Icons.Rounded.TaskAlt,
                title = stringResource(R.string.lost_step_followup),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ProcessStep(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(18.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun LostNoticeCard() {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.lost_notice_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.lost_warning_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PasswordFormCard(
    state: LostUiState,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.lost_form_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.lost_form_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(14.dp))
        OutlinedTextField(
            value = state.password,
            onValueChange = { value ->
                onPasswordChange(value.filter(Char::isDigit).take(6))
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.lost_password_hint)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            leadingIcon = {
                Icon(Icons.Outlined.Lock, contentDescription = null)
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onSubmit() })
        )
        Spacer(modifier = Modifier.height(16.dp))
        TintButton(
            text = if (state.isLoading) {
                stringResource(R.string.lost_processing)
            } else {
                stringResource(R.string.lost_submit)
            },
            onClick = onSubmit,
            enabled = state.canSubmit,
            icon = if (state.isLoading) null else Icons.Rounded.Report,
            modifier = Modifier.fillMaxWidth(),
            tint = MaterialTheme.colorScheme.error
        )
    }
}
