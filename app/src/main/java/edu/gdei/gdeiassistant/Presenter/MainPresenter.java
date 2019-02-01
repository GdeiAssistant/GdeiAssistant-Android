package edu.gdei.gdeiassistant.Presenter;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;

import com.taobao.sophix.SophixManager;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import edu.gdei.gdeiassistant.Activity.LoginActivity;
import edu.gdei.gdeiassistant.Activity.MainActivity;
import edu.gdei.gdeiassistant.Application.GdeiAssistantApplication;
import edu.gdei.gdeiassistant.Constant.ActivityRequestCodeConstant;
import edu.gdei.gdeiassistant.Constant.MainTagConstant;
import edu.gdei.gdeiassistant.Constant.PermissionRequestCodeConstant;
import edu.gdei.gdeiassistant.Constant.RequestConstant;
import edu.gdei.gdeiassistant.Model.BitmapFileModel;
import edu.gdei.gdeiassistant.Model.MainModel;
import edu.gdei.gdeiassistant.Pojo.Entity.Access;
import edu.gdei.gdeiassistant.Pojo.Entity.Profile;
import edu.gdei.gdeiassistant.Service.UpgradeService;
import edu.gdei.gdeiassistant.Tools.TokenUtils;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class MainPresenter {

    private BroadcastReceiver receiver;

    private MainActivity mainActivity;

    private MainActivityHandler mainActivityHandler;

    private MainModel mainModel;

    private String cachePath;

    /**
     * 移除所有的回调和消息，防止内存泄露
     */
    public void RemoveCallBacksAndMessages() {
        mainActivityHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 注销广播
     */
    public void UnregisterReceiver() {
        if (receiver != null) {
            mainActivity.unregisterReceiver(receiver);
        }
    }

    /**
     * 下载新版本
     *
     * @param downloadURL
     */
    public void DownLoadNewVersion(String downloadURL) {
        //使用浏览器下载新版本应用，并接收返回结果
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.setData(Uri.parse(downloadURL));
        intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
        mainActivity.startActivityForResult(Intent.createChooser(intent, "请选择浏览器"), ActivityRequestCodeConstant.BROWSER_UPDATE_REQUEST_CODE);
    }

    /**
     * 检查软件更新
     */
    public void CheckUpgrade() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mainActivity.startForegroundService(new Intent(mainActivity, UpgradeService.class));
        } else {
            mainActivity.startService(new Intent(mainActivity, UpgradeService.class));
        }
    }

    public static class MainActivityHandler extends Handler {

        private MainActivity mainActivity;

        MainActivityHandler(MainActivity mainActivity) {
            this.mainActivity = new WeakReference<>(mainActivity).get();
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case RequestConstant.REQUEST_FAILURE:
                    switch (msg.getData().getInt("Tag")) {
                        case MainTagConstant.DOWNLOAD_AVATAR:
                        case MainTagConstant.GET_USER_ACCESS:
                        case MainTagConstant.GET_USER_PROFILE:
                        case MainTagConstant.UPLOAD_AVATAR:
                            mainActivity.ShowToast(msg.getData().getString("Message"));
                            break;
                    }
                    break;

                case RequestConstant.REQUEST_SUCCESS:
                    switch (msg.getData().getInt("Tag")) {
                        case MainTagConstant.DOWNLOAD_AVATAR:
                        case MainTagConstant.UPLOAD_AVATAR:
                            //下载或上传头像成功
                            File file = new File(mainActivity.getApplicationContext().getCacheDir() + "/gdeiassistant/" + "avatar.jpg");
                            if (file.exists() && file.canRead()) {
                                Drawable drawable = Drawable.createFromPath(file.getPath());
                                mainActivity.SetAvatarImage(drawable);
                            } else {
                                mainActivity.ShowToast("获取头像失败，请检查是否正确开启存储权限");
                            }
                            break;

                        case MainTagConstant.GET_USER_PROFILE:
                            //获取用户资料
                            Profile profile = (Profile) msg.getData().getSerializable("Profile");
                            if (profile != null) {
                                mainActivity.UpdateMainNavigationWelcomeText(profile.getKickname());
                            }
                            break;

                        case MainTagConstant.GET_USER_ACCESS:
                            //加载用户权限并显示功能菜单
                            Access access = (Access) msg.getData().getSerializable("Access");
                            mainActivity.LoadAccessAndShowMenu(access);
                            break;
                    }
                    break;

                case RequestConstant.REQUEST_TIMEOUT:
                    //网络连接异常
                    switch (msg.getData().getInt("Tag")) {
                        case MainTagConstant.DOWNLOAD_AVATAR:
                        case MainTagConstant.UPLOAD_AVATAR:
                            mainActivity.ShowToast("网络连接超时，请重试");
                            break;

                        case MainTagConstant.GET_USER_PROFILE:
                            break;

                        case MainTagConstant.GET_USER_ACCESS:
                            mainActivity.ShowToast("网络异常，加载功能菜单失败，请尝试重新登录");
                            break;
                    }
                    break;

                case RequestConstant.SERVER_ERROR:
                    //服务器异常
                    switch (msg.getData().getInt("Tag")) {
                        case MainTagConstant.DOWNLOAD_AVATAR:
                        case MainTagConstant.UPLOAD_AVATAR:
                            mainActivity.ShowToast("服务暂不可用，请稍候再试");
                            break;

                        case MainTagConstant.GET_USER_PROFILE:
                            break;

                        case MainTagConstant.GET_USER_ACCESS:
                            mainActivity.ShowToast("服务器异常，加载功能菜单失败，请尝试重新登录");
                            break;
                    }
                    break;

                case RequestConstant.EMPTY_RESULT:
                    //返回结果为空
                    switch (msg.getData().getInt("Tag")) {
                        case MainTagConstant.DOWNLOAD_AVATAR:
                        case MainTagConstant.UPLOAD_AVATAR:
                            //显示默认头像
                            break;

                        case MainTagConstant.GET_USER_PROFILE:
                        case MainTagConstant.GET_USER_ACCESS:
                            break;
                    }
                    break;

                case RequestConstant.CLIENT_ERROR:
                    //加载头像失败
                    switch (msg.getData().getInt("Tag")) {
                        case MainTagConstant.DOWNLOAD_AVATAR:
                        case MainTagConstant.UPLOAD_AVATAR:
                            mainActivity.ShowToast("获取头像失败，请检查是否正确开启存储权限");
                            break;

                        case MainTagConstant.GET_USER_PROFILE:
                        case MainTagConstant.GET_USER_ACCESS:
                            break;
                    }
                    break;

                case RequestConstant.UNKNOWN_ERROR:
                    //未知异常
                    switch (msg.getData().getInt("Tag")) {
                        case MainTagConstant.DOWNLOAD_AVATAR:
                        case MainTagConstant.UPLOAD_AVATAR:
                            mainActivity.ShowToast("解析头像出现错误");
                            break;

                        case MainTagConstant.GET_USER_PROFILE:
                            break;

                        case MainTagConstant.GET_USER_ACCESS:
                            mainActivity.ShowToast("加载功能菜单出现未知异常，请尝试重新登录");
                            break;
                    }
                    break;
            }
        }
    }

    public MainPresenter(final MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.cachePath = mainActivity.getApplicationContext().getCacheDir() + "/gdeiassistant/";
        this.mainModel = new MainModel();
        this.mainActivityHandler = new MainActivityHandler(mainActivity);
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
                            mainActivity.ShowPatchRelaunchTip();
                        } else if (intent.getAction().equals("edu.gdei.gdeiassistant.CHECK_UPGRADE")) {
                            String versionCodeName = intent.getStringExtra("VersionCodeName");
                            String versionInfo = intent.getStringExtra("VersionInfo");
                            String downloadURL = intent.getStringExtra("DownloadURL");
                            String fileSize = intent.getStringExtra("FileSize");
                            //显示更新提示
                            mainActivity.ShowUpgradeTip(versionCodeName, versionInfo, downloadURL, fileSize);
                        }
                    }
                }
            };
            //注册广播监听
            mainActivity.registerReceiver(receiver, new IntentFilter("edu.gdei.gdeiassistant.PATCH_RELAUNCH"));
            mainActivity.registerReceiver(receiver, new IntentFilter("edu.gdei.gdeiassistant.CHECK_UPGRADE"));
        }
        //获取用户权限列表信息
        mainModel.GetUserAccess(mainActivityHandler, mainActivity.getApplicationContext());
    }

    /**
     * 退出账号
     */
    public void Logout() {
        try {
            final String accessToken = TokenUtils.GetUserAccessToken(mainActivity.getApplicationContext());
            final String refreshToken = TokenUtils.GetUserRefreshToken(mainActivity.getApplicationContext());
            //发送Token失效请求
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    try {
                        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                                .connectTimeout(3, TimeUnit.SECONDS).readTimeout(3, TimeUnit.SECONDS)
                                .writeTimeout(3, TimeUnit.SECONDS).build();
                        RequestBody requestBody = new FormBody.Builder().add("token", accessToken).build();
                        Request request = new Request.Builder().post(requestBody).url("https://www.gdeiassistant.cn/rest/token/expire").build();
                        okHttpClient.newCall(request).execute();
                    } catch (Exception ignored) {

                    }
                }
            }.start();
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    try {
                        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                                .connectTimeout(3, TimeUnit.SECONDS).readTimeout(3, TimeUnit.SECONDS)
                                .writeTimeout(3, TimeUnit.SECONDS).build();
                        RequestBody requestBody = new FormBody.Builder().add("token", refreshToken).build();
                        Request request = new Request.Builder().post(requestBody).url("https://www.gdeiassistant.cn/rest/token/expire").build();
                        okHttpClient.newCall(request).execute();
                    } catch (Exception ignored) {

                    }
                }
            }.start();
            //清除SharedPreferences保存的令牌信息
            TokenUtils.ClearUserToken(mainActivity.getApplicationContext());
            //清除本地缓存的用户凭证
            GdeiAssistantApplication application = (GdeiAssistantApplication) mainActivity.getApplication();
            application.removeAllData();
            //清除缓存头像信息
            File avatarFile = new File(cachePath + "avatar.jpg");
            if (avatarFile.exists() && avatarFile.canWrite()) {
                avatarFile.delete();
            }
            mainActivity.startActivity(new Intent(mainActivity, LoginActivity.class));
            mainActivity.finish();
        } catch (Exception ignored) {

        }
    }

    /**
     * 结束应用程序使补丁生效
     */
    public void PatchRelaunchAndStopProcess() {
        SophixManager.getInstance().killProcessSafely();
    }

    /**
     * 加载用户资料
     */
    public void InitUserProfile() {
        if (!CheckStoragePermission()) {
            mainActivity.ShowRequestStoragePermissionDialog(PermissionRequestCodeConstant.LOAD_AVATAR);
            return;
        }
        mainModel.DownloadAvatarToOSS(mainActivityHandler, mainActivity.getApplicationContext());
        mainModel.GetUserProfile(mainActivityHandler, mainActivity.getApplicationContext());
    }

    /**
     * 打开相册界面，从相册选择图片
     */
    public void GetPhotoFromAlbum() {
        if (!CheckStoragePermission()) {
            //若没有访问存储的权限，则进行申请
            mainActivity.ShowRequestStoragePermissionDialog(PermissionRequestCodeConstant.PERMISSION_ALBUM);
            return;
        }
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        mainActivity.startActivityForResult(intent, ActivityRequestCodeConstant.RESULT_PHOTO_FROM_ALBUM);
    }

    /**
     * 从相机拍照获取图片
     */
    public void GetPhotoFromCamera() {
        if (!CheckStoragePermission()) {
            //若没有访问存储的权限，则进行申请
            mainActivity.ShowRequestStoragePermissionDialog(PermissionRequestCodeConstant.PERMISSION_ALBUM);
            return;
        }
        if (!CheckCameraPermission()) {
            //若没有访问相机的权限，则进行申请
            mainActivity.ShowRequestCameraPermissionDialog(PermissionRequestCodeConstant.PERMISSION_CAMERA);
            return;
        }
        File avatarDirectory = new File(mainActivity.getCacheDir() + "/gdeiassistant/");
        if (!avatarDirectory.exists()) {
            avatarDirectory.mkdirs();
        }
        File file = new File(mainActivity.getCacheDir() + "/gdeiassistant/photo.jpg");
        //删除原有的残留照片
        if (file.exists()) {
            file.delete();
        }
        Intent intent = new Intent();
        intent.setAction("android.media.action.IMAGE_CAPTURE");
        intent.addCategory("android.intent.category.DEFAULT");
        //保存照片
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri uri = FileProvider.getUriForFile(mainActivity.getApplicationContext(), "edu.gdei.gdeiassistant.fileprovider", file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        } else {
            Uri uri = Uri.fromFile(file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }
        mainActivity.startActivityForResult(intent, ActivityRequestCodeConstant.RESULT_PHOTO_FROM_CAMERA);
    }

    /**
     * 将得到的图片进行图片裁剪
     *
     * @param uri
     */
    public void StartPhotoZoom(Uri uri) {
        if (uri != null) {
            Intent intent = new Intent("com.android.camera.action.CROP");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            intent.setDataAndType(uri, "image/*");
            //crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
            intent.putExtra("crop", "true");
            //aspectX aspectY 是宽高的比例
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            //outputX outputY 是裁剪图片宽高
            intent.putExtra("outputX", 300);
            intent.putExtra("outputY", 300);
            intent.putExtra("return-data", true);
            mainActivity.startActivityForResult(intent, ActivityRequestCodeConstant.RESULT_SAVE_PHOTO_TO_VIEW);
        }
    }

    /**
     * 从相机拍照得到照片，进行裁剪
     */
    public void StartCamera(Context context) {
        File tempFile = new File(cachePath + "photo.jpg");
        if (tempFile.exists() && tempFile.canRead()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri uri = FileProvider.getUriForFile(context, "edu.gdei.gdeiassistant.fileprovider", tempFile);
                StartPhotoZoom(uri);
            } else {
                StartPhotoZoom(Uri.fromFile(tempFile));
            }
        }
    }

    /**
     * 保存图片到对象存储OSS和本地，同时更新头像图片
     *
     * @param data
     */
    public void SaveAvatarAndSetPhotoToView(Intent data) {
        if (data != null) {
            File avatarDirectory = new File(mainActivity.getApplicationContext().getCacheDir() + "/gdeiassistant/");
            if (!avatarDirectory.exists()) {
                avatarDirectory.mkdirs();
            }
            //保存裁剪之后的照片
            Bundle extras = data.getExtras();
            if (extras != null) {
                //取得SDCard图片路径做显示
                Bitmap photo = extras.getParcelable("data");
                String urlPath = BitmapFileModel.saveFile(mainActivity.getApplicationContext()
                        , mainActivity.getCacheDir() + "/gdeiassistant/"
                        , ((GdeiAssistantApplication) mainActivity.getApplication()).getUsername() + ".jpg", photo);
                //删除拍照残留照片
                File cameraFile = new File(mainActivity.getCacheDir() + "/gdeiassistant/photo.jpg");
                if (cameraFile.exists() && cameraFile.canWrite()) {
                    cameraFile.delete();
                }
                File file = new File(urlPath);
                if (file.exists() && file.canRead() && file.canWrite()) {
                    //上传头像文件到对象存储OSS
                    mainModel.UploadAvatarToOSS(mainActivityHandler, mainActivity.getApplicationContext(), file);
                    return;
                }
            }
            mainActivity.ShowToast("保存头像失败，请检查是否正确开启存储权限");
        }
    }

    /**
     * 申请访问存储权限
     *
     * @param requestCode
     */
    public void RequestStoragePermission(int requestCode) {
        ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
                , Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
    }

    /**
     * 申请访问相机权限
     *
     * @param requestCode
     */
    public void RequestCameraPermission(int requestCode) {
        ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.CAMERA}, requestCode);
    }

    /**
     * 检查是否已经获得访问存储权限
     *
     * @return
     */
    private boolean CheckStoragePermission() {
        if (ContextCompat.checkSelfPermission(mainActivity.getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            return ContextCompat.checkSelfPermission(mainActivity.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    /**
     * 检查是否已经获得访问相机的权限
     *
     * @return
     */
    private boolean CheckCameraPermission() {
        return ContextCompat.checkSelfPermission(mainActivity.getApplicationContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }
}