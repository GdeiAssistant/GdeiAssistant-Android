package edu.gdei.gdeiassistant.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.Toast;

import edu.gdei.gdeiassistant.Constant.ActivityRequestCodeConstant;
import edu.gdei.gdeiassistant.Presenter.GuidePresenter;
import edu.gdei.gdeiassistant.R;

public class GuideActivity extends AppCompatActivity {

    private long exitTime = 0;

    private AlertDialog alertDialog;

    private GuidePresenter guidePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        //配置加载Presenter
        guidePresenter = new GuidePresenter(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        guidePresenter.RemoveCallBacksAndMessages();
        guidePresenter.UnregisterReceiver();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case ActivityRequestCodeConstant.BROWSER_UPDATE_REQUEST_CODE:
                //返回页面时，进行页面跳转
                guidePresenter.SwitchPageAfterHandledUpdateTip();
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 显示Toast提示
     *
     * @param message
     */
    public void ShowToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 弹出补丁冷启动提示
     */
    public void ShowPatchRelaunchTip() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("新补丁更新");
        alertDialogBuilder.setMessage("新补丁已成功安装，请重启应用以生效");
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton("关闭应用", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                guidePresenter.PatchRelaunchAndStopProcess();
            }
        });
        alertDialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                guidePresenter.PatchRelaunchAndStopProcess();
            }
        });
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    /**
     * 显示更新提示
     *
     * @param versionCodeName
     * @param versionInfo
     * @param downloadURL
     */
    public void ShowUpgradeTip(String versionCodeName, String versionInfo, final String downloadURL, String fileSize) {
        AlertDialog.Builder updateDialogBuilder = new AlertDialog.Builder(this);
        updateDialogBuilder.setTitle("新版本更新");
        StringBuilder dialogMessage = new StringBuilder("新版本:" + versionCodeName + "，大小:" + fileSize);
        String infos[] = versionInfo.split(";");
        for (String string : infos) {
            dialogMessage.append("\n");
            dialogMessage.append(string);
        }
        updateDialogBuilder.setMessage(dialogMessage);
        updateDialogBuilder.setPositiveButton("更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                guidePresenter.DownLoadNewVersion(downloadURL);
            }
        });
        updateDialogBuilder.setNegativeButton("暂不更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                guidePresenter.SwitchPageAfterHandledUpdateTip();
            }
        });
        updateDialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                guidePresenter.SwitchPageAfterHandledUpdateTip();
            }
        });
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        alertDialog = updateDialogBuilder.create();
        alertDialog.show();
    }

    /**
     * 防止用户意外返回键退出
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finishAffinity();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
