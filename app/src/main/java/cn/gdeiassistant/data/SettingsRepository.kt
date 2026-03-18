package cn.gdeiassistant.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore(name = "developer_settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext context: Context
) {

    private val appContext = context.applicationContext

    val isMockModeEnabled: Flow<Boolean> =
        appContext.settingsDataStore.data
            .map { preferences -> preferences[KEY_MOCK_MODE_ENABLED] ?: DEFAULT_MOCK_MODE_ENABLED }
            .distinctUntilChanged()
            .onEach { enabled -> mockModeEnabledCache = enabled }

    val scheduleBackgroundUri: Flow<String?> =
        appContext.settingsDataStore.data
            .map { preferences -> preferences[KEY_SCHEDULE_BACKGROUND_URI] }
            .distinctUntilChanged()

    val themeColor: Flow<String> =
        appContext.settingsDataStore.data
            .map { preferences -> preferences[KEY_THEME_COLOR] ?: DEFAULT_THEME_COLOR }
            .distinctUntilChanged()

    suspend fun setMockModeEnabled(enabled: Boolean) {
        appContext.settingsDataStore.edit { preferences ->
            preferences[KEY_MOCK_MODE_ENABLED] = enabled
        }
        mockModeEnabledCache = enabled
    }

    suspend fun setScheduleBackgroundUri(uri: String?) {
        appContext.settingsDataStore.edit { preferences ->
            if (uri.isNullOrBlank()) {
                preferences.remove(KEY_SCHEDULE_BACKGROUND_URI)
            } else {
                preferences[KEY_SCHEDULE_BACKGROUND_URI] = uri
            }
        }
    }

    suspend fun setThemeColor(color: String) {
        appContext.settingsDataStore.edit { preferences ->
            preferences[KEY_THEME_COLOR] = color
        }
    }

    suspend fun initializeMockModeCache() {
        mockModeEnabledCache = isMockModeEnabled.first()
    }

    companion object {
        private val KEY_MOCK_MODE_ENABLED = booleanPreferencesKey("is_mock_mode_enabled")
        private val KEY_SCHEDULE_BACKGROUND_URI = stringPreferencesKey("schedule_background_uri")
        private val KEY_THEME_COLOR = stringPreferencesKey("theme_color")
        private const val DEFAULT_MOCK_MODE_ENABLED = true
        const val DEFAULT_THEME_COLOR = "purple"

        @Volatile
        private var mockModeEnabledCache: Boolean = DEFAULT_MOCK_MODE_ENABLED

        fun isMockModeEnabledSync(): Boolean = mockModeEnabledCache
    }
}
