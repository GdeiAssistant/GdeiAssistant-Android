package edu.gdei.gdeiassistant.Model;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;

import edu.gdei.gdeiassistant.Constant.CetTagConstant;
import edu.gdei.gdeiassistant.Constant.RequestConstant;
import edu.gdei.gdeiassistant.Exception.ResponseStatusCodeException;
import edu.gdei.gdeiassistant.NetWork.CetQueryNetWork;
import edu.gdei.gdeiassistant.Pojo.Entity.Cet;
import edu.gdei.gdeiassistant.Pojo.JsonResult.DataJsonResult;

public class CetQueryModel {

    private CetQueryNetWork cetQueryNetWork = new CetQueryNetWork();

    /**
     * 获取四六级成绩查询验证码
     *
     * @param handler
     * @param context
     */
    public void CetCheckCode(final Handler handler, final Context context) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                Message message = handler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putInt("Tag", CetTagConstant.CHECK_CODE);
                try {
                    DataJsonResult<String> result = cetQueryNetWork.CetCheckCode(context);
                    if (Boolean.TRUE.equals(result.isSuccess())) {
                        if (result.getData() != null) {
                            bundle.putSerializable("CheckCode", result.getData());
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

    /**
     * 查询四六级成绩
     *
     * @param handler
     * @param number
     * @param name
     */
    public void CetQuery(final Handler handler, final String number, final String name, final String checkcode, final Context context) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                Message message = handler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putInt("Tag", CetTagConstant.CET_QUERY);
                try {
                    message.what = RequestConstant.SHOW_PROGRESS;
                    handler.sendMessage(message);
                    message = handler.obtainMessage();
                    DataJsonResult<Cet> result = cetQueryNetWork.CetQuery(number, name, checkcode, context);
                    if (Boolean.TRUE.equals(result.isSuccess())) {
                        if (result.getData() != null) {
                            bundle.putSerializable("Cet", result.getData());
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
