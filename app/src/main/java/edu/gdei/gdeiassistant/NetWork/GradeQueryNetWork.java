package edu.gdei.gdeiassistant.NetWork;

import android.content.Context;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.gdei.gdeiassistant.Application.GdeiAssistantApplication;
import edu.gdei.gdeiassistant.Constant.NetWorkTimeoutConstant;
import edu.gdei.gdeiassistant.Exception.ResponseStatusCodeException;
import edu.gdei.gdeiassistant.Pojo.GradeQuery.GradeQueryResult;
import edu.gdei.gdeiassistant.Pojo.JsonResult.DataJsonResult;
import edu.gdei.gdeiassistant.Tools.OkHttpUtils;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GradeQueryNetWork {

    /**
     * 查询学年成绩
     *
     * @param year
     * @param context
     * @return
     */
    public DataJsonResult<GradeQueryResult> GradeQuery(@Nullable Integer year, Context context) throws Exception {
        GdeiAssistantApplication application = (GdeiAssistantApplication) context;
        OkHttpClient okHttpClient = OkHttpUtils.GetOkHttpClient(NetWorkTimeoutConstant.GRADE_QUERY_NETWORK_TIMEOUT, context);
        RequestBody requestBody = null;
        if (year == null) {
            //若没有指定查询学年，则查询默认学年
            requestBody = new FormBody.Builder().add("token", application.getToken()).build();
        } else {
            requestBody = new FormBody.Builder().add("year", String.valueOf(year))
                    .add("token", application.getToken()).build();
        }
        Request request = new Request.Builder().url("https://www.gdeiassistant.cn/rest/gradequery").post(requestBody).build();
        Response response = okHttpClient.newCall(request).execute();
        if (response.isSuccessful()) {
            return new Gson().fromJson(response.body().string()
                    , new TypeToken<DataJsonResult<GradeQueryResult>>() {
                    }.getType());
        }
        throw new ResponseStatusCodeException("服务暂不可用");
    }
}
