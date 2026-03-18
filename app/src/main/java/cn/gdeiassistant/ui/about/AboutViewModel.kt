package cn.gdeiassistant.ui.about

import android.content.Context
import androidx.core.content.pm.PackageInfoCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.gdeiassistant.R
import cn.gdeiassistant.data.SettingsRepository
import cn.gdeiassistant.data.UpgradeRepository
import cn.gdeiassistant.model.CheckUpgradeResult
import cn.gdeiassistant.ui.util.UiText
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
    val isMockModeEnabled: Boolean = true,
    val isCheckingUpdate: Boolean = false,
    val latestVersion: CheckUpgradeResult? = null,
    val updateAvailable: Boolean = false,
    val status: UiText? = null,
    val error: UiText? = null
) {
    val hasDownloadUrl: Boolean
        get() = !latestVersion?.downloadURL.isNullOrBlank()
}

@HiltViewModel
class AboutViewModel @Inject constructor(
    private val upgradeRepository: UpgradeRepository,
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

    fun checkForUpdate() {
        viewModelScope.launch {
            _state.update {
                it.copy(isCheckingUpdate = true, error = null, status = null)
            }

            upgradeRepository.checkUpgrade().fold(
                onSuccess = { result ->
                    when {
                        result == null || result.versionCode == null -> {
                            _state.update {
                                it.copy(
                                    isCheckingUpdate = false,
                                    latestVersion = null,
                                    updateAvailable = false,
                                    status = UiText.StringResource(R.string.about_update_none)
                                )
                            }
                        }

                        result.versionCode > _state.value.currentVersionCode -> {
                            _state.update {
                                it.copy(
                                    isCheckingUpdate = false,
                                    latestVersion = result,
                                    updateAvailable = true,
                                    status = UiText.StringResource(R.string.about_update_available)
                                )
                            }
                        }

                        else -> {
                            _state.update {
                                it.copy(
                                    isCheckingUpdate = false,
                                    latestVersion = result,
                                    updateAvailable = false,
                                    status = UiText.StringResource(R.string.about_latest_version)
                                )
                            }
                        }
                    }
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(
                            isCheckingUpdate = false,
                            error = e.message?.takeIf { message -> message.isNotBlank() }?.let { message ->
                                UiText.DynamicString(message)
                            } ?: UiText.StringResource(R.string.load_failed)
                        )
                    }
                }
            )
        }
    }

    fun setMockModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            runCatching {
                settingsRepository.setMockModeEnabled(enabled)
            }.onSuccess {
                _events.emit(context.getString(R.string.about_mock_mode_toast))
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        error = error.message?.takeIf(String::isNotBlank)?.let(UiText::DynamicString)
                            ?: UiText.StringResource(R.string.load_failed)
                    )
                }
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
