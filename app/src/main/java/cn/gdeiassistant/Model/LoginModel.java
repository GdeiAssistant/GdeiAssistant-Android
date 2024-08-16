package cn.gdeiassistant.Model;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;

import cn.gdeiassistant.Constant.RequestConstant;
import cn.gdeiassistant.Constant.ResultConstant;
import cn.gdeiassistant.Exception.ResponseStatusCodeException;
import cn.gdeiassistant.NetWork.LoginNetWork;
import cn.gdeiassistant.Pojo.JsonResult.DataJsonResult;
import cn.gdeiassistant.Pojo.Login.UserLoginResult;

public class LoginModel {

    private LoginNetWork loginNetWork = new LoginNetWork();

    /**
     * 用户登录
     *
     * @param handler
     * @param username
     * @param password
     */
    public void UserLogin(final Handler handler, final String username, final String password, final Context context) {
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
                    DataJsonResult<UserLoginResult> result = loginNetWork.UserLogin(username, password, context);
                    if (Boolean.TRUE.equals(result.isSuccess())) {
                        if (result.getData() != null) {
                            //发送服务器返回的用户和令牌信息
                            bundle.putString("AccessToken", result.getData().getAccessToken().getSignature());
                            bundle.putString("RefreshToken", result.getData().getRefreshToken().getSignature());
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
                    message.what = ResultConstant.NETWORK_TIMEOUT;
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
