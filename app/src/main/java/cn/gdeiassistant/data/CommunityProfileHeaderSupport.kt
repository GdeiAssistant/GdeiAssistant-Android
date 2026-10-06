package cn.gdeiassistant.data

import cn.gdeiassistant.model.UserProfileSummary

internal data class CommunityProfileHeader(
    val displayName: String,
    val avatarUrl: String?,
    val headline: String
)

internal fun buildCommunityProfileHeader(
    profile: UserProfileSummary?,
    defaultDisplayName: String,
    defaultHeadline: String
): CommunityProfileHeader {
    val displayName = profile?.nickname
        ?.trim()
        ?.takeIf(String::isNotBlank)
        ?: profile?.username
            ?.trim()
            ?.takeIf(String::isNotBlank)
        ?: defaultDisplayName

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
        ?: defaultHeadline

    return CommunityProfileHeader(
        displayName = displayName,
        avatarUrl = avatarUrl,
        headline = headline
    )
}
