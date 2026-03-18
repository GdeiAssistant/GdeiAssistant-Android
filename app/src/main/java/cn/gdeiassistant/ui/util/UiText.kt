package cn.gdeiassistant.ui.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat

/**
 * 统一 UI 文案类型，解决 ViewModel 无法持有 Context 做 i18n 的问题。
 * ViewModel 暴露 UiText，Compose 层通过 [asString] 解析为 String。
 */
sealed class UiText {

    /** 动态字符串（如服务端返回的 message） */
    data class DynamicString(val value: String) : UiText()

    /** 字符串资源 ID，由 UI 层 resolve */
    data class StringResource(val resId: Int) : UiText()
}

/**
 * 在 Composable 中将 [UiText] 解析为 [String]。
 */
@Composable
fun UiText.asString(): String = when (this) {
    is UiText.DynamicString -> value
    is UiText.StringResource -> stringResource(resId)
}

fun UiText.resolve(context: Context): String = when (this) {
    is UiText.DynamicString -> value
    is UiText.StringResource -> ContextCompat.getString(context, resId)
}
