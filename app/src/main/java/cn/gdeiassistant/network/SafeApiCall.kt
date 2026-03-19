package cn.gdeiassistant.network

import cn.gdeiassistant.model.DataJsonResult
import cn.gdeiassistant.model.JsonResult
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import retrofit2.HttpException
import java.io.IOException

/**
 * 统一封装 API 调用：将 DataJsonResult 转为 Result。
 * 优先使用后端返回的 message（AppException / success=false 的 message），否则使用兜底文案。
 */
suspend fun <T> safeApiCall(block: suspend () -> DataJsonResult<T>): Result<T?> {
    return safeResultCall(
        block = block,
        isSuccessful = { it.success == true },
        errorMessage = { it.message },
        errorCode = { it.code }
    ) { response ->
        response.data
    }
}

/**
 * 对无 data 的 JsonResult 接口（如评教提交）的统一封装。
 */
suspend fun safeJsonResultCall(block: suspend () -> JsonResult): Result<Unit> {
    return safeResultCall(
        block = block,
        isSuccessful = { it.success == true },
        errorMessage = { it.message },
        errorCode = { it.code }
    ) {
        Unit
    }
}

private suspend inline fun <Response, ResultType> safeResultCall(
    crossinline block: suspend () -> Response,
    crossinline isSuccessful: (Response) -> Boolean,
    crossinline errorMessage: (Response) -> String?,
    crossinline errorCode: (Response) -> Int?,
    crossinline successValue: (Response) -> ResultType
): Result<ResultType> {
    return try {
        val response = block()
        if (isSuccessful(response)) {
            Result.success(successValue(response))
        } else {
            Result.failure(
                AppException(
                    errorMessage(response) ?: NetworkConstants.MESSAGE_REQUEST_FAILED,
                    errorCode(response) ?: -1
                )
            )
        }
    } catch (e: AppException) {
        Result.failure(e)
    } catch (e: HttpException) {
        val message = e.response()?.errorBody()?.string()?.let { parseMessageFromErrorBody(it) }
            ?: when (e.code()) {
                NetworkConstants.HTTP_UNAUTHORIZED -> NetworkConstants.MESSAGE_LOGIN_EXPIRED
                NetworkConstants.HTTP_FORBIDDEN -> NetworkConstants.MESSAGE_FORBIDDEN
                else -> NetworkConstants.MESSAGE_SERVER_ERROR.format(e.code())
            }
        Result.failure(AppException(message, e.code()))
    } catch (e: IOException) {
        Result.failure(AppException(NetworkConstants.MESSAGE_NETWORK_ERROR, -1))
    } catch (e: Exception) {
        Result.failure(e)
    }
}

private fun parseMessageFromErrorBody(bodyStr: String): String? {
    if (bodyStr.isBlank()) return null
    return try {
        (com.google.gson.Gson().fromJson(bodyStr, JsonObject::class.java)?.get("message") as? JsonPrimitive)?.asString
    } catch (_: Exception) {
        null
    }
}
