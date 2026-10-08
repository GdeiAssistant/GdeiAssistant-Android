package cn.gdeiassistant.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.unit.IntOffset

/** 2.0 全站弹簧动效，禁止 tween（按压反馈 0.12s 为设计规范例外，见 [pressDurationMs]） */
object GdeiMotion {
    val stiffness = Spring.StiffnessMediumLow
    val damping = Spring.DampingRatioNoBouncy

    /** Press feedback: controls settle at scale 0.98 within 0.12s. */
    val pressScaleTarget = 0.98f
    val pressDurationMs = 120

    val defaultSpring = spring<Float>(
        dampingRatio = damping,
        stiffness = stiffness
    )

    val responsiveSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val offsetSpring = spring<IntOffset>(
        dampingRatio = damping,
        stiffness = stiffness
    )
}
