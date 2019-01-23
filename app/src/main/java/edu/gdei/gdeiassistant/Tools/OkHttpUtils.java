package edu.gdei.gdeiassistant.Tools;

import android.content.Context;
import android.support.annotation.NonNull;

import com.alibaba.sdk.android.httpdns.HttpDns;
import com.alibaba.sdk.android.httpdns.HttpDnsService;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import edu.gdei.gdeiassistant.Application.GdeiAssistantApplication;
import edu.gdei.gdeiassistant.R;
import okhttp3.Dns;
import okhttp3.OkHttpClient;

public class OkHttpUtils {

    /**
     * 使用HTTPDNS替代系统DNS
     */
    public static class OkHttpDns implements Dns {

        private static final Dns SYSTEM = Dns.SYSTEM;
        HttpDnsService httpdns;//httpdns 解析服务
        private static OkHttpDns instance = null;

        private OkHttpDns(Context context) {
            this.httpdns = HttpDns.getService(context, context.getString(R.string.httpdns_account_id), context.getString(R.string.httpdns_secret_key));
        }

        public static OkHttpDns getInstance(Context context) {
            if (instance == null) {
                instance = new OkHttpDns(context);
            }
            return instance;
        }

        @Override
        public List<InetAddress> lookup(@NonNull String hostname) throws UnknownHostException {
            //通过异步解析接口获取ip
            String ip = httpdns.getIpByHostAsync(hostname);
            if (ip != null) {
                //如果ip不为null，直接使用该ip进行网络请求
                return Arrays.asList(InetAddress.getAllByName(ip));
            }
            //如果返回null，走系统DNS服务解析域名
            return Dns.SYSTEM.lookup(hostname);
        }
    }

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
                .dns(OkHttpDns.getInstance(context)).build();
    }
}
