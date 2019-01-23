package edu.gdei.gdeiassistant.NetWork;

import android.content.Context;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.gdei.gdeiassistant.Application.GdeiAssistantApplication;
import edu.gdei.gdeiassistant.Constant.NetWorkTimeoutConstant;
import edu.gdei.gdeiassistant.Exception.ResponseStatusCodeException;
import edu.gdei.gdeiassistant.Pojo.JsonResult.DataJsonResult;
import edu.gdei.gdeiassistant.Pojo.ScheduleQuery.ScheduleQueryResult;
import edu.gdei.gdeiassistant.Tools.OkHttpUtils;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ScheduleQueryNetWork {

    /**
     * 查询课表信息
     *
     * @param week
     * @param context
     * @return
     * @throws Exception
     */
    public DataJsonResult<ScheduleQueryResult> ScheduleQuery(@Nullable Integer week, Context context) throws Exception {
        GdeiAssistantApplication application = (GdeiAssistantApplication) context;
        OkHttpClient okHttpClient = OkHttpUtils.GetOkHttpClient(NetWorkTimeoutConstant.SCHEDULE_QUERY_NETWORK_TIMEOUT, context);
        RequestBody requestBody = null;
        if (week == null) {
            requestBody = new FormBody.Builder().add("token", application.getToken()).build();
        } else {
            requestBody = new FormBody.Builder().add("token", application.getToken()).add("week", String.valueOf(week)).build();
        }
        Request request = new Request.Builder().url("https://www.gdeiassistant.cn/rest/schedulequery").post(requestBody).build();
        Response response = okHttpClient.newCall(request).execute();
        if (response.isSuccessful()) {
            return new Gson().fromJson(response.body().string(), new TypeToken<DataJsonResult<ScheduleQueryResult>>() {
            }.getType());
        }
        throw new ResponseStatusCodeException("服务暂不可用");
    }
}
