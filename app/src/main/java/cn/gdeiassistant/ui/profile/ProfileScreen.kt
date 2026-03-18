package cn.gdeiassistant.ui.profile

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Feedback
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import cn.gdeiassistant.model.ProfileFormSupport
import cn.gdeiassistant.model.ProfileLocationRegion
import cn.gdeiassistant.model.ProfileLocationSelection
import cn.gdeiassistant.model.UserProfileSummary
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.SectionCard
import cn.gdeiassistant.ui.components.StatusBanner
import cn.gdeiassistant.ui.navigation.Routes
import cn.gdeiassistant.ui.theme.AppShapes
import kotlinx.coroutines.flow.collectLatest
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private enum class ProfileTextEditorField {
    Nickname,
    Bio
}

private enum class ProfileSelectionEditorField {
    College,
    Major,
    Enrollment
}

@Composable
fun ProfileScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: ProfileViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val screenTitle = stringResource(R.string.profile_title)
    val accountActionsTitle = stringResource(R.string.profile_account_actions_title)
    val moreServicesTitle = stringResource(R.string.profile_more_services_title)
    val accountActionItems = listOf(
        ProfileMenuItem(Icons.Rounded.Lock, stringResource(R.string.profile_privacy_title)) {
            navController.navigate(Routes.PROFILE_PRIVACY)
        },
        ProfileMenuItem(Icons.Rounded.History, stringResource(R.string.profile_login_records_title)) {
            navController.navigate(Routes.PROFILE_LOGIN_RECORDS)
        },
        ProfileMenuItem(Icons.Rounded.PhoneAndroid, stringResource(R.string.profile_bind_phone_title)) {
            navController.navigate(Routes.PROFILE_BIND_PHONE)
        },
        ProfileMenuItem(Icons.Rounded.Email, stringResource(R.string.profile_bind_email_title)) {
            navController.navigate(Routes.PROFILE_BIND_EMAIL)
        },
        ProfileMenuItem(Icons.Rounded.VerifiedUser, stringResource(R.string.profile_delete_title)) {
            navController.navigate(Routes.PROFILE_DELETE_ACCOUNT)
        }
    )
    val serviceItems = listOf(
        ProfileMenuItem(Icons.Rounded.CloudDownload, stringResource(R.string.profile_download_data_title)) {
            navController.navigate(Routes.PROFILE_DOWNLOAD_DATA)
        },
        ProfileMenuItem(Icons.Rounded.Feedback, stringResource(R.string.profile_feedback_title)) {
            navController.navigate(Routes.PROFILE_FEEDBACK)
        },
        ProfileMenuItem(Icons.Rounded.Settings, stringResource(R.string.profile_settings_title)) {
            navController.navigate(Routes.PROFILE_SETTINGS)
        }
    )

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ProfileEvent.ShowMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
                ProfileEvent.NavigateToLogin -> {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    LaunchedEffect(savedStateHandle) {
        savedStateHandle?.getStateFlow(Routes.PROFILE_AVATAR_REFRESH_FLAG, false)?.collectLatest { needsRefresh ->
            if (needsRefresh) {
                viewModel.refresh()
                savedStateHandle[Routes.PROFILE_AVATAR_REFRESH_FLAG] = false
            }
        }
    }

    LazyScreen(
        title = screenTitle,
        showLoadingPlaceholder = state.isLoading && state.profile == null
    ) {
        if (!state.error.isNullOrBlank() && state.profile == null) {
            item {
                StatusBanner(
                    title = stringResource(R.string.load_failed),
                    body = state.error.orEmpty(),
                    icon = Icons.Rounded.Person
                )
            }
        }

        when (val profile = state.profile) {
            null -> item {
                ProfileEmptyCard()
            }

            else -> {
                item {
                    ProfileAccountCard(
                        profile = profile,
                        state = state,
                        onOpenAvatarManager = { navController.navigate(Routes.PROFILE_AVATAR) },
                        onSaveNickname = viewModel::saveNickname,
                        onSaveBirthday = viewModel::saveBirthday,
                        onClearBirthday = viewModel::clearBirthdayAndSave,
                        onSaveCollege = viewModel::saveCollege,
                        onSaveMajor = viewModel::saveMajor,
                        onSaveEnrollment = viewModel::saveEnrollment,
                        onSaveBio = viewModel::saveBio,
                        onSaveLocation = viewModel::saveLocation
                    )
                }
            }
        }

        profileMenuSection(
            title = accountActionsTitle,
            items = accountActionItems
        )

        profileMenuSection(
            title = moreServicesTitle,
            items = serviceItems
        )

        item {
            ProfileLogoutButton(onClick = viewModel::logout)
        }
    }
}

