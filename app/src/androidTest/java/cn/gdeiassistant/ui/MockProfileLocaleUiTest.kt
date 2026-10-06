package cn.gdeiassistant.ui

import android.content.res.Configuration
import android.view.KeyEvent
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
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
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
        scrollToTag("profile.privacy.blocks").performClick()
        waitForText(localized("zh-CN", R.string.social_blocks_title))
        composeRule.onNodeWithText(localized("zh-CN", R.string.social_blocks_title)).assertIsDisplayed()
    }

    @Test
    fun appearanceSwitchesAllSixLocalesThroughTheActualLanguageControls() {
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

    private fun localized(locale: String, resource: Int): String {
        val context = instrumentation.targetContext
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(Locale.forLanguageTag(locale))
        return context.createConfigurationContext(configuration).getString(resource)
    }
}
