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
        statusCode = { it.code },
        businessErrorCode = { it.errorCode }
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
        statusCode = { it.code },
        businessErrorCode = { it.errorCode }
    ) {
        Unit
    }
}

private suspend inline fun <Response, ResultType> safeResultCall(
    crossinline block: suspend () -> Response,
    crossinline isSuccessful: (Response) -> Boolean,
    crossinline errorMessage: (Response) -> String?,
    crossinline statusCode: (Response) -> Int?,
    crossinline businessErrorCode: (Response) -> String?,
    crossinline successValue: (Response) -> ResultType
): Result<ResultType> {
    return try {
        val response = block()
        if (isSuccessful(response)) {
            Result.success(successValue(response))
        } else {
            Result.failure(
                AppException(
                    message = errorMessage(response) ?: NetworkConstants.messageRequestFailed(),
                    code = statusCode(response) ?: -1,
                    errorCode = businessErrorCode(response)
                )
            )
        }
    } catch (e: AppException) {
        Result.failure(e)
    } catch (e: HttpException) {
        val parsed = e.response()?.errorBody()?.string()?.let(::parseErrorBody)
        val message = parsed?.first
            ?: when (e.code()) {
                NetworkConstants.HTTP_UNAUTHORIZED -> NetworkConstants.messageLoginExpired()
                NetworkConstants.HTTP_FORBIDDEN -> NetworkConstants.messageForbidden()
                else -> NetworkConstants.messageServerError(e.code())
            }
        Result.failure(AppException(message, e.code(), parsed?.second))
    } catch (e: IOException) {
        Result.failure(AppException(NetworkConstants.messageNetworkError(), -1))
    } catch (e: Exception) {
        Result.failure(e)
    }
}

private fun parseErrorBody(bodyStr: String): Pair<String?, String?>? {
    if (bodyStr.isBlank()) return null
    return try {
        val json = com.google.gson.Gson().fromJson(bodyStr, JsonObject::class.java) ?: return null
        val message = (json.get("message") as? JsonPrimitive)?.asString
        val errorCode = (json.get("errorCode") as? JsonPrimitive)?.asString
        message to errorCode
    } catch (_: Exception) {
        null
    }
}
