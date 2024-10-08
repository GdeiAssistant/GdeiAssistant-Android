package cn.gdeiassistant.NetWork;

import android.content.Context;

import com.google.gson.Gson;

import cn.gdeiassistant.Application.GdeiAssistantApplication;
import cn.gdeiassistant.Constant.NetWorkTimeoutConstant;
import cn.gdeiassistant.Exception.ResponseStatusCodeException;
import cn.gdeiassistant.Pojo.JsonResult.JsonResult;
import cn.gdeiassistant.R;
import cn.gdeiassistant.Tools.OkHttpUtils;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LostNetWork {

    /**
     * 校园卡挂失
     *
     * @param cardPassword
     * @param context
     */
    public JsonResult CardLost(String cardPassword, Context context) throws Exception {
        GdeiAssistantApplication application = (GdeiAssistantApplication) context;
        OkHttpClient okHttpClient = OkHttpUtils.GetOkHttpClient(NetWorkTimeoutConstant.CARD_LOST_NETWORK_TIMEOUT, context);
        RequestBody requestBody = new FormBody.Builder().add("token", application.getToken())
                .add("cardPassword", cardPassword).build();
        Request request = new Request.Builder().url(context.getString(R.string.resource_domain) + "rest/cardlost").post(requestBody).build();
        Response response = okHttpClient.newCall(request).execute();
        if (response.isSuccessful()) {
            return new Gson().fromJson(response.body().string(), JsonResult.class);
        }
        throw new ResponseStatusCodeException("服务暂不可用");
    }
}
