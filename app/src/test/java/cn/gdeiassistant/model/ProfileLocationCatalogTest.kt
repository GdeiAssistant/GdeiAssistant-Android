package cn.gdeiassistant.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ProfileLocationCatalogTest {
    @Test
    fun locationAndHometownUseTraditionalPlaceNamesForHongKongAndTaiwan() {
        listOf("zh-HK", "zh-TW").forEach { locale ->
            assertEquals(locale, "中國 廣東 廣州", ProfileLocationCatalog.displayName("CN", "44", "1", locale))
            assertEquals(locale, "中國 廣東 汕頭", ProfileLocationCatalog.displayName("CN", "44", "5", locale))
        }
    }

    @Test
    fun allSixLocalesKeepTheirExistingCountryAndSubdivisionConventions() {
        val expected = mapOf(
            "zh-CN" to "中国 广东 广州",
            "zh-HK" to "中國 廣東 廣州",
            "zh-TW" to "中國 廣東 廣州",
            "en" to "Guangzhou, Guangdong, China",
            "ja" to "広州, 広東, 中国",
            "ko" to "광저우, 광둥, 중국"
        )
        expected.forEach { (locale, display) ->
            val selection = checkNotNull(ProfileLocationCatalog.selection("CN", "44", "1", locale))
            assertEquals(locale, display, selection.displayName)
            assertEquals("CN", selection.regionCode)
            assertEquals("44", selection.stateCode)
            assertEquals("1", selection.cityCode)
        }
    }

    @Test
    fun changingTheAppliedLocaleDoesNotKeepPreviousPlaceLabels() {
        try {
            listOf("zh-HK", "en", "zh-TW", "zh-CN").forEach { locale ->
                AppLocaleSupport.setCurrentLocale(locale)
                assertEquals(
                    ProfileLocationCatalog.displayName("CN", "44", "1", locale),
                    ProfileLocationCatalog.displayName("CN", "44", "1")
                )
            }
        } finally {
            AppLocaleSupport.setCurrentLocale(null)
        }
    }

    @Test
    fun localizedPickerDirectoriesPreserveEveryCodeAndTheSourceCatalog() {
        val original = ProfileLocationCatalog.regions
        val codes = original.map { region -> region.code to region.states.map { state -> state.code to state.cities.map { it.code } } }
        listOf("zh-CN", "zh-HK", "zh-TW", "en", "ja", "ko").forEach { locale ->
            val localized = ProfileLocationCatalog.regionsForLocale(locale)
            assertEquals(locale, codes, localized.map { region -> region.code to region.states.map { state -> state.code to state.cities.map { it.code } } })
            if (locale == "zh-HK" || locale == "zh-TW") {
                assertNotEquals("The traditional catalog must not reuse every simplified label", original.map { it.name }, localized.map { it.name })
            }
        }
        assertEquals("中国", original.first { it.code == "CN" }.name)
        assertEquals(original, ProfileLocationCatalog.regions)
    }

    @Test
    fun cachedSelectionsAndPickerListsRelocalizeWithoutChangingTheirCodes() {
        val cachedSelection = checkNotNull(ProfileLocationCatalog.selection("CN", "44", "1", "zh-CN"))
        val cachedPicker = ProfileLocationCatalog.regionsForLocale("zh-CN").filter { it.code == "CN" }
        assertEquals("中國 廣東 廣州", ProfileLocationCatalog.selectionDisplayName(cachedSelection, cachedSelection.displayName, "zh-HK"))
        assertEquals("Guangzhou, Guangdong, China", ProfileLocationCatalog.selectionDisplayName(cachedSelection, cachedSelection.displayName, "en"))
        val picker = ProfileLocationCatalog.localizeRegions(cachedPicker, "zh-TW")
        assertEquals("中國", picker.single().name)
        assertEquals("廣東", picker.single().states.first { it.code == "44" }.name)
        assertEquals("廣州", picker.single().states.first { it.code == "44" }.cities.first { it.code == "1" }.name)
        assertEquals("中国 广东 广州", cachedSelection.displayName)
        assertEquals(cachedPicker.single().states.map { it.code }, picker.single().states.map { it.code })
    }

    @Test
    fun ipAreasResolveOnlyCompleteKnownNamesAndFollowTheCurrentLocale() {
        mapOf("zh-CN" to "广东", "zh-HK" to "廣東", "zh-TW" to "廣東", "en" to "Guangdong", "ja" to "広東", "ko" to "광둥").forEach { (locale, expected) ->
            listOf("广东", "廣東", "Guangdong", "広東", "광둥").forEach { source ->
                assertEquals("$locale / $source", expected, ProfileLocationCatalog.localizeIpArea(source, locale))
            }
        }
        assertEquals("Foshan", ProfileLocationCatalog.localizeIpArea("佛山", "en"))
        listOf("", "未知地区", "林广东喜欢广州", "广东 广州 / 自定义", "Guangdong user text").forEach { unknown ->
            assertEquals(unknown, ProfileLocationCatalog.localizeIpArea(unknown, "zh-HK"))
        }
        // The raw catalog has two regions named 圭亚那 with different English labels.
        assertEquals("圭亚那", ProfileLocationCatalog.localizeIpArea("圭亚那", "en"))
    }

    @Test
    fun ipPathsKeepOnlyTheLevelsPresentInTheRecognizedWholeValue() {
        assertEquals("廣東 廣州", ProfileLocationCatalog.localizeIpArea("广东 广州", "zh-HK"))
        assertEquals("廣東 廣州", ProfileLocationCatalog.localizeIpArea("Guangzhou, Guangdong", "zh-TW"))
        assertEquals("Guangzhou, Guangdong", ProfileLocationCatalog.localizeIpArea("廣東 廣州", "en"))
        assertEquals("広州, 広東", ProfileLocationCatalog.localizeIpArea("广东 广州", "ja"))
        assertEquals("광저우, 광둥", ProfileLocationCatalog.localizeIpArea("广东 广州", "ko"))
        assertEquals("中國 廣東 廣州", ProfileLocationCatalog.localizeIpArea("中国 广东 广州", "zh-HK"))
        assertEquals("中国 广东 广州", ProfileLocationCatalog.localizeIpArea("광저우, 광둥, 중국", "zh-CN"))
        assertEquals("中國 廣東", ProfileLocationCatalog.localizeIpArea("中国广东", "zh-HK"))
        assertEquals("中國 廣東 廣州", ProfileLocationCatalog.localizeIpArea("中国广东广州", "zh-TW"))
        assertEquals("Guangzhou, Guangdong, China", ProfileLocationCatalog.localizeIpArea("中國廣東廣州", "en"))
        assertEquals("廣東 廣州", ProfileLocationCatalog.localizeIpArea("Guangdong Guangzhou", "zh-HK"))
        assertEquals("廣東 廣州", ProfileLocationCatalog.localizeIpArea("廣東廣州", "zh-HK"))
        assertEquals("中国 广东 广州", ProfileLocationCatalog.localizeIpArea("중국 광둥 광저우", "zh-CN"))
        listOf("广东 广州 / 自定义", "广州 广东", "中国广东广州 / 自定义", "广东/广州", "Guangdong user text").forEach { unknown ->
            assertEquals(unknown, ProfileLocationCatalog.localizeIpArea(unknown, "zh-HK"))
        }
    }

    @Test
    fun unknownSavedCodesAndUnstructuredFallbacksStayIntact() {
        val unknown = ProfileLocationSelection("", "UNKNOWN", "state", "city")
        assertEquals("自定义地区", ProfileLocationCatalog.selectionDisplayName(unknown, "自定义地区", "zh-HK"))
        assertEquals("原始内容广州", ProfileLocationCatalog.selectionDisplayName(null, "原始内容广州", "zh-TW"))
        assertEquals("UNKNOWN", unknown.regionCode)
        listOf(ProfileLocationSelection("原始自定义地区", "CN", "unknown", ""),
            ProfileLocationSelection("原始自定义地区", "CN", "44", "unknown")).forEach { partial ->
            assertEquals("原始自定义地区", ProfileLocationCatalog.selectionDisplayName(partial, partial.displayName, "zh-HK"))
        }
    }

    @Test
    fun traditionalLabelsCoverEveryExistingCatalogNode() {
        val regions = ProfileLocationCatalog.regions
        val states = regions.flatMap { it.states }
        val cities = states.flatMap { it.cities }
        assertEquals(236, regions.size)
        assertEquals(266, states.size)
        assertEquals(3777, cities.size)
        listOf("zh-HK", "zh-TW").forEach { locale ->
            regions.forEach { assertEquals(true, it.localizedNames?.get(locale)?.isNotBlank()) }
            states.forEach { assertEquals(true, it.localizedNames?.get(locale)?.isNotBlank()) }
            cities.forEach { assertEquals(true, it.localizedNames?.get(locale)?.isNotBlank()) }
        }
    }
}
