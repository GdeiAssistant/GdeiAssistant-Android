package edu.gdei.gdeiassistant.NetWork;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.Date;

import edu.gdei.gdeiassistant.Constant.NetWorkTimeoutConstant;
import edu.gdei.gdeiassistant.Exception.ResponseStatusCodeException;
import edu.gdei.gdeiassistant.Pojo.JsonResult.DataJsonResult;
import edu.gdei.gdeiassistant.Pojo.Login.UserLoginResult;
import edu.gdei.gdeiassistant.R;
import edu.gdei.gdeiassistant.Tools.OkHttpUtils;
import edu.gdei.gdeiassistant.Tools.StringUtils;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginNetWork {

    /**
     * 用户登录
     *
     * @param username
     * @param password
     * @param context
     * @return
     */
    public DataJsonResult<UserLoginResult> UserLogin(String username, String password, Context context) throws Exception {
        OkHttpClient okHttpClient = OkHttpUtils.GetOkHttpClient(NetWorkTimeoutConstant.LOGIN_NETWORK_TIMEOUT, context);
        String unionid = StringUtils.getUniquePsuedoID();
        String nonce = StringUtils.getUUID();
        String timestamp = String.valueOf(new Date().getTime());
        String signature = new String(Hex.encodeHex(DigestUtils.sha1(timestamp + nonce + context.getString(R.string.request_validate_token))));
        RequestBody requestBody = new FormBody.Builder().add("username", username)
                .add("password", password)
                .add("unionid", unionid).add("nonce", nonce)
                .add("timestamp", timestamp).add("signature", signature).build();
        Request request = new Request.Builder().url("https://www.gdeiassistant.cn/rest/userlogin")
                .post(requestBody).build();
        Response response = okHttpClient.newCall(request).execute();
        if (response.isSuccessful()) {
            return new Gson().fromJson(response.body().string()
                    , new TypeToken<DataJsonResult<UserLoginResult>>() {
                    }.getType());
        }
        throw new ResponseStatusCodeException("服务暂不可用");
    }
}
