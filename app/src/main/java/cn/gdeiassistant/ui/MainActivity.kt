package cn.gdeiassistant.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.gdeiassistant.data.SessionManager
import cn.gdeiassistant.data.UserPreferencesRepository
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import cn.gdeiassistant.ui.theme.GdeiAssistantTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        actionBar?.hide()
        setContent {
            val hasActiveSession = remember { sessionManager.hasActiveSession() }
            val themeMode by userPreferencesRepository.themeMode
                .collectAsStateWithLifecycle(initialValue = UserPreferencesRepository.THEME_SYSTEM)
            val fontScale by userPreferencesRepository.fontScale
                .collectAsStateWithLifecycle(initialValue = 1.0f)

            LaunchedEffect(themeMode) {
                AppCompatDelegate.setDefaultNightMode(
                    when (themeMode) {
                        UserPreferencesRepository.THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                        UserPreferencesRepository.THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
                        else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    }
                )
            }

            CompositionLocalProvider(
                LocalDensity provides Density(
                    density = LocalDensity.current.density,
                    fontScale = LocalDensity.current.fontScale * fontScale
                )
            ) {
                GdeiAssistantTheme(themeMode = themeMode) {
                    GdeiAssistantApp(hasActiveSession = hasActiveSession)
                }
            }
        }
    }
}
