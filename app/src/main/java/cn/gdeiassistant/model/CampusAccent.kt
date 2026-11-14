package cn.gdeiassistant.model

/**
 * Campus theme accent options persisted in user preferences and rendered by GdeiAssistantTheme.
 */
enum class CampusAccent(val storageKey: String) {
    BANYAN("banyan"),
    KAPOK("kapok"),
    HAIZHU("haizhu"),
    BAUHINIA("bauhinia");

    companion object {
        val DEFAULT: CampusAccent = BANYAN

        fun fromStorageKey(raw: String?): CampusAccent =
            entries.firstOrNull { it.storageKey == raw } ?: DEFAULT
    }
}
