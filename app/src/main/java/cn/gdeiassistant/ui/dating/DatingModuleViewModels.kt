package cn.gdeiassistant.ui.dating

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.R
import cn.gdeiassistant.data.DatingRepository
import cn.gdeiassistant.data.ProfileOptionsRepository
import cn.gdeiassistant.data.mapper.DatingDisplayMapper
import cn.gdeiassistant.model.DatingArea
import cn.gdeiassistant.model.DatingProfileCard
import cn.gdeiassistant.model.DatingProfileDetail
import cn.gdeiassistant.model.DatingPublishDraft
import cn.gdeiassistant.model.ProfileFormSupport
import cn.gdeiassistant.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DatingFeedUiState(
    val selectedArea: DatingArea = DatingArea.GIRL,
    val profiles: List<DatingProfileCard> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

data class DatingDetailUiState(
    val detail: DatingProfileDetail? = null,
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val error: String? = null
)

data class DatingPublishUiState(
    val facultyOptions: List<String> = ProfileFormSupport.defaultOptions.facultyOptions,
    val isSubmitting: Boolean = false,
    val error: String? = null
)

sealed interface DatingModuleEvent {
    data class ShowMessage(val message: String) : DatingModuleEvent
    data object Submitted : DatingModuleEvent
}

@HiltViewModel
class DatingFeedViewModel @Inject constructor(
    private val datingRepository: DatingRepository,
    private val datingDisplayMapper: DatingDisplayMapper
) : ViewModel() {

    private val _state = MutableStateFlow(DatingFeedUiState())
    val state: StateFlow<DatingFeedUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun selectArea(area: DatingArea) {
        if (_state.value.selectedArea == area) {
            return
        }
        _state.update { it.copy(selectedArea = area) }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            datingRepository.getProfiles(_state.value.selectedArea)
                .onSuccess { profiles ->
                    _state.update {
                        it.copy(
                            profiles = datingDisplayMapper.applyCardDefaults(profiles),
                            isLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            profiles = emptyList(),
                            isLoading = false,
                            error = error.message
                        )
                    }
                }
        }
    }
}

@HiltViewModel
class DatingDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val datingRepository: DatingRepository,
    private val datingDisplayMapper: DatingDisplayMapper
) : ViewModel() {

    private val profileId: String = savedStateHandle.get<String>(Routes.DATING_PROFILE_ID).orEmpty()

    private val _state = MutableStateFlow(DatingDetailUiState())
    val state: StateFlow<DatingDetailUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<DatingModuleEvent>()
    val events: SharedFlow<DatingModuleEvent> = _events.asSharedFlow()

    init {
        refresh()
    }

    fun refresh() {
        if (profileId.isBlank()) {
            _state.update {
                it.copy(
                    isLoading = false,
                    error = context.getString(R.string.dating_detail_missing)
                )
            }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            datingRepository.getProfileDetail(profileId)
                .onSuccess { detail ->
                    _state.update { it.copy(detail = datingDisplayMapper.applyDetailDefaults(detail), isLoading = false, error = null) }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            detail = null,
                            isLoading = false,
                            error = context.getString(R.string.dating_detail_load_failed)
                        )
                    }
                }
        }
    }

    fun submitPick(content: String) {
        val normalizedContent = content.trim()
        when {
            normalizedContent.isBlank() -> emitMessage(context.getString(R.string.dating_pick_required))
            normalizedContent.length > 50 -> emitMessage(context.getString(R.string.dating_pick_too_long))
            else -> {
                viewModelScope.launch {
                    _state.update { it.copy(isSubmitting = true, error = null) }
                    datingRepository.submitPick(profileId = profileId, content = normalizedContent)
                        .onSuccess {
                            _state.update { it.copy(isSubmitting = false, error = null) }
                            _events.emit(DatingModuleEvent.ShowMessage(context.getString(R.string.dating_pick_success)))
                            refresh()
                        }
                        .onFailure { _ ->
                            val msg = context.getString(R.string.dating_pick_failed)
                            _state.update { it.copy(isSubmitting = false, error = msg) }
                            emitMessage(msg)
                        }
                }
            }
        }
    }

    private fun emitMessage(message: String) {
        viewModelScope.launch {
            _events.emit(DatingModuleEvent.ShowMessage(message))
        }
    }
}

@HiltViewModel
class DatingPublishViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val datingRepository: DatingRepository,
    private val profileOptionsRepository: ProfileOptionsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DatingPublishUiState())
    val state: StateFlow<DatingPublishUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<DatingModuleEvent>()
    val events: SharedFlow<DatingModuleEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            val options = profileOptionsRepository.getOptions()
                .getOrDefault(profileOptionsRepository.currentOptions())
                .facultyOptions
            _state.update { it.copy(facultyOptions = options) }
        }
    }

    fun submit(
        nickname: String,
        grade: Int,
        area: DatingArea,
        faculty: String,
        hometown: String,
        qq: String,
        wechat: String,
        content: String,
        image: Uri?
    ) {
        val normalizedNickname = nickname.trim()
        val normalizedFaculty = ProfileFormSupport.normalizeSelection(faculty)
        val normalizedHometown = hometown.trim()
        val normalizedQq = qq.trim()
        val normalizedWechat = wechat.trim()
        val normalizedContent = content.trim()

        when {
            normalizedNickname.isBlank() -> emitMessage(context.getString(R.string.dating_publish_nickname_required))
            normalizedNickname.length > 15 -> emitMessage(context.getString(R.string.dating_publish_nickname_too_long))
            normalizedFaculty == ProfileFormSupport.UnselectedOption ->
                emitMessage(context.getString(R.string.dating_publish_faculty_required))
            normalizedHometown.isBlank() -> emitMessage(context.getString(R.string.dating_publish_hometown_required))
            normalizedHometown.length > 10 -> emitMessage(context.getString(R.string.dating_publish_hometown_too_long))
            normalizedContent.isBlank() -> emitMessage(context.getString(R.string.dating_publish_content_required))
            normalizedContent.length > 100 -> emitMessage(context.getString(R.string.dating_publish_content_too_long))
            normalizedQq.length > 15 -> emitMessage(context.getString(R.string.dating_publish_qq_too_long))
            normalizedWechat.length > 20 -> emitMessage(context.getString(R.string.dating_publish_wechat_too_long))
            else -> {
                viewModelScope.launch {
                    _state.update { it.copy(isSubmitting = true, error = null) }
                    datingRepository.publish(
                        draft = DatingPublishDraft(
                            nickname = normalizedNickname,
                            grade = grade,
                            area = area,
                            faculty = normalizedFaculty,
                            hometown = normalizedHometown,
                            qq = normalizedQq.ifBlank { null },
                            wechat = normalizedWechat.ifBlank { null },
                            content = normalizedContent
                        ),
                        image = image
                    ).onSuccess {
                        _state.update { it.copy(isSubmitting = false, error = null) }
                        _events.emit(DatingModuleEvent.ShowMessage(context.getString(R.string.dating_publish_success)))
                        _events.emit(DatingModuleEvent.Submitted)
                    }.onFailure { error ->
                        val msg = context.getString(R.string.dating_publish_failed)
                        _state.update { it.copy(isSubmitting = false, error = msg) }
                        emitMessage(msg)
                    }
                }
            }
        }
    }

    private fun emitMessage(message: String) {
        viewModelScope.launch {
            _events.emit(DatingModuleEvent.ShowMessage(message))
        }
    }
}
