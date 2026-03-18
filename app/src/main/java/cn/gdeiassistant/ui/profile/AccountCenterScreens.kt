package cn.gdeiassistant.ui.profile

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudDownload
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
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import cn.gdeiassistant.ui.components.EmptyState
import cn.gdeiassistant.ui.components.ActionTile
import cn.gdeiassistant.ui.components.BadgePill
import cn.gdeiassistant.ui.components.HeroCard
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.StatusBanner
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
                    navController.navigate(
                        Routes.webView(
                            title = context.getString(R.string.profile_download_data_title),
                            url = event.url,
                            allowJavaScript = true
                        )
                    )
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
            HeroCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                BadgePill(
                    text = stringResource(
                        when (state.exportState) {
                            UserDataExportState.NOT_EXPORTED -> R.string.profile_export_idle_badge
                            UserDataExportState.EXPORTING -> R.string.profile_export_exporting_badge
                            UserDataExportState.EXPORTED -> R.string.profile_export_exported_badge
                        }
                    ),
                    onGradient = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.profile_download_data_subtitle),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color.White
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.92f)
                )
            }
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
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionTile(
                    title = stringResource(R.string.profile_export_start_action),
                    subtitle = stringResource(R.string.profile_export_start_hint),
                    icon = Icons.Rounded.CloudDownload,
                    onClick = viewModel::startExport,
                    tint = MaterialTheme.colorScheme.primary,
                    emphasized = state.exportState != UserDataExportState.EXPORTED,
                    modifier = Modifier.weight(1f)
                )
                ActionTile(
                    title = stringResource(R.string.profile_export_download_action),
                    subtitle = stringResource(R.string.profile_export_download_hint),
                    icon = Icons.Rounded.Security,
                    onClick = viewModel::download,
                    tint = MaterialTheme.colorScheme.tertiary,
                    emphasized = state.exportState == UserDataExportState.EXPORTED,
                    modifier = Modifier.weight(1f)
                )
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
            HeroCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                BadgePill(text = stringResource(R.string.profile_privacy_badge), onGradient = true)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.profile_privacy_subtitle),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color.White
                )
            }
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
            Button(
                onClick = viewModel::save,
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text(text = stringResource(R.string.profile_privacy_save_action))
            }
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
    var expanded by rememberSaveable { mutableStateOf(false) }

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
                AttributionDropdown(
                    expanded = expanded,
                    selected = state.attributions.firstOrNull { it.code == state.selectedAttributionCode },
                    options = state.attributions,
                    onExpandedChange = { expanded = it },
                    onSelect = {
                        expanded = false
                        viewModel.selectAttribution(it.code)
                    }
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
                    Button(
                        onClick = { viewModel.sendVerification(phone) },
                        enabled = !state.isSendingCode,
                        modifier = Modifier.weight(1f).height(52.dp)
                    ) {
                        Text(text = stringResource(R.string.profile_send_verification_action))
                    }
                    Button(
                        onClick = { viewModel.bind(phone, code) },
                        enabled = !state.isSubmitting,
                        modifier = Modifier.weight(1f).height(52.dp)
                    ) {
                        Text(text = stringResource(R.string.profile_bind_action))
                    }
                }
                if (state.status.isBound) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = viewModel::unbind,
                        enabled = !state.isSubmitting,
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Text(text = stringResource(R.string.profile_unbind_action))
                    }
                }
            }
        }
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
                    Button(
                        onClick = { viewModel.sendVerification(email) },
                        enabled = !state.isSendingCode,
                        modifier = Modifier.weight(1f).height(52.dp)
                    ) {
                        Text(text = stringResource(R.string.profile_send_verification_action))
                    }
                    Button(
                        onClick = { viewModel.bind(email, code) },
                        enabled = !state.isSubmitting,
                        modifier = Modifier.weight(1f).height(52.dp)
                    ) {
                        Text(text = stringResource(R.string.profile_bind_action))
                    }
                }
                if (state.status.isBound) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = viewModel::unbind,
                        enabled = !state.isSubmitting,
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Text(text = stringResource(R.string.profile_unbind_action))
                    }
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
            HeroCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                BadgePill(text = stringResource(R.string.profile_feedback_badge), onGradient = true)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.profile_feedback_subtitle),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color.White
                )
            }
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
                Button(
                    onClick = { viewModel.submit(content = content, contact = contact, type = type) },
                    enabled = !state.isSubmitting,
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text(text = stringResource(R.string.profile_feedback_submit_action))
                }
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
                Button(
                    onClick = { viewModel.submit(password) },
                    enabled = !state.isSubmitting,
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text(text = stringResource(R.string.profile_delete_confirm_action))
                }
            }
        }
    }
}

@Composable
fun ProfileSettingsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: ProfileSettingsViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ProfileSettingsEvent.ShowMessage -> Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    LazyScreen(
        title = stringResource(R.string.profile_settings_title),
        onBack = navController::popBackStack
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
                SettingInfoRow(
                    title = stringResource(R.string.profile_settings_version_title),
                    value = state.appVersion
                )
            }
        }
        item {
            ActionTile(
                title = stringResource(R.string.about_title),
                subtitle = stringResource(R.string.feature_about_subtitle),
                icon = Icons.Rounded.Settings,
                onClick = { navController.navigate(Routes.ABOUT) },
                tint = MaterialTheme.colorScheme.secondary,
                emphasized = true,
                modifier = Modifier.fillMaxWidth()
            )
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
    HeroCard(
        modifier = Modifier.fillMaxWidth(),
        start = tint
    ) {
        BadgePill(text = title, onGradient = true)
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White)
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.ui.graphics.Color.White
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = note,
            style = MaterialTheme.typography.bodyLarge,
            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.92f)
        )
    }
}

@Composable
private fun AttributionDropdown(
    expanded: Boolean,
    selected: PhoneAttribution?,
    options: List<PhoneAttribution>,
    onExpandedChange: (Boolean) -> Unit,
    onSelect: (PhoneAttribution) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selected?.displayText.orEmpty(),
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandedChange(true) },
            readOnly = true,
            shape = RoundedCornerShape(16.dp),
            label = { Text(text = stringResource(R.string.profile_phone_area_label)) }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            options.forEach { attribution ->
                DropdownMenuItem(
                    text = { Text(text = attribution.displayText) },
                    onClick = { onSelect(attribution) }
                )
            }
        }
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
