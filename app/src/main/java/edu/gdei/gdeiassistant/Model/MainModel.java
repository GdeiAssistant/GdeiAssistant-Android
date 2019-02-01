package edu.gdei.gdeiassistant.Model;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationToken;
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import edu.gdei.gdeiassistant.Application.GdeiAssistantApplication;
import edu.gdei.gdeiassistant.Constant.MainTagConstant;
import edu.gdei.gdeiassistant.Constant.RequestConstant;
import edu.gdei.gdeiassistant.Exception.ResponseStatusCodeException;
import edu.gdei.gdeiassistant.NetWork.AccessNetWork;
import edu.gdei.gdeiassistant.NetWork.OSSNetWork;
import edu.gdei.gdeiassistant.NetWork.ProfileNetWork;
import edu.gdei.gdeiassistant.Pojo.Entity.Access;
import edu.gdei.gdeiassistant.Pojo.Entity.Profile;
import edu.gdei.gdeiassistant.Pojo.JsonResult.DataJsonResult;
import edu.gdei.gdeiassistant.Pojo.OssFederationToken.OssFederationTokenResult;

public class MainModel {

    private OSSNetWork ossNetWork = new OSSNetWork();

    private ProfileNetWork profileNetWork = new ProfileNetWork();

    private AccessNetWork accessNetWork = new AccessNetWork();

    /**
     * 获取用户权限列表
     *
     * @param handler
     * @param context
     */
    public void GetUserAccess(final Handler handler, final Context context) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                Message message = handler.obtainMessage();
                Bundle bundle = new Bundle();
                try {
                    bundle.putInt("Tag", MainTagConstant.GET_USER_ACCESS);
                    DataJsonResult<Access> result = accessNetWork.GetAndroidAccess(context);
                    if (Boolean.TRUE.equals(result.isSuccess())) {
                        if (result.getData() != null) {
                            bundle.putSerializable("Access", result.getData());
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

    /**
     * 获取用户资料
     *
     * @param handler
     * @param context
     */
    public void GetUserProfile(final Handler handler, final Context context) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                Message message = handler.obtainMessage();
                Bundle bundle = new Bundle();
                try {
                    bundle.putInt("Tag", MainTagConstant.GET_USER_PROFILE);
                    DataJsonResult<Profile> result = profileNetWork.GetUserProfile(context);
                    if (Boolean.TRUE.equals(result.isSuccess())) {
                        if (result.getData() != null) {
                            bundle.putSerializable("Profile", result.getData());
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

    /**
     * 下载头像文件
     *
     * @param handler
     * @param context
     */
    public void DownloadAvatarToOSS(final Handler handler, final Context context) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                File avatarDirectory = new File(context.getCacheDir() + "/gdeiassistant/");
                if (!avatarDirectory.exists()) {
                    avatarDirectory.mkdirs();
                }
                InputStream inputStream = null;
                OutputStream outputStream = null;
                Message message = handler.obtainMessage();
                Bundle bundle = new Bundle();
                try {
                    bundle.putInt("Tag", MainTagConstant.DOWNLOAD_AVATAR);
                    DataJsonResult<OssFederationTokenResult> result = ossNetWork.GetOSSFederationToken(context);
                    if (Boolean.TRUE.equals(result.isSuccess())) {
                        if (result.getData() != null && result.getData().getStatus().equals("200")) {
                            //获取临时访问令牌成功
                            OssFederationTokenResult ossFederationTokenResult = result.getData();
                            OSSFederationToken ossFederationToken = new OSSFederationToken(ossFederationTokenResult.getAccessKeyId()
                                    , ossFederationTokenResult.getAccessKeySecret(), ossFederationTokenResult.getSecurityToken()
                                    , ossFederationTokenResult.getExpiration());
                            String endPoint = "oss-cn-shenzhen.aliyuncs.com";
                            String accessKeyId = ossFederationToken.getTempAK();
                            String accessKeySecret = ossFederationToken.getTempSK();
                            String securityToken = ossFederationToken.getSecurityToken();
                            //创建OSSClient实例
                            OSSCredentialProvider credentialProvider = new OSSStsTokenCredentialProvider(accessKeyId
                                    , accessKeySecret, securityToken);
                            OSS oss = new OSSClient(context, endPoint, credentialProvider);
                            String bucketName = "gdeiassistant-userdata";
                            String objectKey = "avatar/" + ((GdeiAssistantApplication) context).getUsername() + ".jpg";
                            if (oss.doesObjectExist(bucketName, objectKey)) {
                                GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, objectKey);
                                //同步执行下载请求，返回结果
                                GetObjectResult getObjectResult = oss.getObject(getObjectRequest);
                                //获取文件输入流
                                inputStream = getObjectResult.getObjectContent();
                                File file = new File(context.getCacheDir() + "/gdeiassistant/avatar.jpg");
                                //写入头像文件
                                outputStream = new FileOutputStream(file);
                                byte[] buffer = new byte[2048];
                                int length;
                                while ((length = inputStream.read(buffer)) != -1) {
                                    // 处理下载的数据，比如图片展示或者写入文件等
                                    outputStream.write(buffer, 0, length);
                                }
                                outputStream.flush();
                                message.what = RequestConstant.REQUEST_SUCCESS;
                            } else {
                                //没有上传头像,使用默认头像
                                message.what = RequestConstant.EMPTY_RESULT;
                            }
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
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException ignored) {

                        }
                    }
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException ignored) {

                        }
                    }
                }
                message.setData(bundle);
                handler.sendMessage(message);
            }
        }.start();
    }

