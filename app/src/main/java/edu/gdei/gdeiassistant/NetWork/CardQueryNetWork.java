package edu.gdei.gdeiassistant.NetWork;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.gdei.gdeiassistant.Application.GdeiAssistantApplication;
import edu.gdei.gdeiassistant.Constant.NetWorkTimeoutConstant;
import edu.gdei.gdeiassistant.Exception.ResponseStatusCodeException;
import edu.gdei.gdeiassistant.Pojo.CardQuery.CardQueryResult;
import edu.gdei.gdeiassistant.Pojo.Entity.CardInfo;
import edu.gdei.gdeiassistant.Pojo.JsonResult.DataJsonResult;
import edu.gdei.gdeiassistant.Tools.OkHttpUtils;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CardQueryNetWork {

    /**
     * 查询校园卡基本信息
     *
     * @param context
     * @return
     */
    public DataJsonResult<CardInfo> CardInfoQuery(Context context) throws Exception {
        GdeiAssistantApplication application = (GdeiAssistantApplication) context;
        OkHttpClient okHttpClient = OkHttpUtils.GetOkHttpClient(NetWorkTimeoutConstant.CARD_QUERY_NETWORK_TIMEOUT, context);
        RequestBody requestBody = new FormBody.Builder().add("token", application.getToken()).build();
        Request request = new Request.Builder().post(requestBody).url("https://www.gdeiassistant.cn/rest/cardinfo").build();
        Response response = okHttpClient.newCall(request).execute();
        if (response.isSuccessful()) {
            return new Gson().fromJson(response.body().string()
                    , new TypeToken<DataJsonResult<CardInfo>>() {
                    }.getType());
        }
        throw new ResponseStatusCodeException("服务暂不可用");
    }

    /**
     * 查询消费流水记录
     *
     * @param year
     * @param month
     * @param date
     * @param context
     * @return
     * @throws Exception
     */
    public DataJsonResult<CardQueryResult> CardQuery(int year, int month, int date, Context context) throws Exception {
        GdeiAssistantApplication application = (GdeiAssistantApplication) context;
        OkHttpClient okHttpClient = OkHttpUtils.GetOkHttpClient(NetWorkTimeoutConstant.CARD_QUERY_NETWORK_TIMEOUT, context);
        RequestBody requestBody = new FormBody.Builder().add("token", application.getToken())
                .add("year", year + "").add("month", month + "")
                .add("date", date + "").build();
        Request request = new Request.Builder().post(requestBody).url("https://www.gdeiassistant.cn/rest/cardquery").build();
        Response response = okHttpClient.newCall(request).execute();
        if (response.isSuccessful()) {
            return new Gson().fromJson(response.body().string()
                    , new TypeToken<DataJsonResult<CardQueryResult>>() {
                    }.getType());
        }
        throw new ResponseStatusCodeException("服务暂不可用");
    }
}
