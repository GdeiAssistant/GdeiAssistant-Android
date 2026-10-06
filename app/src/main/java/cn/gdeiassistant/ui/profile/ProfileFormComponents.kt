package cn.gdeiassistant.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import cn.gdeiassistant.ui.components.ListDivider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cn.gdeiassistant.R
import cn.gdeiassistant.model.AppLocaleSupport
import cn.gdeiassistant.model.ProfileLocationCatalog
import cn.gdeiassistant.model.ProfileFormSupport
import cn.gdeiassistant.model.UserProfileSummary
import cn.gdeiassistant.ui.theme.AppShapes

@Composable
internal fun ProfileSummaryContent(
    profile: UserProfileSummary,
    profileOptions: cn.gdeiassistant.model.ProfileOptions,
    isSaving: Boolean,
    onEditNickname: () -> Unit,
    onEditBirthday: () -> Unit,
    onEditCollege: () -> Unit,
    onEditMajor: () -> Unit,
    onEditEnrollment: () -> Unit,
    onEditLocation: () -> Unit,
    onEditHometown: () -> Unit,
    onEditBio: () -> Unit
) {
    val locale = AppLocaleSupport.normalizeLocale(LocalConfiguration.current.locales[0].toLanguageTag())
    val faculty = profileOptions.facultyNameFor(profile.facultyCode) ?: profile.faculty
    val major = profileOptions.majorLabelFor(faculty.orEmpty(), profile.majorCode.orEmpty()) ?: profile.major
    Column(modifier = Modifier.testTag("profile.details")) {
        ProfileSummaryRow(
            title = stringResource(R.string.profile_info_nickname),
            value = displayText(profile.nickname, stringResource(R.string.profile_info_not_set)),
            onClick = onEditNickname,
            enabled = !isSaving
        )
        ListDivider(inset = 16.dp)
        ProfileSummaryRow(
            title = stringResource(R.string.profile_info_birthday),
            value = displayText(profile.birthday, stringResource(R.string.profile_not_selected)),
            onClick = onEditBirthday,
            enabled = !isSaving
        )
        ListDivider(inset = 16.dp)
        ProfileSummaryRow(
            title = stringResource(R.string.profile_college_label),
            value = displayText(faculty, stringResource(R.string.profile_not_selected)),
            onClick = onEditCollege,
            enabled = !isSaving
        )
        ListDivider(inset = 16.dp)
        ProfileSummaryRow(
            title = stringResource(R.string.profile_info_major),
            value = displayText(major, stringResource(R.string.profile_not_selected)),
            onClick = onEditMajor,
            enabled = !isSaving
        )
        ListDivider(inset = 16.dp)
        ProfileSummaryRow(
            title = stringResource(R.string.profile_info_enrollment),
            value = displayText(profile.enrollment, stringResource(R.string.profile_not_selected)),
            monospace = true,
            onClick = onEditEnrollment,
            enabled = !isSaving
        )
        ListDivider(inset = 16.dp)
        ProfileSummaryRow(
            title = stringResource(R.string.profile_country_region_label),
            value = displayText(ProfileLocationCatalog.selectionDisplayName(profile.locationSelection, profile.location, locale), stringResource(R.string.profile_not_selected)),
            onClick = onEditLocation,
            enabled = !isSaving
        )
        ListDivider(inset = 16.dp)
        ProfileSummaryRow(
            title = stringResource(R.string.profile_info_hometown),
            value = displayText(ProfileLocationCatalog.selectionDisplayName(profile.hometownSelection, profile.hometown, locale), stringResource(R.string.profile_not_selected)),
            onClick = onEditHometown,
            enabled = !isSaving
        )
        ListDivider(inset = 16.dp)
        ProfileSummaryRow(
            title = stringResource(R.string.profile_info_intro),
            value = displayText(profile.introduction, stringResource(R.string.profile_info_not_set)),
            onClick = onEditBio,
            enabled = !isSaving,
            multiline = true
        )
    }
}

@Composable
internal fun ProfileEditingContent(
    state: ProfileUiState,
    onNicknameChange: (String) -> Unit,
    onBirthdayChange: (String) -> Unit,
    onSelectCollege: (String) -> Unit,
    onSelectMajor: (String) -> Unit,
    onSelectEnrollment: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onOpenLocationPicker: (ProfileLocationField) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    onShowBirthdayPicker: () -> Unit
) {
    val locale = AppLocaleSupport.normalizeLocale(LocalConfiguration.current.locales[0].toLanguageTag())
    val draft = state.draft
    val profileOptions = state.profileOptions
    val majorOptions = profileOptions.majorOptionsFor(draft.college)
    val canSelectMajor = profileOptions.canSelectMajor(draft.college)
    val enrollmentOptions = listOf(ProfileFormSupport.UnselectedOption) + ProfileFormSupport.enrollmentOptions

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
            leadingIcon = Icons.Rounded.CalendarMonth
        )

        ProfileSelectionField(
            title = stringResource(R.string.profile_college_label),
            value = draft.college,
            options = profileOptions.facultyOptions,
            onSelect = onSelectCollege
        )

        ProfileSelectionField(
            title = stringResource(R.string.profile_info_major),
            value = draft.major,
            options = majorOptions,
            enabled = canSelectMajor,
            disabledLabel = stringResource(R.string.profile_select_college_first),
            onSelect = onSelectMajor
        )

        ProfileSelectionField(
            title = stringResource(R.string.profile_info_enrollment),
            value = draft.grade.ifBlank { ProfileFormSupport.UnselectedOption },
            options = enrollmentOptions,
            onSelect = onSelectEnrollment,
            monospace = true
        )

        ProfileValueField(
            title = stringResource(R.string.profile_country_region_label),
            value = displayText(ProfileLocationCatalog.selectionDisplayName(draft.locationSelection, draft.location, locale), stringResource(R.string.profile_not_selected)),
            onClick = { onOpenLocationPicker(ProfileLocationField.Location) }
        )

        ProfileValueField(
            title = stringResource(R.string.profile_info_hometown),
            value = displayText(ProfileLocationCatalog.selectionDisplayName(draft.hometownSelection, draft.hometown, locale), stringResource(R.string.profile_not_selected)),
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
internal fun ProfileTextInput(
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
internal fun ProfileValueField(
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
internal fun ProfileSelectionField(
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
internal fun ProfileSummaryRow(
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
            .heightIn(min = 52.dp)
            .clickable(enabled = enabled && onClick != null, role = Role.Button) { onClick?.invoke() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = if (multiline) Alignment.Top else Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.widthIn(min = 72.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
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
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(AppShapes.pill)
                    .clickable(enabled = enabled && onActionClick != null) { onActionClick?.invoke() }
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            )
        }
        if (onClick != null) {
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
internal fun ProfileGhostButton(
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
internal fun ProfileTintButton(
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

internal fun displayText(value: String?, fallback: String): String {
    val trimmed = value?.trim().orEmpty()
    return if (trimmed.isEmpty()) fallback else trimmed
}