private fun LazyListScope.profileMenuSection(
    title: String,
    items: List<ProfileMenuItem>
) {
    item {
        SectionCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            items.forEachIndexed { index, item ->
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 60.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
                ProfileMenuRow(item = item)
            }
        }
    }
}

@Composable
private fun ProfileAccountCard(
    profile: UserProfileSummary,
    state: ProfileUiState,
    onOpenAvatarManager: () -> Unit,
    onSaveNickname: (String) -> Unit,
    onSaveBirthday: (String) -> Unit,
    onClearBirthday: () -> Unit,
    onSaveCollege: (String) -> Unit,
    onSaveMajor: (String) -> Unit,
    onSaveEnrollment: (String) -> Unit,
    onSaveBio: (String) -> Unit,
    onSaveLocation: (ProfileLocationField, ProfileLocationSelection) -> Unit
) {
    val context = LocalContext.current
    var showBirthdayPicker by rememberSaveable { mutableStateOf(false) }
    var activeLocationField by remember { mutableStateOf<ProfileLocationField?>(null) }
    var activeTextEditor by remember { mutableStateOf<ProfileTextEditorField?>(null) }
    var textEditorValue by rememberSaveable { mutableStateOf("") }
    var activeSelectionEditor by remember { mutableStateOf<ProfileSelectionEditorField?>(null) }
    val displayName = profile.nickname?.takeIf(String::isNotBlank)
        ?: stringResource(R.string.profile_edit_placeholder)
    val currentCollege = profile.faculty?.takeIf(String::isNotBlank) ?: ProfileFormSupport.UnselectedOption
    val currentMajor = profile.major?.takeIf(String::isNotBlank) ?: ProfileFormSupport.UnselectedOption
    val currentEnrollment = profile.enrollment?.takeIf(String::isNotBlank) ?: ProfileFormSupport.UnselectedOption
    val canSelectMajor = ProfileFormSupport.canSelectMajor(currentCollege)

    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.profile_account_data_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            ProfileAvatar(
                imageModel = profile.avatar?.trim()?.takeIf(String::isNotBlank),
                fallbackLabel = displayName,
                size = 72.dp,
                modifier = Modifier.clickable(onClick = onOpenAvatarManager)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.profile_username_label, profile.username),
                    style = MaterialTheme.typography.labelLarge,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!profile.ipArea.isNullOrBlank()) {
                    Text(
                        text = stringResource(R.string.profile_ip_area_label, profile.ipArea),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(16.dp))

        ProfileSummaryContent(
            profile = profile,
            isSaving = state.isSaving,
            onEditNickname = {
                textEditorValue = profile.nickname.orEmpty()
                activeTextEditor = ProfileTextEditorField.Nickname
            },
            onEditBirthday = { showBirthdayPicker = true },
            onClearBirthday = onClearBirthday,
            onEditCollege = { activeSelectionEditor = ProfileSelectionEditorField.College },
            onEditMajor = {
                if (!canSelectMajor) {
                    Toast.makeText(context, context.getString(R.string.profile_select_college_first), Toast.LENGTH_LONG).show()
                } else {
                    activeSelectionEditor = ProfileSelectionEditorField.Major
                }
            },
            onEditEnrollment = { activeSelectionEditor = ProfileSelectionEditorField.Enrollment },
            onEditLocation = { activeLocationField = ProfileLocationField.Location },
            onEditHometown = { activeLocationField = ProfileLocationField.Hometown },
            onEditBio = {
                textEditorValue = profile.introduction.orEmpty()
                activeTextEditor = ProfileTextEditorField.Bio
            }
        )

        state.saveError?.takeIf(String::isNotBlank)?.let { errorMessage ->
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }

    if (showBirthdayPicker) {
        ProfileBirthdayPickerDialog(
            currentBirthday = profile.birthday.orEmpty(),
            onDismiss = { showBirthdayPicker = false },
            onConfirm = { selectedBirthday ->
                onSaveBirthday(selectedBirthday)
                showBirthdayPicker = false
            }
        )
    }

    activeLocationField?.let { field ->
        ProfileLocationPickerSheet(
            title = when (field) {
                ProfileLocationField.Location -> stringResource(R.string.profile_country_region_picker_title)
                ProfileLocationField.Hometown -> stringResource(R.string.profile_hometown_picker_title)
            },
            regions = state.locationRegions,
            onDismiss = { activeLocationField = null },
            onConfirm = { selection ->
                onSaveLocation(field, selection)
                activeLocationField = null
            }
        )
    }
    activeTextEditor?.let { editor ->
        ProfileTextEditorDialog(
            title = when (editor) {
                ProfileTextEditorField.Nickname -> stringResource(R.string.profile_info_nickname)
                ProfileTextEditorField.Bio -> stringResource(R.string.profile_info_intro)
            },
            placeholder = when (editor) {
                ProfileTextEditorField.Nickname -> stringResource(R.string.profile_nickname_placeholder)
                ProfileTextEditorField.Bio -> stringResource(R.string.profile_bio_placeholder)
            },
            value = textEditorValue,
            onValueChange = { textEditorValue = it },
            onDismiss = { activeTextEditor = null },
            onConfirm = {
                when (editor) {
                    ProfileTextEditorField.Nickname -> onSaveNickname(textEditorValue)
                    ProfileTextEditorField.Bio -> onSaveBio(textEditorValue)
                }
                activeTextEditor = null
            },
            isSaving = state.isSaving,
            singleLine = editor == ProfileTextEditorField.Nickname
        )
    }

    activeSelectionEditor?.let { editor ->
        ProfileSelectionPickerSheet(
            title = when (editor) {
                ProfileSelectionEditorField.College -> stringResource(R.string.profile_college_label)
                ProfileSelectionEditorField.Major -> stringResource(R.string.profile_info_major)
                ProfileSelectionEditorField.Enrollment -> stringResource(R.string.profile_info_enrollment)
            },
            options = when (editor) {
                ProfileSelectionEditorField.College -> ProfileFormSupport.facultyOptions
                ProfileSelectionEditorField.Major -> ProfileFormSupport.majorOptionsFor(currentCollege)
                ProfileSelectionEditorField.Enrollment -> listOf(ProfileFormSupport.UnselectedOption) + ProfileFormSupport.enrollmentOptions
            },
            selectedValue = when (editor) {
                ProfileSelectionEditorField.College -> currentCollege
                ProfileSelectionEditorField.Major -> currentMajor
                ProfileSelectionEditorField.Enrollment -> currentEnrollment
            },
            monospace = editor == ProfileSelectionEditorField.Enrollment,
            onDismiss = { activeSelectionEditor = null },
            onSelect = { option ->
                when (editor) {
                    ProfileSelectionEditorField.College -> onSaveCollege(option)
                    ProfileSelectionEditorField.Major -> onSaveMajor(option)
                    ProfileSelectionEditorField.Enrollment -> onSaveEnrollment(option)
                }
                activeSelectionEditor = null
            }
        )
    }
}