    /**
     * 上传头像文件到OSS对象存储
     *
     * @param handler
     * @param context
     * @param file
     */
    public void UploadAvatarToOSS(final Handler handler, final Context context, final File file) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                Message message = handler.obtainMessage();
                Bundle bundle = new Bundle();
                try {
                    bundle.putInt("Tag", MainTagConstant.UPLOAD_AVATAR);
                    //获取临时访问令牌
                    DataJsonResult<OssFederationTokenResult> result = ossNetWork.GetOSSFederationToken(context);
                    if (Boolean.TRUE.equals(result.isSuccess())) {
                        if (result.getData() != null && result.getData().getStatus().equals("200")) {
                            //获取临时访问令牌成功
                            OssFederationTokenResult ossFederationTokenResult = result.getData();
                            OSSFederationToken ossFederationToken = new OSSFederationToken(ossFederationTokenResult.getAccessKeyId()
                                    , ossFederationTokenResult.getAccessKeySecret(), ossFederationTokenResult.getSecurityToken()
                                    , ossFederationTokenResult.getExpiration());
                            String endPoint = "oss-cn-shenzhen.aliyuncs.com";
                            String accessKeyId = ossFederationToken.getTempAK();
                            String accessKeySecret = ossFederationToken.getTempSK();
                            String securityToken = ossFederationToken.getSecurityToken();
                            //创建OSSClient实例
                            OSSCredentialProvider credentialProvider = new OSSStsTokenCredentialProvider(accessKeyId, accessKeySecret, securityToken);
                            OSSClient ossClient = new OSSClient(context, endPoint, credentialProvider);
                            //上传头像文件
                            if (file.exists() && file.canRead() && file.canWrite()) {
                                PutObjectRequest putObjectRequest = new PutObjectRequest("gdeiassistant-userdata", "avatar/"
                                        + ((GdeiAssistantApplication) context).getUsername() + ".jpg", file.getPath());
                                ossClient.putObject(putObjectRequest);
                                file.renameTo(new File(context.getCacheDir() + "/gdeiassistant/avatar.jpg"));
                                message.what = RequestConstant.REQUEST_SUCCESS;
                            } else {
                                message.what = RequestConstant.CLIENT_ERROR;
                            }
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
