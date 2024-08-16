package cn.gdeiassistant.NetWork;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import cn.gdeiassistant.Application.GdeiAssistantApplication;
import cn.gdeiassistant.Constant.NetWorkTimeoutConstant;
import cn.gdeiassistant.Exception.ResponseStatusCodeException;
import cn.gdeiassistant.Pojo.Entity.EncryptedData;
import cn.gdeiassistant.Pojo.JsonResult.DataJsonResult;
import cn.gdeiassistant.R;
import cn.gdeiassistant.Tools.OkHttpUtils;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChargeNetWork {

    /**
     * 提交充值请求
     *
     * @param amount
     * @param context
     * @return
     */
    public DataJsonResult<EncryptedData> ChargeRequest(String amount, String userAgent, String nonce, Long timestamp, String signature
            , String clientRSAPublicKey, String clientAESKey, String clientRSASignature, Context context) throws Exception {
        GdeiAssistantApplication application = (GdeiAssistantApplication) context;
        RequestBody requestBody = new FormBody.Builder()
                .add("amount", amount).add("token", application.getToken())
                .add("nonce", nonce).add("timestamp", String.valueOf(timestamp)).add("signature", signature)
                .add("clientRSAPublicKey", clientRSAPublicKey).add("clientAESKey", clientAESKey)
                .add("clientRSASignature", clientRSASignature)
                .build();
        Request request = new Request.Builder()
                .header("Version-Code", "V" + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName)
                .header("Client-Type", "Android")
                .header("User-Agent", userAgent)
                .post(requestBody).url(context.getString(R.string.resource_domain) + "rest/charge").build();
        OkHttpClient okHttpClient = OkHttpUtils.GetOkHttpClient(NetWorkTimeoutConstant.CHARGE_NETWORK_TIMEOUT, context);
        Response response = okHttpClient.newCall(request).execute();
        if (response.isSuccessful()) {
            return new Gson().fromJson(response.body().string(), new TypeToken<DataJsonResult<EncryptedData>>() {
            }.getType());
        }
        throw new ResponseStatusCodeException("服务暂不可用");
    }
}
