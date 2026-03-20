package cn.gdeiassistant.ui.dating

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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.NoteAdd
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.PhotoLibrary
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import cn.gdeiassistant.model.DatingArea
import cn.gdeiassistant.model.DatingProfileCard
import cn.gdeiassistant.model.DatingProfileDetail
import cn.gdeiassistant.model.ProfileFormSupport
import cn.gdeiassistant.ui.components.ActionTile
import cn.gdeiassistant.ui.components.BadgePill
import cn.gdeiassistant.ui.components.EmptyState
import cn.gdeiassistant.ui.components.GhostButton
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.NativeImageGallery
import cn.gdeiassistant.ui.components.RemoteThumbnail
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.SelectionPill
import cn.gdeiassistant.ui.components.StatusBanner
import cn.gdeiassistant.ui.components.TintButton
import cn.gdeiassistant.ui.navigation.Routes
import kotlinx.coroutines.flow.collectLatest

@Composable
fun DatingScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: DatingFeedViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle

    LaunchedEffect(savedStateHandle) {
        savedStateHandle?.getStateFlow(Routes.DATING_REFRESH_FLAG, false)?.collectLatest { needsRefresh ->
            if (needsRefresh) {
                viewModel.refresh()
                savedStateHandle[Routes.DATING_REFRESH_FLAG] = false
            }
        }
    }

    LazyScreen(
        title = stringResource(R.string.dating_title),
        onBack = navController::popBackStack,
        actions = {
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.dating_feed_refresh))
            }
        }
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionTile(
                    title = stringResource(R.string.dating_center_entry_title),
                    subtitle = stringResource(R.string.dating_center_entry_subtitle),
                    icon = Icons.Rounded.ChatBubbleOutline,
                    onClick = { navController.navigate(Routes.datingCenter()) },
                    tint = MaterialTheme.colorScheme.primary,
                    emphasized = true,
                    modifier = Modifier.weight(1f)
                )
                ActionTile(
                    title = stringResource(R.string.dating_publish_title),
                    subtitle = stringResource(R.string.dating_publish_subtitle),
                    icon = Icons.AutoMirrored.Rounded.NoteAdd,
                    onClick = { navController.navigate(Routes.DATING_PUBLISH) },
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            SectionCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
            ) {
                BadgePill(text = stringResource(R.string.dating_feed_badge))
                Spacer(modifier = Modifier.size(16.dp))
                Text(
                    text = stringResource(R.string.dating_feed_subtitle),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
        item {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DatingArea.entries.forEach { area ->
                    SelectionPill(
                        text = datingAreaLabel(area),
                        selected = state.selectedArea == area,
                        onClick = { viewModel.selectArea(area) }
                    )
                }
            }
        }
        if (!state.error.isNullOrBlank()) {
            item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.People
                )
            }
        }
        when {
            state.isLoading && state.profiles.isEmpty() -> item { DatingModuleLoadingPane() }
            state.profiles.isEmpty() -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    EmptyState(
                        icon = Icons.Rounded.People,
                        message = stringResource(R.string.dating_feed_empty),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            else -> {
                items(state.profiles, key = { it.id }) { item ->
                    DatingFeedCard(
                        item = item,
                        onClick = { navController.navigate(Routes.datingDetail(item.id)) }
                    )
                }
            }
        }
    }
}

