package cn.gdeiassistant.ui.messages

import androidx.navigation.NavController
import cn.gdeiassistant.model.InteractionMessage
import cn.gdeiassistant.ui.navigation.Routes

internal fun NavController.openInteractionMessage(item: InteractionMessage) {
    item.destinationRoute()?.let { navigate(it) }
}

private fun InteractionMessage.destinationRoute(): String? {
    val normalizedModule = normalizedModule()
    val normalizedTargetType = targetType?.trim()?.lowercase().orEmpty()
    val target = targetId?.trim().orEmpty()

    return when (normalizedModule) {
        "secret" -> if (target.isNotEmpty()) Routes.secretDetail(target) else Routes.SECRET
        "express" -> if (target.isNotEmpty()) Routes.expressDetail(target) else Routes.EXPRESS
        "topic" -> if (target.isNotEmpty()) Routes.topicDetail(target) else Routes.TOPIC
        "photograph" -> if (target.isNotEmpty()) Routes.photographDetail(target) else Routes.PHOTOGRAPH
        "delivery" -> if (target.isNotEmpty()) Routes.deliveryDetail(target) else Routes.DELIVERY
        "marketplace" -> if (target.isNotEmpty()) Routes.marketplaceDetail(target) else Routes.MARKETPLACE
        "lostandfound" -> if (target.isNotEmpty()) Routes.lostFoundDetail(target) else Routes.LOST_FOUND
        "dating" -> Routes.datingCenter(
            tab = when (normalizedTargetType) {
                "sent" -> "sent"
                "posts", "published" -> "posts"
                else -> "received"
            }
        )
        else -> null
    }
}

private fun InteractionMessage.normalizedModule(): String? {
    return when (module?.trim()?.lowercase().orEmpty()) {
        "ershou", "secondhand" -> "marketplace"
        "lost_found", "lostfound" -> "lostandfound"
        "roommate" -> "dating"
        else -> module?.trim()?.lowercase()?.takeIf(String::isNotBlank)
    }
}
