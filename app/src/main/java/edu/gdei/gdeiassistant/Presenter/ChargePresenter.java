package edu.gdei.gdeiassistant.Presenter;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.inputmethod.InputMethodManager;

import java.lang.ref.WeakReference;
import java.util.List;

import edu.gdei.gdeiassistant.Activity.ChargeActivity;
import edu.gdei.gdeiassistant.Activity.WebViewActivity;
import edu.gdei.gdeiassistant.Constant.ChargeTagConstant;
import edu.gdei.gdeiassistant.Constant.RequestConstant;
import edu.gdei.gdeiassistant.Model.ChargeModel;
import edu.gdei.gdeiassistant.Pojo.Entity.CardInfo;
import edu.gdei.gdeiassistant.Pojo.Entity.Charge;
import edu.gdei.gdeiassistant.Pojo.Entity.Cookie;
import edu.gdei.gdeiassistant.Tools.StringUtils;

public class ChargePresenter {

    private ChargeActivity chargeActivity;

    private ChargeModel chargeModel;

    private ChargeActivityHandler chargeActivityHandler;

    public ChargePresenter(ChargeActivity chargeActivity) {
        this.chargeActivity = chargeActivity;
        this.chargeModel = new ChargeModel();
        this.chargeActivityHandler = new ChargeActivityHandler(chargeActivity);
        Init();
    }

    private void Init() {
        GetCardInfo();
    }

    public static class ChargeActivityHandler extends Handler {

        private ChargeActivity chargeActivity;

        ChargeActivityHandler(ChargeActivity chargeActivity) {
            this.chargeActivity = new WeakReference<>(chargeActivity).get();
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case RequestConstant.SHOW_PROGRESS:
                    //显示进度条
                    chargeActivity.HideFailTip();
                    chargeActivity.HideInputLayout();
                    chargeActivity.ShowProgressbar();
                    break;

                case RequestConstant.REQUEST_SUCCESS:
                    switch (msg.getData().getInt("Tag")) {
                        case ChargeTagConstant.GET_CARD_INFO:
                            //加载校园卡基本信息
                            CardInfo cardInfo = (CardInfo) msg.getData().getSerializable("CardInfo");
                            if (cardInfo != null) {
                                chargeActivity.LoadCardInfo(cardInfo.getName(), cardInfo.getNumber(), cardInfo.getCardBalance());
                                chargeActivity.HideFailTip();
                                chargeActivity.HideProgressbar();
                                chargeActivity.ShowInputLayout();
                            } else {
                                chargeActivity.HideInputLayout();
                                chargeActivity.HideProgressbar();
                                chargeActivity.ShowFailTip(msg.getData().getString("服务暂不可用，请稍后再试"));
                            }
                            break;

                        case ChargeTagConstant.SUBMIT_CHARGE:
                            //充值请求提交成功
                            Charge charge = (Charge) msg.getData().getSerializable("Charge");
                            if (charge != null) {
                                String alipayURL = charge.getAlipayURL();
                                List<Cookie> cookieList = charge.getCookieList();
                                chargeActivity.HideInputLayout();
                                chargeActivity.HideFailTip();
                                chargeActivity.HideProgressbar();
                                chargeActivity.LoadWebView(cookieList, alipayURL);
                            } else {
                                chargeActivity.HideInputLayout();
                                chargeActivity.HideProgressbar();
                                chargeActivity.ShowFailTip(msg.getData().getString("服务暂不可用，请稍后再试"));
                            }
                            break;
                    }
                    break;

                case RequestConstant.CLIENT_ERROR:
                    //安全校验失败
                    chargeActivity.HideInputLayout();
                    chargeActivity.HideProgressbar();
                    chargeActivity.ShowFailTip("上网环境存在风险，请切换到安全网络充值");
                    break;

                case RequestConstant.REQUEST_TIMEOUT:
                    //网络连接超时
                    chargeActivity.HideInputLayout();
                    chargeActivity.HideProgressbar();
                    chargeActivity.ShowFailTip("网络连接超时，请重试");
                    break;

                case RequestConstant.UNKNOWN_ERROR:
                    //出现未知异常
                    chargeActivity.HideInputLayout();
                    chargeActivity.HideProgressbar();
                    chargeActivity.ShowFailTip("出现未知异常，请联系管理员");
                    break;

                case RequestConstant.SERVER_ERROR:
                    //服务暂不可用
                    chargeActivity.HideInputLayout();
                    chargeActivity.HideProgressbar();
                    chargeActivity.ShowFailTip(msg.getData().getString("服务暂不可用，请稍后再试"));
                    break;

                case RequestConstant.REQUEST_FAILURE:
                    //显示失败信息
                    chargeActivity.HideInputLayout();
                    chargeActivity.HideProgressbar();
                    chargeActivity.ShowFailTip(msg.getData().getString("Message"));
                    break;
            }
        }
    }

    /**
     * 移除所有的回调和消息，防止内存泄露
     */
    public void RemoveCallBacksAndMessages() {
        chargeActivityHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 获取饭卡信息
     */
    private void GetCardInfo() {
        chargeModel.GetCardInfo(chargeActivityHandler, chargeActivity.getApplicationContext());
    }

    /**
     * 显示用户协议
     */
    public void ShowAgreement() {
        Intent intent = new Intent(chargeActivity, WebViewActivity.class);
        intent.putExtra("title", "缴费服务条款");
        intent.putExtra("url", "http://ecard.gdei.edu.edu/CardManage/CardInfo/TransferClause");
        chargeActivity.startActivity(intent);
    }

    /**
     * 提交充值请求
     */
    public void ChargeSubmit(String amount, String userAgent) {
        if (StringUtils.isBlank(amount)) {
            chargeActivity.ShowToast("充值金额不能为空");
        } else {
            try {
                int number = Integer.valueOf(amount);
                if (number <= 0 || number > 500) {
                    chargeActivity.ShowToast("充值金额不合法");
                } else {
                    InputMethodManager inputMethodManager = (InputMethodManager) chargeActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (inputMethodManager != null) {
                        //收起虚拟键盘
                        inputMethodManager.hideSoftInputFromWindow(chargeActivity.getWindow().getDecorView().getWindowToken(), 0);
                    }
                    chargeModel.ChargeSubmit(chargeActivityHandler, Integer.valueOf(amount), userAgent, chargeActivity.getApplicationContext());
                }
            } catch (NumberFormatException e) {
                chargeActivity.ShowToast("充值金额不合法");
            }
        }
    }
}