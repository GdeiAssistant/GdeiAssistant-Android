package edu.gdei.gdeiassistant.Presenter;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.inputmethod.InputMethodManager;

import java.lang.ref.WeakReference;

import edu.gdei.gdeiassistant.Activity.LostActivity;
import edu.gdei.gdeiassistant.Constant.RequestConstant;
import edu.gdei.gdeiassistant.Model.LostModel;
import edu.gdei.gdeiassistant.Tools.StringUtils;

public class LostPresenter {

    private LostActivity lostActivity;

    private LostModel lostModel;

    private LostActivityHandler lostActivityHandler;

    public LostPresenter(LostActivity lostActivity) {
        this.lostActivity = lostActivity;
        this.lostModel = new LostModel();
        this.lostActivityHandler = new LostActivityHandler(lostActivity);
    }

    public static class LostActivityHandler extends Handler {

        private LostActivity lostActivity;

        LostActivityHandler(LostActivity lostActivity) {
            this.lostActivity = new WeakReference<>(lostActivity).get();
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case RequestConstant.SHOW_PROGRESS:
                    //显示进度条
                    lostActivity.ShowProgressDialog();
                    break;

                case RequestConstant.REQUEST_SUCCESS:
                    //挂失成功
                    lostActivity.HideProgressDialog();
                    lostActivity.ShowToast("挂失校园卡成功");
                    break;

                case RequestConstant.REQUEST_FAILURE:
                    //挂失失败
                    lostActivity.HideProgressDialog();
                    lostActivity.ShowToast(msg.getData().getString("Message"));
                    break;

                case RequestConstant.SERVER_ERROR:
                    //服务暂不可用
                    lostActivity.HideProgressDialog();
                    lostActivity.ShowToast("服务暂不可用，请稍后再试");
                    break;

                case RequestConstant.REQUEST_TIMEOUT:
                    //网络连接超时
                    lostActivity.HideProgressDialog();
                    lostActivity.ShowToast("网络连接超时，请重试");
                    break;

                case RequestConstant.UNKNOWN_ERROR:
                    //出现未知异常
                    lostActivity.HideProgressDialog();
                    lostActivity.ShowToast("出现未知异常，请联系管理员");
                    break;

            }
        }
    }

    public void RemoveCallBacksAndMessages() {
        lostActivityHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 校园卡挂失
     *
     * @param cardPassword
     */
    public void CardLost(String cardPassword) {
        if (StringUtils.isBlank(cardPassword)) {
            lostActivity.ShowToast("校园卡密码不能为空");
        } else if (cardPassword.length() != 6) {
            lostActivity.ShowToast("校园卡密码为6位数字");
        } else {
            InputMethodManager inputMethodManager = (InputMethodManager) lostActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                //收起虚拟键盘
                inputMethodManager.hideSoftInputFromWindow(lostActivity.getWindow().getDecorView().getWindowToken(), 0);
            }
            lostModel.CardLost(cardPassword, lostActivityHandler, lostActivity.getApplicationContext());
        }
    }
}
