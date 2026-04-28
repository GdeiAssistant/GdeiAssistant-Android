package cn.gdeiassistant.network.mock

import okhttp3.Request

/** Mock provider for authentication and app upgrade endpoints. */
object MockAuthProvider {

    fun mockLogin(request: Request): String {
        MockCampusCredentialProvider.resetForLogin(request)
        return """
            {"success":true,"code":200,"message":"success","data":{
                "token":"mock_jwt_${System.currentTimeMillis()}"
            }}
        """.trimIndent()
    }

    fun mockUpgrade(request: Request): String = """
        {"success":true,"code":200,"message":"","data":{
            "downloadURL":"https://gdeiassistant.cn/download/android/mock-release.apk",
            "versionInfo":"Mock 模式下的升级说明：用于验证关于页、下载按钮与版本提示链路。",
            "versionCodeName":"2.1.0-dev",
            "fileSize":"18.2 MB",
            "versionCode":99
        }}
    """.trimIndent()
}
