package cn.gdeiassistant.ui.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.R
import cn.gdeiassistant.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileThemeUiState(
    val themeColor: String = SettingsRepository.DEFAULT_THEME_COLOR
)

sealed interface ProfileThemeEvent {
    data class ShowMessage(val message: String) : ProfileThemeEvent
}

@HiltViewModel
class ProfileThemeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileThemeUiState())
    val state: StateFlow<ProfileThemeUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ProfileThemeEvent>()
    val events: SharedFlow<ProfileThemeEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            settingsRepository.themeColor.collectLatest { themeColor ->
                _state.update { it.copy(themeColor = themeColor) }
            }
        }
    }

    fun selectTheme(themeKey: String) {
        if (_state.value.themeColor == themeKey) {
            return
        }
        viewModelScope.launch {
            runCatching { settingsRepository.setThemeColor(themeKey) }
                .onFailure { error ->
                    _events.emit(
                        ProfileThemeEvent.ShowMessage(
                            error.message ?: context.getString(R.string.profile_theme_update_failed)
                        )
                    )
                }
        }
    }
}
