package cn.gdeiassistant.model

import cn.gdeiassistant.data.ProfileLocationMockCatalog
import java.lang.reflect.Method
import java.util.Locale

object ProfileLocationCatalog {

    val regions: List<ProfileLocationRegion>
        get() = ProfileLocationMockCatalog.regions

    fun regionsForLocale(locale: String = AppLocaleSupport.currentLocale()): List<ProfileLocationRegion> {
        return regions.map { region ->
            ProfileLocationRegion(
                code = region.code,
                name = localizeRegionName(region.code, region.name, locale),
                states = region.states.map { state ->
                    ProfileLocationState(
                        code = state.code,
                        name = localizeStateName(region.code, state.code, state.name, locale),
                        cities = state.cities.map { city ->
                            ProfileLocationCity(
                                code = city.code,
                                name = localizeCityName(region.code, state.code, city.code, city.name, locale),
                                latinName = city.latinName,
                                localizedNames = city.localizedNames
                            )
                        },
                        latinName = state.latinName,
                        localizedNames = state.localizedNames
                    )
                },
                latinName = region.latinName,
                localizedNames = region.localizedNames,
                iso = region.iso
            )
        }
    }

    fun selection(
        regionCode: String,
        stateCode: String,
        cityCode: String,
        locale: String = AppLocaleSupport.currentLocale()
    ): ProfileLocationSelection? {
        val region = regionsForLocale(locale).firstOrNull { it.code == regionCode } ?: return null
        val state = region.states.firstOrNull { it.code == stateCode }
        val city = state?.cities?.firstOrNull { it.code == cityCode }
        return ProfileLocationSelection(
            displayName = ProfileFormSupport.makeLocationDisplay(
                region = region.name,
                state = state?.name.orEmpty(),
                city = city?.name.orEmpty(),
                locale = locale
            ),
            regionCode = region.code,
            stateCode = state?.code.orEmpty(),
            cityCode = city?.code.orEmpty()
        )
    }

    fun displayName(
        regionCode: String,
        stateCode: String,
        cityCode: String,
        locale: String = AppLocaleSupport.currentLocale()
    ): String {
        return selection(regionCode, stateCode, cityCode, locale)?.displayName.orEmpty()
    }

    fun localizeRegionName(regionCode: String, fallbackName: String, locale: String = AppLocaleSupport.currentLocale()): String {
        val region = regions.firstOrNull { it.code == regionCode }
        return localizeName(
            name = region?.name ?: fallbackName,
            code = region?.iso ?: regionCode,
            locale = locale,
            latinName = region?.latinName,
            localizedNames = region?.localizedNames,
            preferCountryLookup = true
        )
    }

    fun localizeStateName(
        regionCode: String,
        stateCode: String,
        fallbackName: String,
        locale: String = AppLocaleSupport.currentLocale()
    ): String {
        val localizedState = regions.firstOrNull { it.code == regionCode }
            ?.states
            ?.firstOrNull { it.code == stateCode }
        return localizeName(
            name = localizedState?.name ?: fallbackName,
            code = stateCode,
            locale = locale,
            latinName = localizedState?.latinName,
            localizedNames = localizedState?.localizedNames
        )
    }

    fun localizeCityName(
        regionCode: String,
        stateCode: String,
        cityCode: String,
        fallbackName: String,
        locale: String = AppLocaleSupport.currentLocale()
    ): String {
        val localizedCity = regions.firstOrNull { it.code == regionCode }
            ?.states
            ?.firstOrNull { it.code == stateCode }
            ?.cities
            ?.firstOrNull { it.code == cityCode }
        return localizeName(
            name = localizedCity?.name ?: fallbackName,
            code = cityCode,
            locale = locale,
            latinName = localizedCity?.latinName,
            localizedNames = localizedCity?.localizedNames
        )
    }

    private fun localizeName(
        name: String,
        code: String,
        locale: String,
        latinName: String? = null,
        localizedNames: Map<String, String>? = emptyMap(),
        preferCountryLookup: Boolean = false
    ): String {
        val normalizedLocale = AppLocaleSupport.normalizeLocale(locale)
        if (normalizedLocale.startsWith("zh")) {
            return name
        }

        localizedNames?.get(normalizedLocale)
            ?.trim()
            ?.takeIf(String::isNotEmpty)
            ?.let { return it }

        if (preferCountryLookup) {
            val countryName = localeDisplayCountry(code, normalizedLocale)
            if (countryName.isNotBlank()) {
                return countryName
            }
        }

        latinName
            ?.trim()
            ?.takeIf(String::isNotEmpty)
            ?.let { return it }

        val transliterated = transliterateToLatin(name)
        return when {
            transliterated.isNotBlank() && transliterated != name -> transliterated
            name.all { it.code < 128 } -> name
            else -> code
        }
    }

    private fun localeDisplayCountry(code: String, locale: String): String {
        return runCatching { Locale("", code).getDisplayCountry(AppLocaleSupport.localeObject(locale)) }
            .getOrDefault("")
            .trim()
    }

    private fun transliterateToLatin(value: String): String {
        val handle = hanLatinTransliterator ?: return value
        val converted = runCatching {
            handle.method.invoke(handle.instance, value) as? String
        }.getOrNull().orEmpty()
        if (converted.isBlank()) {
            return value
        }
        return converted
            .replace(Regex("\\s+"), " ")
            .trim()
            .split(' ')
            .filter(String::isNotBlank)
            .joinToString(" ") { segment ->
                segment.lowercase(Locale.ROOT).replaceFirstChar { character ->
                    character.titlecase(Locale.ROOT)
                }
            }
    }

    private data class TransliteratorHandle(
        val instance: Any,
        val method: Method
    )

    private val hanLatinTransliterator: TransliteratorHandle? by lazy {
        runCatching {
            val clazz = Class.forName("android.icu.text.Transliterator")
            val instance = clazz.getMethod("getInstance", String::class.java)
                .invoke(null, "Han-Latin; Latin-ASCII")
                ?: return@runCatching null
            TransliteratorHandle(instance = instance, method = clazz.getMethod("transliterate", String::class.java))
        }.getOrNull()
    }
}
