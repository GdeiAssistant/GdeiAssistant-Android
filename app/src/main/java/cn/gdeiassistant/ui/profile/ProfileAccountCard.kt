package cn.gdeiassistant.ui.profile

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import cn.gdeiassistant.ui.components.ListGroup
import cn.gdeiassistant.ui.components.SectionHeader
import cn.gdeiassistant.ui.components.StatItem
import cn.gdeiassistant.ui.components.StatStrip
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cn.gdeiassistant.R
import cn.gdeiassistant.model.AppLocaleSupport
import cn.gdeiassistant.model.ProfileLocationCatalog
import cn.gdeiassistant.model.ProfileFormSupport
import cn.gdeiassistant.model.ProfileLocationSelection
import cn.gdeiassistant.model.SocialRelationshipKind
import cn.gdeiassistant.model.UserProfileSummary

@Composable
internal fun ProfileAccountCard(
    profile: UserProfileSummary,
    state: ProfileUiState,
    onOpenAvatarManager: () -> Unit,
    onOpenRelations: (String, SocialRelationshipKind) -> Unit,
    onSaveNickname: (String) -> Unit,
    onSaveBirthday: (String) -> Unit,
    onSaveCollege: (String) -> Unit,
    onSaveMajor: (String) -> Unit,
    onSaveEnrollment: (String) -> Unit,
    onSaveBio: (String) -> Unit,
    onSaveLocation: (ProfileLocationField, ProfileLocationSelection) -> Unit
) {
    val context = LocalContext.current
    val locale = AppLocaleSupport.normalizeLocale(LocalConfiguration.current.locales[0].toLanguageTag())
    var showBirthdayPicker by rememberSaveable { mutableStateOf(false) }
    var activeLocationField by remember { mutableStateOf<ProfileLocationField?>(null) }
    var activeTextEditor by remember { mutableStateOf<ProfileTextEditorField?>(null) }
    var textEditorValue by rememberSaveable { mutableStateOf("") }
    var activeSelectionEditor by remember { mutableStateOf<ProfileSelectionEditorField?>(null) }
    val displayName = profile.nickname?.takeIf(String::isNotBlank)
        ?: stringResource(R.string.profile_info_not_set)
    val avatarFallbackLabel = profile.nickname?.takeIf(String::isNotBlank)
        ?: profile.username
    val profileOptions = state.profileOptions.localizedForLocale(locale)
    val currentCollege = profileOptions.facultyNameFor(profile.facultyCode)
        ?: profile.faculty?.takeIf(String::isNotBlank) ?: ProfileFormSupport.UnselectedOption
    val currentMajor = profileOptions.majorLabelFor(currentCollege, profile.majorCode.orEmpty())
        ?: profile.major?.takeIf(String::isNotBlank) ?: ProfileFormSupport.UnselectedOption
    val currentEnrollment = profile.enrollment?.takeIf(String::isNotBlank) ?: ProfileFormSupport.UnselectedOption
    val canSelectMajor = profileOptions.canSelectMajor(currentCollege)
    val selectCollegeFirstText = stringResource(R.string.profile_select_college_first)

    Column(
        modifier = Modifier.fillMaxWidth().testTag("profile.account"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp)
                .testTag("profile.identity"),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileAvatar(
                imageModel = profile.avatar?.trim()?.takeIf(String::isNotBlank),
                fallbackLabel = avatarFallbackLabel,
                size = 72.dp,
                modifier = Modifier.clickable(role = Role.Button, onClick = onOpenAvatarManager)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.profile_username_label, profile.username),
                    style = MaterialTheme.typography.labelLarge,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!profile.ipArea.isNullOrBlank()) {
                    Text(
                        text = stringResource(R.string.profile_ip_area_label, ProfileLocationCatalog.localizeIpArea(profile.ipArea, locale)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        state.socialMe?.let { socialMe ->
            StatStrip(
                modifier = Modifier.testTag("profile.social.stats"),
                emphasizeFirst = false,
                items = listOf(
                    StatItem(
                        label = stringResource(R.string.social_stat_following),
                        value = socialMe.followingCount.toString(),
                        onClick = { onOpenRelations(socialMe.id, SocialRelationshipKind.FOLLOWING) },
                        testTag = "profile.social.following"
                    ),
                    StatItem(
                        label = stringResource(R.string.social_stat_followers),
                        value = socialMe.followerCount.toString(),
                        onClick = { onOpenRelations(socialMe.id, SocialRelationshipKind.FOLLOWERS) },
                        testTag = "profile.social.followers"
                    ),
                    StatItem(
                        label = stringResource(R.string.social_stat_friends),
                        value = socialMe.friendCount.toString(),
                        onClick = { onOpenRelations(socialMe.id, SocialRelationshipKind.FRIENDS) },
                        testTag = "profile.social.friends"
                    )
                )
            )
        }

        SectionHeader(title = stringResource(R.string.profile_account_data_title))
        ListGroup {
            ProfileSummaryContent(
                profile = profile,
                profileOptions = profileOptions,
                isSaving = state.isSaving,
                onEditNickname = {
                    textEditorValue = profile.nickname.orEmpty()
                    activeTextEditor = ProfileTextEditorField.Nickname
                },
                onEditBirthday = { showBirthdayPicker = true },
                onEditCollege = { activeSelectionEditor = ProfileSelectionEditorField.College },
                onEditMajor = {
                    if (!canSelectMajor) {
                        Toast.makeText(context, selectCollegeFirstText, Toast.LENGTH_LONG).show()
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
        }

        state.saveError?.takeIf(String::isNotBlank)?.let { errorMessage ->
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 4.dp)
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
            currentSelection = when (field) {
                ProfileLocationField.Location -> profile.locationSelection
                ProfileLocationField.Hometown -> profile.hometownSelection
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
                ProfileSelectionEditorField.College -> profileOptions.facultyOptions
                ProfileSelectionEditorField.Major -> profileOptions.majorOptionsFor(currentCollege)
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
