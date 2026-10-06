package cn.gdeiassistant.ui.profile

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Refresh
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.StatusBanner
import cn.gdeiassistant.ui.components.TintButton
import cn.gdeiassistant.ui.navigation.Routes
import kotlinx.coroutines.flow.collectLatest

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
                badge = null,
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
            SectionCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("profile.privacy.dm")
                    .clickable(role = Role.Button) { navController.navigate(Routes.SOCIAL_DM_PRIVACY) }
            ) {
                Text(
                    text = stringResource(R.string.social_dm_privacy_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.social_dm_privacy_entry_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            SectionCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("profile.privacy.blocks")
                    .clickable(role = Role.Button) { navController.navigate(Routes.SOCIAL_BLOCKS) }
            ) {
                Text(
                    text = stringResource(R.string.social_blocks_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.social_blocks_entry_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
