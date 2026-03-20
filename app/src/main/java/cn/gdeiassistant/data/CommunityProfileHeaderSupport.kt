package cn.gdeiassistant.data

import android.content.Context
import cn.gdeiassistant.R
import cn.gdeiassistant.model.UserProfileSummary

internal data class CommunityProfileHeader(
    val displayName: String,
    val avatarUrl: String?,
    val headline: String
)

internal fun Context.toCommunityProfileHeader(profile: UserProfileSummary?): CommunityProfileHeader {
    val displayName = profile?.nickname
        ?.trim()
        ?.takeIf(String::isNotBlank)
        ?: profile?.username
            ?.trim()
            ?.takeIf(String::isNotBlank)
        ?: getString(R.string.profile_default_username)

    val avatarUrl = profile?.avatar
        ?.trim()
        ?.takeIf(String::isNotBlank)

    val headline = profile?.introduction
        ?.trim()
        ?.takeIf(String::isNotBlank)
        ?: listOfNotNull(
            profile?.faculty?.trim()?.takeIf(String::isNotBlank),
            profile?.major?.trim()?.takeIf(String::isNotBlank),
            profile?.location?.trim()?.takeIf(String::isNotBlank)
        ).joinToString(" · ").takeIf(String::isNotBlank)
        ?: getString(R.string.profile_subtitle_default)

    return CommunityProfileHeader(
        displayName = displayName,
        avatarUrl = avatarUrl,
        headline = headline
    )
}
