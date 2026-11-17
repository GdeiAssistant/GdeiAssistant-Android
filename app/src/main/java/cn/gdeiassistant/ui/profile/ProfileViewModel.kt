package cn.gdeiassistant.ui.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.R
import cn.gdeiassistant.data.ProfileRepository
import cn.gdeiassistant.data.SessionManager
import cn.gdeiassistant.data.SocialRepository
import cn.gdeiassistant.data.SocialSessionCoordinator
import cn.gdeiassistant.model.ProfileFormSupport
import cn.gdeiassistant.model.ProfileLocationRegion
import cn.gdeiassistant.model.ProfileLocationSelection
import cn.gdeiassistant.model.ProfileOptions
import cn.gdeiassistant.model.ProfileUpdateRequest
import cn.gdeiassistant.model.SocialUser
import cn.gdeiassistant.model.UserProfileSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileDraftUiState(
    val nickname: String = "",
    val facultyCode: Int = 0,
    val majorCode: String? = null,
    val enrollment: Int? = null,
    val bio: String = "",
    val birthday: String = "",
    val location: String = "",
    val hometown: String = "",
    val locationSelection: ProfileLocationSelection? = null,
    val hometownSelection: ProfileLocationSelection? = null
) {
    val isFormValid: Boolean
        get() = nickname.trim().isNotEmpty()
}

data class ProfileUiState(
    val isLoading: Boolean = false,
    val profile: UserProfileSummary? = null,
    val socialMe: SocialUser? = null,
    val profileOptions: ProfileOptions = ProfileFormSupport.defaultOptions,
    val locationRegions: List<ProfileLocationRegion> = emptyList(),
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val draft: ProfileDraftUiState = ProfileDraftUiState(),
    val error: String? = null
)

sealed interface ProfileEvent {
    data class ShowMessage(val message: String) : ProfileEvent
    data object NavigateToLogin : ProfileEvent
}

