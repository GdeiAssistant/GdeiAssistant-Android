package edu.gdei.gdeiassistant.Presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.view.inputmethod.InputMethodManager;

import java.lang.ref.WeakReference;

import edu.gdei.gdeiassistant.Constant.CetTagConstant;
import edu.gdei.gdeiassistant.Constant.RequestConstant;
import edu.gdei.gdeiassistant.Fragment.FragmentCet;
import edu.gdei.gdeiassistant.Model.CetQueryModel;
import edu.gdei.gdeiassistant.Pojo.Entity.Cet;
import edu.gdei.gdeiassistant.Tools.StringUtils;

public class CetPresenter {

    private FragmentCet fragmentCet;

    private CetQueryModel cetQueryModel;

    private FragmentCetHandler fragmentCetHandler;

    public CetPresenter(FragmentCet fragmentCet) {
        this.fragmentCet = fragmentCet;
        this.cetQueryModel = new CetQueryModel();
        this.fragmentCetHandler = new FragmentCetHandler(fragmentCet, this);
        Init();
    }

    private void Init() {
        CetCheckCode();
    }

    public static class FragmentCetHandler extends Handler {

        private FragmentCet fragmentCet;

        private CetPresenter cetPresenter;

        FragmentCetHandler(FragmentCet fragmentCet, CetPresenter cetPresenter) {
            this.cetPresenter = cetPresenter;
            this.fragmentCet = new WeakReference<>(fragmentCet).get();
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case RequestConstant.SHOW_PROGRESS:
                    //显示进度条
                    fragmentCet.HideDataLayout();
                    fragmentCet.HideInputLayout();
                    fragmentCet.ShowProgressbar();
                    break;

                case RequestConstant.REQUEST_SUCCESS:
                    switch (msg.getData().getInt("Tag")) {
                        case CetTagConstant.CHECK_CODE:
                            //加载验证码成功
                            String checkcode = msg.getData().getString("CheckCode");
                            if (StringUtils.isNotBlank(checkcode)) {
                                byte[] bitmapArray = Base64.decode(checkcode, Base64.DEFAULT);
                                Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
                                fragmentCet.ShowCheckCode(new BitmapDrawable(fragmentCet.getActivity()
                                        .getApplicationContext().getResources(), bitmap));
                            } else {
                                fragmentCet.ShowCheckCodeNotAvailableImage();
                            }
                            break;

                        case CetTagConstant.CET_QUERY:
                            //查询四六级成绩成功
                            fragmentCet.HideProgressbar();
                            fragmentCet.HideInputLayout();
                            //加载四六级成绩信息
                            fragmentCet.LoadCetData((Cet) msg.getData().getSerializable("Cet"));
                            fragmentCet.ShowDataLayout();
                            break;
                    }
                    break;

                case RequestConstant.REQUEST_FAILURE:
                    //查询四六级成绩或加载验证码信息失败
                    switch (msg.getData().getInt("Tag")) {
                        case CetTagConstant.CHECK_CODE:
                            fragmentCet.ShowCheckCodeNotAvailableImage();
                            break;

                        case CetTagConstant.CET_QUERY:
                            fragmentCet.HideProgressbar();
                            fragmentCet.ShowToastTip(msg.getData().getString("Message"));
                            fragmentCet.ShowInputLayout();
                            fragmentCet.ResetCheckCode();
                            cetPresenter.CetCheckCode();
                            break;
                    }
                    break;

                case RequestConstant.REQUEST_TIMEOUT:
                    //网络连接超时
                    switch (msg.getData().getInt("Tag")) {
                        case CetTagConstant.CHECK_CODE:
                            fragmentCet.ShowCheckCodeNotAvailableImage();
                            break;

                        case CetTagConstant.CET_QUERY:
                            fragmentCet.HideProgressbar();
                            fragmentCet.ShowToastTip("网络连接超时，请重试");
                            fragmentCet.ShowInputLayout();
                            fragmentCet.ResetCheckCode();
                            cetPresenter.CetCheckCode();
                            break;
                    }
                    break;

                case RequestConstant.SERVER_ERROR:
                    //服务暂不可用
                    switch (msg.getData().getInt("Tag")) {
                        case CetTagConstant.CHECK_CODE:
                            fragmentCet.ShowCheckCodeNotAvailableImage();
                            break;

                        case CetTagConstant.CET_QUERY:
                            fragmentCet.HideProgressbar();
                            fragmentCet.ShowToastTip("服务暂不可用，请重试");
                            fragmentCet.ShowInputLayout();
                            fragmentCet.ResetCheckCode();
                            cetPresenter.CetCheckCode();
                            break;
                    }
                    break;

                case RequestConstant.UNKNOWN_ERROR:
                    //出现未知异常
                    switch (msg.getData().getInt("Tag")) {
                        case CetTagConstant.CHECK_CODE:
                            fragmentCet.ShowCheckCodeNotAvailableImage();
                            break;

                        case CetTagConstant.CET_QUERY:
                            fragmentCet.HideProgressbar();
                            fragmentCet.ShowToastTip("出现未知异常，请重试");
                            fragmentCet.ShowInputLayout();
                            fragmentCet.ResetCheckCode();
                            cetPresenter.CetCheckCode();
                            break;
                    }
                    break;
            }
        }
    }

    /**
     * 移除所有的回调和消息，防止内存泄露
     */
    public void RemoveCallBacksAndMessages() {
        fragmentCetHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 更新四六级成绩查询验证码
     */
    public void CetCheckCode() {
        cetQueryModel.CetCheckCode(fragmentCetHandler, fragmentCet.getActivity().getApplicationContext());
    }

    /**
     * 查询四六级成绩
     *
     * @param name
     * @param number
     */
    public void CetQuery(String number, String name, String checkcode) {
        if (StringUtils.isBlank(number)) {
            fragmentCet.ShowToastTip("准考证号不能为空");
        } else if (StringUtils.isBlank(name)) {
            fragmentCet.ShowToastTip("姓名不能为空");
        } else if (StringUtils.isBlank(checkcode)) {
            fragmentCet.ShowToastTip("验证码不能为空");
        } else if (number.length() != 15) {
            fragmentCet.ShowToastTip("准考证号不合法");
        } else {
            InputMethodManager inputMethodManager = (InputMethodManager) fragmentCet.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                //收起虚拟键盘
                inputMethodManager.hideSoftInputFromWindow(fragmentCet.getActivity().getWindow().getDecorView().getWindowToken(), 0);
            }
            cetQueryModel.CetQuery(fragmentCetHandler, number, name, checkcode, fragmentCet.getActivity().getApplicationContext());
        }
    }
}
