package cn.gdeiassistant.ui

import android.content.Intent
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import cn.gdeiassistant.ui.navigation.Routes
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class MockUiSmokeTest {

    @get:Rule
    val composeRule = AndroidComposeTestRule(
        ActivityScenarioRule<MainActivity>(
            Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java).apply {
                putExtra(MainActivity.EXTRA_UI_USE_MOCK, true)
                putExtra(MainActivity.EXTRA_UI_LOCALE, "zh-CN")
                putExtra(MainActivity.EXTRA_UI_CLEAR_SESSION, true)
            }
        )
    ) { rule ->
        var activity: MainActivity? = null
        rule.scenario.onActivity { activity = it }
        checkNotNull(activity)
    }

    @Test
    fun mockLoginShowsHomeEntries() {
        loginAsMockUser()

        composeRule.onNodeWithTag("home.entry.${Routes.GRADE}").assertIsDisplayed()
        scrollHomeContent()
        composeRule.onNodeWithTag("home.entry.${Routes.MARKETPLACE}").assertIsDisplayed()
    }

    @Test
    fun mockMessagesTabShowsAnnouncementsAndInteractions() {
        loginAsMockUser()

        composeRule.onNodeWithContentDescription("资讯").performClick()
        composeRule.onNodeWithText("系统维护通知").assertIsDisplayed()
        composeRule.onNodeWithText("你收到一条新的评论").assertIsDisplayed()
    }

    @Test
    fun mockMarketplaceFlowShowsListAndDetail() {
        loginAsMockUser()

        scrollHomeContent()
        composeRule.onNodeWithTag("home.entry.${Routes.MARKETPLACE}").performClick()
        composeRule.onNodeWithText("95 新蓝牙耳机").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("日常通勤使用，续航稳定，附原装充电盒和备用耳帽。").assertIsDisplayed()
    }

    @Test
    fun mockGradeEntryLoadsAcademicContent() {
        loginAsMockUser()

        composeRule.onNodeWithTag("home.entry.${Routes.GRADE}").performClick()
        composeRule.onNodeWithText("第一学期").assertIsDisplayed()
        composeRule.onNodeWithText("高等数学").assertIsDisplayed()
    }

    private fun loginAsMockUser() {
        composeRule.onNodeWithTag("login.username").assertIsDisplayed().performTextInput("gdeiassistant")
        composeRule.onNodeWithTag("login.password").assertIsDisplayed().performTextInput("gdeiassistant")
        composeRule.onNodeWithTag("login.submit").performClick()
        composeRule.waitUntilAtLeastOneExists(hasTestTag("home.entry.${Routes.GRADE}"), 20_000)
    }

    private fun scrollHomeContent() {
        repeat(2) {
            composeRule.onRoot().performTouchInput { swipeUp() }
        }
    }
}
