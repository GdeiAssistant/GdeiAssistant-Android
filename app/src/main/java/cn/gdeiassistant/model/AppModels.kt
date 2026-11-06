package cn.gdeiassistant.model

import androidx.compose.runtime.Immutable
import java.io.Serializable

@Immutable
data class Book(
    val id: String? = null,
    val sn: String? = null,
    val code: String? = null,
    val name: String? = null,
    val author: String? = null,
    val borrowDate: String? = null,
    val returnDate: String? = null,
    val renewTime: Int? = null
) : Serializable
