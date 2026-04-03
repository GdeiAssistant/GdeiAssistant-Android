package cn.gdeiassistant.ui.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RestoreFromTrash
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
fun AvatarEditScreen(navController: NavHostController) {
    val viewModel: AvatarEditViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showRestoreConfirmation by rememberSaveable { mutableStateOf(false) }
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        viewModel.selectImage(uri)
    }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is AvatarEditEvent.AvatarChanged -> {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(Routes.PROFILE_AVATAR_REFRESH_FLAG, true)
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    LazyScreen(
        title = stringResource(R.string.profile_avatar_title),
        onBack = navController::popBackStack,
        showLoadingPlaceholder = state.isLoading && state.profile == null,
        actions = {
            IconButton(onClick = viewModel::load, enabled = !state.isLoading && !state.isSubmitting) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.profile_avatar_refresh))
            }
        }
    ) {
        item {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                AnimatedContent(
                    targetState = state.previewModel,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "avatar-preview"
                ) { previewModel ->
                    ProfileAvatar(
                        imageModel = previewModel,
                        fallbackLabel = state.fallbackLabel,
                        size = 132.dp
                    )
                }
            }
        }

        if (!state.error.isNullOrBlank()) {
            item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.PhotoLibrary
                )
            }
        }

        item {
            SectionCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.profile_avatar_picker_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.profile_avatar_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                GhostButton(
                    text = stringResource(R.string.profile_avatar_pick_action),
                    icon = Icons.Rounded.PhotoLibrary,
                    onClick = {
                        imagePicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                AnimatedContent(
                    targetState = state.selectedImageUri != null,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "avatar-selected"
                ) { hasSelection ->
                    if (hasSelection) {
                        AvatarSelectionSummary(uri = state.selectedImageUri)
                    } else {
                        Spacer(modifier = Modifier.height(0.dp))
                    }
                }
                AnimatedContent(
                    targetState = state.selectedImageUri != null,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "avatar-upload-button"
                ) { hasSelection ->
                    if (hasSelection) {
                        AvatarPrimaryButton(
                            text = stringResource(R.string.profile_avatar_upload_action),
                            icon = Icons.Rounded.CloudUpload,
                            onClick = viewModel::uploadSelectedAvatar,
                            enabled = state.canUpload,
                            loading = state.isSubmitting,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Spacer(modifier = Modifier.height(0.dp))
                    }
                }
            }
        }

        item {
            SectionCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.profile_avatar_restore_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.profile_avatar_restore_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                AvatarDangerButton(
                    text = stringResource(R.string.profile_avatar_restore_action),
                    icon = Icons.Rounded.RestoreFromTrash,
                    onClick = { showRestoreConfirmation = true },
                    enabled = state.canRestoreDefault,
                    modifier = Modifier.fillMaxWidth()
                )
                state.submitError?.takeIf(String::isNotBlank)?.let { errorMessage ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    if (showRestoreConfirmation) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirmation = false },
            title = { Text(text = stringResource(R.string.profile_avatar_restore_confirm_title)) },
            text = { Text(text = stringResource(R.string.profile_avatar_restore_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestoreConfirmation = false
                        viewModel.restoreDefaultAvatar()
                    }
                ) {
                    Text(text = stringResource(R.string.profile_avatar_restore_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirmation = false }) {
                    Text(text = stringResource(R.string.profile_info_cancel))
                }
            }
        )
    }
}

@Composable
private fun AvatarSelectionSummary(uri: Uri?) {
    Spacer(modifier = Modifier.height(14.dp))
    Text(
        text = stringResource(
            R.string.profile_avatar_selected_file,
            uri?.lastPathSegment.orEmpty().ifBlank { stringResource(R.string.profile_avatar_selected_file_fallback) }
        ),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun AvatarPrimaryButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    enabled: Boolean,
    loading: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = cn.gdeiassistant.ui.theme.AppShapes.button,
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = if (enabled) 0.94f else 0.54f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Spacer(modifier = Modifier.size(10.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun AvatarDangerButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = cn.gdeiassistant.ui.theme.AppShapes.button,
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = if (enabled) 0.7f else 0.44f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.size(10.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}
