package cn.gdeiassistant.model

import androidx.compose.runtime.Immutable
import java.io.Serializable

@Immutable
data class Card(
    val tradeTime: String? = null,
    val merchantName: String? = null,
    val tradeName: String? = null,
    val tradePrice: String? = null,
    val accountBalance: String? = null
) : Serializable

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

@Immutable
data class CardQueryResult(
    val cardInfo: CardInfo? = null,
    val cardList: List<Card>? = null
) : Serializable

@Immutable
data class Charge(
    val alipayURL: String? = null,
    val cookieList: List<Cookie>? = null,
    val orderId: String? = null,
    val status: String? = null,
    val message: String? = null,
    val retryAfter: Int? = null
) : Serializable

@Immutable
data class ChargeOrder(
    val orderId: String? = null,
    val amount: Int? = null,
    val status: String? = null,
    val message: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val submittedAt: String? = null,
    val completedAt: String? = null,
    val retryAfter: Int? = null
) : Serializable

object ChargeOrderStatuses {
    const val CREATED = "CREATED"
    const val PROCESSING = "PROCESSING"
    const val PAYMENT_SESSION_CREATED = "PAYMENT_SESSION_CREATED"
    const val FAILED = "FAILED"
    const val UNKNOWN = "UNKNOWN"
    const val MANUAL_REVIEW = "MANUAL_REVIEW"
}
