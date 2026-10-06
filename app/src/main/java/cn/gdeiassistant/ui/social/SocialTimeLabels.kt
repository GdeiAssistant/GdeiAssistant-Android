package cn.gdeiassistant.ui.social

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Only formats the display label; server timestamps remain unchanged. */
internal fun formatSocialTime(raw: String): String = runCatching {
    Instant.parse(raw).atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
}.getOrDefault(raw)
