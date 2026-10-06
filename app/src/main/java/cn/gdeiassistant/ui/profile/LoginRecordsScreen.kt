package cn.gdeiassistant.ui.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import cn.gdeiassistant.model.AppLocaleSupport
import cn.gdeiassistant.model.ProfileLocationCatalog
import cn.gdeiassistant.ui.components.EmptyState
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.StatusBanner

@Composable
fun LoginRecordsScreen(navController: NavHostController) {
    val viewModel: LoginRecordsViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val locale = AppLocaleSupport.normalizeLocale(LocalConfiguration.current.locales[0].toLanguageTag())

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
                            value = ProfileLocationCatalog.localizeIpArea(record.area, locale)
                        )
                        LoginRecordLine(
                            label = stringResource(R.string.profile_login_device),
                            value = record.device
                        )
                        LoginRecordLine(
                            label = stringResource(R.string.profile_login_status),
                            value = stringResource(R.string.profile_login_status_success)
                        )
                    }
                }
            }
        }
    }
}
