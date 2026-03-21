package cn.gdeiassistant.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.data.SettingsRepository
import cn.gdeiassistant.data.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppearanceViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val themeMode: StateFlow<String> = userPreferencesRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferencesRepository.THEME_SYSTEM)

    val fontScaleStep: StateFlow<Int> = userPreferencesRepository.fontScaleStep
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    val locale: StateFlow<String> = settingsRepository.locale
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "zh-CN")

    fun setThemeMode(mode: String) {
        viewModelScope.launch { userPreferencesRepository.setThemeMode(mode) }
    }

    fun setFontScaleStep(step: Int) {
        viewModelScope.launch { userPreferencesRepository.setFontScaleStep(step) }
    }

    fun setLocale(locale: String) {
        viewModelScope.launch { settingsRepository.setLocale(locale) }
    }
}
