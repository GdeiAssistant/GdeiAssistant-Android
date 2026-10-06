package cn.gdeiassistant.ui.profile

import cn.gdeiassistant.ui.theme.AppShapes
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.navigation.Routes
import kotlinx.coroutines.flow.collectLatest

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
            enabled = !state.isCampusCredentialActionRunning && !state.isBackendTargetChanging,
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
            enabled = !state.isCampusCredentialActionRunning && !state.isBackendTargetChanging,
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
                enabled = !state.isCampusCredentialLoading &&
                    !state.isCampusCredentialActionRunning &&
                    !state.isBackendTargetChanging
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
                if (state.canUseDemoMode) {
                    SettingSwitchRow(
                        title = stringResource(R.string.profile_settings_mock_title),
                        subtitle = stringResource(R.string.profile_settings_mock_subtitle),
                        checked = state.isMockModeEnabled,
                        enabled = !state.isBackendTargetChanging && !state.isCampusCredentialActionRunning,
                        onCheckedChange = viewModel::setMockModeEnabled
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))
                }
                if (state.canChangeNetworkEnvironment) {
                    SettingEnvironmentRow(
                        selectedEnvironment = state.networkEnvironment,
                        baseUrl = state.environmentBaseUrl,
                        enabled = !state.isBackendTargetChanging &&
                            !state.isCampusCredentialActionRunning,
                        onEnvironmentSelected = viewModel::setNetworkEnvironment
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))
                }
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
                        shape = AppShapes.card
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
