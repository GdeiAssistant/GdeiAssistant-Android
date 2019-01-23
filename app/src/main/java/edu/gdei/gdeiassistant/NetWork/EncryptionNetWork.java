package edu.gdei.gdeiassistant.NetWork;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.gdei.gdeiassistant.Constant.NetWorkTimeoutConstant;
import edu.gdei.gdeiassistant.Exception.ResponseStatusCodeException;
import edu.gdei.gdeiassistant.Pojo.JsonResult.DataJsonResult;
import edu.gdei.gdeiassistant.Tools.OkHttpUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class EncryptionNetWork {

    /**
     * 获取服务端RSA公钥
     *
     * @param context
     * @return
     * @throws Exception
     */
    public DataJsonResult<String> GetServerPublicKey(Context context) throws Exception {
        OkHttpClient okHttpClient = OkHttpUtils.GetOkHttpClient(NetWorkTimeoutConstant.ENCRYPT_NETWORK_TIMEOUT, context);
        Request request = new Request.Builder().url("https://www.gdeiassistant.cn/rest/encryption/rsa/publickey").build();
        Response response = okHttpClient.newCall(request).execute();
        if (response.isSuccessful()) {
            return new Gson().fromJson(response.body().string()
                    , new TypeToken<DataJsonResult<String>>() {
                    }.getType());
        }
        throw new ResponseStatusCodeException("服务暂不可用");
    }
}
