package cn.gdeiassistant.NetWork;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import cn.gdeiassistant.Application.GdeiAssistantApplication;
import cn.gdeiassistant.Constant.NetWorkTimeoutConstant;
import cn.gdeiassistant.Exception.ResponseStatusCodeException;
import cn.gdeiassistant.Pojo.Entity.Book;
import cn.gdeiassistant.Pojo.JsonResult.DataJsonResult;
import cn.gdeiassistant.Pojo.JsonResult.JsonResult;
import cn.gdeiassistant.R;
import cn.gdeiassistant.Tools.OkHttpUtils;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BookNetWork {

    /**
     * 提交图书借阅查询请求
     *
     * @param password
     * @param context
     * @return
     */
    public DataJsonResult<List<Book>> BookQuery(String password, Context context) throws Exception {
        GdeiAssistantApplication application = (GdeiAssistantApplication) context;
        OkHttpClient okHttpClient = OkHttpUtils.GetOkHttpClient(NetWorkTimeoutConstant.BOOK_QUERY_NETWORK_TIMEOUT, context);
        RequestBody requestBody = new FormBody.Builder().add("password", password)
                .add("token", application.getToken()).build();
        Request request = new Request.Builder().url(context.getString(R.string.resource_domain) + "rest/bookquery").post(requestBody).build();
        Response response = okHttpClient.newCall(request).execute();
        if (response.isSuccessful()) {
            return new Gson().fromJson(response.body().string(), new TypeToken<DataJsonResult<List<Book>>>() {
            }.getType());
        }
        throw new ResponseStatusCodeException("服务暂不可用");
    }

    /**
     * 提交图书续借请求
     *
     * @param password
     * @param context
     * @return
     */
    public JsonResult BookRenew(String password, String sn, String code, Context context) throws Exception {
        GdeiAssistantApplication application = (GdeiAssistantApplication) context;
        OkHttpClient okHttpClient = OkHttpUtils.GetOkHttpClient(NetWorkTimeoutConstant.BOOK_QUERY_NETWORK_TIMEOUT, context);
        RequestBody requestBody = new FormBody.Builder().add("password", password).add("sn", sn)
                .add("code", code).add("token", application.getToken()).build();
        Request request = new Request.Builder().url(context.getString(R.string.resource_domain) + "rest/bookrenew").post(requestBody).build();
        Response response = okHttpClient.newCall(request).execute();
        if (response.isSuccessful()) {
            return new Gson().fromJson(response.body().string(), JsonResult.class);
        }
        throw new ResponseStatusCodeException("服务暂不可用");
    }
}
