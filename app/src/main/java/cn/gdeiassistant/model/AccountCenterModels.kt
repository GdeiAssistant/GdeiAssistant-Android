package cn.gdeiassistant.model

import androidx.compose.runtime.Immutable
import java.io.Serializable
import java.util.Locale

@Immutable
data class PrivacySettings(
    val facultyOpen: Boolean = false,
    val majorOpen: Boolean = false,
    val locationOpen: Boolean = false,
    val hometownOpen: Boolean = false,
    val introductionOpen: Boolean = true,
    val enrollmentOpen: Boolean = false,
    val ageOpen: Boolean = false,
    val cacheAllow: Boolean = false,
    val robotsIndexAllow: Boolean = false
) : Serializable

@Immutable
data class LoginRecordItem(
    val id: String,
    val timeText: String,
    val ip: String,
    val area: String,
    val device: String,
    val statusText: String
) : Serializable

@Immutable
data class PhoneAttribution(
    val id: Int,
    val code: Int,
    val flag: String,
    val name: String
) : Serializable {
    fun displayName(locale: Locale = AppLocaleSupport.localeObject()): String {
        val localizedName = regionCode?.let { Locale("", it).getDisplayCountry(locale) }.orEmpty()
        if (localizedName.isNotBlank()) {
            return localizedName
        }
        return name.ifBlank { "+$code" }
    }

    val displayText: String
        get() = listOf(flag.takeIf { it.isNotBlank() }, displayName())
            .filterNotNull()
            .joinToString(" ")
            .plus(" (+$code)")

    private val regionCode: String?
        get() {
            val codePoints = flag.codePoints().toArray()
            if (codePoints.size != 2) {
                return null
            }
            val builder = StringBuilder()
            codePoints.forEach { codePoint ->
                val asciiValue = codePoint - 127397
                if (asciiValue !in 'A'.code..'Z'.code) {
                    return null
                }
                builder.append(asciiValue.toChar())
            }
            return builder.toString()
        }
}

@Immutable
data class ContactBindingStatus(
    val isBound: Boolean = false,
    val rawValue: String? = null,
    val maskedValue: String = "",
    val note: String = "",
    val countryCode: Int? = null,
    val username: String? = null
) : Serializable

@Immutable
data class FeedbackSubmission(
    val content: String,
    val contact: String? = null,
    val type: String? = null
) : Serializable

fun maskPhone(phone: String?): String {
    val normalized = phone?.trim().orEmpty()
    if (normalized.isBlank()) return ""
    return when {
        normalized.length >= 11 -> {
            "${normalized.take(3)}****${normalized.takeLast(4)}"
        }
        normalized.length > 4 -> {
            "${normalized.take(2)}***${normalized.takeLast(2)}"
        }
        else -> normalized
    }
}

fun maskEmail(email: String?): String {
    val normalized = email?.trim().orEmpty()
    if (normalized.isBlank()) return ""
    val parts = normalized.split("@")
    if (parts.size != 2) return normalized
    val local = parts[0]
    val domain = parts[1]
    return "${local.take(3)}***@$domain"
}

fun displayDeviceName(rawValue: String?): String {
    val value = rawValue.orEmpty()
        .replace("客户端", "")
        .trim()
    val lowercased = value.lowercase()
    return when {
        lowercased.contains("web") -> "Web"
        lowercased.contains("ipad") -> "iPad"
        lowercased.contains("iphone") || lowercased.contains("ios") -> "iPhone"
        lowercased.contains("android") -> "Android"
        value.isBlank() -> ""
        else -> value
    }
}
