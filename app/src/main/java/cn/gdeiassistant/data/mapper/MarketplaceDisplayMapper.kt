package cn.gdeiassistant.data.mapper

import cn.gdeiassistant.R
import cn.gdeiassistant.data.text.DisplayTextResolver
import cn.gdeiassistant.model.MarketplaceDetail
import cn.gdeiassistant.model.MarketplaceItem
import cn.gdeiassistant.model.MarketplacePersonalSummary
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Applies display-layer formatting to raw marketplace domain objects returned by
 * MarketplaceRepository. Resolves localized fallback strings that were previously
 * baked into the repository layer.
 */
@Singleton
class MarketplaceDisplayMapper @Inject constructor(
    private val textResolver: DisplayTextResolver
) {

    fun applyItemDefaults(item: MarketplaceItem): MarketplaceItem {
        return item.copy(
            title = item.title.ifBlank { textResolver.getString(R.string.marketplace_default_title) },
            summary = item.summary.ifBlank { textResolver.getString(R.string.marketplace_default_summary) },
            sellerName = item.sellerName.ifBlank { textResolver.getString(R.string.marketplace_default_seller) },
            postedAt = item.postedAt.ifBlank { textResolver.getString(R.string.marketplace_default_posted_at) },
            location = item.location.ifBlank { textResolver.getString(R.string.marketplace_default_location) }
        )
    }

    fun applyItemDefaults(items: List<MarketplaceItem>): List<MarketplaceItem> {
        return items.map(::applyItemDefaults)
    }

    fun applyDetailDefaults(detail: MarketplaceDetail): MarketplaceDetail {
        return detail.copy(
            item = applyItemDefaults(detail.item),
            description = detail.description.ifBlank { textResolver.getString(R.string.marketplace_description_empty) },
            contactHint = detail.contactHint.ifBlank { textResolver.getString(R.string.marketplace_contact_private) },
            sellerGrade = detail.sellerGrade ?: detail.sellerEnrollment?.let {
                textResolver.getString(R.string.marketplace_seller_grade_suffix, it)
            }
        )
    }

    fun applyPersonalSummaryDefaults(summary: MarketplacePersonalSummary): MarketplacePersonalSummary {
        return summary.copy(
            nickname = summary.nickname.ifBlank { textResolver.getString(R.string.profile_default_username) },
            introduction = summary.introduction.ifBlank { textResolver.getString(R.string.profile_subtitle_default) },
            doing = applyItemDefaults(summary.doing),
            sold = applyItemDefaults(summary.sold),
            off = applyItemDefaults(summary.off)
        )
    }

    fun buildContactHint(qq: String?, phone: String?): String {
        return listOfNotNull(
            qq?.trim()?.takeIf { it.isNotBlank() }?.let { textResolver.getString(R.string.marketplace_contact_qq, it) },
            phone?.trim()?.takeIf { it.isNotBlank() }?.let { textResolver.getString(R.string.marketplace_contact_phone, it) }
        ).joinToString(" / ")
    }
}
