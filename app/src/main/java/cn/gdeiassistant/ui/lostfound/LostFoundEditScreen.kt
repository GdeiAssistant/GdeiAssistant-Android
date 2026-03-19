package cn.gdeiassistant.ui.lostfound

import android.widget.Toast
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
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import cn.gdeiassistant.ui.components.EmptyState
import cn.gdeiassistant.ui.components.GhostButton
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.NativeImageGallery
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.SelectionPill
import cn.gdeiassistant.ui.components.StatusBanner
import cn.gdeiassistant.ui.components.TintButton
import cn.gdeiassistant.ui.navigation.Routes
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LostFoundEditScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: LostFoundEditViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var qq by rememberSaveable { mutableStateOf("") }
    var wechat by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var selectedType by rememberSaveable { mutableStateOf(LostFoundType.LOST) }
    var selectedItemTypeId by rememberSaveable { mutableStateOf(0) }
    var initializedForItemId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is LostFoundEditEvent.ShowMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
                LostFoundEditEvent.Submitted -> {
                    runCatching { navController.getBackStackEntry(Routes.LOST_FOUND) }
                        .getOrNull()
                        ?.savedStateHandle
                        ?.set(Routes.LOST_FOUND_REFRESH_FLAG, true)
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(Routes.LOST_FOUND_PROFILE_REFRESH_FLAG, true)
                    navController.popBackStack()
                }
            }
        }
    }

    LaunchedEffect(state.item?.id) {
        val item = state.item ?: return@LaunchedEffect
        if (initializedForItemId == item.id) return@LaunchedEffect
        title = item.title
        description = item.description
        location = item.location
        qq = item.qq.orEmpty()
        wechat = item.wechat.orEmpty()
        phone = item.phone.orEmpty()
        selectedType = item.type
        selectedItemTypeId = item.itemTypeId
        initializedForItemId = item.id
    }

    LazyScreen(
        title = stringResource(R.string.lost_found_edit_title),
        onBack = navController::popBackStack,
        actions = {
            IconButton(onClick = viewModel::refresh, enabled = !state.isSubmitting) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.lost_found_refresh))
            }
        }
    ) {
        item {
            AnimatedContent(
                targetState = Triple(state.isLoading, state.item != null, state.error),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "lost_found_edit_content"
            ) { (isLoading, hasItem, error) ->
                when {
                    isLoading -> LostFoundEditLoadingPane()
                    !error.isNullOrBlank() -> StatusBanner(
                        title = stringResource(R.string.load_failed),
                        body = error,
                        icon = Icons.Rounded.EditNote,
                        tint = MaterialTheme.colorScheme.error
                    )
                    !hasItem -> EmptyState(
                        icon = Icons.Rounded.EditNote,
                        message = stringResource(R.string.lost_found_edit_missing),
                        modifier = Modifier.fillMaxWidth()
                    )
                    else -> LostFoundEditContent(
                        title = title,
                        description = description,
                        location = location,
                        qq = qq,
                        wechat = wechat,
                        phone = phone,
                        selectedType = selectedType,
                        selectedItemTypeId = selectedItemTypeId,
                        itemTypeOptions = state.itemTypeOptions,
                        imageUrls = state.item?.imageUrls.orEmpty(),
                        isSubmitting = state.isSubmitting,
                        onTitleChange = { title = it },
                        onDescriptionChange = { description = it },
                        onLocationChange = { location = it },
                        onQqChange = { qq = it },
                        onWechatChange = { wechat = it },
                        onPhoneChange = { phone = it },
                        onTypeSelected = { selectedType = it },
                        onItemTypeSelected = { selectedItemTypeId = it },
                        onSubmit = {
                            viewModel.submit(
                                title = title,
                                type = selectedType,
                                itemTypeId = selectedItemTypeId,
                                description = description,
                                location = location,
                                qq = qq,
                                wechat = wechat,
                                phone = phone
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LostFoundEditContent(
    title: String,
    description: String,
    location: String,
    qq: String,
    wechat: String,
    phone: String,
    selectedType: LostFoundType,
    selectedItemTypeId: Int,
    itemTypeOptions: List<cn.gdeiassistant.model.LostFoundItemTypeOption>,
    imageUrls: List<String>,
    isSubmitting: Boolean,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onQqChange: (String) -> Unit,
    onWechatChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onTypeSelected: (LostFoundType) -> Unit,
    onItemTypeSelected: (Int) -> Unit,
    onSubmit: () -> Unit
) {
    androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        ) {
            BadgePill(text = stringResource(R.string.lost_found_edit_title))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.lost_found_edit_subtitle),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
        }
        if (imageUrls.isNotEmpty()) {
            SectionCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.lost_found_edit_images_readonly),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                NativeImageGallery(
                    imageUrls = imageUrls,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
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
                        onClick = { onTypeSelected(type) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(R.string.lost_found_publish_title_label)) },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                label = { Text(text = stringResource(R.string.lost_found_publish_description_label)) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = location,
                onValueChange = onLocationChange,
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
                itemTypeOptions.forEach { option ->
                    SelectionPill(
                        text = option.title,
                        selected = selectedItemTypeId == option.id,
                        onClick = { onItemTypeSelected(option.id) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = qq,
                onValueChange = onQqChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(R.string.lost_found_publish_qq_label)) },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = wechat,
                onValueChange = onWechatChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(R.string.lost_found_publish_wechat_label)) },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = onPhoneChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(R.string.lost_found_publish_phone_label)) },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            GhostButton(
                text = stringResource(R.string.lost_found_edit_images_locked),
                onClick = {},
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            TintButton(
                text = if (isSubmitting) {
                    stringResource(R.string.lost_found_edit_submitting)
                } else {
                    stringResource(R.string.lost_found_edit_submit)
                },
                onClick = onSubmit,
                enabled = !isSubmitting,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun LostFoundEditLoadingPane() {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.material3.CircularProgressIndicator()
        }
    }
}
