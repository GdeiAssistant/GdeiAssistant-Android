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
            itemTypeOptions = lostFoundRepository.currentItemTypeOptions()
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
            val itemTypeOptions = lostFoundRepository.getItemTypeOptions()
                .getOrDefault(_state.value.itemTypeOptions.ifEmpty { lostFoundRepository.currentItemTypeOptions() })
            _state.update { it.copy(itemTypeOptions = itemTypeOptions, isLoading = true, error = null) }
            lostFoundRepository.getEditableItem(itemId)
                .onSuccess { item ->
                    _state.update { it.copy(item = item, itemTypeOptions = itemTypeOptions, isLoading = false, error = null) }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            item = null,
                            itemTypeOptions = itemTypeOptions,
                            isLoading = false,
                            error = error.message
                        )
                    }
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
        val form = LostFoundFormValidator.normalize(title, description, location, qq, wechat, phone)
        val normalizedTitle = form.title
        val normalizedDescription = form.description
        val normalizedLocation = form.location
        val normalizedQq = form.qq
        val normalizedWechat = form.wechat
        val normalizedPhone = form.phone
        val errorRes = LostFoundFormValidator.errorRes(form)

        when {
            errorRes != null -> emitMessage(context.getString(errorRes))
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
