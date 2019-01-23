package edu.gdei.gdeiassistant.NetWork;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.gdei.gdeiassistant.Application.GdeiAssistantApplication;
import edu.gdei.gdeiassistant.Constant.NetWorkTimeoutConstant;
import edu.gdei.gdeiassistant.Exception.ResponseStatusCodeException;
import edu.gdei.gdeiassistant.Pojo.Entity.Profile;
import edu.gdei.gdeiassistant.Pojo.JsonResult.DataJsonResult;
import edu.gdei.gdeiassistant.Tools.OkHttpUtils;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProfileNetWork {

    /**
     * 获取用户资料
     *
     * @param context
     * @return
     * @throws Exception
     */
    public DataJsonResult<Profile> GetUserProfile(Context context) throws Exception {
        GdeiAssistantApplication application = (GdeiAssistantApplication) context;
        OkHttpClient okHttpClient = OkHttpUtils.GetOkHttpClient(NetWorkTimeoutConstant.PROFILE_NETWORK_TIMEOUT, context);
        RequestBody requestBody = new FormBody.Builder().add("token", application.getToken()).build();
        Request request = new Request.Builder().url("https://www.gdeiassistant.cn/rest/profile").post(requestBody).build();
        Response response = okHttpClient.newCall(request).execute();
        if (response.isSuccessful()) {
            return new Gson().fromJson(response.body().string(), new TypeToken<DataJsonResult<Profile>>() {
            }.getType());
        }
        throw new ResponseStatusCodeException("服务暂不可用");
    }
}
