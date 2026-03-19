package cn.gdeiassistant.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import cn.gdeiassistant.data.SessionManager
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import cn.gdeiassistant.data.SettingsRepository
import cn.gdeiassistant.ui.theme.GdeiAssistantTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        actionBar?.hide()
        setContent {
            val themeColor by settingsRepository.themeColor.collectAsState(
                initial = SettingsRepository.DEFAULT_THEME_COLOR
            )
            val hasActiveSession = remember { sessionManager.hasActiveSession() }
            GdeiAssistantTheme(themeColor = themeColor) {
                GdeiAssistantApp(hasActiveSession = hasActiveSession)
            }
        }
    }
}