@Composable
private fun ProfileSummaryContent(
    profile: UserProfileSummary,
    isSaving: Boolean,
    onEditNickname: () -> Unit,
    onEditBirthday: () -> Unit,
    onClearBirthday: () -> Unit,
    onEditCollege: () -> Unit,
    onEditMajor: () -> Unit,
    onEditEnrollment: () -> Unit,
    onEditLocation: () -> Unit,
    onEditHometown: () -> Unit,
    onEditBio: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ProfileSummaryRow(
            title = stringResource(R.string.profile_info_nickname),
            value = displayText(profile.nickname, stringResource(R.string.profile_edit_placeholder)),
            onClick = onEditNickname,
            enabled = !isSaving
        )
        ProfileSummaryRow(
            title = stringResource(R.string.profile_info_birthday),
            value = displayText(profile.birthday, stringResource(R.string.profile_not_selected)),
            onClick = onEditBirthday,
            enabled = !isSaving,
            actionLabel = if (!profile.birthday.isNullOrBlank()) stringResource(R.string.profile_clear_birthday) else null,
            onActionClick = onClearBirthday
        )
        ProfileSummaryRow(
            title = stringResource(R.string.profile_college_label),
            value = displayText(profile.faculty, stringResource(R.string.profile_not_selected)),
            onClick = onEditCollege,
            enabled = !isSaving
        )
        ProfileSummaryRow(
            title = stringResource(R.string.profile_info_major),
            value = displayText(profile.major, stringResource(R.string.profile_not_selected)),
            onClick = onEditMajor,
            enabled = !isSaving
        )
        ProfileSummaryRow(
            title = stringResource(R.string.profile_info_enrollment),
            value = displayText(profile.enrollment, stringResource(R.string.profile_not_selected)),
            monospace = true,
            onClick = onEditEnrollment,
            enabled = !isSaving
        )
        ProfileSummaryRow(
            title = stringResource(R.string.profile_country_region_label),
            value = displayText(profile.location, stringResource(R.string.profile_not_selected)),
            onClick = onEditLocation,
            enabled = !isSaving
        )
        ProfileSummaryRow(
            title = stringResource(R.string.profile_info_hometown),
            value = displayText(profile.hometown, stringResource(R.string.profile_not_selected)),
            onClick = onEditHometown,
            enabled = !isSaving
        )
        ProfileSummaryRow(
            title = stringResource(R.string.profile_info_intro),
            value = displayText(profile.introduction, stringResource(R.string.profile_bio_placeholder)),
            onClick = onEditBio,
            enabled = !isSaving,
            multiline = true
        )
    }
}

