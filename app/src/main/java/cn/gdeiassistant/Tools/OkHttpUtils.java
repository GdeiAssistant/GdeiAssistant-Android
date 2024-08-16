package cn.gdeiassistant.Tools;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import cn.gdeiassistant.Application.GdeiAssistantApplication;
import okhttp3.OkHttpClient;

public class OkHttpUtils {

    /**
     * 获取OkHttpClient对象
     *
     * @param timeout 超时时间，单位为秒
     * @param context
     * @return
     */
    public static OkHttpClient GetOkHttpClient(int timeout, Context context) {
        return new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.SECONDS).readTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS).cookieJar(((GdeiAssistantApplication) context).getCookieJar())
                .build();
    }
}
