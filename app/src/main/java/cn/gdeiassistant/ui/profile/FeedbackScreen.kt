package cn.gdeiassistant.ui.profile

import cn.gdeiassistant.ui.theme.AppShapes
import android.widget.Toast
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Feedback
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.StatusBanner
import cn.gdeiassistant.ui.components.TintButton
import kotlinx.coroutines.flow.collectLatest

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
                badge = null,
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
                    shape = AppShapes.input,
                    label = { Text(text = stringResource(R.string.profile_feedback_type_label)) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = contact,
                    onValueChange = { contact = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.input,
                    label = { Text(text = stringResource(R.string.profile_feedback_contact_label)) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.input,
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