@Composable
private fun ProfileEditingContent(
    state: ProfileUiState,
    onNicknameChange: (String) -> Unit,
    onBirthdayChange: (String) -> Unit,
    onClearBirthday: () -> Unit,
    onSelectCollege: (String) -> Unit,
    onSelectMajor: (String) -> Unit,
    onSelectEnrollment: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onOpenLocationPicker: (ProfileLocationField) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    onShowBirthdayPicker: () -> Unit
) {
    val draft = state.draft

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ProfileTextInput(
            title = stringResource(R.string.profile_info_nickname),
            value = draft.nickname,
            placeholder = stringResource(R.string.profile_nickname_placeholder),
            onValueChange = onNicknameChange
        )

        ProfileValueField(
            title = stringResource(R.string.profile_info_birthday),
            value = displayText(draft.birthday, stringResource(R.string.profile_not_selected)),
            onClick = onShowBirthdayPicker,
            leadingIcon = Icons.Rounded.CalendarMonth,
            trailingContent = {
                if (draft.birthday.isNotBlank()) {
                    TextButton(onClick = onClearBirthday) {
                        Text(text = stringResource(R.string.profile_clear_birthday))
                    }
                }
            }
        )

        ProfileSelectionField(
            title = stringResource(R.string.profile_college_label),
            value = draft.college,
            options = ProfileFormSupport.facultyOptions,
            onSelect = onSelectCollege
        )

        ProfileSelectionField(
            title = stringResource(R.string.profile_info_major),
            value = draft.major,
            options = draft.majorOptions,
            enabled = draft.canSelectMajor,
            disabledLabel = stringResource(R.string.profile_select_college_first),
            onSelect = onSelectMajor
        )

        ProfileSelectionField(
            title = stringResource(R.string.profile_info_enrollment),
            value = draft.grade.ifBlank { ProfileFormSupport.UnselectedOption },
            options = draft.enrollmentOptions,
            onSelect = onSelectEnrollment,
            monospace = true
        )

        ProfileValueField(
            title = stringResource(R.string.profile_country_region_label),
            value = displayText(draft.location, stringResource(R.string.profile_not_selected)),
            onClick = { onOpenLocationPicker(ProfileLocationField.Location) }
        )

        ProfileValueField(
            title = stringResource(R.string.profile_info_hometown),
            value = displayText(draft.hometown, stringResource(R.string.profile_not_selected)),
            onClick = { onOpenLocationPicker(ProfileLocationField.Hometown) }
        )

        ProfileTextInput(
            title = stringResource(R.string.profile_info_intro),
            value = draft.bio,
            placeholder = stringResource(R.string.profile_bio_placeholder),
            onValueChange = onBioChange,
            minLines = 3,
            maxLines = 6,
            singleLine = false
        )

        state.saveError?.takeIf(String::isNotBlank)?.let { errorMessage ->
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ProfileGhostButton(
                text = stringResource(R.string.profile_info_cancel),
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            )
            ProfileTintButton(
                text = stringResource(R.string.profile_save_profile_action),
                onClick = onSave,
                enabled = draft.isFormValid && !state.isSaving,
                loading = state.isSaving,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ProfileTextInput(
    title: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    minLines: Int = 1,
    maxLines: Int = 1,
    singleLine: Boolean = true
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder) },
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines,
            shape = AppShapes.button
        )
    }
}

