package cn.gdeiassistant.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class JsonResult(
    @get:JvmName("isSuccess") val success: Boolean? = null,
    val code: Int? = null,
    val message: String? = null
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class DataJsonResult<T>(
    val success: Boolean? = null,
    val code: Int? = null,
    val message: String? = null,
    val data: T? = null
) : Serializable
