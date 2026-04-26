package cn.gdeiassistant.ui.profile

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Feedback
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import cn.gdeiassistant.model.PhoneAttribution
import cn.gdeiassistant.model.PrivacySettings
import cn.gdeiassistant.model.UserDataExportState
import cn.gdeiassistant.network.NetworkEnvironment
import cn.gdeiassistant.ui.components.BadgePill
import cn.gdeiassistant.ui.components.EmptyState
import cn.gdeiassistant.ui.components.GhostButton
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.SelectionPill
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.StatusBanner
import cn.gdeiassistant.ui.components.TintButton
import cn.gdeiassistant.ui.navigation.Routes
import kotlinx.coroutines.flow.collectLatest

@Composable
fun DownloadDataScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: DownloadDataViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is DownloadDataEvent.ShowMessage -> Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                is DownloadDataEvent.OpenDownload -> {
                    launchExternal(context, event.url)
                }
            }
        }
    }

    LazyScreen(
        title = stringResource(R.string.profile_download_data_title),
        onBack = navController::popBackStack,
        actions = {
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.schedule_refresh))
            }
        }
    ) {
        item {
            AccountIntroCard(
                badge = stringResource(
                    when (state.exportState) {
                        UserDataExportState.NOT_EXPORTED -> R.string.profile_export_idle_badge
                        UserDataExportState.EXPORTING -> R.string.profile_export_exporting_badge
                        UserDataExportState.EXPORTED -> R.string.profile_export_exported_badge
                    }
                ),
                title = stringResource(R.string.profile_download_data_title),
                subtitle = stringResource(R.string.profile_download_data_subtitle),
                body = stringResource(
                    when (state.exportState) {
                        UserDataExportState.NOT_EXPORTED -> R.string.profile_export_state_idle
                        UserDataExportState.EXPORTING -> R.string.profile_export_state_exporting
                        UserDataExportState.EXPORTED -> R.string.profile_export_state_exported
                    }
                ),
                icon = Icons.Rounded.CloudDownload,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        if (!state.error.isNullOrBlank()) {
            item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.CloudDownload,
                )
            }
        }
        item {
            SectionCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.profile_account_actions_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.profile_export_start_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TintButton(
                        text = stringResource(R.string.profile_export_start_action),
                        icon = Icons.Rounded.CloudDownload,
                        onClick = viewModel::startExport,
                        enabled = state.exportState != UserDataExportState.EXPORTED,
                        modifier = Modifier.weight(1f)
                    )
                    GhostButton(
                        text = stringResource(R.string.profile_export_download_action),
                        icon = Icons.Rounded.Security,
                        onClick = viewModel::download,
                        enabled = state.exportState == UserDataExportState.EXPORTED,
                        modifier = Modifier.weight(1f),
                        borderColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.28f),
                        contentColor = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

@Composable
fun PrivacySettingsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: PrivacySettingsViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is PrivacySettingsEvent.ShowMessage -> Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    LazyScreen(
        title = stringResource(R.string.profile_privacy_title),
        onBack = navController::popBackStack,
        actions = {
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.schedule_refresh))
            }
        }
    ) {
        item {
            AccountIntroCard(
                badge = stringResource(R.string.profile_privacy_badge),
                title = stringResource(R.string.profile_privacy_title),
                subtitle = stringResource(R.string.profile_privacy_subtitle),
                icon = Icons.Rounded.Lock,
                tint = MaterialTheme.colorScheme.secondary
            )
        }
        if (!state.error.isNullOrBlank()) {
            item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.Lock,
                )
            }
        }
        item {
            SectionCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.profile_privacy_profile_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                PrivacyToggleRow(
                    label = stringResource(R.string.profile_privacy_faculty),
                    checked = state.settings.facultyOpen,
                    onCheckedChange = { checked -> viewModel.updateSettings { it.copy(facultyOpen = checked) } }
                )
                PrivacyToggleRow(
                    label = stringResource(R.string.profile_privacy_major),
                    checked = state.settings.majorOpen,
                    onCheckedChange = { checked -> viewModel.updateSettings { it.copy(majorOpen = checked) } }
                )
                PrivacyToggleRow(
                    label = stringResource(R.string.profile_privacy_location),
                    checked = state.settings.locationOpen,
                    onCheckedChange = { checked -> viewModel.updateSettings { it.copy(locationOpen = checked) } }
                )
                PrivacyToggleRow(
                    label = stringResource(R.string.profile_privacy_hometown),
                    checked = state.settings.hometownOpen,
                    onCheckedChange = { checked -> viewModel.updateSettings { it.copy(hometownOpen = checked) } }
                )
                PrivacyToggleRow(
                    label = stringResource(R.string.profile_privacy_intro),
                    checked = state.settings.introductionOpen,
                    onCheckedChange = { checked -> viewModel.updateSettings { it.copy(introductionOpen = checked) } }
                )
                PrivacyToggleRow(
                    label = stringResource(R.string.profile_privacy_enrollment),
                    checked = state.settings.enrollmentOpen,
                    onCheckedChange = { checked -> viewModel.updateSettings { it.copy(enrollmentOpen = checked) } }
                )
                PrivacyToggleRow(
                    label = stringResource(R.string.profile_privacy_age),
                    checked = state.settings.ageOpen,
                    onCheckedChange = { checked -> viewModel.updateSettings { it.copy(ageOpen = checked) } }
                )
            }
        }
        item {
            SectionCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.profile_privacy_platform_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                PrivacyToggleRow(
                    label = stringResource(R.string.profile_privacy_cache),
                    checked = state.settings.cacheAllow,
                    onCheckedChange = { checked -> viewModel.updateSettings { it.copy(cacheAllow = checked) } }
                )
                PrivacyToggleRow(
                    label = stringResource(R.string.profile_privacy_robots),
                    checked = state.settings.robotsIndexAllow,
                    onCheckedChange = { checked -> viewModel.updateSettings { it.copy(robotsIndexAllow = checked) } }
                )
            }
        }
        item {
            TintButton(
                text = stringResource(R.string.profile_privacy_save_action),
                onClick = viewModel::save,
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun LoginRecordsScreen(navController: NavHostController) {
    val viewModel: LoginRecordsViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyScreen(
        title = stringResource(R.string.profile_login_records_title),
        onBack = navController::popBackStack,
        actions = {
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.schedule_refresh))
            }
        }
    ) {
        when {
            state.isLoading && state.items.isEmpty() -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    EmptyState(
                        icon = Icons.Rounded.History,
                        message = stringResource(R.string.profile_login_records_loading),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            !state.error.isNullOrBlank() && state.items.isEmpty() -> item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.History,
                )
            }
            state.items.isEmpty() -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    EmptyState(
                        icon = Icons.Rounded.History,
                        message = stringResource(R.string.profile_login_records_empty),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            else -> {
                items(state.items, key = { it.id }) { record ->
                    SectionCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = record.timeText,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        LoginRecordLine(
                            label = stringResource(R.string.profile_login_ip),
                            value = record.ip
                        )
                        LoginRecordLine(
                            label = stringResource(R.string.profile_login_area),
                            value = record.area
                        )
                        LoginRecordLine(
                            label = stringResource(R.string.profile_login_device),
                            value = record.device
                        )
                        LoginRecordLine(
                            label = stringResource(R.string.profile_login_status),
                            value = record.statusText
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BindPhoneScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: BindPhoneViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var phone by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }
    var showAreaCodeSheet by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is BindPhoneEvent.ShowMessage -> Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    LazyScreen(
        title = stringResource(R.string.profile_bind_phone_title),
        onBack = navController::popBackStack,
        actions = {
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.schedule_refresh))
            }
        }
    ) {
        item {
            BindingStatusCard(
                title = stringResource(R.string.profile_phone_title),
                value = state.status.maskedValue,
                note = state.status.note,
                icon = Icons.Rounded.PhoneAndroid,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        if (!state.error.isNullOrBlank()) {
            item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.PhoneAndroid,
                )
            }
        }
        item {
            SectionCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.profile_bind_phone_form_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = state.attributions.firstOrNull { it.code == state.selectedAttributionCode }?.displayText.orEmpty(),
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAreaCodeSheet = true },
                    readOnly = true,
                    shape = RoundedCornerShape(16.dp),
                    label = { Text(text = stringResource(R.string.profile_phone_area_label)) }
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it.filter(Char::isDigit) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    label = { Text(text = stringResource(R.string.profile_phone_input_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.filter(Char::isDigit) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    label = { Text(text = stringResource(R.string.profile_verification_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GhostButton(
                        text = stringResource(R.string.profile_send_verification_action),
                        onClick = { viewModel.sendVerification(phone) },
                        enabled = !state.isSendingCode,
                        modifier = Modifier.weight(1f)
                    )
                    TintButton(
                        text = stringResource(R.string.profile_bind_action),
                        onClick = { viewModel.bind(phone, code) },
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

    if (showAreaCodeSheet) {
        BindPhoneAreaCodeSheet(
            attributions = state.attributions,
            selectedCode = state.selectedAttributionCode,
            onDismiss = { showAreaCodeSheet = false },
            onSelect = { attribution ->
                viewModel.selectAttribution(attribution.code)
                showAreaCodeSheet = false
            }
        )
    }
}

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
                    shape = RoundedCornerShape(16.dp),
                    label = { Text(text = stringResource(R.string.profile_email_input_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.filter(Char::isDigit) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
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

@Composable
fun FeedbackScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: FeedbackViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var content by rememberSaveable { mutableStateOf("") }
    var contact by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is FeedbackEvent.ShowMessage -> Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                FeedbackEvent.Submitted -> {
                    content = ""
                    contact = ""
                    type = ""
                }
            }
        }
    }

    LazyScreen(
        title = stringResource(R.string.profile_feedback_title),
        onBack = navController::popBackStack
    ) {
        item {
            AccountIntroCard(
                badge = stringResource(R.string.profile_feedback_badge),
                title = stringResource(R.string.profile_feedback_title),
                subtitle = stringResource(R.string.profile_feedback_subtitle),
                icon = Icons.Rounded.Feedback,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        if (!state.error.isNullOrBlank()) {
            item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.Feedback,
                )
            }
        }
        item {
            SectionCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    label = { Text(text = stringResource(R.string.profile_feedback_type_label)) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = contact,
                    onValueChange = { contact = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    label = { Text(text = stringResource(R.string.profile_feedback_contact_label)) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    minLines = 5,
                    label = { Text(text = stringResource(R.string.profile_feedback_content_label)) }
                )
                Spacer(modifier = Modifier.height(14.dp))
                TintButton(
                    text = stringResource(R.string.profile_feedback_submit_action),
                    onClick = { viewModel.submit(content = content, contact = contact, type = type) },
                    enabled = !state.isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun DeleteAccountScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: DeleteAccountViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var password by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is DeleteAccountEvent.ShowMessage -> Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                DeleteAccountEvent.Deleted -> {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    LazyScreen(
        title = stringResource(R.string.profile_delete_title),
        onBack = navController::popBackStack
    ) {
        item {
            StatusBanner(
                title = stringResource(R.string.profile_delete_warning_title),
                body = stringResource(R.string.profile_delete_warning_body),
                icon = Icons.Rounded.WarningAmber,
            )
        }
        if (!state.error.isNullOrBlank()) {
            item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.DeleteForever,
                )
            }
        }
        item {
            SectionCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    label = { Text(text = stringResource(R.string.profile_delete_password_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(14.dp))
                GhostButton(
                    text = stringResource(R.string.profile_delete_confirm_action),
                    onClick = { viewModel.submit(password) },
                    enabled = !state.isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.22f),
                    contentColor = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun ProfileSettingsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: ProfileSettingsViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showRevokeConfirmation by rememberSaveable { mutableStateOf(false) }
    var showDeleteConfirmation by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ProfileSettingsEvent.ShowMessage -> Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    if (showRevokeConfirmation) {
        CampusCredentialConfirmationDialog(
            title = stringResource(R.string.profile_settings_campus_credentials_revoke_title),
            message = stringResource(R.string.profile_settings_campus_credentials_revoke_confirmation),
            confirmText = stringResource(R.string.profile_settings_campus_credentials_revoke_action),
            enabled = !state.isCampusCredentialActionRunning,
            onDismiss = { showRevokeConfirmation = false },
            onConfirm = {
                showRevokeConfirmation = false
                viewModel.revokeCampusCredentialConsent()
            }
        )
    }

    if (showDeleteConfirmation) {
        CampusCredentialConfirmationDialog(
            title = stringResource(R.string.profile_settings_campus_credentials_delete_title),
            message = stringResource(R.string.profile_settings_campus_credentials_delete_confirmation),
            confirmText = stringResource(R.string.profile_settings_campus_credentials_delete_action),
            enabled = !state.isCampusCredentialActionRunning,
            onDismiss = { showDeleteConfirmation = false },
            onConfirm = {
                showDeleteConfirmation = false
                viewModel.deleteCampusCredential()
            }
        )
    }

    LazyScreen(
        title = stringResource(R.string.profile_settings_title),
        onBack = navController::popBackStack,
        actions = {
            IconButton(
                onClick = viewModel::refreshCampusCredentialStatus,
                enabled = !state.isCampusCredentialLoading && !state.isCampusCredentialActionRunning
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.schedule_refresh))
            }
        }
    ) {
        item {
            SectionCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.profile_settings_general_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                SettingSwitchRow(
                    title = stringResource(R.string.profile_settings_mock_title),
                    subtitle = stringResource(R.string.profile_settings_mock_subtitle),
                    checked = state.isMockModeEnabled,
                    onCheckedChange = viewModel::setMockModeEnabled
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))
                SettingEnvironmentRow(
                    selectedEnvironment = state.networkEnvironment,
                    baseUrl = state.environmentBaseUrl,
                    enabled = state.canChangeNetworkEnvironment,
                    onEnvironmentSelected = viewModel::setNetworkEnvironment
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))
                SettingInfoRow(
                    title = stringResource(R.string.profile_settings_version_title),
                    value = state.appVersion
                )
            }
        }
        item {
            CampusCredentialManagementCard(
                state = state,
                onToggleQuickAuth = viewModel::setQuickAuthEnabled,
                onRevokeClick = { showRevokeConfirmation = true },
                onDeleteClick = { showDeleteConfirmation = true }
            )
        }
        item {
            SectionCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate(Routes.ABOUT) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.about_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.feature_about_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CampusCredentialManagementCard(
    state: ProfileSettingsUiState,
    onToggleQuickAuth: (Boolean) -> Unit,
    onRevokeClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val status = state.campusCredentialStatus
    val quickAuthActionText = if (status.quickAuthEnabled) {
        stringResource(R.string.profile_settings_campus_credentials_quick_auth_disable_action)
    } else {
        stringResource(R.string.profile_settings_campus_credentials_quick_auth_enable_action)
    }
    val quickAuthSubtitle = if (status.quickAuthEnabled) {
        stringResource(R.string.profile_settings_campus_credentials_quick_auth_enabled_subtitle)
    } else {
        stringResource(R.string.profile_settings_campus_credentials_quick_auth_disabled_subtitle)
    }

    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.profile_settings_campus_credentials_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.profile_settings_campus_credentials_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (state.isCampusCredentialLoading) {
            Text(
                text = stringResource(R.string.profile_settings_campus_credentials_loading),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (!state.campusCredentialError.isNullOrBlank()) {
            StatusBanner(
                title = stringResource(R.string.load_failed),
                body = state.campusCredentialError.orEmpty(),
                icon = Icons.Rounded.Security,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        SettingInfoRow(
            title = stringResource(R.string.profile_settings_campus_credentials_consent_status_label),
            value = stringResource(
                if (status.hasActiveConsent) {
                    R.string.profile_settings_campus_credentials_status_authorized
                } else {
                    R.string.profile_settings_campus_credentials_status_unauthorized
                }
            )
        )
        Spacer(modifier = Modifier.height(10.dp))
        SettingInfoRow(
            title = stringResource(R.string.profile_settings_campus_credentials_saved_label),
            value = stringResource(
                if (status.hasSavedCredential) {
                    R.string.profile_settings_campus_credentials_boolean_yes
                } else {
                    R.string.profile_settings_campus_credentials_boolean_no
                }
            )
        )
        Spacer(modifier = Modifier.height(10.dp))
        SettingInfoRow(
            title = stringResource(R.string.profile_settings_campus_credentials_quick_auth_label),
            value = stringResource(
                if (status.quickAuthEnabled) {
                    R.string.profile_settings_campus_credentials_quick_auth_on
                } else {
                    R.string.profile_settings_campus_credentials_quick_auth_off
                }
            )
        )
        status.maskedCampusAccount?.takeIf(String::isNotBlank)?.let { maskedAccount ->
            Spacer(modifier = Modifier.height(10.dp))
            SettingInfoRow(
                title = stringResource(R.string.profile_settings_campus_credentials_account_label),
                value = maskedAccount
            )
        }
        status.consentedAt?.takeIf(String::isNotBlank)?.let { consentedAt ->
            Spacer(modifier = Modifier.height(10.dp))
            SettingInfoRow(
                title = stringResource(R.string.profile_settings_campus_credentials_consented_at_label),
                value = consentedAt
            )
        }
        status.revokedAt?.takeIf(String::isNotBlank)?.let { revokedAt ->
            Spacer(modifier = Modifier.height(10.dp))
            SettingInfoRow(
                title = stringResource(R.string.profile_settings_campus_credentials_revoked_at_label),
                value = revokedAt
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        TintButton(
            text = quickAuthActionText,
            onClick = { onToggleQuickAuth(!status.quickAuthEnabled) },
            enabled = !state.isCampusCredentialLoading && !state.isCampusCredentialActionRunning,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = quickAuthSubtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        GhostButton(
            text = stringResource(R.string.profile_settings_campus_credentials_revoke_action),
            icon = Icons.Rounded.WarningAmber,
            onClick = onRevokeClick,
            enabled = !state.isCampusCredentialLoading && !state.isCampusCredentialActionRunning,
            modifier = Modifier.fillMaxWidth(),
            borderColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.28f),
            contentColor = MaterialTheme.colorScheme.tertiary
        )
        Spacer(modifier = Modifier.height(10.dp))
        GhostButton(
            text = stringResource(R.string.profile_settings_campus_credentials_delete_action),
            icon = Icons.Rounded.DeleteForever,
            onClick = onDeleteClick,
            enabled = !state.isCampusCredentialLoading && !state.isCampusCredentialActionRunning,
            modifier = Modifier.fillMaxWidth(),
            borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.28f),
            contentColor = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun CampusCredentialConfirmationDialog(
    title: String,
    message: String,
    confirmText: String,
    enabled: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = enabled) {
                Text(text = confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = enabled) {
                Text(text = stringResource(R.string.profile_info_cancel))
            }
        }
    )
}

@Composable
private fun AccountIntroCard(
    badge: String,
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    body: String? = null
) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        BadgePill(text = badge, tint = tint)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                color = tint.copy(alpha = 0.12f),
                shape = RoundedCornerShape(22.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.padding(14.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                body?.takeIf(String::isNotBlank)?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PrivacyToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun LoginRecordLine(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
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
            color = MaterialTheme.colorScheme.onSurface
        )
    }
    Spacer(modifier = Modifier.height(6.dp))
}

@Composable
private fun BindingStatusCard(
    title: String,
    value: String,
    note: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: androidx.compose.ui.graphics.Color
) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        BadgePill(text = title, tint = tint)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                color = tint.copy(alpha = 0.12f),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = note,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BindPhoneAreaCodeSheet(
    attributions: List<PhoneAttribution>,
    selectedCode: Int,
    onDismiss: () -> Unit,
    onSelect: (PhoneAttribution) -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val filteredAttributions = remember(attributions, searchQuery) {
        val normalizedQuery = searchQuery.trim()
        if (normalizedQuery.isBlank()) {
            attributions
        } else {
            attributions.filter { attribution ->
                attribution.displayName().contains(normalizedQuery, ignoreCase = true) ||
                    attribution.name.contains(normalizedQuery, ignoreCase = true) ||
                    attribution.code.toString().contains(normalizedQuery.removePrefix("+"))
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.profile_phone_area_picker_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                label = { Text(text = stringResource(R.string.profile_phone_area_search_label)) },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (filteredAttributions.isEmpty()) {
                Text(
                    text = stringResource(R.string.profile_phone_area_search_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredAttributions, key = { it.code }) { attribution ->
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = attribution.displayText,
                                    fontWeight = if (attribution.code == selectedCode) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            supportingContent = if (attribution.code == selectedCode) {
                                { Text(text = stringResource(R.string.profile_selected_badge)) }
                            } else {
                                null
                            },
                            modifier = Modifier.clickable { onSelect(attribution) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingEnvironmentRow(
    selectedEnvironment: NetworkEnvironment,
    baseUrl: String,
    enabled: Boolean,
    onEnvironmentSelected: (NetworkEnvironment) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.profile_settings_environment_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = if (enabled) {
                stringResource(R.string.profile_settings_environment_subtitle)
            } else {
                stringResource(R.string.profile_settings_environment_release_subtitle)
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            NetworkEnvironment.entries.forEach { environment ->
                SelectionPill(
                    text = environment.storageValue.uppercase(),
                    selected = environment == selectedEnvironment,
                    onClick = {
                        if (enabled) {
                            onEnvironmentSelected(environment)
                        }
                    }
                )
            }
        }
        SettingInfoRow(
            title = stringResource(R.string.profile_settings_environment_endpoint_title),
            value = baseUrl
        )
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingInfoRow(
    title: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun launchExternal(context: android.content.Context, url: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }.onFailure {
        Toast.makeText(context, context.getString(R.string.about_open_failed), Toast.LENGTH_LONG).show()
    }
}
