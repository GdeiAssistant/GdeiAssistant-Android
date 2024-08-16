package cn.gdeiassistant.NetWork;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import cn.gdeiassistant.Constant.NetWorkTimeoutConstant;
import cn.gdeiassistant.Exception.ResponseStatusCodeException;
import cn.gdeiassistant.Pojo.Entity.Cet;
import cn.gdeiassistant.Pojo.JsonResult.DataJsonResult;
import cn.gdeiassistant.R;
import cn.gdeiassistant.Tools.OkHttpUtils;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CetQueryNetWork {

    /**
     * 加载四六级成绩查询页面验证码
     *
     * @param context
     * @return
     * @throws Exception
     */
    public DataJsonResult<String> CetCheckCode(Context context) throws Exception {
        OkHttpClient okHttpClient = OkHttpUtils.GetOkHttpClient(NetWorkTimeoutConstant.CET_QUERY_NETWORK_TIMEOUT, context);
        Request request = new Request.Builder().url(context.getString(R.string.resource_domain) + "rest/cet/checkcode").build();
        Response response = okHttpClient.newCall(request).execute();
        if (response.isSuccessful()) {
            return new Gson().fromJson(response.body().string()
                    , new TypeToken<DataJsonResult<String>>() {
                    }.getType());
        }
        throw new ResponseStatusCodeException("服务暂不可用");
    }

    /**
     * 查询四六级成绩
     *
     * @param number
     * @param name
     * @param checkcode
     * @param context
     * @return
     */
    public DataJsonResult<Cet> CetQuery(String number, String name, String checkcode, Context context) throws Exception {
        OkHttpClient okHttpClient = OkHttpUtils.GetOkHttpClient(NetWorkTimeoutConstant.CET_QUERY_NETWORK_TIMEOUT, context);
        RequestBody requestBody = new FormBody.Builder().add("number", number).add("name", name)
                .add("checkcode", checkcode).build();
        Request request = new Request.Builder().url(context.getString(R.string.resource_domain) + "rest/cetquery").post(requestBody).build();
        Response response = okHttpClient.newCall(request).execute();
        if (response.isSuccessful()) {
            return new Gson().fromJson(response.body().string()
                    , new TypeToken<DataJsonResult<Cet>>() {
                    }.getType());
        }
        throw new ResponseStatusCodeException("服务暂不可用");
    }
}
