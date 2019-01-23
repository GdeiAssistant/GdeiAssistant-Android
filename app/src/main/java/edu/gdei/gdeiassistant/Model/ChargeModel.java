package edu.gdei.gdeiassistant.Model;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;

import com.alibaba.fastjson.JSON;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.gdei.gdeiassistant.Constant.ChargeTagConstant;
import edu.gdei.gdeiassistant.Constant.RequestConstant;
import edu.gdei.gdeiassistant.Exception.ResponseStatusCodeException;
import edu.gdei.gdeiassistant.NetWork.CardQueryNetWork;
import edu.gdei.gdeiassistant.NetWork.ChargeNetWork;
import edu.gdei.gdeiassistant.NetWork.EncryptionNetWork;
import edu.gdei.gdeiassistant.Pojo.Entity.CardInfo;
import edu.gdei.gdeiassistant.Pojo.Entity.Charge;
import edu.gdei.gdeiassistant.Pojo.Entity.EncryptedData;
import edu.gdei.gdeiassistant.Pojo.JsonResult.DataJsonResult;
import edu.gdei.gdeiassistant.R;
import edu.gdei.gdeiassistant.Tools.AESUtils;
import edu.gdei.gdeiassistant.Tools.RSAUtils;
import edu.gdei.gdeiassistant.Tools.StringUtils;

public class ChargeModel {

    private ChargeNetWork chargeNetWork = new ChargeNetWork();

    private CardQueryNetWork cardQueryNetWork = new CardQueryNetWork();

    private EncryptionNetWork encryptionNetWork = new EncryptionNetWork();

    /**
     * 提交充值订单
     *
     * @param handler
     */
    public void ChargeSubmit(final Handler handler, final Integer amount, final String userAgent, final Context context) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                Message message = handler.obtainMessage();
                Bundle bundle = new Bundle();
                try {
                    message.what = RequestConstant.SHOW_PROGRESS;
                    handler.sendMessage(message);
                    message = handler.obtainMessage();
                    bundle.putInt("Tag", ChargeTagConstant.SUBMIT_CHARGE);
                    //获取服务端RSA公钥
                    DataJsonResult<String> result = encryptionNetWork.GetServerPublicKey(context);
                    if (result.isSuccess()) {
                        String serverRSAPublicKey = result.getData();
                        //生成请求随机值、时间戳和签名
                        String nonce = StringUtils.getUUID();
                        Long timestamp = new Date().getTime();
                        String signature = new String(Hex.encodeHex(DigestUtils.sha1(timestamp + nonce + context.getString(R.string.request_validate_token))));
                        //生成客户端RSA公钥
                        String clientRSAPublicKey = Base64.encodeToString(RSAUtils.GetPublicKey(), Base64.NO_WRAP);
                        //生成对称加密AES密钥
                        String originClientAESKey = Base64.encodeToString(AESUtils.GenerateSecretKey(), Base64.NO_WRAP);
                        //使用服务端RSA公钥加密AES密钥
                        String encryptedClientAESKey = Base64.encodeToString(RSAUtils.EncryptByteWithPublicKey(Base64.decode(serverRSAPublicKey, Base64.NO_WRAP)
                                , Base64.decode(originClientAESKey, Base64.NO_WRAP)), Base64.NO_WRAP);
                        //请求参数和随机值、时间戳共同生成客户端数字签名
                        Map<String, Object> objectMap = new LinkedHashMap<>();
                        objectMap.put("amount", amount);
                        objectMap.put("nonce", nonce);
                        objectMap.put("timestamp", timestamp);
                        String clientRSASignature = Base64.encodeToString(RSAUtils.EncryptByteWithPrivateKey(RSAUtils.GetPrivateKey()
                                , new String(Hex.encodeHex(DigestUtils.sha1(JSON.toJSONString(objectMap)))).getBytes()), Base64.NO_WRAP);
                        DataJsonResult<EncryptedData> chargeResult = chargeNetWork.ChargeRequest(String.valueOf(amount), userAgent
                                , nonce, timestamp, signature, clientRSAPublicKey, encryptedClientAESKey, clientRSASignature, context);
                        if (Boolean.TRUE.equals(chargeResult.isSuccess())) {
                            if (chargeResult.getData() != null) {
                                Charge charge = JSON.parseObject(new String(AESUtils.DecryptByte(Base64.decode(originClientAESKey
                                        , Base64.NO_WRAP), Base64.decode(chargeResult.getData().getData(), Base64.NO_WRAP))), Charge.class);
                                //校验服务端的数字签名
                                if (new String(Hex.encodeHex(DigestUtils.sha1(JSON.toJSONString(charge))))
                                        .equals(new String(RSAUtils.DecryptByteWithPublicKey(Base64.decode(serverRSAPublicKey, Base64.NO_WRAP)
                                                , Base64.decode(chargeResult.getData().getSignature(), Base64.NO_WRAP))))) {
                                    bundle.putSerializable("Charge", charge);
                                    message.what = RequestConstant.REQUEST_SUCCESS;
                                } else {
                                    message.what = RequestConstant.CLIENT_ERROR;
                                }
                            } else {
                                message.what = RequestConstant.SERVER_ERROR;
                            }
                        } else {
                            bundle.putString("Message", chargeResult.getMessage());
                            message.what = RequestConstant.REQUEST_FAILURE;
                        }
                    } else {
                        bundle.putString("Message", result.getMessage());
                        message.what = RequestConstant.REQUEST_FAILURE;
                    }
                } catch (NullPointerException ignored) {

                } catch (IOException e) {
                    message.what = RequestConstant.REQUEST_TIMEOUT;
                } catch (GeneralSecurityException e) {
                    message.what = RequestConstant.CLIENT_ERROR;
                } catch (ResponseStatusCodeException e) {
                    message.what = RequestConstant.SERVER_ERROR;
                } catch (Exception e) {
                    message.what = RequestConstant.UNKNOWN_ERROR;
                }
                message.setData(bundle);
                handler.sendMessage(message);
            }
        }.start();
    }

    /**
     * 获取校园卡基本信息
     *
     * @param handler
     * @param context
     */
    public void GetCardInfo(final Handler handler, final Context context) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                Message message = handler.obtainMessage();
                Bundle bundle = new Bundle();
                try {
                    message.what = RequestConstant.SHOW_PROGRESS;
                    handler.sendMessage(message);
                    message = handler.obtainMessage();
                    bundle.putInt("Tag", ChargeTagConstant.GET_CARD_INFO);
                    DataJsonResult<CardInfo> result = cardQueryNetWork.CardInfoQuery(context);
                    if (Boolean.TRUE.equals(result.isSuccess())) {
                        if (result.getData() != null) {
                            bundle.putSerializable("CardInfo", result.getData());
                            message.what = RequestConstant.REQUEST_SUCCESS;
                        } else {
                            message.what = RequestConstant.SERVER_ERROR;
                        }
                    } else {
                        bundle.putString("Message", result.getMessage());
                        message.what = RequestConstant.REQUEST_FAILURE;
                    }
                } catch (NullPointerException ignored) {

                } catch (IOException e) {
                    message.what = RequestConstant.REQUEST_TIMEOUT;
                } catch (ResponseStatusCodeException e) {
                    message.what = RequestConstant.SERVER_ERROR;
                } catch (Exception e) {
                    message.what = RequestConstant.UNKNOWN_ERROR;
                }
                message.setData(bundle);
                handler.sendMessage(message);
            }
        }.start();

    }
}
