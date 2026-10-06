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
    fun internationalCitiesUseStandardNamesAndKeepSavedPickerCodesAcrossLocales() {
        val examples = listOf(
            Triple(listOf("USA", "NY", "QEE"), "纽约市", listOf("New York City", "ニューヨーク", "뉴욕")),
            Triple(listOf("USA", "CA", "LAX"), "洛杉矶", listOf("Los Angeles", "ロサンゼルス", "로스앤젤레스")),
            Triple(listOf("GBR", "ENG", "LND"), "伦敦", listOf("London", "ロンドン", "런던")),
            Triple(listOf("FRA", "FRA", "PAR"), "巴黎", listOf("Paris", "パリ", "파리"))
        )
        examples.forEach { (codes, sourceName, expectedNames) ->
            val saved = checkNotNull(ProfileLocationCatalog.selection(codes[0], codes[1], codes[2], "zh-CN"))
            listOf("en", "ja", "ko").forEachIndexed { index, locale ->
                val selected = checkNotNull(ProfileLocationCatalog.selection(codes[0], codes[1], codes[2], locale))
                val city = ProfileLocationCatalog.regionsForLocale(locale)
                    .single { it.code == codes[0] }.states
                    .single { it.code == codes[1] }.cities
                    .single { it.code == codes[2] }
                assertEquals("$codes / $locale", expectedNames[index], city.name)
                assertEquals(expectedNames[index], ProfileLocationCatalog.localizeCityName(codes[0], codes[1], codes[2], sourceName, locale))
                assertEquals(codes, listOf(selected.regionCode, selected.stateCode, selected.cityCode))
                assertEquals(selected.displayName, ProfileLocationCatalog.selectionDisplayName(saved, saved.displayName, locale))
            }
            assertEquals(codes, listOf(saved.regionCode, saved.stateCode, saved.cityCode))
        }
    }

    @Test
    fun newYorkCityAndStateStaySeparateAndAmbiguousTextIsNotGuessed() {
        assertEquals("New York", ProfileLocationCatalog.localizeStateName("USA", "NY", "纽约", "en"))
        assertEquals("New York City", ProfileLocationCatalog.localizeCityName("USA", "NY", "QEE", "纽约市", "en"))
        assertEquals("ニューヨーク州", ProfileLocationCatalog.localizeStateName("USA", "NY", "纽约", "ja"))
        assertEquals("ニューヨーク", ProfileLocationCatalog.localizeCityName("USA", "NY", "QEE", "纽约市", "ja"))
        assertEquals("뉴욕주", ProfileLocationCatalog.localizeStateName("USA", "NY", "纽约", "ko"))
        assertEquals("뉴욕", ProfileLocationCatalog.localizeCityName("USA", "NY", "QEE", "纽约市", "ko"))
        mapOf("en" to "New York", "ja" to "ニューヨーク州", "ko" to "뉴욕주").forEach { (locale, stateName) ->
            assertEquals(stateName, ProfileLocationCatalog.localizeIpArea("New York", locale))
            assertEquals("Custom area near London", ProfileLocationCatalog.localizeIpArea("Custom area near London", locale))
        }
    }

    @Test
    fun frenchGuianaAndGuyanaKeepDistinctCountryCodesAndNamesInAllSixLocales() {
        val frenchGuiana = ProfileLocationCatalog.regions.single { it.code == "GUF" }
        val guyana = ProfileLocationCatalog.regions.single { it.code == "GUY" }
        assertEquals("GF", frenchGuiana.iso)
        assertEquals("GY", guyana.iso)
        assertEquals("圭亚那", frenchGuiana.name)
        assertEquals("圭亚那", guyana.name)
        mapOf(
            "zh-CN" to ("法属圭亚那" to "圭亚那"),
            "zh-HK" to ("法屬圭亞那" to "圭亞那"),
            "zh-TW" to ("法屬圭亞那" to "圭亞那"),
            "en" to ("French Guiana" to "Guyana"),
            "ja" to ("仏領ギアナ" to "ガイアナ"),
            "ko" to ("프랑스령 기아나" to "가이아나")
        ).forEach { (locale, names) ->
            val localized = ProfileLocationCatalog.regionsForLocale(locale)
            assertEquals(names.first, localized.single { it.code == "GUF" }.name)
            assertEquals(names.second, localized.single { it.code == "GUY" }.name)
            assertEquals(names.first, ProfileLocationCatalog.localizeIpArea("法属圭亚那", locale))
            assertEquals(names.first, ProfileLocationCatalog.localizeIpArea("French Guiana", locale))
            assertEquals("圭亚那", ProfileLocationCatalog.localizeIpArea("圭亚那", locale))
            val selected = checkNotNull(ProfileLocationCatalog.selection("GUF", "", "", locale))
            assertEquals("GUF", selected.regionCode)
            assertEquals(names.first, selected.displayName)
        }
    }

    @Test
    fun japanesePrefecturesRetainAll47CodesAndUseTheirJapaneseAdministrativeNames() {
        val expectedJapaneseNames = listOf(
            "北海道", "青森県", "岩手県", "宮城県", "秋田県", "山形県", "福島県", "茨城県", "栃木県", "群馬県",
            "埼玉県", "千葉県", "東京都", "神奈川県", "新潟県", "富山県", "石川県", "福井県", "山梨県", "長野県",
            "岐阜県", "静岡県", "愛知県", "三重県", "滋賀県", "京都府", "大阪府", "兵庫県", "奈良県", "和歌山県",
            "鳥取県", "島根県", "岡山県", "広島県", "山口県", "徳島県", "香川県", "愛媛県", "高知県", "福岡県",
            "佐賀県", "長崎県", "熊本県", "大分県", "宮崎県", "鹿児島県", "沖縄県"
        )
        val source = ProfileLocationCatalog.regions.single { it.code == "JPN" }.states.single().cities
        assertEquals((1..47).map(Int::toString).toSet(), source.map { it.code }.toSet())
        listOf("en", "ja", "ko").forEach { locale ->
            val localized = ProfileLocationCatalog.regionsForLocale(locale).single { it.code == "JPN" }.states.single().cities
            assertEquals(source.map { it.code }, localized.map { it.code })
            source.forEach { city ->
                assertEquals(true, city.localizedNames?.get(locale)?.isNotBlank())
                val selection = checkNotNull(ProfileLocationCatalog.selection("JPN", "JPN", city.code, locale))
                assertEquals(listOf("JPN", "JPN", city.code), listOf(selection.regionCode, selection.stateCode, selection.cityCode))
            }
            if (locale == "ja") {
                localized.forEach { city -> assertEquals(city.code, expectedJapaneseNames[city.code.toInt() - 1], city.name) }
            }
        }
        assertEquals("Tokyo", ProfileLocationCatalog.localizeCityName("JPN", "JPN", "13", "东京", "en"))
        assertEquals("도쿄 도", ProfileLocationCatalog.localizeCityName("JPN", "JPN", "13", "东京", "ko"))
        assertEquals("Tochigi", ProfileLocationCatalog.localizeCityName("JPN", "JPN", "9", "枥木", "en"))
        assertEquals("도치기 현", ProfileLocationCatalog.localizeCityName("JPN", "JPN", "9", "枥木", "ko"))
        listOf("zh-CN", "zh-HK", "zh-TW").forEach { locale ->
            assertEquals("栃木", ProfileLocationCatalog.localizeCityName("JPN", "JPN", "9", "枥木", locale))
        }
        assertEquals("枥木", source.single { it.code == "9" }.name)
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
        // The legacy raw name is shared by French Guiana and Guyana; do not guess a country.
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
