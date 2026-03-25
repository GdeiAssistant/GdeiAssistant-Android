package cn.gdeiassistant.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import cn.gdeiassistant.BuildConfig
import cn.gdeiassistant.network.NetworkEnvironment
import cn.gdeiassistant.model.AppLocaleSupport
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
    private val syncCachePreferences =
        appContext.getSharedPreferences(SYNC_CACHE_PREFERENCES_NAME, Context.MODE_PRIVATE)

    init {
        mockModeEnabledCache = syncCachePreferences.getBoolean(
            SYNC_CACHE_KEY_MOCK_MODE_ENABLED,
            DEFAULT_MOCK_MODE_ENABLED
        )
        networkEnvironmentCache = NetworkEnvironment.fromStorage(
            syncCachePreferences.getString(
                SYNC_CACHE_KEY_NETWORK_ENVIRONMENT,
                BuildConfig.DEFAULT_NETWORK_ENVIRONMENT
            )
        )
    }

    val isMockModeEnabled: Flow<Boolean> =
        appContext.settingsDataStore.data
            .map { preferences -> preferences[KEY_MOCK_MODE_ENABLED] ?: DEFAULT_MOCK_MODE_ENABLED }
            .distinctUntilChanged()
            .onEach { enabled -> persistMockModeCache(enabled) }

    val scheduleBackgroundUri: Flow<String?> =
        appContext.settingsDataStore.data
            .map { preferences -> preferences[KEY_SCHEDULE_BACKGROUND_URI] }
            .distinctUntilChanged()

    val networkEnvironment: Flow<NetworkEnvironment> =
        appContext.settingsDataStore.data
            .map { preferences ->
                NetworkEnvironment.fromStorage(
                    preferences[KEY_NETWORK_ENVIRONMENT] ?: BuildConfig.DEFAULT_NETWORK_ENVIRONMENT
                )
            }
            .distinctUntilChanged()
            .onEach(::persistNetworkEnvironmentCache)

    suspend fun setMockModeEnabled(enabled: Boolean) {
        appContext.settingsDataStore.edit { preferences ->
            preferences[KEY_MOCK_MODE_ENABLED] = enabled
        }
        persistMockModeCache(enabled)
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

    val locale: Flow<String> =
        appContext.settingsDataStore.data
            .map { preferences -> AppLocaleSupport.normalizeLocale(preferences[KEY_LOCALE] ?: detectSystemLocale()) }
            .distinctUntilChanged()

    suspend fun setLocale(locale: String) {
        appContext.settingsDataStore.edit { preferences ->
            preferences[KEY_LOCALE] = AppLocaleSupport.normalizeLocale(locale)
        }
    }

    private fun detectSystemLocale(): String {
        return AppLocaleSupport.detectSystemLocale()
    }

    suspend fun setNetworkEnvironment(environment: NetworkEnvironment) {
        appContext.settingsDataStore.edit { preferences ->
            preferences[KEY_NETWORK_ENVIRONMENT] = environment.storageValue
        }
        persistNetworkEnvironmentCache(environment)
    }

    suspend fun initializeSyncCache() {
        persistMockModeCache(isMockModeEnabled.first())
        persistNetworkEnvironmentCache(networkEnvironment.first())
    }

    companion object {
        private val KEY_MOCK_MODE_ENABLED = booleanPreferencesKey("is_mock_mode_enabled")
        private val KEY_SCHEDULE_BACKGROUND_URI = stringPreferencesKey("schedule_background_uri")
        private val KEY_LOCALE = stringPreferencesKey("locale")
        private val KEY_NETWORK_ENVIRONMENT = stringPreferencesKey("network_environment")
        private const val SYNC_CACHE_PREFERENCES_NAME = "developer_settings_sync_cache"
        private const val SYNC_CACHE_KEY_MOCK_MODE_ENABLED = "is_mock_mode_enabled"
        private const val SYNC_CACHE_KEY_NETWORK_ENVIRONMENT = "network_environment"
        private const val DEFAULT_MOCK_MODE_ENABLED = false

        @Volatile
        private var mockModeEnabledCache: Boolean = DEFAULT_MOCK_MODE_ENABLED

        @Volatile
        private var networkEnvironmentCache: NetworkEnvironment = NetworkEnvironment.default()

        fun isMockModeEnabledSync(): Boolean = mockModeEnabledCache

        fun currentNetworkEnvironmentSync(): NetworkEnvironment = networkEnvironmentCache
    }

    private fun persistMockModeCache(enabled: Boolean) {
        mockModeEnabledCache = enabled
        syncCachePreferences.edit()
            .putBoolean(SYNC_CACHE_KEY_MOCK_MODE_ENABLED, enabled)
            .apply()
    }

    private fun persistNetworkEnvironmentCache(environment: NetworkEnvironment) {
        networkEnvironmentCache = environment
        syncCachePreferences.edit()
            .putString(SYNC_CACHE_KEY_NETWORK_ENVIRONMENT, environment.storageValue)
            .apply()
    }
}
