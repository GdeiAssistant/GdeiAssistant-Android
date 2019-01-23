package edu.gdei.gdeiassistant.Model;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.Serializable;

import edu.gdei.gdeiassistant.Constant.RequestConstant;
import edu.gdei.gdeiassistant.Exception.ResponseStatusCodeException;
import edu.gdei.gdeiassistant.NetWork.ScheduleQueryNetWork;
import edu.gdei.gdeiassistant.Pojo.JsonResult.DataJsonResult;
import edu.gdei.gdeiassistant.Pojo.ScheduleQuery.ScheduleQueryResult;

public class ScheduleQueryModel {

    private ScheduleQueryNetWork scheduleQueryNetWork = new ScheduleQueryNetWork();

    /**
     * 课表查询
     *
     * @param week
     * @param handler
     * @param context
     */
    public void ScheduleQuery(@Nullable final Integer week, final Handler handler, final Context context) {
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
                    DataJsonResult<ScheduleQueryResult> result = scheduleQueryNetWork.ScheduleQuery(week, context);
                    if (Boolean.TRUE.equals(result.isSuccess())) {
                        if (result.getData() != null) {
                            bundle.putSerializable("ScheduleList", (Serializable) result.getData().getScheduleList());
                            message.what = RequestConstant.REQUEST_SUCCESS;
                        } else {
                            message.what = RequestConstant.SERVER_ERROR;
                        }
                    } else {
                        bundle.putString("Message", result.getMessage());
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
