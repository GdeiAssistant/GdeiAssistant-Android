package cn.gdeiassistant.ui.about

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cn.gdeiassistant.BuildConfig
import cn.gdeiassistant.R
import cn.gdeiassistant.model.CheckUpgradeResult
import cn.gdeiassistant.ui.components.*
import cn.gdeiassistant.ui.theme.AppShapes
import cn.gdeiassistant.ui.util.asString
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AboutScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: AboutViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    AboutContent(
        state = state,
        onBack = navController::popBackStack,
        onCheckUpdate = viewModel::checkForUpdate,
        onMockModeChange = viewModel::setMockModeEnabled,
        onOpenSite = {
            launchExternal(context, context.getString(R.string.about_official_site_url))
        },
        onOpenFeedback = {
            launchExternal(context, "mailto:${context.getString(R.string.about_support_email)}")
        },
        onOpenDownload = { url ->
            launchExternal(context, url)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutContent(
    state: AboutUiState,
    onBack: () -> Unit,
    onCheckUpdate: () -> Unit,
    onMockModeChange: (Boolean) -> Unit,
    onOpenSite: () -> Unit,
    onOpenFeedback: () -> Unit,
    onOpenDownload: (String) -> Unit
) {
    val statusText = state.status?.asString()
    val errorText = state.error?.asString()
    val versionName = state.currentVersionName.ifBlank { "—" }
    val sourceText = if (state.isMockModeEnabled) {
        stringResource(R.string.about_data_source_mock)
    } else {
        stringResource(R.string.about_data_source_live)
    }

    LazyScreen(
        title = stringResource(R.string.about_title),
        onBack = onBack
    ) {
        item {
            AboutOverviewCard(
                versionName = versionName,
                versionCode = state.currentVersionCode,
                sourceText = sourceText,
                onOpenSite = onOpenSite,
                onOpenFeedback = onOpenFeedback
            )
        }
        item {
            UpdateCard(
                state = state,
                statusText = statusText,
                errorText = errorText,
                onCheckUpdate = onCheckUpdate,
                onOpenDownload = onOpenDownload
            )
        }
        item {
            DeveloperSettingsCard(
                isMockModeEnabled = state.isMockModeEnabled,
                sourceText = sourceText,
                onMockModeChange = onMockModeChange
            )
        }
        item {
            SupportCard(
                onOpenSite = onOpenSite,
                onOpenFeedback = onOpenFeedback
            )
        }
    }
}

@Composable
private fun AboutOverviewCard(
    versionName: String,
    versionCode: Long,
    sourceText: String,
    onOpenSite: () -> Unit,
    onOpenFeedback: () -> Unit
) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        BadgePill(text = stringResource(R.string.app_name))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.about_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AboutMetricCard(
                label = stringResource(R.string.about_metric_version),
                value = versionName,
                modifier = Modifier.weight(1f)
            )
            AboutMetricCard(
                label = stringResource(R.string.about_metric_source),
                value = sourceText,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        AboutMetricCard(
            label = stringResource(R.string.about_metric_build),
            value = versionCode.toString(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GhostButton(
                text = stringResource(R.string.about_open_official_site),
                onClick = onOpenSite,
                icon = Icons.Rounded.OpenInBrowser,
                modifier = Modifier.weight(1f)
            )
            TintButton(
                text = stringResource(R.string.about_feedback),
                onClick = onOpenFeedback,
                icon = Icons.AutoMirrored.Rounded.Send,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun UpdateCard(
    state: AboutUiState,
    statusText: String?,
    errorText: String?,
    onCheckUpdate: () -> Unit,
    onOpenDownload: (String) -> Unit
) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.about_check_update),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            GhostButton(
                text = if (state.isCheckingUpdate) stringResource(R.string.about_checking) else stringResource(R.string.about_check_update),
                onClick = { if (!state.isCheckingUpdate) onCheckUpdate() },
                icon = if (state.isCheckingUpdate) null else Icons.Rounded.Refresh
            )
        }

        when {
            state.isCheckingUpdate -> {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            !errorText.isNullOrBlank() -> {
                Spacer(modifier = Modifier.height(12.dp))
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = errorText,
                    icon = Icons.Rounded.Info
                )
            }
            !statusText.isNullOrBlank() -> {
                Spacer(modifier = Modifier.height(12.dp))
                StatusBanner(
                    title = statusText,
                    body = state.latestVersion?.versionInfo ?: stringResource(R.string.about_update_notes),
                    icon = Icons.Rounded.Info,
                    surface = MaterialTheme.colorScheme.primaryContainer,
                    border = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        state.latestVersion?.let { update ->
            Spacer(modifier = Modifier.height(12.dp))
            UpdateSnapshot(update = update, updateAvailable = state.updateAvailable)
            if (state.updateAvailable && state.hasDownloadUrl) {
                Spacer(modifier = Modifier.height(12.dp))
                TintButton(
                    text = stringResource(R.string.about_download_update),
                    onClick = { onOpenDownload(state.latestVersion?.downloadURL.orEmpty()) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun UpdateSnapshot(
    update: CheckUpgradeResult,
    updateAvailable: Boolean
) {
    val snapshotScrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (updateAvailable) {
                    stringResource(R.string.about_update_available)
                } else {
                    stringResource(R.string.about_latest_version)
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.horizontalScroll(snapshotScrollState),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BadgePill(
                    text = stringResource(R.string.about_version, update.versionCodeName ?: "—"),
                    tint = MaterialTheme.colorScheme.primary
                )
                update.fileSize?.takeIf { it.isNotBlank() }?.let { size ->
                    BadgePill(
                        text = stringResource(R.string.about_file_size, size),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            update.versionInfo?.takeIf { it.isNotBlank() }?.let { info ->
                Text(
                    text = info,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DeveloperSettingsCard(
    isMockModeEnabled: Boolean,
    sourceText: String,
    onMockModeChange: (Boolean) -> Unit
) {
    SectionCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.about_developer_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            BadgePill(
                text = sourceText,
                tint = if (isMockModeEnabled) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.about_mock_mode_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.about_mock_mode_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Switch(
                    checked = isMockModeEnabled,
                    onCheckedChange = onMockModeChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.about_mock_mode_restart_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SupportCard(
    onOpenSite: () -> Unit,
    onOpenFeedback: () -> Unit
) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.about_backend_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        SupportRow(
            icon = Icons.Rounded.OpenInBrowser,
            label = stringResource(R.string.about_open_official_site),
            value = stringResource(R.string.about_official_site_url),
            onClick = onOpenSite
        )
        HorizontalDivider(
            modifier = Modifier.padding(start = 52.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
        SupportRow(
            icon = Icons.AutoMirrored.Rounded.Send,
            label = stringResource(R.string.about_feedback),
            value = stringResource(R.string.about_support_email),
            onClick = onOpenFeedback
        )
        HorizontalDivider(
            modifier = Modifier.padding(start = 52.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun AboutMetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SupportRow(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), AppShapes.small),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label.trimEnd('：'),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (onClick != null) {
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun launchExternal(context: Context, target: String) {
    runCatching {
        val uri = Uri.parse(target)
        val action = if (target.startsWith("mailto:")) Intent.ACTION_SENDTO else Intent.ACTION_VIEW
        context.startActivity(Intent(action, uri))
    }.onFailure {
        Toast.makeText(context, context.getString(R.string.about_open_failed), Toast.LENGTH_LONG).show()
    }
}
