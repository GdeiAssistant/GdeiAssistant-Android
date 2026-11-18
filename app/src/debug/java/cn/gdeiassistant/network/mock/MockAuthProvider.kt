package cn.gdeiassistant.network.mock

import okhttp3.Request

object MockAuthProvider {

    fun mockLogin(request: Request): String {
        MockCampusCredentialProvider.resetForLogin(request)
        return """
            {"success":true,"code":200,"message":"success","data":{
                "token":"mock_jwt_${System.currentTimeMillis()}"
            }}
        """.trimIndent()
    }
}
