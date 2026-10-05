package cn.gdeiassistant.network

import cn.gdeiassistant.R

/**
 * 携带后端返回的 message、HTTP/业务 code 与稳定 errorCode 的统一异常。
 * 用于拦截器、SafeApiCall 及 UI 层展示后端原始错误文案。
 */
class AppException(
    message: String?,
    val code: Int = -1,
    val errorCode: String? = null
) : RuntimeException(
    message
        ?: AppContextProvider.contextOrNull?.getString(R.string.network_error_generic)
        ?: "Request failed"
)
