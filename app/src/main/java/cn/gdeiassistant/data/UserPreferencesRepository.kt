package cn.gdeiassistant.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_FONT_SCALE_STEP = intPreferencesKey("font_scale_step")

        const val THEME_SYSTEM = "system"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"

        val FONT_SCALE_VALUES = floatArrayOf(0.85f, 1.0f, 1.15f, 1.3f)
    }

    val themeMode: Flow<String> = context.userPreferencesDataStore.data
        .map { it[KEY_THEME_MODE] ?: THEME_SYSTEM }

    val fontScaleStep: Flow<Int> = context.userPreferencesDataStore.data
        .map { (it[KEY_FONT_SCALE_STEP] ?: 1).coerceIn(0, 3) }

    val fontScale: Flow<Float> = fontScaleStep
        .map { FONT_SCALE_VALUES[it] }

    suspend fun setThemeMode(mode: String) {
        require(mode in listOf(THEME_SYSTEM, THEME_LIGHT, THEME_DARK))
        context.userPreferencesDataStore.edit { it[KEY_THEME_MODE] = mode }
    }

    suspend fun setFontScaleStep(step: Int) {
        require(step in 0..3)
        context.userPreferencesDataStore.edit { it[KEY_FONT_SCALE_STEP] = step }
    }
}
