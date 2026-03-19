package cn.gdeiassistant.model

import androidx.compose.runtime.Immutable
import java.io.Serializable

@Immutable
data class UserLoginResult(
    val token: String? = null
) : Serializable

@Immutable
data class Access(
    val grade: Boolean? = null,
    val schedule: Boolean? = null,
    val cet: Boolean? = null,
    val evaluate: Boolean? = null,
    val book: Boolean? = null,
    val card: Boolean? = null,
    val bill: Boolean? = null,
    val lost: Boolean? = null,
    val charge: Boolean? = null
) : Serializable

@Immutable
data class Cookie(
    val name: String? = null,
    val value: String? = null,
    val domain: String? = null
) : Serializable

@Immutable
data class Profile(
    val avatarURL: String? = null,
    val username: String? = null,
    val nickname: String? = null
) : Serializable
