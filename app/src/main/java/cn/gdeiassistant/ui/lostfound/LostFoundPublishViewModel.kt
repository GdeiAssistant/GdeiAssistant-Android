package cn.gdeiassistant.ui.lostfound

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.R
import cn.gdeiassistant.data.LostFoundRepository
import cn.gdeiassistant.model.LostFoundDraft
import cn.gdeiassistant.model.LostFoundItemTypeOption
import cn.gdeiassistant.model.LostFoundType
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

data class LostFoundPublishUiState(
    val itemTypeOptions: List<LostFoundItemTypeOption> = emptyList(),
    val isSubmitting: Boolean = false,
    val error: String? = null
)

sealed interface LostFoundPublishEvent {
    data class ShowMessage(val message: String) : LostFoundPublishEvent
    data object Submitted : LostFoundPublishEvent
}

@HiltViewModel
class LostFoundPublishViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lostFoundRepository: LostFoundRepository
) : ViewModel() {

    private val _state = MutableStateFlow(
        LostFoundPublishUiState(itemTypeOptions = lostFoundRepository.currentItemTypeOptions())
    )
    val state: StateFlow<LostFoundPublishUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<LostFoundPublishEvent>()
    val events: SharedFlow<LostFoundPublishEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            val itemTypeOptions = lostFoundRepository.getItemTypeOptions()
                .getOrDefault(_state.value.itemTypeOptions.ifEmpty { lostFoundRepository.currentItemTypeOptions() })
            _state.update { it.copy(itemTypeOptions = itemTypeOptions) }
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
        phone: String,
        images: List<Uri>
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
            images.isEmpty() -> emitMessage(context.getString(R.string.lost_found_publish_images_required))
            else -> {
                viewModelScope.launch {
                    _state.update { it.copy(isSubmitting = true, error = null) }
                    lostFoundRepository.publish(
                        draft = LostFoundDraft(
                            title = normalizedTitle,
                            type = type,
                            itemTypeId = itemTypeId,
                            description = normalizedDescription,
                            location = normalizedLocation,
                            qq = normalizedQq.ifBlank { null },
                            wechat = normalizedWechat.ifBlank { null },
                            phone = normalizedPhone.ifBlank { null }
                        ),
                        images = images
                    ).onSuccess {
                        _state.update { it.copy(isSubmitting = false, error = null) }
                        _events.emit(LostFoundPublishEvent.ShowMessage(context.getString(R.string.lost_found_publish_success)))
                        _events.emit(LostFoundPublishEvent.Submitted)
                    }.onFailure { error ->
                        _state.update { it.copy(isSubmitting = false, error = error.message) }
                        emitMessage(error.message ?: context.getString(R.string.lost_found_publish_failed))
                    }
                }
            }
        }
    }

    private fun emitMessage(message: String) {
        viewModelScope.launch {
            _events.emit(LostFoundPublishEvent.ShowMessage(message))
        }
    }
}
