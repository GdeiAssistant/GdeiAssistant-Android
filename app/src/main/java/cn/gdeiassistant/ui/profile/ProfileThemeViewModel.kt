package cn.gdeiassistant.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileThemeUiState(
    val locale: String? = null
)

@HiltViewModel
class ProfileThemeViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileThemeUiState())
    val state: StateFlow<ProfileThemeUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.locale.collectLatest { locale ->
                _state.update { it.copy(locale = locale) }
            }
        }
    }

    fun setLocale(locale: String) {
        viewModelScope.launch {
            runCatching { settingsRepository.setLocale(locale) }
        }
    }
}
