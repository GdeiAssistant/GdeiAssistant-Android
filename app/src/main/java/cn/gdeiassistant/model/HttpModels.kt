package cn.gdeiassistant.model

import java.io.Serializable

data class JsonResult(
    val success: Boolean? = null,
    val code: Int? = null,
    val message: String? = null
) : Serializable

data class DataJsonResult<T>(
    val success: Boolean? = null,
    val code: Int? = null,
    val message: String? = null,
    val data: T? = null
) : Serializable
