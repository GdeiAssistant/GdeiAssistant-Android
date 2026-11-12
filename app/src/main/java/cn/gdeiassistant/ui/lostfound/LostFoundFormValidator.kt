package cn.gdeiassistant.ui.lostfound

import cn.gdeiassistant.R

/**
 * The publish and edit screens accept the same form. Keeping the rules here means a new
 * field or limit is changed once instead of in two view models that can silently drift.
 */
data class LostFoundForm(
    val title: String,
    val description: String,
    val location: String,
    val qq: String,
    val wechat: String,
    val phone: String
)

object LostFoundFormValidator {

    fun normalize(
        title: String,
        description: String,
        location: String,
        qq: String,
        wechat: String,
        phone: String
    ): LostFoundForm = LostFoundForm(
        title = title.trim(),
        description = description.trim(),
        location = location.trim(),
        qq = qq.trim(),
        wechat = wechat.trim(),
        phone = phone.trim()
    )

    /** String resource of the first failed rule, or null when the form is valid. */
    fun errorRes(form: LostFoundForm): Int? = when {
        form.title.isBlank() -> R.string.lost_found_publish_title_required
        form.title.length > 25 -> R.string.lost_found_publish_title_too_long
        form.description.isBlank() -> R.string.lost_found_publish_description_required
        form.description.length > 100 -> R.string.lost_found_publish_description_too_long
        form.location.isBlank() -> R.string.lost_found_publish_location_required
        form.location.length > 30 -> R.string.lost_found_publish_location_too_long
        form.qq.isBlank() && form.wechat.isBlank() && form.phone.isBlank() ->
            R.string.lost_found_publish_contact_required
        form.qq.length > 20 -> R.string.lost_found_publish_qq_too_long
        form.wechat.length > 20 -> R.string.lost_found_publish_wechat_too_long
        form.phone.length > 11 -> R.string.lost_found_publish_phone_too_long
        else -> null
    }
}
