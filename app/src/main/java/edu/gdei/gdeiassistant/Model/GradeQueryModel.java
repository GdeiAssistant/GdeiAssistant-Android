package edu.gdei.gdeiassistant.Model;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;

import edu.gdei.gdeiassistant.Constant.RequestConstant;
import edu.gdei.gdeiassistant.Exception.ResponseStatusCodeException;
import edu.gdei.gdeiassistant.NetWork.GradeQueryNetWork;
import edu.gdei.gdeiassistant.Pojo.GradeQuery.GradeQueryResult;
import edu.gdei.gdeiassistant.Pojo.JsonResult.DataJsonResult;

public class GradeQueryModel {

    private GradeQueryNetWork gradeQueryNetWork = new GradeQueryNetWork();

    /**
     * 查询成绩
     *
     * @param handler
     * @param year
     */
    public void GradeQuery(final Handler handler, final Integer year, final Context context) {
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
                    DataJsonResult<GradeQueryResult> result = gradeQueryNetWork.GradeQuery(year, context);
                    if (Boolean.TRUE.equals(result.isSuccess())) {
                        if (result.getData() != null) {
                            bundle.putSerializable("GradeData", result.getData());
                            message.what = RequestConstant.REQUEST_SUCCESS;
                        } else {
                            message.what = RequestConstant.SERVER_ERROR;
                        }
                    } else {
                        bundle.putString("Message", result.getMessage());
                        message.setData(bundle);
                        message.what = RequestConstant.REQUEST_FAILURE;
                    }
                } catch (NullPointerException ignored) {

                } catch (IOException e) {
                    message.what = RequestConstant.REQUEST_TIMEOUT;
                } catch (ResponseStatusCodeException e) {
                    message.what = RequestConstant.SERVER_ERROR;
                } catch (Exception e) {
                    message.what = RequestConstant.UNKNOWN_ERROR;
                }
                message.setData(bundle);
                handler.sendMessage(message);
            }
        }.start();
    }
}
