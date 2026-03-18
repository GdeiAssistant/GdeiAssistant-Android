package cn.gdeiassistant.ui.secret

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import cn.gdeiassistant.model.SecretDraftMode
import cn.gdeiassistant.ui.components.BadgePill
import cn.gdeiassistant.ui.components.HeroCard
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.navigation.Routes
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SecretPublishScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: SecretPublishViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var content by rememberSaveable { mutableStateOf("") }
    var themeId by rememberSaveable { mutableStateOf(1) }
    var timerEnabled by rememberSaveable { mutableStateOf(false) }
    var modeRaw by rememberSaveable { mutableStateOf(SecretDraftMode.TEXT.name) }
    var voiceUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var voiceName by rememberSaveable { mutableStateOf("") }
    val mode = SecretDraftMode.valueOf(modeRaw)

    val voicePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        voiceUri = uri
        voiceName = uri?.let { queryDisplayName(context, it) }.orEmpty()
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is SecretPublishEvent.ShowMessage -> Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                SecretPublishEvent.Submitted -> {
                    navController.previousBackStackEntry?.savedStateHandle?.set(Routes.SECRET_REFRESH_FLAG, true)
                    navController.popBackStack()
                }
            }
        }
    }

    LazyScreen(
        title = stringResource(R.string.secret_publish_title),
        onBack = navController::popBackStack
    ) {
        item {
            HeroCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                BadgePill(text = stringResource(R.string.secret_publish_title), onGradient = true)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.secret_publish_subtitle),
                    style = MaterialTheme.typography.titleMedium,
                    color = androidx.compose.ui.graphics.Color.White
                )
            }
        }
        item {
            SectionCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.secret_publish_theme_label),
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    (1..4).forEach { id ->
                        FilterChip(
                            selected = themeId == id,
                            onClick = { themeId = id },
                            label = { Text(text = secretThemeLabel(id)) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.secret_publish_mode_text),
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChip(
                        selected = mode == SecretDraftMode.TEXT,
                        onClick = { modeRaw = SecretDraftMode.TEXT.name },
                        label = { Text(text = stringResource(R.string.secret_publish_mode_text)) }
                    )
                    FilterChip(
                        selected = mode == SecretDraftMode.VOICE,
                        onClick = { modeRaw = SecretDraftMode.VOICE.name },
                        label = { Text(text = stringResource(R.string.secret_publish_mode_voice)) }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (mode == SecretDraftMode.TEXT) {
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = stringResource(R.string.secret_publish_content_label)) },
                        minLines = 4
                    )
                } else {
                    Button(
                        onClick = { voicePicker.launch(arrayOf("audio/*")) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = stringResource(R.string.secret_publish_pick_voice))
                    }
                    if (voiceName.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = stringResource(R.string.secret_publish_voice_selected, voiceName),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.secret_publish_timer_label),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = timerEnabled,
                        onCheckedChange = { timerEnabled = it }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        viewModel.submit(
                            content = content,
                            themeId = themeId,
                            timerEnabled = timerEnabled,
                            mode = mode,
                            voiceUri = voiceUri
                        )
                    },
                    enabled = !state.isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.secret_publish_submit_action))
                }
            }
        }
    }
}

@Composable
private fun secretThemeLabel(themeId: Int): String {
    return when (themeId) {
        1 -> stringResource(R.string.secret_publish_theme_1)
        2 -> stringResource(R.string.secret_publish_theme_2)
        3 -> stringResource(R.string.secret_publish_theme_3)
        else -> stringResource(R.string.secret_publish_theme_4)
    }
}

private fun queryDisplayName(context: Context, uri: Uri): String {
    val cursor = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
    cursor?.use {
        val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && it.moveToFirst()) {
            return it.getString(index).orEmpty()
        }
    }
    return uri.lastPathSegment.orEmpty()
}
