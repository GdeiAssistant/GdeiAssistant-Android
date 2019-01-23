package edu.gdei.gdeiassistant.Presenter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.inputmethod.InputMethodManager;

import com.taobao.sophix.SophixManager;

import java.lang.ref.WeakReference;

import edu.gdei.gdeiassistant.Activity.LoginActivity;
import edu.gdei.gdeiassistant.Activity.MainActivity;
import edu.gdei.gdeiassistant.Activity.WebViewActivity;
import edu.gdei.gdeiassistant.Application.GdeiAssistantApplication;
import edu.gdei.gdeiassistant.Constant.ActivityRequestCodeConstant;
import edu.gdei.gdeiassistant.Constant.RequestConstant;
import edu.gdei.gdeiassistant.Model.LoginModel;
import edu.gdei.gdeiassistant.Tools.StringUtils;
import edu.gdei.gdeiassistant.Tools.TokenUtils;

public class LoginPresenter {

    private BroadcastReceiver receiver;

    private LoginActivity loginActivity;

    private LoginActivityHandler loginActivityHandler;

    private LoginModel loginModel;

    /**
     * 移除所有的回调和消息，防止内存泄露
     */
    public void RemoveCallBacksAndMessages() {
        loginActivityHandler.removeCallbacksAndMessages(null);
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
        loginActivity.startActivityForResult(Intent.createChooser(intent, "请选择浏览器"), ActivityRequestCodeConstant.BROWSER_UPDATE_REQUEST_CODE);
    }

    private static class LoginActivityHandler extends Handler {

        private LoginActivity loginActivity;

        LoginActivityHandler(LoginActivity loginActivity) {
            this.loginActivity = new WeakReference<>(loginActivity).get();
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case RequestConstant.SHOW_PROGRESS:
                    //显示进度条
                    loginActivity.ShowProgressDialog();
                    break;

                case RequestConstant.REQUEST_FAILURE:
                    //隐藏进度条
                    loginActivity.HideProgressDialog();
                    //显示错误信息
                    loginActivity.ShowToast(msg.getData().getString("Message"));
                    break;

                case RequestConstant.SERVER_ERROR:
                    //隐藏进度条
                    loginActivity.HideProgressDialog();
                    //显示错误信息
                    loginActivity.ShowToast("服务暂不可用，请稍后再试");
                    break;

                case RequestConstant.REQUEST_TIMEOUT:
                    //隐藏进度条
                    loginActivity.HideProgressDialog();
                    //显示错误信息
                    loginActivity.ShowToast("网络连接超时，请重试");
                    break;

                case RequestConstant.UNKNOWN_ERROR:
                    //隐藏进度条
                    loginActivity.HideProgressDialog();
                    //显示错误信息
                    loginActivity.ShowToast("出现未知异常，请联系管理员");
                    break;

                case RequestConstant.REQUEST_SUCCESS:
                    //登录成功，解析并缓存用户登录凭证
                    GdeiAssistantApplication application = (GdeiAssistantApplication) loginActivity.getApplication();
                    String accessToken = msg.getData().getString("AccessToken");
                    String refreshToken = msg.getData().getString("RefreshToken");
                    String username = TokenUtils.GetAccessTokenUsername(accessToken);
                    if (StringUtils.isNotBlank(username)) {
                        application.setToken(accessToken);
                        application.setUsername(username);
                        //保存令牌到SharedPreferences
                        TokenUtils.SaveUserToken(accessToken, refreshToken, loginActivity.getApplicationContext());
                        //跳转到应用主界面
                        loginActivity.startActivity(new Intent(loginActivity, MainActivity.class));
                        //隐藏进度条
                        loginActivity.HideProgressDialog();
                        loginActivity.finish();
                    } else {
                        //隐藏进度条
                        loginActivity.HideProgressDialog();
                        //显示令牌信息解析出错的提示
                        loginActivity.ShowToast("解析用户登录凭证出现错误，请尝试重新登录");
                    }
                    break;
            }
        }

    }

    public LoginPresenter(final LoginActivity loginActivity) {
        this.loginActivity = loginActivity;
        this.loginModel = new LoginModel();
        this.loginActivityHandler = new LoginActivityHandler(loginActivity);
        Init();
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
                            loginActivity.ShowPatchRelaunchTip();
                        } else if (intent.getAction().equals("edu.gdei.gdeiassistant.CHECK_UPGRADE")) {
                            String versionCodeName = intent.getStringExtra("VersionCodeName");
                            String versionInfo = intent.getStringExtra("VersionInfo");
                            String downloadURL = intent.getStringExtra("DownloadURL");
                            String fileSize = intent.getStringExtra("FileSize");
                            //显示更新提示
                            loginActivity.ShowUpgradeTip(versionCodeName, versionInfo, downloadURL, fileSize);
                        }
                    }
                }
            };
            //注册广播监听
            loginActivity.registerReceiver(receiver, new IntentFilter("edu.gdei.gdeiassistant.PATCH_RELAUNCH"));
            loginActivity.registerReceiver(receiver, new IntentFilter("edu.gdei.gdeiassistant.CHECK_UPGRADE"));
        }
    }

    /**
     * 注销广播
     */
    public void UnregisterReceiver() {
        if (receiver != null) {
            loginActivity.unregisterReceiver(receiver);
        }
    }

    /**
     * 结束应用程序使补丁生效
     */
    public void PatchRelaunchAndStopProcess() {
        SophixManager.getInstance().killProcessSafely();
    }

    /**
     * 显示用户协议
     */
    public void ShowAgreement() {
        Intent intent = new Intent(loginActivity, WebViewActivity.class);
        intent.putExtra("title", "易小助用户协议");
        intent.putExtra("url", "https://www.gdeiassistant.cn/agreement");
        loginActivity.startActivity(intent);
    }

    /**
     * 显示隐私政策
     */
    public void ShowPrivacyPolicy() {
        Intent intent = new Intent(loginActivity, WebViewActivity.class);
        intent.putExtra("title", "易小助隐私政策");
        intent.putExtra("url", "https://www.gdeiassistant.cn/policy/privacy");
        loginActivity.startActivity(intent);
    }

    /**
     * 用户登录操作
     *
     * @param username
     * @param password
     */
    public void UserLogin(final String username, final String password) {
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            loginActivity.ShowToast("账户信息不能为空");
        } else {
            InputMethodManager inputMethodManager = (InputMethodManager) loginActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                //收起虚拟键盘
                inputMethodManager.hideSoftInputFromWindow(loginActivity.getWindow().getDecorView().getWindowToken(), 0);
            }
            loginModel.UserLogin(loginActivityHandler, username, password, loginActivity.getApplicationContext());
        }
    }
}
