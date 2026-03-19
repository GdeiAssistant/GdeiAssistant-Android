package cn.gdeiassistant.network.mock

import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.Request
import okio.Buffer
import java.net.URLDecoder

/** Shared utility functions and constants for mock providers. */
object MockUtils {

    const val MOCK_CURRENT_USERNAME = "gdeiassistant"
    const val MOCK_STUDENT_NUMBER = "2023001746"
    const val MOCK_PROFILE_NICKNAME = "林知远"
    const val MOCK_PROFILE_WECHAT = "linzhiyuan"

    const val MOCK_CAPTCHA_BASE64 =
        "iVBORw0KGgoAAAANSUhEUgAAAMAAAAA8CAIAAACVO0mNAAAA/0lEQVR4nO3TsQ3DMBQFQdP9d7aBkYzWxExA0RduCMsZ/HXXIQK5vCk1vZ1tc665fb3e8DqgJoCaAmoCqAmgJoCaAmoCqAmgJoCaAmoCqAmgJoCaAmoCqAmgJoCaAmoCqAmgJoCaAmoCqAmgJoCaAmoCqAmgJoCaAmoCqAmgJoCaAmoCqAmgJoCaAmoCqAmgJoCaAmoCqAmgJoCaAmoCqAmgJoCaAmoCqAmgJoCaAmoCqAmgJoCaAmoCqAmgJoCaAmoCqAmgJoCaAmoCqAmgJoCaAmoCqAmgJoCaAmoCqAmgJoCaAmoCqAmgJoCaAmoCqAmgJqBv+B8C9ZV8o8AAAAASUVORK5CYII="

    fun successDataJson(data: Any?, message: String = ""): String {
        return Gson().toJson(
            linkedMapOf(
                "success" to true,
                "code" to 200,
                "message" to message,
                "data" to data
            )
        )
    }

    fun successJson(message: String): String {
        return successDataJson(data = null, message = message)
    }

    fun failureJson(message: String): String =
        """{"success":false,"code":500,"message":"$message","data":null}"""

    fun escapeJson(value: String?): String {
        return value.orEmpty()
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
    }

    fun Request.formFields(): Map<String, String> {
        val body = body ?: return emptyMap()
        val buffer = Buffer()
        body.writeTo(buffer)
        return buffer.readUtf8()
            .split('&')
            .filter { it.contains('=') }
            .associate { entry ->
                val (key, value) = entry.split('=', limit = 2)
                URLDecoder.decode(key, "UTF-8") to URLDecoder.decode(value, "UTF-8")
            }
    }

    fun Request.bodyUtf8(): String {
        val body = body ?: return ""
        val buffer = Buffer()
        body.writeTo(buffer)
        return buffer.readUtf8()
    }

    fun Request.jsonObjectBody(): JsonObject? {
        val raw = bodyUtf8()
        if (raw.isBlank()) return null
        return runCatching { Gson().fromJson(raw, JsonObject::class.java) }.getOrNull()
    }

    fun JsonObject.getString(key: String): String? =
        get(key)?.takeIf { it.isJsonPrimitive }?.asString

    fun JsonObject.getInteger(key: String): Int? =
        get(key)?.takeIf { it.isJsonPrimitive }?.asInt

    fun JsonObject.getBoolean(key: String): Boolean? =
        get(key)?.takeIf { it.isJsonPrimitive }?.asBoolean

    fun Request.itemIdFromPath(): String? {
        val segments = url.pathSegments
        val idIndex = segments.indexOf("id")
        return segments.getOrNull(idIndex + 1)
    }

    fun Request.pathValueAfter(key: String): String? {
        val segments = url.pathSegments
        val targetIndex = segments.indexOf(key)
        return segments.getOrNull(targetIndex + 1)
    }

    fun Request.multipartField(name: String): String? {
        val raw = bodyUtf8()
        if (raw.isBlank()) return null
        val segments = raw.split("--")
        return segments.firstNotNullOfOrNull { segment ->
            if (!segment.contains("name=\"$name\"")) {
                null
            } else {
                segment.substringAfter("\r\n\r\n", "")
                    .substringBeforeLast("\r\n", "")
                    .trim()
                    .takeIf { it.isNotBlank() }
            }
        }
    }

    fun Request.multipartImageCount(): Int {
        return Regex("name=\"image\\d+\"")
            .findAll(bodyUtf8())
            .count()
    }

    fun Request.hasMultipartPart(name: String): Boolean {
        return bodyUtf8().contains("name=\"$name\"")
    }
}
