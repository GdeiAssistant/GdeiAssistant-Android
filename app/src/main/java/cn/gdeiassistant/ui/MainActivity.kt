package cn.gdeiassistant.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import cn.gdeiassistant.data.SessionManager
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import cn.gdeiassistant.ui.theme.GdeiAssistantTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        actionBar?.hide()
        setContent {
            val hasActiveSession = remember { sessionManager.hasActiveSession() }
            GdeiAssistantTheme {
                GdeiAssistantApp(hasActiveSession = hasActiveSession)
            }
        }
    }
}
