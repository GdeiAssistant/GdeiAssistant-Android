package cn.gdeiassistant.ui.profile

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import cn.gdeiassistant.model.UserDataExportState
import cn.gdeiassistant.ui.components.GhostButton
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.StatusBanner
import cn.gdeiassistant.ui.components.TintButton
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
