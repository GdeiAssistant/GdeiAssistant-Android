package cn.gdeiassistant

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import cn.gdeiassistant.data.SettingsRepository
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
        applicationScope.launch {
            settingsRepository.initializeSyncCache()
        }
        createServiceForegroundNotificationChannel()
    }

    fun createServiceForegroundNotificationChannel() {
        val channelId = "service"
        val channelName = "系统通知"
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
