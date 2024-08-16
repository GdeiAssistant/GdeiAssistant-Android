package cn.gdeiassistant.NetWork;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import cn.gdeiassistant.Application.GdeiAssistantApplication;
import cn.gdeiassistant.Constant.NetWorkTimeoutConstant;
import cn.gdeiassistant.Exception.ResponseStatusCodeException;
import cn.gdeiassistant.Pojo.CardQuery.CardQueryResult;
import cn.gdeiassistant.Pojo.Entity.CardInfo;
import cn.gdeiassistant.Pojo.JsonResult.DataJsonResult;
import cn.gdeiassistant.R;
import cn.gdeiassistant.Tools.OkHttpUtils;
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
        Request request = new Request.Builder().post(requestBody).url(context.getString(R.string.resource_domain) + "rest/cardinfo").build();
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
        Request request = new Request.Builder().post(requestBody).url(context.getString(R.string.resource_domain) + "rest/cardquery").build();
        Response response = okHttpClient.newCall(request).execute();
        if (response.isSuccessful()) {
            return new Gson().fromJson(response.body().string()
                    , new TypeToken<DataJsonResult<CardQueryResult>>() {
                    }.getType());
        }
        throw new ResponseStatusCodeException("服务暂不可用");
    }
}
