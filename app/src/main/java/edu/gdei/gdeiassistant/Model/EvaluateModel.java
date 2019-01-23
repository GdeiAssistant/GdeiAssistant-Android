package edu.gdei.gdeiassistant.Model;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;

import edu.gdei.gdeiassistant.Constant.EvaluateTagConstant;
import edu.gdei.gdeiassistant.Constant.RequestConstant;
import edu.gdei.gdeiassistant.NetWork.EvaluateNetWork;
import edu.gdei.gdeiassistant.Pojo.JsonResult.JsonResult;

public class EvaluateModel {

    private EvaluateNetWork evaluateNetWork = new EvaluateNetWork();

    /**
     * 提交一键评教请求
     *
     * @param directlySubmit
     * @param context
     */
    public void SubmitEvaluate(final boolean directlySubmit, final Handler handler, final Context context) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                Message message = handler.obtainMessage();
                Bundle bundle = new Bundle();
                try {
                    message.what = RequestConstant.SHOW_PROGRESS;
                    handler.sendMessage(message);
                    message = handler.obtainMessage();
                    if (directlySubmit) {
                        bundle.putInt("Tag", EvaluateTagConstant.DIRECTLY_SUBMIT);
                    } else {
                        bundle.putInt("Tag", EvaluateTagConstant.NORMAL_SUBMIT);
                    }
                    JsonResult result = evaluateNetWork.SubmitEvaluateRequest(directlySubmit, context);
                    if (Boolean.TRUE.equals(result.isSuccess())) {
                        message.what = RequestConstant.REQUEST_SUCCESS;
                    } else {
                        bundle.putString("Message", result.getMessage());
                        message.what = RequestConstant.REQUEST_FAILURE;
                    }
                } catch (NullPointerException ignored) {

                } catch (IOException e) {
                    message.what = RequestConstant.REQUEST_TIMEOUT;
                } catch (Exception e) {
                    message.what = RequestConstant.UNKNOWN_ERROR;
                }
                message.setData(bundle);
                handler.sendMessage(message);
            }
        }.start();
    }

}
