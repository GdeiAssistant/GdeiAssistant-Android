package cn.gdeiassistant.NetWork;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import cn.gdeiassistant.Application.GdeiAssistantApplication;
import cn.gdeiassistant.Constant.NetWorkTimeoutConstant;
import cn.gdeiassistant.Exception.ResponseStatusCodeException;
import cn.gdeiassistant.Pojo.Entity.Access;
import cn.gdeiassistant.Pojo.JsonResult.DataJsonResult;
import cn.gdeiassistant.R;
import cn.gdeiassistant.Tools.OkHttpUtils;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AccessNetWork {

    /**
     * 获取安卓客户端权限列表
     *
     * @param context
     * @return
     */
    public DataJsonResult<Access> GetAndroidAccess(Context context) throws Exception {
        GdeiAssistantApplication application = (GdeiAssistantApplication) context;
        OkHttpClient okHttpClient = OkHttpUtils.GetOkHttpClient(NetWorkTimeoutConstant.CARD_QUERY_NETWORK_TIMEOUT, context);
        RequestBody requestBody = new FormBody.Builder().add("token", application.getToken()).build();
        Request request = new Request.Builder().post(requestBody).url(context.getString(R.string.resource_domain) + "rest/access/android").build();
        Response response = okHttpClient.newCall(request).execute();
        if (response.isSuccessful()) {
            return new Gson().fromJson(response.body().string()
                    , new TypeToken<DataJsonResult<Access>>() {
                    }.getType());
        }
        throw new ResponseStatusCodeException("服务暂不可用");
    }
}
