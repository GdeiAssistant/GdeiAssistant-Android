package cn.gdeiassistant.service

import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.app.ServiceCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import cn.gdeiassistant.constant.NotificationIDConstant
import cn.gdeiassistant.network.api.UpgradeApi
import cn.gdeiassistant.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class UpgradeService : LifecycleService() {

    @Inject lateinit var upgradeApi: UpgradeApi

    override fun onCreate() { super.onCreate() }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, "service")
            .setSmallIcon(R.drawable.ic_notification_info)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            .setContentTitle(getString(R.string.check_upgrade))
            .setContentText(getString(R.string.checking_update))
            .setWhen(System.currentTimeMillis())
            .setAutoCancel(true)
            .build()
        startForeground(NotificationIDConstant.UPGRADE_NOTIFICATION_ID, notification)
        checkUpgrade()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
    }

    override fun onBind(intent: Intent): IBinder? = super.onBind(intent)

    private fun checkUpgrade() {
        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) { runCatching { upgradeApi.getUpgradeInfo() }.getOrNull() }
                if (result?.success == true && result.data != null) {
                    val currentVersionCode = PackageInfoCompat.getLongVersionCode(
                        packageManager.getPackageInfo(packageName, 0)
                    ).toInt()
                    val newCode = result.data!!.versionCode ?: 0
                    if (newCode > currentVersionCode) {
                        sendBroadcast(Intent("cn.gdeiassistant.CHECK_UPGRADE").apply {
                            result.data!!.versionCodeName?.let { putExtra("VersionCodeName", it) }
                            result.data!!.downloadURL?.let { putExtra("DownloadURL", it) }
                            result.data!!.versionInfo?.let { putExtra("VersionInfo", it) }
                            result.data!!.fileSize?.let { putExtra("FileSize", it) }
                        })
                    }
                }
            } catch (_: Exception) { }
            stopSelf()
        }
    }
}
