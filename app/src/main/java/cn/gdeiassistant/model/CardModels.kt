package cn.gdeiassistant.model

import androidx.compose.runtime.Immutable
import com.alibaba.fastjson.annotation.JSONField
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
@Immutable
data class Card(
    val tradeTime: String? = null,
    val merchantName: String? = null,
    val tradeName: String? = null,
    val tradePrice: String? = null,
    val accountBalance: String? = null
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
@Immutable
data class CardInfo(
    val name: String? = null,
    val number: String? = null,
    val cardBalance: String? = null,
    val cardInterimBalance: String? = null,
    val cardNumber: String? = null,
    val cardLostState: String? = null,
    val cardFreezeState: String? = null
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
@Immutable
data class CardQueryResult(
    val cardInfo: CardInfo? = null,
    val cardList: List<Card>? = null
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
@Immutable
data class Charge(
    @JSONField(ordinal = 1) val alipayURL: String? = null,
    @JSONField(ordinal = 2) val cookieList: List<Cookie>? = null
) : Serializable
