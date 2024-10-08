package cn.gdeiassistant.Presenter;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

import cn.gdeiassistant.Activity.EvaluateActivity;
import cn.gdeiassistant.Constant.EvaluateTagConstant;
import cn.gdeiassistant.Constant.RequestConstant;
import cn.gdeiassistant.Model.EvaluateModel;

public class EvaluatePresenter {

    private EvaluateActivity evaluateActivity;

    private EvaluateModel evaluateModel;

    private EvaluateActivityHandler evaluateActivityHandler;

    public EvaluatePresenter(EvaluateActivity evaluateActivity) {
        this.evaluateActivity = evaluateActivity;
        this.evaluateModel = new EvaluateModel();
        this.evaluateActivityHandler = new EvaluateActivityHandler(evaluateActivity);
    }

    public static class EvaluateActivityHandler extends Handler {

        private EvaluateActivity evaluateActivity;

        EvaluateActivityHandler(EvaluateActivity evaluateActivity) {
            this.evaluateActivity = new WeakReference<>(evaluateActivity).get();
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case RequestConstant.REQUEST_SUCCESS:
                    //教学评价成功
                    evaluateActivity.HideProgressDialog();
                    switch (msg.getData().getInt("Tag")) {
                        case EvaluateTagConstant.NORMAL_SUBMIT:
                            evaluateActivity.ShowToast("教学评价成功，请登录教务系统进行最终确认");
                            break;

                        case EvaluateTagConstant.DIRECTLY_SUBMIT:
                            evaluateActivity.ShowToast("教学评价成功");
                            break;
                    }
                    break;

                case RequestConstant.REQUEST_FAILURE:
                    //教学评价失败
                    evaluateActivity.HideProgressDialog();
                    evaluateActivity.ShowToast(msg.getData().getString("Message"));
                    break;

                case RequestConstant.REQUEST_TIMEOUT:
                    //网络连接超时
                    evaluateActivity.HideProgressDialog();
                    evaluateActivity.ShowToast("网络连接超时，请重试");
                    break;

                case RequestConstant.UNKNOWN_ERROR:
                    //出现未知异常
                    evaluateActivity.HideProgressDialog();
                    evaluateActivity.ShowToast("出现未知异常，请联系管理员");
                    break;

                case RequestConstant.SHOW_PROGRESS:
                    //显示进度条
                    evaluateActivity.ShowProgressDialog();
                    break;
            }
        }
    }

    /**
     * 移除所有的回调和消息，防止内存泄露
     */
    public void RemoveCallBacksAndMessages() {
        evaluateActivityHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 提交教学评价请求
     */
    public void SubmitEvaluate(boolean directlySubmit) {
        evaluateModel.SubmitEvaluate(directlySubmit, evaluateActivityHandler, evaluateActivity.getApplicationContext());
    }

}
