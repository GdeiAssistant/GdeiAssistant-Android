package cn.gdeiassistant.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

/** Window width buckets for adaptive layout. Breakpoints match androidx WindowSizeClass. */
enum class GdeiWindowWidthClass { Compact, Medium, Expanded }

@Composable
fun rememberGdeiWindowWidthClass(): GdeiWindowWidthClass {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    return when {
        screenWidthDp < 600 -> GdeiWindowWidthClass.Compact
        screenWidthDp < 840 -> GdeiWindowWidthClass.Medium
        else -> GdeiWindowWidthClass.Expanded
    }
}
