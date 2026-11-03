package cn.gdeiassistant.ui

import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.platform.app.InstrumentationRegistry

/**
 * Closes the two external system ANR dialogs seen while the isolated CI AVD is still settling.
 *
 * A modal "Pixel Launcher isn't responding" dialog owns native window focus, so a Compose dialog in
 * the app under test never reports `hasWindowFocus()`. Only the exact titles below are handled;
 * app, provider and other system ANRs are left in place so real failures still surface.
 */
internal object KnownEmulatorSystemAnr {
    private const val TAG = "KnownSystemAnr"
    private val knownTitles = setOf("Pixel Launcher isn't responding", "Messages isn't responding")

    private val enabled: Boolean
        get() = InstrumentationRegistry.getArguments().getString("gdeiEmulator") == "true"

    /** Returns true when a known dialog was found and closed (or is still closing). */
    fun dismissIfPresent(): Boolean {
        if (!enabled) return false
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        val info = automation.serviceInfo
        val required = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
            AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
            AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        if (info.flags and required != required) {
            info.flags = info.flags or required
            automation.serviceInfo = info
        }
        val roots = buildList {
            automation.windows.sortedByDescending { it.layer }.forEach { window ->
                window.root?.let { add(it) }
            }
            automation.rootInActiveWindow?.let { add(it) }
        }.distinctBy { it.windowId }
        for (dialog in roots) {
            val title = walk(dialog).firstOrNull { node ->
                node.packageName?.toString() == "android" && node.viewIdResourceName == "android:id/alertTitle"
            }?.text?.toString() ?: continue
            if (title !in knownTitles) continue
            val close = walk(dialog).firstOrNull { node ->
                node.packageName?.toString() == "android" && node.viewIdResourceName == "android:id/aerr_close"
            } ?: return false
            val closed = clickNodeOrParent(close)
            Log.w(TAG, "Known system ANR title=$title closeClicked=$closed")
            return closed
        }
        return false
    }

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
}
