package cn.gdeiassistant.ui.profile

import cn.gdeiassistant.ui.theme.AppShapes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cn.gdeiassistant.R
import cn.gdeiassistant.model.CampusCredentialStatus
import cn.gdeiassistant.ui.components.GhostButton
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.StatusBanner

@Composable
internal fun CampusCredentialManagementCard(
    state: ProfileSettingsUiState,
    onToggleQuickAuth: (Boolean) -> Unit,
    onRevokeClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val status = state.campusCredentialStatus
    val quickAuthSubtitle = if (status.quickAuthEnabled) {
        stringResource(R.string.profile_settings_campus_credentials_quick_auth_enabled_subtitle)
    } else {
        stringResource(R.string.profile_settings_campus_credentials_quick_auth_disabled_subtitle)
    }
    val canRunCredentialAction = !state.isCampusCredentialLoading &&
        !state.isCampusCredentialActionRunning &&
        !state.isBackendTargetChanging
    val canToggleQuickAuth = canRunCredentialAction &&
        (status.quickAuthEnabled || (status.hasActiveConsent && status.hasSavedCredential))

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

        CredentialStatusSummary(status = status)
        Spacer(modifier = Modifier.height(16.dp))

        status.maskedCampusAccount?.takeIf(String::isNotBlank)?.let { maskedAccount ->
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

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        SettingSwitchRow(
            title = stringResource(R.string.profile_settings_campus_credentials_quick_auth_label),
            subtitle = quickAuthSubtitle,
            checked = status.quickAuthEnabled,
            enabled = canToggleQuickAuth,
            onCheckedChange = onToggleQuickAuth
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        Text(
            text = stringResource(R.string.profile_settings_campus_credentials_danger_title),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        GhostButton(
            text = stringResource(R.string.profile_settings_campus_credentials_revoke_action),
            icon = Icons.Rounded.WarningAmber,
            onClick = onRevokeClick,
            enabled = canRunCredentialAction,
            modifier = Modifier.fillMaxWidth(),
            borderColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.28f),
            contentColor = MaterialTheme.colorScheme.tertiary
        )
        Spacer(modifier = Modifier.height(10.dp))
        GhostButton(
            text = stringResource(R.string.profile_settings_campus_credentials_delete_action),
            icon = Icons.Rounded.DeleteForever,
            onClick = onDeleteClick,
            enabled = canRunCredentialAction,
            modifier = Modifier.fillMaxWidth(),
            borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.28f),
            contentColor = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
internal fun CredentialStatusSummary(status: CampusCredentialStatus) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CredentialStatusChip(
            label = stringResource(R.string.profile_settings_campus_credentials_consent_status_label),
            value = stringResource(
                if (status.hasActiveConsent) {
                    R.string.profile_settings_campus_credentials_status_authorized
                } else {
                    R.string.profile_settings_campus_credentials_status_unauthorized
                }
            ),
            active = status.hasActiveConsent,
            modifier = Modifier.weight(1f)
        )
        CredentialStatusChip(
            label = stringResource(R.string.profile_settings_campus_credentials_saved_label),
            value = stringResource(
                if (status.hasSavedCredential) {
                    R.string.profile_settings_campus_credentials_boolean_yes
                } else {
                    R.string.profile_settings_campus_credentials_boolean_no
                }
            ),
            active = status.hasSavedCredential,
            modifier = Modifier.weight(1f)
        )
        CredentialStatusChip(
            label = stringResource(R.string.profile_settings_campus_credentials_quick_auth_label),
            value = stringResource(
                if (status.quickAuthEnabled) {
                    R.string.profile_settings_campus_credentials_quick_auth_on
                } else {
                    R.string.profile_settings_campus_credentials_quick_auth_off
                }
            ),
            active = status.quickAuthEnabled,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
internal fun CredentialStatusChip(
    label: String,
    value: String,
    active: Boolean,
    modifier: Modifier = Modifier
) {
    val tint = if (active) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        modifier = modifier,
        shape = AppShapes.card,
        color = tint.copy(alpha = if (active) 0.10f else 0.06f)
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = tint
            )
        }
    }
}

@Composable
internal fun CampusCredentialConfirmationDialog(
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