@Composable
private fun ProfileValueField(
    title: String,
    value: String,
    onClick: () -> Unit,
    leadingIcon: ImageVector? = null,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            shape = AppShapes.button,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                leadingIcon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                trailingContent?.invoke()
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                )
            }
        }
    }
}

@Composable
private fun ProfileSelectionField(
    title: String,
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    enabled: Boolean = true,
    disabledLabel: String? = null,
    monospace: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Box {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = enabled && options.isNotEmpty()) { expanded = true },
                shape = AppShapes.button,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (enabled) {
                            displayText(value, ProfileFormSupport.UnselectedOption)
                        } else {
                            disabledLabel ?: ProfileFormSupport.UnselectedOption
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = if (monospace) FontFamily.Monospace else FontFamily.Default,
                        color = if (enabled) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                fontFamily = if (monospace) FontFamily.Monospace else FontFamily.Default
                            )
                        },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileSummaryRow(
    title: String,
    value: String,
    monospace: Boolean = false,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    multiline: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.button)
            .clickable(enabled = enabled && onClick != null) { onClick?.invoke() }
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            fontFamily = if (monospace) FontFamily.Monospace else FontFamily.Default,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
            maxLines = if (multiline) Int.MAX_VALUE else 2,
            overflow = if (multiline) TextOverflow.Clip else TextOverflow.Ellipsis
        )
        actionLabel?.let { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(AppShapes.pill)
                    .clickable(enabled = enabled && onActionClick != null) { onActionClick?.invoke() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        if (onClick != null) {
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = if (multiline) 2.dp else 0.dp)
            )
        }
    }
}

@Composable
private fun ProfileTextEditorDialog(
    title: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isSaving: Boolean,
    singleLine: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(placeholder) },
                singleLine = singleLine,
                minLines = if (singleLine) 1 else 4,
                maxLines = if (singleLine) 1 else 6,
                shape = AppShapes.button
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isSaving) {
                Text(text = stringResource(R.string.profile_confirm_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text(text = stringResource(R.string.profile_info_cancel))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileSelectionPickerSheet(
    title: String,
    options: List<String>,
    selectedValue: String,
    monospace: Boolean,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 420.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(options, key = { it }) { option ->
                ListItem(
                    headlineContent = {
                        Text(
                            text = option,
                            fontFamily = if (monospace) FontFamily.Monospace else FontFamily.Default,
                            fontWeight = if (option == selectedValue) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    supportingContent = if (option == selectedValue) {
                        { Text(text = stringResource(R.string.profile_selected_badge)) }
                    } else {
                        null
                    },
                    modifier = Modifier.clickable { onSelect(option) }
                )
            }
        }
    }
}

@Composable
private fun ProfileGhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = AppShapes.button,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f))
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 18.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ProfileTintButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    destructive: Boolean = false
) {
    val containerColor = if (destructive) {
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.72f)
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
    }
    val contentColor = if (destructive) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }

    Surface(
        modifier = modifier.clickable(enabled = enabled && !loading, onClick = onClick),
        shape = AppShapes.button,
        color = containerColor,
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.16f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = contentColor
                )
            } else {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) contentColor else contentColor.copy(alpha = 0.45f)
                )
            }
        }
    }
}

@Composable
private fun ProfileMenuRow(item: ProfileMenuItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = item.onClick)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.52f),
                    shape = AppShapes.small
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }

        Text(
            text = item.label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
        )
    }
}

