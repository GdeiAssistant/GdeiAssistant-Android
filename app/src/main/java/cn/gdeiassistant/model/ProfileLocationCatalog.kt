package cn.gdeiassistant.model

import cn.gdeiassistant.data.ProfileLocationMockCatalog
import java.lang.reflect.Method
import java.util.Locale

object ProfileLocationCatalog {

    val regions: List<ProfileLocationRegion>
        get() = ProfileLocationMockCatalog.regions

    fun regionsForLocale(locale: String = AppLocaleSupport.currentLocale()): List<ProfileLocationRegion> {
        return localizeRegions(regions, locale)
    }

    fun localizeRegions(
        source: List<ProfileLocationRegion>,
        locale: String = AppLocaleSupport.currentLocale()
    ): List<ProfileLocationRegion> {
        return source.map { region ->
            region.copy(
                name = localizeRegionName(region.code, region.name, locale),
                states = region.states.map { state ->
                    state.copy(
                        name = localizeStateName(region.code, state.code, state.name, locale),
                        cities = state.cities.map { city ->
                            city.copy(
                                name = localizeCityName(region.code, state.code, city.code, city.name, locale)
                            )
                        }
                    )
                }
            )
        }
    }

    fun selection(
        regionCode: String,
        stateCode: String,
        cityCode: String,
        locale: String = AppLocaleSupport.currentLocale()
    ): ProfileLocationSelection? {
        val region = regions.firstOrNull { it.code == regionCode } ?: return null
        val state = region.states.firstOrNull { it.code == stateCode }
        val city = state?.cities?.firstOrNull { it.code == cityCode }
        return ProfileLocationSelection(
            displayName = ProfileFormSupport.makeLocationDisplay(
                region = localizeRegionName(region.code, region.name, locale),
                state = state?.let { localizeStateName(region.code, it.code, it.name, locale) }.orEmpty(),
                city = city?.let { localizeCityName(region.code, state.code, it.code, it.name, locale) }.orEmpty(),
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

    fun selectionDisplayName(
        selection: ProfileLocationSelection?,
        fallback: String?,
        locale: String = AppLocaleSupport.currentLocale()
    ): String {
        if (selection == null) return fallback.orEmpty()
        val resolved = ProfileLocationCatalog.selection(selection.regionCode, selection.stateCode, selection.cityCode, locale)
        return resolved?.takeIf {
            it.regionCode == selection.regionCode && it.stateCode == selection.stateCode && it.cityCode == selection.cityCode
        }?.displayName?.ifBlank { fallback.orEmpty() } ?: fallback.orEmpty()
    }

    // Only system IP-area fields use this exact catalog lookup. Unknown or ambiguous
    // values stay intact; this must never be applied to names, biographies or posts.
    fun localizeIpArea(value: String, locale: String = AppLocaleSupport.currentLocale()): String {
        fun resolve(name: String): String? {
            val matches = ipAreaPaths[name.lowercase(Locale.ROOT)] ?: return null
            return matches.map { path -> areaPathDisplay(path, locale) }.distinct().singleOrNull()
        }
        return resolve(value.trim()) ?: value
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
        localizedNames?.get(normalizedLocale)
            ?.trim()
            ?.takeIf(String::isNotEmpty)
            ?.let { return it }

        if (normalizedLocale.startsWith("zh")) {
            return name
        }

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
        return runCatching {
            Locale.Builder()
                .setRegion(code)
                .build()
                .getDisplayCountry(AppLocaleSupport.localeObject(locale))
        }
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

    private data class AreaName(
        val name: String,
        val code: String,
        val latinName: String?,
        val localizedNames: Map<String, String>?,
        val country: Boolean = false
    )

    private fun areaPathDisplay(path: List<AreaName>, locale: String): String {
        val names = path.map { item -> localizeName(item.name, item.code, locale, item.latinName, item.localizedNames, item.country) }
        return ProfileFormSupport.makeLocationDisplay(names[0], names.getOrElse(1) { "" }, names.getOrElse(2) { "" }, locale)
    }

    private val ipAreaPaths: Map<String, List<List<AreaName>>> by lazy {
        val paths = buildList {
            regions.forEach { region ->
                val country = AreaName(region.name, region.iso ?: region.code, region.latinName, region.localizedNames, country = true)
                add(listOf(country))
                region.states.forEach { state ->
                    val province = AreaName(state.name, state.code, state.latinName, state.localizedNames)
                    add(listOf(province))
                    add(listOf(country, province))
                    state.cities.forEach { city ->
                        val locality = AreaName(city.name, city.code, city.latinName, city.localizedNames)
                        add(listOf(locality))
                        add(listOf(province, locality))
                        add(listOf(country, province, locality))
                    }
                }
            }
        }
        val locales = regions.flatMap { it.localizedNames.orEmpty().keys }.toSet() + AppLocaleSupport.fallbackLocale
        val nodes = paths.flatten().distinct()
        val labelsByLocale = locales.associateWith { locale ->
            nodes.associateWith { item -> localizeName(item.name, item.code, locale, item.latinName, item.localizedNames, item.country) }
        }
        paths.flatMap { path ->
            val aliases = buildList {
                add(path.joinToString(" ") { it.name })
                add(path.joinToString("") { it.name })
                add(path.joinToString(" ") { it.latinName ?: it.name })
                locales.forEach { locale ->
                    val names = path.map { labelsByLocale.getValue(locale).getValue(it) }
                    add(ProfileFormSupport.makeLocationDisplay(names[0], names.getOrElse(1) { "" }, names.getOrElse(2) { "" }, locale))
                    add(names.joinToString(" "))
                    if (locale.startsWith("zh")) add(names.joinToString(""))
                }
            }
            aliases.distinct().map { it.lowercase(Locale.ROOT) to path }
        }.groupBy({ it.first }, { it.second })
    }

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
