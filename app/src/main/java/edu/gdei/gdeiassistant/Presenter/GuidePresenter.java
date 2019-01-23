package edu.gdei.gdeiassistant.Presenter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.taobao.sophix.SophixManager;

import java.lang.ref.WeakReference;

import edu.gdei.gdeiassistant.Activity.GuideActivity;
import edu.gdei.gdeiassistant.Activity.LoginActivity;
import edu.gdei.gdeiassistant.Activity.MainActivity;
import edu.gdei.gdeiassistant.Application.GdeiAssistantApplication;
import edu.gdei.gdeiassistant.Constant.ActivityRequestCodeConstant;
import edu.gdei.gdeiassistant.Constant.RequestConstant;
import edu.gdei.gdeiassistant.Model.GuideModel;
import edu.gdei.gdeiassistant.Service.PatchService;
import edu.gdei.gdeiassistant.Service.UpgradeService;
import edu.gdei.gdeiassistant.Tools.NetWorkUtils;
import edu.gdei.gdeiassistant.Tools.StringUtils;
import edu.gdei.gdeiassistant.Tools.TokenUtils;

public class GuidePresenter {

    private GuideActivity guideActivity;

    private GuideModel guideModel;

    private BroadcastReceiver receiver;

    private GuideActivityHandler guideActivityHandler;

    private Handler delayHandler = new Handler();

    public GuidePresenter(GuideActivity guideActivity) {
        this.guideActivity = guideActivity;
        this.guideModel = new GuideModel();
        this.guideActivityHandler = new GuideActivityHandler(guideActivity);
        Init();
    }

    public static class GuideActivityHandler extends Handler {

        private GuideActivity guideActivity;

