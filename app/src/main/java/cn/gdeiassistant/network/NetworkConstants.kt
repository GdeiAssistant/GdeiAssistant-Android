package cn.gdeiassistant.network

import cn.gdeiassistant.R

/**
 * 网络层常量：HTTP 状态码与超时时间，避免魔法数字。
 */
object NetworkConstants {

    const val HTTP_UNAUTHORIZED = 401
    const val HTTP_FORBIDDEN = 403

    /** 连接超时（秒） */
    const val CONNECT_TIMEOUT_SECONDS = 15

    /** 读写超时（秒） */
    const val READ_WRITE_TIMEOUT_SECONDS = 20

    /* ---- localised error messages resolved via AppContextProvider ---- */

    fun messageLoginExpired(): String =
        AppContextProvider.context.getString(R.string.login_expired_toast)

    fun messageForbidden(): String =
        AppContextProvider.context.getString(R.string.network_error_forbidden)

    fun messageNetworkError(): String =
        AppContextProvider.context.getString(R.string.network_error_generic)

    fun messageRequestFailed(): String =
        AppContextProvider.context.getString(R.string.network_error_request_failed)

    fun messageServerError(code: Int): String =
        AppContextProvider.context.getString(R.string.network_error_server, code)
}
