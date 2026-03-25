package cn.gdeiassistant.model

import androidx.compose.runtime.Immutable
import java.io.Serializable
import java.util.Calendar

@Immutable
data class ProfileLocationCity(
    val code: String,
    val name: String
) : Serializable

@Immutable
data class ProfileLocationState(
    val code: String,
    val name: String,
    val cities: List<ProfileLocationCity>
) : Serializable

@Immutable
data class ProfileLocationRegion(
    val code: String,
    val name: String,
    val states: List<ProfileLocationState>
) : Serializable

@Immutable
data class ProfileLocationSelection(
    val displayName: String,
    val regionCode: String,
    val stateCode: String,
    val cityCode: String
) : Serializable

@Immutable
data class ProfileUpdateRequest(
    val nickname: String,
    val college: String,
    val major: String,
    val grade: String,
    val bio: String,
    val birthday: String,
    val location: ProfileLocationSelection?,
    val hometown: ProfileLocationSelection?
) : Serializable

@Immutable
data class ProfileDictionaryOption(
    val code: Int,
    val label: String
) : Serializable

@Immutable
data class ProfileMajorOption(
    val code: String,
    val label: String
) : Serializable

@Immutable
data class ProfileFacultyOption(
    val code: Int,
    val label: String,
    val majors: List<ProfileMajorOption>
) : Serializable

@Immutable
data class ProfileOptions(
    val faculties: List<ProfileFacultyOption>,
    val marketplaceItemTypes: List<ProfileDictionaryOption>,
    val lostFoundItemTypes: List<ProfileDictionaryOption>,
    val lostFoundModes: List<ProfileDictionaryOption>
) : Serializable {

    val facultyOptions: List<String>
        get() = faculties.map(ProfileFacultyOption::label)

    fun majorOptionsFor(faculty: String): List<String> {
        val normalizedFaculty = normalizeOptionLookup(faculty)
        return faculties.firstOrNull { normalizeOptionLookup(it.label) == normalizedFaculty }
            ?.majors
            ?.map(ProfileMajorOption::label)
            ?.takeIf(List<String>::isNotEmpty)
            ?: listOf(ProfileFormSupport.UnselectedOption)
    }

    fun canSelectMajor(faculty: String): Boolean {
        val normalized = ProfileFormSupport.normalizeSelection(faculty)
        return normalized.isNotEmpty() && normalized != ProfileFormSupport.UnselectedOption
    }

    fun facultyCodeFor(college: String): Int? {
        val normalizedCollege = normalizeOptionLookup(college)
        return faculties.firstOrNull { normalizeOptionLookup(it.label) == normalizedCollege }?.code
    }

    fun facultyNameFor(code: Int?): String? {
        val label = code?.let { value ->
            faculties.firstOrNull { it.code == value }?.label
        }
        val normalized = label?.trim().orEmpty()
        return normalized.takeIf { it.isNotEmpty() && it != ProfileFormSupport.UnselectedOption }
    }

    fun majorCodeFor(faculty: String, majorLabel: String): String? {
        val normalizedFaculty = normalizeOptionLookup(faculty)
        val normalizedMajor = normalizeOptionLookup(majorLabel)
        return faculties.firstOrNull { normalizeOptionLookup(it.label) == normalizedFaculty }
            ?.majors
            ?.firstOrNull { normalizeOptionLookup(it.label) == normalizedMajor }
            ?.code
    }

    fun majorLabelFor(faculty: String, majorCode: String): String? {
        val normalizedFaculty = normalizeOptionLookup(faculty)
        return faculties.firstOrNull { normalizeOptionLookup(it.label) == normalizedFaculty }
            ?.majors
            ?.firstOrNull { it.code == majorCode }
            ?.label
    }

    fun marketplaceTypeOptions(): List<MarketplaceTypeOption> {
        return marketplaceItemTypes.map { option ->
            MarketplaceTypeOption(id = option.code, title = option.label)
        }
    }

    fun lostFoundItemTypeOptions(): List<LostFoundItemTypeOption> {
        return lostFoundItemTypes.map { option ->
            LostFoundItemTypeOption(id = option.code, title = option.label)
        }
    }

    fun marketplaceTypeTitle(value: Int?): String {
        return dictionaryLabelFor(
            options = marketplaceItemTypes,
            code = value,
            fallback = LocalizedProfileCatalog.currentCatalog().otherLabel
        )
    }

    fun lostFoundItemTypeTitle(value: Int?): String {
        return dictionaryLabelFor(
            options = lostFoundItemTypes,
            code = value,
            fallback = LocalizedProfileCatalog.currentCatalog().otherLabel
        )
    }

    fun lostFoundModeTitle(value: Int?): String {
        return dictionaryLabelFor(
            options = lostFoundModes,
            code = value,
            fallback = ProfileFormSupport.UnselectedOption
        )
    }
}

object ProfileFormSupport {
    val UnselectedOption: String
        get() = LocalizedProfileCatalog.currentCatalog().unselectedLabel

    val defaultOptions: ProfileOptions
        get() = LocalizedProfileCatalog.currentCatalog().defaultOptions

    val enrollmentOptions: List<String>
        get() {
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            return (2014..currentYear).map(Int::toString)
        }

    fun makeLocationDisplay(region: String, state: String, city: String): String {
        return listOf(region, state, city)
            .map(String::trim)
            .filter(String::isNotEmpty)
            .fold(mutableListOf<String>()) { result, item ->
                if (result.lastOrNull() != item) {
                    result.add(item)
                }
                result
            }
            .joinToString(" ")
    }

    fun normalizeSelection(value: String): String {
        val trimmed = value.trim()
        return if (trimmed.isEmpty()) UnselectedOption else trimmed
    }

    fun normalizeOptionalSelection(value: String): String? {
        val trimmed = value.trim()
        return trimmed.takeIf { it.isNotEmpty() && it != UnselectedOption }
    }
}

private fun normalizeOptionLookup(value: String): String {
    return value
        .trim()
        .replace(" ", "")
        .replace("\u3000", "")
}

private fun dictionaryLabelFor(
    options: List<ProfileDictionaryOption>,
    code: Int?,
    fallback: String
): String {
    return options.firstOrNull { it.code == code }?.label
        ?: options.lastOrNull()?.label.orEmpty().ifBlank { fallback }
}
