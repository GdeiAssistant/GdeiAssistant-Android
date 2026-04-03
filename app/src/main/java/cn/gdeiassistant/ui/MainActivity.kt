package cn.gdeiassistant.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import cn.gdeiassistant.data.SessionManager
import cn.gdeiassistant.data.SettingsRepository
import cn.gdeiassistant.data.UserPreferencesRepository
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import cn.gdeiassistant.model.AppLocaleSupport
import cn.gdeiassistant.ui.theme.GdeiAssistantTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_UI_USE_MOCK = "cn.gdeiassistant.extra.UI_USE_MOCK"
        const val EXTRA_UI_LOCALE = "cn.gdeiassistant.extra.UI_LOCALE"
        const val EXTRA_UI_CLEAR_SESSION = "cn.gdeiassistant.extra.UI_CLEAR_SESSION"
        const val EXTRA_UI_INITIAL_ROUTE = "cn.gdeiassistant.extra.UI_INITIAL_ROUTE"
        const val EXTRA_UI_SEED_TOKEN = "cn.gdeiassistant.extra.UI_SEED_TOKEN"
        const val EXTRA_UI_SEED_USERNAME = "cn.gdeiassistant.extra.UI_SEED_USERNAME"
    }

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        actionBar?.hide()

        val uiTestUseMock = intent?.getBooleanExtra(EXTRA_UI_USE_MOCK, false) == true
        val uiTestLocale = intent?.getStringExtra(EXTRA_UI_LOCALE)?.trim().orEmpty()
        val uiTestClearSession = intent?.getBooleanExtra(EXTRA_UI_CLEAR_SESSION, false) == true
        val uiTestInitialRoute = intent?.getStringExtra(EXTRA_UI_INITIAL_ROUTE)?.trim().orEmpty()
        val uiTestSeedToken = intent?.getStringExtra(EXTRA_UI_SEED_TOKEN)?.trim().orEmpty()
        val uiTestSeedUsername = intent?.getStringExtra(EXTRA_UI_SEED_USERNAME)?.trim().orEmpty()

        if (uiTestClearSession) {
            sessionManager.clearTokens()
        }
        if (uiTestSeedToken.isNotEmpty()) {
            sessionManager.saveToken(
                accessToken = uiTestSeedToken,
                username = uiTestSeedUsername.ifBlank { "gdeiassistant" }
            )
        }
        if (uiTestUseMock || uiTestLocale.isNotEmpty()) {
            runBlocking {
                if (uiTestUseMock) {
                    settingsRepository.setMockModeEnabled(true)
                }
                if (uiTestLocale.isNotEmpty()) {
                    settingsRepository.setLocale(uiTestLocale)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsRepository.locale.collect { locale ->
                    AppLocaleSupport.setCurrentLocale(locale)
                    val currentAppLocales = AppCompatDelegate.getApplicationLocales()
                    val newLocales = LocaleListCompat.forLanguageTags(locale)
                    if (currentAppLocales != newLocales) {
                        AppCompatDelegate.setApplicationLocales(newLocales)
                    }
                }
            }
        }

        setContent {
            val hasActiveSession = remember { sessionManager.hasActiveSession() }
            val themeMode by userPreferencesRepository.themeMode
                .collectAsStateWithLifecycle(initialValue = UserPreferencesRepository.THEME_SYSTEM)
            val fontScale by userPreferencesRepository.fontScale
                .collectAsStateWithLifecycle(initialValue = 1.0f)

            CompositionLocalProvider(
                LocalDensity provides Density(
                    density = LocalDensity.current.density,
                    fontScale = LocalDensity.current.fontScale * fontScale
                )
            ) {
                GdeiAssistantTheme(themeMode = themeMode) {
                    GdeiAssistantApp(
                        initialRoute = uiTestInitialRoute.ifBlank { null },
                        hasActiveSession = hasActiveSession
                    )
                }
            }
        }
    }
}
