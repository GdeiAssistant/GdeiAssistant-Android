package cn.gdeiassistant.ui.marketplace

import cn.gdeiassistant.R

/**
 * Shared rules for the marketplace publish and edit forms. Both screens used to carry the
 * same eleven-branch validation chain, so a limit change had to be applied twice.
 */
data class MarketplaceForm(
    val title: String,
    val price: Double?,
    val description: String,
    val location: String,
    val qq: String,
    val phone: String
)

object MarketplaceFormValidator {

    fun normalize(
        title: String,
        priceText: String,
        description: String,
        location: String,
        qq: String,
        phone: String
    ): MarketplaceForm = MarketplaceForm(
        title = title.trim(),
        price = priceText.trim().toDoubleOrNull(),
        description = description.trim(),
        location = location.trim(),
        qq = qq.trim(),
        phone = phone.trim()
    )

    /** String resource of the first failed rule, or null when the form is valid. */
    fun errorRes(form: MarketplaceForm): Int? = when {
        form.title.isBlank() -> R.string.marketplace_publish_title_required
        form.title.length > 25 -> R.string.marketplace_publish_title_too_long
        form.price == null || form.price <= 0.0 || form.price > 9999.99 ->
            R.string.marketplace_publish_price_invalid
        form.description.isBlank() -> R.string.marketplace_publish_description_required
        form.description.length > 100 -> R.string.marketplace_publish_description_too_long
        form.location.isBlank() -> R.string.marketplace_publish_location_required
        form.location.length > 30 -> R.string.marketplace_publish_location_too_long
        form.qq.isBlank() -> R.string.marketplace_publish_qq_required
        form.qq.length > 20 -> R.string.marketplace_publish_qq_too_long
        form.phone.length > 11 -> R.string.marketplace_publish_phone_too_long
        else -> null
    }

    /** Non-null price; call only after [errorRes] returned null. */
    fun price(form: MarketplaceForm): Double = requireNotNull(form.price) {
        "price() requires a validated form"
    }
}
