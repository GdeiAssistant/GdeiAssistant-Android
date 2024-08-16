package cn.gdeiassistant.Model;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;

import cn.gdeiassistant.Constant.RequestConstant;
import cn.gdeiassistant.Exception.ResponseStatusCodeException;
import cn.gdeiassistant.NetWork.TokenNetWork;
import cn.gdeiassistant.Pojo.JsonResult.DataJsonResult;
import cn.gdeiassistant.Pojo.Token.TokenRefreshResult;

public class GuideModel {

    private TokenNetWork tokenNetWork = new TokenNetWork();

    /**
     * 刷新权限令牌
     *
     * @param refreshToken
     */
    public void RefreshToken(final Handler handler, final String refreshToken, final Context context) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                Message message = handler.obtainMessage();
                Bundle bundle = new Bundle();
                try {
                    DataJsonResult<TokenRefreshResult> result = tokenNetWork.RefreshToken(refreshToken, context);
                    if (Boolean.TRUE.equals(result.isSuccess())) {
                        if (result.getData() != null) {
                            //缓存刷新后的令牌信息
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
