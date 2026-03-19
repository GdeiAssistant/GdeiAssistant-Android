package cn.gdeiassistant.network.mock

import okhttp3.Request

/** Mock provider for authentication and token related endpoints. */
object MockAuthProvider {

    fun mockLogin(request: Request): String = """
        {"success":true,"code":200,"message":"success","data":{
            "token":"mock_jwt_${System.currentTimeMillis()}"
        }}
    """.trimIndent()

    fun mockTokenRefresh(request: Request): String = """
        {"success":true,"code":200,"message":"","data":{
            "accessToken":{"signature":"mock_access_${System.currentTimeMillis()}","createTime":${System.currentTimeMillis()},"expireTime":${System.currentTimeMillis() + 3600000}},
            "refreshToken":{"signature":"mock_refresh_${System.currentTimeMillis()}","createTime":${System.currentTimeMillis()},"expireTime":${System.currentTimeMillis() + 86400000}}
        }}
    """.trimIndent()

    fun mockTokenExpire(request: Request): String =
        """{"success":true,"code":200,"message":"token 有效"}"""

    fun mockUpgrade(request: Request): String = """
        {"success":true,"code":200,"message":"","data":{
            "downloadURL":"https://gdeiassistant.cn/download/android/mock-release.apk",
            "versionInfo":"Mock 模式下的升级说明：用于验证关于页、下载按钮与版本提示链路。",
            "versionCodeName":"2.1.0-dev",
            "fileSize":"18.2 MB",
            "versionCode":99
        }}
    """.trimIndent()

    fun mockPresignedUrl(request: Request): String =
        """{"success":true,"code":200,"message":"","data":{"url":"https://api.gdeiassistant.cn/api/upload/mock/file","expiresIn":300}}"""

    fun mockUploadFile(request: Request): String =
        MockUtils.successJson("上传成功")
}
