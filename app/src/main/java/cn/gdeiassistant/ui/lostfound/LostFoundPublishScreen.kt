package cn.gdeiassistant.ui.lostfound

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import cn.gdeiassistant.model.LostFoundType
import cn.gdeiassistant.ui.components.BadgePill
import cn.gdeiassistant.ui.components.GhostButton
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.SelectionPill
import cn.gdeiassistant.ui.components.TintButton
import cn.gdeiassistant.ui.navigation.Routes
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LostFoundPublishScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: LostFoundPublishViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var qq by rememberSaveable { mutableStateOf("") }
    var wechat by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var selectedType by rememberSaveable { mutableStateOf(LostFoundType.LOST) }
    var selectedItemTypeId by rememberSaveable { mutableStateOf(0) }
    val selectedImages = remember { mutableStateListOf<Uri>() }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(4)
    ) { uris ->
        selectedImages.clear()
        selectedImages.addAll(uris)
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is LostFoundPublishEvent.ShowMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
                LostFoundPublishEvent.Submitted -> {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(Routes.LOST_FOUND_REFRESH_FLAG, true)
                    navController.popBackStack()
                }
            }
        }
    }

    LazyScreen(
        title = stringResource(R.string.lost_found_publish_title),
        onBack = navController::popBackStack
    ) {
        item {
            SectionCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            ) {
                BadgePill(text = stringResource(R.string.lost_found_publish_title))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.lost_found_publish_subtitle),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
        item {
            SectionCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.lost_found_publish_mode_label),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LostFoundType.entries.forEach { type ->
                        SelectionPill(
                            text = when (type) {
                                LostFoundType.LOST -> stringResource(R.string.lost_found_filter_lost)
                                LostFoundType.FOUND -> stringResource(R.string.lost_found_filter_found)
                            },
                            selected = selectedType == type,
                            onClick = { selectedType = type }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = stringResource(R.string.lost_found_publish_title_label)) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    label = { Text(text = stringResource(R.string.lost_found_publish_description_label)) }
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = stringResource(R.string.lost_found_publish_location_label)) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.lost_found_publish_item_type_label),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    viewModel.itemTypeOptions.forEach { option ->
                        SelectionPill(
                            text = option.title,
                            selected = selectedItemTypeId == option.id,
                            onClick = { selectedItemTypeId = option.id }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = qq,
                    onValueChange = { qq = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = stringResource(R.string.lost_found_publish_qq_label)) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = wechat,
                    onValueChange = { wechat = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = stringResource(R.string.lost_found_publish_wechat_label)) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = stringResource(R.string.lost_found_publish_phone_label)) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GhostButton(
                        text = stringResource(R.string.lost_found_publish_pick_images),
                        icon = Icons.Rounded.PhotoLibrary,
                        onClick = {
                            imagePicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        enabled = !state.isSubmitting,
                        modifier = Modifier.weight(1f)
                    )
                    TintButton(
                        text = if (state.isSubmitting) {
                            stringResource(R.string.lost_found_publish_submitting)
                        } else {
                            stringResource(R.string.lost_found_publish_submit)
                        },
                        onClick = {
                            viewModel.submit(
                                title = title,
                                type = selectedType,
                                itemTypeId = selectedItemTypeId,
                                description = description,
                                location = location,
                                qq = qq,
                                wechat = wechat,
                                phone = phone,
                                images = selectedImages.toList()
                            )
                        },
                        enabled = !state.isSubmitting,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                AnimatedContent(
                    targetState = selectedImages.toList(),
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "lost_found_publish_images"
                ) { images ->
                    androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.lost_found_publish_selected_count, images.size),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        images.forEach { uri ->
                            Text(
                                text = queryDisplayName(context, uri),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
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
