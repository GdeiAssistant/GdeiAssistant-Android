package cn.gdeiassistant.network

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

    /** Token 刷新专用客户端超时（秒），避免递归拦截 */
    const val REFRESH_CLIENT_TIMEOUT_SECONDS = 10

    /** 401 时兜底提示（网络层无 Context，与 strings.login_expired_toast 保持一致） */
    const val MESSAGE_LOGIN_EXPIRED = "登录已过期，请重新登录"

    const val MESSAGE_FORBIDDEN = "权限不足"
    const val MESSAGE_NETWORK_ERROR = "网络异常，请检查网络连接"
    const val MESSAGE_REQUEST_FAILED = "请求失败"
    const val MESSAGE_SERVER_ERROR = "服务器错误: %d"
}
