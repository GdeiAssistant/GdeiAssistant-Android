package cn.gdeiassistant.ui.profile

import cn.gdeiassistant.ui.theme.AppShapes
import android.widget.Toast
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.WarningAmber
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
import cn.gdeiassistant.ui.navigation.Routes
import kotlinx.coroutines.flow.collectLatest

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
                    shape = AppShapes.input,
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
