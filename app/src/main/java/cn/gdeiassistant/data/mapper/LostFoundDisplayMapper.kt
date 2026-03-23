package cn.gdeiassistant.data.mapper

import cn.gdeiassistant.R
import cn.gdeiassistant.data.text.DisplayTextResolver
import cn.gdeiassistant.model.LostFoundDetail
import cn.gdeiassistant.model.LostFoundItem
import cn.gdeiassistant.model.LostFoundItemState
import cn.gdeiassistant.model.LostFoundPersonalSummary
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Applies display-layer formatting to raw lost-and-found domain objects returned by
 * LostFoundRepository. Resolves localized fallback strings that were previously
 * baked into the repository layer.
 */
@Singleton
class LostFoundDisplayMapper @Inject constructor(
    private val textResolver: DisplayTextResolver
) {

    fun applyItemDefaults(item: LostFoundItem): LostFoundItem {
        return item.copy(
            title = item.title.ifBlank { textResolver.getString(R.string.lost_found_default_title) },
            summary = item.summary.ifBlank { textResolver.getString(R.string.lost_found_default_summary) },
            location = item.location.ifBlank { textResolver.getString(R.string.lost_found_default_location) },
            createdAt = item.createdAt.ifBlank { textResolver.getString(R.string.common_just_now) }
        )
    }

    fun applyItemDefaults(items: List<LostFoundItem>): List<LostFoundItem> {
        return items.map(::applyItemDefaults)
    }

    fun applyDetailDefaults(detail: LostFoundDetail): LostFoundDetail {
        val formattedContact = buildContactHint(detail.contactQQ, detail.contactWechat, detail.contactPhone)
        return detail.copy(
            item = applyItemDefaults(detail.item),
            description = detail.description.ifBlank { textResolver.getString(R.string.lost_found_description_empty) },
            contactHint = formattedContact.ifBlank { textResolver.getString(R.string.lost_found_contact_private) },
            statusText = detail.statusText.ifBlank { resolveStatusText(detail.item.state) }
        )
    }

    fun applyPersonalSummaryDefaults(summary: LostFoundPersonalSummary): LostFoundPersonalSummary {
        return summary.copy(
            nickname = summary.nickname.ifBlank { textResolver.getString(R.string.profile_default_username) },
            introduction = summary.introduction.ifBlank { textResolver.getString(R.string.profile_subtitle_default) },
            lost = applyItemDefaults(summary.lost),
            found = applyItemDefaults(summary.found),
            didFound = applyItemDefaults(summary.didFound)
        )
    }

    fun buildContactHint(qq: String?, wechat: String?, phone: String?): String {
        return listOfNotNull(
            qq?.trim()?.takeIf { it.isNotBlank() }?.let { textResolver.getString(R.string.lost_found_contact_qq, it) },
            wechat?.trim()?.takeIf { it.isNotBlank() }?.let { textResolver.getString(R.string.lost_found_contact_wechat, it) },
            phone?.trim()?.takeIf { it.isNotBlank() }?.let { textResolver.getString(R.string.lost_found_contact_phone, it) }
        ).joinToString(" / ")
    }

    private fun resolveStatusText(state: LostFoundItemState): String {
        return when (state) {
            LostFoundItemState.ACTIVE -> textResolver.getString(R.string.lost_found_status_active)
            LostFoundItemState.RESOLVED -> textResolver.getString(R.string.lost_found_status_resolved)
            LostFoundItemState.SYSTEM_DELETED -> textResolver.getString(R.string.lost_found_status_deleted)
        }
    }
}