enum class ProfileLocationField {
    Location,
    Hometown
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val socialRepository: SocialRepository,
    private val socialSessionCoordinator: SocialSessionCoordinator,
    private val sessionManager: SessionManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState(isLoading = true))
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ProfileEvent>()
    val events: SharedFlow<ProfileEvent> = _events.asSharedFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val profileDeferred = async { profileRepository.getProfile() }
            val optionsDeferred = async { profileRepository.getProfileOptions() }
            val locationDeferred = async { profileRepository.getLocationRegions() }
            val socialDeferred = async { socialRepository.getMe() }

            val profileResult = profileDeferred.await()
            val optionsResult = optionsDeferred.await()
            val locationResult = locationDeferred.await()
            val socialResult = socialDeferred.await()
            val fallbackUsername = sessionManager.currentUsername().orEmpty()
            val profile = profileResult.getOrNull() ?: UserProfileSummary(
                username = fallbackUsername.ifBlank { context.getString(R.string.profile_default_username) }
            )
            val options = optionsResult.getOrDefault(_state.value.profileOptions)

            _state.update { current ->
                current.copy(
                    isLoading = false,
                    profile = profile,
                    socialMe = socialResult.getOrNull() ?: current.socialMe,
                    profileOptions = options,
                    locationRegions = locationResult.getOrDefault(current.locationRegions),
                    draft = if (current.isEditing) {
                        sanitizeDraft(current.draft, options)
                    } else {
                        draftStateFrom(profile, options)
                    },
                    error = profileResult.exceptionOrNull()?.message
                )
            }
        }
    }

    fun startEditing() {
        val profile = _state.value.profile ?: return
        _state.update {
            it.copy(
                isEditing = true,
                isSaving = false,
                saveError = null,
                draft = draftStateFrom(profile, it.profileOptions)
            )
        }
    }

    fun cancelEditing() {
        val profile = _state.value.profile ?: return
        _state.update {
            it.copy(
                isEditing = false,
                isSaving = false,
                saveError = null,
                draft = draftStateFrom(profile, it.profileOptions)
            )
        }
    }

    fun updateNickname(value: String) {
        _state.update { it.copy(draft = it.draft.copy(nickname = value), saveError = null) }
    }

    fun updateBio(value: String) {
        _state.update { it.copy(draft = it.draft.copy(bio = value), saveError = null) }
    }

    fun updateBirthday(value: String) {
        _state.update { it.copy(draft = it.draft.copy(birthday = value), saveError = null) }
    }

    fun clearBirthday() {
        _state.update { it.copy(draft = it.draft.copy(birthday = ""), saveError = null) }
    }

    fun selectCollege(facultyCode: Int) {
        _state.update { current ->
            val resolvedFacultyCode = current.profileOptions.facultyOptionFor(facultyCode)?.code ?: 0
            val validMajors = current.profileOptions.majorOptionsFor(resolvedFacultyCode)
            current.copy(
                draft = current.draft.copy(
                    facultyCode = resolvedFacultyCode,
                    majorCode = current.draft.majorCode?.takeIf { code -> validMajors.any { it.code == code } }
                ),
                saveError = null
            )
        }
    }

    fun selectMajor(majorCode: String?) {
        _state.update { current ->
            if (!current.profileOptions.canSelectMajor(current.draft.facultyCode)) {
                current.copy(saveError = context.getString(R.string.profile_select_college_first))
            } else {
                val normalized = majorCode?.trim()
                    ?.takeIf { it.isNotEmpty() && it != ProfileFormSupport.UnselectedCode }
                current.copy(
                    draft = current.draft.copy(majorCode = normalized),
                    saveError = null
                )
            }
        }
    }

    fun selectEnrollment(enrollment: Int?) {
        _state.update {
            it.copy(
                draft = it.draft.copy(enrollment = enrollment),
                saveError = null
            )
        }
    }

    fun updateLocationSelection(field: ProfileLocationField, selection: ProfileLocationSelection?) {
        _state.update { current ->
            val draft = when (field) {
                ProfileLocationField.Location -> current.draft.copy(
                    location = selection?.displayName.orEmpty(),
                    locationSelection = selection
                )
                ProfileLocationField.Hometown -> current.draft.copy(
                    hometown = selection?.displayName.orEmpty(),
                    hometownSelection = selection
                )
            }
            current.copy(draft = draft, saveError = null)
        }
    }

    fun saveNickname(value: String) {
        saveDraftUpdate { draft, _ -> draft.copy(nickname = value) }
    }

    fun saveBio(value: String) {
        saveDraftUpdate { draft, _ -> draft.copy(bio = value) }
    }

    fun saveBirthday(value: String) {
        saveDraftUpdate { draft, _ -> draft.copy(birthday = value) }
    }

    fun saveCollege(facultyCode: Int) {
        saveDraftUpdate { draft, options ->
            val resolvedFacultyCode = options.facultyOptionFor(facultyCode)?.code ?: 0
            val validMajors = options.majorOptionsFor(resolvedFacultyCode)
            draft.copy(
                facultyCode = resolvedFacultyCode,
                majorCode = draft.majorCode?.takeIf { code -> validMajors.any { it.code == code } }
            )
        }
    }

    fun saveMajor(majorCode: String?) {
        saveDraftUpdate { draft, options ->
            if (!options.canSelectMajor(draft.facultyCode)) {
                throw IllegalStateException(context.getString(R.string.profile_select_college_first))
            }
            val normalized = majorCode?.trim()
                ?.takeIf { it.isNotEmpty() && it != ProfileFormSupport.UnselectedCode }
            draft.copy(majorCode = normalized)
        }
    }

    fun saveEnrollment(enrollment: Int?) {
        saveDraftUpdate { draft, _ ->
            draft.copy(enrollment = enrollment)
        }
    }

    fun saveLocation(field: ProfileLocationField, selection: ProfileLocationSelection?) {
        saveDraftUpdate { draft, _ ->
            when (field) {
                ProfileLocationField.Location -> draft.copy(
                    location = selection?.displayName.orEmpty(),
                    locationSelection = selection
                )
                ProfileLocationField.Hometown -> draft.copy(
                    hometown = selection?.displayName.orEmpty(),
                    hometownSelection = selection
                )
            }
        }
    }

    fun saveProfile() {
        val currentState = _state.value
        if (!currentState.draft.isFormValid) {
            _state.update {
                it.copy(saveError = context.getString(R.string.profile_fill_nickname))
            }
            return
        }
        if (currentState.isSaving) return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, saveError = null) }

            profileRepository.updateProfile(request = requestFrom(_state.value.draft)).onSuccess { updatedProfile ->
                _state.update {
                    it.copy(
                        profile = updatedProfile,
                        isEditing = false,
                        isSaving = false,
                        saveError = null,
                        draft = draftStateFrom(updatedProfile, it.profileOptions)
                    )
                }
                _events.emit(ProfileEvent.ShowMessage(context.getString(R.string.profile_info_saved)))
            }.onFailure { error ->
                _state.update {
                        it.copy(
                            isSaving = false,
                            saveError = error.message ?: context.getString(R.string.profile_info_save_failed)
                        )
                    }
            }
        }
    }

    private fun saveDraftUpdate(transform: (ProfileDraftUiState, ProfileOptions) -> ProfileDraftUiState) {
        val profile = _state.value.profile ?: return
        if (_state.value.isSaving) return

        val updatedDraft = runCatching {
            transform(draftStateFrom(profile, _state.value.profileOptions), _state.value.profileOptions)
        }.getOrElse { error ->
            _state.update {
                it.copy(
                    saveError = error.message ?: context.getString(R.string.profile_info_save_failed)
                )
            }
            return
        }

        if (!updatedDraft.isFormValid) {
            _state.update { it.copy(saveError = context.getString(R.string.profile_fill_nickname)) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, saveError = null, draft = updatedDraft) }

            profileRepository.updateProfile(request = requestFrom(updatedDraft))
                .onSuccess { updatedProfile ->
                    _state.update {
                        it.copy(
                            profile = updatedProfile,
                            isEditing = false,
                            isSaving = false,
                            saveError = null,
                            draft = draftStateFrom(updatedProfile, it.profileOptions)
                        )
                    }
                    _events.emit(ProfileEvent.ShowMessage(context.getString(R.string.profile_info_saved)))
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isSaving = false,
                            saveError = error.message ?: context.getString(R.string.profile_info_save_failed),
                            draft = draftStateFrom(profile, it.profileOptions)
                        )
                    }
                }
        }
    }

    fun logout() {
        socialSessionCoordinator.onLogout()
        sessionManager.clearTokens()
        viewModelScope.launch {
            _events.emit(ProfileEvent.NavigateToLogin)
        }
    }

    private fun requestFrom(draft: ProfileDraftUiState): ProfileUpdateRequest {
        return ProfileUpdateRequest(
            nickname = draft.nickname.trim(),
            facultyCode = draft.facultyCode,
            majorCode = draft.majorCode,
            enrollment = draft.enrollment,
            bio = draft.bio.trim(),
            birthday = draft.birthday.trim().takeIf(String::isNotEmpty),
            location = draft.locationSelection,
            hometown = draft.hometownSelection
        )
    }

    private fun draftStateFrom(profile: UserProfileSummary, options: ProfileOptions): ProfileDraftUiState {
        val resolvedFacultyCode = profile.facultyCode ?: 0
        val resolvedMajorCode = profile.majorCode
            ?.trim()
            ?.takeIf { code -> code.isNotEmpty() && code != ProfileFormSupport.UnselectedCode }
        return ProfileDraftUiState(
            nickname = profile.nickname.orEmpty(),
            facultyCode = resolvedFacultyCode,
            majorCode = resolvedMajorCode,
            enrollment = profile.enrollment?.trim()?.toIntOrNull(),
            bio = profile.introduction.orEmpty(),
            birthday = profile.birthday.orEmpty(),
            location = profile.location.orEmpty(),
            hometown = profile.hometown.orEmpty(),
            locationSelection = profile.locationSelection,
            hometownSelection = profile.hometownSelection
        )
    }

    private fun sanitizeDraft(draft: ProfileDraftUiState, options: ProfileOptions): ProfileDraftUiState {
        return draft.copy(
            majorCode = draft.majorCode?.trim()?.takeIf { code ->
                code.isNotEmpty() && code != ProfileFormSupport.UnselectedCode
            }
        )
    }
}
