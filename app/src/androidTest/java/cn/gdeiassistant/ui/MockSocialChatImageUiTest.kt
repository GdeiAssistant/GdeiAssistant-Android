package cn.gdeiassistant.ui

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Environment
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import cn.gdeiassistant.data.SocialChatImageMetadata
import cn.gdeiassistant.model.DmPolicy
import cn.gdeiassistant.network.mock.MockSocialProvider
import cn.gdeiassistant.ui.navigation.Routes
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.Request
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.OutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** Uses the actual OS picker and Android image pipeline; only the remote service is an in-memory demo. */
@SdkSuppress(minSdkVersion = 33)
@OptIn(ExperimentalTestApi::class)
class MockSocialChatImageUiTest : BaseMockUiSmokeTest(
    seedSession = true,
    initialRoute = Routes.SOCIAL_CONVERSATIONS
) {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val automation get() = instrumentation.uiAutomation
    private val context get() = instrumentation.targetContext
    private val cacheDirectory get() = File(context.cacheDir, "social_chat_images")
    private val evidenceRelativePath = "${Environment.DIRECTORY_DOWNLOADS}/GdeiSocialUiEvidence"
    private var fixtureUri: Uri? = null
    private var originalAccessibilityFlags = 0
    private var conversationId = ""
    private var evidencePrefix = "social-image"
    private var dismissedLauncherAnr = false

    @Before
    fun prepareRealGalleryImageAndOpenConversation() {
        val info = automation.serviceInfo
        originalAccessibilityFlags = info.flags
        info.flags = info.flags or AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
            AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
            AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        automation.serviceInfo = info
        assertTrue("Previous test left private image files", cacheFiles().isEmpty())
        fixtureUri = insertSyntheticPhoto()
        conversationId = mockData("/api/social/conversations").getAsJsonArray("items")[0]
            .asJsonObject.get("id").asString
        composeRule.waitUntilAtLeastOneExists(hasTestTag("social.conversation.${MockSocialProvider.PEER_ALICE_ID}"), 20_000)
        composeRule.onNodeWithTag("social.conversation.${MockSocialProvider.PEER_ALICE_ID}")
            .assertIsDisplayed().performClick()
        composeRule.waitUntilAtLeastOneExists(hasTestTag("social.chat"), 20_000)
        composeRule.waitUntil(20_000) {
            runCatching { composeRule.onNodeWithTag("social.image.pick").assertIsEnabled() }.isSuccess
        }
    }

    @After
    fun saveDiagnosticsAndRemoveOnlyThisSyntheticPhoto() {
        saveEvidence("final")
        fixtureUri?.let { context.contentResolver.delete(it, null, null) }
        fixtureUri = null
        val info = automation.serviceInfo
        info.flags = originalAccessibilityFlags
        automation.serviceInfo = info
    }

    @Test
    fun systemPickerCancelAndPreviewRemovalDoNotSendOrLeavePrivateFiles() {
        evidencePrefix = "picker-cancel"
        launchSystemPicker()
        saveEvidence("system-picker")
        pressSystemBack()
        waitForAppWindow()
        composeRule.onNodeWithTag("social.image.preview").assertDoesNotExist()
        assertTrue(cacheFiles().isEmpty())

        selectPhotoThroughSystemPicker()
        assertRenderedFixture("social.image.preview")
        val canonical = singleCachedImage().readBytes()
        assertEquals(SocialChatImageMetadata.Header(320, 240, "image/jpeg"), SocialChatImageMetadata.inspect(canonical))
        saveEvidence("preview")
        composeRule.onNodeWithTag("social.image.cancel").performClick()
        composeRule.waitUntil(10_000) { cacheFiles().isEmpty() }
        composeRule.onNodeWithTag("social.image.preview").assertDoesNotExist()
        assertTrue(ownImageMessages().isEmpty())
        assertTrue(MockSocialProvider.imageSendAttemptsForTest().isEmpty())
    }

    @Test
    fun selectedImageUploadsActualBytesAndDisplaysAuthenticatedBubbleAndViewer() {
        evidencePrefix = "send-view"
        selectPhotoThroughSystemPicker()
        val canonical = singleCachedImage().readBytes()
        val expectedHash = SocialChatImageMetadata.sha256Hex(canonical)
        composeRule.onNodeWithTag("social.image.confirm").performClick()
        composeRule.waitUntil(20_000) { ownImageMessages().size == 1 && cacheFiles().isEmpty() }
        val message = ownImageMessages().single()
        assertEquals(expectedHash, MockSocialProvider.imageSendAttemptsForTest().single().sha256)
        assertArrayAndPixels(message, canonical)
        val id = message.get("id").asString
        assertRenderedFixture("social.image.$id")
        saveEvidence("sent")
        composeRule.onNodeWithTag("social.image.$id").performClick()
        composeRule.waitUntilAtLeastOneExists(hasTestTag("social.image.viewer"), 10_000)
        assertRenderedFixture("social.image.viewer.pixels")
        saveEvidence("viewer")
        composeRule.onNodeWithTag("social.image.viewer.close").performClick()
        composeRule.onNodeWithTag("social.image.viewer").assertDoesNotExist()
    }

    @Test
    fun failedImageRetainsOriginalIdAndBytesAcrossPrivacyRejectionAndRetry() {
        evidencePrefix = "failure-privacy-retry"
        selectPhotoThroughSystemPicker()
        val canonical = singleCachedImage().readBytes()
        val expectedHash = SocialChatImageMetadata.sha256Hex(canonical)
        MockSocialProvider.failNextImageSendForTest()
        composeRule.onNodeWithTag("social.image.confirm").performClick()
        composeRule.waitUntil(20_000) { MockSocialProvider.imageSendAttemptsForTest().size == 1 }
        val clientId = MockSocialProvider.imageSendAttemptsForTest().single().clientMessageId
        waitForRetry(clientId)
        assertTrue(ownImageMessages().isEmpty())
        org.junit.Assert.assertArrayEquals(canonical, singleCachedImage().readBytes())
        saveEvidence("failed")

        MockSocialProvider.setPeerDmPolicyForTest(MockSocialProvider.PEER_ALICE_ID, DmPolicy.NONE)
        refreshPermission(enabled = false)
        composeRule.onNodeWithTag("social.message.retry.$clientId").performScrollTo().performClick()
        composeRule.waitUntil(20_000) { MockSocialProvider.imageSendAttemptsForTest().size == 2 }
        waitForRetry(clientId)
        assertTrue(ownImageMessages().isEmpty())
        org.junit.Assert.assertArrayEquals(canonical, singleCachedImage().readBytes())

        MockSocialProvider.setPeerDmPolicyForTest(MockSocialProvider.PEER_ALICE_ID, DmPolicy.MUTUAL)
        refreshPermission(enabled = true)
        composeRule.onNodeWithTag("social.message.retry.$clientId").performScrollTo().performClick()
        composeRule.waitUntil(20_000) { ownImageMessages().size == 1 && cacheFiles().isEmpty() }
        assertEquals(3, MockSocialProvider.imageSendAttemptsForTest().size)
        assertTrue(MockSocialProvider.imageSendAttemptsForTest().all { it.clientMessageId == clientId && it.sha256 == expectedHash })
        val message = ownImageMessages().single()
        assertEquals(clientId, message.get("clientMessageId").asString)
        composeRule.onNodeWithTag("social.message.retry.$clientId").assertDoesNotExist()

        MockSocialProvider.setPeerDmPolicyForTest(MockSocialProvider.PEER_ALICE_ID, DmPolicy.NONE)
        refreshPermission(enabled = false)
        val id = message.get("id").asString
        assertRenderedFixture("social.image.$id")
        composeRule.onNodeWithTag("social.image.$id").performClick()
        assertRenderedFixture("social.image.viewer.pixels")
        saveEvidence("history-after-privacy")
    }

    @Test
    fun leavingChatAfterFailedUploadRemovesPrivateFileAndPendingImage() {
        evidencePrefix = "leave-cleanup"
        selectPhotoThroughSystemPicker()
        MockSocialProvider.failNextImageSendForTest()
        composeRule.onNodeWithTag("social.image.confirm").performClick()
        composeRule.waitUntil(20_000) { MockSocialProvider.imageSendAttemptsForTest().size == 1 }
        val clientId = MockSocialProvider.imageSendAttemptsForTest().single().clientMessageId
        waitForRetry(clientId)
        assertEquals(1, cacheFiles().size)
        pressSystemBack()
        composeRule.waitUntilAtLeastOneExists(hasTestTag("social.conversation.${MockSocialProvider.PEER_ALICE_ID}"), 10_000)
        composeRule.waitUntil(10_000) { cacheFiles().isEmpty() }
        composeRule.onNodeWithTag("social.conversation.${MockSocialProvider.PEER_ALICE_ID}").performClick()
        composeRule.waitUntilAtLeastOneExists(hasTestTag("social.chat"), 10_000)
        composeRule.onNodeWithTag("social.message.retry.$clientId").assertDoesNotExist()
        assertTrue(ownImageMessages().isEmpty())
        saveEvidence("returned-clean")
    }

    private fun refreshPermission(enabled: Boolean) {
        composeRule.onNodeWithTag("social.chat.refresh").assertIsEnabled().performClick()
        composeRule.waitUntil(20_000) {
            runCatching {
                composeRule.onNodeWithTag("social.chat.refresh").assertIsEnabled()
                if (enabled) composeRule.onNodeWithTag("social.image.pick").assertIsEnabled()
                else composeRule.onNodeWithTag("social.image.pick").assertIsNotEnabled()
            }.isSuccess
        }
    }

    private fun waitForRetry(clientId: String) {
        composeRule.waitUntilAtLeastOneExists(hasTestTag("social.message.retry.$clientId"), 20_000)
        composeRule.onNodeWithTag("social.message.status.$clientId").assertTextContains("发送失败", substring = true)
    }

    private fun cacheFiles(): List<File> = cacheDirectory.listFiles()?.filter(File::isFile).orEmpty()
    private fun singleCachedImage(): File = cacheFiles().single()

    private fun mockData(path: String): JsonObject {
        val response = checkNotNull(MockSocialProvider.route(Request.Builder().url("http://localhost$path")
            .header("Authorization", "Bearer mock_ui_token").get().build()))
        assertEquals(200, response.httpCode)
        return JsonParser.parseString(response.body).asJsonObject.getAsJsonObject("data")
    }

    private fun ownImageMessages(): List<JsonObject> = mockData("/api/social/conversations/$conversationId/messages")
        .getAsJsonArray("items").map { it.asJsonObject }.filter {
            it.get("type").asString == "IMAGE" && it.get("senderId").asString == MockSocialProvider.CURRENT_PUBLIC_ID
        }

    private fun assertArrayAndPixels(message: JsonObject, canonical: ByteArray) {
        val path = message.getAsJsonObject("image").get("url").asString
        val result = checkNotNull(MockSocialProvider.route(Request.Builder().url("http://localhost$path")
            .header("Authorization", "Bearer mock_ui_token").get().build()))
        assertEquals("private, no-store", result.headers["Cache-Control"])
        org.junit.Assert.assertArrayEquals(canonical, result.binaryBody)
        val bytes = checkNotNull(result.binaryBody)
        val bitmap = checkNotNull(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
        assertEquals(320, bitmap.width)
        assertEquals(240, bitmap.height)
        assertTrue(Color.red(bitmap.getPixel(40, 40)) > 180)
        assertTrue(Color.green(bitmap.getPixel(40, 40)) < 100)
        bitmap.recycle()
    }

    private fun insertSyntheticPhoto(): Uri {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "gdei-chat-ci-${UUID.randomUUID()}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/GdeiAssistantQa")
            put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = checkNotNull(context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values))
        val bitmap = Bitmap.createBitmap(320, 240, Bitmap.Config.ARGB_8888)
        for (y in 0 until 240) for (x in 0 until 320) {
            bitmap.setPixel(x, y, when {
                x < 160 && y < 120 -> Color.rgb(235, 40, 40)
                x >= 160 && y < 120 -> Color.rgb(30, 90, 235)
                x < 160 -> Color.rgb(40, 190, 70)
                else -> Color.rgb(240, 200, 35)
            })
        }
        try {
            checkNotNull(context.contentResolver.openOutputStream(uri)).use { check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            assertEquals(1, context.contentResolver.update(uri, values, null, null))
        } catch (error: Throwable) {
            context.contentResolver.delete(uri, null, null)
            throw error
        } finally {
            bitmap.recycle()
        }
        return uri
    }

    private fun launchSystemPicker() {
        composeRule.onNodeWithTag("social.image.pick").assertIsEnabled().performClick()
        waitNative("Photo picker did not open") { root -> isSystemPicker(root) }
    }

    private fun selectPhotoThroughSystemPicker() {
        launchSystemPicker()
        val photo = waitNativeNode("Synthetic gallery image was not exposed by system picker") { root ->
            if (!isSystemPicker(root)) null else walk(root).firstOrNull { node ->
                val id = node.viewIdResourceName.orEmpty().substringAfterLast('/')
                val description = node.contentDescription?.toString().orEmpty()
                node.isVisibleToUser && (id == "icon_thumbnail" || description == "Media" ||
                    description.startsWith("Photo taken") || description.startsWith("Photo,") || description.startsWith("照片"))
            }
        }
        assertTrue("System photo grid did not accept click", clickNodeOrParent(photo))
        // Modern picker modules may require a Done/Add confirmation even for a single selection.
        val deadline = SystemClock.uptimeMillis() + 20_000
        while (SystemClock.uptimeMillis() < deadline) {
            val roots = nativeRoots()
            val pickerRoots = roots.filter(::isSystemPicker)
            if (pickerRoots.isEmpty() && roots.any(::isAppWindow)) break
            for (root in pickerRoots) {
                val confirm = walk(root).firstOrNull { node ->
                    val label = node.text?.toString() ?: node.contentDescription?.toString().orEmpty()
                    node.isVisibleToUser && (label in setOf("Add", "Done", "Select", "添加", "完成", "选择") || label.startsWith("Add ("))
                }
                if (confirm != null) clickNodeOrParent(confirm)
            }
            SystemClock.sleep(200)
        }
        waitForAppWindow()
        composeRule.waitUntilAtLeastOneExists(hasTestTag("social.image.preview"), 20_000)
        composeRule.onNodeWithTag("social.image.preview").assertIsDisplayed()
    }

    private fun isSystemPicker(root: AccessibilityNodeInfo): Boolean {
        return walk(root).any { node ->
            val pkg = node.packageName?.toString().orEmpty()
            pkg.contains("providers.media") || pkg.contains("photopicker") || pkg.endsWith("documentsui")
        }
    }

    private fun isAppWindow(root: AccessibilityNodeInfo): Boolean =
        walk(root).any { it.packageName?.toString() == context.packageName }

    // The system picker is a separate translucent window. The previously touched app window
    // can remain the accessibility "active" window, so inspect every interactive window.
    private fun nativeRoots(): List<AccessibilityNodeInfo> = buildList {
        automation.windows.sortedByDescending { it.layer }.forEach { window ->
            window.root?.let { add(it) }
        }
        automation.rootInActiveWindow?.let { add(it) }
    }.distinctBy { it.windowId }

    private fun walk(root: AccessibilityNodeInfo): Sequence<AccessibilityNodeInfo> = sequence {
        yield(root)
        for (index in 0 until root.childCount) root.getChild(index)?.let { yieldAll(walk(it)) }
    }

    private fun clickNodeOrParent(node: AccessibilityNodeInfo): Boolean {
        var current: AccessibilityNodeInfo? = node
        repeat(5) {
            val item = current ?: return false
            if (item.isClickable && item.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true
            current = item.parent
        }
        return false
    }

    private fun waitNative(message: String, predicate: (AccessibilityNodeInfo) -> Boolean) {
        waitNativeNode(message) { root -> root.takeIf(predicate) }
    }

    private fun waitNativeNode(message: String, select: (AccessibilityNodeInfo) -> AccessibilityNodeInfo?): AccessibilityNodeInfo {
        val deadline = SystemClock.uptimeMillis() + 20_000
        while (SystemClock.uptimeMillis() < deadline) {
            val roots = nativeRoots()
            if (dismissKnownEmulatorLauncherAnr(roots)) continue
            for (root in roots) select(root)?.let { return it }
            SystemClock.sleep(200)
        }
        saveEvidence("native-timeout")
        throw AssertionError(message)
    }

    private fun dismissKnownEmulatorLauncherAnr(roots: List<AccessibilityNodeInfo>): Boolean {
        if (dismissedLauncherAnr || InstrumentationRegistry.getArguments().getString("gdeiEmulator") != "true") return false
        val dialog = roots.firstOrNull { root ->
            walk(root).any { node ->
                node.viewIdResourceName == "android:id/alertTitle" &&
                    node.text?.toString() == "Pixel Launcher isn't responding"
            }
        } ?: return false
        val close = walk(dialog).firstOrNull { it.viewIdResourceName == "android:id/aerr_close" }
            ?: return false
        // The isolated AVD's launcher is unrelated to this app or the real system picker.
        // Preserve the ANR evidence; app/picker ANRs and repeat launcher ANRs remain failures.
        saveEvidence("launcher-anr")
        assertTrue("System launcher ANR could not be dismissed", clickNodeOrParent(close))
        dismissedLauncherAnr = true
        Log.w("SocialImageUiTest", "Closed known Pixel Launcher ANR in isolated CI emulator")
        return true
    }

    private fun waitForAppWindow() {
        waitNative("Picker did not return to app") { root ->
            isAppWindow(root) && nativeRoots().none(::isSystemPicker)
        }
    }

    private fun pressSystemBack() {
        val downTime = SystemClock.uptimeMillis()
        assertTrue(automation.injectInputEvent(
            KeyEvent(downTime, downTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK, 0), true
        ))
        assertTrue(automation.injectInputEvent(
            KeyEvent(downTime, SystemClock.uptimeMillis(), KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK, 0), true
        ))
    }

    private fun assertRenderedFixture(tag: String) {
        composeRule.waitUntilAtLeastOneExists(hasTestTag(tag), 10_000)
        if (tag != "social.image.preview" && tag != "social.image.viewer.pixels") {
            composeRule.onNodeWithTag(tag).performScrollTo()
        }
        composeRule.onNodeWithTag(tag).assertIsDisplayed()
        composeRule.waitUntil(15_000) {
            // Compose captures the node's actual Android window, including centered dialogs.
            val bitmap = composeRule.onNodeWithTag(tag).captureToImage().asAndroidBitmap()
            try {
                var redPixels = 0
                for (y in 0 until bitmap.height step 4) for (x in 0 until bitmap.width step 4) {
                    val color = bitmap.getPixel(x, y)
                    if (Color.red(color) > 180 && Color.green(color) < 100 && Color.blue(color) < 100) redPixels++
                }
                redPixels > 30
            } finally { bitmap.recycle() }
        }
    }

    private fun saveEvidence(stage: String) {
        // MediaStore Downloads survive UTP's app cleanup, unlike app-owned external files.
        // These files contain only synthetic mock test data.
        runCatching {
            val screenshot = automation.takeScreenshot()
            if (screenshot == null) Log.w("SocialImageUiTest", "Screenshot unavailable at $stage")
            screenshot?.let { captured ->
                try {
                    // A PNG in Downloads would itself appear as the newest picker photo.
                    persistEvidence("$evidencePrefix-$stage.zip", "application/zip") { output ->
                        ZipOutputStream(output).use { zip ->
                            zip.putNextEntry(ZipEntry("$evidencePrefix-$stage.png"))
                            check(captured.compress(Bitmap.CompressFormat.PNG, 100, zip))
                            zip.closeEntry()
                        }
                    }
                } finally {
                    captured.recycle()
                }
            }
        }.onFailure { Log.e("SocialImageUiTest", "Screenshot evidence failed at $stage", it) }
        runCatching {
            val roots = nativeRoots()
            Log.i("SocialImageUiTest", "stage=$stage target=${context.packageName} flags=${automation.serviceInfo.flags} windows=${roots.map { it.packageName }}")
            val tree = roots.joinToString("\n\n") { root ->
                "Window ${root.windowId}\n" + walk(root).joinToString("\n") { node ->
                    "${node.packageName} | ${node.viewIdResourceName} | ${node.text} | ${node.contentDescription} | clickable=${node.isClickable}"
                }
            }.ifEmpty { "No interactive accessibility windows" }
            persistEvidence("$evidencePrefix-$stage.txt", "text/plain") { it.write(tree.toByteArray(Charsets.UTF_8)) }
        }.onFailure { Log.e("SocialImageUiTest", "Window evidence failed at $stage", it) }
    }

    private fun persistEvidence(name: String, mimeType: String, write: (OutputStream) -> Unit) {
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, name)
            put(MediaStore.Downloads.MIME_TYPE, mimeType)
            put(MediaStore.Downloads.RELATIVE_PATH, evidenceRelativePath)
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val uri = checkNotNull(context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values))
        try {
            checkNotNull(context.contentResolver.openOutputStream(uri)).use(write)
            values.clear()
            values.put(MediaStore.Downloads.IS_PENDING, 0)
            check(context.contentResolver.update(uri, values, null, null) == 1)
        } catch (error: Throwable) {
            context.contentResolver.delete(uri, null, null)
            throw error
        }
    }
}
