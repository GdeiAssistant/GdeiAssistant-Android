package cn.gdeiassistant.ui

import android.content.Intent
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import cn.gdeiassistant.ui.navigation.Routes
import org.junit.Rule
import org.junit.Test

private const val MOCK_USERNAME = "gdeiassistant"
private const val MOCK_PASSWORD = "gdeiassistant"
private const val MOCK_TOKEN = "mock_ui_token"

private fun createMockComposeRule(
    seedSession: Boolean = false,
    initialRoute: String? = null
): AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity> {
    return AndroidComposeTestRule(
        ActivityScenarioRule<MainActivity>(
            Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java).apply {
                putExtra(MainActivity.EXTRA_UI_USE_MOCK, true)
                putExtra(MainActivity.EXTRA_UI_LOCALE, "zh-CN")
                putExtra(MainActivity.EXTRA_UI_CLEAR_SESSION, true)
                if (seedSession) {
                    putExtra(MainActivity.EXTRA_UI_SEED_TOKEN, MOCK_TOKEN)
                    putExtra(MainActivity.EXTRA_UI_SEED_USERNAME, MOCK_USERNAME)
                }
                if (!initialRoute.isNullOrBlank()) {
                    putExtra(MainActivity.EXTRA_UI_INITIAL_ROUTE, initialRoute)
                }
            }
        )
    ) { rule ->
        var activity: MainActivity? = null
        rule.scenario.onActivity { activity = it }
        checkNotNull(activity)
    }
}

@OptIn(ExperimentalTestApi::class)
abstract class BaseMockUiSmokeTest(
    seedSession: Boolean = false,
    initialRoute: String? = null
) {

    @get:Rule
    val composeRule = createMockComposeRule(
        seedSession = seedSession,
        initialRoute = initialRoute
    )

    protected fun waitForText(text: String, timeoutMillis: Long = 20_000) {
        composeRule.waitUntilAtLeastOneExists(hasText(text), timeoutMillis)
    }

    protected fun assertPrimaryTabsVisible() {
        composeRule.waitUntilAtLeastOneExists(hasTestTag("tab.${Routes.HOME}"), 20_000)
        composeRule.onNodeWithTag("tab.${Routes.HOME}").assertIsDisplayed()
        composeRule.onNodeWithTag("tab.${Routes.MESSAGES}").assertIsDisplayed()
        composeRule.onNodeWithTag("tab.${Routes.PROFILE}").assertIsDisplayed()
    }
}

class MockLoginUiSmokeTest : BaseMockUiSmokeTest() {

    @Test
    fun mockLoginShowsPrimaryTabs() {
        composeRule.onNodeWithTag("login.username").assertIsDisplayed().performTextReplacement(MOCK_USERNAME)
        composeRule.onNodeWithTag("login.password").assertIsDisplayed().performTextReplacement(MOCK_PASSWORD)
        composeRule.onNodeWithTag("login.submit").assertIsEnabled().performClick()

        assertPrimaryTabsVisible()
    }
}

class MockMessagesUiSmokeTest : BaseMockUiSmokeTest(
    seedSession = true,
    initialRoute = Routes.MESSAGES
) {

    @Test
    fun mockMessagesRouteShowsAnnouncementsAndInteractions() {
        waitForText("Message Hub")
        composeRule.onNodeWithText("Message Hub").assertIsDisplayed()
        composeRule.onNodeWithText("News, system notices and interaction messages — all in one place.").assertIsDisplayed()
    }
}

class MockMarketplaceUiSmokeTest : BaseMockUiSmokeTest(
    seedSession = true,
    initialRoute = Routes.MARKETPLACE
) {

    @Test
    fun mockMarketplaceRouteShowsListAndDetail() {
        waitForText("Marketplace")
        composeRule.onNodeWithText("Marketplace").assertIsDisplayed()
        composeRule.onNodeWithText("95 新蓝牙耳机").assertIsDisplayed()
    }
}

class MockGradeUiSmokeTest : BaseMockUiSmokeTest(
    seedSession = true,
    initialRoute = Routes.GRADE
) {

    @Test
    fun mockGradeRouteShowsAcademicContent() {
        waitForText("Grades")
        composeRule.onNodeWithText("Grades").assertIsDisplayed()
        composeRule.onNodeWithText("Academic Year").assertIsDisplayed()
        composeRule.onNodeWithText("5 courses recorded for this academic year").assertIsDisplayed()
    }
}
