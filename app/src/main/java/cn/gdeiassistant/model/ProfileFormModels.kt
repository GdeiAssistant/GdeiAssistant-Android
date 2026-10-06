package cn.gdeiassistant.model

import androidx.compose.runtime.Immutable
import java.io.Serializable
import java.util.Calendar

@Immutable
data class ProfileLocationCity(
    val code: String,
    val name: String,
    val latinName: String? = null,
    val localizedNames: Map<String, String>? = null
) : Serializable

@Immutable
data class ProfileLocationState(
    val code: String,
    val name: String,
    val cities: List<ProfileLocationCity>,
    val latinName: String? = null,
    val localizedNames: Map<String, String>? = null
) : Serializable

@Immutable
data class ProfileLocationRegion(
    val code: String,
    val name: String,
    val states: List<ProfileLocationState>,
    val latinName: String? = null,
    val localizedNames: Map<String, String>? = null,
    val iso: String? = null
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

    fun localizedForLocale(locale: String = AppLocaleSupport.currentLocale()): ProfileOptions {
        val labels = LocalizedProfileCatalog.catalogForLocale(locale).defaultOptions
        fun localizeDictionary(source: List<ProfileDictionaryOption>, known: List<ProfileDictionaryOption>) =
            source.map { option -> option.copy(label = known.firstOrNull { it.code == option.code }?.label ?: option.label) }
        return copy(
            faculties = faculties.map { faculty ->
                val known = labels.faculties.firstOrNull { it.code == faculty.code }
                faculty.copy(
                    label = known?.label ?: faculty.label,
                    majors = faculty.majors.map { major -> major.copy(label = known?.majors?.firstOrNull { it.code == major.code }?.label ?: major.label) }
                )
            },
            marketplaceItemTypes = localizeDictionary(marketplaceItemTypes, labels.marketplaceItemTypes),
            lostFoundItemTypes = localizeDictionary(lostFoundItemTypes, labels.lostFoundItemTypes),
            lostFoundModes = localizeDictionary(lostFoundModes, labels.lostFoundModes)
        )
    }

    private fun facultyForLabel(value: String): ProfileFacultyOption? {
        return faculties.firstOrNull { normalizeOptionLookup(it.label) == normalizeOptionLookup(value) }
            ?: LocalizedProfileCatalog.facultyCodeForLabel(value)?.let { code -> faculties.firstOrNull { it.code == code } }
    }

    val facultyOptions: List<String>
        get() = localizedForLocale().faculties.map(ProfileFacultyOption::label)

    fun majorOptionsFor(faculty: String): List<String> {
        val code = facultyForLabel(faculty)?.code
        return localizedForLocale().faculties.firstOrNull { it.code == code }
            ?.majors
            ?.map(ProfileMajorOption::label)
            ?.takeIf(List<String>::isNotEmpty)
            ?: listOf(ProfileFormSupport.UnselectedOption)
    }

    fun canSelectMajor(faculty: String): Boolean {
        return facultyForLabel(faculty)?.code?.let { it != 0 } ?: false
    }

    fun facultyCodeFor(college: String): Int? {
        return facultyForLabel(college)?.code
    }

    fun facultyNameFor(code: Int?): String? {
        val label = code?.let { value ->
            localizedForLocale().faculties.firstOrNull { it.code == value }?.label
        }
        val normalized = label?.trim().orEmpty()
        return normalized.takeIf { it.isNotEmpty() && it != ProfileFormSupport.UnselectedOption }
    }

    fun majorCodeFor(faculty: String, majorLabel: String): String? {
        val option = facultyForLabel(faculty) ?: return null
        val normalizedMajor = normalizeOptionLookup(majorLabel)
        return option.majors.firstOrNull { normalizeOptionLookup(it.label) == normalizedMajor }?.code
            ?: LocalizedProfileCatalog.majorCodeForLabel(option.code, majorLabel)?.takeIf { code -> option.majors.any { it.code == code } }
    }

    fun majorLabelFor(faculty: String, majorCode: String): String? {
        val code = facultyForLabel(faculty)?.code
        return localizedForLocale().faculties.firstOrNull { it.code == code }
            ?.majors
            ?.firstOrNull { it.code == majorCode }
            ?.label
    }

    fun marketplaceTypeOptions(): List<MarketplaceTypeOption> {
        return localizedForLocale().marketplaceItemTypes.map { option ->
            MarketplaceTypeOption(id = option.code, title = option.label)
        }
    }

    fun lostFoundItemTypeOptions(): List<LostFoundItemTypeOption> {
        return localizedForLocale().lostFoundItemTypes.map { option ->
            LostFoundItemTypeOption(id = option.code, title = option.label)
        }
    }

    fun marketplaceTypeTitle(value: Int?): String {
        return dictionaryLabelFor(
            options = localizedForLocale().marketplaceItemTypes,
            code = value,
            fallback = LocalizedProfileCatalog.currentCatalog().otherLabel
        )
    }

    fun lostFoundItemTypeTitle(value: Int?): String {
        return dictionaryLabelFor(
            options = localizedForLocale().lostFoundItemTypes,
            code = value,
            fallback = LocalizedProfileCatalog.currentCatalog().otherLabel
        )
    }

    fun lostFoundModeTitle(value: Int?): String {
        return dictionaryLabelFor(
            options = localizedForLocale().lostFoundModes,
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

    fun makeLocationDisplay(
        region: String,
        state: String,
        city: String,
        locale: String = AppLocaleSupport.currentLocale()
    ): String {
        val normalizedLocale = AppLocaleSupport.normalizeLocale(locale)
        val parts = listOf(region, state, city)
            .map(String::trim)
            .filter(String::isNotEmpty)
            .fold(mutableListOf<String>()) { result, item ->
                if (result.lastOrNull() != item) {
                    result.add(item)
                }
                result
            }

        return if (normalizedLocale == "en" || normalizedLocale == "ja" || normalizedLocale == "ko") {
            parts.asReversed().joinToString(", ")
        } else {
            parts.joinToString(" ")
        }
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
