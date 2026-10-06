package cn.gdeiassistant.ui

import android.content.res.Configuration
import android.view.KeyEvent
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.test.platform.app.InstrumentationRegistry
import cn.gdeiassistant.R
import cn.gdeiassistant.model.AppLocaleSupport
import cn.gdeiassistant.ui.navigation.Routes
import cn.gdeiassistant.ui.profile.supportedLanguageOptions
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

@OptIn(ExperimentalTestApi::class)
class MockProfileLocaleUiTest : BaseMockUiSmokeTest(seedSession = true, initialRoute = Routes.PROFILE) {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @Test
    fun relationCountsAreInTheIdentityHeaderAndPrivacyContainsDmAndBlocks() {
        composeRule.waitUntilAtLeastOneExists(hasTestTag("profile.social.stats"), 20_000)
        composeRule.onNodeWithTag("profile.social.stats").assert(hasAnyAncestor(hasTestTag("profile.account")))
        val identity = composeRule.onNodeWithTag("profile.identity").fetchSemanticsNode().boundsInRoot
        val stats = composeRule.onNodeWithTag("profile.social.stats").fetchSemanticsNode().boundsInRoot
        val details = composeRule.onNodeWithTag("profile.details").fetchSemanticsNode().boundsInRoot
        assertTrue("Relations must follow the identity and precede the profile form", identity.bottom <= stats.top && stats.bottom <= details.top)
        listOf("following", "followers", "friends").forEach { kind ->
            composeRule.onNodeWithTag("profile.social.$kind").assertIsDisplayed()
        }
        scrollToText(localized("zh-CN", R.string.social_profile_actions_title))
        composeRule.onNodeWithText(localized("zh-CN", R.string.social_blocks_title)).assertDoesNotExist()
        composeRule.onNodeWithText(localized("zh-CN", R.string.social_dm_privacy_title)).assertDoesNotExist()
        scrollToText(localized("zh-CN", R.string.profile_privacy_title)).performClick()
        scrollToTag("profile.privacy.dm").performClick()
        waitForText(localized("zh-CN", R.string.social_dm_privacy_title))
        composeRule.onNodeWithText(localized("zh-CN", R.string.social_dm_privacy_title)).assertIsDisplayed()
        composeRule.onNode(hasContentDescription(localized("zh-CN", R.string.back))).performClick()
        waitForText(localized("zh-CN", R.string.profile_privacy_title))
        scrollToTag("profile.privacy.blocks").performClick()
        waitForText(localized("zh-CN", R.string.social_blocks_title))
        composeRule.onNodeWithText(localized("zh-CN", R.string.social_blocks_title)).assertIsDisplayed()
    }

