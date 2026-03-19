package cn.gdeiassistant.ui.lostfound

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.R
import cn.gdeiassistant.data.LostFoundRepository
import cn.gdeiassistant.model.LostFoundEditableItem
import cn.gdeiassistant.model.LostFoundItemTypeOption
import cn.gdeiassistant.model.LostFoundType
import cn.gdeiassistant.model.LostFoundUpdateDraft
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

data class LostFoundEditUiState(
    val item: LostFoundEditableItem? = null,
    val itemTypeOptions: List<LostFoundItemTypeOption> = emptyList(),
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val error: String? = null
)

sealed interface LostFoundEditEvent {
    data class ShowMessage(val message: String) : LostFoundEditEvent
    data object Submitted : LostFoundEditEvent
}

@HiltViewModel
class LostFoundEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val lostFoundRepository: LostFoundRepository
) : ViewModel() {

    private val itemId: String = savedStateHandle.get<String>(Routes.LOST_FOUND_EDIT_ITEM_ID).orEmpty()

    private val _state = MutableStateFlow(
        LostFoundEditUiState(
            itemTypeOptions = lostFoundRepository.itemTypeOptions
        )
    )
    val state: StateFlow<LostFoundEditUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<LostFoundEditEvent>()
    val events: SharedFlow<LostFoundEditEvent> = _events.asSharedFlow()

    init {
        refresh()
    }

    fun refresh() {
        if (itemId.isBlank()) {
            _state.update {
                it.copy(
                    isLoading = false,
                    error = context.getString(R.string.lost_found_edit_missing)
                )
            }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            lostFoundRepository.getEditableItem(itemId)
                .onSuccess { item ->
                    _state.update { it.copy(item = item, isLoading = false, error = null) }
                }
                .onFailure { error ->
                    _state.update { it.copy(item = null, isLoading = false, error = error.message) }
                }
        }
    }

    fun submit(
        title: String,
        type: LostFoundType,
        itemTypeId: Int,
        description: String,
        location: String,
        qq: String,
        wechat: String,
        phone: String
    ) {
        val normalizedTitle = title.trim()
        val normalizedDescription = description.trim()
        val normalizedLocation = location.trim()
        val normalizedQq = qq.trim()
        val normalizedWechat = wechat.trim()
        val normalizedPhone = phone.trim()

        when {
            normalizedTitle.isBlank() -> emitMessage(context.getString(R.string.lost_found_publish_title_required))
            normalizedTitle.length > 25 -> emitMessage(context.getString(R.string.lost_found_publish_title_too_long))
            normalizedDescription.isBlank() -> emitMessage(context.getString(R.string.lost_found_publish_description_required))
            normalizedDescription.length > 100 -> emitMessage(context.getString(R.string.lost_found_publish_description_too_long))
            normalizedLocation.isBlank() -> emitMessage(context.getString(R.string.lost_found_publish_location_required))
            normalizedLocation.length > 30 -> emitMessage(context.getString(R.string.lost_found_publish_location_too_long))
            normalizedQq.isBlank() && normalizedWechat.isBlank() && normalizedPhone.isBlank() ->
                emitMessage(context.getString(R.string.lost_found_publish_contact_required))
            normalizedQq.length > 20 -> emitMessage(context.getString(R.string.lost_found_publish_qq_too_long))
            normalizedWechat.length > 20 -> emitMessage(context.getString(R.string.lost_found_publish_wechat_too_long))
            normalizedPhone.length > 11 -> emitMessage(context.getString(R.string.lost_found_publish_phone_too_long))
            else -> {
                viewModelScope.launch {
                    _state.update { it.copy(isSubmitting = true, error = null) }
                    lostFoundRepository.updateItem(
                        id = itemId,
                        draft = LostFoundUpdateDraft(
                            title = normalizedTitle,
                            type = type,
                            itemTypeId = itemTypeId,
                            description = normalizedDescription,
                            location = normalizedLocation,
                            qq = normalizedQq.ifBlank { null },
                            wechat = normalizedWechat.ifBlank { null },
                            phone = normalizedPhone.ifBlank { null }
                        )
                    ).onSuccess {
                        _state.update { it.copy(isSubmitting = false, error = null) }
                        _events.emit(LostFoundEditEvent.ShowMessage(context.getString(R.string.lost_found_edit_success)))
                        _events.emit(LostFoundEditEvent.Submitted)
                    }.onFailure { error ->
                        _state.update { it.copy(isSubmitting = false, error = error.message) }
                        emitMessage(error.message ?: context.getString(R.string.lost_found_edit_failed))
                    }
                }
            }
        }
    }

    private fun emitMessage(message: String) {
        viewModelScope.launch {
            _events.emit(LostFoundEditEvent.ShowMessage(message))
        }
    }
}
