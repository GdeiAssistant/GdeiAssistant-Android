package edu.gdei.gdeiassistant.NetWork;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.gdei.gdeiassistant.Constant.NetWorkTimeoutConstant;
import edu.gdei.gdeiassistant.Exception.ResponseStatusCodeException;
import edu.gdei.gdeiassistant.Pojo.JsonResult.DataJsonResult;
import edu.gdei.gdeiassistant.Pojo.Upgrade.CheckUpgradeResult;
import edu.gdei.gdeiassistant.Tools.OkHttpUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CheckUpgradeNetWork {

    /**
     * 检查更新网络请求
     *
     * @return
     */
    public DataJsonResult<CheckUpgradeResult> GetUpgradeInformation(Context context) throws Exception {
        OkHttpClient okHttpClient = OkHttpUtils.GetOkHttpClient(NetWorkTimeoutConstant.CHECK_UPGRADE_NETWORK_TIMEOUT, context);
        Request request = new Request.Builder().url("https://www.gdeiassistant.cn/rest/update/android").build();
        Response response = okHttpClient.newCall(request).execute();
        if (response.isSuccessful()) {
            return new Gson().fromJson(response.body().string()
                    , new TypeToken<DataJsonResult<CheckUpgradeResult>>() {
                    }.getType());
        }
        throw new ResponseStatusCodeException("服务暂不可用");
    }
}
