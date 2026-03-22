package cn.gdeiassistant

import android.app.Application
import cn.gdeiassistant.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import cn.gdeiassistant.data.SettingsRepository
import cn.gdeiassistant.network.AppContextProvider
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class GdeiAssistantApplication : Application() {

    @Inject lateinit var settingsRepository: SettingsRepository
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        AppContextProvider.init(this)
        applicationScope.launch {
            settingsRepository.initializeSyncCache()
        }
        createServiceForegroundNotificationChannel()
    }

    fun createServiceForegroundNotificationChannel() {
        val channelId = "service"
        val channelName = getString(R.string.notification_channel_service)
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_NONE
        ).apply {
            enableLights(true)
            lightColor = Color.RED
            setShowBadge(true)
        }
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)
    }
}
