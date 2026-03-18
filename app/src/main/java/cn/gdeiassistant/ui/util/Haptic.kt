package cn.gdeiassistant.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/** 2.0 关键操作触觉反馈：成功、切换学期等（Compose 标准 API） */
@Composable
fun PerformSuccessHaptic() {
    val haptic = LocalHapticFeedback.current
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
}