@Composable
fun DatingDetailScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: DatingDetailViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var pickContent by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is DatingModuleEvent.ShowMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
                DatingModuleEvent.Submitted -> Unit
            }
        }
    }

    LazyScreen(
        title = stringResource(R.string.dating_detail_title),
        onBack = navController::popBackStack,
        actions = {
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.dating_feed_refresh))
            }
        }
    ) {
        when {
            state.isLoading -> item { DatingModuleLoadingPane() }
            !state.error.isNullOrBlank() && state.detail == null -> item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.People
                )
            }
            state.detail == null -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    EmptyState(
                        icon = Icons.Rounded.People,
                        message = stringResource(R.string.dating_detail_missing),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            else -> {
                val detail = requireNotNull(state.detail)
                item { DatingDetailHero(detail = detail) }
                detail.profile.imageUrl?.takeIf { it.isNotBlank() }?.let { imageUrl ->
                    item {
                        SectionCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = stringResource(R.string.dating_detail_gallery_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.size(14.dp))
                            NativeImageGallery(
                                imageUrls = listOf(imageUrl),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                item {
                    DatingContactCard(detail = detail)
                }
                item {
                    DatingPickCard(
                        detail = detail,
                        content = pickContent,
                        onContentChange = { pickContent = it },
                        onSubmit = { viewModel.submitPick(pickContent) },
                        isSubmitting = state.isSubmitting
                    )
                }
            }
        }
    }
}

@Composable
fun DatingPublishScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: DatingPublishViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var nickname by rememberSaveable { mutableStateOf("") }
    var hometown by rememberSaveable { mutableStateOf("") }
    var qq by rememberSaveable { mutableStateOf("") }
    var wechat by rememberSaveable { mutableStateOf("") }
    var content by rememberSaveable { mutableStateOf("") }
    var selectedGrade by rememberSaveable { mutableStateOf(1) }
    var selectedArea by rememberSaveable { mutableStateOf(DatingArea.GIRL) }
    var selectedFaculty by rememberSaveable { mutableStateOf(ProfileFormSupport.UnselectedOption) }
    var selectedImage by remember { mutableStateOf<Uri?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedImage = uri
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is DatingModuleEvent.ShowMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
                DatingModuleEvent.Submitted -> {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(Routes.DATING_REFRESH_FLAG, true)
                    navController.popBackStack()
                }
            }
        }
    }

    LazyScreen(
        title = stringResource(R.string.dating_publish_title),
        onBack = navController::popBackStack
    ) {
        item {
            SectionCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f)
            ) {
                BadgePill(text = stringResource(R.string.dating_publish_title))
                Spacer(modifier = Modifier.size(16.dp))
                Text(
                    text = stringResource(R.string.dating_publish_page_subtitle),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
        item {
            SectionCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = stringResource(R.string.dating_publish_nickname_label)) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                DatingSelectionSection(
                    title = stringResource(R.string.dating_publish_grade_label)
                ) {
                    (1..4).forEach { grade ->
                        SelectionPill(
                            text = datingGradeLabel(grade),
                            selected = selectedGrade == grade,
                            onClick = { selectedGrade = grade }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                DatingSelectionSection(
                    title = stringResource(R.string.dating_publish_area_label)
                ) {
                    DatingArea.entries.forEach { area ->
                        SelectionPill(
                            text = datingAreaLabel(area),
                            selected = selectedArea == area,
                            onClick = { selectedArea = area }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                DatingSelectionSection(
                    title = stringResource(R.string.dating_publish_faculty_label)
                ) {
                    state.facultyOptions.forEach { faculty ->
                        SelectionPill(
                            text = faculty,
                            selected = selectedFaculty == faculty,
                            onClick = { selectedFaculty = faculty }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = hometown,
                    onValueChange = { hometown = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = stringResource(R.string.dating_publish_hometown_label)) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = qq,
                    onValueChange = { qq = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = stringResource(R.string.dating_publish_qq_label)) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = wechat,
                    onValueChange = { wechat = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = stringResource(R.string.dating_publish_wechat_label)) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    label = { Text(text = stringResource(R.string.dating_publish_content_label)) }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GhostButton(
                        text = stringResource(R.string.dating_publish_pick_image),
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
                            stringResource(R.string.dating_publish_submitting)
                        } else {
                            stringResource(R.string.dating_publish_submit)
                        },
                        onClick = {
                            viewModel.submit(
                                nickname = nickname,
                                grade = selectedGrade,
                                area = selectedArea,
                                faculty = selectedFaculty,
                                hometown = hometown,
                                qq = qq,
                                wechat = wechat,
                                content = content,
                                image = selectedImage
                            )
                        },
                        enabled = !state.isSubmitting,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                AnimatedContent(
                    targetState = selectedImage,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "dating_publish_image"
                ) { image ->
                    Text(
                        text = image?.let { queryDisplayName(context, it) }
                            ?: stringResource(R.string.dating_publish_image_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DatingFeedCard(
    item: DatingProfileCard,
    onClick: () -> Unit
) {
    SectionCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(188.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RemoteThumbnail(
                imageModel = item.imageUrl,
                fallbackLabel = item.nickname,
                width = 92.dp,
                height = 120.dp,
                tint = MaterialTheme.colorScheme.tertiary
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = item.nickname,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${item.grade} · ${item.faculty}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.dating_hometown_prefix, item.hometown),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = item.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3
                )
            }
        }
    }
}

@Composable
private fun DatingDetailHero(detail: DatingProfileDetail) {
    SectionCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
    ) {
        BadgePill(text = datingAreaLabel(detail.profile.area))
        Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = detail.profile.nickname,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = "${detail.profile.grade} · ${detail.profile.faculty} · ${stringResource(R.string.dating_hometown_prefix, detail.profile.hometown)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.size(12.dp))
        Text(
            text = detail.profile.content,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun DatingContactCard(detail: DatingProfileDetail) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.dating_detail_contact_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.size(12.dp))
        if (detail.isContactVisible) {
            detail.qq?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = stringResource(R.string.dating_contact_qq, it),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            detail.wechat?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = stringResource(R.string.dating_contact_wechat, it),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (detail.qq.isNullOrBlank() && detail.wechat.isNullOrBlank()) {
                Text(
                    text = stringResource(R.string.dating_detail_contact_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Text(
                text = stringResource(R.string.dating_detail_contact_hidden),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DatingPickCard(
    detail: DatingProfileDetail,
    content: String,
    onContentChange: (String) -> Unit,
    onSubmit: () -> Unit,
    isSubmitting: Boolean
) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.dating_pick_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.size(12.dp))
        when {
            detail.profile.isMine -> {
                Text(
                    text = stringResource(R.string.dating_pick_self_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            detail.isContactVisible -> {
                Text(
                    text = stringResource(R.string.dating_pick_contact_visible_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            detail.isPickNotAvailable -> {
                Text(
                    text = stringResource(R.string.dating_pick_existing_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            else -> {
                OutlinedTextField(
                    value = content,
                    onValueChange = onContentChange,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    label = { Text(text = stringResource(R.string.dating_pick_label)) }
                )
                Spacer(modifier = Modifier.size(12.dp))
                TintButton(
                    text = if (isSubmitting) {
                        stringResource(R.string.dating_pick_submitting)
                    } else {
                        stringResource(R.string.dating_pick_submit)
                    },
                    onClick = onSubmit,
                    enabled = !isSubmitting,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun DatingSelectionSection(
    title: String,
    content: @Composable () -> Unit
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(10.dp))
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        content()
    }
}

@Composable
private fun DatingModuleLoadingPane() {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.dating_loading_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.dating_loading_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun datingAreaLabel(area: DatingArea): String {
    return when (area) {
        DatingArea.GIRL -> stringResource(R.string.dating_area_girl)
        DatingArea.BOY -> stringResource(R.string.dating_area_boy)
    }
}

@Composable
private fun datingGradeLabel(grade: Int): String {
    return when (grade) {
        1 -> stringResource(R.string.dating_grade_one)
        2 -> stringResource(R.string.dating_grade_two)
        3 -> stringResource(R.string.dating_grade_three)
        else -> stringResource(R.string.dating_grade_four)
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
