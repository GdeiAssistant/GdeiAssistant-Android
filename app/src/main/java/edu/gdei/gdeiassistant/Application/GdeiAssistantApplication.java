package edu.gdei.gdeiassistant.Application;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.gdei.gdeiassistant.Tools.StringUtils;
import edu.gdei.gdeiassistant.Tools.TokenUtils;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public class GdeiAssistantApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            createServiceForegroundNotificationChannel();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createServiceForegroundNotificationChannel() {
        String notificationChannelID = "service";
        String notificationChannelName = "系统通知";
        NotificationChannel notificationChannel = null;
        notificationChannel = new NotificationChannel(notificationChannelID, notificationChannelName
                , NotificationManager.IMPORTANCE_NONE);
        //在图标右上角展示小红点
        notificationChannel.enableLights(true);
        //小红点颜色
        notificationChannel.setLightColor(Color.RED);
        //在长按图标时显示此渠道的通知
        notificationChannel.setShowBadge(true);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(notificationChannel);
    }

    private String token;

    private String username;

    private CookieJar cookieJar;

    public CookieJar getCookieJar() {

        if (cookieJar != null) {

            return cookieJar;

        } else {

            this.cookieJar = new CookieJar() {

                private HashMap<String, List<Cookie>> cookieStore = new HashMap<>();

                @Override
                public void saveFromResponse(@NonNull HttpUrl url, @NonNull List<Cookie> cookies) {
                    cookieStore.put(url.host(), cookies);
                }

                @Override
                public List<Cookie> loadForRequest(@NonNull HttpUrl url) {
                    List<Cookie> cookies = cookieStore.get(url.host());
                    return cookies != null ? cookies : new ArrayList<Cookie>();
                }
            };

            return cookieJar;
        }
    }

    public void setCookieJar(CookieJar cookieJar) {
        this.cookieJar = cookieJar;
    }

    public String getToken() {
        if (StringUtils.isBlank(token)) {
            return TokenUtils.GetUserAccessToken(this);
        }
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        if (StringUtils.isBlank(username)) {
            return TokenUtils.GetAccessTokenUsername(getToken());
        }
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void removeAllData() {
        this.cookieJar = null;
        this.username = null;
        this.token = null;
    }
}