        GuideActivityHandler(GuideActivity guideActivity) {
            this.guideActivity = new WeakReference<>(guideActivity).get();
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case RequestConstant.REQUEST_TIMEOUT:
                    //刷新令牌超时，跳转到登录界面
                    guideActivity.ShowToast("刷新登录凭证超时，请尝试重新登录");
                    guideActivity.startActivity(new Intent(guideActivity, LoginActivity.class));
                    guideActivity.finish();
                    break;

                case RequestConstant.REQUEST_SUCCESS:
                    //刷新令牌成功
                    Bundle bundle = msg.getData();
                    String accessToken = bundle.getString("AccessToken");
                    String refreshToken = bundle.getString("RefreshToken");
                    ((GdeiAssistantApplication) guideActivity.getApplication()).setToken(accessToken);
                    TokenUtils.SaveUserToken(accessToken, refreshToken, guideActivity.getApplicationContext());
                    guideActivity.startActivity(new Intent(guideActivity, MainActivity.class));
                    guideActivity.finish();
                    break;

                case RequestConstant.REQUEST_FAILURE:
                    //刷新令牌失败
                    bundle = msg.getData();
                    String message = bundle.getString("Message");
                    guideActivity.ShowToast("刷新登录凭证失败，错误信息为" + message + "。请尝试重新登录");
                    guideActivity.startActivity(new Intent(guideActivity, LoginActivity.class));
                    guideActivity.finish();
                    break;

                case RequestConstant.SERVER_ERROR:
                    //服务器异常
                    guideActivity.ShowToast("服务暂不可用，请尝试重新登录");
                    guideActivity.startActivity(new Intent(guideActivity, LoginActivity.class));
                    guideActivity.finish();
                    break;

                case RequestConstant.UNKNOWN_ERROR:
                    //未知异常
                    guideActivity.ShowToast("出现未知异常，请尝试重新登录");
                    guideActivity.startActivity(new Intent(guideActivity, LoginActivity.class));
                    guideActivity.finish();
                    break;
            }
        }
    }

    private void Init() {
        //注册监听广播
        if (receiver == null) {
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction() != null) {
                        if (intent.getAction().equals("edu.gdei.gdeiassistant.PATCH_RELAUNCH")) {
                            //显示补丁冷启动提示
                            guideActivity.ShowPatchRelaunchTip();
                            //停止延时跳转，处理补丁冷启动任务
                            delayHandler.removeCallbacksAndMessages(null);
                        } else if (intent.getAction().equals("edu.gdei.gdeiassistant.CHECK_UPGRADE")) {
                            //停止延时跳转，处理应用更新任务
                            delayHandler.removeCallbacksAndMessages(null);
                            String versionCodeName = intent.getStringExtra("VersionCodeName");
                            String versionInfo = intent.getStringExtra("VersionInfo");
                            String downloadURL = intent.getStringExtra("DownloadURL");
                            String fileSize = intent.getStringExtra("FileSize");
                            //显示更新提示
                            guideActivity.ShowUpgradeTip(versionCodeName, versionInfo, downloadURL, fileSize);
                        }
                    }
                }
            };
            //注册广播监听
            guideActivity.registerReceiver(receiver, new IntentFilter("edu.gdei.gdeiassistant.PATCH_RELAUNCH"));
            guideActivity.registerReceiver(receiver, new IntentFilter("edu.gdei.gdeiassistant.CHECK_UPGRADE"));
        }
        //WIFI状态下启动检测更新服务
        if (NetWorkUtils.IsUsingWifi(guideActivity.getApplicationContext())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                guideActivity.startForegroundService(new Intent(guideActivity, UpgradeService.class));
            } else {
                guideActivity.startService(new Intent(guideActivity, UpgradeService.class));
            }
        }
        //启动补丁更新服务
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            guideActivity.startForegroundService(new Intent(guideActivity, PatchService.class));
        } else {
            guideActivity.startService(new Intent(guideActivity, PatchService.class));
        }
        //检查用户登录状态
        String token = TokenUtils.GetUserAccessToken(guideActivity.getApplicationContext());
        if (StringUtils.isNotBlank(token)) {
            //校验令牌时间戳有效性
            if (TokenUtils.ValidateTokenTimestamp(token)) {
                //获取权限令牌中保存的用户名
                String username = TokenUtils.GetAccessTokenUsername(token);
                if (StringUtils.isNotBlank(username)) {
                    //缓存令牌和用户名到应用数据
                    ((GdeiAssistantApplication) guideActivity.getApplication()).setToken(token);
                    ((GdeiAssistantApplication) guideActivity.getApplication()).setUsername(username);
                    //延时跳转到主页面
                    Intent intent = new Intent(guideActivity, MainActivity.class);
                    StartActivityOnDelayed(intent);
                } else {
                    //显示解析令牌信息错误的提示
                    guideActivity.ShowToast("解析用户登录凭证出现错误，请尝试重新登录");
                    //延时跳转到登录界面
                    Intent intent = new Intent(guideActivity, LoginActivity.class);
                    StartActivityOnDelayed(intent);
                }
            } else {
                //刷新令牌
                String refreshToken = TokenUtils.GetUserRefreshToken(guideActivity.getApplicationContext());
                guideModel.RefreshToken(guideActivityHandler, refreshToken, guideActivity.getApplicationContext());
            }
        } else {
            //延时跳转到登录界面
            Intent intent = new Intent(guideActivity, LoginActivity.class);
            StartActivityOnDelayed(intent);
        }
    }

    /**
     * 移除所有的回调和消息，防止内存泄露
     */
    public void RemoveCallBacksAndMessages() {
        guideActivityHandler.removeCallbacksAndMessages(null);
        delayHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 注销广播
     */
    public void UnregisterReceiver() {
        if (receiver != null) {
            guideActivity.unregisterReceiver(receiver);
        }
    }

    /**
     * 下载新版本
     */
    public void DownLoadNewVersion(String downloadURL) {
        //使用浏览器下载新版本应用，并接收返回结果
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.setData(Uri.parse(downloadURL));
        intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
        guideActivity.startActivityForResult(Intent.createChooser(intent, "请选择浏览器"), ActivityRequestCodeConstant.BROWSER_UPDATE_REQUEST_CODE);
    }

    /**
     * 处理完更新提示信息后，进行页面的跳转
     */
    public void SwitchPageAfterHandledUpdateTip() {
        //检查用户登录状态
        String token = TokenUtils.GetUserAccessToken(guideActivity.getApplicationContext());
        if (StringUtils.isNotBlank(token)) {
            //校验令牌时间戳有效性
            if (TokenUtils.ValidateTokenTimestamp(token)) {
                //缓存令牌到应用数据
                ((GdeiAssistantApplication) guideActivity.getApplication()).setToken(token);
                //跳转到主页面
                guideActivity.startActivity(new Intent(guideActivity, MainActivity.class));
                guideActivity.finish();
            } else {
                //刷新令牌
                String refreshToken = TokenUtils.GetUserRefreshToken(guideActivity.getApplicationContext());
                guideModel.RefreshToken(guideActivityHandler, refreshToken, guideActivity.getApplicationContext());
            }
        } else {
            //跳转到登录界面
            guideActivity.startActivity(new Intent(guideActivity, LoginActivity.class));
            guideActivity.finish();
        }
    }

    /**
     * 结束应用程序使补丁生效
     */
    public void PatchRelaunchAndStopProcess() {
        SophixManager.getInstance().killProcessSafely();
    }

    /**
     * 延时跳转Activity
     *
     * @param intent
     */
    private void StartActivityOnDelayed(final Intent intent) {
        delayHandler.postDelayed(new Runnable() {
            public void run() {
                guideActivity.startActivity(intent);
                guideActivity.finish();
            }
        }, 1500);
    }

}
