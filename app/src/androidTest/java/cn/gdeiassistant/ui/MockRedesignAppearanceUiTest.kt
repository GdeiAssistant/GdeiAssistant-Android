package cn.gdeiassistant.ui

import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import cn.gdeiassistant.data.UserPreferencesRepository
import cn.gdeiassistant.ui.navigation.Routes
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** Render the redesigned primary screens with real Compose and demo data. */
@OptIn(ExperimentalTestApi::class)
class MockRedesignAppearanceUiTest : BaseMockUiSmokeTest(seedSession = true, initialRoute = Routes.HOME) {
    @Test
    fun primaryScreensRemainUsableInBothThemesAtMaximumAppFontSize() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val preferences = UserPreferencesRepository(context)
        val originalTheme = runBlocking { preferences.themeMode.first() }
        val originalFont = runBlocking { preferences.fontScaleStep.first() }
        val resolver = context.contentResolver
        val uri = checkNotNull(resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "redesign-appearance.zip")
            put(MediaStore.MediaColumns.MIME_TYPE, "application/zip")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/GdeiSocialUiEvidence")
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }))
        try {
            ZipOutputStream(checkNotNull(resolver.openOutputStream(uri))).use { output ->
                listOf(UserPreferencesRepository.THEME_LIGHT, UserPreferencesRepository.THEME_DARK).forEach { theme ->
                    runBlocking {
                        preferences.setThemeMode(theme)
                        preferences.setFontScaleStep(3)
                    }
                    listOf(Routes.HOME, Routes.DISCOVERY, Routes.MESSAGES, Routes.PROFILE).forEach { route ->
                        assertPrimaryTabsVisible()
                        composeRule.onNodeWithTag("tab.$route").assertIsDisplayed().performClick()
                        if (route == Routes.PROFILE) {
                            composeRule.waitUntilAtLeastOneExists(hasTestTag("profile.social.stats"), 20_000)
                            listOf("following", "followers", "friends").forEach { kind ->
                                composeRule.onNodeWithTag("profile.social.$kind").assertIsDisplayed()
                            }
                        }
                        composeRule.waitForIdle()
                        val bitmap = composeRule.onRoot().captureToImage().asAndroidBitmap()
                        output.putNextEntry(ZipEntry("redesign-$theme-large-$route.png"))
                        check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
                        output.closeEntry()
                    }
                }
            }
            resolver.update(uri, ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }, null, null)
        } finally {
            runBlocking {
                preferences.setThemeMode(originalTheme)
                preferences.setFontScaleStep(originalFont)
            }
        }
    }
}
