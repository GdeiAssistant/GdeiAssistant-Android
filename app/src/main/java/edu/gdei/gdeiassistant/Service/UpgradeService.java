package edu.gdei.gdeiassistant.Service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import edu.gdei.gdeiassistant.Constant.NotificationIDConstant;
import edu.gdei.gdeiassistant.NetWork.CheckUpgradeNetWork;
import edu.gdei.gdeiassistant.Pojo.JsonResult.DataJsonResult;
import edu.gdei.gdeiassistant.Pojo.Upgrade.CheckUpgradeResult;
import edu.gdei.gdeiassistant.R;

public class UpgradeService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //构建通知消息
        Notification notification = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "service");
            notification = notificationBuilder
                    .setSmallIcon(R.drawable.info_small_icon).setBadgeIconType(R.mipmap.ic_launcher).setContentTitle("检查软件更新").setContentText("正在检查应用更新...")
                    .setWhen(System.currentTimeMillis()).setAutoCancel(true)
                    .build();
        } else {
            Notification.Builder builder = new Notification.Builder(this);
            builder.setSmallIcon(R.mipmap.ic_launcher).setContentTitle("检查软件更新").setContentText("正在检查应用更新...")
                    .setWhen(System.currentTimeMillis()).setAutoCancel(true);
            notification = builder.build();
        }
        //显示新服务的用户可见通知
        startForeground(NotificationIDConstant.UPGRADE_NOTIFICATION_ID, notification);
        //检查应用更新
        CheckUpgrade();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    /**
     * 检查更新，若有可用更新，则发送广播通知监听的Activity
     */
    private void CheckUpgrade() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    DataJsonResult<CheckUpgradeResult> result = new CheckUpgradeNetWork().GetUpgradeInformation(getApplicationContext());
                    if (result.isSuccess()) {
                        int currentVersionCode = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0).versionCode;
                        if (result.getData().getVersionCode() > currentVersionCode) {
                            //有新版本,提示更新
                            Intent intent = new Intent("edu.gdei.gdeiassistant.CHECK_UPGRADE");
                            if (result.getData().getVersionCodeName() != null) {
                                intent.putExtra("VersionCodeName", result.getData().getVersionCodeName());
                            }
                            if (result.getData().getDownloadURL() != null) {
                                intent.putExtra("DownloadURL", result.getData().getDownloadURL());
                            }
                            if (result.getData().getVersionInfo() != null) {
                                intent.putExtra("VersionInfo", result.getData().getVersionInfo());
                            }
                            if (result.getData().getFileSize() != null) {
                                intent.putExtra("FileSize", result.getData().getFileSize());
                            }
                            //发送广播通知更新
                            sendBroadcast(intent);
                        }
                    }
                    //检查更新完成后关闭服务
                    stopSelf();
                } catch (Exception ignored) {

                }
            }
        }.start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
