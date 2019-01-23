package edu.gdei.gdeiassistant.Tools;

import android.content.Context;
import android.content.SharedPreferences;

import com.auth0.android.jwt.DecodeException;
import com.auth0.android.jwt.JWT;

import java.util.Date;

public class TokenUtils {

    /**
     * 获取用户权限令牌
     *
     * @param context
     * @return
     */
    public static String GetUserAccessToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("GdeiAssistant", Context.MODE_PRIVATE);
        return sharedPreferences.getString("AccessToken", null);
    }

    /**
     * 获取用户刷新令牌
     *
     * @param context
     * @return
     */
    public static String GetUserRefreshToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("GdeiAssistant", Context.MODE_PRIVATE);
        return sharedPreferences.getString("RefreshToken", null);
    }

    /**
     * 删除用户保存的权限和刷新令牌信息
     *
     * @param context
     */
    public static void ClearUserToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("GdeiAssistant", Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();
    }

    /**
     * 保存用户令牌信息
     *
     * @param accessToken
     * @param refreshToken
     * @param context
     */
    public static void SaveUserToken(String accessToken, String refreshToken, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("GdeiAssistant", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("AccessToken", accessToken);
        editor.putString("RefreshToken", refreshToken);
        editor.apply();
    }

    /**
     * 校验令牌时间戳有效性
     *
     * @param token
     * @return
     */
    public static boolean ValidateTokenTimestamp(String token) {
        try {
            String expireTime = new JWT(token).getClaim("expireTime").asString();
            if (StringUtils.isNotBlank(expireTime)) {
                return TimeUtils.GetTimestampDifference(new Date(), new Date(Long.valueOf(expireTime)), TimeUtils.TimeUnit.HOUR) > 1;
            }
            return false;
        } catch (DecodeException | NumberFormatException e) {
            return false;
        }
    }

    public static String GetAccessTokenUsername(String accessToken) {
        try {
            return new JWT(accessToken).getClaim("username").asString();
        } catch (DecodeException e) {
            return null;
        }
    }
}
