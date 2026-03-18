package cn.gdeiassistant.model

import androidx.compose.runtime.Immutable
import com.alibaba.fastjson.annotation.JSONField
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
@Immutable
data class Token(
    val signature: String? = null,
    val createTime: Long? = null,
    val expireTime: Long? = null
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
@Immutable
data class UserLoginResult(
    val token: String? = null
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
@Immutable
data class TokenRefreshResult(
    val accessToken: Token? = null,
    val refreshToken: Token? = null
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
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

@JsonIgnoreProperties(ignoreUnknown = true)
@Immutable
data class Cookie(
    @JSONField(ordinal = 1) val name: String? = null,
    @JSONField(ordinal = 2) val value: String? = null,
    @JSONField(ordinal = 3) val domain: String? = null
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
@Immutable
data class Profile(
    val avatarURL: String? = null,
    val username: String? = null,
    val nickname: String? = null
) : Serializable