@Composable
private fun ProfileLogoutButton(onClick: () -> Unit) {
    ProfileTintButton(
        text = stringResource(R.string.profile_logout),
        onClick = onClick,
        destructive = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ProfileEmptyCard() {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.profile_empty_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.profile_empty_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileBirthdayPickerDialog(
    currentBirthday: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val initialMillis = remember(currentBirthday) {
        runCatching {
            LocalDate.parse(currentBirthday, DateTimeFormatter.ISO_LOCAL_DATE)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        }.getOrNull()
    }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = datePickerState.selectedDateMillis ?: return@TextButton
                    val date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                    onConfirm(date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                }
            ) {
                Text(text = stringResource(R.string.profile_info_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.profile_info_cancel))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileLocationPickerSheet(
    title: String,
    regions: List<ProfileLocationRegion>,
    onDismiss: () -> Unit,
    onConfirm: (ProfileLocationSelection) -> Unit
) {
    var selectedRegionCode by remember(regions) { mutableStateOf(regions.firstOrNull()?.code.orEmpty()) }
    var selectedStateCode by remember(regions) { mutableStateOf("") }
    var selectedCityCode by remember(regions) { mutableStateOf("") }

    val currentRegion = remember(regions, selectedRegionCode) {
        regions.firstOrNull { it.code == selectedRegionCode } ?: regions.firstOrNull()
    }
    val currentStates = currentRegion?.states.orEmpty()
    val currentState = remember(currentStates, selectedStateCode) {
        currentStates.firstOrNull { it.code == selectedStateCode } ?: currentStates.firstOrNull()
    }
    val currentCities = currentState?.cities.orEmpty()
    val currentCity = remember(currentCities, selectedCityCode) {
        currentCities.firstOrNull { it.code == selectedCityCode } ?: currentCities.firstOrNull()
    }

    LaunchedEffect(currentRegion?.code, currentStates) {
        if (currentStates.none { it.code == selectedStateCode }) {
            selectedStateCode = currentStates.firstOrNull()?.code.orEmpty()
        }
    }

    LaunchedEffect(currentState?.code, currentCities) {
        if (currentCities.none { it.code == selectedCityCode }) {
            selectedCityCode = currentCities.firstOrNull()?.code.orEmpty()
        }
    }

    val currentSelection = remember(currentRegion, currentState, currentCity) {
        currentRegion?.let { region ->
            ProfileLocationSelection(
                displayName = ProfileFormSupport.makeLocationDisplay(
                    region = region.name,
                    state = currentState?.name.orEmpty(),
                    city = currentCity?.name.orEmpty()
                ),
                regionCode = region.code,
                stateCode = currentState?.code.orEmpty(),
                cityCode = currentCity?.code.orEmpty()
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = AppShapes.container
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            if (regions.isEmpty()) {
                Text(
                    text = stringResource(R.string.profile_location_unavailable),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                ProfileSelectionField(
                    title = stringResource(R.string.profile_country_region_label),
                    value = currentRegion?.name.orEmpty(),
                    options = regions.map { it.name },
                    onSelect = { selectedName ->
                        selectedRegionCode = regions.firstOrNull { it.name == selectedName }?.code.orEmpty()
                    }
                )

                if (currentStates.isNotEmpty()) {
                    ProfileSelectionField(
                        title = stringResource(R.string.profile_state_label),
                        value = currentState?.name.orEmpty(),
                        options = currentStates.map { it.name },
                        onSelect = { selectedName ->
                            selectedStateCode = currentStates.firstOrNull { it.name == selectedName }?.code.orEmpty()
                        }
                    )
                }

                if (currentCities.isNotEmpty()) {
                    ProfileSelectionField(
                        title = stringResource(R.string.profile_city_label),
                        value = currentCity?.name.orEmpty(),
                        options = currentCities.map { it.name },
                        onSelect = { selectedName ->
                            selectedCityCode = currentCities.firstOrNull { it.name == selectedName }?.code.orEmpty()
                        }
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ProfileGhostButton(
                    text = stringResource(R.string.profile_info_cancel),
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                )
                ProfileTintButton(
                    text = stringResource(R.string.profile_confirm_action),
                    onClick = { currentSelection?.let(onConfirm) },
                    enabled = currentSelection != null,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

private data class ProfileMenuItem(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit
)

private fun displayText(value: String?, fallback: String): String {
    val trimmed = value?.trim().orEmpty()
    return if (trimmed.isEmpty()) fallback else trimmed
}
