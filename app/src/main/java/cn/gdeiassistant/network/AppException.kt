package cn.gdeiassistant.network

import cn.gdeiassistant.R
import java.io.IOException

/**
 * 携带后端返回的 message 与 HTTP 状态码的统一异常。
 * 用于拦截器、SafeApiCall 及 UI 层展示后端原始错误文案。
 * 继承 IOException，使 OkHttp 异步调用通过 onFailure 传递错误而不抛出未捕获异常。
 */
class AppException(
    message: String?,
    val code: Int = -1
) : IOException(
    message
        ?: AppContextProvider.contextOrNull?.getString(R.string.network_error_generic)
        ?: "Request failed"
)