    @Test
    fun appearanceSwitchesAllSixLocalesThroughTheActualLanguageControls() {
        // Profile loading renders ShimmerScreen instead of the LazyColumn. Wait until the
        // seeded profile and initial navigation are present before scrolling or clearing extras.
        composeRule.waitUntilAtLeastOneExists(hasTestTag("profile.identity"), 20_000)
        // Locale/route extras seed this fixture once; let a real configuration recreation retain
        // the user's language and navigation state, rather than reapplying the test bootstrap.
        composeRule.runOnUiThread {
            composeRule.activity.intent.removeExtra(MainActivity.EXTRA_UI_LOCALE)
            composeRule.activity.intent.removeExtra(MainActivity.EXTRA_UI_INITIAL_ROUTE)
        }
        scrollToText(localized("zh-CN", R.string.appearance_title)).performClick()
        val switches = supportedLanguageOptions.drop(1) + supportedLanguageOptions.first()
        switches.forEach { option ->
            scrollToText(option.nativeName).performClick()
            composeRule.waitUntil(20_000) { AppLocaleSupport.currentLocale() == option.code }
            composeRule.waitUntilAtLeastOneExists(hasText(localized(option.code, R.string.appearance_title)), 20_000)
            composeRule.onNodeWithText(localized(option.code, R.string.appearance_title)).assertIsDisplayed()
            composeRule.onNode(hasContentDescription(localized(option.code, R.string.back))).performClick()
            waitForText(localized(option.code, R.string.profile_title))
            // Navigation restores the LazyColumn near the appearance menu. Its off-screen
            // identity item is not composed until we scroll back to it.
            scrollToTag("profile.identity").assertIsDisplayed()
            composeRule.waitUntilAtLeastOneExists(hasTestTag("profile.identity"), 20_000)
            val (location, hometown, ipArea) = when (option.code) {
                "zh-HK", "zh-TW" -> Triple("中國 廣東 廣州", "中國 廣東 汕頭", "廣東")
                "en" -> Triple("Guangzhou, Guangdong, China", "Shantou, Guangdong, China", "Guangdong")
                "ja" -> Triple("広州, 広東, 中国", "汕頭, 広東, 中国", "広東")
                "ko" -> Triple("광저우, 광둥, 중국", "산터우, 광둥, 중국", "광둥")
                else -> Triple("中国 广东 广州", "中国 广东 汕头", "广东")
            }
            scrollToText(localized(option.code, R.string.profile_ip_area_label, ipArea)).assertIsDisplayed()
            val (faculty, major) = when (option.code) {
                "zh-HK" -> "計算機科學系" to "軟件工程"
                "zh-TW" -> "計算機科學系" to "軟體工程"
                "en" -> "Department of Computer Science" to "Software Engineering"
                "ja" -> "計算機科学科" to "ソフトウェア工学"
                "ko" -> "컴퓨터과학과" to "소프트웨어공학"
                else -> "计算机科学系" to "软件工程"
            }
            scrollToText(faculty).assertIsDisplayed()
            scrollToText(major).assertIsDisplayed()
            scrollToText(hometown).assertIsDisplayed()
            scrollToText(location).assertIsDisplayed().performClick()
            composeRule.waitUntilAtLeastOneExists(hasTestTag("profile.location.picker"), 20_000)
            composeRule.onNode(hasText(location) and hasAnyAncestor(hasTestTag("profile.location.picker"))).assertIsDisplayed()
            val country = when (option.code) {
                "zh-HK", "zh-TW" -> "中國"
                "en" -> "China"
                "ko" -> "중국"
                else -> "中国"
            }
            composeRule.onNode(hasText(country) and hasAnyAncestor(hasTestTag("profile.location.picker"))).assertIsDisplayed()
            composeRule.onNode(hasText(ipArea) and hasAnyAncestor(hasTestTag("profile.location.picker"))).assertIsDisplayed()
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            composeRule.waitUntilDoesNotExist(hasTestTag("profile.location.picker"), 20_000)
            scrollToText(hometown).performClick()
            composeRule.waitUntilAtLeastOneExists(hasTestTag("profile.location.picker"), 20_000)
            composeRule.onNode(hasText(hometown) and hasAnyAncestor(hasTestTag("profile.location.picker"))).assertIsDisplayed()
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            composeRule.waitUntilDoesNotExist(hasTestTag("profile.location.picker"), 20_000)
            scrollToText(localized(option.code, R.string.appearance_title)).performClick()
        }
    }

    private fun scrollToText(text: String) = composeRule.onNodeWithText(text).also {
        composeRule.onNode(hasScrollToIndexAction()).performScrollToNode(hasText(text))
        it.performScrollTo()
    }

    private fun scrollToTag(tag: String) = composeRule.onNodeWithTag(tag).also {
        composeRule.onNode(hasScrollToIndexAction()).performScrollToNode(hasTestTag(tag))
        it.performScrollTo()
    }

    private fun localized(locale: String, resource: Int, vararg arguments: Any): String {
        val context = instrumentation.targetContext
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(Locale.forLanguageTag(locale))
        return context.createConfigurationContext(configuration).getString(resource, *arguments)
    }
}
