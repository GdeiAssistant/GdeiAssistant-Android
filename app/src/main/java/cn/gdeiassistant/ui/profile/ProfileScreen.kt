package cn.gdeiassistant.ui.profile

import android.widget.Toast
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Feedback
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonSearch
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cn.gdeiassistant.R
import cn.gdeiassistant.ui.components.LazyScreen
import cn.gdeiassistant.ui.components.StatusBanner
import cn.gdeiassistant.ui.navigation.Routes
import kotlinx.coroutines.flow.collectLatest

internal enum class ProfileTextEditorField {
    Nickname,
    Bio
}

internal enum class ProfileSelectionEditorField {
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
    val socialActionsTitle = stringResource(R.string.social_profile_actions_title)
    val socialSearchTitle = stringResource(R.string.social_search_title)
    val socialConversationsTitle = stringResource(R.string.social_conversations_title)
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
        ProfileMenuItem(Icons.Rounded.Palette, stringResource(R.string.appearance_title)) {
            navController.navigate(Routes.APPEARANCE)
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
                        onOpenRelations = { userId, kind ->
                            navController.navigate(Routes.socialRelations(userId, kind))
                        },
                        onSaveNickname = viewModel::saveNickname,
                        onSaveBirthday = viewModel::saveBirthday,
                        onSaveCollege = viewModel::saveCollege,
                        onSaveMajor = viewModel::saveMajor,
                        onSaveEnrollment = viewModel::saveEnrollment,
                        onSaveBio = viewModel::saveBio,
                        onSaveLocation = viewModel::saveLocation
                    )
                }
                state.socialMe?.let {
                    profileMenuSection(
                        title = socialActionsTitle,
                        items = listOf(
                            ProfileMenuItem(
                                Icons.Rounded.PersonSearch,
                                socialSearchTitle
                            ) { navController.navigate(Routes.SOCIAL_SEARCH) },
                            ProfileMenuItem(
                                Icons.Rounded.Chat,
                                socialConversationsTitle
                            ) { navController.navigate(Routes.SOCIAL_CONVERSATIONS) }
                        )
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





































internal data class ProfileMenuItem(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit
)
