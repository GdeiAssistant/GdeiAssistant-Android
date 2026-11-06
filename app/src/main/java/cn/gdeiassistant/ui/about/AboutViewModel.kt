package cn.gdeiassistant.ui.about

import android.content.Context
import androidx.core.content.pm.PackageInfoCompat
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

data class AboutUiState(
    val currentVersionName: String = "",
    val currentVersionCode: Long = 0L,
    val isMockModeEnabled: Boolean = false,
    val canUseDemoMode: Boolean = SettingsRepository.canUseDemoMode()
)

@HiltViewModel
class AboutViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(AboutUiState())
    val state: StateFlow<AboutUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    init {
        loadLocalVersionInfo()
        observeDeveloperSettings()
    }

    fun setMockModeEnabled(enabled: Boolean) {
        if (!SettingsRepository.canUseDemoMode()) return
        viewModelScope.launch {
            runCatching {
                settingsRepository.setMockModeEnabled(enabled)
            }.onSuccess {
                _events.emit(context.getString(R.string.about_mock_mode_toast))
            }.onFailure { error ->
                _events.emit(
                    error.message?.takeIf(String::isNotBlank)
                        ?: context.getString(R.string.load_failed)
                )
            }
        }
    }

    private fun loadLocalVersionInfo() {
        val packageManager = context.packageManager
        val packageName = context.packageName
        @Suppress("DEPRECATION")
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        _state.update {
            it.copy(
                currentVersionName = packageInfo.versionName.orEmpty(),
                currentVersionCode = PackageInfoCompat.getLongVersionCode(packageInfo)
            )
        }
    }

    private fun observeDeveloperSettings() {
        viewModelScope.launch {
            settingsRepository.isMockModeEnabled.collectLatest { enabled ->
                _state.update { it.copy(isMockModeEnabled = enabled) }
            }
        }
    }
}
