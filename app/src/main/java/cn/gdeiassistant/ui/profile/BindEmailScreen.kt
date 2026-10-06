package cn.gdeiassistant.ui.profile

import cn.gdeiassistant.ui.theme.AppShapes
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import cn.gdeiassistant.ui.components.GhostButton
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.StatusBanner
import cn.gdeiassistant.ui.components.TintButton
import kotlinx.coroutines.flow.collectLatest

@Composable
fun BindEmailScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: BindEmailViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var email by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is BindEmailEvent.ShowMessage -> Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    LazyScreen(
        title = stringResource(R.string.profile_bind_email_title),
        onBack = navController::popBackStack,
        actions = {
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.schedule_refresh))
            }
        }
    ) {
        item {
            BindingStatusCard(
                title = stringResource(R.string.profile_email_title),
                value = state.status.maskedValue,
                note = state.status.note,
                icon = Icons.Rounded.Mail,
                tint = MaterialTheme.colorScheme.tertiary
            )
        }
        if (!state.error.isNullOrBlank()) {
            item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.Email,
                )
            }
        }
        item {
            SectionCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.profile_bind_email_form_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.input,
                    label = { Text(text = stringResource(R.string.profile_email_input_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.filter(Char::isDigit) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.input,
                    label = { Text(text = stringResource(R.string.profile_verification_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GhostButton(
                        text = stringResource(R.string.profile_send_verification_action),
                        onClick = { viewModel.sendVerification(email) },
                        enabled = !state.isSendingCode,
                        modifier = Modifier.weight(1f)
                    )
                    TintButton(
                        text = stringResource(R.string.profile_bind_action),
                        onClick = { viewModel.bind(email, code) },
                        enabled = !state.isSubmitting,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (state.status.isBound) {
                    Spacer(modifier = Modifier.height(12.dp))
                    GhostButton(
                        text = stringResource(R.string.profile_unbind_action),
                        onClick = viewModel::unbind,
                        enabled = !state.isSubmitting,
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.22f),
                        contentColor = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
